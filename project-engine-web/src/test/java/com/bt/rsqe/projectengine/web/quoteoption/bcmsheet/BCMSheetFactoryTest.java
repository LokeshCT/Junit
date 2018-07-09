package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;


import com.bt.rsqe.client.Pmr;
import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.SalesCatalogue;
import com.bt.rsqe.domain.bom.fixtures.ProductOfferingFixture;
import com.bt.rsqe.domain.bom.fixtures.SellableProductFixture;
import com.bt.rsqe.domain.bom.parameters.ProductInstanceId;
import com.bt.rsqe.domain.bom.parameters.ProductSCode;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.SellableProduct;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.expedio.fixtures.SiteDTOFixture;
import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.pricing.PricingClient;
import com.bt.rsqe.pricing.config.dto.ChargingSchemeConfig;
import com.bt.rsqe.pricing.config.dto.PricingConfig;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTOFixture;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.PricingSheetTestDataFixture;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model.PricingSheetDataModel;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model.PricingSheetDataModelFixture;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model.PricingSheetProductModel;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model.PricingSheetProductModelFixture;
import com.bt.rsqe.security.UserContextManager;
import com.google.common.base.Optional;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import java.util.List;
import java.util.Map;

import static com.bt.rsqe.security.UserContextBuilder.*;
import static com.google.common.collect.Lists.*;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class BCMSheetFactoryTest {

    private PmrClient pmrClient;
    private HSSFWorkbook workbook;
    private BCMSheetFactory bcmSheetFactory;
    private ProductInstanceClient productInstanceClient;
    private BCMDataRowModelFactory bcmDataRowModelFactory;

    @Before
    public void setUp(){

        BCMSheetGenerator bcmSheetGenerator = new BCMSheetGenerator();
        pmrClient = mock(PmrClient.class);
        productInstanceClient = mock(ProductInstanceClient.class);
        PricingClient pricingClient = mock(PricingClient.class);
        HeaderRowModelFactory headerRowModelFactory = new HeaderRowModelFactory(pmrClient);
        workbook = new HSSFWorkbook();
        bcmDataRowModelFactory = new BCMDataRowModelFactory(productInstanceClient);
        PricingConfig pricingConfig = mock(PricingConfig.class);
        bcmSheetFactory = new BCMSheetFactory(headerRowModelFactory, bcmDataRowModelFactory,pmrClient, bcmSheetGenerator, pricingClient);
        when(pricingClient.getPricingConfig()).thenReturn(pricingConfig);
        PricingConfig.ChargingSchemeFilterCriteria chargingSchemeFilterCriteria = mock(PricingConfig.ChargingSchemeFilterCriteria.class);
        List<ChargingSchemeConfig> chargingSchemeConfigs = newArrayList();
        when(chargingSchemeFilterCriteria.search()).thenReturn(chargingSchemeConfigs);
        when(pricingConfig.chargingSchemes()).thenReturn(chargingSchemeFilterCriteria);
        when(chargingSchemeFilterCriteria.forName(anyString())).thenReturn(chargingSchemeFilterCriteria);
        when(productInstanceClient.getSourceAsset(anyString())).thenReturn(Optional.<ProductInstance>absent());
    }

    private void withIndirectUser() {
        UserContextManager.setCurrent(anIndirectUserContext().build());
    }

    private void withDirectUser() {
        UserContextManager.setCurrent(aDirectUserContext().build());
    }

    @Test
    public void shouldCreateBidInfoSheet(){
        List<Map<String,String>> dataRowModel =  newArrayList();
        bcmSheetFactory.createBidInfoSheet(workbook,dataRowModel,"Bid Info");
        assertThat(workbook.getSheet("Bid Info").getLastRowNum(), is(0));
    }

    @Test
    public void shouldCreateProductsPerSiteSheet(){
        List<Map<String,String>> dataRowModel =  newArrayList();
        bcmSheetFactory.createProductPerSiteSheet(workbook, dataRowModel, "PPS");
        assertThat(workbook.getSheet("PPS").rowIterator().hasNext(), is(true));
    }

    @Test
    public void shouldCreateSpecialBidSheet(){
        List<Map<String,String>> dataRowModel =  newArrayList();
        bcmSheetFactory.createSpecialBidSheet(workbook, dataRowModel, "Special Bid");
        assertThat(workbook.getSheet("Special Bid").rowIterator().hasNext(), is(true));
    }

    @Test
    public void shouldCreateSiteBasedRootProductSheet(){
        SellableProduct product1 = SellableProductFixture.aProduct().withId("S0308454").withName("Connect Acceleration Site").withSiteInstallable(true).withIsImportable(false).build();
        SalesCatalogue salesCatalogue = new SalesCatalogue();
        salesCatalogue.addSellableProduct(product1) ;
        when(pmrClient.getSalesCatalogue()).thenReturn(salesCatalogue);
        Pmr.ProductOfferings offerings = mock(Pmr.ProductOfferings.class);
        when(pmrClient.productOffering(ProductSCode.newInstance("S0308454"))).thenReturn(offerings);
        when(pmrClient.getProductName("S0308454")).thenReturn("Connect Acceleration Site");
        when(pmrClient.getProductHCode("S0308454")).thenReturn(Optional.of(new ProductIdentifier("H0300521","CA","1.0")));
        ProductOffering aProductOffering = ProductOfferingFixture.aProductOffering().withSiteSpecific().build();
        when(offerings.get()).thenReturn(aProductOffering);

        withIndirectUser();
        PricingSheetTestDataFixture pricingSheetTestDataFixture = new PricingSheetTestDataFixture();
        ProductInstance productInstance = pricingSheetTestDataFixture.aRootProductAndChildWithRelationshipName();
        QuoteOptionItemDTO itemDTO = QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().build();
        SiteDTO siteDTO = SiteDTOFixture.aSiteDTO()
                                        .withName("aSiteName")
                                        .withCity("aCity")
                                        .withCountry("aCountry")
                                        .withBfgSiteId("123").build();

        PricingSheetProductModel sheetProductModel = PricingSheetProductModelFixture.aPricingSheetProductModel()
                                                                                    .withProductInstance(productInstance)
                                                                                    .withQuoteOptionItem(itemDTO)
                                                                                    .withSiteDTO(siteDTO)
                                                                                    .build();
        PricingSheetDataModel dataModel= PricingSheetDataModelFixture.aPricingSheetModel().withPricingSheetProductModel(sheetProductModel).build();
        when(productInstanceClient.getLatestProduct(Matchers.<ProductInstanceId>anyObject(), anyString())).thenReturn(productInstance);
        bcmSheetFactory.createSiteServiceRootProductSheet(workbook,dataModel);
        assertEquals(workbook.getNumberOfSheets(),2);
        assertEquals(workbook.getSheetName(0), "CA Site");
        assertEquals(workbook.getSheetName(1), "CA Site Management");
    }

    @Test
    public void shouldCreateSiteBasedRootProductSheetWithOutVendorMaintainance(){
        SellableProduct product1 = SellableProductFixture.aProduct().withId("S0308454").withName("Connect Acceleration Site").withSiteInstallable(true).withIsImportable(false).build();
        SalesCatalogue salesCatalogue = new SalesCatalogue();
        salesCatalogue.addSellableProduct(product1) ;
        when(pmrClient.getSalesCatalogue()).thenReturn(salesCatalogue);
        Pmr.ProductOfferings offerings = mock(Pmr.ProductOfferings.class);
        when(pmrClient.productOffering(ProductSCode.newInstance("S0308454"))).thenReturn(offerings);
        when(pmrClient.getProductName("S0308454")).thenReturn("Connect Acceleration Site");
        when(pmrClient.getProductHCode("S0308454")).thenReturn(Optional.of(new ProductIdentifier("H0300521","CA","1.0")));
        ProductOffering aProductOffering = ProductOfferingFixture.aProductOffering().withSiteSpecific().build();
        when(offerings.get()).thenReturn(aProductOffering);

        withIndirectUser();
        PricingSheetTestDataFixture pricingSheetTestDataFixture = new PricingSheetTestDataFixture();
        ProductInstance productInstance = pricingSheetTestDataFixture.aCARootProductAndChildWithRelationshipNameWithoutVendorMaintainance();
        QuoteOptionItemDTO itemDTO = QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().build();
        SiteDTO siteDTO = SiteDTOFixture.aSiteDTO()
                                        .withName("aSiteName")
                                        .withCity("aCity")
                                        .withCountry("aCountry")
                                        .withBfgSiteId("123").build();

        PricingSheetProductModel sheetProductModel = PricingSheetProductModelFixture.aPricingSheetProductModel()
                                                                                    .withProductInstance(productInstance)
                                                                                    .withQuoteOptionItem(itemDTO)
                                                                                    .withSiteDTO(siteDTO)
                                                                                    .build();
        PricingSheetDataModel dataModel= PricingSheetDataModelFixture.aPricingSheetModel().withPricingSheetProductModel(sheetProductModel).build();
        when(productInstanceClient.getLatestProduct(Matchers.<ProductInstanceId>anyObject(), anyString())).thenReturn(productInstance);
        bcmSheetFactory.createSiteServiceRootProductSheet(workbook,dataModel);
        assertEquals(workbook.getNumberOfSheets(),2);
        assertEquals(workbook.getSheetName(0), "CA Site");
        assertEquals(workbook.getSheetName(1), "CA Site Management");
    }

    @Test
    public void shouldCreateSiteManagementSheetWithoutPriceLines(){
        SellableProduct product1 = SellableProductFixture.aProduct().withId("S0308454").withName("Connect Acceleration Site").withSiteInstallable(true).withIsImportable(false).build();
        SalesCatalogue salesCatalogue = new SalesCatalogue();
        salesCatalogue.addSellableProduct(product1) ;
        when(pmrClient.getSalesCatalogue()).thenReturn(salesCatalogue);
        Pmr.ProductOfferings offerings = mock(Pmr.ProductOfferings.class);
        when(pmrClient.productOffering(ProductSCode.newInstance("S0308454"))).thenReturn(offerings);
        when(pmrClient.getProductName("S0308454")).thenReturn("Connect Acceleration Site");
        when(pmrClient.getProductHCode("S0308454")).thenReturn(Optional.of(new ProductIdentifier("H0300521","CA","1.0")));
        ProductOffering aProductOffering = ProductOfferingFixture.aProductOffering().withSiteSpecific().build();
        when(offerings.get()).thenReturn(aProductOffering);

        withIndirectUser();
        PricingSheetTestDataFixture pricingSheetTestDataFixture = new PricingSheetTestDataFixture();
        ProductInstance productInstance = pricingSheetTestDataFixture.aCARootProductAndChildWithRelationshipNameAndWithoutSitemanagementPrticelines();
        QuoteOptionItemDTO itemDTO = QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().build();
        SiteDTO siteDTO = SiteDTOFixture.aSiteDTO()
                                        .withName("aSiteName")
                                        .withCity("aCity")
                                        .withCountry("aCountry")
                                        .withBfgSiteId("112121").build();

        PricingSheetProductModel sheetProductModel = PricingSheetProductModelFixture.aPricingSheetProductModel()
                                                                                    .withProductInstance(productInstance)
                                                                                    .withQuoteOptionItem(itemDTO)
                                                                                    .withSiteDTO(siteDTO)
                                                                                    .build();
        when(productInstanceClient.getLatestProduct(Matchers.<ProductInstanceId>anyObject(), anyString())).thenReturn(productInstance);
        PricingSheetDataModel dataModel= PricingSheetDataModelFixture.aPricingSheetModel().withPricingSheetProductModel(sheetProductModel).build();
        bcmSheetFactory.createSiteServiceRootProductSheet(workbook,dataModel);
        assertThat(workbook.getSheet("CA Site Management").getLastRowNum(), is(1));
    }

    @Test
    public void shouldNotCreateSiteBasedRootProductSheetWhenScodeDoesNotMatch(){
        SellableProduct product1 = SellableProductFixture.aProduct().withId("S03084546").withName("CA Site").withSiteInstallable(true).withIsImportable(false).build();
        SalesCatalogue salesCatalogue = new SalesCatalogue();
        salesCatalogue.addSellableProduct(product1) ;
        when(pmrClient.getSalesCatalogue()).thenReturn(salesCatalogue);
        Pmr.ProductOfferings offerings = mock(Pmr.ProductOfferings.class);
        when(pmrClient.productOffering(ProductSCode.newInstance("S03084546"))).thenReturn(offerings);
        when(pmrClient.getProductHCode("S03084546")).thenReturn(Optional.of(new ProductIdentifier("H0300521","CA","1.0")));
        ProductOffering aProductOffering = ProductOfferingFixture.aProductOffering().withSiteSpecific().build();
        when(offerings.get()).thenReturn(aProductOffering);

        withIndirectUser();
        PricingSheetTestDataFixture pricingSheetTestDataFixture = new PricingSheetTestDataFixture();
        ProductInstance productInstance = pricingSheetTestDataFixture.aRootProductAndChildWithRelationshipName();
        QuoteOptionItemDTO itemDTO = QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().build();
        SiteDTO siteDTO = SiteDTOFixture.aSiteDTO()
                                        .withName("aSiteName")
                                        .withCity("aCity")
                                        .withCountry("aCountry")
                                        .withBfgSiteId("112121").build();

        PricingSheetProductModel sheetProductModel = PricingSheetProductModelFixture.aPricingSheetProductModel()
                                                                                    .withProductInstance(productInstance)
                                                                                    .withQuoteOptionItem(itemDTO)
                                                                                    .withSiteDTO(siteDTO)
                                                                                    .build();
        PricingSheetDataModel dataModel= PricingSheetDataModelFixture.aPricingSheetModel().withPricingSheetProductModel(sheetProductModel).build();
        when(productInstanceClient.getLatestProduct(Matchers.<ProductInstanceId>anyObject(), anyString())).thenReturn(productInstance);
        bcmSheetFactory.createSiteServiceRootProductSheet(workbook,dataModel);
        assertNull(workbook.getSheet("CA Site"));
        assertNull(workbook.getSheet("CA Site Management"));
    }

    @Test
    public void shouldCreateServiceBasedRootProductSheet(){
        String productId= ServiceProductScode.ConnectAccelerationService.getsCode();
        String productName= ServiceProductScode.ConnectAccelerationService.getServiceName();
        String sheetName= ServiceProductScode.ConnectAccelerationService.getShortServiceName();
        SellableProduct product1 = SellableProductFixture.aProduct().withId(productId).withName(productName).withSiteInstallable(false).withIsImportable(false).build();
        SalesCatalogue salesCatalogue = new SalesCatalogue();
        salesCatalogue.addSellableProduct(product1) ;
        when(pmrClient.getSalesCatalogue()).thenReturn(salesCatalogue);
        Pmr.ProductOfferings offerings = mock(Pmr.ProductOfferings.class);
        when(pmrClient.productOffering(ProductSCode.newInstance(productId))).thenReturn(offerings);
        ProductOffering aProductOffering = ProductOfferingFixture.aProductOffering().build();
        when(offerings.get()).thenReturn(aProductOffering);

        withIndirectUser();
        PricingSheetTestDataFixture pricingSheetTestDataFixture = new PricingSheetTestDataFixture();
        ProductInstance productInstance = pricingSheetTestDataFixture.aCAServiceProductInstance();
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
        PricingSheetDataModel dataModel= PricingSheetDataModelFixture.aPricingSheetModel().withPricingSheetProductModel(sheetProductModel).build();
        when(productInstanceClient.getLatestProduct(Matchers.<ProductInstanceId>anyObject(), anyString())).thenReturn(productInstance);
        bcmSheetFactory.createSiteServiceRootProductSheet(workbook, dataModel);
        assertThat(workbook.getSheetName(0), is(sheetName));
        assertThat(workbook.getSheet(sheetName).getLastRowNum(), is(2));

    }

    @Test
    public void shouldCreateServiceBasedRootProductSheetWithHeadersOnly(){
        String productId= ServiceProductScode.ConnectAccelerationService.getsCode();
        String productName= ServiceProductScode.ConnectAccelerationService.getServiceName();
        String sheetName= ServiceProductScode.ConnectAccelerationService.getShortServiceName();
        SellableProduct product1 = SellableProductFixture.aProduct().withId(productId).withName(productName).withSiteInstallable(false).withIsImportable(false).build();
        SalesCatalogue salesCatalogue = new SalesCatalogue();
        salesCatalogue.addSellableProduct(product1) ;
        when(pmrClient.getSalesCatalogue()).thenReturn(salesCatalogue);
        Pmr.ProductOfferings offerings = mock(Pmr.ProductOfferings.class);
        when(pmrClient.productOffering(ProductSCode.newInstance(productId))).thenReturn(offerings);
        ProductOffering aProductOffering = ProductOfferingFixture.aProductOffering().build();
        when(offerings.get()).thenReturn(aProductOffering);

        withIndirectUser();
        PricingSheetTestDataFixture pricingSheetTestDataFixture = new PricingSheetTestDataFixture();
        ProductInstance productInstance = pricingSheetTestDataFixture.aCAServiceProductInstanceWithOutPriceLines();
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
        PricingSheetDataModel dataModel= PricingSheetDataModelFixture.aPricingSheetModel().withPricingSheetProductModel(sheetProductModel).build();
        when(productInstanceClient.getLatestProduct(Matchers.<ProductInstanceId>anyObject(), anyString())).thenReturn(productInstance);
        bcmSheetFactory.createSiteServiceRootProductSheet(workbook, dataModel);
        assertThat(workbook.getSheetName(0), is(sheetName));
        assertThat(workbook.getSheet(sheetName).getLastRowNum(), is(0));

    }

    @Test
    public void shouldNotCreateServiceBasedRootProductSheet(){
        String productId= ServiceProductScode.ConnectAccelerationService.getsCode();
        String productName= ServiceProductScode.ConnectAccelerationService.getServiceName();
        String sheetName= ServiceProductScode.ConnectAccelerationService.getShortServiceName();
        SellableProduct product1 = SellableProductFixture.aProduct().withId(productId).withName(productName).withSiteInstallable(false).withIsImportable(false).build();
        SalesCatalogue salesCatalogue = new SalesCatalogue();
        salesCatalogue.addSellableProduct(product1) ;
        when(pmrClient.getSalesCatalogue()).thenReturn(salesCatalogue);
        Pmr.ProductOfferings offerings = mock(Pmr.ProductOfferings.class);
        when(pmrClient.productOffering(ProductSCode.newInstance(productId))).thenReturn(offerings);
        ProductOffering aProductOffering = ProductOfferingFixture.aProductOffering().build();
        when(offerings.get()).thenReturn(aProductOffering);

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
        PricingSheetDataModel dataModel= PricingSheetDataModelFixture.aPricingSheetModel().withPricingSheetProductModel(sheetProductModel).build();
        when(productInstanceClient.getLatestProduct(Matchers.<ProductInstanceId>anyObject(), anyString())).thenReturn(productInstance);
        bcmSheetFactory.createSiteServiceRootProductSheet(workbook,dataModel);
        assertNull(workbook.getSheet(sheetName));
    }

    @Test
    public void shouldNotCreateServiceBasedRootProductSheetWithOutShortName(){
        SellableProduct product1 = SellableProductFixture.aProduct().withId("rootProductScode2").withName("Root Product Name2").withSiteInstallable(false).withIsImportable(false).build();
        SalesCatalogue salesCatalogue = new SalesCatalogue();
        salesCatalogue.addSellableProduct(product1) ;
        when(pmrClient.getSalesCatalogue()).thenReturn(salesCatalogue);
        Pmr.ProductOfferings offerings = mock(Pmr.ProductOfferings.class);
        when(pmrClient.productOffering(ProductSCode.newInstance("rootProductScode2"))).thenReturn(offerings);
        when(pmrClient.getProductHCode("rootProductScode2")).thenReturn(Optional.of(new ProductIdentifier("H0300521","Connect Acceleration","1.0")));
        ProductOffering aProductOffering = ProductOfferingFixture.aProductOffering().build();
        when(offerings.get()).thenReturn(aProductOffering);

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
        PricingSheetDataModel dataModel= PricingSheetDataModelFixture.aPricingSheetModel().withPricingSheetProductModel(sheetProductModel).build();
        bcmSheetFactory.createSiteServiceRootProductSheet(workbook,dataModel);
        assertNull(workbook.getSheet("Connect Acceleration Service"));
    }

    @Test
    public void shouldCreateSiteBasedRootProductSheetBasedOnProductFamily(){
        SellableProduct caSiteSellableProduct = SellableProductFixture.aProduct().withId("S0308454").withName("Connect Acceleration Site").withSiteInstallable(true).build();
        SellableProduct camSiteSellableProduct = SellableProductFixture.aProduct().withId("S0320511").withName("Connect Acceleration Monitoring Site").withSiteInstallable(true).build();
        SalesCatalogue salesCatalogue = new SalesCatalogue();
        salesCatalogue.addSellableProduct(caSiteSellableProduct) ;
        salesCatalogue.addSellableProduct(camSiteSellableProduct) ;
        when(pmrClient.getSalesCatalogue()).thenReturn(salesCatalogue);
        Pmr.ProductOfferings offerings = mock(Pmr.ProductOfferings.class);
        when(pmrClient.productOffering(ProductSCode.newInstance(anyString()))).thenReturn(offerings);
        when(pmrClient.getProductHCode(anyString())).thenReturn(Optional.of(new ProductIdentifier("H0300521","CA","1.0")));
        ProductOffering aProductOffering = ProductOfferingFixture.aProductOffering().withSiteSpecific().build();
        when(offerings.get()).thenReturn(aProductOffering);

        String steelHead = "Steelhead";
        String steelCentral = "SteelCentral";
        withDirectUser();
        PricingSheetTestDataFixture pricingSheetTestDataFixture = new PricingSheetTestDataFixture();
        ProductInstance caProductInstance = pricingSheetTestDataFixture.aRootProductAndChildWithRelationshipNameProductInstance(caSiteSellableProduct, steelHead);
        ProductInstance camProductInstance = pricingSheetTestDataFixture.aRootProductAndChildWithRelationshipNameProductInstance(camSiteSellableProduct, steelCentral);
        QuoteOptionItemDTO itemDTO = QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().build();
        SiteDTO siteDTO = SiteDTOFixture.aSiteDTO()
                                        .withName("aSiteName")
                                        .withCity("aCity")
                                        .withCountry("aCountry")
                                        .withBfgSiteId("123").build();

        PricingSheetProductModel caSiteProductModel = PricingSheetProductModelFixture.aPricingSheetProductModel()
                                                                                    .withProductInstance(caProductInstance)
                                                                                    .withQuoteOptionItem(itemDTO)
                                                                                    .withSiteDTO(siteDTO)
                                                                                    .build();

        PricingSheetProductModel camSiteProductModel = PricingSheetProductModelFixture.aPricingSheetProductModel()
                                                                                     .withProductInstance(camProductInstance)
                                                                                     .withQuoteOptionItem(itemDTO)
                                                                                     .withSiteDTO(siteDTO)
                                                                                     .build();
        PricingSheetDataModel dataModel= PricingSheetDataModelFixture.aPricingSheetModel()
                                                                     .withPricingSheetProductModel(caSiteProductModel)
                                                                     .withPricingSheetProductModel(camSiteProductModel).build();
        when(productInstanceClient.getLatestProduct(caProductInstance.getProductInstanceId(), caProductInstance.getQuoteOptionId())).thenReturn(caProductInstance);
        when(productInstanceClient.getLatestProduct(camProductInstance.getProductInstanceId(), camProductInstance.getQuoteOptionId())).thenReturn(camProductInstance);

        bcmSheetFactory.createSiteServiceRootProductSheet(workbook,dataModel);
        assertEquals(workbook.getNumberOfSheets(),2);
        assertEquals(workbook.getSheetName(0), "CA Site");
        assertEquals(workbook.getSheetAt(0).getRow(1).getCell(6).toString(), steelCentral);
        assertEquals(workbook.getSheetAt(0).getRow(2).getCell(6).toString(), steelHead);
        assertEquals(workbook.getSheetName(1), "CA Site Management");
    }
}
