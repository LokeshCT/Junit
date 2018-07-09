package com.bt.rsqe.ape.source.extractor;

import com.bt.rsqe.ape.repository.entities.AvailabilityParamEntity;
import com.bt.rsqe.ape.repository.entities.AvailabilitySetEntity;
import com.bt.rsqe.ape.repository.entities.SupplierCheckApeRequestEntity;
import com.bt.rsqe.ape.repository.entities.SupplierProductEntity;
import com.bt.rsqe.ape.repository.entities.SupplierSiteEntity;
import com.bt.rsqe.ape.source.SupplierProductStore;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.customerrecord.SiteResource;
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
import static com.bt.rsqe.ape.source.SupplierProductStore.*;
import static com.bt.rsqe.customerinventory.dto.AvailabilityType.*;
import static com.google.common.collect.Lists.*;

/**
 * Created by 605783162 on 09/08/2015.
 */
public class SupplierProductResponseExtractor extends ResponseExtractorStrategy {

    public static final String REQUEST_ID = "requestId";

    @Override
    public void extractResponse(String response, SiteResource siteResource) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new InputSource(new ByteArrayInputStream(response.getBytes(UTF_8))));
            doc.getDocumentElement().normalize();
            String requestId = doc.getElementsByTagName(REQUEST_ID).item(0).getTextContent();
            List<SupplierSiteEntity> supplierSiteEntities = newArrayList();
            SiteDTO siteDto = null;
            SupplierSiteEntity supplierSiteEntity;
            SupplierProductEntity supplierProduct = null;
            NodeList siteNodeList = doc.getElementsByTagName("Site");
            for (int siteNodeIndex = 0; siteNodeIndex < siteNodeList.getLength(); siteNodeIndex++) {
                Node siteNode = siteNodeList.item(siteNodeIndex);
                if (siteNode.getNodeType() == Node.ELEMENT_NODE) {
                    supplierSiteEntity = new SupplierSiteEntity();
                    Element siteElement = (Element) siteNode;
                    supplierSiteEntity.setSiteId(Long.parseLong(getElementValue(siteElement, "siteId")));
                    supplierSiteEntity.setSiteName(getElementValue(siteElement, "siteName"));
                    siteDto = siteResource.getSiteDetails(String.valueOf(supplierSiteEntity.getSiteId()));
                    NodeList supplierProductNodeList = siteElement.getElementsByTagName("SupplierProduct");
                    List<SupplierProductEntity> supplierProductList = newArrayList();
                    for (int supplierProductNodeIndex = 0; supplierProductNodeIndex < supplierProductNodeList.getLength(); supplierProductNodeIndex++) {
                        Node supplierProductNode = supplierProductNodeList.item(supplierProductNodeIndex);
                        if (supplierProductNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element supplierProductElement = (Element) supplierProductNode;
                            supplierProduct = new SupplierProductEntity();
                            supplierProduct.setSpacId(getElementValue(supplierProductElement, "SPACID"));
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
                            supplierProduct.setNumberOfCopperPairs(Long.parseLong(getElementValue(supplierProductElement, "copperPairs")));
                            supplierProduct.setActive(getElementValue(supplierProductElement, "active"));
                            supplierProduct.setAvailabilityDescription(null);
                            supplierProduct.setProductAvailable("Not Checked");
                            List<AvailabilitySetEntity> availabilitySetList = newArrayList();
                            AvailabilitySetEntity set = null;
                            StringBuilder stringBuilder = new StringBuilder();
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
                                            if (getParamValue(siteDto, param.getName()) != null) {
                                                param.setValue(getParamValue(siteDto, param.getName()));
                                            } else {
                                                stringBuilder.append("\n" + param.getName());
                                            }
                                            param.setAvailabilitySetEntity(set);
                                            paramList.add(param);
                                        }
                                    }
                                    set.setParamEntityList(paramList);
                                }
                                if (StringUtils.isNotEmpty(set.getSetName())) {
                                    availabilitySetList.add(set);
                                }
                            }
                            supplierProduct.setMandatroyAttributes(stringBuilder.toString());
                            supplierProduct.setSetEntityList(availabilitySetList);
                            set.setSupplierProductEntity(supplierProduct);
                            supplierProductList.add(supplierProduct);
                        }
                        supplierSiteEntity.setSupplierProductEntityList(supplierProductList);
                        supplierProduct.setSupplierSiteEntity(supplierSiteEntity);
                    }
                    supplierSiteEntities.add(supplierSiteEntity);
                }
            }

            storeSupplierEntities(supplierSiteEntities, requestId);
        } catch (Exception e) {
            logger.error(e);
        }
    }

    private void storeSupplierEntities(List<SupplierSiteEntity> supplierSiteEntities, String apeRequestId) {
        try {
            for (SupplierSiteEntity siteEntity : supplierSiteEntities) {
                SupplierSiteEntity site = SupplierProductStore.getSupplierSiteEntity(siteEntity.getSiteId());
                if ((site.getTimeout() != null)) {
                    if (site.getTimeout().getTime() < new Date().getTime()) continue; // no need to process site which is timed out
                }

                site.setAvailabilityTypeId(Blue.getId());
                site.setTimeout(null);
                storeSupplierSite(site);
                for (SupplierProductEntity product : siteEntity.getSupplierProductEntityList()) {
                    Long suppProdId = SupplierProductStore.getSupplierProductId(product.getSpacId(), siteEntity.getSiteId());
                    if (suppProdId != null) product.setSuppProdId(suppProdId);
                    SupplierCheckApeRequestEntity apeRequestEntity = getApeRequestByApeRequestId(apeRequestId);
                    product.setRequestedTime(apeRequestEntity.getCreatedOn());//todo: this should be availability request time in
                    product.setRequestTimeout(null);//todo: this should be availability request time out based on timeout config property defined in XMLS
                    storeSupplierProduct(product);
                }
            }

        } catch (Exception e) {
            logger.error(e);
        }
    }

    private String getParamValue(SiteDTO siteDTO, String parameter) throws Exception {
        if (TELEPHONE_NUMBER.equalsIgnoreCase(parameter)) {
            return getAvailabilityTelephone(siteDTO.getSiteId().toString());
        } else if (CITY.equalsIgnoreCase(parameter)) {
            return siteDTO.getCity();
        } else if (HOUSE_NUMBER.equalsIgnoreCase(parameter)) {
            return siteDTO.getBuildingNumber();
        } else if (POSTAL_CODE.equalsIgnoreCase(parameter)) {
            return siteDTO.getPostCode();
        } else if (STREET.equalsIgnoreCase(parameter)) {
            return siteDTO.getStreetName();
        } else if (AREA_CODE.equalsIgnoreCase(parameter)) {
            return siteDTO.getTelephoneAreaCode();
        }
        return null;
    }
}
