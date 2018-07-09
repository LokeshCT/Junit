package com.bt.rsqe.ape.source;

import com.bt.rsqe.ape.config.SupplierCheckConfig;
import com.bt.rsqe.ape.config.TimeoutConfig;
import com.bt.rsqe.ape.dto.AvailabilityParam;
import com.bt.rsqe.ape.dto.AvailabilitySet;
import com.bt.rsqe.ape.dto.Supplier;
import com.bt.rsqe.ape.dto.SupplierCheckRequest;
import com.bt.rsqe.ape.dto.SupplierProduct;
import com.bt.rsqe.ape.dto.SupplierSite;
import com.bt.rsqe.ape.dto.sac.SacSiteDTO;
import com.bt.rsqe.ape.dto.sac.SacSupplierProdAvailDTO;
import com.bt.rsqe.ape.repository.entities.SupplierCheckApeRequestEntity;
import com.bt.rsqe.ape.repository.entities.SupplierCheckClientRequestEntity;
import com.bt.rsqe.ape.repository.entities.SupplierProductEntity;
import com.bt.rsqe.ape.repository.entities.SupplierRequestSiteEntity;
import com.bt.rsqe.ape.repository.entities.SupplierRequestSiteSpacEntity;
import com.bt.rsqe.ape.repository.entities.SupplierSiteEntity;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.bt.rsqe.ape.constants.SupplierProductConstants.*;
import static com.bt.rsqe.ape.dto.SupplierStatus.*;
import static com.bt.rsqe.ape.source.SupplierProductStore.*;
import static com.bt.rsqe.customerinventory.dto.AvailabilityType.*;
import static com.google.common.collect.Lists.*;
import static org.apache.commons.beanutils.BeanUtils.*;
import static org.apache.commons.lang.time.DateUtils.*;

/**
 * Created by 605783162 on 25/08/2015.
 */
public class SupplierProductHelper {

    private static SupplierProductHelperLogger logger = LogFactory.createDefaultLogger(SupplierProductHelperLogger.class);

    public static Date currentDate() {
        return new Date();
    }

    public static Date timestamp(){
        Timestamp stamp = new Timestamp(System.currentTimeMillis());
        return new Date(stamp.getTime());
    }

    public static SupplierCheckRequest cloneRequest(SupplierCheckRequest request) {
        SupplierCheckRequest destReq = new SupplierCheckRequest();
        try {
            copyProperties(destReq, request);
        } catch (IllegalAccessException e) {
            logger.error(e);
        } catch (InvocationTargetException e) {
            logger.error(e);
        }
        return destReq;
    }

    public static List<Long> siteIds(List<SupplierSite> list) {
        return Lists.transform(list, new Function<SupplierSite, Long>() {
            @Override
            public Long apply(@Nullable SupplierSite input) {
                return input.getSiteId();
            }
        });
    }

    public static List<Long> getSiteIds(List<SupplierRequestSiteEntity> list) {
        return Lists.transform(list, new Function<SupplierRequestSiteEntity, Long>() {
            @Override
            public Long apply(@Nullable SupplierRequestSiteEntity input) {
                return Long.parseLong(input.getSiteId());
            }
        });
    }

    public static List<String> spacIds(List<SupplierProductEntity> list) {
        return Lists.transform(list, new Function<SupplierProductEntity, String>() {
            @Override
            public String apply(@Nullable SupplierProductEntity input) {
                return input.getSpacId();
            }
        });
    }

    public static List<SupplierSite> convertToSupplierSite(List<SupplierSiteEntity> list) {
        List<SupplierSite> sites = newArrayList();
        for (SupplierSiteEntity siteEntity : list) {
            SupplierSite site = siteEntity.toDto();
            if (siteEntity.getSupplierProductEntityList() != null) {
                site.setSupplierList(convertToSupplier(siteEntity.getSupplierProductEntityList()));
            }
            sites.add(site);
        }
        return sites;
    }

    public static List<SupplierSite> convertSacToSupplierSite(List<SacSiteDTO> list) {
        List<SupplierSite> sites = newArrayList();
        SupplierSite site = null;
        for (SacSiteDTO siteDTO : list) {
            String teleNo = siteDTO.getTelephoneNo();
            site = new SupplierSite();//siteEntity.toDto();
            site.setAvailabilityTelephoneNumber(teleNo);
            site.setSiteId(Long.parseLong(siteDTO.getSiteId()));
            site.setSiteName(siteDTO.getSiteName());
            site.setCountryISOCode(siteDTO.getCountryIsoCode());
            site.setCountryName(siteDTO.getCountryName());

            List<SacSupplierProdAvailDTO> sacSupplierProdAvailDTOs = siteDTO.getSacSupplierProdAvailDTOs();
            if (sacSupplierProdAvailDTOs != null && sacSupplierProdAvailDTOs.size() > 0) {
                site.setSupplierList(convertSacToSupplier(sacSupplierProdAvailDTOs, teleNo));
            }
            sites.add(site);
        }
        return sites;
    }

    public static List<Supplier> convertSacToSupplier(List<SacSupplierProdAvailDTO> list, final String telephoneNo) {
        HashMap<String, List<SacSupplierProdAvailDTO>> map = groupBySacSupplierName(list);
        List<Supplier> suppliers = newArrayList();
        Supplier supplier = null;

        Set<String> supplierNames =map.keySet();

        if(supplierNames!=null){

            for(String suppName: supplierNames){
                List<SacSupplierProdAvailDTO> sacSupplierProdAvailDTOs = map.get(suppName);
                supplier = new Supplier(Long.parseLong(sacSupplierProdAvailDTOs.get(0).getSiteId()), sacSupplierProdAvailDTOs.get(0).getSupplierProduct().getSupplierId(), suppName, entityToSacSupplierProductDto(sacSupplierProdAvailDTOs,telephoneNo));
                suppliers.add(supplier);
            }
        }

        return suppliers;
    }

    public static List<Supplier> convertToSupplier(List<SupplierProductEntity> list) {
        HashMap<String, List<SupplierProductEntity>> map = groupBySupplierName(list);
        List<Supplier> suppliers = newArrayList();
        Iterator<Map.Entry<String, List<SupplierProductEntity>>> entries = map.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, List<SupplierProductEntity>> entry = entries.next();
            Supplier supplier = new Supplier(entry.getValue().get(0).getSupplierSiteEntity().getSiteId(), entry.getValue().get(0).getSupplierId(), entry.getKey(), entityToSupplierProductDto(entry.getValue()));
            suppliers.add(supplier);
        }

        return suppliers;
    }

    public static HashMap<String, List<SacSupplierProdAvailDTO>> groupBySacSupplierName(List<SacSupplierProdAvailDTO> list) {
        HashMap<String, List<SacSupplierProdAvailDTO>> stringListHashMap = new HashMap<String, List<SacSupplierProdAvailDTO>>();
        for (SacSupplierProdAvailDTO supplierProdAvailDTO : list) {
            SupplierProduct prodMaster = supplierProdAvailDTO.getSupplierProduct();
            String key = prodMaster.getSupplierName();
            if (!stringListHashMap.containsKey(key)) {
                List<SacSupplierProdAvailDTO> productEntities = newArrayList();
                productEntities.add(supplierProdAvailDTO);
                stringListHashMap.put(key, productEntities);
            } else {
                stringListHashMap.get(key).add(supplierProdAvailDTO);
            }
        }
        return stringListHashMap;
    }

    public static HashMap<String, List<SupplierProductEntity>> groupBySupplierName(List<SupplierProductEntity> list) {
        HashMap<String, List<SupplierProductEntity>> stringListHashMap = new HashMap<String, List<SupplierProductEntity>>();
        for (SupplierProductEntity supplierProductEntity : list) {
            String key = supplierProductEntity.getSupplierName();
            if (!stringListHashMap.containsKey(key)) {
                List<SupplierProductEntity> productEntities = newArrayList();
                productEntities.add(supplierProductEntity);
                stringListHashMap.put(key, productEntities);
            } else {
                stringListHashMap.get(key).add(supplierProductEntity);
            }
        }
        return stringListHashMap;
    }


    public static Map<Long, List<String>> getSiteProductMap(SupplierCheckRequest request) {
        Map<Long, List<String>> siteProductMap = new HashMap<Long, List<String>>();
        for (SupplierSite site : request.getSupplierSites()) {
            Long key = site.getSiteId().longValue();
            for (Supplier supplier : site.getSupplierList()) {
                for (SupplierProduct product : supplier.getSupplierProductList()) {
                    if (!siteProductMap.containsKey(key)) {
                        List<String> spacIds = newArrayList();
                        spacIds.add(product.getSpacId());
                        siteProductMap.put(key, spacIds);
                    } else {
                        siteProductMap.get(key).add(product.getSpacId());
                    }
                }
            }
        }
        return siteProductMap;
    }

    public static List<SupplierSiteEntity> getSupplierSitesWithSelectedProducts(SupplierCheckRequest request) throws Exception {
        Map<Long, List<String>> siteProductMap = getSiteProductMap(request);
        List<SupplierSiteEntity> siteEntityLists = newArrayList();
        try {
            SupplierSiteEntity siteEntity = null;
            Iterator<Map.Entry<Long, List<String>>> entries = siteProductMap.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<Long, List<String>> entry = entries.next();
                siteEntity = SupplierProductStore.getSupplierSiteEntity(entry.getKey());
                siteEntity.setSupplierProductEntityList(SupplierProductStore.getSupplierProductBySiteIdAndSpacIds(entry.getKey(), entry.getValue()));
                siteEntityLists.add(siteEntity);
            }
        } catch (Exception e) {
            logger.error(e);
        }

        return siteEntityLists;
    }

    public static List<SupplierProduct> entityToSacSupplierProductDto(List<SacSupplierProdAvailDTO> list, String telephoneNo) {
        List<SupplierProduct> supplierProducts = newArrayList();
        for (SacSupplierProdAvailDTO prodAvailDTO : list) {
            SupplierProduct supplierProduct = prodAvailDTO.getSupplierProduct();
            try {
                supplierProduct = supplierProduct.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            supplierProduct.setAvailabilitySets(createAvailabilitySet(telephoneNo));
            supplierProducts.add(supplierProduct);
        }
        return supplierProducts;
    }

    private static List<AvailabilitySet> createAvailabilitySet(String telephoneNo){
        final List<AvailabilitySet> availabilitySets = new ArrayList<>();
        final List<AvailabilityParam> parameterList = new ArrayList<AvailabilityParam>();

        AvailabilityParam availabilityParam = new AvailabilityParam();
        availabilityParam.setName("Telephone Number");
        availabilityParam.setValue(telephoneNo);
        parameterList.add(availabilityParam);

        AvailabilitySet availabilitySet = new AvailabilitySet();
        availabilitySet.setSetName("Set 1");
        availabilitySet.setParameterList(parameterList);

        availabilitySets.add(availabilitySet);
        return availabilitySets;
    }

    public static List<SupplierProduct> entityToSupplierProductDto(List<SupplierProductEntity> list) {
        List<SupplierProduct> supplierProducts = newArrayList();
        for (SupplierProductEntity entity : list) {
            supplierProducts.add(entity.toDto());
        }
        return supplierProducts;
    }

   public static List<SupplierSiteEntity> getSiteWithAutoSupplier(final List<SupplierSiteEntity> sites) {
       List<SupplierSiteEntity> siteToProcess = newArrayList();
       for (SupplierSiteEntity input : sites) {
           List<SupplierProductEntity> autoSupplierProducts = getAutoSupplierProduct(input.getSupplierProductEntityList());
           if (autoSupplierProducts.size() > 0) {
               SupplierSiteEntity supplierSiteEntity = new SupplierSiteEntity(input.getSiteId(), input.getCustomerId(), input.getSiteName(), input.getCountryISOCode(), input.getCountryName(),
                                                                              input.getExpiryDate(), input.getAvailabilityTypeId(), input.getAvailabilityTelephoneNumber(), input.getErrorDescription(),
                                                                              autoSupplierProducts, input.getTimeout());
               siteToProcess.add(supplierSiteEntity);
           }
       }
       return siteToProcess;
   }

    public static List<SupplierProductEntity> getAutoSupplierProduct(List<SupplierProductEntity> items) {
        return newArrayList(Iterables.filter(items, new Predicate<SupplierProductEntity>() {
            @Override
            public boolean apply(SupplierProductEntity input) {
                return isSupplierProductTypeOfAuto(input);
            }
        }));
    }

    private static boolean isSupplierProductTypeOfAuto(SupplierProductEntity entity) {
        return AUTO.equalsIgnoreCase(entity.getAvailabilityCheckType()) && !InProgress.value().equalsIgnoreCase(entity.getStatus());
    }

    public static List<SupplierSiteEntity> getSiteWithManualSupplier(final List<SupplierSiteEntity> sites) {
        List<SupplierSiteEntity> siteToProcess = newArrayList();
        for (SupplierSiteEntity input : sites) {
            List<SupplierProductEntity> manualSupplierProducts = getManualSupplierProduct(input.getSupplierProductEntityList());
            if (manualSupplierProducts.size() > 0) {
                SupplierSiteEntity supplierSiteEntity = new SupplierSiteEntity(input.getSiteId(), input.getCustomerId(), input.getSiteName(), input.getCountryISOCode(), input.getCountryName(),
                                                                               input.getExpiryDate(), input.getAvailabilityTypeId(), input.getAvailabilityTelephoneNumber(), input.getErrorDescription(),
                                                                               manualSupplierProducts, input.getTimeout());
                siteToProcess.add(supplierSiteEntity);
            }
        }
        return siteToProcess;
    }

    public static List<SupplierProductEntity> getManualSupplierProduct(List<SupplierProductEntity> items) {
        return newArrayList(Iterables.filter(items, new Predicate<SupplierProductEntity>() {
            @Override
            public boolean apply(SupplierProductEntity input) {
                return isSupplierProductTypeOfManual(input);
            }
        }));
    }

    private static boolean isSupplierProductTypeOfManual(SupplierProductEntity entity) {
        return MANUAL.equalsIgnoreCase(entity.getAvailabilityCheckType()) && !InProgress.value().equalsIgnoreCase(entity.getStatus()) && YES.equalsIgnoreCase(entity.getCentralizedAvailabilitySupported());
    }

    public static void storeApeRequestLog(SupplierCheckRequest request, String operation, String availCheckType) {
        try {
            SupplierCheckClientRequestEntity clientRequest = SupplierProductStore.getClientRequestById(request.getParentRequestId());
            SupplierCheckApeRequestEntity apeRequestEntity = new SupplierCheckApeRequestEntity(request.getRequestId(), operation, availCheckType, InProgress.value(), timestamp(), timestamp(), clientRequest);

            if (clientRequest.getSupplierCheckApeRequestEntities() == null || clientRequest.getSupplierCheckApeRequestEntities().isEmpty()) {
                clientRequest.setSupplierCheckApeRequestEntities(Lists.newArrayList(apeRequestEntity));
            } else {
                clientRequest.getSupplierCheckApeRequestEntities().add(apeRequestEntity);
            }

            List<SupplierRequestSiteEntity> siteEntities = newArrayList();
            if (request.getSupplierSites() != null) {
                for (SupplierSite site : request.getSupplierSites()) {
                    SupplierRequestSiteEntity siteEntity = SupplierProductStore.getSupplierRequestSiteBySiteId(site.getSiteId(), clientRequest.getId());
                    if (site.getSupplierList() != null) {
                        for (Supplier supplier : site.getSupplierList()) {
                            List<SupplierRequestSiteSpacEntity> spacEntities = newArrayList();
                            for (SupplierProduct product : supplier.getSupplierProductList()) {
                                if (siteEntity == null) {
                                    siteEntity = new SupplierRequestSiteEntity(String.valueOf(site.getSiteId()), InProgress.value(), InProgress.value(), timestamp(), timestamp(), clientRequest);
                                }
                                spacEntities.add(new SupplierRequestSiteSpacEntity(product.getSpacId(), InProgress.value(), InProgress.value(), timestamp(), timestamp(), siteEntity));
                            }
                            siteEntity.setSupplierRequestSiteSpacEntities(spacEntities);
                        }
                        siteEntities.add(siteEntity);
                    }
                }
            }
            clientRequest.setSupplierRequestSiteEntities(siteEntities);
            storeClientRequest(clientRequest);
            logger.storedApeResponse();
        } catch (Exception e) {
            logger.error(e);
        }
    }

    public static void storeApeRequestLogLatest(SupplierCheckRequest request, String operation, String availCheckType) {
        try {
            SupplierCheckClientRequestEntity clientRequestDb = SupplierProductStore.getClientRequestById(request.getParentRequestId());
            SupplierCheckClientRequestEntity clientRequest = null;
            if (clientRequestDb == null) {
                clientRequest = new SupplierCheckClientRequestEntity(request.getParentRequestId(), request.getClientCallbackUri(), request.getTriggerType(), request.getAutoTrigger(),
                                                                     request.getSourceSystemName(), request.getUser(), Long.parseLong(request.getCustomerId()), InProgress.value(), timestamp(), timestamp());
            } else {
                clientRequest = clientRequestDb;
            }

            SupplierCheckApeRequestEntity apeRequestEntity = new SupplierCheckApeRequestEntity(request.getRequestId(), operation, availCheckType, InProgress.value(), timestamp(), timestamp(), clientRequest);

            if (clientRequest.getSupplierCheckApeRequestEntities() == null || clientRequest.getSupplierCheckApeRequestEntities().isEmpty()) {
                clientRequest.setSupplierCheckApeRequestEntities(Lists.newArrayList(apeRequestEntity));
            } else {
                clientRequest.getSupplierCheckApeRequestEntities().add(apeRequestEntity);
            }

            List<SupplierRequestSiteEntity> siteEntities = newArrayList();
            if (request.getSupplierSites() != null) {
                for (SupplierSite site : request.getSupplierSites()) {
                    SupplierRequestSiteEntity siteEntity = SupplierProductStore.getSupplierRequestSiteBySiteId(site.getSiteId(), clientRequest.getId());
                    if (site.getSupplierList() != null) {
                        for (Supplier supplier : site.getSupplierList()) {
                            List<SupplierRequestSiteSpacEntity> spacEntities = newArrayList();
                            for (SupplierProduct product : supplier.getSupplierProductList()) {
                                if (siteEntity == null) {
                                    siteEntity = new SupplierRequestSiteEntity(String.valueOf(site.getSiteId()), InProgress.value(), InProgress.value(), timestamp(), timestamp(), clientRequest);
                                }
                                spacEntities.add(new SupplierRequestSiteSpacEntity(product.getSpacId(), InProgress.value(), InProgress.value(), timestamp(), timestamp(), siteEntity));
                            }
                            siteEntity.setSupplierRequestSiteSpacEntities(spacEntities);
                        }
                        siteEntities.add(siteEntity);
                    }
                }
            }
            clientRequest.setSupplierRequestSiteEntities(siteEntities);
            storeClientRequestlatest(clientRequest);
            logger.storedApeResponse();
        } catch (Exception e) {
            logger.error(e);
        }
    }

    public static void storeSacApeRequestLog(SupplierCheckRequest request, String operation, String availCheckType) {
        try {
            SupplierCheckClientRequestEntity clientRequest = SupplierProductStore.getClientRequestById(request.getParentRequestId());
            SupplierCheckApeRequestEntity apeRequestEntity = new SupplierCheckApeRequestEntity(request.getRequestId(), operation, availCheckType, InProgress.value(), timestamp(), timestamp(), clientRequest);
            if (clientRequest.getSupplierCheckApeRequestEntities() == null || clientRequest.getSupplierCheckApeRequestEntities().isEmpty()) {
                clientRequest.setSupplierCheckApeRequestEntities(Lists.newArrayList(apeRequestEntity));
            }
            clientRequest.getSupplierCheckApeRequestEntities().add(apeRequestEntity);
            apeRequestEntity.setSupplierCheckClientRequestEntity(clientRequest);

            storeApeRequest(apeRequestEntity);

        } catch (Exception e) {
            logger.error(e);
        }
    }

    public static void updateTimeoutForSupplierProductEntity(SupplierCheckRequest request, int timeoutInMin) {
        try {
            for (SupplierSite site : request.getSupplierSites()) {
                for (Supplier supplier : site.getSupplierList()) {
                    for (SupplierProduct product : supplier.getSupplierProductList()) {

                        List<SupplierProductEntity> list = SupplierProductStore.getSupplierProductBySiteIdAndSpacIds(site.getSiteId(), Lists.newArrayList(product.getSpacId()));
                        if (list.size() > 0) {
                            SupplierProductEntity productEntity = list.get(0);
                            productEntity.setRequestedTime(timestamp());
                            productEntity.setRequestTimeout(addMinutes(timestamp(), timeoutInMin));
                            productEntity.setStatus(InProgress.value());
                            productEntity.setMandatroyAttributes("");
                            productEntity.setDescription("");
                            SupplierProductStore.storeSupplierProduct(productEntity);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }

    public static void updateTimeoutForSupplierProducts(SupplierCheckRequest request, int timeoutInMin) {
        try {
            for (SupplierSite site : request.getSupplierSites()) {
                for (Supplier supplier : site.getSupplierList()) {
                    for (SupplierProduct product : supplier.getSupplierProductList()) {
                        List<String> productList = SupplierProductStore.getProductsBySiteAndSupplier(site.getSiteId(), Lists.newArrayList(product.getSpacId()));
                        if (productList.size() > 0) {
                            Date requestedTime = timestamp();
                            SupplierProductStore.updateSupplierProduct(productList, requestedTime, addMinutes(requestedTime, timeoutInMin), InProgress.value(), "", "");
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }

    public static List<SiteDTO> filterOutNonSupportedSites(List<SiteDTO> siteDTOList, final List<String> supportedCountries) {
        return newArrayList(Iterables.filter(siteDTOList, new Predicate<SiteDTO>() {
            @Override
            public boolean apply(SiteDTO input) {
                return supportedCountries.contains(input.countryISOCode());
            }
        }));
    }

    public static List<SiteDTO> getNonExistingSites(List<SiteDTO> supportedSites, final List<Long> existingSites) {
        return newArrayList(Iterables.filter(supportedSites, new Predicate<SiteDTO>() {
            @Override
            public boolean apply(SiteDTO input) {
                return !existingSites.contains(BigDecimal.valueOf(input.getSiteId().getValue()));
            }
        }));
    }

    public static List<SupplierSiteEntity> getNonExistingSupplierSites(List<SupplierSiteEntity> supportedSites, final List<Long> existingSites) {
        return newArrayList(Iterables.filter(supportedSites, new Predicate<SupplierSiteEntity>() {
            @Override
            public boolean apply(SupplierSiteEntity input) {
                return !existingSites.contains(BigDecimal.valueOf(input.getSiteId()));
            }
        }));
    }

    public static void filterExistingSitesFromRequest(SupplierCheckRequest request, final List<Long> existingSites) {
        for (Iterator<SupplierSite> iterator = request.getSupplierSites().iterator(); iterator.hasNext(); ) {
            SupplierSite site = iterator.next();
            if (existingSites.contains(BigDecimal.valueOf(site.getSiteId()))) {
                iterator.remove();
            }
        }
    }

    public static List<SupplierSiteEntity> getSupplierSiteEntityList(final SupplierCheckRequest request, final SupplierCheckConfig config) {
        return newArrayList(Lists.transform(request.getSupplierSites(), new Function<SupplierSite, SupplierSiteEntity>() {
            @Override
            public SupplierSiteEntity apply(@Nullable SupplierSite input) {
                Long cusId = request.getCustomerId() == null ? null : Long.parseLong(request.getCustomerId());
                return new SupplierSiteEntity(input.getSiteId(), cusId, input.getSiteName(), input.getCountryISOCode(),
                                              input.getCountryName(), addMonths(currentDate(), Integer.parseInt(config.getDataExpiryConfig().getValue())), Grey.getId(), input.getAvailabilityTelephoneNumber(), input.getErrorDescription(), null, addMinutes(currentDate(), config.getServiceConfig().getTimeoutConfig(TimeoutConfig.PRODUCT_LIST).getValue()));
            }
        }));
    }

    public static void excludeInValidProducts(HashMap<String, Boolean> validatorMap, SupplierCheckRequest request) {
        for (SupplierSite site : request.getSupplierSites()) {
            for (Supplier supplier : site.getSupplierList()) {
                for (Iterator<com.bt.rsqe.ape.dto.SupplierProduct> iterator = supplier.getSupplierProductList().iterator(); iterator.hasNext(); ) {
                    SupplierProduct product = iterator.next();
                    if ((validatorMap.size() > 0)) {
                        if (validatorMap.get(product.getSpacId()) == null || !validatorMap.get(product.getSpacId())) {
                            logger.removingSupplierProductAsItIsInvalid(product.getSpacId());
                            iterator.remove();
                        }
                    }
                }
            }
        }
    }

    public static Document getDocument(String xml) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(new InputSource(new ByteArrayInputStream(xml.getBytes("utf-8"))));
        doc.getDocumentElement().normalize();
        return doc;
    }

    public static String getElementValue(Element element, String xpath) {
        return element.getElementsByTagName(xpath).item(0).getTextContent();
    }

    private interface SupplierProductHelperLogger {
        @Log(level = LogLevel.ERROR, format = "Error : '%s'")
        void error(Exception e);

        @Log(level = LogLevel.ERROR, format = "Removing Supplier Product As It Is Invalid : '%s'")
        void removingSupplierProductAsItIsInvalid(String spacId);

        @Log(level = LogLevel.INFO, format = "Stored")
        void storedApeResponse();

    }

}
