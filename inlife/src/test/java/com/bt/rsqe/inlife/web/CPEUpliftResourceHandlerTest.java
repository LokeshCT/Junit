package com.bt.rsqe.inlife.web;

import com.bt.rsqe.customerinventory.builder.AssetDTOBuilder;
import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.dto.AssetDTO;
import com.bt.rsqe.customerinventory.dto.FutureAssetRelationshipDTO;
import com.bt.rsqe.customerinventory.dto.PriceLineDTO;
import com.bt.rsqe.customerinventory.fixtures.PriceLineDTOFixture;
import com.bt.rsqe.customerinventory.parameter.LengthConstrainingProductInstanceId;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerinventory.parameter.PpsrId;
import com.bt.rsqe.customerinventory.parameter.ProductInstanceVersion;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.ContractDTO;
import com.bt.rsqe.domain.PriceBookDTO;
import com.bt.rsqe.domain.product.DefaultProductInstanceFixture;
import com.bt.rsqe.domain.product.PriceType;
import com.bt.rsqe.domain.product.ProductSCode;
import com.bt.rsqe.domain.product.parameters.ProductCategoryCode;
import com.bt.rsqe.domain.product.parameters.RelationshipName;
import com.bt.rsqe.domain.product.parameters.RelationshipType;
import com.bt.rsqe.domain.project.PriceLine;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.enums.AssetVersionStatus;
import com.bt.rsqe.enums.ProductCodes;
import com.bt.rsqe.integration.PriceLineFixture;
import com.bt.rsqe.pricing.AutoPriceAggregator;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemResource;
import com.bt.rsqe.projectengine.QuoteOptionResource;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.google.common.collect.Lists.*;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CPEUpliftResourceHandlerTest {
    @Test
    public void shouldUpliftCPEDataAndAutoAggregateICGPrices() {
        AutoPriceAggregator autoPriceAggregator = mock(AutoPriceAggregator.class);
        ProductInstanceClient productInstanceClient = mock(ProductInstanceClient.class);
        ProjectResource projectResource = mock(ProjectResource.class);

        //Given

        PriceLineDTO cpeCease = new PriceLineDTOFixture().withId("cpeCease").withPpsrId(new PpsrId(1L)).withChargingSchemeName("Total CPE Cease").build();
        PriceLineDTO cpePrice = new PriceLineDTOFixture().withId("cpePrice").withPpsrId(new PpsrId(2L)).withChargingSchemeName("Total CPE Price").build();
        PriceLineDTO cpeCancellation = new PriceLineDTOFixture().withId("cpeCancellation").withPpsrId(new PpsrId(3L)).withChargingSchemeName("Total CPE Cancellation Charge").build();

        AssetDTO cpe = new AssetDTOBuilder().withProductInstanceId(new LengthConstrainingProductInstanceId("cpe")).withProductInstanceVersion(new ProductInstanceVersion(1L)).withPriceLines(newArrayList(cpeCease, cpePrice, cpeCancellation)).build();
        AssetDTO secondaryCpe = new AssetDTOBuilder().withProductInstanceId(new LengthConstrainingProductInstanceId("secondaryCpe")).withProductInstanceVersion(new ProductInstanceVersion(1L)).withPriceLines(newArrayList(cpeCease, cpePrice, cpeCancellation)).build();

        FutureAssetRelationshipDTO relationshipDTO1 = new FutureAssetRelationshipDTO(RelationshipName.newInstance("CPE"), RelationshipType.RelatedTo, cpe);
        FutureAssetRelationshipDTO relationshipDTO2 = new FutureAssetRelationshipDTO(RelationshipName.newInstance("SecondaryCPE"), RelationshipType.RelatedTo, secondaryCpe);

        AssetDTO accessReqAsset = new AssetDTOBuilder().withFutureAssetRelationshipDTOs(newArrayList(relationshipDTO1, relationshipDTO2)).build();
        FutureAssetRelationshipDTO accessReqAssetRelation = new FutureAssetRelationshipDTO(RelationshipName.newInstance("AccessRequirements"), RelationshipType.Child, accessReqAsset);

        AssetDTO icg = new AssetDTOBuilder().withProductInstanceId(new LengthConstrainingProductInstanceId("anAssetId")).withProductInstanceVersion(new ProductInstanceVersion(1L))
                                            .withLineItemId(new LineItemId(accessReqAsset.getLineItemId())).withFutureAssetRelationshipDTOs(newArrayList(accessReqAssetRelation)).build();

        when(productInstanceClient.getAssetKeysByProduct(org.mockito.Matchers.any(ProductSCode.class), org.mockito.Matchers.any(Date.class), eq(""))).thenReturn(newArrayList(AssetKey.newInstance("anAssetId", 1l)));
        when(productInstanceClient.getAssetDtoByAssetKey(new LengthConstrainingProductInstanceId("anAssetId"), new ProductInstanceVersion(1L))).thenReturn(icg);

        when(productInstanceClient.getAssetDtoByAssetKey(new LengthConstrainingProductInstanceId(cpe.getId()), new ProductInstanceVersion(cpe.getVersion()))).thenReturn(cpe);
        when(productInstanceClient.getAssetDtoByAssetKey(new LengthConstrainingProductInstanceId(secondaryCpe.getId()), new ProductInstanceVersion(secondaryCpe.getVersion()))).thenReturn(secondaryCpe);


        ProductInstance productInstance = new DefaultProductInstanceFixture("icg").withProductInstanceId("anAssetId").withLineItemId("icgLineItemId").build();
        when(productInstanceClient.getByAssetKey(AssetKey.newInstance("anAssetId", 1L))).thenReturn(productInstance);

        PriceBookDTO priceBookDTO = new PriceBookDTO("someId", "someRequestId", "someEupPriceBook", "somePTPPriceBook", null, null);
        ContractDTO contractDTO = new ContractDTO(UUID.randomUUID().toString(), "", asList(priceBookDTO));
        QuoteOptionItemDTO optionItemDTO = new QuoteOptionItemDTO(productInstance.getLineItemId(), "", "", null, "", "", "", null, null, null, null, null, null,
                                                                  null, "", null, contractDTO, false, false, false, null, null, true, new ProductCategoryCode("H123"), null, false);
        QuoteOptionResource quoteOptionResource = mock(QuoteOptionResource.class);
        QuoteOptionItemResource quoteOptionItemResource = mock(QuoteOptionItemResource.class);
        when(projectResource.quoteOptionResource(productInstance.getProjectId())).thenReturn(quoteOptionResource);
        when(quoteOptionResource.quoteOptionItemResource(productInstance.getQuoteOptionId())).thenReturn(quoteOptionItemResource);
        when(quoteOptionItemResource.get(productInstance.getLineItemId())).thenReturn(optionItemDTO);

        CPEUpliftResourceHandler cpeUpliftResourceHandler = new CPEUpliftResourceHandler(productInstanceClient, autoPriceAggregator, projectResource);
        Response response = cpeUpliftResourceHandler.uplift(ProductCodes.InternetConnectTopology.productCode(), "1", "", "");

        ArgumentCaptor<AssetDTO> assetDTOArgumentCaptor = ArgumentCaptor.forClass(AssetDTO.class);
        verify(productInstanceClient, times(2)).putAsset(assetDTOArgumentCaptor.capture());

        List<AssetDTO> allValues = assetDTOArgumentCaptor.getAllValues();
        for (AssetDTO assetDTO : allValues) {
            assertThat(assetDTO.getPriceLines().size(), is(1));
            assertThat(assetDTO.getPriceLines().get(0).getChargingSchemeName(), is("CPE Cease"));
            assertThat(assetDTO.getPriceLines().get(0).getPriceLineName(), is("CPE Cease"));
            assertThat(assetDTO.getPriceLines().get(0).getDescription(), is("CPE Cease"));

        }

        verify(autoPriceAggregator, times(1)).aggregatePricesOf(priceBookDTO, new LineItemId(productInstance.getLineItemId()));
        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
    }

    @Test
    public void shouldUpliftPriceLineId() {
        //Given
        AutoPriceAggregator autoPriceAggregator = mock(AutoPriceAggregator.class);
        ProductInstanceClient productInstanceClient = mock(ProductInstanceClient.class);
        ProjectResource projectResource = mock(ProjectResource.class);

        PriceLine initialAssetCpeCease = new PriceLineFixture().withId("initialCpeCease").withPpsrId(1L).withChargingSchemeName("Total CPE Cease").withPriceType(PriceType.ONE_TIME).build();
        PriceLine initialAssetCpe = new PriceLineFixture().withId("initialCpePrice").withPpsrId(2L).withChargingSchemeName("Total ICg CPE Price").withPriceType(PriceType.ONE_TIME).build();
        PriceLine initialAssetCpeCancellation = new PriceLineFixture().withId("initialCpeCancellation").withPpsrId(3L).withChargingSchemeName("Total ICg CPE Cancellation Charge").withPriceType(PriceType.ONE_TIME).build();

        ProductInstance initialProductInstance = new DefaultProductInstanceFixture("icg").withProductInstanceId("anAssetId").withLineItemId("initialIcgLineItemId") .withProductInstanceVersion(1L)
                                                                                  .withPriceLines(newArrayList(initialAssetCpe, initialAssetCpeCease, initialAssetCpeCancellation)).build();

        PriceLine assetCpeCease = new PriceLineFixture().withId("cpeCease").withPpsrId(1L).withChargingSchemeName("Total CPE Cease").withPriceType(PriceType.ONE_TIME).build();
        PriceLine assetCpe = new PriceLineFixture().withId("cpePrice").withPpsrId(2L).withChargingSchemeName("Total ICg CPE Price").withPriceType(PriceType.ONE_TIME).build();
        PriceLine assetCpeCancellation = new PriceLineFixture().withId("cpeCancellation").withPpsrId(3L).withChargingSchemeName("Total ICg CPE Cancellation Charge").withPriceType(PriceType.ONE_TIME).build();

        ProductInstance productInstance = new DefaultProductInstanceFixture("icg").withProductInstanceId("anAssetId").withLineItemId("icgLineItemId") .withProductInstanceVersion(2L)
                                                                                  .withPriceLines(newArrayList(assetCpe, assetCpeCease, assetCpeCancellation)).build();

        when(productInstanceClient.getAssets(new LengthConstrainingProductInstanceId("anAssetId"))).thenReturn(newArrayList(initialProductInstance, productInstance));

        //When
        CPEUpliftResourceHandler cpeUpliftResourceHandler = new CPEUpliftResourceHandler(productInstanceClient, autoPriceAggregator, projectResource);
        cpeUpliftResourceHandler.upliftPriceLineID("anAssetId");

        //The
        ArgumentCaptor<ProductInstance>  productInstanceArgumentCaptor = ArgumentCaptor.forClass(ProductInstance.class);

        verify(productInstanceClient, times(1)).put(productInstanceArgumentCaptor.capture());

        ProductInstance instance = productInstanceArgumentCaptor.getValue();

        assertThat(instance.getProductInstanceVersion(), is(2L));
        List<PriceLine> priceLines = instance.getPriceLines();
        assertThat(priceLines.size(), is(3));

        assertThat(priceLines.get(0).getId(), is("initialCpePrice"));
        assertThat(priceLines.get(1).getId(), is("initialCpeCease"));
        assertThat(priceLines.get(2).getId(), is("initialCpeCancellation"));
    }

    @Test
    public void shouldRemoveTotalCPEPriceLinesFromICT(){

        AutoPriceAggregator autoPriceAggregator = mock(AutoPriceAggregator.class);
        ProductInstanceClient productInstanceClient = mock(ProductInstanceClient.class);
        ProjectResource projectResource = mock(ProjectResource.class);

        PriceLineDTO cpeCease = new PriceLineDTOFixture().withId("cpeCease").withPpsrId(new PpsrId(1L)).withChargingSchemeName("CPE Cease").build();
        PriceLineDTO cpePrice = new PriceLineDTOFixture().withId("cpePrice").withPpsrId(new PpsrId(2L)).withChargingSchemeName("CPE").build();
        PriceLineDTO cpeCancellation = new PriceLineDTOFixture().withId("cpeCancellation").withPpsrId(new PpsrId(3L)).withChargingSchemeName("CPE Cancellation Charge").build();

        PriceLineDTO icgCPECeaseTotal = new PriceLineDTOFixture().withId("icgCPECeaseTotal").withPpsrId(new PpsrId(4L)).withChargingSchemeName("Total CPE Cease").build();
        PriceLineDTO icgCPEPriceTotal = new PriceLineDTOFixture().withId("icgCPEPriceTotal").withPpsrId(new PpsrId(5L)).withChargingSchemeName("Total ICg CPE Price").build();
        PriceLineDTO icgCPECancellationTotal = new PriceLineDTOFixture().withId("icgCPECancellationTotal").withPpsrId(new PpsrId(6L)).withChargingSchemeName("Total ICg CPE Cancellation Charge").build();
        PriceLineDTO icgPriceTotal = new PriceLineDTOFixture().withId("icgPriceTotal").withPpsrId(new PpsrId(7L)).withChargingSchemeName("Total Internet Connect Global Site").build();

        AssetDTO cpe = new AssetDTOBuilder().withProductInstanceId(new LengthConstrainingProductInstanceId("cpeAssetId")).withProductInstanceVersion(new ProductInstanceVersion(1L)).withPriceLines(newArrayList(cpeCease, cpePrice, cpeCancellation)).build();
        AssetDTO secondaryCpe = new AssetDTOBuilder().withProductInstanceId(new LengthConstrainingProductInstanceId("secondaryCpeAssetId")).withProductInstanceVersion(new ProductInstanceVersion(1L)).withPriceLines(newArrayList(cpeCease, cpePrice, cpeCancellation)).build();

        FutureAssetRelationshipDTO relationshipDTO1 = new FutureAssetRelationshipDTO(RelationshipName.newInstance("CPE"), RelationshipType.RelatedTo, cpe);
        FutureAssetRelationshipDTO relationshipDTO2 = new FutureAssetRelationshipDTO(RelationshipName.newInstance("SecondaryCPE"), RelationshipType.RelatedTo, secondaryCpe);

        AssetDTO accessReqAsset = new AssetDTOBuilder().withFutureAssetRelationshipDTOs(newArrayList(relationshipDTO1, relationshipDTO2)).build();
        FutureAssetRelationshipDTO accessReqAssetRelation = new FutureAssetRelationshipDTO(RelationshipName.newInstance("AccessRequirements"), RelationshipType.Child, accessReqAsset);

        AssetDTO icg = new AssetDTOBuilder().withProductInstanceId(new LengthConstrainingProductInstanceId("icgAssetId")).withProductInstanceVersion(new ProductInstanceVersion(1L))
                                            .withPriceLines(newArrayList(icgCPEPriceTotal, icgCPECeaseTotal, icgCPECancellationTotal,icgPriceTotal))
                                            .withLineItemId(new LineItemId(accessReqAsset.getLineItemId())).withFutureAssetRelationshipDTOs(newArrayList(accessReqAssetRelation)).build();

        when(productInstanceClient.getAssetKeysByProduct(org.mockito.Matchers.any(ProductSCode.class), org.mockito.Matchers.any(Date.class), eq("")))
            .thenReturn(newArrayList(AssetKey.newInstance("icgAssetId", 1l)));
        when(productInstanceClient.getAssetDtoByAssetKey(new LengthConstrainingProductInstanceId("icgAssetId"), new ProductInstanceVersion(1L))).thenReturn(icg);

        when(productInstanceClient.getAssetDtoByAssetKey(new LengthConstrainingProductInstanceId(cpe.getId()), new ProductInstanceVersion(cpe.getVersion()))).thenReturn(cpe);
        when(productInstanceClient.getAssetDtoByAssetKey(new LengthConstrainingProductInstanceId(secondaryCpe.getId()), new ProductInstanceVersion(secondaryCpe.getVersion()))).thenReturn(secondaryCpe);


        ProductInstance icgInstance = new DefaultProductInstanceFixture("icg").withProductInstanceId("icgAssetId").withLineItemId("icgLineItemId").build();
        when(productInstanceClient.getByAssetKey(AssetKey.newInstance("icgAssetId", 1L))).thenReturn(icgInstance);
        ProductInstance cpeInstance = new DefaultProductInstanceFixture("cpe").withProductInstanceId("cpeAssetId").withLineItemId("cpeLineItemId").build();
        when(productInstanceClient.getByAssetKey(AssetKey.newInstance("cpeAssetId", 1L))).thenReturn(cpeInstance);
        ProductInstance secondaryCpeInstance = new DefaultProductInstanceFixture("secondaryCpe").withProductInstanceId("secondaryCpeAssetId").withLineItemId("secondaryCpeLineItemId").build();
        when(productInstanceClient.getByAssetKey(AssetKey.newInstance("secondaryCpeAssetId", 1L))).thenReturn(secondaryCpeInstance);

        PriceBookDTO priceBookDTO = new PriceBookDTO("someId", "someRequestId", "someEupPriceBook", "somePTPPriceBook", null, null);
        ContractDTO contractDTO = new ContractDTO(UUID.randomUUID().toString(), "", asList(priceBookDTO));
        QuoteOptionItemDTO optionItemDTO = new QuoteOptionItemDTO(icg.getLineItemId(), "", "", null,  "", "", "", null, null, null, null, null, null,
                                                                  null, "", null, contractDTO, false, false, false, null, null, true, new ProductCategoryCode("H123"), null, false);
        QuoteOptionResource quoteOptionResource = mock(QuoteOptionResource.class);
        QuoteOptionItemResource quoteOptionItemResource = mock(QuoteOptionItemResource.class);
        when(projectResource.quoteOptionResource(anyString())).thenReturn(quoteOptionResource);
        when(quoteOptionResource.quoteOptionItemResource(anyString())).thenReturn(quoteOptionItemResource);
        when(quoteOptionItemResource.get(anyString())).thenReturn(optionItemDTO);

        CPEUpliftResourceHandler cpeUpliftResourceHandler = new CPEUpliftResourceHandler(productInstanceClient, autoPriceAggregator, projectResource);
        Response response = cpeUpliftResourceHandler.upliftCPEICGPrices(ProductCodes.InternetConnectTopology.productCode(), "1", "", "");

        ArgumentCaptor<AssetDTO> assetDTOArgumentCaptor = ArgumentCaptor.forClass(AssetDTO.class);
        verify(productInstanceClient, times(1)).putAsset(assetDTOArgumentCaptor.capture());

        List<AssetDTO> allValues = assetDTOArgumentCaptor.getAllValues();
        for (AssetDTO assetDTO : allValues) {
            assertThat(assetDTO.getPriceLines().size(), is(1));
        }

        verify(autoPriceAggregator, times(1)).aggregatePricesOf(priceBookDTO, new LineItemId(cpeInstance.getLineItemId()));
        verify(autoPriceAggregator, times(1)).aggregatePricesOf(priceBookDTO, new LineItemId(secondaryCpeInstance.getLineItemId()));
        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));

    }

    @Test
    public void shouldRemoveTotalCPEPriceLinesFromICT1(){

        AutoPriceAggregator autoPriceAggregator = mock(AutoPriceAggregator.class);
        ProductInstanceClient productInstanceClient = mock(ProductInstanceClient.class);
        ProjectResource projectResource = mock(ProjectResource.class);

        PriceLineDTO totalWebVpnCpe = new PriceLineDTOFixture().withId("totalWebVpnCpe").withPpsrId(new PpsrId(1L)).withChargingSchemeName("Total Web-VPN CPE").build();
        PriceLineDTO totalWebVpnVBasicService = new PriceLineDTOFixture().withId("totalWebVpnVBasicService").withPpsrId(new PpsrId(2L)).withChargingSchemeName("Total Web-VPN Basic Service").build();
        PriceLineDTO BasicService = new PriceLineDTOFixture().withId("BasicService").withPpsrId(new PpsrId(3L)).withChargingSchemeName("Basic Service").build();

        PriceLineDTO totalAdditionalVPNConnection = new PriceLineDTOFixture().withId("BasicService").withPpsrId(new PpsrId(3L)).withChargingSchemeName("Basic Service").build();

        AssetDTO vpnMembership = new AssetDTOBuilder().withProductInstanceId(new LengthConstrainingProductInstanceId("vpnMembership")).withProductInstanceVersion(new ProductInstanceVersion(1L)).withLineItemId(new LineItemId("webVpnLineItemId")).withPriceLines(newArrayList(totalAdditionalVPNConnection)).build();
        FutureAssetRelationshipDTO relationshipDTO1 = new FutureAssetRelationshipDTO(RelationshipName.newInstance("additionalVpnConnection"), RelationshipType.Child, vpnMembership);


        AssetDTO webVpn = new AssetDTOBuilder().withProductInstanceId(new LengthConstrainingProductInstanceId("webVpn"))
                                               .withLineItemId(new LineItemId("webVpnLineItemId"))
                                               .withProductInstanceVersion(new ProductInstanceVersion(1L))
                                               .withFutureAssetRelationshipDTOs(newArrayList(relationshipDTO1))
                                               .withAssetVersionStatus(AssetVersionStatus.DRAFT)
                                               .withPriceLines(newArrayList(totalWebVpnCpe, totalWebVpnVBasicService, BasicService)).build();

        when(productInstanceClient.getAssetDtoByAssetKey(new LengthConstrainingProductInstanceId("webVpn"), new ProductInstanceVersion(1L))).thenReturn(webVpn);

        ProductInstance webVpnInstance = new DefaultProductInstanceFixture("webVpn").withProductInstanceId("webVpn").withLineItemId("webVpnLineItemId").build();

        when(productInstanceClient.getAssetKeysByProduct(org.mockito.Matchers.any(ProductSCode.class), org.mockito.Matchers.any(Date.class), eq("")))
            .thenReturn(newArrayList(AssetKey.newInstance("webVpn", 1l)));
        when(productInstanceClient.getByAssetKey(AssetKey.newInstance("webVpn", 1L))).thenReturn(webVpnInstance);


        PriceBookDTO priceBookDTO = new PriceBookDTO("someId", "someRequestId", "someEupPriceBook", "somePTPPriceBook", null, null);
        ContractDTO contractDTO = new ContractDTO(UUID.randomUUID().toString(), "", asList(priceBookDTO));
        QuoteOptionItemDTO optionItemDTO = new QuoteOptionItemDTO(webVpn.getLineItemId(), "", "", null,  "", "", "", null, null, null, null, null, null,
                                                                  null, "", null, contractDTO, false, false, false, null, null, true, new ProductCategoryCode("H123"), null, false);
        QuoteOptionResource quoteOptionResource = mock(QuoteOptionResource.class);
        QuoteOptionItemResource quoteOptionItemResource = mock(QuoteOptionItemResource.class);
        when(projectResource.quoteOptionResource(anyString())).thenReturn(quoteOptionResource);
        when(quoteOptionResource.quoteOptionItemResource(anyString())).thenReturn(quoteOptionItemResource);
        when(quoteOptionItemResource.get(anyString())).thenReturn(optionItemDTO);

        CPEUpliftResourceHandler cpeUpliftResourceHandler = new CPEUpliftResourceHandler(productInstanceClient, autoPriceAggregator, projectResource);
        Response response = cpeUpliftResourceHandler.upliftWepVpnPrices("S0336638", "1", "", "");

        ArgumentCaptor<ProductInstance> assetDTOArgumentCaptor = ArgumentCaptor.forClass(ProductInstance.class);


        verify(autoPriceAggregator, times(1)).aggregatePricesOf(priceBookDTO, new LineItemId(webVpn.getLineItemId()));
        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
    }
}
