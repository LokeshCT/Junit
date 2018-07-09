package com.bt.rsqe.ape.source.scheduler;

import com.bt.cqm.client.SACAvailabilityCheckerClient;
import com.bt.rsqe.ape.SupplierProductResourceClient;
import com.bt.rsqe.ape.SupplierProductResourceHandler;
import com.bt.rsqe.ape.dto.sac.SacBulkInputDTO;
import com.bt.rsqe.ape.dto.sac.SacBulkUploadStatus;
import com.bt.rsqe.ape.dto.sac.SacEmailType;
import com.bt.rsqe.ape.dto.sac.SacSiteDTO;
import com.bt.rsqe.ape.repository.APEQrefJPARepository;
import com.bt.rsqe.ape.repository.entities.SacBulkUploadEntity;
import com.bt.rsqe.ape.repository.entities.SacRequestEntity;
import com.bt.rsqe.ape.repository.entities.SacSupplierProdAvailEntity;
import com.bt.rsqe.ape.repository.entities.SupplierCheckLogEntity;
import com.bt.rsqe.ape.util.DateFormatEnum;
import com.bt.rsqe.persistence.JPAEntityManagerProvider;
import com.bt.rsqe.persistence.JPAPersistenceManager;
import com.bt.rsqe.persistence.JPATransactionUnit;
import com.bt.rsqe.persistence.JPATransactionalContext;
import com.bt.rsqe.persistence.PersistenceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Query;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: 608026723
 * Date: 12/10/15
 * Time: 14:11
 * To change this template use File | Settings | File Templates.
 */
public class SacApeResponseCheckTask implements Runnable {
    private String fileName;
    private SACAvailabilityCheckerClient sacAvailabilityCheckerClient;
    private SupplierProductResourceClient supplierProductResourceClient;
    private JPAEntityManagerProvider provider;
    private final JPATransactionalContext persistence;
    private final Integer iterationCount;
    private final Integer triggerInterval;
    private static Logger LOG = LoggerFactory.getLogger(SacApeResponseCheckTask.class);

    public SacApeResponseCheckTask(String fileName, Integer iterationCount,Integer triggerInterval, SACAvailabilityCheckerClient sacAvailabilityCheckerClient, SupplierProductResourceClient supplierProductResourceClient, JPAEntityManagerProvider provider) {
        this.fileName = fileName;
        this.iterationCount = iterationCount;
        this.triggerInterval = triggerInterval;
        this.sacAvailabilityCheckerClient = sacAvailabilityCheckerClient;
        this.supplierProductResourceClient = supplierProductResourceClient;
        this.provider = provider;
        this.persistence = new JPATransactionalContext(provider);
    }

    @Override
    public void run() {
        SacBulkInputDTO sacBulkUploadDTO = null;
        try {
            LOG.info(String.format("SAC Ape Response check , follow up Job called : [%s]", fileName));
            sacBulkUploadDTO = getSacBulkUploadDTO(fileName, false);

            if (SacBulkUploadStatus.PROCESSING.getStatus().equals(sacBulkUploadDTO.getAvailabilityStatus())) {
                Long itrCount = sacBulkUploadDTO.getItrCount();
                itrCount = (itrCount == null) ? 0 : itrCount;

                if (!hasPassed24Hrs(fileName)) {

                    List<SacSiteDTO> noRespRequestList = getNoResponseRequests(fileName);
                    if (noRespRequestList != null && noRespRequestList.size() > 0) {
                        if (itrCount < iterationCount) {

                            LOG.info(String.format("[%s] Number of Sites send for reprocessing - %s", fileName, noRespRequestList.size()));
                            sacBulkUploadDTO.setSites(noRespRequestList);
                            Future<Response> futureTask = supplierProductResourceClient.initiateSacSupplierAvailabilityCheckAsynch(sacBulkUploadDTO);

                            while (!futureTask.isDone()) {
                                Thread.sleep(30000); // Sleep for 30 sec
                            }

                            sacBulkUploadDTO.setItrCount(++itrCount);
                            updateSacBulkUpload(sacBulkUploadDTO);

                            Calendar finalReportGenTime = Calendar.getInstance();
                            finalReportGenTime.setTime(sacBulkUploadDTO.getCreateDateTime());
                            finalReportGenTime.set(Calendar.HOUR, finalReportGenTime.get(Calendar.HOUR) + 24);

                            Long now = new Date().getTime();

                            Long timeLeftInMinForReportGen = (finalReportGenTime.getTimeInMillis() - now)/(60*1000);

                            if(timeLeftInMinForReportGen <(triggerInterval*60)){
                                LOG.info(String.format("File [%s] :: [%s] minutes left to generate report",fileName,timeLeftInMinForReportGen.intValue()));
                                SacScheduler.trackApeResponse(sacBulkUploadDTO.getFileName(),timeLeftInMinForReportGen.intValue()+1, TimeUnit.MINUTES);
                            }else{
                                LOG.info(String.format("File [%s] :: Scheduling for next checkpoint after %s Hours",fileName,triggerInterval));
                                SacScheduler.trackApeResponse(sacBulkUploadDTO.getFileName());
                            }

                        } else {
                            completeTask(sacBulkUploadDTO.getFileName());
                        }
                    } else {
                        completeTask(sacBulkUploadDTO.getFileName());
                    }
                } else {
                    completeTask(sacBulkUploadDTO.getFileName());
                }
            }
            LOG.info(String.format("SAC Ape Response check , follow up Job End : [%s]", fileName));
        } catch (Exception ex) {
            LOG.error("Failed while executing followup Job for SAC APE Response Check", ex);
            if (sacBulkUploadDTO != null) {
                completeTask(sacBulkUploadDTO.getFileName());
                LOG.info(String.format("Marked the file - [%s] to completion", sacBulkUploadDTO.getFileName()));
            }
        }
    }


    private void completeTask(String fileName) {
        if (isAnyAvailabilityCheckComplete(fileName)) {
            SacBulkInputDTO sacBulkInputDTO = getSacBulkUploadDTO(fileName, true);
            sacBulkInputDTO.setSystem("SAC");
            LOG.info(String.format("Going to Generate Report for file - [%s] ...", fileName));
            sacAvailabilityCheckerClient.generateAvailabilityReport(sacBulkInputDTO);
        } else {
            SacBulkInputDTO sacBulkInputDTO = getSacBulkUploadDTO(fileName, true);
            sacBulkInputDTO.setAvailabilityStatus(SacBulkUploadStatus.FAILED.getStatus());
            updateSacBulkUpload(sacBulkInputDTO);
            sacAvailabilityCheckerClient.sendEmailNotification(sacBulkInputDTO, SacEmailType.REPORT_GENERATION_FAILURE);
            LOG.info(String.format("Marked the file - [%s] as FAILED !!", fileName));
        }

    }

    private SacBulkInputDTO getSacBulkUploadDTO(final String fileName, final boolean shouldFetchChildTables) {
        SacBulkInputDTO sacBulkInputDTO = null;
        try {
            Query query = provider.entityManager().createQuery("select b from SacBulkUploadEntity b where b.fileName = :fileName", SacBulkUploadEntity.class);
            query.setParameter("fileName", fileName);
            SacBulkUploadEntity sacBulkUploadEntity = (SacBulkUploadEntity) query.getSingleResult();

            if (sacBulkUploadEntity != null) {
                if (shouldFetchChildTables) {
                    sacBulkInputDTO = sacBulkUploadEntity.toDto();
                } else {
                    sacBulkInputDTO = sacBulkUploadEntity.toShallowDto();
                }
            }

        } catch (Exception ex) {
            LOG.error("Failed to fetch SacBulkUploadEntity for File Name =" + fileName, ex);
        }

        return sacBulkInputDTO;
    }

    private List<SacSiteDTO> getNoResponseRequests(final String fileName) {
        List<SacSiteDTO> sacSiteDTOs = null;
        try {

            Query query = provider.entityManager().createNativeQuery(" select distinct r.* from sac_site_requests r , sac_supplier_availability a where " +
                                                                     " r.site_id = a.site_id " +
                                                                     " and a.availability_status is null " +
                                                                     " and (a.status not in ('Completed','Failed') or a.status is null)" +
                                                                     " and r.file_name=:fileName ", SacRequestEntity.class);

            //Query query = provider.entityManager().createQuery(" select distinct(r) from SacRequestEntity r left join r.suppliers  p with (p.availStatus = null and  coalesce(p.status,'TIMEOUT') !='Success' and p.requestEntity.fileName = :fileName) where r.fileName = :fileName ", SacRequestEntity.class);

            query.setParameter("fileName", fileName);


            List<SacRequestEntity> sacRequestEntities = query.getResultList();

            if (sacRequestEntities != null) {
                sacSiteDTOs = new ArrayList<>();
                if (sacRequestEntities != null && sacRequestEntities.size() > 0) {
                    for (SacRequestEntity sacRequestEntity : sacRequestEntities) {
                        provider.entityManager().detach(sacRequestEntity);
                        Query availQuery = provider.entityManager().createNativeQuery(" select distinct a.* from sac_supplier_availability a where " +
                                                                                      " a.availability_status is null " +
                                                                                      " and (a.status not in ('Completed','Failed') or a.status is null) " +
                                                                                      " and a.site_id=:siteId ", SacSupplierProdAvailEntity.class);

                        //Query query = provider.entityManager().createQuery(" select distinct(r) from SacRequestEntity r left join r.suppliers  p with (p.availStatus = null and  coalesce(p.status,'TIMEOUT') !='Success' and p.requestEntity.fileName = :fileName) where r.fileName = :fileName ", SacRequestEntity.class);

                        availQuery.setParameter("siteId", sacRequestEntity.getSiteId());
                        availQuery.setHint(PersistenceManager.FETCH_SIZE, 50);

                        List<SacSupplierProdAvailEntity> sacSuppAvailEntities = availQuery.getResultList();
                        sacRequestEntity.setSuppliers(sacSuppAvailEntities);
                        sacSiteDTOs.add(sacRequestEntity.toDto());
                    }
                }
            }


        } catch (Exception ex) {
            LOG.error("Failed to fetch getNoResponseRequests for File Name =" + fileName, ex);
        }

        return sacSiteDTOs;
    }

    private boolean hasPassed24Hrs(final String fileName) {
        boolean hasPassed24Hrs = false;
        try {
            Calendar now = Calendar.getInstance();
            now.setTime(new Date());
            now.set(Calendar.HOUR, now.get(Calendar.HOUR) - 24);
            String last24HrDateTime = DateFormatEnum.SAC_DB_DATE_FORMAT.getValue().format(now.getTime());

            Query query = provider.entityManager().createNativeQuery("select count(*) from SAC_BULK_UPLOAD " +
                                                                     "where " +
                                                                     " create_datetime < to_date(:last24HrDateTime,'DD/MM/YYYY HH24:MI:SS')" +
                                                                     " and file_name =:fileName ");
            query.setParameter("last24HrDateTime", last24HrDateTime);
            query.setParameter("fileName", fileName);

            BigDecimal count = (BigDecimal) query.getSingleResult();
            if (count == null || (count != null && count.intValue() < 1)) {
                hasPassed24Hrs = false;
                LOG.info(String.format(" File [%s] yet to cross 24hrs since creation !!", fileName));
            } else {
                hasPassed24Hrs = true;
                LOG.info(String.format(" File [%s] crossed 24hrs since creation !!", fileName));
            }

        } catch (Exception ex) {
            LOG.error("Failed to Check 24 Hr Test for File Name =" + fileName, ex);
            hasPassed24Hrs = false;
        }

        return hasPassed24Hrs;
    }

    private void updateSacBulkUpload(final SacBulkInputDTO bulkInputDTO) {
        persistence.execute(new JPATransactionUnit() {
            @Override
            public void execute(JPAPersistenceManager connection) {
                APEQrefJPARepository repository = new APEQrefJPARepository(connection);
                repository.updateSacBulkUpload(bulkInputDTO);
            }
        });
    }

    private boolean isAnyAvailabilityCheckComplete(final String fileName) {
        boolean hasCompletedRecord = false;
        Query query = provider.entityManager().createNativeQuery(" select count(*) from sac_site_requests r, sac_supplier_availability a where " +
                                                                 " r.site_id = a.site_id " +
                                                                 " and a.availability_status is not null " +
                                                                 " and r.file_name =:fileName");
        query.setParameter("fileName", fileName);
        BigDecimal count = (BigDecimal) query.getSingleResult();

        if (count == null || (count != null && count.intValue() < 1)) {
            hasCompletedRecord = false;
        } else {
            hasCompletedRecord = true;
        }

        return hasCompletedRecord;
    }
}
