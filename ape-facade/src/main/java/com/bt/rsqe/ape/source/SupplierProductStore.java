package com.bt.rsqe.ape.source;

import com.bt.rsqe.ape.config.SupplierCheckConfig;
import com.bt.rsqe.ape.dto.SiteAvailabilityStatus;
import com.bt.rsqe.ape.dto.SupplierCheckRequest;
import com.bt.rsqe.ape.dto.SupplierSite;
import com.bt.rsqe.ape.dto.sac.SacApeStatus;
import com.bt.rsqe.ape.dto.sac.SacBulkInputDTO;
import com.bt.rsqe.ape.dto.sac.SacSiteDTO;
import com.bt.rsqe.ape.dto.sac.SacSupplierProdAvailDTO;
import com.bt.rsqe.ape.repository.APEQrefJPARepository;
import com.bt.rsqe.ape.repository.entities.AvailabilityRequestQueue;
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
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.utils.AssertObject;
import com.google.common.base.Joiner;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.bt.rsqe.ape.source.SupplierProductHelper.timestamp;
import static com.bt.rsqe.customerinventory.dto.AvailabilityType.*;
import static com.google.common.collect.Lists.*;

/**
 * Created by 605783162 on 09/09/2015.
 */
public class SupplierProductStore {


    private static APEQrefJPARepository repository;

    public SupplierProductStore(APEQrefJPARepository repository) {
        this.repository = repository;
    }

    public static String generateClientRequestId() throws Exception {
        return SupplierCheckRequestId.forClient(repository).value();
    }

    public static String generateApeRequestId() throws Exception {
        return SupplierCheckRequestId.forApe(repository).value();
    }

    public static List<SupplierSiteEntity> getSupplierSitesByCustomerId(Long customerId) throws Exception {
        return repository.getSupplierSitesByCustomerId(customerId);
    }

    public static SupplierSiteEntity getSupplierSiteBySiteId(Long siteId) throws Exception {
        return repository.getSupplierSiteEntity(siteId);
    }

    public static SupplierSiteEntity getSupplierSiteBySiteName(Long customerId, String siteName) throws Exception {
        return repository.getSupplierSiteBySiteName(customerId, siteName);
    }

    public static List<String> getDslEfmSupportedCountries() throws Exception {
        return repository.getDslEfmSupportedCountries();
    }

    public static List<Long> getExistingSitesButExcludeFailedSites(Long customerId) throws Exception {
        return repository.getExistingSitesAfterExcludingFailedSites(customerId);
    }

    public static List<Long> getExistingSites(Long customerId) throws Exception {
        return repository.getExistingSites(customerId);
    }

    public static List<SupplierSiteEntity> getSupplierSitesBySiteIds(List<Long> siteIds) throws Exception {
        return repository.getSupplierSitesBySiteId(siteIds);
    }

    public static List<SacRequestEntity> getAllSacRequestEntity(String fileName){
        return repository.getAllSacRequestEntity(fileName);
    }

    public static SacBulkUploadEntity getSacBulkUploadEntity(String fileName){
        return repository.getSacBulkUploadEntity(fileName);
    }

    public static void storeLog(String requestId, String logType, String operation, String payload, String user, String description) {
        repository.save(new SupplierCheckLogEntity(requestId, logType, operation, payload, user, timestamp(), description));
    }

    public static void storeLogLatest(String requestId, String logType, String operation, String payload, String user, String description) {
        repository.saveAlone(new SupplierCheckLogEntity(requestId, logType, operation, payload, user, timestamp(), description));
    }

    public static void storeBatchSupplierSiteUsingSiteDto(List<SiteDTO> sites, Long customerId) {
        for (SiteDTO site : sites) {
            repository.save(new SupplierSiteEntity(site.getSiteId().getValue(), customerId, site.getSiteName(), site.getCountryISOCode(),
                                                   site.getCountryName(), null, Red.getId(), null, "Error while placing GetSupplierProductList call to APE, Please trigger it manually", null, null));
        }
    }

    public static void storeClientRequest(SupplierCheckClientRequestEntity entity) {
        repository.save(entity);
    }


    public static void storeClientRequestlatest(SupplierCheckClientRequestEntity entity) {
        repository.saveAlone(entity);
    }

    public static List<SiteAvailabilityStatus> getSiteAvailabilityStatus(Long customerId) throws Exception {
        return repository.getSiteAvailabilityStatus(customerId);
    }

    public static void storeApeRequest(SupplierCheckApeRequestEntity entity) {
        repository.createApeRequest(entity);
    }

    public static void updateTelephoneNumber(long siteId, String availabilityTelephoneNumber) throws Exception {
        if (AssertObject.isNotNull(availabilityTelephoneNumber)) {
            repository.updateAvailabilityTelephoneNumber(siteId, availabilityTelephoneNumber);
        }
    }

    public static void updateSupplierProductInformation(long siteId, String spacId, String productAvailable, String checkedReference) throws Exception {
        repository.updateSupplierProduct(siteId, spacId, productAvailable, checkedReference);
    }


    public static boolean isRequestIdValid(String requestId) throws Exception {
        return repository.isRequestValid(requestId);
    }

    public static String getCustomerIdByApeRequestId(String requestId) throws Exception {
        return String.valueOf(repository.getCustomerId(requestId));
    }

    public static boolean isAvailabilityCheckAutoTriggerRequired(String requestId) throws Exception {
        return repository.getAutoTriggerValue(requestId);
    }

    public static void updateStatusForSupplierProduct(long siteId, String supplierId, String spacId, String status, String failureMessage, String productAvailableStatus) throws Exception {
        repository.updateStatusForSupplierProduct(siteId, spacId, supplierId, status, failureMessage, productAvailableStatus);
    }

    public static List<SupplierProductEntity> getSupplierProductBySiteIdAndSpacIds(Long siteId, List<String> spacIds) throws Exception {
        return repository.getSupplierProductBySiteIdAndSpacIds(siteId, spacIds);
    }

    public static List<String> getProductsBySiteAndSupplier(Long siteId, List<String> spacIds) throws Exception {
        return repository.getProductsBySiteAndSupplier(siteId, spacIds);
    }

    public static void storeSupplierProduct(SupplierProductEntity productEntity) {
        repository.save(productEntity);
    }

    public static void updateSupplierProduct(List<String> productList, Date requestedTime, Date requestTimeout, String status, String mandatory, String description) {
        repository.updateSupplierProduct(productList, requestedTime, requestTimeout, status, mandatory, description, null);
    }

    public static void storeSacSupplierProdMaster(SacSupplierProdMasterEntity productEntity) {
        repository.save(productEntity);
    }

    public static SupplierSiteEntity getSupplierSiteEntity(Long siteId) throws Exception {
        return repository.getSupplierSiteEntity(siteId);
    }

    public static void updateSacSiteAvailStatus(SacSupplierProdMasterPK[] spacs, String status) {
        repository.updateSacSiteAvailStatus(spacs, status);
    }

    public static void storeSupplierSite(SupplierSiteEntity site) {
        repository.save(site);
    }

    public static void storeSupplierSiteList(List<SupplierSiteEntity> siteEntityList) {
        for (SupplierSiteEntity site : siteEntityList) {
            repository.save(site);
        }
    }

    public static void storeSupplierSiteUsingRequest(SupplierCheckRequest request, SupplierCheckConfig config) throws Exception {
        storeSupplierSiteFromRequest(request, config);
    }

    public static Long getSupplierProductId(String spacId, Long siteId) throws Exception {
        return repository.getSupplierProductId(spacId, siteId);
    }

    public static String getAvailabilityTelephone(String siteId) throws Exception {
        return repository.getAvailabilityTelephone(siteId);
    }

    public static SupplierCheckApeRequestEntity getApeRequestByApeRequestId(String requestId) throws Exception {
        return repository.getSupplierCheckApeRequest(requestId);
    }

    public static SupplierCheckClientRequestEntity getClientRequestByApeRequestId(String requestId) throws Exception {
        return repository.getSupplierCheckApeRequest(requestId).getSupplierCheckClientRequestEntity();
    }

    public static SupplierCheckClientRequestEntity getClientRequestById(String clientRequestId) throws Exception {
        return repository.getClientRequest(clientRequestId);
    }

    public static List<SupplierSiteEntity> getSupplierSitesBySiteId(List<Long> siteIds) throws Exception {
        return repository.getSupplierSitesBySiteId(siteIds);
    }

    public static List<SupplierSiteEntity> getSupplierSiteRequestObjectBySiteId(List<Long> siteIds) throws Exception {
        List<SupplierSiteEntity> sites = newArrayList();
        for (Long siteId :siteIds) {
            sites.add(new SupplierSiteEntity(siteId));
        }
        return sites;
    }

    public static void storeSupplierSiteFromRequest(SupplierCheckRequest request, SupplierCheckConfig config) throws Exception {
        List<SupplierSiteEntity> siteEntities = SupplierProductHelper.getSupplierSiteEntityList(request, config);
        for (SupplierSiteEntity siteEntity : siteEntities) {
            repository.removeSiteEntity(siteEntity);
            siteEntity.setSupplierProductEntityList(null);
            siteEntity.setErrorDescription("");
            repository.save(siteEntity);
        }
    }

    public static SacRequestEntity getSACSiteRequest(String fileName, Long siteId) throws Exception {
        return repository.getSACSiteRequest(fileName, siteId);
    }

    public static List<SacRequestEntity> getAllAvailableSacRequestsForAvailCheck(String fileName) {
        return repository.getAllAvailableSacRequestsForAvailCheck(fileName);
    }

    public static void storeSacRequest(SacRequestEntity sacSiteRequestsEntity) {
        repository.save(sacSiteRequestsEntity);
    }

    public static void updateSacSiteErrorDesc(SacSiteDTO sacSiteDTO) {
        repository.updateSacSiteErrorDesc(sacSiteDTO);
    }

    public static void updateSacSiteErrorDesc(String fileName,String errorDesc,String status) {
        repository.updateSacSiteErrorDesc(fileName,errorDesc,status);
    }

    public static String createSacBulkUpload(SacBulkInputDTO bulkInputDTO) {
        return repository.createSacBulkUpload(bulkInputDTO);
    }

    public static void updateSacSupplierProdToSite(Long siteId, List<SacSupplierProdAvailDTO> sacSupplierProdAvailDTOs){
         repository.updateSacSupplierProdToSite(siteId,sacSupplierProdAvailDTOs);
    }

    public static void createSacSiteRequest(SacBulkInputDTO bulkInputDTO) {
        repository.createSacSiteRequest(bulkInputDTO);
    }

    public static void deleteSacBulkUpload(String fileName) {
        repository.deleteSacBulkUpload(fileName);
    }

    public static List<SacBulkUploadEntity> getInProgressUploads(String userid) {
        return repository.getInProgressUploads(userid);
    }

    public static List<SacBulkUploadEntity> getAllProcessingReports(Date tillDate) {
        return repository.getAllProcessingReports(tillDate);
    }

    public static List<SacSupplierProdAvailEntity> getUpdatedProductListInLast24Hrs(String countryIsoCode){
        return repository.getUpdatedProductListInLast24Hrs(countryIsoCode);
    }

    public static boolean hasUpdatedProductListInLast24Hrs(String countryIsoCode){
        return repository.hasUpdatedProductListInLast24Hrs(countryIsoCode);
    }

    public static void updateSacBulkUpload(SacBulkInputDTO bulkInputDTO) {
        repository.updateSacBulkUpload(bulkInputDTO);
    }

    public static void updateSacRequestStatus(Long[] siteIds, String status,String apeReq2Id,String apeReq3Id) {
        repository.updateSacRequestStatus(siteIds, status, apeReq2Id, apeReq3Id);
    }

    public static void updateSacRequestStatus(String fileName, String status,String apeReq2Id,String apeReq3Id) {
        repository.updateSacRequestStatus(fileName, status,apeReq2Id,apeReq3Id);
    }

    public static List<SacRequestEntity> getAllAvailableForProcessingSacRequests(String fileName) {
        return repository.getAllAvailableForProcessingSacRequests(fileName);
    }

    public static SacRequestEntity getFirstAvailableForProcessingSacRequests(String fileName) {
        return repository.getFirstAvailableForProcessingSacRequests(fileName);
    }

    public static SacRequestEntity getSacSiteRequests(String ape2ndReId,String ape3rdReqId) {
        return repository.getSacSiteRequests(ape2ndReId, ape3rdReqId);
    }


    public static SacRequestEntity getSacSiteRequestBySiteId(Long siteId) {
        return repository.getSacSiteRequestBySiteId(siteId);
    }


    public static SacSupplierProdMasterEntity getSacSupplierProdMasterEntity(long siteId, Long supplierId, String spacId) throws Exception {
        return repository.getSacSupplierProdMasterEntity(siteId, supplierId, spacId);
    }

    public static SupplierRequestSiteEntity getSupplierRequestSiteBySiteId(Long siteId, String sccrId) throws Exception {
        return repository.getSupplierRequestSite(siteId, sccrId);
    }

    public static List<Long> getSitesWhichNeedsToBeProcessed(SupplierCheckClientRequestEntity clientRequestEntity) {
        return repository.getSiteIdsByClientRequestId(clientRequestEntity.getId());
    }

    public static void updateSupplierSiteStatusAsFailure(SupplierCheckRequest request, String status, String description) throws Exception {
        for (SupplierSite site : request.getSupplierSites()) {
            repository.updateSupplierSiteStatus(site.getSiteId(), Red.getId(), Joiner.on(" ").join(status, description));
        }
    }

    public static void updateSupplierSiteStatusAsFailureLatest(SupplierCheckRequest request, String status, String description) throws Exception {
        for (SupplierSite site : request.getSupplierSites()) {
            repository.updateSupplierSiteStatusLatest(site.getSiteId(), Red.getId(), Joiner.on(" ").join(status, description));
        }
    }

    public static void storeSupplierRequestSiteEntity(SupplierRequestSiteEntity requestSiteEntity) {
        repository.save(requestSiteEntity);
    }

    public static SacSupplierProdMasterEntity getSuppProdMaster(String spacId) {
        return repository.getSuppProdMaster(spacId);
    }


    public static List<SacRequestEntity> getAllSacSitesWithStatus(String fileName, SacApeStatus status) {
        return repository.getAllSacSitesWithStatus(fileName, status);
    }

    public static List<SacSupplierProdAvailEntity> getAllSacSuppAvailEntityForSite(Long siteId) {
        return repository.getAllSacSuppAvailEntityForSite(siteId);
    }

    public static SacSupplierProdAvailEntity getSacSuppAvailEntityForSite(Long siteId, String spacId) {
        return repository.getSacSuppAvailEntityForSite(siteId, spacId);
    }

    public static void saveSacSupplierProdAvailEntities(List<SacSupplierProdAvailEntity> sacSupplierProdAvailEntities) {
        repository.saveSacSupplierProdAvailEntities(sacSupplierProdAvailEntities);
    }

    public static void saveSacSupplierProdAvailEntity(SacSupplierProdAvailEntity sacSupplierProdAvailEntity) {
        repository.saveSacSupplierProdAvailEntity(sacSupplierProdAvailEntity);
    }

    public static boolean isAllAvailabilityCheckCompleted(String fileName){
       return repository.isAllAvailabilityCheckCompleted(fileName);
    }


    public static boolean isAnyReportGenInprogress(String userId){
        return repository.isAnyReportGenInProgress(userId);
    }

    public static void storeSacSupplierDetails(List<SacSupplierProdAvailEntity> sacSupplierProdAvailEntities) {

        Set<Long> siteIds = new HashSet<Long>();

        Set<String> fileNames = new HashSet<String>();
        repository.saveSacSupProducts(sacSupplierProdAvailEntities);

        for (SacSupplierProdAvailEntity prodAvailEntity : sacSupplierProdAvailEntities) {
            siteIds.add(prodAvailEntity.getSiteId());
        }

        for (Long siteId : siteIds) {
            SacRequestEntity sacRequestEntity = repository.getSacSiteRequestBySiteId(siteId);
            String fileName = sacRequestEntity.getFileName();
            fileNames.add(fileName);
        }

        for (String fileName : fileNames) {
            List<SacRequestEntity> sacRequestEntities = repository.getAllApe2ndReqInitiatedSites(fileName);
            for (SacRequestEntity initiatedRequests : sacRequestEntities) {

                for (SacSupplierProdAvailEntity prodAvailEntity : sacSupplierProdAvailEntities) {
                    prodAvailEntity.setSiteId(initiatedRequests.getSiteId());
                }
                repository.saveSacSupProducts(sacSupplierProdAvailEntities);

            }
        }

        repository.updateSacRequestStatus(siteIds.toArray(new Long[siteIds.size()]), SacApeStatus.APE_RESPONSE_SUCCESS.getStatus(),null,null);

    }

    public static String getIsoCode(String country) {
        return repository.getCountryIsoCode(country);
    }

    public static void storeRequestQueue(List<AvailabilityRequestQueue> queues) {
        repository.saveAvailabilityRequestList(queues);
    }

    public static List<SacBulkUploadEntity> getAllInProcessingSacUploadsBeyond24Hrs(){
       return repository.getAllInProcessingSacUploadsBeyond24Hrs();
    }
}
