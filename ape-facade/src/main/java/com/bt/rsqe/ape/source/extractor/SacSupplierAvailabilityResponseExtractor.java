package com.bt.rsqe.ape.source.extractor;

import com.bt.rsqe.ape.repository.entities.SacRequestEntity;
import com.bt.rsqe.ape.repository.entities.SacSupplierProdAvailEntity;
import com.bt.rsqe.ape.repository.entities.SacSupplierProdMasterEntity;
import com.bt.rsqe.ape.repository.entities.SacSupplierProdMasterPK;
import com.bt.rsqe.ape.source.SupplierProductStore;
import com.bt.rsqe.customerrecord.SiteResource;
import com.bt.rsqe.utils.AssertObject;
import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.bt.rsqe.ape.dto.SupplierStatus.Completed;
import static com.bt.rsqe.ape.source.SupplierProductHelper.currentDate;
import static com.bt.rsqe.ape.source.SupplierProductStore.*;
import static com.google.common.collect.Lists.*;

public class SacSupplierAvailabilityResponseExtractor extends ResponseExtractorStrategy {
    org.slf4j.Logger LOG = LoggerFactory.getLogger(SacSupplierAvailabilityResponseExtractor.class);

    @Override
    public void extractResponse(String response, SiteResource siteResource) throws Exception {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new InputSource(new ByteArrayInputStream(response.getBytes("utf-8"))));
            doc.getDocumentElement().normalize();
            String requestId = doc.getElementsByTagName("requestId").item(0).getTextContent();
            List<SacSupplierProdAvailEntity> sacSupplierProdAvailEntities = newArrayList();
            //List<SacSupplierProdMasterEntity> supplierProductEntityList = null;
            //SupplierSiteEntity supplierSiteEntity = null;
            SacRequestEntity sacSiteRequestsEntity = null;
            String siteId = null;
            String siteName = null;

            boolean isResponseValidToParse = true;
            isResponseValidToParse = isResponseValidToParse(doc);

            if (isResponseValidToParse) {
                LOG.info("Going to parse SAC Availability APE Response ...");
                NodeList siteNodeList = doc.getElementsByTagName("SiteDetailsAvailabiltyRes");
                for (int siteNodeIndex = 0; siteNodeIndex < siteNodeList.getLength(); siteNodeIndex++) {
                    Node siteNode = siteNodeList.item(siteNodeIndex);
                    if (siteNode.getNodeType() == Node.ELEMENT_NODE) {
                        sacSiteRequestsEntity = new SacRequestEntity();
                        Element siteElement = (Element) siteNode;
                        siteId = getElementValue(siteElement, "siteId");
                        siteName = getElementValue(siteElement, "siteName");

                        NodeList supplierNodeList = ((Element) siteNode).getElementsByTagName("SupplierResponse");
                        for (int supplierNodeIndex = 0; supplierNodeIndex < supplierNodeList.getLength(); supplierNodeIndex++) {
                            Node supplierNode = supplierNodeList.item(supplierNodeIndex);
                            if (supplierNode.getNodeType() == Node.ELEMENT_NODE) {
                                Element supplierElement = (Element) supplierNode;
                                //supplierProductEntityList = newArrayList();
                                sacSupplierProdAvailEntities = newArrayList();

                                SacSupplierProdAvailEntity sacSupplierProdAvailEntity = null;
                                //SacSupplierProdMasterEntity sacSupplierProdMasterEntity = null;

                                NodeList supplierProductNodeList = ((Element) supplierNode).getElementsByTagName("SupplierProductResponse");
                                for (int supplierProductNodeIndex = 0; supplierProductNodeIndex < supplierProductNodeList.getLength(); supplierProductNodeIndex++) {
                                    Node supplierProductNode = supplierProductNodeList.item(supplierProductNodeIndex);
                                    if (supplierProductNode.getNodeType() == Node.ELEMENT_NODE) {
                                        Element supplierProductElement = (Element) supplierProductNode;
                                        //sacSupplierProdAvailEntity = new SacSupplierProdAvailEntity();
                                        //sacSupplierProdMasterEntity = new SacSupplierProdMasterEntity();
                                        sacSupplierProdAvailEntity = new SacSupplierProdAvailEntity();

                                        /*sacSupplierProdMasterEntity.setSupplierId(Long.parseLong(getElementValue(supplierElement, "supplierId")));
                                        sacSupplierProdMasterEntity.setSupplierName(getElementValue(supplierElement, "supplierName"));*/
                                        sacSupplierProdAvailEntity.setSiteId(Long.parseLong(siteId));
                                        if (isSafeToExract(supplierProductElement, "SPACID")) {
                                            sacSupplierProdAvailEntity.setSpacId(getElementValue(supplierProductElement, "SPACID"));
                                        }
                                        if (isSafeToExract(supplierProductElement, "AvailabilityStatus")) {
                                            sacSupplierProdAvailEntity.setAvailStatus(getElementValue(supplierProductElement, "AvailabilityStatus"));
                                        }

                                        /* if (isSafeToExract(supplierProductElement, "DisplaySupplierProductName")) {
                                            sacSupplierProdMasterEntity.setDisplaySupplierProductName(getElementValue(supplierProductElement, "DisplaySupplierProductName"));
                                        }
                                        if (isSafeToExract(supplierProductElement, "AvailabilityCheckType")) {
                                            sacSupplierProdMasterEntity.setAvailabilityCheckType(getElementValue(supplierProductElement, "AvailabilityCheckType"));
                                        }
                                        if (isSafeToExract(supplierProductElement, "ServiceVariant")) {
                                            sacSupplierProdMasterEntity.setServiceVariant(getElementValue(supplierProductElement, "ServiceVariant"));
                                        }
                                        if (isSafeToExract(supplierProductElement, "CheckReference")) {
                                            sacSupplierProdMasterEntity.setCheckedReference(getElementValue(supplierProductElement, "CheckReference"));
                                        }
                                        if (isSafeToExract(supplierProductElement, "SymetricBandwidth")) {
                                            sacSupplierProdMasterEntity.setSymmetricSpeedBandwidth(getElementValue(supplierProductElement, "SymetricBandwidth"));
                                        }
                                        if (isSafeToExract(supplierProductElement, "MaxUpstreamBadwidth")) {
                                            sacSupplierProdMasterEntity.setMaxUpstreamSpeedBandwidth(getElementValue(supplierProductElement, "MaxUpstreamBadwidth"));
                                        }
                                        if (isSafeToExract(supplierProductElement, "MaxDownstreamBandwidth")) {
                                            sacSupplierProdMasterEntity.setMaxDownstreamBandwidth(getElementValue(supplierProductElement, "MaxDownstreamBandwidth"));
                                        }
                                        if (isSafeToExract(supplierProductElement, "ExchangeCode")) {
                                            sacSupplierProdMasterEntity.setExchangeCode(getElementValue(supplierProductElement, "ExchangeCode"));
                                        }
                                        if (isSafeToExract(supplierProductElement, "NumberOfCopperPairs")) {
                                            sacSupplierProdMasterEntity.setNumberOfCopperPairs(Long.parseLong(getElementValue(supplierProductElement, "NumberOfCopperPairs")));
                                        }
                                        if (isSafeToExract(supplierProductElement, "AvailabilityDescription")) {
                                            sacSupplierProdMasterEntity.setAvailabilityDescription(getElementValue(supplierProductElement, "AvailabilityDescription"));
                                        }*/

                                        sacSupplierProdAvailEntities.add(sacSupplierProdAvailEntity);

                                    }
                                }
                            }
                        }
                        LOG.info(String.format("Going to Persist SAC Availability to DB. Request ID :%s , Site ID : %s , Site Name :%s ",requestId,siteId,siteName));
                        try{
                            updateSupplierAvailability(siteId, sacSupplierProdAvailEntities, requestId);
                            LOG.info(String.format("Persisted SAC Availability to DB. Request ID :%s , Site ID : %s , Site Name : %s",requestId,siteId,siteName));
                        }catch (Exception ex){
                            LOG.error(String.format("Failed to persist SAC Availability to DB for Request ID :%s , Site ID : %s , Site Name : %s",requestId,siteId,siteName));
                        }

                    }
                }
            }else{
                LOG.warn("Invalid Availability Response to Parse !! Response ->"+response);
            }
        } catch (Exception e) {
            LOG.error("FAILED to extract SAC Availability from APE Response !!", e);
            throw e;
        }
    }

    private boolean isResponseValidToParse(Document doc) throws Exception {
        boolean isResponseValidToParse = true;
        String description = null;
        NodeList errorDetailNodeList = doc.getElementsByTagName("ErrorDetails");
        for (int errorDetailNodeIndex = 0; errorDetailNodeIndex < errorDetailNodeList.getLength(); errorDetailNodeIndex++) {
            Node errorDetailNode = errorDetailNodeList.item(errorDetailNodeIndex);
            if (errorDetailNode.getNodeType() == Node.ELEMENT_NODE) {
                Element errorDetailElement = (Element) errorDetailNode;
                String status = getElementValue(errorDetailElement, "status");
                description = getElementValue(errorDetailElement, "description");
                if (StringUtils.contains(status, "[Failure]")) {
                    isResponseValidToParse = false;
                }
            }
        }

        if (!isResponseValidToParse) {
            handleError(doc, description);
        }
        return isResponseValidToParse;
    }

    private void handleError(Document doc, String failureMessage) throws Exception {
        NodeList siteDetailAvailNodeList = doc.getElementsByTagName("SiteDetailsAvailabiltyRes");
        for (int siteDetailAvailIndex = 0; siteDetailAvailIndex < siteDetailAvailNodeList.getLength(); siteDetailAvailIndex++) {
            Node siteDetailAvailNode = siteDetailAvailNodeList.item(siteDetailAvailIndex);
            if (siteDetailAvailNode.getNodeType() == Node.ELEMENT_NODE) {
                Element siteDetailAvailElement = (Element) siteDetailAvailNode;
                String siteId = getElementValue(siteDetailAvailElement, "siteId");

                NodeList supplierResponseNodeList = doc.getElementsByTagName("SupplierResponse");
                for (int supplierResponseNodeIndex = 0; supplierResponseNodeIndex < supplierResponseNodeList.getLength(); supplierResponseNodeIndex++) {
                    Node supplierResponseNode = supplierResponseNodeList.item(supplierResponseNodeIndex);
                    if (supplierResponseNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element supplierResponseElement = (Element) supplierResponseNode;
                        String supplierId = isSafeToExract(supplierResponseElement, "supplierId") ? getElementValue(supplierResponseElement, "supplierId") : null;
                        String spacId = isSafeToExract(supplierResponseElement, "SPACID") ? getElementValue(supplierResponseElement, "SPACID") : null;

                        /*SacSupplierProdMasterEntity productEntity = getSacSupplierProdMasterEntity(Long.parseLong(siteId), Long.parseLong(supplierId), spacId);
                        productEntity.setStatus("Failed");
                        productEntity.setDescription(failureMessage);
                        SupplierProductStore.storeSacSupplierProdMaster(productEntity);*/
                        SacSupplierProdAvailEntity sacSupplierProdAvailEntity = getSacSuppAvailEntityForSite(Long.parseLong(siteId),spacId);
                        sacSupplierProdAvailEntity.setStatus("Failed");
                        sacSupplierProdAvailEntity.setErrorDesc(failureMessage);
                        sacSupplierProdAvailEntity.setUpdateDate(new Timestamp(new Date().getTime()));
                        SupplierProductStore.saveSacSupplierProdAvailEntity(sacSupplierProdAvailEntity);
                    }
                }
            }
        }
    }

    private void updateSupplierAvailability(String siteId, List<SacSupplierProdAvailEntity> sacSupplierProdAvailEntities, String requestId) {
        List<SacSupplierProdAvailEntity> dbEntities = new ArrayList<SacSupplierProdAvailEntity>();
        for (SacSupplierProdAvailEntity sacSupplierProdAvailEntity : sacSupplierProdAvailEntities) {
            SacSupplierProdAvailEntity dbEntity = getSacSuppAvailEntityForSite(Long.parseLong(siteId), sacSupplierProdAvailEntity.getSpacId());
            dbEntity.setAvailStatus(sacSupplierProdAvailEntity.getAvailStatus());
            dbEntity.setUpdateDate(new Timestamp(currentDate().getTime()));
            dbEntity.setStatus("Completed");
            dbEntities.add(dbEntity);
        }

        saveSacSupplierProdAvailEntities(dbEntities);
    }

/*    private void storeSupplierSiteEntity(List<SacSupplierProdMasterEntity> supplierSiteEntityList, String requestId) {
        try {
            Set<SacSupplierProdMasterPK> suppSpacIds = new HashSet<SacSupplierProdMasterPK>();
            for (SacSupplierProdMasterEntity tempProdEntity : supplierSiteEntityList) {
                SacSupplierProdMasterEntity productEntity = getSacSupplierProdMasterEntity(tempProdEntity.getSiteId(), tempProdEntity.getSupplierId(), tempProdEntity.getSpacId());
                updateWithChanges(productEntity, tempProdEntity);
                productEntity.setStatus(Completed.value());
                Date now = new Date();
                productEntity.setUpdateDate(now);
                storeSacSupplierProdMaster(productEntity);

                suppSpacIds.add(new SacSupplierProdMasterPK(tempProdEntity.getSiteId(), tempProdEntity.getSpacId()));
            }

            updateSacSiteAvailStatus((SacSupplierProdMasterPK[]) suppSpacIds.toArray(), Completed.value());


        } catch (Exception e) {
            LOG.error("Unable to save supplier site response", e);
            e.printStackTrace();
        }
    }*/

/*    private void updateWithChanges(SacSupplierProdMasterEntity productEntity, SacSupplierProdMasterEntity tempProd) {
        if (AssertObject.isNotNull(tempProd.getAvailabilityDescription())) {
            productEntity.setAvailabilityDescription(tempProd.getAvailabilityDescription());
        }
        if (AssertObject.isNotNull(tempProd.getNumberOfCopperPairs())) {
            productEntity.setNumberOfCopperPairs(tempProd.getNumberOfCopperPairs());
        }
        if (AssertObject.isNotNull(tempProd.getExchangeCode())) {
            productEntity.setExchangeCode(tempProd.getExchangeCode());
        }
        if (AssertObject.isNotNull(tempProd.getMaxDownstreamBandwidth())) {
            productEntity.setMaxDownstreamBandwidth(tempProd.getMaxDownstreamBandwidth());
        }
        if (AssertObject.isNotNull(tempProd.getMaxUpstreamSpeedBandwidth())) {
            productEntity.setMaxUpstreamSpeedBandwidth(tempProd.getMaxUpstreamSpeedBandwidth());
        }
        if (AssertObject.isNotNull(tempProd.getSymmetricSpeedBandwidth())) {
            productEntity.setSymmetricSpeedBandwidth(tempProd.getSymmetricSpeedBandwidth());
        }
        if (AssertObject.isNotNull(tempProd.getCheckedReference())) {
            productEntity.setCheckedReference(tempProd.getCheckedReference());
        }
        if (AssertObject.isNotNull(tempProd.getAvailabilityStatus())) {
            productEntity.setAvailabilityStatus(tempProd.getAvailabilityStatus());
        }
        if (AssertObject.isNotNull(tempProd.getServiceVariant())) {
            productEntity.setServiceVariant(tempProd.getServiceVariant());
        }
        if (AssertObject.isNotNull(tempProd.getAvailabilityCheckType())) {
            productEntity.setAvailabilityCheckType(tempProd.getAvailabilityCheckType());
        }
        if (AssertObject.isNotNull(tempProd.getDisplaySupplierProductName())) {
            productEntity.setDisplaySupplierProductName(tempProd.getDisplaySupplierProductName());
        }
    }*/


    boolean isSafeToExract(Element element, String xpath) {
        return element.getElementsByTagName(xpath).item(0) != null;
    }
}
