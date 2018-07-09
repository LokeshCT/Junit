package com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet;

import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.dto.AssetDTO;
import com.bt.rsqe.customerinventory.parameter.LengthConstrainingProductInstanceId;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.AbstractNotificationEvent;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.Notification;
import com.bt.rsqe.domain.bom.parameters.ProductInstanceId;
import com.bt.rsqe.domain.product.Association;
import com.bt.rsqe.domain.product.Attribute;
import com.bt.rsqe.domain.product.AttributeName;
import com.bt.rsqe.domain.product.ConfigurationPhase;
import com.bt.rsqe.domain.product.ContributesToCharacteristicUpdater;
import com.bt.rsqe.domain.product.InstanceCharacteristic;
import com.bt.rsqe.domain.product.InstanceTreeScenario;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.ProductSalesRelationshipInstance;
import com.bt.rsqe.domain.product.constraints.AllowedValuesProvider;
import com.bt.rsqe.domain.product.constraints.AttributeValue;
import com.bt.rsqe.domain.product.extensions.RuleValidation;
import com.bt.rsqe.domain.product.extensions.StructuredRule;
import com.bt.rsqe.domain.project.BasicLocalDPOnlyValidator;
import com.bt.rsqe.domain.project.DPConstraintValidator;
import com.bt.rsqe.domain.project.InstanceCharacteristicNotFound;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.domain.project.ProductOfferingAvailabilityValidator;
import com.bt.rsqe.domain.project.RFOUploadValidator;
import com.bt.rsqe.expedio.contact.BFGContactCreationFailureException;
import com.bt.rsqe.expressionevaluator.AssetTreeEvaluator;
import com.bt.rsqe.expressionevaluator.ContextualEvaluatorMap;
import com.bt.rsqe.keys.ProjectQuoteKey;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.bt.rsqe.productinstancemerge.ChangeType;
import com.bt.rsqe.productinstancemerge.MergeResult;
import com.bt.rsqe.projectengine.migration.QuoteMigrationDetailsProvider;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet.bfgcontactsattributes.BFGContactAttribute;
import com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet.bfgcontactsattributes.BFGContactsStrategy;
import com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet.bfgcontactsattributes.BFGContactsStrategyFactory;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.bt.rsqe.domain.product.InstanceTreeScenario.*;
import static com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet.RFOSheetMarshaller.Column.*;
import static com.bt.rsqe.utils.AssertObject.*;
import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Iterables.*;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Lists.transform;
import static com.google.common.collect.Maps.*;

public class RFOSheetModel {

    private static final RFOSheetLog LOG = LogFactory.createDefaultLogger(RFOSheetLog.class);
    public static final String SITE_ID_HEADER = SITE_ID.header;
    public static final String SITE_NAME_HEADER = SITE_NAME.header;
    public static final String SUMMARY_HEADER = SUMMARY.header;
    public static final String LINE_ITEM_ID_HEADER = LINE_ITEM_ID.header;
    public static final String PRODUCT_INSTANCE_ID_HEADER = PRODUCT_INSTANCE_ID.header;
    public static final String PRODUCT_NAME_HEADER = PRODUCT_NAME.header;
    public static final String SUBLOCATION_NAME_HEADER = SUBLOCATION_NAME.header;
    public static final String SUBLOCATION_ID_HEADER = SUBLOCATION_ID.header;
    public static final String ROOM_HEADER = ROOM.header;
    public static final String FLOOR_HEADER = FLOOR.header;
    public static final String MANDATORY_FLAG = " (M)";
    public static final String OPTIONAL_FLAG = " (O)";

    private final ProductInstanceClient futureProductInstanceClient;
    private List<RFORowModel> rfoExportModel = newArrayList();
    private Map<Integer, String[]> columnRestrctions = new HashMap<Integer, String[]>();
    private String sheetName;
    private BFGContactsStrategyFactory bfgContactsStrategyFactory;
    private String contractId;
    private QuoteMigrationDetailsProvider migrationDetailsProvider;
    private ContributesToCharacteristicUpdater contributesToCharacteristicUpdater;
    private Map<ProjectQuoteKey, Boolean> migrationQuoteDetails = newHashMap();

    public String getsCode() {
        return sCode;
    }
    private String sCode;

    public RFOSheetModel(ProductInstanceClient futureProductInstanceClient, String sheetName, String sCode,
                         QuoteMigrationDetailsProvider migrationDetailsProvider,
                         ContributesToCharacteristicUpdater contributesToCharacteristicUpdater) {
        this.futureProductInstanceClient = futureProductInstanceClient;
        this.sheetName = sheetName;
        this.sCode = sCode;
        this.contributesToCharacteristicUpdater = contributesToCharacteristicUpdater;
        this.bfgContactsStrategyFactory = new BFGContactsStrategyFactory();
        this.migrationDetailsProvider = migrationDetailsProvider;
    }

      RFOSheetModel(BFGContactsStrategyFactory bfgContactsStrategyFactory, ProductInstanceClient futureProductInstanceClient, String sheetName, String sCode, QuoteMigrationDetailsProvider migrationDetailsProvider, ContributesToCharacteristicUpdater contributesToCharacteristicUpdater) {
        this(futureProductInstanceClient, sheetName, sCode, migrationDetailsProvider, contributesToCharacteristicUpdater);
        this.bfgContactsStrategyFactory = bfgContactsStrategyFactory;
    }

    public void add(LineItemModel lineItem, String sCode) {
        rfoExportModel.add(createRFOModelFor(lineItem, sCode));
    }

    private RFORowModel createRFOModelFor(LineItemModel lineItem, String sCode) {
        final SiteDTO site = lineItem.getSite();
        String summary = lineItem.getSummary();

        AssetDTO asset = lineItem.getRootInstance();
        ProductInstance productInstance = futureProductInstanceClient.convertAssetToLightweightInstance(asset);

        RFORowModel model = new RFORowModel(lineItem.getLineItemId(),
                                            site.bfgSiteID,
                                            site.name,
                                            sCode,
                                            summary,
                                            productInstance,
                                            Maps.<String, List<String>>newLinkedHashMap());

        final Optional<AssetDTO> asIsAsset = futureProductInstanceClient.getSourceAssetDTO(productInstance.getProductInstanceId().getValue());
        ProductInstance asIsInstance = null;
        if(asIsAsset.isPresent()) {
            asIsInstance = futureProductInstanceClient.convertAssetToLightweightInstance(asIsAsset.get());
        }

        final MergeResult mergeResult = futureProductInstanceClient.getMergeResult(productInstance, asIsInstance, PROVIDE);

        Optional<? extends BFGContactsStrategy> bfgContactsStrategy = bfgContactsStrategyFactory.getStrategyFor(productInstance.getSimpleProductOfferingType());

        populateRowModelRecursively(model, productInstance, bfgContactsStrategy, mergeResult);

        return model;
    }

    private List<RFOAttribute> getConfigurableReadyForOrderAttributes(final ProductInstance productInstance, final InstanceTreeScenario scenario) {
        List<Attribute> attributes = productInstance.whatReadyForOrderAttributesShouldIConfigureForScenario(scenario);

        final boolean serviceDeliveryAttributesAllowed = checkForMigrationQuote(productInstance);

        // if migration quote then all service delivery attributes are applicable in RFO sheet,
        // otherwise revert to the attribute's behaviour for eligibility.
        Iterable<Attribute> configurableAttributes = Iterables.filter(attributes, new Predicate<Attribute>() {
            @Override
            public boolean apply(@Nullable Attribute input) {
                if (input.isServiceDelivery()) {
                    return serviceDeliveryAttributesAllowed;
                } else {
                    return !input.isHidden();
                }
            }
        });

        return newArrayList(Iterables.transform(configurableAttributes, new Function<Attribute, RFOAttribute>() {
            @Override
            public RFOAttribute apply(@Nullable Attribute input) {
                boolean mandatoryAttribute = input.isRequiredForOrderPhase(scenario);
                return new RFOAttribute(input, mandatoryAttribute);
            }
        }));
    }

    private boolean checkForMigrationQuote(ProductInstance productInstance) {
        ProjectQuoteKey key = new ProjectQuoteKey(productInstance.getProjectId(), productInstance.getQuoteOptionId());

        Boolean isMigrationQuote = migrationQuoteDetails.get(key);

        if(null == isMigrationQuote) {
            isMigrationQuote = migrationDetailsProvider.conditionalFor(productInstance)
                                       .isMigrationQuote()
                                       .check();
            migrationQuoteDetails.put(key, isMigrationQuote);
        }

        return isMigrationQuote;
    }

    private void populateRowModelRecursively(RFORowModel model,
                                             ProductInstance productInstance,
                                             Optional<? extends BFGContactsStrategy> bfgContactsStrategy,
                                             MergeResult mergeResult) {
        try {
            InstanceTreeScenario scenario = identifyJourneyTypeBasedOn(productInstance, mergeResult);
            List<RuleValidation> productInstanceRules = addFilteredRules(productInstance);
            final boolean changeNotAdd = !ChangeType.ADD.equals(mergeResult.changeFor(productInstance));

            for (RFOAttribute attribute : getConfigurableReadyForOrderAttributes(productInstance, InstanceTreeScenario.PROVIDE)) {
                InstanceCharacteristic instanceCharacteristic = productInstance.getInstanceCharacteristic(attribute.get().getName());
                final Optional<AllowedValuesProvider> allowedValuesProvider = instanceCharacteristic.getAllowedValuesProvider();

                if (allowedValuesProvider.isPresent()) {
                    model.addAllowedValues(getHeaderFrom(attribute), getAllowedValues(allowedValuesProvider.get().getAllowedValues()));
                    model.addAllowOverride(getHeaderFrom(attribute), allowedValuesProvider.get().getAllowUserOverride());
                }

                if (instanceCharacteristic.isReadOnly() || instanceCharacteristic.isWritabliltyFixed() && changeNotAdd) {
                    model.addLockedColumns(getHeaderFrom(attribute));
                }

                if (instanceCharacteristic.isHiddenFor(scenario) || instanceCharacteristic.isMarkedAsConstant() ||
                    instanceCharacteristic.isWritabliltyFixed() && changeNotAdd) {
                    model.addGrayOutColumns(getHeaderFrom(attribute));
                }

                model.addAttribute(getHeaderFrom(attribute), instanceCharacteristic.getStringValue());

                if (!productInstanceRules.isEmpty() && getHeaderFrom(attribute).contains(OPTIONAL_FLAG)) {
                    List<String> errorTexts = getErrorTexts(productInstanceRules, attribute.get(), productInstance);
                    model.addConditionalAttributes(getHeaderFrom(attribute), errorTexts);
                }
            }

            addBFGContactsAttributes(model, bfgContactsStrategy, productInstance);
        } catch (InstanceCharacteristicNotFound instanceCharacteristicNotFound) {
            // Do nothing.
        }

        for (ProductInstance child : productInstance.getChildren()) {
            String childProductScode = sCode(child);
            RFORowModel childRowModel = new RFORowModel(child.getProductInstanceId(), retrieveProductOrStencilName(child), childProductScode);
            Optional<? extends BFGContactsStrategy> bfgContactsStrategyForChild = bfgContactsStrategyFactory.getStrategyFor(child.getSimpleProductOfferingType());
            populateRowModelRecursively(childRowModel, child, bfgContactsStrategyForChild, mergeResult);
            model.addChild(childProductScode, childRowModel);
        }
    }

    private List<String> getErrorTexts(List<RuleValidation> productInstanceRules, Attribute attribute, ProductInstance productInstance) {
        List<String> errorTexts = newArrayList();
        for (RuleValidation ruleValidation : productInstanceRules) {
            if (ruleValidation.getNonFilterContributingAttributes().contains(attribute.getName().getName())) {
                errorTexts.add(ruleValidation.getErrorText(ContextualEvaluatorMap.defaultEvaluator(new AssetTreeEvaluator(productInstance))));
            }
        }
        return errorTexts;
    }

    private List<RuleValidation> addFilteredRules(ProductInstance productInstance) {
        List<RuleValidation> productInstanceRules = newArrayList();
        for (StructuredRule structuredRule : productInstance.getProductOffering().getRules()) {
            if (structuredRule.isValidationRule()) {
                productInstanceRules.add((RuleValidation) structuredRule);
            }
        }
        return productInstanceRules;
    }

    private InstanceTreeScenario identifyJourneyTypeBasedOn(ProductInstance productInstance, MergeResult mergeResult) {
        final ChangeType changeType = mergeResult.changeFor(productInstance);
        return ChangeType.ADD.equals(changeType) ? PROVIDE : (ChangeType.UPDATE.equals(changeType) ? MODIFY : CEASE);
    }

    private List<String> getAllowedValues(List<AttributeValue> allowedValues) {
        return transform(allowedValues, new Function<AttributeValue, String>() {
            @Override
            public String apply(AttributeValue input) {
                return input.getValue().toString();
            }
        });
    }

    private String getHeaderFrom(RFOAttribute attribute) {
        String header = null != attribute.get().getDisplayName() ? attribute.get().getDisplayName() : attribute.get().getName().getName();

        if (attribute.isMandatory()) {
            header = header.concat(MANDATORY_FLAG);
        } else {
            header = header.concat(OPTIONAL_FLAG);
        }

        return header;
    }

    private String retrieveProductOrStencilName(ProductInstance productInstance) {
        if (productInstance.isStencilable()) {
            InstanceCharacteristic instanceCharacteristic = null;
            try {
                if (productInstance.hasInstanceCharacteristic(new AttributeName(ProductOffering.PRODUCT_IDENTIFIER_RESERVED_NAME))) {
                    instanceCharacteristic = productInstance.getInstanceCharacteristic(new AttributeName(ProductOffering.PRODUCT_IDENTIFIER_RESERVED_NAME));
                } else if (productInstance.hasInstanceCharacteristic(new AttributeName(ProductOffering.STENCIL_RESERVED_NAME))) {
                    instanceCharacteristic = productInstance.getInstanceCharacteristic(new AttributeName(ProductOffering.STENCIL_RESERVED_NAME));
                }
            } catch (InstanceCharacteristicNotFound instanceCharacteristicNotFound) {
                // do nothing.
            }
            if(instanceCharacteristic != null && instanceCharacteristic.getSingleConstraintValueOption().isPresent()){
                return instanceCharacteristic.getSingleConstraintValueOption().or(AttributeValue.EMPTY).getCaption();
            }
        }
        return productInstance.getProductIdentifier().getProductName();
    }

    private void addBFGContactsAttributes(RFORowModel model, Optional<? extends BFGContactsStrategy> bfgContactsStrategy,
                                          ProductInstance productInstance) throws InstanceCharacteristicNotFound {

        if (bfgContactsStrategy.isPresent()) {
            BFGContactsStrategy strategy = bfgContactsStrategy.get();
            List<BFGContactAttribute> bfgContactsAttributes = strategy.getBFGContactsAttributes(productInstance);
            for (BFGContactAttribute attribute : bfgContactsAttributes) {
                model.addAttribute(attribute.getName().concat(MANDATORY_FLAG), attribute.getValue());
            }
        }
    }

    private String sCode(ProductInstance productInstances) {
        return productInstances.getProductIdentifier().getProductId();
    }

    public List<RFORowModel> getRFOExportModel() {
        return rfoExportModel;
    }

    public Map<Integer, String[]> getColumnRestrictions() {
        return columnRestrctions;
    }

    // TODO XLS sheet has a max chars length and so the
    // comparison of sheet names is failing if the
    // first so many characters are the same
    public String sheetName() {
        if(sheetName.toUpperCase().contains("COTC")){
            sheetName = "COTC " + sheetName;
        }
        return sheetName;
    }

    public void update() {
        for (RFORowModel rfoRowModel : rfoExportModel) {
            ProductInstance productInstance = reloadAsset(rfoRowModel.productInstance);

            Optional<? extends BFGContactsStrategy> bfgContactsStrategy = bfgContactsStrategyFactory.getStrategyFor(productInstance.getSimpleProductOfferingType());
            final Optional<ProductInstance> asIsOptional = futureProductInstanceClient.getSourceAsset(new LengthConstrainingProductInstanceId(productInstance.getProductInstanceId().getValue()));
                    final MergeResult mergeResult = futureProductInstanceClient.getAssetsDiff(productInstance.getProductInstanceId().getValue(), productInstance.getProductInstanceVersion(),
                        asIsOptional.isPresent() ? asIsOptional.get().getProductInstanceVersion() : null, PROVIDE);
            updateProductInstanceFromRFOSheet(productInstance, rfoRowModel, bfgContactsStrategy);

            futureProductInstanceClient.put(productInstance);
            updateContributesToCharacteristics(productInstance, rfoRowModel);

            productInstance = futureProductInstanceClient.getByAssetKey(productInstance.getKey());
            InstanceTreeScenario instanceTreeScenario = identifyJourneyTypeBasedOn(productInstance, mergeResult);
            updateProductInstanceWithApplicableValidators(productInstance,instanceTreeScenario, mergeResult);
            Notification notification = productInstance.validate(instanceTreeScenario,ConfigurationPhase.POST_CREDIT_VET);

            /**
             *  Evaluate rules with validation type as RFOUploadValidation
             */
            notification.add(new RFOUploadValidator(productInstance).validate());

            if(notification.hasErrors()){

                LOG.invalidRFOValues(new RFOImportException(RFOImportException.DEFAULT_ERROR_MESSAGE + " for the product "+ productInstance.getProductName() + " : Detailed Errors :: " + notification.getErrorEvents().toString()));
               throw new RFOImportException(RFOImportException.DEFAULT_ERROR_MESSAGE + " for the product "+ productInstance.getProductName()+ ".<br/><br/>"+ Joiner.on(",<br/>").join(Iterables.transform(notification.getErrorEvents(),new Function<AbstractNotificationEvent, Object>() {
                    @Override
                    public String apply(AbstractNotificationEvent input) {
                        return input.getMessage()+"<br/>";
                    }
                }))+"<br/>Please correct the values and import again.");
            }

        }
    }

    private void updateContributesToCharacteristics(ProductInstance productInstance, RFORowModel rfoRowModel) {
        List<String> modifiedAttributes = rfoRowModel.modifiedAttributes();
        addAdditionalAttributes(productInstance,modifiedAttributes);
        if (!modifiedAttributes.isEmpty()) {
            productInstance = reloadAsset(productInstance);
            contributesToCharacteristicUpdater.update(productInstance, modifiedAttributes);
        }
        for (final RFORowModel childRfoRowModel : rfoRowModel.getChildren()) {
            Optional<ProductInstance> childInstance = tryFind(productInstance.getChildren(), new Predicate<ProductInstance>() {
                @Override
                public boolean apply(ProductInstance input) {
                    return input.getProductInstanceId().equals(childRfoRowModel.getProductInstanceId());
                }
            });
            if(childInstance.isPresent()) {
                updateContributesToCharacteristics(childInstance.get(), childRfoRowModel);
            }
            else {
                String childInstanceId= childRfoRowModel.getProductInstanceId().getValue();
                LOG.invalidValues(childInstanceId);
            }
        }
    }

    private void addAdditionalAttributes(ProductInstance productInstance, List<String> modifiedAttributes) {
        List<String> attributesWithExecutionPhaseDefined = productInstance.getProductOffering().getAttributesWithExecutionPhaseDefined();
        productInstance = reloadAsset(productInstance);
        contributesToCharacteristicUpdater.reloadCharacteristics(productInstance, attributesWithExecutionPhaseDefined);
        modifiedAttributes.addAll(attributesWithExecutionPhaseDefined);
    }


    private void updateContributesToNonCharacteristics(ProductInstance productInstance) {
        Map<Association, List<AssetKey>> attributeAssociations = newHashMap();
        productInstance = reloadAsset(productInstance);
        attributeAssociations.putAll(futureProductInstanceClient.getContextActionAssociation(productInstance));
        contributesToCharacteristicUpdater.update(attributeAssociations);
    }

    private ProductInstance reloadAsset(ProductInstance productInstance) {
        return futureProductInstanceClient.getByAssetKey(productInstance.getKey());
    }

    private void updateProductInstanceWithApplicableValidators(ProductInstance productInstance, InstanceTreeScenario instanceTreeScenario, MergeResult mergeResult) {
        if(InstanceTreeScenario.MODIFY.equals(instanceTreeScenario) || InstanceTreeScenario.CEASE.equals(instanceTreeScenario) || checkForMigrationQuote(productInstance)){
           if(productInstance.getValidators().isEmpty()){
               productInstance.addValidator(new BasicLocalDPOnlyValidator());
               productInstance.addValidator(new DPConstraintValidator());
           }else{
               productInstance.getValidators().remove(new ProductOfferingAvailabilityValidator());
           }
        }
    }

    private void updateProductInstanceFromRFOSheet(ProductInstance productInstance, RFORowModel rfoRowModel,
                                                   Optional<? extends BFGContactsStrategy> bfgContactsStrategy) {
        if (bfgContactsStrategy.isPresent()) {
            try {
                BFGContactsStrategy strategy = bfgContactsStrategy.get();
                strategy.createAndPersistBFGContactID(rfoRowModel);
            } catch (BFGContactCreationFailureException e) {
                throw new RFOImportException("Contacts not created in BFG " + e.getMessage());
            } catch (InstanceCharacteristicNotFound instanceCharacteristicNotFound) {
                // Do nothing
            }
        }

        if (!StringUtils.isBlank(contractId)) {
            updateContractID(productInstance);
        }
        try {
            for (RFOAttribute attribute : getConfigurableReadyForOrderAttributes(productInstance, InstanceTreeScenario.PROVIDE)) {
                InstanceCharacteristic instanceCharacteristic = productInstance.getInstanceCharacteristic(attribute.get().getName());
                String existingValue = instanceCharacteristic.getStringValue();
                String newValue = rfoRowModel.getAttributes().get(getHeaderFrom(attribute));
                if( !existingValue.equals(newValue)) {
                    instanceCharacteristic.setValue(newValue);
                    rfoRowModel.addModifiedAttribute(attribute.get().getName().getName());
                }
            }
        } catch (InstanceCharacteristicNotFound instanceCharacteristicNotFound) {
            // Do nothing
        }
        for (ProductInstance child : productInstance.getChildren()) {
            String childScode = child.getProductOffering().getProductIdentifier().getProductId();
            Optional<RFOSheetModel.RFORowModel> childRFORowModel = rfoRowModel.getChild(childScode, child.getProductInstanceId());
            if (childRFORowModel.isPresent()) {
                Optional<? extends BFGContactsStrategy> childBfgContactsStrategy = bfgContactsStrategyFactory.getStrategyFor(child.getSimpleProductOfferingType());
                updateProductInstanceFromRFOSheet(child, childRFORowModel.get(), childBfgContactsStrategy);
            }
        }
    }

    private void updateContractID(ProductInstance productInstance) {
        for(ProductSalesRelationshipInstance productSalesRelationshipInstance : productInstance.getRelationships()) {
            ProductInstance relatedProductInstance = productSalesRelationshipInstance.getRelatedProductInstance();
            updateContractID(relatedProductInstance);
        }
        try {
            InstanceCharacteristic contractIdInstanceCharacteristic = productInstance.getInstanceCharacteristic("CONTRACT ID");
            contractIdInstanceCharacteristic.setValue(contractId);
        } catch (InstanceCharacteristicNotFound instanceCharacteristicNotFound) {
            //skip for products which do not have hidden RFO - Contract ID
        }
    }

    public void setContractId(String contractId) {
        this.contractId = contractId;
    }

    private Collection<Attribute> getReadyForOrderAttributesAndFilterHidden(ProductInstance productInstance, InstanceTreeScenario scenario) throws InstanceCharacteristicNotFound {
        return filter(productInstance.whatReadyForOrderAttributesShouldIConfigureForScenario(scenario), new Predicate<Attribute>() {
            @Override
            public boolean apply(Attribute attribute) {
                return !attribute.isHidden();
            }
        });
    }

    public static class RFORowModel {
        private String sCode;
        private String siteId;
        private Map<String,String> rfoModel = newLinkedHashMap();
        private List<String> modifiedAttributes = newArrayList();
        private Map<String,List<String>> allowedValues = newLinkedHashMap();
        private Map<String,Boolean> allowOverride = newLinkedHashMap();
        private List<String> lockedColumns = newArrayList();
        private List<String> grayOutColumns = newArrayList();
        private Map<String, List<RFORowModel>> sCodeChildMap = new TreeMap<String, List<RFORowModel>>();
        private Map<String, List<String>> conditionalAttributes = newLinkedHashMap();

        private ProductInstance productInstance;

        public void addChild(String sCode, RFORowModel rfoRowModel) {
            if (!sCodeChildMap.containsKey(sCode)) {
                sCodeChildMap.put(sCode, new ArrayList<RFORowModel>());
            }
            sCodeChildMap.get(sCode).add(rfoRowModel);
        }

        public RFORowModel(LineItemId lineItemId, String siteID, String siteName, String sCode, String summary) {
            this(lineItemId, siteID, siteName, sCode, summary, null);
        }

        public RFORowModel(LineItemId lineItemId, String siteID, String siteName, String sCode, String summary, ProductInstance productInstance) {
            this(lineItemId, sCode);
            this.productInstance = productInstance;
            this.siteId = siteID;
            rfoModel.put(SITE_ID_HEADER,siteID);
            rfoModel.put(SITE_NAME_HEADER, siteName);
            rfoModel.put(SUMMARY_HEADER, summary);
        }

        public RFORowModel(LineItemId lineItemId, String siteID, String siteName, String sCode, String summary, ProductInstance productInstance, Map<String, List<String>> conditionalAttributes) {
            this(lineItemId, siteID, siteName, sCode, summary, productInstance);
            this.conditionalAttributes = conditionalAttributes;
        }

        private RFORowModel(LineItemId lineItemId, String sCode) {
            rfoModel.put(LINE_ITEM_ID_HEADER, lineItemId.value());
            this.sCode = sCode;
        }

        public RFORowModel(ProductInstanceId productInstanceId, String productName, String sCode) {
            rfoModel.put(PRODUCT_INSTANCE_ID_HEADER, productInstanceId.getValue());
            rfoModel.put(PRODUCT_NAME_HEADER, productName);
            this.sCode = sCode;
        }

        public RFORowModel(String sCode) {
            this.sCode = sCode;
        }

        public void addAttribute(String headerName, String value) {
            rfoModel.put(headerName, value);
        }

        public String getAttribute(String headerName) {
            return rfoModel.get(headerName);
        }

        Map<String, String> getAttributes() {
            return rfoModel;
        }

        void addModifiedAttribute(String headerName) {
            modifiedAttributes.add(headerName);
        }

        List<String> modifiedAttributes() {
            return modifiedAttributes;
        }

        boolean hasModifiedAttributes() {
            return modifiedAttributes.size() > 0;
        }

        boolean hasAsset() {
            return isNotNull(productInstance);
        }

        public void addAllowedValues(String headerName, List<String> allowedValues){
          this.allowedValues.put(headerName,allowedValues);
        }

        public void addAllowOverride(String headerName, Boolean allowOverride){
          this.allowOverride.put(headerName, allowOverride);
        }

        public List<String> getAllowedValues(String headerName){
           return allowedValues.get(headerName);
        }

        public Boolean getAllowOverride(String headerName){
            if(allowOverride.containsKey(headerName)){
                return allowOverride.get(headerName);
            }
            return false;
        }

        public void addConditionalAttributes(String attributeName, List<String> errorTexts){
            this.conditionalAttributes.put(attributeName, errorTexts);
        }

        public List<String> getConditionalAttributes(String attributeName){
            return conditionalAttributes.get(attributeName);
        }

        Map<String, List<String>> getConditionalAttributes(){
            return conditionalAttributes;
        }

        public void addLockedColumns(String headerName){
            this.lockedColumns.add(headerName);
        }

        public void addGrayOutColumns(String headerName) {
            this.grayOutColumns.add(headerName);
        }

        public boolean getLockedColumns(String headerName) {
            return lockedColumns.contains(headerName);
        }

        public boolean getGrayOutColumns(String headerName) {
            return grayOutColumns.contains(headerName);
        }

        public List<RFORowModel> getChildren(String sCode) {
            return sCodeChildMap.get(sCode) != null ? sCodeChildMap.get(sCode) : new ArrayList<RFORowModel>();
        }

        public List<RFORowModel> getChildren() {
            List<RFORowModel> children = newArrayList();
            for (List<RFORowModel> rfoRowModels : sCodeChildMap.values()) {
                children.addAll(rfoRowModels);
            }
            return children;
        }

        public Optional<RFORowModel> getChild(String sCode, ProductInstanceId productInstanceId) {
            RFORowModel childRFORowModel = null;
            for (RFORowModel rfoRowModel : getChildren(sCode)) {
                if (productInstanceId.equals(rfoRowModel.getProductInstanceId())) {
                    childRFORowModel = rfoRowModel;
                    break;
                }
            }
            return Optional.fromNullable(childRFORowModel);
        }

        public int getLeafNodeCount() {
            return getLeafNodeCount(this);
        }

        private int getLeafNodeCount(RFOSheetModel.RFORowModel rfoRowModel) {
            if(rfoRowModel.hasChildren()){
                int childTotal = 0;
                for (String sCode : rfoRowModel.getRFOChildrenMap().keySet()) {
                    for (RFOSheetModel.RFORowModel child : rfoRowModel.getChildren(sCode)) {
                        childTotal += getLeafNodeCount(child);
                    }
                }
                return childTotal;
            }else{
                return 1;
            }
        }

        public Map<String, List<RFORowModel>> getRFOChildrenMap() {
            return sCodeChildMap;
        }

        public boolean hasChildren() {
            return sCodeChildMap.size() > 0;
        }

        private ProductInstanceId getProductInstanceId() {
            return new ProductInstanceId(rfoModel.get(PRODUCT_INSTANCE_ID_HEADER));
        }

        public String getsCode() {
            return sCode;
        }

        public String getSiteId() {
            return siteId;
        }

        public ProductInstance getProductInstance() {
            return productInstance;
        }

        public boolean hasAttribute(String attributeName) {
            return rfoModel.containsKey(attributeName);
        }
    }

    private interface RFOSheetLog {
        @Log(level = LogLevel.WARN, format = "%s")
        void invalidRFOValues(Throwable error);

        @Log(level = LogLevel.ERROR, format = "ChildInstance is not present in RFORowModel with id :%s")
        void invalidValues(String childInstanceId);
    }

    private class RFOAttribute {
        private Attribute attribute;
        private boolean mandatory;

        public RFOAttribute(Attribute attribute, boolean mandatory) {
            this.attribute = attribute;
            this.mandatory = mandatory;
        }

        public Attribute get() {
            return attribute;
        }

        public boolean isMandatory() {
            return mandatory;
        }
    }
}
