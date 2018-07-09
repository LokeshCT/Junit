package com.bt.rsqe.ape.source.scheduler;

import com.bt.cqm.client.SACAvailabilityCheckerClient;
import com.bt.rsqe.ape.config.SACRequestReSubmitConfig;
import com.bt.rsqe.ape.config.SchedulerConfig;
import com.bt.rsqe.ape.dto.sac.SacBulkInputDTO;
import com.bt.rsqe.ape.dto.sac.SacBulkUploadStatus;
import com.bt.rsqe.ape.dto.sac.SacEmailType;
import com.bt.rsqe.ape.repository.entities.SacBulkUploadEntity;
import com.bt.rsqe.ape.util.DateFormatEnum;
import com.bt.rsqe.persistence.JPAPersistenceManager;
import com.bt.rsqe.persistence.JPATransactionUnit;
import com.bt.rsqe.persistence.JPATransactionalContext;
import com.bt.rsqe.utils.AssertObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Query;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * Created with IntelliJ IDEA.
 * User: 608026723
 * Date: 15/10/15
 * Time: 15:34
 * To change this template use File | Settings | File Templates.
 */
public class SacServerStartupHouseKeepingTask implements Runnable {
    private static Logger LOG = LoggerFactory.getLogger(SacServerStartupHouseKeepingTask.class);
    private SACAvailabilityCheckerClient sacAvailabilityCheckerClient;
    private JPATransactionalContext jpaTransactionalContext;
    private boolean rescheduleEnabled = false;

    public SacServerStartupHouseKeepingTask(SACAvailabilityCheckerClient sacAvailabilityCheckerClient, JPATransactionalContext apeQrefJPARepository, SchedulerConfig schedConfig) {
        this.sacAvailabilityCheckerClient = sacAvailabilityCheckerClient;
        this.jpaTransactionalContext = apeQrefJPARepository;
        rescheduleEnabled = Boolean.parseBoolean(schedConfig.getSACRequestReSubmitConfig(SACRequestReSubmitConfig.RESUBMIT_INTERVAL).getRescheduleOnRestart());
    }

    @Override
    public void run() {
        try {
            LOG.info("Executing SAC Server Startup cleanse job .");

            List<SacBulkInputDTO> sacUploadsBeyond24Hrs = getAllInProcessingSacUploadsBeyond24Hrs();
            if (sacUploadsBeyond24Hrs != null) {
                LOG.info(String.format("Identified [%s] SAC Upload records that passed 24 hours.", sacUploadsBeyond24Hrs.size()));
                int recordCount = 0;
                for (SacBulkInputDTO sacBulkUploadEntity : sacUploadsBeyond24Hrs) {
                    LOG.info(String.format("Going to process completion task for records that passed 24 hours. Record [%s] - File Name [%s] ...", ++recordCount, sacBulkUploadEntity.getFileName()));
                    completeTask(sacBulkUploadEntity);
                }
                LOG.info("SAC Automated Report Generation Job completed !!");
            }

            if (rescheduleEnabled) {
                List<SacBulkInputDTO> sacUploadsWithin24Hrs = getAllInProcessingSacUploadsWithin24Hrs();
                if (sacUploadsWithin24Hrs != null && sacUploadsWithin24Hrs.size() > 0) {
                    LOG.info(String.format("Identified [%s] SAC records that are uploaded within 24 hours and are in PROCESSING state on server startup.", sacUploadsWithin24Hrs.size()));
                    for (SacBulkInputDTO sacBulkInputDTO : sacUploadsWithin24Hrs) {
                        LOG.info(String.format("Going to reschedule the APE response tracking for SAC records that are in PROCESSING state on server startup.  File Name [%s] ...", sacBulkInputDTO.getFileName()));

                        Calendar finalReportGenTime = Calendar.getInstance();
                        finalReportGenTime.setTime(sacBulkInputDTO.getCreateDateTime());
                        finalReportGenTime.set(Calendar.HOUR, finalReportGenTime.get(Calendar.HOUR) + 24);

                        Long now = new Date().getTime();

                        Long timeLeftInMinForReportGen = (finalReportGenTime.getTimeInMillis() - now)/(60*1000);
                        int _8_Hrs = 8*60;
                        if(timeLeftInMinForReportGen<_8_Hrs){
                            SacScheduler.trackApeResponse(sacBulkInputDTO.getFileName(), timeLeftInMinForReportGen.intValue()+1,TimeUnit.MINUTES);
                        }else{
                            SacScheduler.trackApeResponse(sacBulkInputDTO.getFileName());
                        }
                    }
                }
            }

        } catch (Exception ex) {
            LOG.warn("Failed while executing SAC Server Startup cleanse job !! ", ex);
        }
    }

    private void completeTask(SacBulkInputDTO bulkInputDTO) {
        if (isAnyAvailabilityCheckComplete(bulkInputDTO.getFileName())) {
            bulkInputDTO.setSystem("SAC");
            LOG.info(String.format("Server StartUp Job :: Going to Generate Report for file - [%s] ...", bulkInputDTO.getFileName()));
            sacAvailabilityCheckerClient.generateAvailabilityReport(bulkInputDTO);
        } else {
            bulkInputDTO.setAvailabilityStatus(SacBulkUploadStatus.FAILED.getStatus());
            updateSacBulkUpload(bulkInputDTO);
            sacAvailabilityCheckerClient.sendEmailNotification(bulkInputDTO, SacEmailType.REPORT_GENERATION_FAILURE);
            LOG.info(String.format("Server StartUp Job :: Marked the file - [%s] as FAILED !!", bulkInputDTO.getFileName()));
        }
    }

    private List<SacBulkInputDTO> getAllInProcessingSacUploadsBeyond24Hrs() {
        final List<SacBulkInputDTO> sacBulkInputDTOs = new ArrayList<SacBulkInputDTO>();
        jpaTransactionalContext.execute(
            new JPATransactionUnit() {
                @Override
                public void execute(JPAPersistenceManager connection) {
                    Calendar now = Calendar.getInstance();
                    now.setTime(new Date());
                    now.set(Calendar.HOUR, now.get(Calendar.HOUR) - 24);
                    String last24HrDateTime = DateFormatEnum.SAC_DB_DATE_FORMAT.getValue().format(now.getTime());
                    String hostName = null;
                    Query query = null;
                    try {
                        hostName = InetAddress.getLocalHost().getHostName();
                    } catch (UnknownHostException e) {
                        LOG.warn("Couldn't get HOST Name ", e);
                    }

                    if (hostName == null) {
                        query = connection.entityManager().createNativeQuery("select * from SAC_BULK_UPLOAD " +
                                                                             "where " +
                                                                             "AVAILABILITY_STATUS = 'PROCESSING' " +
                                                                             "and create_datetime < to_date(:last24HrDateTime,'DD/MM/YYYY HH24:MI:SS')"
                            , SacBulkUploadEntity.class);
                        query.setParameter("last24HrDateTime", last24HrDateTime);

                    } else {
                        query = connection.entityManager().createNativeQuery("select * from SAC_BULK_UPLOAD " +
                                                                             "where " +
                                                                             "AVAILABILITY_STATUS = 'PROCESSING' " +
                                                                             "and create_datetime < to_date(:last24HrDateTime,'DD/MM/YYYY HH24:MI:SS')" +
                                                                             "and host_name =:hostName"
                            , SacBulkUploadEntity.class);
                        query.setParameter("last24HrDateTime", last24HrDateTime);
                        query.setParameter("hostName", hostName);
                    }

                    List<SacBulkUploadEntity> resultList = query.getResultList();
                    if (resultList != null) {
                        for (SacBulkUploadEntity sacBulkUploadEntity : resultList) {
                            sacBulkInputDTOs.add(sacBulkUploadEntity.toDto());
                        }
                    }
                }
            }
        );


        return sacBulkInputDTOs;
    }

    private List<SacBulkInputDTO> getAllInProcessingSacUploadsWithin24Hrs() {
        final List<SacBulkInputDTO> sacBulkInputDTOs = new ArrayList<SacBulkInputDTO>();
        jpaTransactionalContext.execute(
            new JPATransactionUnit() {
                @Override
                public void execute(JPAPersistenceManager connection) {
                    Calendar now = Calendar.getInstance();
                    now.setTime(new Date());
                    now.set(Calendar.HOUR, now.get(Calendar.HOUR) - 24);
                    String last24HrDateTime = DateFormatEnum.SAC_DB_DATE_FORMAT.getValue().format(now.getTime());
                    String hostName = null;
                    Query query = null;
                    try {
                        hostName = InetAddress.getLocalHost().getHostName();
                    } catch (UnknownHostException e) {
                        LOG.warn("Couldn't get HOST Name ", e);
                    }

                    if (hostName == null) {
                        query = connection.entityManager().createNativeQuery("select * from SAC_BULK_UPLOAD " +
                                                                             "where " +
                                                                             "AVAILABILITY_STATUS = 'PROCESSING' " +
                                                                             "and create_datetime > to_date(:last24HrDateTime,'DD/MM/YYYY HH24:MI:SS')"
                            , SacBulkUploadEntity.class);
                        query.setParameter("last24HrDateTime", last24HrDateTime);
                    } else {
                        query = connection.entityManager().createNativeQuery("select * from SAC_BULK_UPLOAD " +
                                                                             "where " +
                                                                             "AVAILABILITY_STATUS = 'PROCESSING' " +
                                                                             "and create_datetime > to_date(:last24HrDateTime,'DD/MM/YYYY HH24:MI:SS')" +
                                                                             "and host_name =:hostName"
                            , SacBulkUploadEntity.class);
                        query.setParameter("last24HrDateTime", last24HrDateTime);
                        query.setParameter("hostName", hostName);
                    }

                    List<SacBulkUploadEntity> resultList = query.getResultList();
                    if (resultList != null) {
                        for (SacBulkUploadEntity sacBulkUploadEntity : resultList) {
                            sacBulkInputDTOs.add(sacBulkUploadEntity.toShallowDto());
                        }
                    }
                }
            }
        );


        return sacBulkInputDTOs;
    }

    private boolean isAnyAvailabilityCheckComplete(final String fileName) {
        final boolean[] hasCompletedRecordArr = new boolean[1];
        jpaTransactionalContext.execute(
            new JPATransactionUnit() {
                @Override
                public void execute(JPAPersistenceManager connection) {
                    boolean hasCompletedRecord = false;
                    Query query = connection.entityManager().createNativeQuery(" select count(*) from sac_site_requests r, sac_supplier_availability a where " +
                                                                               " r.site_id = a.site_id " +
                                                                               " and a.availability_status is not null" +
                                                                               " and r.file_name =:fileName");
                    query.setParameter("fileName", fileName);
                    BigDecimal count = (BigDecimal) query.getSingleResult();

                    if (count == null || (count != null && count.intValue() < 1)) {
                        hasCompletedRecord = false;
                    } else {
                        hasCompletedRecord = true;
                    }

                    hasCompletedRecordArr[0] = hasCompletedRecord;
                }
            }
        );

        return hasCompletedRecordArr[0];
    }

    private void updateSacBulkUpload(final SacBulkInputDTO bulkInputDTO) {
        jpaTransactionalContext.execute(
            new JPATransactionUnit() {
                @Override
                public void execute(JPAPersistenceManager connection) {
                    SacBulkUploadEntity bulkUploadEntity = connection.get(SacBulkUploadEntity.class, bulkInputDTO.getFileName());
                    bulkUploadEntity.setSharePointFailDocId(bulkInputDTO.getSharePointFailDocId());
                    bulkUploadEntity.setSharePointOrgDocId(bulkInputDTO.getSharePointOrgDocId());
                    bulkUploadEntity.setSharePointResultDocId(bulkInputDTO.getSharePointResultDocId());
                    bulkUploadEntity.setValidationStatus(bulkInputDTO.getValidationStatus());
                    bulkUploadEntity.setAvailabilityStatus(bulkInputDTO.getAvailabilityStatus());
                    bulkUploadEntity.setUpdateDate(new Timestamp(new Date().getTime()));
                    bulkUploadEntity.setUpdateUser(bulkInputDTO.getUserId());
                    connection.saveAndCommit(bulkUploadEntity);
                }
            }
        );

    }
}
