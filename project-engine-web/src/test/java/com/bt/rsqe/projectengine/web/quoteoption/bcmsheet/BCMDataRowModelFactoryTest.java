package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;

import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.bom.parameters.OrderType;
import com.bt.rsqe.domain.bom.parameters.ProductInstanceId;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.expedio.fixtures.SiteDTOFixture;
import com.bt.rsqe.pricing.PricingClient;
import com.bt.rsqe.pricing.config.dto.PricingConfig;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTOFixture;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.PricingSheetTestDataFixture;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model.AbstractPricingSheetProductModel;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model.PricingSheetProductModel;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model.PricingSheetProductModelFixture;
import com.bt.rsqe.security.UserContextManager;
import com.google.common.base.Optional;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import java.util.List;

import static com.bt.rsqe.security.UserContextBuilder.*;
import static com.google.common.collect.Lists.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class BCMDataRowModelFactoryTest {
    PricingSheetProductModel pricingSheetProductModel;

    private PricingConfig pricingConfig;
    private ProductInstanceClient productInstanceClient;
    private BCMDataRowModelFactory bcmDataRowModelFactory;


    @Before
    public void setup() {
        PricingClient pricingClient = mock(PricingClient.class);
        pricingConfig = mock(PricingConfig.class);
        productInstanceClient = mock(ProductInstanceClient.class);
        bcmDataRowModelFactory = new BCMDataRowModelFactory(productInstanceClient);
        when(pricingClient.getPricingConfig()).thenReturn(pricingConfig);
        when(productInstanceClient.getSourceAsset(anyString())).thenReturn(Optional.<ProductInstance>absent());
    }

    private void withIndirectUser() {
        UserContextManager.setCurrent(anIndirectUserContext().withIndirectUser().build());
    }

    @Test
    public void shouldReturnListOfRowModels() {
        pricingSheetProductModel = new PricingSheetTestDataFixture().aPricingSheetProductModelWithAChild();
        List<BCMDataRowModel> rows = bcmDataRowModelFactory.fetchBcmRowModel(pricingSheetProductModel);
        assertThat(rows.size(), is(5));
        assertThat(rows.get(1).getChildProducts().size(), is(1));
        assertThat(rows.get(1).getCostLines().size(), is(1));
    }

    @Test
    public void shouldReturnListOfProductDataRowModels(){
        withIndirectUser();
        PricingSheetTestDataFixture pricingSheetTestDataFixture = new PricingSheetTestDataFixture();
        ProductInstance productInstance = pricingSheetTestDataFixture.aRootProductAndChildWithRelationshipName();
        QuoteOptionItemDTO itemDTO = QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().build();
        SiteDTO siteDTO = SiteDTOFixture.aSiteDTO()
                                        .withName("aSiteName")
                                        .withCity("aCity")
                                        .withCountry("aCountry")
                                        .withBfgSiteId("aSiteID").build();

        PricingSheetProductModel sheetProductModel = PricingSheetProductModelFixture.aPricingSheetProductModel()
                                                                                    .withProductInstance(productInstance)
                                                                                    .withQuoteOptionItem(itemDTO)
                                                                                    .withSiteDTO(siteDTO)
                                                                                    .build();
        when(productInstanceClient.getLatestProduct(Matchers.<ProductInstanceId>anyObject(), anyString())).thenReturn(productInstance);
        List<AbstractPricingSheetProductModel> productModels = newArrayList((AbstractPricingSheetProductModel) sheetProductModel);
        List<ProductDataRowModel> dataRows = bcmDataRowModelFactory.createProductRowModel(productModels);
        assertThat(dataRows.size(), is(1));
        ProductDataRowModel dataRow = dataRows.get(0);
        assertThat(dataRow.getSite().getSiteName(), is("aSiteName"));
        assertThat(dataRow.getQuoteOptionItem().action, is("PROVIDE"));
        assertThat(dataRow.getCpeProductInstance().getBcmPriceModel().getOnetimeEUPPrice(), is("100.00"));
        assertThat(dataRow.getCpeProductInstance().getBcmPriceModel().getRecurringEUPPrice(), is("100.00"));
        assertThat(dataRow.getVendorMaintenanceInstance().getBcmCostModel().get(0).getOnetimeEUPPrice(), is("100.00"));
        assertThat(dataRow.getVendorMaintenanceInstance().getBcmCostModel().get(0).getRecurringEUPPrice(), is("100.00"));
    }

    @Test
    public void shouldReturnListOfProductDataRowModelWithLicences(){
        withIndirectUser();
        PricingSheetTestDataFixture pricingSheetTestDataFixture = new PricingSheetTestDataFixture();
        ProductInstance productInstance = pricingSheetTestDataFixture.anInstallableRootProductWithAChildAndSubChild();
        QuoteOptionItemDTO itemDTO = QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().build();
        SiteDTO siteDTO = SiteDTOFixture.aSiteDTO()
                                        .withName("aSiteName")
                                        .withCity("aCity")
                                        .withCountry("aCountry")
                                        .withBfgSiteId("aSiteID").build();

        PricingSheetProductModel sheetProductModel = PricingSheetProductModelFixture.aPricingSheetProductModel()
                                                                                    .withProductInstance(productInstance)
                                                                                    .withQuoteOptionItem(itemDTO)
                                                                                    .withSiteDTO(siteDTO)
                                                                                    .build();
        when(productInstanceClient.getLatestProduct(Matchers.<ProductInstanceId>anyObject(), anyString())).thenReturn(productInstance);
        List<AbstractPricingSheetProductModel> productModels = newArrayList((AbstractPricingSheetProductModel) sheetProductModel);
        List<ProductDataRowModel> dataRows = bcmDataRowModelFactory.createProductRowModel(productModels);
        assertThat(dataRows.size(), is(1));
        ProductDataRowModel dataRow = dataRows.get(0);
        assertThat(dataRow.getSite().getSiteName(), is("aSiteName"));
        assertThat(dataRow.getQuoteOptionItem().action, is("PROVIDE"));
        assertThat(dataRow.getLicences().size(), is(1));
        assertThat(dataRow.getLicences().get(0).getBcmPriceModel().getOnetimeEUPPrice(), is("100.00"));
        assertThat(dataRow.getLicences().get(0).getBcmPriceModel().getRecurringEUPPrice(), is("100.00"));
    }

    @Test
    public void shouldReturnListOfProductDataRowModelForSiteManagement(){
        withIndirectUser();
        PricingSheetTestDataFixture pricingSheetTestDataFixture = new PricingSheetTestDataFixture();
        ProductInstance productInstance = pricingSheetTestDataFixture.anInstallableRootProductWithAChild();
        QuoteOptionItemDTO itemDTO = QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().build();
        SiteDTO siteDTO = SiteDTOFixture.aSiteDTO()
                                        .withName("aSiteName")
                                        .withCity("aCity")
                                        .withCountry("aCountry")
                                        .withBfgSiteId("aSiteID").build();

        PricingSheetProductModel sheetProductModel = PricingSheetProductModelFixture.aPricingSheetProductModel()
                                                                                    .withProductInstance(productInstance)
                                                                                    .withQuoteOptionItem(itemDTO)
                                                                                    .withSiteDTO(siteDTO)
                                                                                    .build();
        when(productInstanceClient.getLatestProduct(Matchers.<ProductInstanceId>anyObject(), anyString())).thenReturn(productInstance);
        List<AbstractPricingSheetProductModel> productModels = newArrayList((AbstractPricingSheetProductModel) sheetProductModel);
        List<ProductDataRowModel> dataRows = bcmDataRowModelFactory.createSiteManagementRowModel(productModels, pricingConfig);
        assertThat(dataRows.size(), is(1));
        ProductDataRowModel dataRow = dataRows.get(0);
        assertThat(dataRow.getSite().getSiteName(), is("aSiteName"));
        assertThat(dataRow.getQuoteOptionItem().action, is("PROVIDE"));
        assertThat(dataRow.getRootProductInstance().getBcmPriceModel().getOnetimeEUPPrice(), is("100.00"));
        assertThat(dataRow.getRootProductInstance().getBcmPriceModel().getRecurringEUPPrice(), is("100.00"));
    }

    @Test
    public void shouldReturnListOfProductDataRowModelForSiteAgnosticProduct(){
        withIndirectUser();
        PricingSheetTestDataFixture pricingSheetTestDataFixture = new PricingSheetTestDataFixture();
        ProductInstance productInstance = pricingSheetTestDataFixture.aSiteAgnosticProductInstance();
        QuoteOptionItemDTO itemDTO = QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().build();
        SiteDTO siteDTO = SiteDTOFixture.aSiteDTO()
                                        .withName("aSiteName")
                                        .withCity("aCity")
                                        .withCountry("aCountry")
                                        .withBfgSiteId("aSiteID").build();

        PricingSheetProductModel sheetProductModel = PricingSheetProductModelFixture.aPricingSheetProductModel()
                                                                                    .withProductInstance(productInstance)
                                                                                    .withQuoteOptionItem(itemDTO)
                                                                                    .withSiteDTO(siteDTO)
                                                                                    .build();
        when(productInstanceClient.getLatestProduct(Matchers.<ProductInstanceId>anyObject(), anyString())).thenReturn(productInstance);
        List<AbstractPricingSheetProductModel> productModels = newArrayList((AbstractPricingSheetProductModel) sheetProductModel);
        List<ProductDataRowModel> dataRows = bcmDataRowModelFactory.createServiceRowModel(productModels, pricingConfig);
        assertThat(dataRows.size(), is(2));
        ProductDataRowModel dataRow = dataRows.get(0);
        assertThat(dataRow.getSite().getSiteName(), is("aSiteName"));
        assertThat(dataRow.getQuoteOptionItem().action, is("PROVIDE"));
        assertNull(dataRow.getCpeProductInstance());
        assertThat(dataRow.getRootProductInstance().getBcmPriceModel().getOnetimeEUPPrice(), is("100.00"));
        assertThat(dataRow.getRootProductInstance().getBcmPriceModel().getRecurringEUPPrice(), is("100.00"));

        dataRow = dataRows.get(1);
        assertThat(dataRow.getSite().getSiteName(), is("aSiteName"));
        assertThat(dataRow.getQuoteOptionItem().action, is("PROVIDE"));
        assertNull(dataRow.getCpeProductInstance());
        assertThat(dataRow.getRootProductInstance().getBcmPriceModel().getOnetimeEUPPrice(), is("100.00"));
        assertThat(dataRow.getRootProductInstance().getBcmPriceModel().getRecurringEUPPrice(), is("100.00"));
    }

    @Test
    public void shouldReturnListOfProductDataRowModelForSiteAgnosticProductWithGrandChild(){
        withIndirectUser();
        PricingSheetTestDataFixture pricingSheetTestDataFixture = new PricingSheetTestDataFixture();
        ProductInstance productInstance = pricingSheetTestDataFixture.aSiteAgnosticProductInstanceWithGrandChild();
        QuoteOptionItemDTO itemDTO = QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().build();
        SiteDTO siteDTO = SiteDTOFixture.aSiteDTO()
                                        .withName("aSiteName")
                                        .withCity("aCity")
                                        .withCountry("aCountry")
                                        .withBfgSiteId("aSiteID").build();

        PricingSheetProductModel sheetProductModel = PricingSheetProductModelFixture.aPricingSheetProductModel()
                                                                                    .withProductInstance(productInstance)
                                                                                    .withQuoteOptionItem(itemDTO)
                                                                                    .withSiteDTO(siteDTO)
                                                                                    .build();
        when(productInstanceClient.getLatestProduct(Matchers.<ProductInstanceId>anyObject(), anyString())).thenReturn(productInstance);
        List<AbstractPricingSheetProductModel> productModels = newArrayList((AbstractPricingSheetProductModel) sheetProductModel);
        List<ProductDataRowModel> dataRows = bcmDataRowModelFactory.createServiceRowModel(productModels, pricingConfig);
        assertThat(dataRows.size(), is(3));
        ProductDataRowModel dataRow = dataRows.get(0);
        assertThat(dataRow.getSite().getSiteName(), is("aSiteName"));
        assertThat(dataRow.getQuoteOptionItem().action, is("PROVIDE"));
        assertNull(dataRow.getCpeProductInstance());
        assertThat(dataRow.getRootProductInstance().getBcmPriceModel().getOnetimeEUPPrice(), is("200.00"));
        assertThat(dataRow.getRootProductInstance().getBcmPriceModel().getRecurringEUPPrice(), is("200.00"));

        dataRow = dataRows.get(1);
        assertThat(dataRow.getSite().getSiteName(), is("aSiteName"));
        assertThat(dataRow.getQuoteOptionItem().action, is("PROVIDE"));
        assertNull(dataRow.getCpeProductInstance());
        assertThat(dataRow.getRootProductInstance().getBcmPriceModel().getOnetimeEUPPrice(), is("200.00"));
        assertThat(dataRow.getRootProductInstance().getBcmPriceModel().getRecurringEUPPrice(), is("200.00"));

        dataRow = dataRows.get(2);
        assertThat(dataRow.getSite().getSiteName(), is("aSiteName"));
        assertThat(dataRow.getQuoteOptionItem().action, is("PROVIDE"));
        assertThat(dataRow.getRootProductInstance().getBcmPriceModel().getOnetimeEUPPrice(), is("200.00"));
        assertThat(dataRow.getRootProductInstance().getBcmPriceModel().getRecurringEUPPrice(), is("200.00"));
    }

    @Test
    public void shouldGetCostLinesFromChildren() {
        pricingSheetProductModel = new PricingSheetTestDataFixture().aPricingSheetProductModelWithTwoSteelHead();
        List<BCMPriceModel> bcmPriceModels = newArrayList();
        bcmDataRowModelFactory.getCostLinesFromChildren(pricingSheetProductModel.getProductInstance(), bcmPriceModels, OrderType.PROVIDE.name());
        assertThat(bcmPriceModels.size(), is(1));
    }

    @Test
    public void shouldReturnTwoRowsForTwoSteelHead() {
        pricingSheetProductModel = new PricingSheetTestDataFixture().aPricingSheetProductModelWithTwoSteelHead();
        List<AbstractPricingSheetProductModel> productModels = newArrayList((AbstractPricingSheetProductModel)pricingSheetProductModel);
        List<BCMDataRowModel> rows = bcmDataRowModelFactory.createRowModel(productModels);
        assertThat(rows.size(), is(8));
    }

    @Test
    public void shouldPopulateChildRelationshipName() {
        pricingSheetProductModel = new PricingSheetTestDataFixture().aPricingSheetProductModelWithRelations();
        List<BCMDataRowModel> rows = bcmDataRowModelFactory.fetchBcmRowModel(pricingSheetProductModel);
        assertThat(rows.size(), is(2));
    }
}
