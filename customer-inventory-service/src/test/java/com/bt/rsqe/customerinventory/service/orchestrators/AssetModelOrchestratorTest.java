package com.bt.rsqe.customerinventory.service.orchestrators;

import com.bt.rsqe.LazyValue;
import com.bt.rsqe.bfgfacade.exception.BfgReadException;
import com.bt.rsqe.customerinventory.bfg.readers.AssetReader;
import com.bt.rsqe.customerinventory.dto.AssetDTO;
import com.bt.rsqe.customerinventory.fixtures.AssetDTOFixture;
import com.bt.rsqe.customerinventory.parameter.ContractId;
import com.bt.rsqe.customerinventory.parameter.CustomerId;
import com.bt.rsqe.customerinventory.parameter.ProductCode;
import com.bt.rsqe.customerinventory.parameter.ProductVersion;
import com.bt.rsqe.customerinventory.parameter.ProjectId;
import com.bt.rsqe.customerinventory.parameter.SiteId;
import com.bt.rsqe.customerinventory.repository.ImmutableAssetException;
import com.bt.rsqe.customerinventory.repository.ProductInstanceRepository;
import com.bt.rsqe.customerinventory.repository.StaleAssetException;
import com.bt.rsqe.customerinventory.repository.jpa.ExternalAssetRepository;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.bom.fixtures.ProductOfferingFixture;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.SimpleProductOfferingType;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.product.parameters.RelationshipName;
import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.pmr.client.PmrMocker;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionResource;
import com.bt.rsqe.projectengine.web.QuoteOptionDTOFixture;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;

import static com.bt.rsqe.matchers.LazyValueMatcher.*;
import static com.google.common.collect.Lists.*;
import static com.thoughtworks.selenium.SeleneseTestBase.assertFalse;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.*;

public class AssetModelOrchestratorTest {
    private AssetModelOrchestrator assetModelOrchestrator;
    private ProductInstanceRepository productInstanceRepository;
    private ExternalAssetRepository externalAssetRepository;
    private AssetReader assetReader;
    private PmrClient pmr;
    private ProjectResource projectResource;

    @Before
    public void setup() {
        productInstanceRepository = mock(ProductInstanceRepository.class);
        externalAssetRepository = mock(ExternalAssetRepository.class);
        assetReader = mock(AssetReader.class);
        pmr = PmrMocker.getMockedInstance();
        projectResource = mock(ProjectResource.class);

        assetModelOrchestrator = new AssetModelOrchestrator(productInstanceRepository,
                                                            externalAssetRepository,
                                                            assetReader,
                                                            pmr,
                                                            projectResource);
    }

    @Test
    public void shouldFetchAssetByKey() throws Exception {
        when(productInstanceRepository.getFutureAsset(new com.bt.rsqe.customerinventory.repository.jpa.keys.AssetKey("A1", 1L)))
            .thenReturn(AssetDTOFixture.anAsset().withId("A1").build());
        assertThat(assetModelOrchestrator.fetchAsset(new AssetKey("A1", 1L)).getId(), is("A1"));
    }

    @Test
    public void shouldFetchAssetsByCustomerContractAndProduct() throws Exception {
        CustomerId customerId = new CustomerId("aCustomerId");
        ContractId contractId = new ContractId("aContractId");
        ProductIdentifier productIdentifier = new ProductIdentifier("aProductCode", "1.0");

        when(productInstanceRepository.getFutureAssets(customerId, contractId, new ProductCode(productIdentifier.getProductId()), new ProductVersion(productIdentifier.getVersionNumber())))
            .thenReturn(newArrayList(AssetDTOFixture.anAsset().withId("A1").build()));

        List<AssetDTO> assets = assetModelOrchestrator.fetchAssets(customerId, contractId, productIdentifier);

        assertThat(assets.size(), is(1));
        assertThat(assets.get(0).getId(), is("A1"));
    }

    @Test
    public void shouldFetchProductOffering() throws Exception {
        final ProductOffering offering = ProductOfferingFixture.aProductOffering().withProductIdentifier("P1").withStencil("S1").build();
        PmrMocker.returnForProduct(pmr, offering);

        assertThat(assetModelOrchestrator.fetchOffering(AssetDTOFixture.anAsset()
                        .withProductCode(new ProductCode("P1"))
                        .withAssetCharacteristic("STENCIL", "S1")
                        .build()),
                   is(offering));
    }

    @Test
    public void shouldFetchBaseProductOffering() throws Exception {
        final ProductOffering offering = ProductOfferingFixture.aProductOffering().withProductIdentifier("P1").build();
        PmrMocker.returnForProduct(pmr, offering);

        assertThat(assetModelOrchestrator.fetchBaseOffering(AssetDTOFixture.anAsset()
                        .withProductCode(new ProductCode("P1"))
                        .build()),
                   is(offering));
    }

    @Test
    public void shouldFetchQuoteOption() throws Exception {
        QuoteOptionResource quoteOptionResource = mock(QuoteOptionResource.class);
        when(quoteOptionResource.get("aQuoteOptionId")).thenReturn(QuoteOptionDTOFixture.aQuoteOptionDTO().withId("aQuoteOptionId").build());
        when(projectResource.quoteOptionResource("aProjectId")).thenReturn(quoteOptionResource);

        assertThat(assetModelOrchestrator.fetchQuoteOption(AssetDTOFixture.anAsset()
                                                                          .withProjectId(new ProjectId("aProjectId"))
                                                                          .withQuoteOptionId("aQuoteOptionId")
                                                                          .build()).getId(),
                   is("aQuoteOptionId"));
    }

    @Test
    public void shouldReturnTrueIfRelationshipIsExternal() throws Exception {
        final ProductOffering offering = ProductOfferingFixture.aProductOffering()
                                                               .withProductIdentifier("P1")
                                                               .withSimpleProductOfferingType(SimpleProductOfferingType.LegacyVPN)
                                                               .build();
        PmrMocker.returnForProduct(pmr, offering);

        assertTrue(assetModelOrchestrator.isExternalRelationship(new ProductIdentifier("P1", "1.0")));
    }

    @Test
    public void shouldReturnFalseIfRelationshipIsNotExternal() throws Exception {
        final ProductOffering offering = ProductOfferingFixture.aProductOffering()
                                                               .withProductIdentifier("P1")
                                                               .withSimpleProductOfferingType(SimpleProductOfferingType.Package)
                                                               .build();
        PmrMocker.returnForProduct(pmr, offering);

        assertFalse(assetModelOrchestrator.isExternalRelationship(new ProductIdentifier("P1", "1.0")));
    }

    @Test
    public void shouldFetchSiteSpecificExternalAssets() throws Exception {
        seedOffering("P1", SimpleProductOfferingType.LegacyVPN);

        AssetDTO owner = AssetDTOFixture.anAsset()
                                        .withCustomerId(new CustomerId("aCustomerId"))
                                        .withContractId(new ContractId("aContractId"))
                                        .withSiteId(new SiteId("12"))
                                        .build();

        when(externalAssetRepository.getAssets(eq(new ProductCode("P1")), eq(new SiteId("12")), argThat(isALazyValue("aQuoteOptionId")), eq(false), org.mockito.Matchers.any(Optional.class)))
                                    .thenReturn(newArrayList(AssetDTOFixture.anAsset().withId("externalAsset").build()));

        List<AssetDTO> assets = assetModelOrchestrator.fetchExternalAssets(owner, new ProductIdentifier("P1", "1.0"), LazyValue.eagerValue("aQuoteOptionId"), true, RelationshipName.newInstance("relationShipName"), false);

        assertThat(assets.size(), is(1));
        assertThat(assets.get(0).getId(), is("externalAsset"));
    }

    @Test
    public void shouldReturnEmptyListForSiteSpecificExternalAssetsWhenNoSiteIdIsPresent() throws Exception {
        seedOffering("P1", SimpleProductOfferingType.LegacyVPN);

        AssetDTO owner = AssetDTOFixture.anAsset()
                                        .withCustomerId(new CustomerId("aCustomerId"))
                                        .withContractId(new ContractId("aContractId"))
                                        .withSiteId(null)
                                        .build();

        List<AssetDTO> assets = assetModelOrchestrator.fetchExternalAssets(owner, new ProductIdentifier("P1", "1.0"), LazyValue.eagerValue("aQuoteOptionId"), true, RelationshipName.newInstance("relationShipName"), false);

        assertThat(assets.size(), is(0));

        verify(externalAssetRepository, never()).getAssets(Mockito.<ProductCode>any(), Mockito.<SiteId>any(), Mockito.<LazyValue>any(), Mockito.anyBoolean(), Mockito.any(Optional.class));
    }

    @Test
    public void shouldFetchNonSiteSpecificExternalAssets() throws Exception {
        seedOffering("P1", SimpleProductOfferingType.LegacyVPN);

        AssetDTO owner = AssetDTOFixture.anAsset()
                                        .withCustomerId(new CustomerId("aCustomerId"))
                                        .withContractId(new ContractId("aContractId"))
                                        .build();

        when(externalAssetRepository.getAssets(eq(new ContractId("aContractId")), eq(new ProductCode("P1")), argThat(isALazyValue("aQuoteOptionId"))))
                                    .thenReturn(newArrayList(AssetDTOFixture.anAsset().withId("externalAsset").build()));

        List<AssetDTO> assets = assetModelOrchestrator.fetchExternalAssets(owner, new ProductIdentifier("P1", "1.0"), LazyValue.eagerValue("aQuoteOptionId"), false, RelationshipName.newInstance("relationShipName"), false);

        assertThat(assets.size(), is(1));
        assertThat(assets.get(0).getId(), is("externalAsset"));
    }

    @Test
    public void shouldFetchExternalAssetsUsingBFGWhenNoOtherExternalAssetsHaveBeenReturned() throws Exception {
        seedOffering("P1", SimpleProductOfferingType.LegacyVPN);

        AssetDTO owner = AssetDTOFixture.anAsset()
                                        .withCustomerId(new CustomerId("aCustomerId"))
                                        .withContractId(new ContractId("aContractId"))
                                        .withAssetCharacteristic("VPN ID", "VPN ID")
                                        .build();

        when(externalAssetRepository.getAssets(eq(new ContractId("aContractId")), eq(new ProductCode("P1")), argThat(isALazyValue("aQuoteOptionId"))))
                                    .thenReturn(Lists.<AssetDTO>newArrayList());

        when(assetReader.readExternalAssets(new CustomerId("aCustomerId"), new ProductCode("P1"), RelationshipName.newInstance("relationShipName"), owner.getCharacteristic("VPN ID").getValue()))
            .thenReturn(newArrayList(AssetDTOFixture.anAsset().withId("externalAssetFromReader").build()));

        List<AssetDTO> assets = assetModelOrchestrator.fetchExternalAssets(owner, new ProductIdentifier("P1", "1.0"), LazyValue.eagerValue("aQuoteOptionId"), false, RelationshipName.newInstance("relationShipName"), false);

        assertThat(assets.size(), is(1));
        assertThat(assets.get(0).getId(), is("externalAssetFromReader"));
    }

    @Test
    public void shouldReturnEmptyListWhenRelationshipIsNotExternalAndNoOtherSiteOrNonSiteExternalAssetsWhereFound() throws Exception {
        seedOffering("P1", SimpleProductOfferingType.Package);

        AssetDTO owner = AssetDTOFixture.anAsset()
                                        .withCustomerId(new CustomerId("aCustomerId"))
                                        .withContractId(new ContractId("aContractId"))
                                        .build();

        when(externalAssetRepository.getAssets(eq(new ContractId("aContractId")), eq(new ProductCode("P1")), argThat(isALazyValue("aQuoteOptionId"))))
                                    .thenReturn(Lists.<AssetDTO>newArrayList());

        List<AssetDTO> assets = assetModelOrchestrator.fetchExternalAssets(owner, new ProductIdentifier("P1", "1.0"), LazyValue.eagerValue("aQuoteOptionId"), false, RelationshipName.newInstance("relationShipName"), false);

        assertThat(assets.size(), is(0));

        verify(assetReader, never()).readExternalAssets(Mockito.<CustomerId>any(), Mockito.<ProductCode>any(), Mockito.<RelationshipName>any(), Mockito.<String>any());
    }

    @Test
    public void shouldReturnEmptyListWhenBFGReadExceptionWasThrownDuringExternalAssetRead() throws Exception {
       seedOffering("P1", SimpleProductOfferingType.LegacyVPN);

        AssetDTO owner = AssetDTOFixture.anAsset()
                                        .withCustomerId(new CustomerId("aCustomerId"))
                                        .withContractId(new ContractId("aContractId"))
                                        .withAssetCharacteristic("VPN ID", "VPN ID")
                                        .build();

        when(externalAssetRepository.getAssets(eq(new ContractId("aContractId")), eq(new ProductCode("P1")), argThat(isALazyValue("aQuoteOptionId"))))
                                    .thenReturn(Lists.<AssetDTO>newArrayList());

        when(assetReader.readExternalAssets(new CustomerId("aCustomerId"), new ProductCode("P1"), RelationshipName.newInstance("relationShipName"), owner.getCharacteristic("VPN ID").getValue()))
            .thenThrow(BfgReadException.class);

        List<AssetDTO> assets = assetModelOrchestrator.fetchExternalAssets(owner, new ProductIdentifier("P1", "1.0"), LazyValue.eagerValue("aQuoteOptionId"), false, RelationshipName.newInstance("relationShipName"), false);

        assertThat(assets.size(), is(0));
    }

    @Test
    public void shouldSaveAnAsset() throws ImmutableAssetException, StaleAssetException {
        AssetDTO assetDTO = AssetDTOFixture.anAsset().build();
        assetModelOrchestrator.put(assetDTO);

        verify(productInstanceRepository, times(1)).saveStubAsset(assetDTO);
    }

    private ProductOffering seedOffering(String productCode, SimpleProductOfferingType simpleProductOfferingType) {
        final ProductOffering offering = ProductOfferingFixture.aProductOffering()
                                                               .withProductIdentifier(productCode)
                                                               .withSimpleProductOfferingType(simpleProductOfferingType)
                                                               .build();
        PmrMocker.returnForProduct(pmr, offering);

        return offering;
    }
}
