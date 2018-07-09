package com.bt.rsqe.ape.source.extractor;

import com.bt.rsqe.ape.repository.entities.SupplierProductEntity;
import com.bt.rsqe.ape.repository.entities.SupplierRequestSiteEntity;
import com.bt.rsqe.ape.repository.entities.SupplierRequestSiteSpacEntity;
import com.bt.rsqe.ape.repository.entities.SupplierSiteEntity;
import com.bt.rsqe.customerrecord.SiteResource;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.List;

import static com.bt.rsqe.ape.constants.SupplierProductConstants.*;
import static com.bt.rsqe.ape.dto.SupplierStatus.*;
import static com.bt.rsqe.ape.source.SupplierProductHelper.*;
import static com.bt.rsqe.ape.source.SupplierProductStore.*;
import static com.bt.rsqe.utils.AssertObject.*;
import static com.google.common.collect.Lists.*;

/**
 * Created by 605783162 on 09/08/2015.
 */
public class SupplierAvailabilityResponseExtractor extends ResponseExtractorStrategy {

    // APEQrefJPARepository repository = null;

    @Override
    public void extractResponse(String response, SiteResource siteResource) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new InputSource(new ByteArrayInputStream(response.getBytes("utf-8"))));
            doc.getDocumentElement().normalize();
            String requestId = doc.getElementsByTagName("requestId").item(0).getTextContent();
            logger.startedExtractingAvailabilityResponseForRequest(requestId);
            List<SupplierSiteEntity> supplierSiteEntityList = newArrayList();
            SupplierSiteEntity supplierSiteEntity = null;

            boolean isResponseValidToParse = true;
            isResponseValidToParse = isResponseValidToParse(doc);

            if (isResponseValidToParse) {
                NodeList siteNodeList = doc.getElementsByTagName("SiteDetailsAvailabiltyRes");
                for (int siteNodeIndex = 0; siteNodeIndex < siteNodeList.getLength(); siteNodeIndex++) {
                    Node siteNode = siteNodeList.item(siteNodeIndex);
                    if (siteNode.getNodeType() == Node.ELEMENT_NODE) {
                        supplierSiteEntity = new SupplierSiteEntity();
                        Element siteElement = (Element) siteNode;
                        supplierSiteEntity.setSiteId(Long.parseLong(getElementValue(siteElement, "siteId")));
                        supplierSiteEntity.setSiteName(getElementValue(siteElement, "siteName"));

                        NodeList supplierNodeList = ((Element) siteNode).getElementsByTagName("SupplierResponse");
                        for (int supplierNodeIndex = 0; supplierNodeIndex < supplierNodeList.getLength(); supplierNodeIndex++) {
                            Node supplierNode = supplierNodeList.item(supplierNodeIndex);
                            if (supplierNode.getNodeType() == Node.ELEMENT_NODE) {
                                Element supplierElement = (Element) supplierNode;
                                List<SupplierProductEntity> supplierProductEntityList = newArrayList();
                                SupplierProductEntity supplierProductEntity = null;
                                NodeList supplierProductNodeList = ((Element) supplierNode).getElementsByTagName("SupplierProductResponse");
                                for (int supplierProductNodeIndex = 0; supplierProductNodeIndex < supplierProductNodeList.getLength(); supplierProductNodeIndex++) {
                                    Node supplierProductNode = supplierProductNodeList.item(supplierProductNodeIndex);
                                    if (supplierProductNode.getNodeType() == Node.ELEMENT_NODE) {
                                        Element supplierProductElement = (Element) supplierProductNode;
                                        supplierProductEntity = new SupplierProductEntity();
                                        supplierProductEntity.setSupplierId(Long.parseLong(getElementValue(supplierElement, SUPPLIER_ID)));
                                        supplierProductEntity.setSupplierName(getElementValue(supplierElement, "supplierName"));

                                        if (isSafeToExract(supplierProductElement, SPACID))
                                            supplierProductEntity.setSpacId(getElementValue(supplierProductElement, SPACID));
                                        if (isSafeToExract(supplierProductElement, "DisplaySupplierProductName"))
                                            supplierProductEntity.setDisplaySupplierProductName(getElementValue(supplierProductElement, "DisplaySupplierProductName"));
                                        if (isSafeToExract(supplierProductElement, "AvailabilityCheckType"))
                                            supplierProductEntity.setAvailabilityCheckType(getElementValue(supplierProductElement, "AvailabilityCheckType"));
                                        if (isSafeToExract(supplierProductElement, "ServiceVariant"))
                                            supplierProductEntity.setServiceVariant(getElementValue(supplierProductElement, "ServiceVariant"));
                                        if (isSafeToExract(supplierProductElement, "AvailabilityStatus"))
                                            supplierProductEntity.setProductAvailable(getElementValue(supplierProductElement, "AvailabilityStatus"));
                                        if (isSafeToExract(supplierProductElement, "CheckReference"))
                                            supplierProductEntity.setCheckedReference(getElementValue(supplierProductElement, "CheckReference"));
                                        if (isSafeToExract(supplierProductElement, "SymetricBandwidth"))
                                            supplierProductEntity.setSymmetricSpeedBandwidth(getElementValue(supplierProductElement, "SymetricBandwidth"));
                                        if (isSafeToExract(supplierProductElement, "MaxUpstreamBadwidth"))
                                            supplierProductEntity.setMaxUpstreamSpeedBandwidth(getElementValue(supplierProductElement, "MaxUpstreamBadwidth"));
                                        if (isSafeToExract(supplierProductElement, "MaxDownstreamBandwidth"))
                                            supplierProductEntity.setMaxDownstreamBandwidth(getElementValue(supplierProductElement, "MaxDownstreamBandwidth"));
                                        if (isSafeToExract(supplierProductElement, "ExchangeCode"))
                                            supplierProductEntity.setExchangeCode(getElementValue(supplierProductElement, "ExchangeCode"));
                                        if (isSafeToExract(supplierProductElement, "NumberOfCopperPairs"))
                                            supplierProductEntity.setNumberOfCopperPairs(Long.parseLong(getElementValue(supplierProductElement, "NumberOfCopperPairs")));
                                        if (isSafeToExract(supplierProductElement, "AvailabilityDescription"))
                                            supplierProductEntity.setAvailabilityDescription(getElementValue(supplierProductElement, "AvailabilityDescription"));

                                        supplierProductEntityList.add(supplierProductEntity);
                                    }
                                }
                                supplierSiteEntity.setSupplierProductEntityList(supplierProductEntityList);
                            }
                        }
                        supplierSiteEntityList.add(supplierSiteEntity);
                    }
                }
                logger.extractedResponseNowStoringDataFor(requestId);
                storeSupplierSiteEntity(supplierSiteEntityList, requestId);
            }
        } catch (Exception e) {
            logger.error(e);
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
                logger.info("Response is not valid, getting " + status + " with description : " + description);
                if (StringUtils.contains(status, "[Failure]")) isResponseValidToParse = false;
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
                        String supplierId = isSafeToExract(supplierResponseElement, SUPPLIER_ID) ? getElementValue(supplierResponseElement, SUPPLIER_ID) : null;
                        String spacId = isSafeToExract(supplierResponseElement, SPACID) ? getElementValue(supplierResponseElement, SPACID) : null;
                        updateStatusForSupplierProduct(Long.parseLong(siteId), supplierId, spacId, Failed.value(), failureMessage, NO_RESPONSE);
                    }
                }
            }
        }
    }

    private void storeSupplierSiteEntity(List<SupplierSiteEntity> supplierSiteEntityList, String requestId) {
        try {
            logger.startedStoringAvailabilityResponse(requestId);
            Date now = new Date();
            for (SupplierSiteEntity siteEntity : supplierSiteEntityList) {
                for (SupplierProductEntity product : siteEntity.getSupplierProductEntityList()) {
                    List<SupplierProductEntity> productEntityList = getSupplierProductBySiteIdAndSpacIds(siteEntity.getSiteId(), Lists.newArrayList(product.getSpacId()));
                    if (productEntityList.size() > 0) {
                        SupplierProductEntity productEntity = productEntityList.get(0);
                        updateWithChanges(productEntity, product);
                        if (RETRY.equalsIgnoreCase(productEntity.getProductAvailable())) {
                            productEntity.setStatus(Retry.value());
                            productEntity.setProductAvailable(NO_RESPONSE);
                            if (isNull(productEntity.getRetryCount())) {
                                productEntity.setRetryCount(0);
                            }
                        } else {
                            productEntity.setStatus(Completed.value());
                            productEntity.setDescription("");
                        }
                        //  External system response timed out
                        if (productEntity.getRequestTimeout() != null) {
                            if (productEntity.getRequestTimeout().compareTo(timestamp()) < 0) {
                                productEntity.setStatus(Timeout.value());
                                productEntity.setProductAvailable(NO_RESPONSE);
                                if (isNull(productEntity.getRetryCount())) {
                                    productEntity.setRetryCount(0);
                                }
                                productEntity.setDescription(EXTERNAL_SYSTEM_RESPONSE_TIMED_OUT);
                                logger.seemsThisIsTimedOut(productEntity.getSpacId());
                            }
                        }
                        storeSupplierProduct(productEntity);
                    }
                }

                updateSpacEntities(requestId, siteEntity.getSiteId(), getSupplierSiteEntity(siteEntity.getSiteId()));
                logger.persistenceFinishedForAvailabilityResponseFor(requestId);
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }

    private void updateSpacEntities(String requestId, Long siteId, SupplierSiteEntity siteEntityToUpdate) throws Exception {
        SupplierRequestSiteEntity requestSiteEntity = getSupplierRequestSiteBySiteId(siteId, getApeRequestByApeRequestId(requestId).getSupplierCheckClientRequestEntity().getId());
        for (SupplierProductEntity productEntity : siteEntityToUpdate.getSupplierProductEntityList()) {
            Optional<SupplierRequestSiteSpacEntity> spacEntity = getSpacEntity(requestSiteEntity.getSupplierRequestSiteSpacEntities(), productEntity.getSpacId());
            if(spacEntity.isPresent()) {
                spacEntity.get().setStatus(productEntity.getStatus());
            }
        }
        storeSupplierRequestSiteEntity(requestSiteEntity);
    }


    public static Optional<SupplierRequestSiteSpacEntity> getSpacEntity(List<SupplierRequestSiteSpacEntity> items, final String spacId) {
        return Iterables.tryFind(items, new Predicate<SupplierRequestSiteSpacEntity>() {
            public boolean apply(SupplierRequestSiteSpacEntity arg) {
                return isNotNull(arg.getSpacId()) && arg.getSpacId().equalsIgnoreCase(spacId);
            }
        });
    }

    private void updateWithChanges(SupplierProductEntity productEntity, SupplierProductEntity product) {
        if (isNotNull(product.getAvailabilityDescription()))
            productEntity.setAvailabilityDescription(product.getAvailabilityDescription());
        if (isNotNull(product.getNumberOfCopperPairs()))
            productEntity.setNumberOfCopperPairs(product.getNumberOfCopperPairs());
        if (isNotNull(product.getExchangeCode())) productEntity.setExchangeCode(product.getExchangeCode());
        if (isNotNull(product.getMaxDownstreamBandwidth()))
            productEntity.setMaxDownstreamBandwidth(product.getMaxDownstreamBandwidth());
        if (isNotNull(product.getMaxUpstreamSpeedBandwidth()))
            productEntity.setMaxUpstreamSpeedBandwidth(product.getMaxUpstreamSpeedBandwidth());
        if (isNotNull(product.getSymmetricSpeedBandwidth()))
            productEntity.setSymmetricSpeedBandwidth(product.getSymmetricSpeedBandwidth());
        if (isNotNull(product.getCheckedReference())) productEntity.setCheckedReference(product.getCheckedReference());
        if (isNotNull(product.getProductAvailable())) productEntity.setProductAvailable(product.getProductAvailable());
        if (isNotNull(product.getServiceVariant())) productEntity.setServiceVariant(product.getServiceVariant());
        if (isNotNull(product.getAvailabilityCheckType()))
            productEntity.setAvailabilityCheckType(product.getAvailabilityCheckType());
        if (isNotNull(product.getDisplaySupplierProductName()))
            productEntity.setDisplaySupplierProductName(product.getDisplaySupplierProductName());
    }


    boolean isSafeToExract(Element element, String xpath) {
        return element.getElementsByTagName(xpath).item(0) != null;
    }
}
