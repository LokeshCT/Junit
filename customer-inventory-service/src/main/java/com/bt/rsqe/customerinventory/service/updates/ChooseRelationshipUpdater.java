package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.customerinventory.dto.AssetDTO;
import com.bt.rsqe.customerinventory.dto.ExternalIdentifierDTO;
import com.bt.rsqe.customerinventory.parameter.LengthConstrainingProductInstanceId;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetKey;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetRelationship;
import com.bt.rsqe.customerinventory.service.client.domain.updates.ChooseRelationshipRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.ChooseRelationshipResponse;
import com.bt.rsqe.customerinventory.service.client.domain.updates.UpdateRequestSource;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.externals.PmrHelper;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;
import com.bt.rsqe.customerinventory.service.providers.AssetCandidateProvider;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.parameters.RelationshipName;
import com.bt.rsqe.enums.AssetType;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.List;

import static com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension.*;
import static com.bt.rsqe.domain.product.parameters.RelationshipName.*;
import static com.bt.rsqe.enums.AssetVersionStatus.IN_SERVICE;
import static com.bt.rsqe.utils.AssertObject.isEmpty;
import static com.bt.rsqe.utils.AssertObject.*;
import static com.google.common.collect.Lists.*;
import static org.apache.commons.lang.StringUtils.*;

public class ChooseRelationshipUpdater implements CIFAssetUpdater<ChooseRelationshipRequest, ChooseRelationshipResponse> {
    private static final List<CIFAssetExtension> EXTENSIONS = newArrayList(ProductOfferingRelationshipDetail, QuoteOptionItemDetail);
    private final CIFAssetOrchestrator assetOrchestrator;
    private final DependentUpdateBuilderFactory dependentUpdateBuilderFactory;
    private AssetCandidateProvider assetCandidateProvider;
    private PmrHelper pmrHelper;

    public ChooseRelationshipUpdater(CIFAssetOrchestrator assetOrchestrator, DependentUpdateBuilderFactory dependentUpdateBuilderFactory, AssetCandidateProvider assetCandidateProvider, PmrHelper pmrHelper) {
        this.assetOrchestrator = assetOrchestrator;
        this.dependentUpdateBuilderFactory = dependentUpdateBuilderFactory;
        this.assetCandidateProvider = assetCandidateProvider;
        this.pmrHelper = pmrHelper;
    }

    @Override
    public ChooseRelationshipResponse performUpdate(ChooseRelationshipRequest update) {

        DependantUpdatesBuilder dependantUpdatesBuilder = dependentUpdateBuilderFactory.getDependentUpdateBuilderFactory();
        final CIFAsset parentAsset = assetOrchestrator.getAsset(new CIFAssetKey(update.getAssetKey(), EXTENSIONS));
        final CIFAsset relatedAsset = createOrFindRelatedAsset(update);

        CIFAssetRelationship createdChooseRelationship = assetOrchestrator.relateAssets(parentAsset, relatedAsset, update.getRelationshipName());
        assetOrchestrator.saveAssetAndClearCaches(parentAsset);

        addDependantRequests(relatedAsset, update, dependantUpdatesBuilder);

        if (!relatedAsset.isStub()) {
            dependantUpdatesBuilder.withOptional(dependentUpdateBuilderFactory.getInvalidatePriceRequestBuilder().invalidatePriceForRelationshipChange(parentAsset));

            if(IN_SERVICE != relatedAsset.getAssetVersionStatus()) {   //No need to revise the pricing status of  Inservice assets.
                dependantUpdatesBuilder.withOptional(dependentUpdateBuilderFactory.getInvalidatePriceRequestBuilder().invalidatePriceForRelationshipChange(relatedAsset));
            }
        }

        return new ChooseRelationshipResponse(update, createdChooseRelationship, dependantUpdatesBuilder.dependantRequests());
    }

    private CIFAsset createOrFindRelatedAsset(final ChooseRelationshipRequest update) {
        if (UpdateRequestSource.Client != update.getRequestSource()) {
            return assetOrchestrator.getAsset(new CIFAssetKey(update.getRelatedAssetKey(), EXTENSIONS));
        } else {
            Optional<AssetDTO> matchingCandidate = matchingChoosableCandidate(update);

            if (matchingCandidate.isPresent()) {
                AssetDTO assetDTO = matchingCandidate.get();
                Optional<CIFAsset> cifAssetOptional = getAsset(assetDTO);

                if (AssetType.STUB == assetDTO.getAssetType()) {
                    if (cifAssetOptional.isPresent()) {
                        return cifAssetOptional.get();
                    }
                    enrichAssetDetails(assetDTO);
                    assetCandidateProvider.putAsset(assetDTO);
                    return assetOrchestrator.getAsset(new CIFAssetKey(new AssetKey(assetDTO.getId(), assetDTO.getVersion()), EXTENSIONS));
                }

                if (!cifAssetOptional.isPresent()) {
                    throw new RuntimeException(String.format("Unable to find asset from CIF repository for asset Id - %s, Version - %s", assetDTO.getId(), assetDTO.getVersion()));
                }

                return cifAssetOptional.get();
            }

            throw new RuntimeException(String.format("Unable to find choosable candidate [during choose relation] for the request - %s", update));
        }
    }

    private void enrichAssetDetails(AssetDTO assetDTO) {
        if (isEmpty(assetDTO.getId())) {
            assetDTO.setId(new LengthConstrainingProductInstanceId().toString());
        }

        if (isEmpty(assetDTO.getProductVersion())) {
            ProductOffering productOffering = pmrHelper.getProductOffering(assetDTO.getProductCode());
            assetDTO.detail().setProductVersion(productOffering.getProductIdentifier().getVersionNumber());
        }
    }

    private void addDependantRequests(CIFAsset cifAsset, ChooseRelationshipRequest update, DependantUpdatesBuilder dependantUpdatesBuilder) {
        dependantUpdatesBuilder.withSet(dependentUpdateBuilderFactory.getContributesToChangeRequestBuilder().buildRequests(cifAsset.getAssetKey(),
                cifAsset.getProductCode(), RelationshipName.newInstance(update.getRelationshipName()), 1));
    }

    private Optional<AssetDTO> matchingChoosableCandidate(final ChooseRelationshipRequest update) {
        List<AssetDTO> candidates = assetCandidateProvider.getChoosableCandidates(update.getAssetKey(), newInstance(update.getRelationshipName()));

        return Iterables.tryFind(candidates, new Predicate<AssetDTO>() {
            @Override
            public boolean apply(AssetDTO input) {
                return getCandidateId(input).equals(update.getCandidateId());
            }
        });
    }

    private Optional<CIFAsset> getAsset(AssetDTO assetDTO) {
        try {
            if (isNotEmpty(assetDTO.getId())) {
                CIFAsset asset = assetOrchestrator.getAsset(new CIFAssetKey(new AssetKey(assetDTO.getId(), assetDTO.getVersion()), EXTENSIONS));
                return isNotNull(asset) ? Optional.of(asset) : Optional.<CIFAsset>absent();
            }
        } catch (Exception e) { /*Carry On*/ }
        return Optional.absent();
    }

    private String getCandidateId(AssetDTO input) {
        if (AssetType.STUB != input.getAssetType()) {
            return input.getId();
        } else {
            Optional<ExternalIdentifierDTO> externalIdentifier = input.getExternalIdentifier();
            if (externalIdentifier.isPresent()) {
                return externalIdentifier.get().getValue();
            }
            throw new RuntimeException(String.format("External Identifier is not found (or) has more than one in asset id - %s, version - %s",
                    input.getId(),
                    input.getVersion()));
        }
    }
}
