package com.bt.rsqe.projectengine.web.productconfigurator;

import com.bt.rsqe.client.QuoteOptionClient;
import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.dto.LockVersion;
import com.bt.rsqe.customerinventory.parameter.LengthConstrainingProductInstanceId;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerinventory.parameter.ProductInstanceVersion;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.customerrecord.SiteResource;
import com.bt.rsqe.domain.QuoteOptionItemStatus;
import com.bt.rsqe.domain.StencilId;
import com.bt.rsqe.domain.StencilInfo;
import com.bt.rsqe.domain.bom.parameters.ProductSCode;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.ProductSalesRelationshipInstance;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.product.parameters.RelationshipGroupName;
import com.bt.rsqe.domain.product.parameters.RelationshipName;
import com.bt.rsqe.domain.product.parameters.RelationshipType;
import com.bt.rsqe.domain.product.parameters.SalesRelationship;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.projectengine.IfcAction;
import com.bt.rsqe.projectengine.LineItemDiscountStatus;
import com.bt.rsqe.projectengine.LineItemIcbApprovalStatus;
import com.bt.rsqe.projectengine.LineItemOrderStatus;
import com.bt.rsqe.projectengine.LineItemValidationResultDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemResource;
import com.bt.rsqe.projectengine.web.ImportResults;
import com.bt.rsqe.projectengine.web.productconfigurator.model.BulkConfigAttribute;
import com.bt.rsqe.projectengine.web.productconfigurator.model.BulkConfigAttributeGroup;
import com.bt.rsqe.projectengine.web.productconfigurator.model.BulkConfigAttributeList;
import com.bt.rsqe.projectengine.web.productconfigurator.model.BulkConfigDataModel;
import com.bt.rsqe.projectengine.web.productconfigurator.model.BulkConfigDetailModel;
import com.bt.rsqe.projectengine.web.productconfigurator.model.BulkConfigSiteModel;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.bt.rsqe.utils.AssertObject.*;
import static com.google.common.collect.Maps.newHashMap;


public class BulkSheetImporter {
    private final List<ProductInstance> rootInstances;
    private final ImportResults importResults;
    private final SiteResource siteResource;
    private final Map<ProductIdentifier, ProductOffering> exportableOfferings;
    private ProductInstanceClient instanceClient;
    private QuoteOptionClient quoteOptionClient;
    private QuoteOptionItemResource quoteOptionItemResource;
    private BulkConfigDataModel bulkConfigDataModel;
    private Map<ProductInstance, SiteDTO> siteDetails = newHashMap();

    public BulkSheetImporter(BulkConfigDataModel bulkConfigDataModel, List<ProductInstance> rootInstances, ImportResults importResults, SiteResource siteResource, Map<ProductIdentifier, ProductOffering> offerings, ProductInstanceClient instanceClient, QuoteOptionClient quoteOptionClient, QuoteOptionItemResource quoteOptionItemResource) {
        this.bulkConfigDataModel = bulkConfigDataModel;
        this.rootInstances = rootInstances;
        this.importResults = importResults;
        this.siteResource = siteResource;
        this.exportableOfferings = offerings;
        this.instanceClient = instanceClient;
        this.quoteOptionClient = quoteOptionClient;
        this.quoteOptionItemResource = quoteOptionItemResource;
    }

   //todo : tests to be covered
    public void importSheet() {

        siteDetails = getSiteDetails(rootInstances);
        final Map<ProductInstance, List<BulkConfigAttributeGroup>> productInstanceModelMap = buildProductInstanceModelMap(rootInstances, bulkConfigDataModel);

        for (ProductInstance productInstance : productInstanceModelMap.keySet()) {
            final List<BulkConfigAttributeGroup> bulkConfigAttributeGroups = productInstanceModelMap.get(productInstance);

            updateInstanceWithGroupModel(productInstance, bulkConfigAttributeGroups);

            final List<SalesRelationship> salesRelationships = productInstance.getProductOffering().getSalesRelationships();
            final Optional<SalesRelationship> exportableSalesRelationship = getExportableSalesRelationship(salesRelationships);
            if (exportableSalesRelationship.isPresent()) {
                final List<BulkConfigAttributeGroup> exportableSheetDetailModel = getExportableSheetDetailModel(productInstance, exportableSalesRelationship.get().getProductIdentifier().getProductName());

                if (productInstance.isRelationshipByNameExists(exportableSalesRelationship.get().getRelationshipName().value())) {
                    final Optional<ProductSalesRelationshipInstance> childInstance = productInstance.getRelationshipByName(exportableSalesRelationship.get().getRelationshipName().value());
                    updateInstanceWithGroupModel(childInstance.get().getRelatedProductInstance(), exportableSheetDetailModel);

                } else {
                    createRelatedInstance(productInstance, exportableSalesRelationship.get(), exportableSheetDetailModel);
                }

            }

            productInstance.setLineItemLockVersion(instanceClient.getCurrentLockVersion(new LineItemId(productInstance.getLineItemId())));
            instanceClient.put(productInstance);
        }

    }

    private Optional<SalesRelationship> getExportableSalesRelationship(List<SalesRelationship> salesRelationships) {
        return Iterables.tryFind(salesRelationships, new Predicate<SalesRelationship>() {
            @Override
            public boolean apply(SalesRelationship input) {
                return exportableOfferings.containsKey(input.getProductIdentifier());
            }
        });
    }

    private void createRelatedInstance(ProductInstance productInstance, SalesRelationship salesRelationship, List<BulkConfigAttributeGroup> exportableSheetDetailModel) {
        String lineItemId = salesRelationship.getType() == RelationshipType.Child ? productInstance.getLineItemId() : uuid();
        StencilId stencilId = StencilId.NIL;

        if (isNotNull(salesRelationship.getRelatedProductIdentifier())) {
            stencilId = salesRelationship.getRelatedProductIdentifier().getStencilId();
        }

        ProductInstance childProductInstance = instanceClient.createProductInstance(salesRelationship.getProductIdentifier().getProductId(),
                                                                                    salesRelationship.getProductIdentifier().getVersionNumber(),
                                                                                    lineItemId,
                                                                                    productInstance.getSiteId(),
                                                                                    productInstance.getCustomerId(),
                                                                                    productInstance.getContractId(),
                                                                                    productInstance.getQuoteOptionId(),
                                                                                    stencilId,
                                                                                    productInstance.getProjectId(),
                                                                                    productInstance.getContractTerm(),
                                                                                    quoteOptionClient, productInstance.getProductCategoryCode());

        ProductSalesRelationshipInstance newRelationshipInstance = new ProductSalesRelationshipInstance(salesRelationship, childProductInstance, productInstance);
        productInstance.addRelationship(newRelationshipInstance);

        if (salesRelationship.getType() == RelationshipType.RelatedTo) {
            instanceClient.put(childProductInstance);
            createLineItemsForRelatedToCreatedInstances(Optional.of(new LineItemId(productInstance.getLineItemId())), ProductSCode.newInstance(childProductInstance.getProductIdentifier().getProductId()), childProductInstance);
        }

        if (isNotNull(exportableSheetDetailModel)) {
            updateInstanceWithGroupModel(childProductInstance, exportableSheetDetailModel);
        }

    }


    private void updateInstanceWithGroupModel(ProductInstance productInstance, List<BulkConfigAttributeGroup> bulkConfigAttributeGroups) {
        final HashMultimap<RelationshipGroupName, SalesRelationship> salesGroupRelationships = productInstance.getProductOffering().getSalesGroupRelationships();
        final List<SalesRelationship> normalRelationships = productInstance.getProductOffering().getSalesRelationships();

        for (BulkConfigAttributeGroup bulkConfigAttributeGroup : bulkConfigAttributeGroups) {
            BulkConfigAttributeList attributeList = bulkConfigAttributeGroup.getAttributeList();
            switch (bulkConfigAttributeGroup.getAttributeGroupType()) {
                case BASE_CONFIG: {
                    for (BulkConfigAttribute bulkConfigAttribute : attributeList.getAttributes()) {
                        final Optional<Object> value = bulkConfigAttribute.getValue();
                        productInstance.updateInstanceCharacteristic(bulkConfigAttribute.getName(), value.isPresent() && !isEmpty(value.get().toString()) ? value.get().toString() : null);
                    }
                    break;
                }
                case STENCIL: {
                    final BulkConfigAttribute bulkConfigAttribute = attributeList.getAttributes().get(0);  // stencil comes once
                    final Optional<Object> value = bulkConfigAttribute.getValue();
                    final StencilInfo stencilInfo = setStencilFromAllowedValues(value.get().toString(), productInstance);
                    if (isNotNull(stencilInfo)) {
                        productInstance.updateInstanceCharacteristic(bulkConfigAttribute.getName(), value.isPresent() && !isEmpty(value.get().toString()) ? stencilInfo.getName() : null);
                        productInstance.setStencilId(StencilId.latestVersionFor(stencilInfo.getCode()));
                    } else {
                        productInstance.setStencilId(StencilId.NIL);
                    }
                    break;
                }
                case PARENT_CHILD: {
                    for (BulkConfigAttribute bulkConfigAttribute : attributeList.getAttributes()) {
                        final Optional<Object> value = bulkConfigAttribute.getValue();

                        final boolean relationshipByNameExists = productInstance.isRelationshipByNameExists(bulkConfigAttribute.getName());
                        if (value.isPresent() && !relationshipByNameExists) {
                            final List<SalesRelationship> salesRelationships = productInstance.getProductOffering().getSalesRelationships(RelationshipName.newInstance(bulkConfigAttribute.getName()));
                            createRelatedInstance(productInstance, salesRelationships.get(0), null);

                        } else if (!value.isPresent() && relationshipByNameExists) {
                            final Optional<ProductSalesRelationshipInstance> relationshipByName = productInstance.getRelationshipByName(bulkConfigAttribute.getName());
                            if (relationshipByName.isPresent()) {
                                removeSalesRelationshipInstance(relationshipByName.get(), productInstance);
                            }
                        }

                    }
                    break;
                }
                case RELATIONSHIP_GROUP: {
                    for (BulkConfigAttribute bulkConfigAttribute : attributeList.getAttributes()) {
                        final Optional<Object> value = bulkConfigAttribute.getValue();

                        Set<SalesRelationship> salesRelationships = salesGroupRelationships.get(RelationshipGroupName.newInstance(bulkConfigAttribute.getName()));

                        if (value.isPresent()) {
                            final boolean relationshipByNameExists = productInstance.isRelationshipByNameExists(value.get().toString());

                            final Optional<SalesRelationship> groupRelationshipByName = getGroupRelationshipByName(salesRelationships, value.get().toString());
                            if (groupRelationshipByName.isPresent() && !relationshipByNameExists) {
                                createRelatedInstance(productInstance, groupRelationshipByName.get(), null);
                            }
                            //remove other relationships
                            for (SalesRelationship salesRelationship : salesRelationships) {
                                if (!value.get().equals(groupRelationshipByName.get().getRelationshipName().value())) {
                                    final Optional<ProductSalesRelationshipInstance> relationshipByName = productInstance.getRelationshipByName(salesRelationship.getRelationshipName().value());
                                    if (relationshipByName.isPresent()) {
                                        removeSalesRelationshipInstance(relationshipByName.get(), productInstance);
                                    }
                                }
                            }

                        } else {
                            for (SalesRelationship salesRelationship : salesRelationships) {
                                final Optional<ProductSalesRelationshipInstance> relationshipByName = productInstance.getRelationshipByName(salesRelationship.getRelationshipName().value());
                                if (relationshipByName.isPresent()) {
                                    removeSalesRelationshipInstance(relationshipByName.get(), productInstance);
                                }
                            }
                        }
                    }

                    break;
                }

                case RELATED_TO: {
                    for (BulkConfigAttribute bulkConfigAttribute : attributeList.getAttributes()) {
                        final Optional<Object> value = bulkConfigAttribute.getValue();

                        final boolean relationshipByNameExists = productInstance.isRelationshipByNameExists(bulkConfigAttribute.getName());
                        final Optional<ProductSalesRelationshipInstance> relationshipByName = productInstance.getRelationshipByName(bulkConfigAttribute.getName());

                        if (value.isPresent() && !isEmpty(value.get().toString())) {
                            final Optional<SalesRelationship> stencilRelationship = getStencilRelationship(normalRelationships, value.get().toString());
                            if (!relationshipByNameExists) {
                                createRelatedInstance(productInstance, stencilRelationship.get(), null);
                            } else if (relationshipByNameExists && !relationshipByName.get().getSalesRelationship().equals(stencilRelationship)) {
                                removeSalesRelationshipInstance(relationshipByName.get(), productInstance);
                                createRelatedInstance(productInstance, stencilRelationship.get(), null);

                            }
                        } else if (!value.isPresent() && relationshipByNameExists && relationshipByName.isPresent()) {
                            removeSalesRelationshipInstance(relationshipByName.get(), productInstance);
                        }
                    }

                    break;
                }
            }

        }

    }


    private void removeSalesRelationshipInstance(ProductSalesRelationshipInstance salesRelationshipInstance, ProductInstance productInstance) {
        productInstance.removeRelationshipByName(salesRelationshipInstance.getRelationshipName());

        productInstance.setLineItemLockVersion(instanceClient.getCurrentLockVersion(new LineItemId(productInstance.getLineItemId())));
        final ProductInstance relatedProductInstance = salesRelationshipInstance.getRelatedProductInstance();
        instanceClient.delete(new LengthConstrainingProductInstanceId(productInstance.getProductInstanceId().getValue()),
                              new ProductInstanceVersion(productInstance.getProductInstanceVersion().longValue()),
                              new LineItemId(relatedProductInstance.getLineItemId()),
                              new LengthConstrainingProductInstanceId(relatedProductInstance.getProductInstanceId().getValue()),
                              new ProductInstanceVersion(relatedProductInstance.getProductInstanceVersion().longValue()),
                              salesRelationshipInstance.getType(),
                              new LockVersion(instanceClient.getCurrentLockVersion(new LineItemId(productInstance.getLineItemId()))),
                              com.google.common.base.Optional.<RelationshipName>absent());

        if (salesRelationshipInstance.getType() == RelationshipType.RelatedTo) {
            quoteOptionItemResource.delete(relatedProductInstance.getLineItemId());
        }
    }


    private Optional<SalesRelationship> getGroupRelationshipByName(Set<SalesRelationship> salesRelationships, final String name) {
        return Iterables.tryFind(salesRelationships, new Predicate<SalesRelationship>() {
            @Override
            public boolean apply(SalesRelationship input) {
                return input.getRelationshipName().value().equals(name);
            }
        });
    }

    private Optional<SalesRelationship> getStencilRelationship(List<SalesRelationship> normalRelationships, final String stencil) {
        return Iterables.tryFind(normalRelationships, new Predicate<SalesRelationship>() {
            @Override
            public boolean apply(SalesRelationship input) {
                return stencil.equals(input.getRelatedProductIdentifier().getStencilId().getProductName().getValue());
            }
        });
    }

    private StencilInfo setStencilFromAllowedValues(String caption, ProductInstance productInstance) {

        for (StencilInfo stencilInfo : productInstance.getProductOffering().getAvailableStencils()) {
            if (stencilInfo.getName().equals(caption)) {
                return stencilInfo;
            }
        }

        return null;
    }

    private Map<ProductInstance, List<BulkConfigAttributeGroup>> buildProductInstanceModelMap(List<ProductInstance> productInstances, BulkConfigDataModel bulkConfigDataModel) {
        final List<BulkConfigDetailModel> detailModel = bulkConfigDataModel.getDetailModel();
        Map<ProductInstance, List<BulkConfigAttributeGroup>> productInstanceModel = newHashMap();

        final Map<ProductInstance, SiteDTO> siteDetailsMap = getSiteDetails(productInstances);
        for (ProductInstance productInstance : productInstances) {

            final SiteDTO siteDTO = siteDetailsMap.get(productInstance);
            final BulkConfigSiteModel bulkConfigSiteModelKey = new BulkConfigSiteModel(siteDTO.getSiteName(), siteDTO.getSiteId().toString(), productInstance.getAssetUniqueId().getValue(), siteDTO.getCity(), siteDTO.getCountry());


            for (BulkConfigDetailModel bulkConfigDetailModel : detailModel) {
                final List<BulkConfigAttributeGroup> attributeGroupsForSite = bulkConfigDetailModel.getAttributeGroupsForSite(bulkConfigSiteModelKey);
                if (isNotNull(attributeGroupsForSite) && bulkConfigDetailModel.getModelName().equals(productInstance.getProductOffering().getProductIdentifier().getProductName())) {
                    productInstanceModel.put(productInstance, attributeGroupsForSite);
                    break;
                }
            }
        }

        return productInstanceModel;
    }

    private List<BulkConfigAttributeGroup> getExportableSheetDetailModel(ProductInstance productInstance, String productName) {
        final List<BulkConfigDetailModel> detailModel = bulkConfigDataModel.getDetailModel();

        final SiteDTO siteDTO = siteDetails.get(productInstance);
        final BulkConfigSiteModel bulkConfigSiteModelKey = new BulkConfigSiteModel(siteDTO.getSiteName(), siteDTO.getSiteId().toString(), productInstance.getAssetUniqueId().getValue(), siteDTO.getCity(), siteDTO.getCountry());

        for (BulkConfigDetailModel bulkConfigDetailModel : detailModel) {
            final List<BulkConfigAttributeGroup> attributeGroupsForSite = bulkConfigDetailModel.getAttributeGroupsForSite(bulkConfigSiteModelKey);
            if (isNotNull(attributeGroupsForSite) && bulkConfigDetailModel.getModelName().equals(productName)) {
                return attributeGroupsForSite;
            }
        }

        return null;
    }


    private Map<ProductInstance, SiteDTO> getSiteDetails(List<ProductInstance> productInstances) {
        Map<ProductInstance, SiteDTO> siteDTOMap = newHashMap();
        if (siteDTOMap.isEmpty()) {
            for (ProductInstance productInstance : productInstances) {

                if (productInstance.isSiteInstallable()) {
                    final SiteDTO siteDTO = siteResource.get(productInstance.getSiteId(), productInstance.getProjectId());
                    siteDTOMap.put(productInstance, siteDTO);
                } else {
                    final SiteDTO centralSite = siteResource.getCentralSite(productInstance.getProjectId());
                    siteDTOMap.put(productInstance, centralSite);

                }
            }
        }

        return siteDTOMap;
    }

    private void createLineItemsForRelatedToCreatedInstances(Optional<LineItemId> ownerLineItemId, ProductSCode productCode, ProductInstance createdProductInstance) {
        String offerId = null, offerName = null, orderId = null, billingId = null;
        QuoteOptionItemDTO parentLineItem = null;

        final QuoteOptionItemDTO anotherLineItem = getQuoteOptionItem(ownerLineItemId);
        quoteOptionItemResource.post(new QuoteOptionItemDTO(createdProductInstance.getLineItemId(),
                                                            productCode.getValue(),
                                                            anotherLineItem.action,
                                                            anotherLineItem.siteId,
                                                            offerId,
                                                            offerName,
                                                            anotherLineItem.contractTerm,
                                                            QuoteOptionItemStatus.INITIALIZING,
                                                            LineItemDiscountStatus.NOT_APPLICABLE,
                                                            LineItemIcbApprovalStatus.NOT_APPLICABLE,
                                                            orderId,
                                                            new LineItemValidationResultDTO(LineItemValidationResultDTO.Status.PENDING),
                                                            LineItemOrderStatus.NOT_APPLICABLE,
                                                            IfcAction.NOT_APPLICABLE,
                                                            billingId,
                                                            parentLineItem,
                                                            anotherLineItem.contractDTO,
                                                            false,
                                                            anotherLineItem.isIfc,
                                                            anotherLineItem.isImportable,
                                                            anotherLineItem.getCustomerRequiredDate(), null,
                                                            anotherLineItem.hasFullAddress,
                                                            createdProductInstance.getProductCategoryCode(), null, false));

        quoteOptionItemResource.putInitialValidationSuccessNotification(createdProductInstance.getLineItemId());

    }

    private QuoteOptionItemDTO getQuoteOptionItem(Optional<LineItemId> lineItemId) {
        for (LineItemId itemId : lineItemId.asSet()) {
            return quoteOptionItemResource.get(itemId.value());
        }
        for (QuoteOptionItemDTO itemDTO : quoteOptionItemResource.get()) {
            return itemDTO;
        }
        throw new RuntimeException();
    }


    protected String uuid() {
        return UUID.randomUUID().toString();
    }
}
