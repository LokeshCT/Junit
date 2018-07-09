package com.bt.rsqe.projectengine.web.quoteoptionorders.ecrfsheet;

import com.bt.rsqe.client.Pmr;
import com.bt.rsqe.client.QuoteOptionClient;
import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.dto.ChangeAssetDTO;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerrecord.CustomerResource;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.customerrecord.SiteResource;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.ContractDTO;
import com.bt.rsqe.domain.StencilId;
import com.bt.rsqe.domain.StencilInfo;
import com.bt.rsqe.domain.bom.parameters.ProductSCode;
import com.bt.rsqe.domain.product.Attribute;
import com.bt.rsqe.domain.product.AttributeName;
import com.bt.rsqe.domain.product.DefaultValue;
import com.bt.rsqe.domain.product.InstanceCharacteristic;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.ProductSalesRelationshipInstance;
import com.bt.rsqe.domain.product.parameters.ProductCategoryCode;
import com.bt.rsqe.domain.product.parameters.SalesRelationship;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.pc.client.ConfiguratorContractClient;
import com.bt.rsqe.projectengine.DeliveryAddressDTO;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionResource;
import com.bt.rsqe.projectengine.web.AssetKeyContainer;
import com.bt.rsqe.projectengine.web.ImportResults;
import com.bt.rsqe.security.UserContext;
import com.bt.rsqe.security.UserContextManager;
import com.bt.rsqe.web.rest.exception.ResourceNotFoundException;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.*;
import static com.google.common.collect.Sets.*;
import static org.apache.commons.lang.StringUtils.*;

public abstract class BulkImporter {
    protected final ProductInstanceClient instanceClient;
    protected final QuoteOptionClient quoteOptionClient;
    protected final Pmr pmr;
    protected final CardinalityValidator cardinalityValidator;
    protected final CustomerResource customerResource;
    protected final ConfiguratorContractClient configuratorClient;
    protected final ProductRelationshipService productRelationshipService;
    protected final ProjectResource projectResource;
    protected static final String SITE_ID = "SITE ID";
    protected static final String DELIVERY_ADDRESS = "Delivery Address";
    protected static final String ADDRESS_ID = "Address ID";

    public BulkImporter(ProductInstanceClient instanceClient, QuoteOptionClient quoteOptionClient, Pmr pmr, CardinalityValidator cardinalityValidator, CustomerResource customerResource, ConfiguratorContractClient configuratorClient, ProjectResource projectResource, ProductRelationshipService productRelationshipService) {
        this.instanceClient = instanceClient;
        this.quoteOptionClient = quoteOptionClient;
        this.pmr = pmr;
        this.cardinalityValidator = cardinalityValidator;
        this.customerResource = customerResource;
        this.configuratorClient = configuratorClient;
        this.productRelationshipService = productRelationshipService;
        this.projectResource = projectResource;
    }

    protected Set<LineItemId> importFromSheet(String customerId, String contractId, String contractTerm, String projectId, String quoteOptionId, ECRFWorkBook workBook, ImportResults importResults,
                                              AssetKeyContainer assetKeyContainer, String importedProductCode, Optional<LineItemId> lineItemIdOptional, boolean isMigration, ProductCategoryCode productCategoryCode) throws ECRFImportException {
        Set<LineItemId> impactedLineItems = newHashSet();

        Map<String,SiteDTO> customerSites = getCustomerSites(customerId, projectId);
        ECRFSheet sheetToStartImport = firstImportableProduct(workBook);

        boolean isBulkImport = isABulkImport(importedProductCode);
        //Create the line Item Id for the bulk imported product as this is required as holder Line Item for related products.
        BulkProduct bulkProduct = new BulkProduct(lineItemIdOptional.isPresent() ? lineItemIdOptional.get() : new LineItemId(UUID.randomUUID().toString()), isBulkImport);

        for (int sheetIndex = sheetToStartImport.getSheetIndex(); sheetIndex < getMaxSheetIndex(workBook, sheetToStartImport); sheetIndex++) {
            ECRFSheet ecrfSheet = workBook.getSheetBySheetIndex(sheetIndex);
            if (ecrfSheet.isParentSheet()) {
                ProductOffering productOffering = pmr.productOffering(ProductSCode.newInstance(ecrfSheet.getProductCode())).get();
                impactedLineItems.addAll(importRootProduct(workBook, customerId, projectId, contractId, contractTerm, quoteOptionId, customerSites, importResults, productOffering, assetKeyContainer, ecrfSheet, bulkProduct, lineItemIdOptional, importedProductCode, isMigration, productCategoryCode));
            }
        }
        establishRelationships(workBook, assetKeyContainer, importResults);

        return impactedLineItems;
    }

    private int getMaxSheetIndex(ECRFWorkBook workBook, ECRFSheet sheetToStartImport) {
        return sheetToStartImport.getSheetIndex() + workBook.getControlSheet().size();
    }

    protected boolean isABulkImport(String importedProductCode) {
        return pmr.productOffering(ProductSCode.newInstance(importedProductCode)).get().isBulk();
    }

    protected ECRFSheet firstImportableProduct(ECRFWorkBook workBook) {
        return workBook.getSheetBySheetIndex(workBook.getControlSheetIndex() + 1);
    }

    protected void establishRelationships(ECRFWorkBook workBook, AssetKeyContainer assetKeyContainer, ImportResults importResults) {
        if (workBook.hasRelatedToSheet) {
            for (ECRFSheet ecrfSheet : workBook.getSheets()) {
                if (ecrfSheet.isRelatedProductSheet()) {
                    productRelationshipService.createRelations(ecrfSheet, assetKeyContainer, importResults);
                }
            }
        }
    }

    protected abstract Set<LineItemId> importRootProduct(ECRFWorkBook workBook, String customerId, String projectId, String contractId, String contractTerm, String quoteOptionId,
                                                         Map<String, SiteDTO> customerSites, ImportResults importResults, ProductOffering productOffering, AssetKeyContainer assetKeyContainer,
                                                         ECRFSheet ecrfSheet, BulkProduct bulkProduct, Optional<LineItemId> lineItemIdOptional, String importedProductCode, boolean isMigration, ProductCategoryCode productCategoryCode);

    protected abstract void createQuoteOptionItem(String customerId, String contractId, String projectId, String quoteOptionId, String siteId, String productCode, Optional<LineItemId> lineItemIdOptional, String lineItemId,
                                                  String contractTerm, UserContext userContext, String holderLineItemId, ProductCategoryCode productCategoryCode);

    protected void extractDeliveryAddress(ECRFWorkBook workBook, String rowId, String projectId, String quoteOptionId, String lineItemId, String mappingAttribute) {
        DeliveryAddressExtractor deliveryAddressExtractor = new DeliveryAddressExtractor(rowId, workBook, mappingAttribute);
        DeliveryAddressDTO deliveryAddress = deliveryAddressExtractor.execute();
        deliveryAddress.setLineItemId(lineItemId);
        QuoteOptionResource quoteOptionResource = projectResource.quoteOptionResource(projectId);
        quoteOptionResource.quoteOptionItemResource(quoteOptionId).createDeliveryAddressForLineItem(lineItemId, deliveryAddress);
    }

    protected boolean isAdditionalDetailMapped(ECRFWorkBook workBook, final ECRFSheetModelRow ecrfSheetModelRow, String sheetName, final String mappedAttributeName) {
        for (ECRFSheet ecrfSheet : workBook.getNonProductSheets()) {
            if(ecrfSheet.getSheetName().equals(sheetName)){
                return Iterables.tryFind(ecrfSheet.getRows(), new Predicate<ECRFSheetModelRow>() {
                    @Override
                    public boolean apply(ECRFSheetModelRow input) {
                        return input.getAttributeByName(mappedAttributeName).getValue().equals(ecrfSheetModelRow.getRowId());
                    }
                }).isPresent();
            }
        }
        return false;
    }

    protected void deleteOrphanInstances(String projectId, String quoteOptionId, String lineItemId) {
        quoteOptionClient.deleteQuoteOptionItem(projectId, quoteOptionId, lineItemId);
    }

    protected void validateCardinality(String customerId, String contractId, String quoteOptionId, Set<String> customerSites, ImportResults importResults, ProductOffering firstProductToBeProcessed, List<ECRFSheetModelRow> rows, AssetKey assetToBeIgnored) {
        validateContractCardinality(customerId, contractId, quoteOptionId, firstProductToBeProcessed, rows, assetToBeIgnored);
        if (firstProductToBeProcessed.isSiteInstallable()) {
            validateSiteCardinality(customerId, contractId, quoteOptionId, firstProductToBeProcessed, rows, customerSites);
        }
    }

    protected void importInToProductInstance(String customerId, String contractId, String projectId, String quoteOptionId, ProductInstance rootProductInstance, ECRFWorkBook workBook, ECRFSheetModelRow rootProductRow, AssetKeyContainer assetKeyContainer, boolean isMigration, ProductCategoryCode productCategoryCode) {
        populateProductInstance(rootProductRow, rootProductInstance, workBook.getSheetByProductCode(rootProductInstance.getProductOffering().getProductIdentifier().getProductId()).getSheetName(), isMigration, productCategoryCode);
        createChildRelationships(workBook, rootProductRow, rootProductInstance, customerId, contractId, projectId, quoteOptionId,
                                 rootProductInstance.getLineItemId(), rootProductInstance.getProductOffering(), new ArrayList<AssetRelationshipMap>(), assetKeyContainer, isMigration, productCategoryCode);
        evaluateRelationshipsBasedOnCardinality(rootProductInstance);
        instanceClient.put(rootProductInstance);
        instanceClient.refreshAttributesOfProductInstance(instanceClient.getByAssetKey(rootProductInstance.getKey()));
    }


    protected LineItemId createAndImport(String projectId, String customerId, String contractId, String contractTerm, String productCode, String siteId, String quoteOptionId, ECRFWorkBook workBook, ECRFSheetModelRow ecrfSheetModelRow, ProductOffering productOffering, AssetKeyContainer assetKeyContainer, BulkProduct bulkProduct, Optional<LineItemId> lineItemIdOptional, String lineItemId, boolean isMigration, ProductCategoryCode productCategoryCode) {
        UserContext userContext = UserContextManager.getCurrent();
        String holderLineItemId = null;
        String productId = productOffering.getProductIdentifier().getProductId();

        if (bulkProduct.isBulkImport() && !productOffering.isBulk()) {
            holderLineItemId = bulkProduct.getLineItemId().value();
        }
        if (productOffering.isBulk()) {
            lineItemId = bulkProduct.getLineItemId().value();
        }

        createQuoteOptionItem(customerId, contractId, projectId, quoteOptionId, siteId, productCode, lineItemIdOptional, lineItemId, contractTerm, userContext, holderLineItemId, productCategoryCode);

        ProductInstance productInstance = instanceClient.createProductInstance(productId,
                                                                               productOffering.getProductIdentifier().getVersionNumber(),
                                                                               lineItemId,
                                                                               siteId,
                                                                               customerId,
                                                                               contractId,
                                                                               quoteOptionId,
                                                                               StencilId.NIL,
                                                                               projectId,
                                                                               contractTerm,
                                                                               quoteOptionClient, productCategoryCode);
        importInToProductInstance(customerId,
                                  contractId,
                                  projectId,
                                  quoteOptionId,
                                  productInstance,
                                  workBook,
                                  ecrfSheetModelRow, assetKeyContainer, isMigration, productCategoryCode);
        assetKeyContainer.addKey(ecrfSheetModelRow.getRowId(), productInstance.getKey());
        return new LineItemId(productInstance.getLineItemId());
    }


    protected void validateContractCardinality(String customerId, String contractId, String quoteOptionId, ProductOffering rootOffering, List<ECRFSheetModelRow> rows, AssetKey assetKey) {
        CardinalityValidationResult contractCardinalityResult = cardinalityValidator.validateContractCardinality(customerId, contractId, quoteOptionId, rootOffering, rows.size(), assetKey);
        if (contractCardinalityResult.isFailed()) {
            throw new ECRFImportException(contractCardinalityResult.getErrorMessage());
        }
    }

    protected void validateSiteCardinality(String customerId, String contractId, String quoteOptionId, ProductOffering rootOffering, List<ECRFSheetModelRow> rows, Set<String> siteIds) {
        for (String siteId : siteIds) {
            CardinalityValidationResult contractCardinalityResult = cardinalityValidator.validateSiteCardinality(customerId, contractId, quoteOptionId, siteId, rootOffering, rows.size(), null);
            if (contractCardinalityResult.isFailed()) {
                throw new ECRFImportException(contractCardinalityResult.getErrorMessage());
            }
        }
    }

    protected void createChildRelationships(ECRFWorkBook workBook, ECRFSheetModelRow parentProductRow,
                                            ProductInstance parentProductInstance, String customerId,
                                            String contractId, String projectId, String quoteOptionId,
                                            String lineItemId, ProductOffering offering, List<AssetRelationshipMap> alreadyMapped, AssetKeyContainer assetKeyContainer, boolean isMigration, ProductCategoryCode productCategoryCode) {
        for (ECRFSheet ecrfSheet : workBook.getSheets()) {
            if (ecrfSheet.isChildSheet()) {
                List<SalesRelationship> salesRelationships = offering.getSalesRelationships();
                for (ECRFSheetModelRow ecrfSheetModelRow : ecrfSheet.getRows()) {
                    if (ecrfSheetModelRow.getParentId() != null) {
                        workBook.parentIdExistsInWorkBook(ecrfSheetModelRow.getParentId(), ecrfSheetModelRow.getSheetName());
                        if (ecrfSheetModelRow.getParentId().equals(parentProductRow.getRowId())) {
                            StencilId stencilId = getStencilIfApplicable(offering, ecrfSheetModelRow, ecrfSheet.getProductCode());
                            ProductInstance childProductInstance = instanceClient.createProductInstance(ecrfSheet.getProductCode(),
                                                                                                        null,
                                                                                                        lineItemId,
                                                                                                        parentProductInstance.getSiteId(),
                                                                                                        customerId, contractId, quoteOptionId,
                                                                                                        stencilId,
                                                                                                        projectId, parentProductInstance.getContractTerm(),
                                                                                                        quoteOptionClient, productCategoryCode);
                            assetKeyContainer.addKey(ecrfSheetModelRow.getRowId(), childProductInstance.getKey());
                            populateProductInstance(ecrfSheetModelRow, childProductInstance, ecrfSheet.getSheetName(), isMigration, productCategoryCode);

                            for (SalesRelationship relationship : filterRelationName(ecrfSheetModelRow.getProductRelationName(), salesRelationships)) {
                                if (relationship.getRelatedProductIdentifier().getProductId().equals(childProductInstance.getProductIdentifier().getProductId())
                                    && relationship.getRelatedProductIdentifier().getStencilId().equals(stencilId)
                                    && childProductInstance.getStencilId().equals(Strings.nullToEmpty(stencilId.getCCode().getValue()))) {
                                    createChildRelationships(workBook, ecrfSheetModelRow, childProductInstance, customerId, contractId, projectId, quoteOptionId, lineItemId, childProductInstance.getProductOffering(), alreadyMapped, assetKeyContainer, isMigration, productCategoryCode);

                                    ProductSalesRelationshipInstance newRelationshipInstance = new ProductSalesRelationshipInstance(relationship, childProductInstance, parentProductInstance);

                                    // The first time we add a relationship we want to clear existing (defaulted) relationships
                                    final AssetRelationshipMap assetRelationshipMap = new AssetRelationshipMap(parentProductInstance.getProductInstanceId(), relationship.getRelationshipName());
                                    if (!alreadyMapped.contains(assetRelationshipMap)) {
                                        alreadyMapped.add(assetRelationshipMap);
                                        Iterator<ProductSalesRelationshipInstance> relationshipInstanceIterator = parentProductInstance.getRelationships().iterator();
                                        while (relationshipInstanceIterator.hasNext()) {
                                            ProductSalesRelationshipInstance productSalesRelationshipInstance = relationshipInstanceIterator.next();
                                            if (productSalesRelationshipInstance.getRelationshipName().equals(relationship.getRelationshipName())) {
                                                try {
                                                    instanceClient.deleteAsset(ChangeAssetDTO.delete(productSalesRelationshipInstance.getRelatedProductInstance()));
                                                    parentProductInstance.setLineItemLockVersion(instanceClient.getCurrentLockVersion(new LineItemId(parentProductInstance.getLineItemId())));
                                                } catch (ResourceNotFoundException e) {
                                                    // We are happy to ignore this - it means it is an asset we have created (and not saved) so
                                                    // it doesn't need deleted.
                                                }
                                                relationshipInstanceIterator.remove();
                                            }
                                        }
                                    }
                                    if (validateSalesRelationShipCount(relationship, parentProductInstance)) {
                                        parentProductInstance.addRelationship(newRelationshipInstance);
                                        childProductInstance.loadConstraintsForInstanceCharacteristics();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private List<SalesRelationship> filterRelationName(final String relationName, List<SalesRelationship> salesRelationships) {
        if (null == relationName) {
            return salesRelationships;
        }
        return newArrayList(Iterables.filter(salesRelationships, new Predicate<SalesRelationship>() {
            @Override
            public boolean apply(@Nullable SalesRelationship input) {
                if (relationName.equals(input.getRelationshipName().value())) {
                    return true;
                }
                return false;
            }

        }));
    }

    protected ContractDTO getContractDto(String contractTerm, String customerId, String productCode, String contractId, String rsqeToken) {
        //todo : need to see this can be cached
        return configuratorClient.createContractDto(contractTerm, customerId, productCode, contractId, rsqeToken);
    }

    protected boolean validateSalesRelationShipCount(SalesRelationship salesRelationship, ProductInstance parentProductInstance) {
        Integer maxSalesRelationshipCount = Math.max(salesRelationship.getMinimumCardinality(parentProductInstance), salesRelationship.getMaximumCardinality(parentProductInstance));
        List<ProductSalesRelationshipInstance> activeRelationships = parentProductInstance.getActiveRelationships();
        Integer countOfMatchedRelations = countRelationshipsFor(salesRelationship, activeRelationships);
        return countOfMatchedRelations < maxSalesRelationshipCount;
    }

    protected Integer countRelationshipsFor(final SalesRelationship requiredSalesRelationship, final List<ProductSalesRelationshipInstance> activeRelationships) {
        return Collections2.filter(activeRelationships, new Predicate<ProductSalesRelationshipInstance>() {
            @Override
            public boolean apply(ProductSalesRelationshipInstance relationshipInstance) {
                return relationshipInstance.getRelationshipName().equals(requiredSalesRelationship.getRelationshipName())
                       && requiredSalesRelationship.isIdentifiedBy(relationshipInstance.getRelatedProductInstance().getProductIdentifier());
            }
        }).size();
    }

    protected void evaluateRelationshipsBasedOnCardinality(ProductInstance productInstance) {
        for (ProductSalesRelationshipInstance productSalesRelationshipInstance : productInstance.getRelationships()) {
            ProductInstance childInstance = productSalesRelationshipInstance.getRelatedProductInstance();
            childInstance.autoAddRelatedInstances(instanceClient, pmr, quoteOptionClient, childInstance.getParentOptional(),
                                                  Optional.of(productSalesRelationshipInstance.getRelationshipName()));
        }
    }

    protected void populateProductInstance(ECRFSheetModelRow row, ProductInstance productInstance, String sheetName, boolean isMigration, ProductCategoryCode productCategoryCode) {
        applyStencilIfApplicable(productInstance, row, sheetName);
        productInstance.setProductCategoryCode(productCategoryCode);
        for (ECRFSheetModelAttribute attribute : row.getAttributes()) {
            Attribute offeringAttribute = productInstance.getProductOffering().getAttribute(new AttributeName(attribute.getName()));
            if (!offeringAttribute.hasAttributeSourceRule()) {
                checkAttributeDataType(offeringAttribute, attribute, sheetName, row.getRowId());
                if (!offeringAttribute.getAllowedValuesAsString().equals(EMPTY)) {
                    checkAllowedValues(offeringAttribute, attribute, sheetName, row.getRowId());
                }
                if (isMigration &&  productInstance.getProductOffering().hasAttribute(new AttributeName(ProductOffering.MIGRATING_ASSET))) {
                    Attribute migrationAttribute = productInstance.getProductOffering().getAttribute(new AttributeName(ProductOffering.MIGRATING_ASSET));
                    productInstance.addInstanceCharacteristic(new InstanceCharacteristic(migrationAttribute, "Yes", InstanceCharacteristic.nullChangeListener()));
                }
                validateMinAndMaximumLength(offeringAttribute, attribute.getValue(), sheetName, row.getRowId());
                productInstance.addInstanceCharacteristic(new InstanceCharacteristic(new Attribute(new AttributeName(attribute.getName()),
                                                                                                   offeringAttribute.getConstraints(),
                                                                                                   offeringAttribute.getBehaviours(),
                                                                                                   offeringAttribute.dataType(),
                                                                                                   DefaultValue.instanceOf(attribute.getValue())),
                                                                                     attribute.getValue(),
                                                                                     InstanceCharacteristic.nullChangeListener()));
            }else if(isNotBlank(attribute.getValue()) && !"null".equals(attribute.getValue())){
                checkAttributeDataType(offeringAttribute, attribute, sheetName, row.getRowId());
                productInstance.addInstanceCharacteristic(new InstanceCharacteristic(new Attribute(new AttributeName(attribute.getName()),
                                                                                                   offeringAttribute.getConstraints(),
                                                                                                   offeringAttribute.getBehaviours(),
                                                                                                   offeringAttribute.dataType(),
                                                                                                   DefaultValue.instanceOf(attribute.getValue())),
                                                                                     attribute.getValue(),
                                                                                     InstanceCharacteristic.nullChangeListener()));
            }
        }
    }

    protected Map<String,SiteDTO> getCustomerSites(String customerId, String projectId) {
        SiteResource siteResource = customerResource.siteResource(customerId);
        List<SiteDTO> siteDTOs = siteResource.get(projectId, SiteResource.SiteFilterType.All);

        return newHashMap(Maps.uniqueIndex(siteDTOs, new Function<SiteDTO, String>() {
            public String apply(SiteDTO input) {
                return input.getSiteId().toString();
            }
        }));
    }

    protected String getCentralSite(String customerId, String projectId) {
        SiteResource siteResource = customerResource.siteResource(customerId);
        SiteDTO siteDTO = siteResource.getCentralSite(projectId);

        return siteDTO.bfgSiteID;
    }

    protected Set<String> checkSiteIdColumnPresentWithValues(ECRFSheetModelRow row) {
        String siteId = row.getAttributeByName(SITE_ID).getValue();
        Set<String> siteIds = newHashSet();
        if (isNotEmpty(siteId) && !siteId.equals("null")) {
            siteIds.add(siteId);
        } else {
            throw new ECRFImportException(String.format(ECRFImportException.siteIdNotFoundInWorkSheet, row.getSheetName()));
        }

        return siteIds;
    }

    protected String getSiteIdFromSheet(ECRFSheetModelRow row, Map<String, SiteDTO> customerSites) {
        String siteId = row.getAttributeByName(SITE_ID).getValue();
        Optional<SiteDTO> site = getSiteIdFromSiteName(customerSites, siteId);
        if (isNotEmpty(siteId) && !siteId.equals("null")
            && !customerSites.containsKey(siteId)  && !site.isPresent()) {
            throw new ECRFImportException(String.format(ECRFImportException.siteIdNotFoundForCustomer, siteId));
        }
        return site.isPresent() ? site.get().bfgSiteID : siteId;
    }

    private Optional<SiteDTO> getSiteIdFromSiteName(Map<String, SiteDTO> customerSites, final String siteName) {
        return Iterables.tryFind(customerSites.values(), new Predicate<SiteDTO>() {
            @Override
            public boolean apply(SiteDTO input) {
                return input.getSiteName().equals(siteName);
            }
        });
    }

    protected void applyStencilIfApplicable(ProductInstance productInstance, ECRFSheetModelRow row, String sheetName) {
        if (productInstance.getProductOffering().isStencilable()) {
            Optional<String> stencilName = row.getStencil();
            if (!stencilName.isPresent()) {
                throw new ECRFImportException(String.format(ECRFImportException.stencilMissingForStencilProduct, sheetName, row.getRowId()));
            }
            boolean stencilFoundAndApplied = false;
            for (StencilInfo stencilInfo : productInstance.getProductOffering().getAvailableStencils()) {
                if (stencilInfo.getName().equals(stencilName.get())) {
                    stencilFoundAndApplied = true;
                    productInstance.setStencilId(StencilId.latestVersionFor(stencilInfo.getCode()));
                }
            }
            if (!stencilFoundAndApplied) {
                throw new ECRFImportException(String.format(ECRFImportException.stencilValueNotInProductOffering, stencilName.get(), sheetName, row.getRowId()));
            }
        }
    }

    protected StencilId getStencilIfApplicable(ProductOffering offering, ECRFSheetModelRow ecrfSheetModelRow, final String sCode) {
        final Optional<String> stencilName = ecrfSheetModelRow.getStencil();
        Optional<SalesRelationship> salesRelationshipOptional = Iterables.tryFind(offering.getSalesRelationships(), new Predicate<SalesRelationship>() {
            @Override
            public boolean apply(SalesRelationship input) {
                return sCode.equals(input.getRelatedProductIdentifier().getProductId()) &&
                       (!stencilName.isPresent() ||
                        stencilName.get().equals(input.getRelatedProductIdentifier().getStencilId().getProductName().getValue()));

            }
        });

        if (salesRelationshipOptional.isPresent()) {
            return salesRelationshipOptional.get().getRelatedProductIdentifier().getStencilId();
        }
        return StencilId.NIL;
    }


    protected void checkAllowedValues(Attribute offeringAttribute, ECRFSheetModelAttribute attribute, String sheetName, String rowId) {
        if (!offeringAttribute.getAllowedValuesAsString().contains(attribute.getValue())) {
            throw new ECRFImportException(String.format(ECRFImportException.valueNotAllowedInAllowedValues,
                                                        attribute.getValue(),
                                                        offeringAttribute.getName(),
                                                        sheetName, rowId, StringUtils.abbreviate(offeringAttribute.getAllowedValuesAsString(), 50)));
        }
    }

    protected void checkAttributeDataType(Attribute offeringAttribute, ECRFSheetModelAttribute attribute, String sheetName, String rowId) {
        try {
            if (isNotEmpty(attribute.getValue())) {
                switch (offeringAttribute.dataType()) {
                    case NUMBER:
                        Double.parseDouble(attribute.getValue());
                        break;
                    case DATE:
                        new SimpleDateFormat(ECRFSheet.CRF_DATE_FORMAT).parse(attribute.getValue());
                        break;
                }
            }
        } catch (Exception ex) {
            if (ex instanceof NumberFormatException || ex instanceof java.text.ParseException) {
                throw new ECRFImportException(String.format(ECRFImportException.attributeDataTypeMisMatch,
                                                            attribute.getValue(),
                                                            offeringAttribute.getName(),
                                                            sheetName,
                                                            rowId,
                                                            offeringAttribute.dataType().toString().toLowerCase()));
            }
        }
    }

    protected void validateMinAndMaximumLength(Attribute offeringAttribute, String value, String sheetName, String rowId) {
        if (isNotEmpty(value)) {
            Optional<Integer> maximumStringLength = offeringAttribute.metaData().getMaximumStringLength();
            if (maximumStringLength.isPresent()) {
                if (maximumStringLength.get() < value.length()) {
                    throw new ECRFImportException(String.format(ECRFImportException.maximumLengthExceeded,
                                                                offeringAttribute.getName().getName(), maximumStringLength.get(), value, sheetName, rowId));

                }
            }

            Optional<Integer> minimumStringLength = offeringAttribute.metaData().getMinimumStringLength();
            if (minimumStringLength.isPresent()) {
                if (minimumStringLength.get() > value.length()) {
                    throw new ECRFImportException(String.format(ECRFImportException.minimumLengthExceeded,
                                                                offeringAttribute.getName().getName(), minimumStringLength.get(), value, sheetName, rowId));

                }
            }
        }
    }

    protected static class BulkProduct {

        private final LineItemId lineItemId;
        private final boolean aBulkImport;

        public BulkProduct(LineItemId lineItemId, boolean aBulkImport) {
            this.lineItemId = lineItemId;
            this.aBulkImport = aBulkImport;
        }

        public LineItemId getLineItemId() {
            return lineItemId;
        }

        public boolean isBulkImport() {
            return aBulkImport;
        }
    }
}
