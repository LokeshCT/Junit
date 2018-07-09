package com.bt.rsqe.customerinventory.driver;

import com.bt.rsqe.customerinventory.AsIsAssets;
import com.bt.rsqe.customerinventory.SpecialPriceBooks;
import com.bt.rsqe.customerinventory.ToBeAssets;
import com.bt.rsqe.customerinventory.client.AssetChangesBasedAssetResourceClient;
import com.bt.rsqe.customerinventory.client.resource.AssetQuoteDetailResourceClient;
import com.bt.rsqe.customerinventory.client.resource.AssociatedAssetClient;
import com.bt.rsqe.customerinventory.client.resource.UniqueIdResourceClient;
import com.bt.rsqe.customerinventory.client.resource.asis.AssetErrorBasedAssetResourceClient;
import com.bt.rsqe.customerinventory.client.resource.asis.AssetKeyBasedAssetResourceClient;
import com.bt.rsqe.customerinventory.client.resource.asis.CustomerBasedAssetResourceClient;
import com.bt.rsqe.customerinventory.client.resource.asis.SiteBasedAssetResourceClient;
import com.bt.rsqe.customerinventory.client.resource.tobe.CharacteristicBasedFutureAssetResourceClient;
import com.bt.rsqe.customerinventory.client.resource.tobe.OptionBasedFutureAssetResourceClient;
import com.bt.rsqe.customerinventory.client.resource.tobe.PathBasedFutureAssetClient;
import com.bt.rsqe.customerinventory.client.resource.tobe.ProjectSyncAssetResourceClient;
import com.bt.rsqe.customerinventory.client.resource.tobe.SiteBasedFutureAssetByProductResourceClient;
import com.bt.rsqe.customerinventory.client.resource.tobe.SiteBasedFutureAssetResourceClient;
import com.bt.rsqe.customerinventory.dto.AssetCharacteristicDTO;
import com.bt.rsqe.customerinventory.dto.AssetDTO;
import com.bt.rsqe.customerinventory.dto.ExternalIdentifierDTO;
import com.bt.rsqe.customerinventory.dto.FutureAssetPriceReportDTO;
import com.bt.rsqe.customerinventory.dto.FutureAssetPricesDTO;
import com.bt.rsqe.customerinventory.parameter.AssetSourceVersion;
import com.bt.rsqe.customerinventory.parameter.City;
import com.bt.rsqe.customerinventory.parameter.ContractId;
import com.bt.rsqe.customerinventory.parameter.ContractTerm;
import com.bt.rsqe.customerinventory.parameter.CustomerId;
import com.bt.rsqe.customerinventory.parameter.LengthConstrainingProductInstanceId;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerinventory.parameter.ProductCode;
import com.bt.rsqe.customerinventory.parameter.ProductInstanceVersion;
import com.bt.rsqe.customerinventory.parameter.ProductVersion;
import com.bt.rsqe.customerinventory.parameter.ProjectId;
import com.bt.rsqe.customerinventory.parameter.QuoteOptionId;
import com.bt.rsqe.customerinventory.parameter.SiteId;
import com.bt.rsqe.dto.InServiceAssetLookupDTO;
import com.bt.rsqe.utils.countries.Country;
import com.bt.rsqe.web.rest.exception.ResourceNotFoundException;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import javax.annotation.Nullable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.bt.rsqe.utils.AssertObject.*;
import static com.google.common.collect.Lists.*;

public class CustomerInventoryStubDriverManager implements CustomerInventoryDriverManager {
    private ToBeAssets tobeAssets;
    private AsIsAssets asisAssets;
    private SpecialPriceBooks priceBooks;

    public CustomerInventoryStubDriverManager(ToBeAssets tobeAssets,
                                              AsIsAssets asisAssets,
                                              SpecialPriceBooks priceBooks) {
        this.tobeAssets = tobeAssets == null ? new ToBeAssets() : tobeAssets;
        this.asisAssets = asisAssets == null ? new AsIsAssets() : asisAssets;
        this.priceBooks = priceBooks == null ? new SpecialPriceBooks() : priceBooks;
    }

    @Override
    public OptionBasedFutureAssetDriver getOptionBasedFutureAssetDriver(LineItemId lineItemId) {
        return new OptionBasedFutureAssetDriverStub(tobeAssets, lineItemId);
    }

    @Override
    public OptionBasedFutureAssetDriver getSiteAndOptionBasedFutureAssetDriver(SiteId siteId, LineItemId lineItemId) {
        throw new UnsupportedOperationException("Not supported in stub implementation");
    }

    @Override
    public FutureAssetDriver getSiteBasedFutureAssetDriver(SiteId siteId, ProductCode productCode, ProductVersion productVersion) {
        throw new UnsupportedOperationException("Not supported in stub implementation");
    }

    @Override
    public ValidationReportDriver getValidationReportDriver() {
        throw new UnsupportedOperationException("Not supported in stub implementation");
    }

    @Override
    public CrossProductValidationReportDriver getCrossProductValidationReportDriver() {
        throw new UnsupportedOperationException("Not supported in stub implementation");
    }

    @Override
    public OptionBasedLockVersionDriver getOptionBasedLockVersionDriver(LineItemId lineItemId) {
        throw new UnsupportedOperationException("Not supported in stub implementation");
    }

    @Override
    public SpecialPriceBookDriver getSpecialPriceBookDriver(QuoteOptionId quoteOptionId, Country country) {
        return new SpecialPriceBookDriverStub(priceBooks, quoteOptionId, country);
    }

    @Override
    public SpecialPriceBookCountryDriver getSpecialPriceBookCountryDriver(QuoteOptionId quoteOptionId) {
        return new SpecialPriceBookCountryDriverStub(priceBooks, quoteOptionId);
    }

    @Override
    public FutureAssetPriceReportDriver getFutureAssetPriceReportDriver() {
        return new FutureAssetPriceReportDriver(URI.create("")) {
            @Override
            public FutureAssetPriceReportDTO post(final List<LineItemId> lineItemIds) {
                final Iterable<AssetDTO> assets = Iterables.filter(tobeAssets.values(), new Predicate<AssetDTO>() {
                    @Override
                    public boolean apply(@Nullable AssetDTO input) {
                        return lineItemIds.contains(new LineItemId(input.getLineItemId()));
                    }
                });
                final Iterable<FutureAssetPricesDTO> futureAssetPrices = Iterables.transform(assets, new Function<AssetDTO, FutureAssetPricesDTO>() {
                    @Override
                    public FutureAssetPricesDTO apply(@Nullable AssetDTO input) {
                        return new FutureAssetPricesDTO(input);
                    }
                });
                return new FutureAssetPriceReportDTO(newArrayList(futureAssetPrices));
            }
        };
    }

    @Override
    public FutureAssetPriceDriver getFutureAssetPriceDriver(LineItemId lineItemId, final LengthConstrainingProductInstanceId assetId) {
        return new FutureAssetPriceDriver(URI.create("")) {
            @Override
            public void put(final FutureAssetPricesDTO prices) {
                final Map.Entry<ToBeAssets.Key, AssetDTO> assetEntry = Iterables.find(tobeAssets.entrySet(), new Predicate<Map.Entry<ToBeAssets.Key, AssetDTO>>() {
                    @Override
                    public boolean apply(@Nullable Map.Entry<ToBeAssets.Key, AssetDTO> input) {
                        return input.getValue().getId().equals(prices.getId());
                    }
                });
                final AssetDTO currentAsset = assetEntry.getValue();
                final AssetDTO newAsset = new AssetDTO(
                    new LengthConstrainingProductInstanceId(currentAsset.getId()),
                    new ProductCode(currentAsset.getProductCode()),
                    new ProductVersion(currentAsset.getProductVersion()),
                    currentAsset.getCharacteristics(),
                    new ArrayList<AssetCharacteristicDTO>(),
                    prices.getPriceLines(),
                    currentAsset.getState(),
                    prices.getPricingStatus(),
                    new SiteId(currentAsset.getSiteId()),
                    new LineItemId(currentAsset.getLineItemId()),
                    currentAsset.getRelationships(),
                    currentAsset.getProjectedUsages(),
                    currentAsset.getPricingCaveats(),
                    currentAsset.getFutureAssetErrors(),
                    new ContractTerm(currentAsset.getContractTerm()),
                    new CustomerId(currentAsset.getCustomerId()),
                    new ContractId(currentAsset.getContractId()),
                    new QuoteOptionId(currentAsset.getQuoteOptionId()), new ProductInstanceVersion(currentAsset.getVersion()), currentAsset.getAssetType(),
                    new ProjectId(currentAsset.getProjectId()),
                    Sets.<ExternalIdentifierDTO>newHashSet(), new AssetSourceVersion(currentAsset.getAssetSourceVersion()), currentAsset.getAssetVersionStatus(), new City(currentAsset.getAlternateCity()),
                    currentAsset.getAssetProcessType(), currentAsset.getAssetSubProcessType(),
                    new LengthConstrainingProductInstanceId(currentAsset.getMovesTo()), currentAsset.getSlaId(), currentAsset.getMagId(), currentAsset.getSsvId(), currentAsset.getCustomerRequiredDate(),currentAsset.getSubLocationId(),currentAsset.getSubLocationName(),currentAsset.getRoom(),currentAsset.getFloor());
                tobeAssets.put(assetEntry.getKey(), newAsset);
            }

            @Override
            public FutureAssetPricesDTO get() {
                throw new UnsupportedOperationException("Not supported in stub implementation");
            }
        };
    }

    @Override
    public OptionBasedFutureAssetResourceClient getOptionBasedFutureAssetResourceClient() {
        try {
            return new OptionBasedFutureAssetResourceClient(new URI("http://localhost"), "") {
                @Override
                public AssetDTO getByOption(String optionId) {
                    return tobeAssets.get(new ToBeAssets.Key(optionId));
                }

                @Override
                public AssetDTO getByOptionAndAssetId(String optionId, String assetId, Long assetVersion) {
                    throw new UnsupportedOperationException("Not supported in stub implementation");
                }

                @Override
                public AssetDTO getByOptionAndAssetIdAsis(String optionId, String assetId) {
                    throw new UnsupportedOperationException("Not supported in stub implementation");
                }

                @Override
                public AssetDTO put(String optionId, String assetId, AssetDTO dto) {
                    tobeAssets.put(new ToBeAssets.Key(dto), dto);
                    return dto;
                }
            };
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Couldn't create a stub OptionBasedFutureAssetResourceClient", e);
        }
    }

    @Override
    public AssetQuoteDetailResourceClient getAssetQuoteDetailResourceClient() {
        throw new UnsupportedOperationException("Not supported in stub implementation");
    }

    @Override
    public FutureAssetPriceDriver getSiteAndOptionBasedFutureAssetPriceDriver(SiteId siteId, LineItemId lineItemId, LengthConstrainingProductInstanceId productInstanceId) {
        throw new UnsupportedOperationException("Not supported in stub implementation");
    }

    @Override
    public OptionBasedValidationReportDriver getProductValidationReportDriver(LineItemId lineItemId) {
        throw new UnsupportedOperationException("Not supported in stub implementation");
    }

    @Override
    public OptionBasedCrossProductValidationReportDriver getCrossProductValidationReportDriver(LineItemId lineItemId) {
        throw new UnsupportedOperationException("Not supported in stub implementation");
    }

    @Override
    public OptionCloneBatchDriver getOptionCloneDriver() {
        throw new UnsupportedOperationException("Not supported in stub implementation");
    }

    @Override
    public OptionCopyBatchDriver getOptionCopyDriver() {
        throw new UnsupportedOperationException("Not supported in stub implementation");
    }

    @Override
    public ProductConfigurationUpdateBatchDriver getProductConfigurationUpdateBatchDriver() {
        throw new UnsupportedOperationException("Not supported in stub implementation");
    }

    @Override
    public FutureAssetDriver getContractBasedFutureAssetDriver(CustomerId customerId, ContractId contractId, ProductCode productCode, ProductVersion productVersion) {
        throw new UnsupportedOperationException("Not supported in stub implementation");
    }

    @Override
    public SiteBasedAssetResourceClient getSiteBasedAssetResourceClient() {
        throw new UnsupportedOperationException("Not supported in stub implementation");
    }

    @Override
    public CustomerBasedAssetResourceClient getCustomerBasedAssetResourceClient() {
        throw new UnsupportedOperationException("Not supported in stub implementation");
    }

    @Override
    public AssetErrorBasedAssetResourceClient getAssetErrorBasedAssetResourceClient() {
        throw new UnsupportedOperationException("Not supported in stub implementation");
    }

    @Override
    public AssetKeyBasedAssetResourceClient getAssetKeyBasedAssetResourceClient() {
        try {
            return new AssetKeyBasedAssetResourceClient(new URI("http://localhost"), "") {
                @Override
                public AssetDTO getByAssetKey(String assetId, Long assetVersion, boolean isIVPN) {
                    if (isNotNull(tobeAssets) && !tobeAssets.entrySet().isEmpty()) {
                        return tobeAssets.entrySet().iterator().next().getValue();
                    }
                    throw new ResourceNotFoundException();
                }

                @Override
                public AssetDTO getAsisAsset(String assetId) {
                    final AssetDTO assetDTO = asisAssets.get(new LengthConstrainingProductInstanceId(assetId));
                    if (isNull(assetDTO)) {
                        throw new ResourceNotFoundException();
                    }
                    return assetDTO;
                }

                @Override
                public InServiceAssetLookupDTO hasInServiceAsset(String assetId) { return null;}
            };
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Couldn't create a stub OptionBasedFutureAssetResourceClient", e);
        }
    }

    @Override
    public SiteBasedFutureAssetResourceClient getSiteBasedFutureAssetResourceClient() {
        throw new UnsupportedOperationException("Not supported in stub implementation");
    }

    @Override
    public CharacteristicBasedFutureAssetResourceClient getCharacteristicBasedFutureAssetResourceClient() {
        throw new UnsupportedOperationException("Not supported in stub implementation");
    }

    @Override
    public AssetDriver getAssetDriver() {
        throw new UnsupportedOperationException("Not supported in stub implementation");
    }

    @Override
    public AssociationBasedFutureAssetDriver getAssociationBasedFutureAssetDriver() {
        throw new UnsupportedOperationException("Not supported in stub implementation");
    }

    @Override
    public UniqueIdResourceClient getUniqueIdResourceClient() {
        throw new UnsupportedOperationException("Not supported in stub implementation");
    }

    @Override
    public SiteBasedFutureAssetByProductResourceClient getSiteBasedFutureAssetByProductResourceClient() {
        throw new UnsupportedOperationException("Not supported in stub implementation");
    }

    @Override
    public CustomerProductCategoryContactsDriver getCustomerProductCategoryContactsDriver(CustomerId customerId, ProductCode productHCode, String contactType) {
        throw new UnsupportedOperationException("Not supported in stub implementation");
    }

    @Override
    public SiteDriver getSiteDriver() {
        throw new UnsupportedOperationException("Not supported in stub implementation");
    }

    @Override
    public InServiceAssetCloneBatchDriver getInServiceAssetCloneBatchDriver() {
        throw new UnsupportedOperationException("Not supported in stub implementation");
    }

    @Override
    public PathBasedFutureAssetClient getPathBasedFutureAssetClient() {
        throw new UnsupportedOperationException("Not supported in stub implementation");
    }

    @Override
    public AssetChangesBasedAssetResourceClient getAssetChangesBasedAssetResourceClient() {
        throw new UnsupportedOperationException("Not supported in stub implementation");
    }

    @Override
    public AssociatedAssetClient getAssociatedAssetClient() {
        throw new UnsupportedOperationException("Not supported in stub implementation");
    }

    @Override
    public ProductDriver getProductDriver() {
        throw new UnsupportedOperationException("Not supported in stub implementation");
    }

    @Override
    public ProjectSyncAssetResourceClient getProjectSyncAssetResourceClient() {
        throw new UnsupportedOperationException("Not supported in stub implementation");
    }

    @Override
    public CreateIvpnAssetDriver getCreateIvpnAssetDriver() {
        throw new UnsupportedOperationException("Not supported in stub implementation");
    }
}
