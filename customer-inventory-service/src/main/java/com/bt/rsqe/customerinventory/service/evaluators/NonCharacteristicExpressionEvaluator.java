package com.bt.rsqe.customerinventory.service.evaluators;

import com.bt.rsqe.client.ExpedioClient;
import com.bt.rsqe.client.Pmr;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristic;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetRelationship;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.repository.CIFAssetJPARepository;
import com.bt.rsqe.customerrecord.CustomerDTO;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.projectengine.OrderResource;
import com.bt.rsqe.projectengine.ProjectResource;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.List;

import static com.bt.rsqe.utils.AssertObject.isNotNull;
import static org.apache.commons.collections.CollectionUtils.*;

public class NonCharacteristicExpressionEvaluator extends CIFAssetCharacteristicEvaluator {
    private Pmr pmrClient;
    private ExpedioClient expedioClient;
    private ProjectResource projectResource;
    private final NonCharacteristicEvaluableExpressions evaluableExpression;
    private CIFAssetJPARepository cIFAssetJPARepository;

    public NonCharacteristicExpressionEvaluator(String characteristicName, Pmr pmrClient, ExpedioClient expedioClient, ProjectResource projectResource, CIFAssetJPARepository cIFAssetJPARepository) {
        super(characteristicName);
        this.cIFAssetJPARepository = cIFAssetJPARepository;
        evaluableExpression = NonCharacteristicEvaluableExpressions.valueOf(getCharacteristicName());
        this.pmrClient = pmrClient;
        this.expedioClient = expedioClient;
        this.projectResource = projectResource;
    }

    @Override
    public List<CIFAssetExtension> getCIFAssetExtensions() {
        return evaluableExpression.getRequiredCIFAssetExtensions();
    }

    @Override
    public Object evaluate(CIFAsset cifAsset) {
        switch(evaluableExpression){
            case AssetUniqueId:
                return AssetUniqueId.getAssetUniqueId(cifAsset);
            case AssetId:
                return cifAsset.getAssetKey().getAssetId();
            case ProductCategoryCode:
                return getProductIdentifierForHCode(cifAsset).getProductId();
            case ProductCategoryName:
                return getProductIdentifierForHCode(cifAsset).getProductName();
            case ProductCode:
                return cifAsset.getProductCode();
            case SalesChannel:
                return getCustomerDTO(cifAsset).getSalesChannel();
            case SalesChannelType:
                return getCustomerDTO(cifAsset).getSalesChannelType();
            case CustomerName:
                return getCustomerDTO(cifAsset).getName();
            case CountryISOCode:
                return getSite(cifAsset).getCountryISOCode();
            case CountryName:
                return getSite(cifAsset).getCountryName();
            case CityName:
                return getSite(cifAsset).getCity();
            case SiteName:
                return getSite(cifAsset).getSiteName();
            case AssetSiteId:
                return cifAsset.getSiteId();
            case FixedAction:
                return FixedAction.fromAssetStatus(cifAsset.getStatus());
            case Action:
                return ChangeTypeEvaluator.fromSourceAndState(cifAsset.getAsIsAsset()!=null, cifAsset.getStatus()).getValue();
            case SubscriberId:
                return SubscriberId.fromAssetUniqueId(AssetUniqueId.getAssetUniqueId(cifAsset));
            case BillingId:
                OrderResource orderResource = projectResource.quoteOptionResource(cifAsset.getProjectId()).quoteOptionOrderResource(cifAsset.getQuoteOptionId());
                return BillingId.fromOrderDTOs(cifAsset.getLineItemId(), orderResource.getAll());
            case LegacyIdentifier:
                return cifAsset.getOfferingDetail().getLegacyIdentifier();
            case MoveType:
                return MoveType.fromAssetSubProcessType(cifAsset.getAssetSubProcessType()).value();
            case Proposition:
                return cifAsset.getOfferingDetail().getProposition();
            case ProjectId:
                return cifAsset.getProjectId();
            case CustomerId:
                return cifAsset.getCustomerId();
            case InstallationHours:
                return InstallationHours.fromAssetProcessType(cifAsset.getAssetProcessType()).getUserFriendlyText();
            case ProductName:
                return cifAsset.getOfferingDetail().getProductName();
            case AssetType:
                return cifAsset.getAssetType().type();
            case FeatureSpecProviderCode:
                return getProductIdentifierForHCode(cifAsset).getProductId();
            case MyRelationshipWithParent:
                return getMyRelationshipNameInParent(cifAsset);
            case ContractResignFlag:
                return cifAsset.getContractResignStatus();
            case ContextAction:
                return cifAsset.getAction().getValue();
            case DeviceRoleAttr:
                final CIFAssetCharacteristic characteristic = cifAsset.getCharacteristic("DEVICE ROLE");
                if (isNotNull(characteristic)) {
                    return characteristic.getValue();
                }
                return "";
        }
        // Currently unreachable because there are cases for all the NonCharacteristicEvaluableExpressions enums but Java still requires a return.
        ///CLOVER:OFF
        return null;
        ///CLOVER:ON
    }

    private Object getMyRelationshipNameInParent(CIFAsset cifAsset) {
        final List<CIFAsset> ownerAssets = cIFAssetJPARepository.getOwnerAssets(cifAsset.getAssetKey(), true);
        if (!isEmpty(ownerAssets)) {
            final CIFAsset parent = ownerAssets.get(0);
            final Optional<CIFAssetRelationship> relationshipInParent = getRelationshipInParent(cifAsset.getAssetKey().getAssetId(), parent.getRelationships());
            if (relationshipInParent.isPresent()) {
               return relationshipInParent.get().getRelationshipName();
            }
        }

        return "";
    }

    private Optional<CIFAssetRelationship> getRelationshipInParent(final String assetId, List<CIFAssetRelationship> relationships) {
        final Optional<CIFAssetRelationship> cifAssetRelationshipOptional = Iterables.tryFind(relationships, new Predicate<CIFAssetRelationship>() {
            @Override
            public boolean apply(CIFAssetRelationship input) {
                return input.getRelated().getAssetKey().getAssetId().equals(assetId);
            }
        });

        return cifAssetRelationshipOptional;
    }

    private CustomerDTO getCustomerDTO(CIFAsset cifAsset) {
        return expedioClient.getCustomerResource().get(cifAsset.getCustomerId(), cifAsset.getContractId());
    }

    private ProductIdentifier getProductIdentifierForHCode(CIFAsset cifAsset) {
        return pmrClient.getProductHCode(cifAsset.getProductCode()).get();
    }

    private SiteDTO getSite(CIFAsset cifAsset) {
        return expedioClient.getCustomerResource().siteResource(cifAsset.getCustomerId())
                                                 .get(cifAsset.getSiteId(), cifAsset.getProjectId());
    }

    public boolean handlesNonCharacteristicExpression(String expression) {
        return NonCharacteristicEvaluableExpressions.containsExpression(expression);
    }
}
