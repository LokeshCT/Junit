package com.bt.rsqe.ape.repository;

import com.bt.rsqe.ape.dto.ApeQref;
import com.bt.rsqe.ape.dto.ApeQrefAttributeDetail;
import com.bt.rsqe.ape.dto.ApeQrefError;
import com.bt.rsqe.ape.dto.ApeQrefIdentifier;
import com.bt.rsqe.ape.dto.SiteAvailabilityStatus;
import com.bt.rsqe.ape.dto.sac.SacApeStatus;
import com.bt.rsqe.ape.dto.sac.SacBulkInputDTO;
import com.bt.rsqe.ape.dto.sac.SacBulkUploadStatus;
import com.bt.rsqe.ape.dto.sac.SacSiteDTO;
import com.bt.rsqe.ape.dto.sac.SacSupplierProdAvailDTO;
import com.bt.rsqe.ape.repository.entities.AccessStaffCommentEntity;
import com.bt.rsqe.ape.repository.entities.AccessUserCommentsEntity;
import com.bt.rsqe.ape.repository.entities.ApeQrefDetailEntity;
import com.bt.rsqe.ape.repository.entities.ApeQrefErrorEntity;
import com.bt.rsqe.ape.repository.entities.ApeRequestEntity;
import com.bt.rsqe.ape.repository.entities.AvailabilityParamEntity;
import com.bt.rsqe.ape.repository.entities.AvailabilityRequestQueue;
import com.bt.rsqe.ape.repository.entities.AvailabilitySetEntity;
import com.bt.rsqe.ape.repository.entities.DslEfmSupportedCountriesEntity;
import com.bt.rsqe.ape.repository.entities.OnnetAvailabilityEntity;
import com.bt.rsqe.ape.repository.entities.OnnetBuildingEntity;
import com.bt.rsqe.ape.repository.entities.OnnetBuildingsWithEFMEntity;
import com.bt.rsqe.ape.repository.entities.SacBulkUploadEntity;
import com.bt.rsqe.ape.repository.entities.SacRequestEntity;
import com.bt.rsqe.ape.repository.entities.SacSupplierProdAvailEntity;
import com.bt.rsqe.ape.repository.entities.SacSupplierProdMasterEntity;
import com.bt.rsqe.ape.repository.entities.SacSupplierProdMasterPK;
import com.bt.rsqe.ape.repository.entities.SupplierCheckApeRequestEntity;
import com.bt.rsqe.ape.repository.entities.SupplierCheckClientRequestEntity;
import com.bt.rsqe.ape.repository.entities.SupplierCheckLogEntity;
import com.bt.rsqe.ape.repository.entities.SupplierProductEntity;
import com.bt.rsqe.ape.repository.entities.SupplierRequestSiteEntity;
import com.bt.rsqe.ape.repository.entities.SupplierSiteEntity;
import com.bt.rsqe.ape.util.DateFormatEnum;
import com.bt.rsqe.dto.SearchCriteria;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.bt.rsqe.persistence.JPAPersistenceManager;
import com.bt.rsqe.persistence.PersistenceManager;
import com.bt.rsqe.utils.AssertObject;
import com.bt.rsqe.web.rest.exception.ResourceNotFoundException;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.hibernate.jdbc.AbstractWork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.bt.rsqe.ape.constants.SupplierProductConstants.*;
import static com.bt.rsqe.ape.dto.SupplierStatus.*;
import static com.bt.rsqe.ape.source.SupplierProductHelper.*;
import static com.bt.rsqe.customerinventory.dto.AvailabilityType.*;
import static com.bt.rsqe.utils.AssertObject.isNull;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Sets.*;
import static net.logstash.logback.encoder.org.apache.commons.lang.StringUtils.*;

public class APEQrefJPARepository implements APEQrefRepository {
    private static final String QREF_ID = "qrefId";
    private static final String REQUEST_ID = "requestId";
    private APEQrefRepositoryLogger LOG = LogFactory.createDefaultLogger(APEQrefRepositoryLogger.class);
    private static final Logger LOGGER = LoggerFactory.getLogger(APEQrefJPARepository.class);

    private JPAPersistenceManager persistence;

    public APEQrefJPARepository(JPAPersistenceManager persistence) {
        this.persistence = persistence;
    }

    @Override
    /**
     * Returns the APE request entity for the given unique id, excluding any that are in a CANCELLED state
     */
    public ApeRequestEntity getAPERequestByUniqueId(String uniqueId) {
        LOG.loadingRequest(uniqueId);
        ApeRequestEntity requestEntity = null;
        try {
            requestEntity = persistence.entityManager()
                                       .createQuery("SELECT entity FROM ApeRequestEntity entity WHERE entity.uniqueId=:uniqueId AND entity.status NOT IN ('CANCELLED')", ApeRequestEntity.class)
                                       .setParameter("uniqueId", uniqueId)
                                       .getSingleResult();
        } catch (NoResultException nre) {
            //do nothing
        }

        return requestEntity;
    }

    @Override
    public List<ApeQrefIdentifier> getQrefIdentifiers(String uniqueId, SearchCriteria searchCriteria) {
        List<ApeQrefIdentifier> qrefIdentifiers = newArrayList();
        try {
            List<ApeQrefDetailEntity> result = persistence.entityManager()
                                                          .createQuery("select qrefDetail from ApeRequestEntity qrefReq, ApeQrefDetailEntity qrefDetail where qrefReq.uniqueId = :uniqueId " +
                                                                       "AND qrefReq.status NOT IN ('CANCELLED') " +
                                                                       "AND qrefReq.requestId = qrefDetail.requestId " +
                                                                       "and qrefDetail.attributeName = 'QREF' " +
                                                                       "order by qrefDetail.sequence", ApeQrefDetailEntity.class)
                                                          .setParameter("uniqueId", uniqueId)
                                                          .getResultList();

            qrefIdentifiers.addAll(transform(result, new Function<ApeQrefDetailEntity, ApeQrefIdentifier>() {
                @Override
                public ApeQrefIdentifier apply(ApeQrefDetailEntity input) {
                    return new ApeQrefIdentifier(input.getQrefId(), input.getAttributeValue());
                }
            }));

            if (null != searchCriteria && searchCriteria.hasSearchTerm()) {
                Set<Integer> pairsKept = newHashSet();
                Set<Integer> pairsLost = newHashSet();
                final Iterator<ApeQrefIdentifier> qrefIdentifierIterator = qrefIdentifiers.iterator();
                while (qrefIdentifierIterator.hasNext()) {
                    final ApeQrefIdentifier identifier = qrefIdentifierIterator.next();
                    final ApeQref qref = getApeQref(identifier.value());
                    final int pairId = qref.getPairId();

                    // don't remove resilient pairs when we have a match on one of the Legs
                    if (pairsKept.contains(pairId)) {
                        continue;
                    }

                    Set<String> allValues = newHashSet(Iterables.transform(qref.getAttributes(), new Function<ApeQrefAttributeDetail, String>() {
                        @Override
                        public String apply(ApeQrefAttributeDetail input) {
                            return input.getAttributeValue();
                        }
                    }));

                    if (pairsLost.contains(pairId) || !searchCriteria.match(allValues)) {
                        qrefIdentifierIterator.remove();
                        recordPair(pairsLost, pairId);
                    } else {
                        recordPair(pairsKept, pairId);
                    }
                }
            }
        } catch (NoResultException nre) {
            //do nothing
        }

        return qrefIdentifiers;
    }

    @Override
    public void save(List<DslEfmSupportedCountriesEntity> entities) {
        int i = 0;
        DslEfmSupportedCountriesEntity toupdate = null;
        for (DslEfmSupportedCountriesEntity entity : entities) {
            toupdate = persistence.entityManager().find(DslEfmSupportedCountriesEntity.class, entity.getIsoCode());
            toupdate.setUpdatedOn(timestamp());
            toupdate.setDslEfmSupported(entity.getDslEfmSupported());
            persistence.entityManager().merge(toupdate);
            if (i % 40 == 0) {
                persistence.entityManager().flush();
                persistence.entityManager().clear();
            }
            i++;
        }
    }

    @Override
    public void saveAvailabilityRequestList(List<AvailabilityRequestQueue> requests) {
        try {
            for (AvailabilityRequestQueue request : requests) {
                persistence.saveAndCommit(request);
            }
        } catch (Exception e) {
            LOG.error(e);
        }
    }

    @Override
    public void saveClientRequestList(List<SupplierCheckClientRequestEntity> requests) {
        int i = 0;
        for (SupplierCheckClientRequestEntity request : requests) {
            persistence.entityManager().merge(request);
            if (i % 40 == 0) {
                persistence.entityManager().flush();
                persistence.entityManager().clear();
            }
            i++;
        }
    }

    @Override
    public void save(SupplierCheckLogEntity entity) {
        persistence.saveAndCommit(entity);
    }

    @Override
    public void save(Object entity) {
        persistence.saveAndCommit(entity);
    }

    @Override
    public void saveAlone(Object entity) {
        persistence.entityManager().merge(entity);
    }

    @Override
    public void save(SupplierCheckClientRequestEntity entity) {
        persistence.saveAndCommit(entity);
    }

    @Override
    public void save(SupplierSiteEntity entity) {
        persistence.saveAndCommit(entity);
    }

    @Override
    public void removeSiteEntity(SupplierSiteEntity entity) {
        persistence.remove(persistence.get(SupplierSiteEntity.class, entity.getSiteId()));
    }

    @Override
    public void save(SupplierProductEntity entity) {
        persistence.saveAndCommit(entity);
    }

    @Override
    public void save(AvailabilitySetEntity entity) {
        persistence.saveAndCommit(entity);
    }

    @Override
    public void save(AvailabilityParamEntity entity) {
        persistence.saveAndCommit(entity);
        ;
    }

    private void recordPair(Set<Integer> pairs, int pair) {
        if (pair > -1) {
            pairs.add(pair);
        }
    }

    @Override
    public void createApeRequest(SupplierCheckApeRequestEntity supplierCheckApeRequestEntity) {
        persistence.create(supplierCheckApeRequestEntity);
    }

    @Override
    public ApeRequestEntity getAPERequestByRequestId(String requestId) {
        LOG.loadingRequestByRequestId(requestId);
        ApeRequestEntity requestEntity = null;
        try {
            requestEntity = persistence.entityManager()
                                       .createQuery("SELECT entity FROM ApeRequestEntity entity WHERE entity.requestId=:requestId", ApeRequestEntity.class)
                                       .setParameter(REQUEST_ID, requestId)
                                       .getSingleResult();
        } catch (NoResultException nre) {
            //do nothing
        }

        return requestEntity;
    }

    @Override
    public List<ApeQrefDetailEntity> getAPEQrefsByUniqueId(String uniqueId) {
        LOG.loadingQRef(uniqueId);
        ApeRequestEntity apeRequest = getAPERequestByUniqueId(uniqueId);
        return apeRequest != null ? apeRequest.getApeQrefDetailsByRequestId() : Lists.<ApeQrefDetailEntity>newArrayList();
    }

    @Override
    public ApeQref getApeQref(String qrefId) {
        List<ApeQrefDetailEntity> results = persistence.entityManager()
                                                       .createQuery("SELECT entity FROM ApeQrefDetailEntity entity WHERE entity.qrefId=:qrefId", ApeQrefDetailEntity.class)
                                                       .setParameter(QREF_ID, qrefId)
                                                       .getResultList();


        if (!results.isEmpty()) {
            ApeQref apeQref = new ApeQref();
            apeQref.setQrefId(results.get(0).getQrefId());
            String requestId = results.get(0).getRequestId();
            apeQref.setRequestId(requestId);

            for (ApeQrefDetailEntity apeQrefDetailEntity : results) {
                apeQref.getAttributes().add(apeQrefDetailEntity.dto());
            }

            List<ApeQrefErrorEntity> apeQrefErrorEntities = getApeQrefErrors(qrefId);

            if (!com.bt.rsqe.utils.Lists.isNullOrEmpty(apeQrefErrorEntities)) {
                List<ApeQrefError> apeQrefErrors = Lists.transform(apeQrefErrorEntities, new Function<ApeQrefErrorEntity, ApeQrefError>() {
                    @Override
                    public ApeQrefError apply(@Nullable ApeQrefErrorEntity input) {
                        return input.toDto();
                    }
                });

                apeQref.getErrors().addAll(apeQrefErrors);
            }

            ApeRequestEntity apeRequestEntity = getAPERequestByRequestId(requestId);
            apeQref.setCurrency(apeRequestEntity.getCurrency());
            apeQref.setRequestAttributes(apeRequestEntity.requestAttributes());

            return apeQref;
        }

        throw new ResourceNotFoundException();
    }

    @Override
    public void deleteApeQref(String qrefId) {
        List<ApeQrefDetailEntity> results = persistence.entityManager()
                                                       .createQuery("SELECT entity FROM ApeQrefDetailEntity entity WHERE entity.qrefId=:qrefId", ApeQrefDetailEntity.class)
                                                       .setParameter(QREF_ID, qrefId)
                                                       .getResultList();
        if (!results.isEmpty()) {
            for (ApeQrefDetailEntity result : results) {
                persistence.remove(result);
            }
        } else {
            throw new ResourceNotFoundException();
        }
    }

    @Override
    public List<ApeQrefErrorEntity> getApeQrefErrors(String qrefId) {
        return persistence.entityManager()
                          .createQuery("SELECT entity FROM ApeQrefErrorEntity entity WHERE entity.qrefId=:qrefId", ApeQrefErrorEntity.class)
                          .setParameter(QREF_ID, qrefId)
                          .getResultList();
    }

    @Override
    public List<AccessStaffCommentEntity> getStaffComments(String qrefId) {
        return persistence.entityManager()
                          .createQuery("SELECT entity FROM AccessStaffCommentEntity entity WHERE entity.qrefId=:qrefId", AccessStaffCommentEntity.class)
                          .setParameter(QREF_ID, qrefId)
                          .getResultList();
    }

    @Override
    public List<String> getApeQrefId(String requiredId) {
        return persistence.entityManager()
                          .createQuery("SELECT entity.qrefId FROM ApeQrefDetailEntity entity WHERE entity.requestId=:requestId", String.class)
                          .setParameter(REQUEST_ID, requiredId)
                          .getResultList();
    }

    @Override
    public void save(ApeQrefDetailEntity apeQrefDetailEntity) {
        LOG.savingQRef(apeQrefDetailEntity.getQrefId(), apeQrefDetailEntity.getAttributeName(), apeQrefDetailEntity.getAttributeValue());
        persistence.save(apeQrefDetailEntity);
    }

    @Override
    public void save(ApeRequestEntity apeRequestEntity) {
        LOG.savingRequest(apeRequestEntity.getRequestId(), apeRequestEntity.getUniqueId());
        persistence.save(apeRequestEntity);
    }

    @Override
    public void save(AccessUserCommentsEntity accessUserCommentsEntity) {
        LOG.savingRequest(accessUserCommentsEntity.getQrefId(), accessUserCommentsEntity.getComment());
        persistence.save(accessUserCommentsEntity);
    }

    @Override
    public void save(ApeQrefErrorEntity apeQrefErrorEntity) {
        LOG.savingQrefError(apeQrefErrorEntity.getQrefId(), apeQrefErrorEntity.getErrorCode(), apeQrefErrorEntity.getErrorMsg());
        persistence.save(apeQrefErrorEntity);
    }

    @Override
    public void save(AccessStaffCommentEntity accessStaffCommentEntity) {
        LOG.savingStaffComment(accessStaffCommentEntity.getComment(), accessStaffCommentEntity.getQrefId());
        persistence.save(accessStaffCommentEntity);
    }

    @Override
    public List<AccessUserCommentsEntity> getUserCommentsForQrefId(String qrefId) {
        LOG.loadingComment(qrefId);
        return persistence.entityManager()
                .createQuery("SELECT entity FROM AccessUserCommentsEntity entity WHERE entity.qrefId=:qrefId order by entity.createdDate desc", AccessUserCommentsEntity.class)
                .setParameter("qrefId", qrefId)
                .getResultList();
    }

    @Override
    public Long getNextValOfSupplierCheckApeRequestId() throws Exception {
        String queryString = "select SCAR_ID.nextval from dual";
        BigDecimal nextSeq = null;
        try {
            Query query = persistence.entityManager().createNativeQuery(queryString);
            nextSeq = (BigDecimal) query.getSingleResult();
        } catch (Exception e) {
            throw new Exception("Couldn't generate sequence :" + e.getMessage());
        }
        return nextSeq.longValue();
    }

    @Override
    public Long getNextValOfSupplierCheckClientRequestId() throws Exception {
        String queryString = "select SCCR_ID.nextval from dual";
        BigDecimal nextSeq = null;
        try {
            Query query = persistence.entityManager().createNativeQuery(queryString);
            nextSeq = (BigDecimal) query.getSingleResult();
        } catch (Exception e) {
            throw new Exception("Couldn't generate sequence :" + e.getMessage());
        }
        return nextSeq.longValue();
    }

    @Override
    public List<SupplierSiteEntity> getSupplierSitesByCustomerId(Long customerId) throws Exception {
        List<SupplierSiteEntity> siteEntities = null;
        try {

            siteEntities = persistence.entityManager().createNativeQuery("select * from supplier_site where customer_id=" + customerId, SupplierSiteEntity.class).getResultList();
/*
            siteEntities = persistence.entityManager()
                                      .createQuery("SELECT entity FROM SupplierSiteEntity entity WHERE entity.customerId=:customerId")
                                      .setParameter("customerId", customerId)
                                      .getResultList();*/
        } catch (NoResultException e) {
            //do nothing
        }
        return siteEntities;
    }

    @Override
    public List<SiteAvailabilityStatus> getSiteAvailabilityStatus(Long customerId) throws Exception {
        List<SiteAvailabilityStatus> status = newArrayList();
        try {
            List<SupplierSiteEntity> siteEntityList = persistence.entityManager()
                                                                 .createNativeQuery("select * from supplier_site where customer_id=" + customerId, SupplierSiteEntity.class).getResultList();
            for (SupplierSiteEntity siteEntity : siteEntityList) {
                status.add(new SiteAvailabilityStatus(siteEntity.getSiteId(), siteEntity.getAvailabilityTypeId(), siteEntity.getAvailabilityTelephoneNumber()));
            }
        } catch (Exception e) {
            // do nothing
        }
        return status;
    }

    @Override
    public List<SupplierSiteEntity> getSupplierSitesBySiteId(List<Long> siteIds) throws Exception {
        List<SupplierSiteEntity> supplierSiteEntityList = newArrayList();
        if(siteIds.isEmpty())
            return supplierSiteEntityList;

        try{
            supplierSiteEntityList = persistence.entityManager()
                                                .createQuery("SELECT entity FROM SupplierSiteEntity entity WHERE entity.siteId IN (:siteIds)")
                                                .setParameter("siteIds", siteIds)
                                                .getResultList();
        }catch(Exception e){
            LOG.error(e);
        }

        return supplierSiteEntityList;
    }

    @Override
    public SupplierProductEntity getSupplierProductBySiteIdAndSpacId(Long siteId, String spacid) throws Exception {
        BigDecimal id = (BigDecimal) persistence.entityManager().createNativeQuery("select supp_prod_id from supplier_product where spac_id='" + spacid + "' and site_id=" + siteId).getSingleResult();
        return persistence.get(SupplierProductEntity.class, id.longValue());
    }

    public SupplierProductEntity getSupplierProductBySiteIdSupplierIdAndSpacId(Long siteId, String supplierId, String spacid) throws Exception {
        BigDecimal id = (BigDecimal) persistence.entityManager().createNativeQuery("select supp_prod_id from supplier_product where supplier_id='" + supplierId + "' and spac_id='" + spacid + "' and site_id=" + siteId).getSingleResult();
        return persistence.get(SupplierProductEntity.class, id.longValue());
    }

    public List<SupplierProductEntity> getSupplierProductBySiteIdAndSupplierId(Long siteId, String supplierId) throws Exception {
        return persistence.entityManager().createNativeQuery("select * from supplier_product where supplier_id='" + supplierId + "' and site_id=" + siteId, SupplierProductEntity.class).getResultList();
    }

    @Override
    public SacSupplierProdMasterEntity getSacSupplierProdMasterEntity(Long siteId, Long supplierId, String spacid) throws Exception {
        Query query = persistence.entityManager().createNativeQuery("  select m.* from  sac_supplier_availability a , sac_supplier_prod_master m where " +
                "  a.sup_prod_id = m.seq_prod_id  and " +
                "  a.site_id = :siteId and " +
                "  a.spac_id =:spacId and " +
                "  m.supplier_id = :supplierId ", SacSupplierProdMasterEntity.class);

        query.setParameter("siteId", siteId);
        query.setParameter("supplierId", supplierId);
        query.setParameter("spacId", spacid);

        return (SacSupplierProdMasterEntity) query.getSingleResult();
    }

    @Override
    public List<SacSupplierProdAvailEntity> getAllAvaliableForProcessingSacSuppliers(String fileName, String countryIso, String telephoneNo) throws Exception {
        Query query = persistence.entityManager().createQuery("select s from  SacSupplierProdAvailEntity s where s.fileName =:fileName and s.countryIsoCode =:countryIsoCode and s.telephoneNo =:telephoneNo and coalesce(s.availStatus,'Timeout')='Timeout'", SacSupplierProdMasterEntity.class);
        query.setParameter("fileName", fileName);
        query.setParameter("countryIsoCode", countryIso);
        query.setParameter("telephoneNo", telephoneNo);

        return (List<SacSupplierProdAvailEntity>) query.getResultList();
    }

    @Override
    public List<SupplierProductEntity> getSupplierProductBySiteIdAndSpacIds(Long siteId, List<String> spacIds) throws Exception {
        Query query = persistence.entityManager().createQuery("select entity FROM SupplierProductEntity entity, SupplierSiteEntity siteEntity WHERE siteEntity.siteId =:siteId and entity.spacId IN (:spacIds) and entity.supplierSiteEntity=siteEntity");
        query.setParameter("spacIds", spacIds);
        query.setParameter("siteId", siteId);
        List<SupplierProductEntity> products = query.getResultList();
        return products;
    }

    @Override
    public List<SupplierProductEntity> getSupplierProducts(Long siteId) throws Exception {
        Query query = persistence.entityManager().createQuery("select entity FROM SupplierProductEntity entity, SupplierSiteEntity siteEntity WHERE siteEntity.siteId =:siteId and entity.supplierSiteEntity=siteEntity and entity.productAvailable in ('Yes','No','YES','NO')");
        query.setParameter("siteId", siteId);
        List<SupplierProductEntity> products = query.getResultList();
        return products;
    }

    @Override
    public void updateStatusForSupplierProduct(Long siteId, String spacId, String supplierId, String status, String description, String productAvailableStatus) throws Exception {
        SupplierProductEntity entity = null;
        if (isNotEmpty(spacId) && isNotEmpty(supplierId)) {
            entity = getSupplierProductBySiteIdSupplierIdAndSpacId(siteId, supplierId, spacId);
            entity.setDescription(description);
            entity.setStatus(status);
            entity.setProductAvailable(productAvailableStatus);
            entity.setRetryCount(0);
            persistence.saveAndCommit(entity);
        } else {
            List<SupplierProductEntity> entityList = getSupplierProductBySiteIdAndSupplierId(siteId, supplierId);
            for (SupplierProductEntity productEntity : entityList) {
                productEntity.setStatus(status);
                productEntity.setDescription(description);
                entity.setProductAvailable(productAvailableStatus);
                entity.setRetryCount(0);
                persistence.saveAndCommit(productEntity);
            }
        }
    }

    @Override
    public List<DslEfmSupportedCountriesEntity> getDslSupportedCountries() throws Exception {
        List<DslEfmSupportedCountriesEntity> supportedCountries = newArrayList();
        try {
            supportedCountries = persistence.entityManager()
                                            .createQuery("SELECT entity from DslEfmSupportedCountriesEntity entity WHERE entity.dslEfmSupported='Yes'", DslEfmSupportedCountriesEntity.class)
                                            .getResultList();

        } catch (NoResultException e) {
            //donothing
        }

        return supportedCountries;
    }

    @Override
    public List<String> getDslEfmSupportedCountries() throws Exception {
        List<String> supportedCountries = newArrayList();
        try {
            supportedCountries = persistence.entityManager()
                                            .createQuery("SELECT entity.isoCode from DslEfmSupportedCountriesEntity entity WHERE entity.dslEfmSupported='Yes'", String.class)
                                            .getResultList();
        } catch (NoResultException e) {
            //donothing
        }
        return supportedCountries;
    }

/*    @Override
    public List<String> getAllSupplierProductStatuses(Long siteId) throws Exception {
        Query query = persistence.entityManager().createQuery("select entity.status FROM SupplierProductEntity entity, SupplierSiteEntity siteEntity WHERE siteEntity.siteId =:siteId  and entity.supplierSiteEntity=siteEntity");
        query.setParameter("siteId", siteId);
        List<String> status = query.getResultList();
        return status;
    }*/

    @Override
    public List<Long> getExistingSites(Long customerId) throws Exception {
        List<Long> existingSites = newArrayList();
        try {
            existingSites = persistence.entityManager().createNativeQuery("select site_id from supplier_site where customer_id = " + customerId).getResultList();
        } catch (NoResultException e) {
            //do nothing..
        }

        return existingSites;
    }

    @Override
    public List<Long> getExistingSitesAfterExcludingFailedSites(Long customerId) throws Exception {
        List<Long> existingSites = newArrayList();
        try {
            existingSites = persistence.entityManager().createNativeQuery("select site_id from supplier_site where availability_type_id != 5 and customer_id =" + customerId).getResultList();
        } catch (NoResultException e) {
            //do nothing..
            LOG.error(e);
        } catch (Exception e) {
            LOG.error(e);
        }

        return existingSites;
    }

    @Override
    public boolean isRequestValid(String requestId) throws Exception {

        String requestIdValue = null;
        try {
            requestIdValue = persistence.entityManager()
                                        .createQuery("SELECT entity.id from SupplierCheckApeRequestEntity entity WHERE entity.id=:requestId", String.class)
                                        .setParameter("requestId", requestId)
                                        .getSingleResult();
        } catch (Exception e) {
            //do nothing
        }
        return requestId.equalsIgnoreCase(requestIdValue) ? true : false;
    }

    @Override
    public SupplierCheckClientRequestEntity getClientRequest(String clientRequestId) throws Exception {
        SupplierCheckClientRequestEntity resultEntity = null;
        try {
            resultEntity = persistence.get(SupplierCheckClientRequestEntity.class, clientRequestId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultEntity;
    }

    @Override
    public <T> List<T> getAll(Class<T> t) {
        return persistence.getAll(t);
    }

    @Override
    public String getAvailabilityTelephone(String siteId) throws Exception {
        SupplierSiteEntity entity = null;
        try {
            entity = persistence.get(SupplierSiteEntity.class, Long.parseLong(siteId));
        } catch (NoResultException e) {
            //do nothing
        }
        return entity.getAvailabilityTelephoneNumber();
    }

    @Override
    public Long getCustomerId(String requestId) throws Exception {
        SupplierCheckApeRequestEntity entity = persistence.get(SupplierCheckApeRequestEntity.class, requestId);
        return entity.getSupplierCheckClientRequestEntity().getCustomerId();
    }

    @Override
    public boolean getAutoTriggerValue(String requestId) throws Exception {
        String value = null;
        try {
            value = persistence.get(SupplierCheckApeRequestEntity.class, requestId).getSupplierCheckClientRequestEntity().getAutoTrigger();
            value = value != null ? value.trim() : "not found";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Yes".equalsIgnoreCase(value.trim());
    }

    @Override
    public Long getSupplierProductId(String spacId, Long siteId) throws Exception {
        BigDecimal result = null;
        try {
            result = (BigDecimal) persistence.entityManager().createNativeQuery("SELECT entity.supp_prod_id from SUPPLIER_Product entity, SUPPLIER_SITE siteEntity \n" +
                                                                                "WHERE entity.spac_Id='" + spacId + "' and siteEntity.SITE_ID=" + siteId + " and entity.SITE_ID = siteEntity.SITE_ID").getSingleResult();
        } catch (NoResultException noResultException) {
            return null;
        }
        return result != null ? result.longValue() : null;
    }

    @Override
    public SupplierSiteEntity getSupplierSiteEntity(Long siteId) throws Exception {
        SupplierSiteEntity site = null;
        try {
            site = persistence.get(SupplierSiteEntity.class, siteId);
        } catch (Exception e) {
            site.setAvailabilityTypeId(GreyRedCross.getId());
        }

        return site != null ? site : new SupplierSiteEntity(GreyRedCross.getId());
    }

    @Override
    public SupplierRequestSiteEntity getSupplierRequestSite(Long siteId, String sccrId) throws Exception {
        SupplierRequestSiteEntity result = null;
        try {
            result = (SupplierRequestSiteEntity) persistence.entityManager().createNativeQuery("SELECT * from Supplier_Request_Site entity, Supplier_Check_Client_Request clientReqEntity WHERE \n" +
                                                                                               "clientReqEntity.sccr_id='" + sccrId + "'  and entity.site_Id=" + siteId + " and entity.sccr_id = clientReqEntity.sccr_id", SupplierRequestSiteEntity.class).getSingleResult();
        } catch (NoResultException noResultException) {
            return null;
        }

        return result;
    }

    @Override
    public SacRequestEntity getSACSiteRequest(String fileName, Long siteId) throws Exception {
        SacRequestEntity result = null;
        try {
            result = persistence.query(SacRequestEntity.class, "select r from SacRequestEntity r where fileName=?0 and siteId=?1 ", fileName, siteId).get(0);
        } catch (Exception ex) {
            result = null;
        }

        return result;
    }

    @Override
    public SupplierCheckClientRequestEntity getSupplierCheckClientRequest(String id) throws Exception {
        return persistence.get(SupplierCheckClientRequestEntity.class, id);
    }

    @Override
    public SupplierCheckApeRequestEntity getSupplierCheckApeRequest(String id) throws Exception {
        return persistence.get(SupplierCheckApeRequestEntity.class, id);
    }

    @Override
    public void updateSupplierSiteStatusExpired() throws Exception {
        persistence.entityManager().createNativeQuery("update supplier_site set availability_type_id=" + Orange.getId() + " where EXPIRY_DATE < sysdate").executeUpdate();
    }

    @Override
    public void updateSupplierSiteStatusFailedIfTimedOut() throws Exception {
        persistence.entityManager().createNativeQuery("update supplier_site set availability_type_id=" + Red.getId() + " where REQUEST_TIMEOUT < sysdate").executeUpdate();
    }

    @Override
    public void updateSupplierProductStatus(Long suppProdId, String status, String errorDescription) throws Exception {
        persistence.entityManager().createNativeQuery("update supplier_product set status='" + status + "' , description = '" + errorDescription + "' where supp_prod_id=" + suppProdId).executeUpdate();
    }

    @Override
    public void updateClientRequestStatus(String clientRequestId, String status) throws Exception {
        persistence.entityManager().createNativeQuery("update supplier_check_client_request set updated_on=sysdate, status='" + status + "' where sccr_id='" + clientRequestId + "'").executeUpdate();
    }

    @Override
    public void updateSupplierSiteStatus(Long siteId, int availabilityTypeId, String description) throws Exception {
        SupplierSiteEntity site = persistence.get(SupplierSiteEntity.class, siteId);
        site.setAvailabilityTypeId(availabilityTypeId);
        site.setErrorDescription(description);
        persistence.saveAndCommit(site);
    }

    @Override
    public void updateSupplierSiteStatusLatest(Long siteId, int availabilityTypeId, String description) throws Exception {
        SupplierSiteEntity site = persistence.get(SupplierSiteEntity.class, siteId);
        site.setAvailabilityTypeId(availabilityTypeId);
        site.setErrorDescription(description);
        persistence.save(site);
    }

    @Override
    public void updateAvailabilityTelephoneNumber(Long siteId, String telephoneNumber) throws Exception {
        persistence.entityManager().createNativeQuery("update SUPPLIER_SITE set AVAILABILITY_TELEPHONE_NUMBER='" + telephoneNumber + "'  where SITE_ID=" + siteId).executeUpdate();
    }

    @Override
    public void updateSupplierProduct(Long siteId, String spacId, String productAvailability, String checkReference) throws Exception {
        int updatedRows = persistence.entityManager().createNativeQuery("update SUPPLIER_PRODUCT set CHECK_REFERENCE='" + checkReference + "', PRODUCT_AVAILABLE='" + productAvailability + "' where SPAC_ID = '" + spacId + "' and SITE_ID=" + siteId).executeUpdate();
        if (YES.equalsIgnoreCase(productAvailability) && updatedRows > 0) {
            persistence.entityManager().createNativeQuery("update supplier_product set status='" + Completed.value() + "' , DESCRIPTION = '' where spac_id = '" + spacId + "' and site_id =" + siteId).executeUpdate();
            persistence.entityManager().createNativeQuery("update supplier_site set availability_type_id=" + Green.getId() + " where site_id =" + siteId).executeUpdate();
        }
    }

    @Override
    public List<SacRequestEntity> getAllAvailableForProcessingSacRequests(String fileName) {
        Query query = persistence.entityManager().createQuery("select r from SacRequestEntity r where r.fileName = :fileName and coalesce(r.status,'TIMEOUT') ='TIMEOUT'", SacRequestEntity.class);
        query.setParameter("fileName", fileName);
        return query.getResultList();
    }

    public SacRequestEntity getFirstAvailableForProcessingSacRequests(String fileName) {
        Query query = persistence.entityManager().createNativeQuery("select * from SAC_SITE_REQUESTS r where r.file_name = :fileName and r.status is null and rownum =1", SacRequestEntity.class);
        query.setParameter("fileName", fileName);
        return (SacRequestEntity) query.getSingleResult();
    }

    @Override
    public List<SacRequestEntity> getAllAvailableSacRequestsForAvailCheck(String fileName) {
        Query query = persistence.entityManager().createQuery("select r from SacRequestEntity r left outer join SacSupplierProdAvailEntity p on r.fileName = p.fileName and r.countryIsoCode = p.countryIsoCode and r.telephoneNo = p.telephoneNo where r.fileName = :fileName and coalesce(p.status,'Timeout') ='Timeout'", SacRequestEntity.class);
        query.setParameter("fileName", fileName);
        return query.getResultList();
    }

    @Override
    public String createSacBulkUpload(SacBulkInputDTO bulkInputDTO) {
        SacBulkUploadEntity bulkUploadEntity = new SacBulkUploadEntity();
        bulkUploadEntity.setFileDesc(bulkInputDTO.getFileDesc());
        bulkUploadEntity.setSalesChannel(bulkInputDTO.getSalesChannel());
        bulkUploadEntity.setCreateDate(new Timestamp(new Date().getTime()));
        bulkUploadEntity.setValidationStatus(bulkInputDTO.getValidationStatus());
        bulkUploadEntity.setCreateUser(bulkInputDTO.getUserId());
        bulkUploadEntity.setUserName(bulkInputDTO.getUserName());
        bulkUploadEntity.setHostName(bulkInputDTO.getHostName());
        persistence.entityManager().persist(bulkUploadEntity); // Persist SacBulkUploadEntity

        persistence.flush();

        return bulkUploadEntity.getFileName();

    }

    @Override
    public void createSacSiteRequest(SacBulkInputDTO bulkInputDTO) {

        SacBulkUploadEntity sacBulkUploadEntity = persistence.get(SacBulkUploadEntity.class, bulkInputDTO.getFileName());

        if (!persistence.entityManager().getTransaction().isActive()) {
            persistence.start();
        }

        for (SacSiteDTO site : bulkInputDTO.getSites()) {
            SacRequestEntity sacRequestEntity = new SacRequestEntity(sacBulkUploadEntity.getFileName(), site.getCountryIsoCode(), site.getTelephoneNo());
            sacRequestEntity.setSiteName(AssertObject.isEmpty(site.getSiteName()) ? site.getTelephoneNo() : site.getSiteName());
            sacRequestEntity.setCountryName(site.getCountryName());
            sacRequestEntity.setCreateDate(new Timestamp(new Date().getTime()));
            sacRequestEntity.setCreateUser(bulkInputDTO.getUserId());
            //siteRequests.add(sacRequestEntity);
            persistence.save(sacRequestEntity);
        }
        persistence.flush();
    }

    @Override
    public void deleteSacBulkUpload(String fileName) {

        if (!persistence.entityManager().getTransaction().isActive()) {
            persistence.start();
        }

        Query deleteProdAvailRec = persistence.entityManager().createNativeQuery("delete from SAC_SUPPLIER_AVAILABILITY where site_id in " +
                                                                                 "(" +
                                                                                 " select distinct(site_id) from SAC_SITE_REQUESTS r where r.file_name = :fileName" +
                                                                                 ")");
        deleteProdAvailRec.setParameter("fileName", fileName);
        deleteProdAvailRec.executeUpdate();

        Query deleteSiteReqRec = persistence.entityManager().createNativeQuery("delete from SAC_SITE_REQUESTS where file_name = :fileName");
        deleteSiteReqRec.setParameter("fileName", fileName);
        deleteSiteReqRec.executeUpdate();

        Query deleteBulkUploadRec = persistence.entityManager().createNativeQuery("delete from SAC_BULK_UPLOAD where file_name = :fileName");
        deleteBulkUploadRec.setParameter("fileName", fileName);
        deleteBulkUploadRec.executeUpdate();

        /*SacBulkUploadEntity sacEntity = persistence.get(SacBulkUploadEntity.class, fileName);
        persistence.remove(sacEntity);*/
        persistence.flush();
    }

    @Override
    public void deleteRequestFromAvailabilityQueue(AvailabilityRequestQueue request) {
        AvailabilityRequestQueue entity = persistence.get(AvailabilityRequestQueue.class, request.getId());
        persistence.remove(entity);
        persistence.flush();
    }

    @Override
    public void updateQueuedRequestStatus(AvailabilityRequestQueue request) {
        AvailabilityRequestQueue entity = persistence.get(AvailabilityRequestQueue.class, request.getId());
        persistence.saveAndCommit(entity);
    }

    @Override
    public List<SacBulkUploadEntity> getInProgressUploads(String userId) {
        return persistence.query(SacBulkUploadEntity.class, "select u from SacBulkUploadEntity u where u.createUser=?0 and u.validationStatus in ('InProgress','Failed') order by createDate desc ", userId);
    }

    @Override
    public List<SacBulkUploadEntity> getAllProcessingReports(Date tillDate) {
        return persistence.query(SacBulkUploadEntity.class, "select u from SacBulkUploadEntity u where u.validationStatus = 'Success' and u.availabilityStatus in ('COMPLETED','PROCESSING','FAILED') and u.createDate >= ?0 order by createDate desc", tillDate);
    }

    @Override
    public void updateSacBulkUpload(SacBulkInputDTO bulkInputDTO) {
        if (!persistence.entityManager().getTransaction().isActive()) {
            persistence.start();
        }

        Query updateStmnt = persistence.entityManager().createNativeQuery("update sac_bulk_upload set share_point_org_doc_id =:sharePointOrgDocId, share_point_fail_doc_id = :sharePointFailDocId, share_point_result_doc_id =:sharePointResultDocId," +
                                                                          " validation_status =:validStatus, availability_status =:availStatus, iteration_count =:itrCount , update_datetime =:updateDate, update_user =:updateUser " +
                                                                          " where file_name =:fileName");
        updateStmnt.setParameter("sharePointOrgDocId", bulkInputDTO.getSharePointOrgDocId());
        updateStmnt.setParameter("sharePointFailDocId", bulkInputDTO.getSharePointFailDocId());
        updateStmnt.setParameter("sharePointResultDocId", bulkInputDTO.getSharePointResultDocId());
        updateStmnt.setParameter("validStatus", bulkInputDTO.getValidationStatus());
        updateStmnt.setParameter("availStatus", bulkInputDTO.getAvailabilityStatus());
        updateStmnt.setParameter("itrCount", bulkInputDTO.getItrCount() == null ? 0 : bulkInputDTO.getItrCount());
        updateStmnt.setParameter("updateDate", new Timestamp(new Date().getTime()));
        updateStmnt.setParameter("updateUser", bulkInputDTO.getUserId());
        updateStmnt.setParameter("fileName", bulkInputDTO.getFileName());

        updateStmnt.executeUpdate();

        persistence.flush();

        if (SacBulkUploadStatus.FAILED.getStatus().equals(bulkInputDTO.getAvailabilityStatus())) {
            LOGGER.warn(String.format("SAC Bulk upload file status moved to FAILED !!. File Name : %s ", bulkInputDTO.getFileName()));
        } else if (SacBulkUploadStatus.COMPLETED.getStatus().equals(bulkInputDTO.getAvailabilityStatus())) {
            LOGGER.info(String.format("SAC Bulk upload file status moved to COMPLETED !!. File Name : %s ", bulkInputDTO.getFileName()));
        }

    }

    public void updateSacSiteErrorDesc(SacSiteDTO sacSiteDTO) {
        if (!persistence.entityManager().getTransaction().isActive()) {
            persistence.start();
        }

        Query updateStmnt = persistence.entityManager().createNativeQuery("update SAC_SITE_REQUESTS set ERROR_DESCRIPTION =:errorDesc , STATUS =:status , UPDATE_DATETIME =:updateDate where SITE_ID =:siteId");
        updateStmnt.setParameter("errorDesc", sacSiteDTO.getErrorDesc());
        updateStmnt.setParameter("status", sacSiteDTO.getStatus());
        updateStmnt.setParameter("updateDate", new Date());
        updateStmnt.setParameter("siteId", sacSiteDTO.getSiteId());

        try {
            updateStmnt.executeUpdate();
        } catch (Exception ex) {
            LOGGER.error("Failed to update SacSiteRequest ", ex);
        }

        persistence.flush();

    }

    public void updateSacSiteErrorDesc(String fileName, String errorDescription, String status) {
        if (!persistence.entityManager().getTransaction().isActive()) {
            persistence.start();
        }

        Query updateStmnt = persistence.entityManager().createNativeQuery("update SAC_SITE_REQUESTS set ERROR_DESCRIPTION =:errorDesc , STATUS =:status , UPDATE_DATETIME =:updateDate where file_name =:fileName");
        updateStmnt.setParameter("errorDesc", errorDescription);
        updateStmnt.setParameter("status", status);
        updateStmnt.setParameter("updateDate", new Date());
        updateStmnt.setParameter("fileName", fileName);

        try {
            updateStmnt.executeUpdate();
        } catch (Exception ex) {
            LOGGER.error("Failed to update SacSiteRequest ", ex);
        }

        persistence.flush();

    }

    @Override
    public void updateSacRequestStatus(Long[] siteIds, String status, String ape2ReqId, String ape3ReqId) {
        StringBuffer commaSepSites = null;
        if (siteIds.length > 0) {
            commaSepSites = new StringBuffer();
            for (Long siteId : siteIds) {
                commaSepSites.append(siteId);
                commaSepSites.append(",");
            }
        }
        String query = null;

        if (!AssertObject.isEmpty(status) && !AssertObject.isEmpty(ape2ReqId) && !AssertObject.isEmpty(ape3ReqId)) {
            query = String.format("update SAC_SITE_REQUESTS set status ='%s' , APE_2ND_REQUEST_ID ='%s', APE_3RD_REQUEST_ID ='%s', update_datetime =sysdate where site_id in (%s)", status, ape2ReqId, ape3ReqId, commaSepSites.substring(0, commaSepSites.length() - 1));

        } else if (!AssertObject.isEmpty(status) && AssertObject.areEmpty(ape2ReqId, ape3ReqId)) {
            query = String.format("update SAC_SITE_REQUESTS set status ='%s', update_datetime =sysdate where site_id in (%s)", status, commaSepSites.substring(0, commaSepSites.length() - 1));
        } else if (!AssertObject.isEmpty(status) && !AssertObject.isEmpty(ape2ReqId)) {
            query = String.format("update SAC_SITE_REQUESTS set status ='%s',APE_2ND_REQUEST_ID ='%s', update_datetime =sysdate where site_id in (%s)", status, ape2ReqId, commaSepSites.substring(0, commaSepSites.length() - 1));
        } else if (!AssertObject.isEmpty(status) && !AssertObject.isEmpty(ape3ReqId)) {
            query = String.format("update SAC_SITE_REQUESTS set status ='%s',APE_3RD_REQUEST_ID ='%s', update_datetime =sysdate where site_id in (%s)", status, ape3ReqId, commaSepSites.substring(0, commaSepSites.length() - 1));
        } else if (AssertObject.isEmpty(status) && !AssertObject.isEmpty(ape2ReqId)) {
            query = String.format("update SAC_SITE_REQUESTS set APE_2ND_REQUEST_ID ='%s', update_datetime =sysdate where site_id in (%s)", ape2ReqId, commaSepSites.substring(0, commaSepSites.length() - 1));
        } else if (AssertObject.isEmpty(status) && !AssertObject.isEmpty(ape3ReqId)) {
            query = String.format("update SAC_SITE_REQUESTS set APE_3RD_REQUEST_ID ='%s', update_datetime =sysdate where site_id in (%s)", ape3ReqId, commaSepSites.substring(0, commaSepSites.length() - 1));
        }

        if (!persistence.entityManager().getTransaction().isActive()) {
            persistence.start();
        }
        persistence.entityManager().createNativeQuery(query).executeUpdate();
        persistence.flush();

    }

    public void updateSacRequestStatus(String fileName, String status, String ape2ReqId, String ape3ReqId) {
        StringBuffer commaSepSites = null;

        String query = null;

        if (!AssertObject.isEmpty(status) && !AssertObject.isEmpty(ape2ReqId) && !AssertObject.isEmpty(ape3ReqId)) {
            query = String.format("update SAC_SITE_REQUESTS set status ='%s' , APE_2ND_REQUEST_ID ='%s', APE_3RD_REQUEST_ID ='%s', update_datetime =sysdate where file_name = '%s'", status, ape2ReqId, ape3ReqId, fileName);

        } else if (!AssertObject.isEmpty(status) && AssertObject.areEmpty(ape2ReqId, ape3ReqId)) {
            query = String.format("update SAC_SITE_REQUESTS set status ='%s', update_datetime =sysdate where file_name = %s", status, fileName);
        } else if (!AssertObject.isEmpty(status) && !AssertObject.isEmpty(ape2ReqId)) {
            query = String.format("update SAC_SITE_REQUESTS set status ='%s',APE_2ND_REQUEST_ID ='%s', update_datetime =sysdate where file_name = '%s'", status, ape2ReqId, fileName);
        } else if (!AssertObject.isEmpty(status) && !AssertObject.isEmpty(ape3ReqId)) {
            query = String.format("update SAC_SITE_REQUESTS set status ='%s',APE_3RD_REQUEST_ID ='%s', update_datetime =sysdate where file_name = '%s'", status, ape3ReqId, fileName);
        } else if (AssertObject.isEmpty(status) && !AssertObject.isEmpty(ape2ReqId)) {
            query = String.format("update SAC_SITE_REQUESTS set APE_2ND_REQUEST_ID ='%s', update_datetime =sysdate where file_name = '%s'", ape2ReqId, fileName);
        } else if (AssertObject.isEmpty(status) && !AssertObject.isEmpty(ape3ReqId)) {
            query = String.format("update SAC_SITE_REQUESTS set APE_3RD_REQUEST_ID ='%s', update_datetime =sysdate where file_name = '%s'", ape3ReqId, fileName);
        }

        if (!persistence.entityManager().getTransaction().isActive()) {
            persistence.start();
        }
        persistence.entityManager().createNativeQuery(query).executeUpdate();
        persistence.flush();

    }

    @Override
    public void updateSacSiteAvailStatus(SacSupplierProdMasterPK[] spacs, String status) {
        if (!persistence.entityManager().getTransaction().isActive()) {
            persistence.start();
        }

        for (SacSupplierProdMasterPK sacSiteAvail : spacs) {
            SacSupplierProdAvailEntity entity = persistence.get(SacSupplierProdAvailEntity.class, sacSiteAvail);

            entity.setStatus(status);
            entity.setUpdateDate(new Timestamp(currentDate().getTime()));
            persistence.entityManager().merge(entity);
        }

        persistence.flush();
    }

    @Override
    public List<SacSupplierProdAvailEntity> getAllSacSuppAvailEntityForSite(Long siteId) {
        return persistence.query(SacSupplierProdAvailEntity.class, "select a from SacSupplierProdAvailEntity a where  a.siteId =?0", siteId);
    }

    @Override
    public SacSupplierProdAvailEntity getSacSuppAvailEntityForSite(Long siteId, String spacId) {

        return persistence.query(SacSupplierProdAvailEntity.class, "select a from SacSupplierProdAvailEntity a where  a.siteId =?0 and a.spacId=?1", siteId, spacId).get(0);
    }

    @Override
    public SacRequestEntity getSacSiteRequests(String ape2ndReId, String ape3rdReqId) {
        SacRequestEntity entity = null;
        String sqlStmt = null;
        if (!AssertObject.isEmpty(ape2ndReId) || !AssertObject.isEmpty(ape3rdReqId)) {
            if (!AssertObject.isEmpty(ape2ndReId) && !AssertObject.isEmpty(ape3rdReqId)) {
                sqlStmt = "select r from SacRequestEntity r where r.ape2ReqId =:ape2ReqId and r.ape3ReqId =: ape3ReqId";
            } else if (!AssertObject.isEmpty(ape2ndReId)) {
                sqlStmt = "select r from SacRequestEntity r where r.ape2ReqId =:ape2ReqId";
            } else {
                sqlStmt = "select r from SacRequestEntity r where r.ape3ReqId =:ape3ReqId";
            }
        } else {
            return null;
        }

        Query query = persistence.entityManager().createQuery(sqlStmt, SacRequestEntity.class);
        if (!AssertObject.isEmpty(ape2ndReId)) {
            query.setParameter("ape2ReqId", ape2ndReId);
        }
        if (!AssertObject.isEmpty(ape3rdReqId)) {
            query.setParameter("ape3ReqId", ape3rdReqId);
        }
        query.setHint(PersistenceManager.FETCH_SIZE, 1);
        List<SacRequestEntity> sacRequestEntities = query.getResultList();
        if (sacRequestEntities != null && sacRequestEntities.size() > 0) {
            entity = sacRequestEntities.get(0);
        }
        return entity;
    }

    @Override
    public SacRequestEntity getSacSiteRequestBySiteId(Long siteId) {
        SacRequestEntity retEntity = null;
        List<SacRequestEntity> requests = persistence.query(SacRequestEntity.class, "select r from SacRequestEntity r where  r.siteId =?0", siteId);

        if (requests != null && requests.size() > 0) {
            retEntity = requests.get(0);
        }

        return retEntity;
    }

    @Override
    public SacSupplierProdMasterEntity getSuppProdMaster(String spacId) {
        return persistence.query(SacSupplierProdMasterEntity.class, "select r from SacSupplierProdMasterEntity r where r.spacId =?0", spacId).get(0);
    }

    @Override
    public List<Long> getSiteIdsByClientRequestId(String id) {
        return persistence.entityManager().createNativeQuery("select site_id from supplier_request_site where SCCR_ID = '" + id + "'").getResultList();
    }


    @Override
    public List<SacRequestEntity> getAllApe2ndReqInitiatedSites(String fileName) {
        return persistence.query(SacRequestEntity.class, "select r from SacRequestEntity r where r.fileName =?0 and r.status='INITIATED'", fileName);
    }

    public List<SacRequestEntity> getAllSacRequestEntity(String fileName) {
        return persistence.query(SacRequestEntity.class, "select r from SacRequestEntity r where r.fileName =?0", fileName);
    }

    @Override
    public List<SacRequestEntity> getAllSacSitesWithStatus(String fileName, SacApeStatus apeStatus) {

        String status = "";
        if (status != null) {
            status = apeStatus.getStatus();
        }
        return persistence.query(SacRequestEntity.class, "select r from SacRequestEntity r where r.fileName =?0 and r.status=?1", fileName, status);
    }

    public SacBulkUploadEntity getSacBulkUploadEntity(String fileName) {

        return persistence.get(SacBulkUploadEntity.class, fileName);
    }

    public void saveSacSupProducts(List<SacSupplierProdAvailEntity> sacSupplierProdAvailEntities) {
        if (!persistence.entityManager().getTransaction().isActive()) {
            persistence.start();
        }
        for (SacSupplierProdAvailEntity prodAvailEntity : sacSupplierProdAvailEntities) {
            prodAvailEntity.setCreateDate(new Timestamp(new Date().getTime()));
            persistence.save(prodAvailEntity);
            SacSupplierProdMasterEntity prodMasterEntity = prodAvailEntity.getSupplierProduct();
            if (prodMasterEntity != null) {
                prodMasterEntity.setCreateDate(new Timestamp(new Date().getTime()));
                persistence.save(prodMasterEntity);
            }
        }

        persistence.flush();
    }

    public void saveSacSupplierProdAvailEntities(List<SacSupplierProdAvailEntity> sacSupplierProdAvailEntities) {
        if (!persistence.entityManager().getTransaction().isActive()) {
            persistence.start();
        }

        for (SacSupplierProdAvailEntity prodAvailEntity : sacSupplierProdAvailEntities) {
            persistence.save(prodAvailEntity);
        }
        persistence.flush();
    }

    public void saveSacSupplierProdAvailEntity(SacSupplierProdAvailEntity sacSupplierProdAvailEntity) {
        if (!persistence.entityManager().getTransaction().isActive()) {
            persistence.start();
        }

        persistence.save(sacSupplierProdAvailEntity);

        persistence.flush();
    }

    @Override
    public List<SacSupplierProdAvailEntity> getUpdatedProductListInLast24Hrs(String countryIsoCode) {
        SacRequestEntity retResult = null;
        Long siteId = null;
        List<SacSupplierProdAvailEntity> sacSupplierProdAvailEntities = null;
        Calendar now = Calendar.getInstance();
        now.setTime(new Date());
        now.set(Calendar.HOUR, now.get(Calendar.HOUR) - 24);
        String last24HrDateTime = DateFormatEnum.SAC_DB_DATE_FORMAT.getValue().format(now.getTime());
        Query query = persistence.entityManager().createNativeQuery(" select site_id from " +
                                                                    " (" +
                                                                    " select r.* from sac_site_requests r , sac_supplier_availability a where " +
                                                                    " r.site_id = a.site_id and " +
                                                                    " nvl(a.created_user,'NA') <> 'SAC_COPY' and" +
                                                                    " a.create_datetime>to_date(:last24HrDateTime,'DD/MM/YYYY HH24:MI:SS') order by a.create_datetime desc " +
                                                                    " ) " +
                                                                    " where rownum =1");

        //Query query = persistence.entityManager().createQuery("select r from SacRequestEntity r left outer join r.suppliers supp with supp.createDate < :last24Hrs ", SacRequestEntity.class);
        query.setParameter("last24HrDateTime", last24HrDateTime);
        query.setHint(PersistenceManager.FETCH_SIZE, 1);

        try {
            BigDecimal result = (BigDecimal) query.getSingleResult();
            if (result != null) {
                siteId = result.longValue();
            }
        } catch (NoResultException ex) {

        }


        if (siteId != null) {
            Query availQuery = persistence.entityManager().createQuery("select a from SacSupplierProdAvailEntity a where a.siteId=:siteId", SacSupplierProdAvailEntity.class);
            availQuery.setParameter("siteId", siteId);
            query.setHint(PersistenceManager.FETCH_SIZE, 50);
            sacSupplierProdAvailEntities = availQuery.getResultList();
        }

        return sacSupplierProdAvailEntities;
    }

    public boolean hasUpdatedProductListInLast24Hrs(String countryIsoCode) {
        SacRequestEntity retResult = null;
        Integer count = null;
        List<SacSupplierProdAvailEntity> sacSupplierProdAvailEntities = null;
        Calendar now = Calendar.getInstance();
        now.setTime(new Date());
        now.set(Calendar.HOUR, now.get(Calendar.HOUR) - 24);
        String last24HrDateTime = DateFormatEnum.SAC_DB_DATE_FORMAT.getValue().format(now.getTime());
        Query query = persistence.entityManager().createNativeQuery(" select count(*) from sac_site_requests r , sac_supplier_availability a where " +
                                                                    " r.site_id = a.site_id and " +
                                                                    " nvl(a.created_user,'NA') <> 'SAC_COPY' and" +
                                                                    " a.create_datetime>to_date(:last24HrDateTime,'DD/MM/YYYY HH24:MI:SS') ");

        query.setParameter("last24HrDateTime", last24HrDateTime);

        try {
            BigDecimal countVal = (BigDecimal) query.getSingleResult();
            if (countVal != null) {
                count = countVal.intValue();
            }
        } catch (NoResultException ex) {

        }


        if (count != null && count > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void updateSacSupplierProdToSite(final Long siteId, final List<SacSupplierProdAvailDTO> sacSupplierProdAvailDTOs) {
        org.hibernate.Session sesssion = (org.hibernate.Session) persistence.entityManager().getDelegate();
        sesssion.doWork(new AbstractWork() {
            @Override
            public void execute(Connection connection) throws SQLException {
                PreparedStatement ps = null;
                try {
                    ps = connection
                        .prepareStatement("insert into sac_supplier_availability (site_id,spac_id,sup_prod_id,ape_request_id,create_datetime,created_user) values (?,?,?,?,?,?)");

                    for (SacSupplierProdAvailDTO sacSupplierProdAvailDTO : sacSupplierProdAvailDTOs) {
                        Long seqProdId = null;
                        if (sacSupplierProdAvailDTO.getSacSupplierProdDetailDTO() != null) {
                            seqProdId = sacSupplierProdAvailDTO.getSacSupplierProdDetailDTO().getSeqProdId();
                        }
                        ps.setLong(1, siteId);
                        ps.setString(2, sacSupplierProdAvailDTO.getSpacId());
                        ps.setLong(3, seqProdId);
                        ps.setString(4, sacSupplierProdAvailDTO.getApeReqId());
                        ps.setDate(5, new java.sql.Date(new Date().getTime()));
                        ps.setString(6, "SAC_COPY");
                        ps.addBatch();

                    }
                    ps.executeBatch();
                    connection.commit();
                } catch (SQLException ex) {
                    LOG.error(ex);
                    connection.rollback();
                } finally {
                    if (ps != null) {
                        ps.close();
                    }

                }
            }
        });


    }

    @Override
    public boolean isAllAvailabilityCheckCompleted(String fileName) {
        boolean hasEmptyAvailStatus = false;
        Query query = persistence.entityManager().createNativeQuery(" select count(*) from sac_site_requests r, sac_supplier_availability a where " +
                " r.site_id = a.site_id " +
                " and a.availability_status is null" +
                " and (a.status not in ('Completed','Failed') or a.status is null)" +
                " and r.file_name =:fileName");
        query.setParameter("fileName", fileName);
        BigDecimal count = (BigDecimal) query.getSingleResult();
        LOGGER.info(String.format("Empty Available states for [%s] : %s", fileName, count));
        if (count == null || (count != null && count.intValue() < 1)) {
            hasEmptyAvailStatus = true;
        }

        return hasEmptyAvailStatus;
    }


    public List<SacBulkUploadEntity> getAllInProcessingSacUploadsBeyond24Hrs() {
        Calendar now = Calendar.getInstance();
        now.setTime(new Date());
        now.set(Calendar.HOUR, now.get(Calendar.HOUR) - 24);
        String last24HrDateTime = DateFormatEnum.SAC_DB_DATE_FORMAT.getValue().format(now.getTime());

        Query query = persistence.entityManager().createNativeQuery("select * from SAC_BULK_UPLOAD " +
                                                                    "where " +
                                                                    "AVAILABILITY_STATUS = 'PROCESSING' and " +
                                                                    "create_datetime < to_date(:last24HrDateTime,'DD/MM/YYYY HH24:MI:SS')", SacBulkUploadEntity.class);
        query.setParameter("last24HrDateTime", last24HrDateTime);

        return query.getResultList();
    }


    @Override
    public String getCountryIsoCode(String country) {
        return (String) persistence.entityManager().createNativeQuery("select ISO_CODE from DSL_EFM_SUPPORTED_COUNTRIES where upper(COUNTRY) = upper('" + country + "')").getSingleResult();
    }


    @Override
    public SupplierSiteEntity getSupplierSiteBySiteName(Long customerId, String siteName) {
        SupplierSiteEntity site = (SupplierSiteEntity) persistence.entityManager().createNativeQuery("select * from supplier_site where site_name = '" + siteName + "' and customer_id = " + customerId, SupplierSiteEntity.class).getSingleResult();
        return site;
    }


    @Override
    public boolean isAnyReportGenInProgress(String userId) {
        Query query = persistence.entityManager().createNativeQuery("select count(*) from SAC_BULK_UPLOAD where availability_status='PROCESSING' and created_user =:userId");
        query.setParameter("userId", userId);

        BigDecimal count = (BigDecimal) query.getSingleResult();
        if (count != null && count.intValue() > 0) {
            return true;
        } else {
            return false;
        }
    }


    @Override
    public void updateSupplierSiteAsGreenIfAnyProductIsAvailable() {
        Query updateSql = persistence.entityManager().
            createNativeQuery("UPDATE supplier_site SET availability_type_id = 4 WHERE availability_type_id = 3 AND site_ID IN (select distinct site_id FROM supplier_product WHERE upper(product_available) = 'YES'  AND status = 'Completed')");
        updateSql.executeUpdate();
    }

    @Override
    public void updateSupplierProductTimeout() {
        persistence.entityManager().createNativeQuery("update supplier_product set status = 'Timeout',product_available='No Response', description = 'External system request timeout',retry_count= 0 where supp_prod_id in (select supp_prod_id from supplier_product where status = 'InProgress' and request_timeout < sysdate)").executeUpdate();
        persistence.entityManager().createNativeQuery("update supplier_product set description = '' where status = 'Completed' and description is not null").executeUpdate();
    }

    @Override
    public List<String> getProductsBySiteAndSupplier(Long siteId, List<String> spacIds) {
        Query query = persistence.entityManager().createNativeQuery("SELECT SUPP_PROD_ID FROM SUPPLIER_PRODUCT WHERE SITE_ID =:SITEID AND SPAC_ID IN (:SPACIDS)");
        query.setParameter("SITEID", siteId);
        query.setParameter("SPACIDS", spacIds);

        List resultList = query.getResultList();

        if(resultList.isEmpty()) {
            return Collections.emptyList();
        }

        return newArrayList(Iterables.transform(resultList, new Function<Object, String>() {
            @Override
            public String apply(Object input) {
                return input.toString();
            }
        }));
    }

    @Override
    public void updateSupplierProduct(List<String> productList, Date requestedTime, Date requestTimeout, String status, String mandatory, String description, Integer retryCount) {
        Query updateSql = persistence.entityManager().createNativeQuery(" UPDATE SUPPLIER_PRODUCT SET " +
                                                                        " REQUESTED_TIME = :REQUESTED_TIME , " +
                                                                        " REQUEST_TIMEOUT = :REQUEST_TIMEOUT," +
                                                                        " STATUS =  :STATUS, " +
                                                                        " MANDATORY_ATTRIBUTES = :MANDATORY ," +
                                                                        " DESCRIPTION = :DESCRIPTION, " +
                                                                        " RETRY_COUNT = :RETRY_COUNT " +
                                                                        " WHERE SUPP_PROD_ID IN (:SUPP_PRODUCT_IDS)");
        updateSql.setParameter("REQUESTED_TIME", requestedTime);
        updateSql.setParameter("REQUEST_TIMEOUT", requestTimeout);
        updateSql.setParameter("STATUS", status);
        updateSql.setParameter("MANDATORY", mandatory);
        updateSql.setParameter("DESCRIPTION", description);
        updateSql.setParameter("RETRY_COUNT",isNull(retryCount)?0:retryCount.intValue());
        updateSql.setParameter("SUPP_PRODUCT_IDS", productList);
        updateSql.executeUpdate();
    }


    @Override
    public List<Long> getExistingSitesAfterExcludingFailedSitesForOnNet(Long customerId) throws Exception {
        List<Long> existingSites = newArrayList();
        try {
            existingSites = persistence.entityManager().createNativeQuery("select site_id from ONNET_AVAILABILITY_STATUS where ONNET_AVAILABILITY_TYPE_ID != 5 and customer_id =" + customerId).getResultList();
        } catch (NoResultException e) {
            //do nothing..
            LOG.error(e);
        } catch (Exception e) {
            LOG.error(e);
        }

        return existingSites;
    }

    @Override
    public List<Long> getExistingOnNetSites(Long customerId) throws Exception {
        List<Long> existingOnNetSites = newArrayList();
        try {
            existingOnNetSites = persistence.entityManager().createNativeQuery("select site_id from ONNET_AVAILABILITY_STATUS where customer_id = " + customerId).getResultList();
        } catch (NoResultException e) {
            //do nothing..
        }

        return existingOnNetSites;
    }

    public void saveSelectedOnnetDetails(List<OnnetBuildingEntity> onnetBuildingEntityList) {
        try{
            for (OnnetBuildingEntity onnetBuildingEntity:onnetBuildingEntityList){
                persistence.saveAndCommit(onnetBuildingEntity);
            }
        }catch (Exception e) {
            throw new RuntimeException("Error while persisting the selected Onnet Details");
        }
    }


    @Override
    public void updateOnNetAvailability(long siteID, String onnetAvailable) {

        if ("Y".equalsIgnoreCase(onnetAvailable)) {
            persistence.entityManager().createNativeQuery("update ONNET_AVAILABILITY_STATUS set ONNET_AVAILABILITY='" + Completed.value() + "' , ONNET_AVAILABILITY_TYPE_ID =" + Green.getId() + " where site_id =" + siteID).executeUpdate();
        }else if  ("N".equalsIgnoreCase(onnetAvailable)){
            persistence.entityManager().createNativeQuery("update ONNET_AVAILABILITY_STATUS set ONNET_AVAILABILITY='" + NotSupported.value() + "' , ONNET_AVAILABILITY_TYPE_ID =" + GreyRedCross.getId() + " where site_id = "+ siteID).executeUpdate();
        }
    }

    @Override
    public OnnetAvailabilityEntity getOnNetAvailabilityEntity(long siteID) {
        OnnetAvailabilityEntity resultEntity = null;
        try {
            resultEntity = persistence.get(OnnetAvailabilityEntity.class, siteID);
        } catch (Exception e) {
            throw new RuntimeException("Error while persisting the Onnet Availability Status");
        }
        return resultEntity;
    }

    @Override
    public OnnetBuildingsWithEFMEntity getOnnetBuildingsWithEFMEntity(long siteID) {
        OnnetBuildingsWithEFMEntity resultEntity = null;
        try {
            resultEntity = persistence.get(OnnetBuildingsWithEFMEntity.class, siteID);
        } catch (Exception e) {
            throw new RuntimeException("Error while retriving the Onnet Details");
        }
        return resultEntity;
    }


    public void saveOnnetBuildingsWithEFMEntity(OnnetBuildingsWithEFMEntity onnetBuildingsWithEFMEntity) {
        try{
            persistence.saveAndCommit(onnetBuildingsWithEFMEntity);

        }catch (Exception e) {
            throw new RuntimeException("Error while persisting the Onnet Details");
        }
    }




    private interface APEQrefRepositoryLogger {
        @Log(level = LogLevel.DEBUG, format = "Saving qref '%s' attribute '%s' with value '%s'.")
        void savingQRef(String qrefId, String attributName, String attributeValue);

        @Log(level = LogLevel.DEBUG, format = "Saving request '%s' with uniqueId '%s'.")
        void savingRequest(String requestId, String uniqueId);

        @Log(level = LogLevel.DEBUG, format = "Saving qref error '%s' with error code '%s' and message '%s'.")
        void savingQrefError(String qrefId, String errorCode, String errorMsg);

        @Log(level = LogLevel.DEBUG, format = "Saving staff comment '%s' with Qref ID '%s'.")
        void savingStaffComment(String comment, String qrefId);

        @Log(level = LogLevel.DEBUG, format = "Loading qrefs for uniqueId '%s'")
        void loadingQRef(String uniqueId);

        @Log(level = LogLevel.DEBUG, format = "Loading request for uniqueId '%s'")
        void loadingRequest(String uniqueId);

        @Log(level = LogLevel.DEBUG, format = "Loading request for requestId '%s'")
        void loadingRequestByRequestId(String requestId);

        @Log(level = LogLevel.DEBUG, format = "Loading comment for qrefId '%s'")
        void loadingComment(String qrefId);

        @Log(level = LogLevel.DEBUG, format = "Info : '%s' ")
        void info(String requestId);

        @Log(level = LogLevel.ERROR, format = "Error : '%s' ")
        void error(Exception e);
    }
}
