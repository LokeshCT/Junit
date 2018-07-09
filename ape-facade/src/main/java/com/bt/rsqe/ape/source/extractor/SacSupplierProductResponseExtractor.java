package com.bt.rsqe.ape.source.extractor;

import com.bt.rsqe.ape.dto.sac.SacApeStatus;
import com.bt.rsqe.ape.repository.APEQrefJPARepository;
import com.bt.rsqe.ape.repository.entities.SacSupplierProdAvailEntity;
import com.bt.rsqe.ape.repository.entities.SacSupplierProdMasterEntity;
import com.bt.rsqe.ape.repository.entities.SupplierSiteEntity;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.customerrecord.SiteResource;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.List;

import static com.bt.rsqe.ape.source.SupplierProductStore.storeSacSupplierDetails;
import static com.google.common.collect.Lists.*;


public class SacSupplierProductResponseExtractor extends ResponseExtractorStrategy {

    public static final String TELEPHONE_NUMBER = "Telephone Number";
    public static final String IN_PROGRESS = "InProgress";
    public static final String COMPLETE = "Complete";
    org.slf4j.Logger LOG= LoggerFactory.getLogger(SacSupplierAvailabilityResponseExtractor.class);
    APEQrefJPARepository repository;

    @Override
    public void extractResponse(String response, SiteResource siteResource) throws Exception{
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new InputSource(new ByteArrayInputStream(response.getBytes("utf-8"))));
            doc.getDocumentElement().normalize();
            String requestId = doc.getElementsByTagName("requestId").item(0).getTextContent();
            List<SupplierSiteEntity> supplierSiteEntities = newArrayList();
            SiteDTO siteDto = null;
            List<SacSupplierProdAvailEntity> supplierProdAvailEntities = null;
            //List<SacSupplierProdMasterEntity> supplierProductList = null;
            SacSupplierProdMasterEntity supplierProduct = null;
            SacSupplierProdAvailEntity sacSupplierProdAvailEntity = null;
            NodeList siteNodeList = doc.getElementsByTagName("Site");
            for (int siteNodeIndex = 0; siteNodeIndex < siteNodeList.getLength(); siteNodeIndex++) {
                Node siteNode = siteNodeList.item(siteNodeIndex);
                if (siteNode.getNodeType() == Node.ELEMENT_NODE) {
                    SupplierSiteEntity supplierSiteEntity = new SupplierSiteEntity();
                    Element siteElement = (Element) siteNode;
                    String siteId = getElementValue(siteElement, "siteId");
                    String spacId = null;
                    /*supplierSiteEntity.setSiteName(getElementValue(siteElement, "siteName"));
                    siteDto = siteResource.getSiteDetails(String.valueOf(supplierSiteEntity.getSiteId()));*/
                    NodeList supplierProductNodeList = siteElement.getElementsByTagName("SupplierProduct");
                    supplierProdAvailEntities = newArrayList();
                    for (int supplierProductNodeIndex = 0; supplierProductNodeIndex < supplierProductNodeList.getLength(); supplierProductNodeIndex++) {
                        Node supplierProductNode = supplierProductNodeList.item(supplierProductNodeIndex);
                        if (supplierProductNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element supplierProductElement = (Element) supplierProductNode;
                            supplierProduct = new SacSupplierProdMasterEntity();
                            sacSupplierProdAvailEntity = new SacSupplierProdAvailEntity();

                            supplierProduct.setSiteId(Long.parseLong(siteId));
                            spacId =getElementValue(supplierProductElement, "SPACID");
                            supplierProduct.setSpacId(spacId);
                            supplierProduct.setSupplierId(Long.parseLong(getElementValue(supplierProductElement, "supplierId")));
                            supplierProduct.setSupplierName(getElementValue(supplierProductElement, "supplierName"));
                            supplierProduct.setSupplierProductId(Long.parseLong(getElementValue(supplierProductElement, "supplierProductId")));
                            supplierProduct.setSupplierProductName(getElementValue(supplierProductElement, "supplierProductName"));
                            supplierProduct.setDisplaySupplierProductName(getElementValue(supplierProductElement, "displaySupplierProductName"));
                            supplierProduct.setProductAvailabilityCode(getElementValue(supplierProductElement, "productAvailabilityCode"));
                            supplierProduct.setAvailabilityCheckType(getElementValue(supplierProductElement, "availabilityCheckType"));
                            supplierProduct.setCustomerLocationType(getElementValue(supplierProductElement, "customerLocationType"));
                            supplierProduct.setAccessType(getElementValue(supplierProductElement, "accessType"));
                            supplierProduct.setParentAccessType(getElementValue(supplierProductElement, "parentAccessType"));
                            supplierProduct.setDeliveryMode(getElementValue(supplierProductElement, "deliveryMode"));
                            supplierProduct.setServiceVariant(getElementValue(supplierProductElement, "serviceVariant"));
                            supplierProduct.setContentionRatio(getElementValue(supplierProductElement, "contentionRatio"));
                            supplierProduct.setCpeAccessType(getElementValue(supplierProductElement, "cpeAccessType"));
                            supplierProduct.setCentralizedAvailabilitySupported(getElementValue(supplierProductElement, "centralizedAvailabilitySupported"));
                            supplierProduct.setCommonAccessCPESupplier(getElementValue(supplierProductElement, "commonAccessCpeSupplier"));
                            supplierProduct.setAccessSpeed(getElementValue(supplierProductElement, "accessSpeed"));
                            supplierProduct.setAccessUom(getElementValue(supplierProductElement, "accessSpeedUOM"));
                            supplierProduct.setInterfaceName(getElementValue(supplierProductElement, "interfaceName"));
                            supplierProduct.setInterfaceId(getElementValue(supplierProductElement, "interfaceID"));
                            supplierProduct.setFramingName(getElementValue(supplierProductElement, "framingName"));
                            supplierProduct.setFramingId(getElementValue(supplierProductElement, "framingID"));
                            supplierProduct.setConnectorId(getElementValue(supplierProductElement, "connectorID"));
                            supplierProduct.setConnectorName(getElementValue(supplierProductElement, "connectorName"));
                            supplierProduct.setNumberOfCopperPairs(getElementValue(supplierProductElement, "copperPairs"));
                            supplierProduct.setActive(getElementValue(supplierProductElement, "active"));
                            supplierProduct.setAvailabilityDescription(null);
                            supplierProduct.setProductAvailable(null);
                            //List<AvailabilitySetEntity> availabilitySetList = newArrayList();
                            /*AvailabilitySetEntity set = null;
                            NodeList setNodeList = supplierProductElement.getElementsByTagName("SetResponse");
                            for (int setNodeIndex = 0; setNodeIndex < setNodeList.getLength(); setNodeIndex++) {
                                Node setNode = setNodeList.item(setNodeIndex);
                                if (setNode.getNodeType() == Node.ELEMENT_NODE) {
                                    Element setElement = (Element) setNode;
                                    set = new AvailabilitySetEntity();
                                    set.setSetName(getElementValue(setElement, "name"));

                                    List<AvailabilityParamEntity> paramList = newArrayList();
                                    AvailabilityParamEntity param = null;
                                    NodeList paramNodeList = setElement.getElementsByTagName("ParamName");
                                    for (int paramNodeIndex = 0; paramNodeIndex < paramNodeList.getLength(); paramNodeIndex++) {
                                        Node paramNode = paramNodeList.item(setNodeIndex);
                                        if (paramNode.getNodeType() == Node.ELEMENT_NODE) {
                                            Element paramElement = (Element) paramNode;
                                            param = new AvailabilityParamEntity();
                                            param.setName(getElementValue(paramElement, "paramName"));
                                            param.setValue(getParamValue(siteDto, param.getName()));
                                            param.setAvailabilitySetEntity(set);
                                            paramList.add(param);
                                        }
                                    }
                                    set.setParamEntityList(paramList);

                                }
                                availabilitySetList.add(set);
                            }
                            supplierProduct.setSetEntityList(availabilitySetList);*/
                            /*set.setSupplierProductEntity(supplierProduct);*/
                            //getSACSiteRequest(requestId,supplierProduct.getSiteId());
                            sacSupplierProdAvailEntity.setSiteId(Long.parseLong(siteId));
                            sacSupplierProdAvailEntity.setSpacId(spacId);
                            sacSupplierProdAvailEntity.setSupplierProduct(supplierProduct);
                            sacSupplierProdAvailEntity.setApeReqId(requestId);
                            sacSupplierProdAvailEntity.setStatus(SacApeStatus.APE_RESPONSE_SUCCESS.getStatus());
                            supplierProdAvailEntities.add(sacSupplierProdAvailEntity);
                            //supplierProductList.add(supplierProduct);
                        }
                        /*supplierSiteEntity.setSupplierProductEntityList(supplierProductList);
                        supplierProduct.setSupplierSiteEntity(supplierSiteEntity);*/
                    }
                    storeSacSupplierDetails(supplierProdAvailEntities);
                    //supplierSiteEntities.add(supplierSiteEntity);
                }
            }

            /*storeSacSupplierDetails(supplierProdAvailEntities);*/
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /*
    private void storeSupplierEntities(List<SacSupplierProdMasterEntity> sacSupplierProdMasterEntities, String apeRequestId) {
        try {
            Set<Long> siteIds = new HashSet<Long>();
            for (SacSupplierProdMasterEntity product : sacSupplierProdMasterEntities) {
                siteIds.add(product.getSiteId());
                repository.save(product);
            }

            repository.updateSacRequestStatus((Long[])siteIds.toArray(),Completed.value());

        } catch (Exception e) {
            LOG.error("Error while saving callback response for Supplier Product list..");
            e.printStackTrace();
        }
    }
    */

/*    private String getParamValue(SiteDTO siteDTO, String parameter) throws Exception {
        if (TELEPHONE_NUMBER.equalsIgnoreCase(parameter)) {
            repository.getAvailabilityTelephone(siteDTO.getSiteId().toString());
            return siteDTO.getTelephoneNumber();
        } else if ("City".equalsIgnoreCase(parameter)) {
            return siteDTO.getCity();
        } else if ("Post Code".equalsIgnoreCase(parameter)) {
            return siteDTO.getPostCode();
        } else if ("Street".equalsIgnoreCase(parameter)) {
            return siteDTO.getSubStreet();
        }

        return null;
    }*/
}
