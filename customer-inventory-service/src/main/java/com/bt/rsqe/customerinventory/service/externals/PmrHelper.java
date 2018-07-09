package com.bt.rsqe.customerinventory.service.externals;

import com.bt.rsqe.client.Pmr;
import com.bt.rsqe.customerinventory.parameter.CustomerId;
import com.bt.rsqe.customerinventory.parameter.ProjectId;
import com.bt.rsqe.customerinventory.parameter.SiteId;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristicValue;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetStencilDetail;
import com.bt.rsqe.customerinventory.service.evaluators.CIFAssetCharacteristicEvaluatorFactory;
import com.bt.rsqe.customerinventory.service.evaluators.CIFAssetEvaluator;
import com.bt.rsqe.customerinventory.service.evaluators.CIFAttributeMatchEvaluator;
import com.bt.rsqe.customerinventory.service.evaluators.CIFNewGroupIdCalculator;
import com.bt.rsqe.customerinventory.service.extenders.CIFAssetStencilDetailConverter;
import com.bt.rsqe.customerinventory.service.extenders.reservedattributes.StencilReservedAttributesHelper;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;
import com.bt.rsqe.customerinventory.service.repository.CIFAssetJPARepository;
import com.bt.rsqe.domain.ProductOfferingVersion;
import com.bt.rsqe.domain.StencilCode;
import com.bt.rsqe.domain.StencilId;
import com.bt.rsqe.domain.bom.parameters.ProductSCode;
import com.bt.rsqe.domain.bom.parameters.QrefRequestUniqueId;
import com.bt.rsqe.domain.product.AccessDetail;
import com.bt.rsqe.domain.product.Attribute;
import com.bt.rsqe.domain.product.AttributeName;
import com.bt.rsqe.domain.product.ProductCategory;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.SimpleProductOfferingType;
import com.bt.rsqe.domain.product.constraints.AttributeValue;
import com.bt.rsqe.domain.product.extensions.RuleAttributeSource;
import com.bt.rsqe.domain.product.parameters.ProductCategoryCode;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.product.parameters.RelationshipName;
import com.bt.rsqe.domain.project.CountryResolver;
import com.bt.rsqe.expressionevaluator.ContextualEvaluatorMap;
import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.pmr.dto.JourneyBehaviourDTO;
import com.bt.rsqe.productinstancemerge.ChangeType;
import com.google.common.base.Optional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.bt.rsqe.customerinventory.service.extenders.CIFAssetCharacteristicValueConverter.*;
import static com.bt.rsqe.domain.product.SimpleProductOfferingType.*;
import static com.bt.rsqe.domain.product.SimpleProductOfferingType.Package;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Sets.newHashSet;
import static org.apache.commons.lang.StringUtils.*;

public class PmrHelper {
    private final PmrClient pmr;
    private final CountryResolver countryResolver;
    private CIFAssetOrchestrator cifAssetOrchestrator;
    private final CIFAssetCharacteristicEvaluatorFactory evaluatorFactory;
    private final StencilReservedAttributesHelper stencilReservedAttributes;
    private CIFAssetJPARepository cifAssetJPARepository;

    public PmrHelper(PmrClient pmr, CIFAssetOrchestrator cifAssetOrchestrator, CountryResolver countryResolver,
                     CIFAssetCharacteristicEvaluatorFactory evaluatorFactory,
                     StencilReservedAttributesHelper stencilReservedAttributes, CIFAssetJPARepository cifAssetJPARepository) {
        this.pmr = pmr;
        this.cifAssetOrchestrator = cifAssetOrchestrator;
        this.countryResolver = countryResolver;
        this.evaluatorFactory = evaluatorFactory;
        this.stencilReservedAttributes = stencilReservedAttributes;
        this.cifAssetJPARepository = cifAssetJPARepository;
    }

    public ProductOffering getProductOffering(CIFAsset cifAsset) {
        AccessDetail accessDetail = new AccessDetail(
            QrefRequestUniqueId.newInstance(
                cifAsset.getAssetKey().getAssetId(),
                Long.toString(cifAsset.getAssetKey().getAssetVersion())).value());

        Pmr.ProductOfferings offerings = pmr.productOffering(ProductSCode.newInstance(cifAsset.getProductCode()))
                                            .forOfferingVersion(ProductOfferingVersion.newInstance(cifAsset.getProductVersion()))
                                            .withAccessDetail(accessDetail);

        if(!isEmpty(cifAsset.getSiteId()) && !isEmpty(cifAsset.getProjectId())){
            offerings = offerings.forCountry(countryResolver.countryForSite(new CustomerId(cifAsset.getCustomerId()),
                                                                            new ProjectId(cifAsset.getProjectId()),
                                                                            new SiteId(cifAsset.getSiteId())));
        }

        final CIFAssetStencilDetail stencilDetail = stencilReservedAttributes.getStencilDetail(cifAsset);
        if(stencilDetail!=null && stencilDetail.getStencilCode()!=null){
            StencilId stencilId = CIFAssetStencilDetailConverter.toStencilId(stencilDetail);
            offerings = offerings.withStencil(stencilId);
        }

        return offerings.get();
        }

    public Optional<List<CIFAssetCharacteristicValue>> getAllowedValues(CIFAsset asset, Attribute attribute) {
        Optional<List<CIFAssetCharacteristicValue>> attributeSourcedCharacteristicValues = getAttributeSourcedValues(attribute);
        Optional<List<CIFAssetCharacteristicValue>> ruleSourcedCharacteristicValues = getRuleSourcedValues(asset, attribute);

        if (ruleSourcedCharacteristicValues.isPresent() && !ruleSourcedCharacteristicValues.get().isEmpty()) {
            return ruleSourcedCharacteristicValues;
        }else{
            return attributeSourcedCharacteristicValues;
        }
    }

    private Optional<List<CIFAssetCharacteristicValue>> getAttributeSourcedValues(Attribute attribute) {
        Optional<List<CIFAssetCharacteristicValue>> attributeSourcedCharacteristicValues = Optional.absent();
        final Optional<List<AttributeValue>> attributeValues = attribute.getAllowedValuesWithCaptions();
        if(attributeValues.isPresent()) {
            attributeSourcedCharacteristicValues = Optional.of(fromAttributeValues(attributeValues.get()));
        }
        return attributeSourcedCharacteristicValues;
    }

    public Optional<List<CIFAssetCharacteristicValue>> getRuleSourcedValues(CIFAsset asset, Attribute attribute) {
        Optional<Set<CIFAssetCharacteristicValue>> ruleSourcedCharacteristicValues = Optional.absent();
        for (RuleAttributeSource ruleAttributeSource : attribute.getAttributeSourceRules()) {
            List<ContextualEvaluatorMap> assetEvaluatorMap = newArrayList(new ContextualEvaluatorMap("",
                    new CIFAssetEvaluator(asset, cifAssetOrchestrator, evaluatorFactory)));

            final CIFAttributeMatchEvaluator contextualEvaluator = new CIFAttributeMatchEvaluator(asset, cifAssetJPARepository, new CIFNewGroupIdCalculator(this));
            assetEvaluatorMap.add(new ContextualEvaluatorMap("Attribute", contextualEvaluator));

            Optional<List<String>> ruleAllowedValues = ruleAttributeSource.execute(assetEvaluatorMap);

            ruleAllowedValues = filterBasedOnMaxResponse(ruleAllowedValues, ruleAttributeSource.getMaxResponses());

            if (ruleSourcedCharacteristicValues.isPresent()) {
                if (ruleAllowedValues.isPresent()) {
                    ruleSourcedCharacteristicValues.get().addAll(asSet(fromStrings(ruleAllowedValues.get())));
                }
            } else {
                if (ruleAllowedValues.isPresent()) {
                    ruleSourcedCharacteristicValues = Optional.of(asSet(fromStrings(ruleAllowedValues.get())));
                }
            }
        }
        if(ruleSourcedCharacteristicValues.isPresent()) {
            List<CIFAssetCharacteristicValue> values = newArrayList(ruleSourcedCharacteristicValues.get());
            return Optional.of(values);
        }

        return Optional.absent();
    }

    private Set<CIFAssetCharacteristicValue> asSet(List<CIFAssetCharacteristicValue> characteristicValues) {
        return newHashSet(characteristicValues);
    }

    private Optional<List<String>> filterBasedOnMaxResponse(Optional<List<String>> ruleAllowedValues, int maxResponses) {
        if(maxResponses == 1 && ruleAllowedValues.isPresent() && !ruleAllowedValues.get().isEmpty()) {
            List<String> singleAllowedValue = newArrayList(ruleAllowedValues.get().get(0));
            return Optional.of(singleAllowedValue);
        }
        return ruleAllowedValues;
    }

    public ProductOffering getProductOffering(String productCode, String stencilCode) {
        Pmr.ProductOfferings offerings = pmr.productOffering(ProductSCode.newInstance(productCode))
            .withStencil(StencilId.latestVersionFor(StencilCode.newInstance(stencilCode)));

        return offerings.get();
    }

    public ProductOffering getProductOffering(String productCode) {
        Pmr.ProductOfferings offerings = pmr.productOffering(ProductSCode.newInstance(productCode));

        return offerings.get();
    }

    public JourneyBehaviourDTO getJourneyBehaviour(ChangeType action, boolean pona, boolean ponc) {
        return pmr.getJourneyBehaviour(action.name(), pona, ponc);
    }

    public List<String> getPackageAndContractProductCodesForCategory(CIFAsset cifAsset) {
        final List<ProductIdentifier> productIdentifiers = pmr.getProductCodesForCategory(cifAsset.getOfferingDetail().getProductGroupName(), newArrayList(CentralService, Package, BundleProduct));

        List<String> productCodes = new ArrayList<String>();
        for (ProductIdentifier productIdentifier : productIdentifiers) {
            productCodes.add(productIdentifier.getProductId());
        }

        return productCodes;
    }

    public List<ProductIdentifier> creatableCandidates(ProductIdentifier ownerIdentifier, RelationshipName relationshipName, Set<ProductIdentifier> linkedIdentifiers, SimpleProductOfferingType ownerProductType) {
            return pmr.filterProductsCreatableBy(ownerIdentifier, relationshipName, newArrayList(linkedIdentifiers), ownerProductType);
    }

    public boolean isRuleFilterSatisfied(CIFAsset cifAsset, String associatedAttribute) {
        ProductOffering productOffering = getProductOffering(cifAsset);
        Attribute attribute = productOffering.getAttribute(new AttributeName(associatedAttribute));
        if(attribute.hasAttributeSourceRule()) {
            CIFAssetEvaluator cifAssetEvaluator = new CIFAssetEvaluator(cifAsset, cifAssetOrchestrator, evaluatorFactory);
            final ContextualEvaluatorMap contextualEvaluatorMap = new ContextualEvaluatorMap("", cifAssetEvaluator);
            List<RuleAttributeSource> attributeSourceRules = attribute.getAttributeSourceRules();
            for (RuleAttributeSource attributeSourceRule : attributeSourceRules) {
                if(attributeSourceRule.isFilterSatisfied(newArrayList(contextualEvaluatorMap)))  {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    public Optional<ProductIdentifier> getProductCategoryCode(String productIdentifier) {
        return pmr.getProductHCode(productIdentifier);
    }

    public String getProductCategoryName(String productCode, ProductCategoryCode productCategoryCode) {
        Optional<ProductCategory> productCategory = pmr.getProductCategory(productCode, productCategoryCode.value());
        if(productCategory.isPresent()) {
            return productCategory.get().getProductIdentifier().getProductName();
        }
        return "";
    }
}
