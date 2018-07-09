package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;

import com.bt.rsqe.client.Pmr;
import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.domain.SalesCatalogue;
import com.bt.rsqe.domain.bom.parameters.ProductSCode;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.excel.ExcelMerge;
import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.pricing.PricingClient;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.PricingSheetTestDataFixture;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model.PricingSheetDataModel;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model.PricingSheetDataModelFactory;
import com.google.common.base.Optional;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.bt.rsqe.projectengine.web.quoteoption.bcmsheet.BCMProductSheetProperty.*;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.*;

public class ProductsBCMSheetFactoryTest {

    @Mock
    private BCMProductSheetGenerator mockBcmProductsSheetGenerator;

    @Mock
    private HeaderRowModelFactory mockHeaderRowModelFactory;

    @Mock
    private BCMDataRowModelFactory mockBcmDataRowModelFactory;

    @Mock
    private PricingSheetDataModelFactory mockPricingSheetDataModelFactory;

    @Mock
    private PmrClient mockPmrClient;

    @Mock
    private SalesCatalogue salesCatalogue;

    @Mock
    private Pmr.ProductOfferings caSiteProductOfferings;

    @Mock
    private Pmr.ProductOfferings caServiceProductOfferings;

    @Mock
    private Pmr.ProductOfferings specialBidProductOfferings;

    @Mock
    private Pmr.ProductOfferings contractProductOfferings;

    ProductsBCMSheetFactory productsBCMSheetFactory;
    @Mock
    private PricingClient pricingClient;
    @Mock
    private ProductInstanceClient productInstanceClient;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);

        productsBCMSheetFactory = new ProductsBCMSheetFactory(mockBcmProductsSheetGenerator,
                                                              mockHeaderRowModelFactory,
                                                              mockBcmDataRowModelFactory,
                                                              mockPricingSheetDataModelFactory,
                                                              mockPmrClient, pricingClient);
        List<ProductIdentifier> productIdentifiers = getRootProductIdentifiers();
        HeaderRowModelFactoryTest headerRowModelFactoryTest = new HeaderRowModelFactoryTest();
        when(mockPmrClient.getSalesCatalogue()).thenReturn(salesCatalogue);
        when(salesCatalogue.getAllSellableProductIdentifiers()).thenReturn(productIdentifiers);
        when(mockPmrClient.productOffering(ProductSCode.newInstance("caSiteScode"))).thenReturn(caSiteProductOfferings);
        when(caSiteProductOfferings.get()).thenReturn(headerRowModelFactoryTest.getCASiteProductOffering());
        when(mockPmrClient.productOffering(ProductSCode.newInstance("caServiceScode"))).thenReturn(caServiceProductOfferings);
        when(caServiceProductOfferings.get()).thenReturn(headerRowModelFactoryTest.getCAServiceProductOffering());
        when(mockPmrClient.productOffering(ProductSCode.newInstance("specialScode"))).thenReturn(specialBidProductOfferings);
        when(specialBidProductOfferings.get()).thenReturn(headerRowModelFactoryTest.getSpecialBidProductOffering());
        when(mockPmrClient.productOffering(ProductSCode.newInstance("aContractSCode"))).thenReturn(contractProductOfferings);
        when(contractProductOfferings.get()).thenReturn(headerRowModelFactoryTest.getContractProductOffering());
        Optional<ProductIdentifier> ConnectAcceleration = Optional.of(new ProductIdentifier("CACode", "Connect Acceleration", "someversion"));
        when(mockPmrClient.getProductHCode("caSiteScode")).thenReturn(ConnectAcceleration);
        when(mockPmrClient.getProductHCode("caServiceScode")).thenReturn(ConnectAcceleration);
        when(mockPmrClient.getProductHCode("specialScode")).thenReturn(ConnectAcceleration);
        when(mockPmrClient.getProductHCode("aContractSCode")).thenReturn(Optional.of(new ProductIdentifier("contractHCode", "Contract Family", "someversion")));
        PricingSheetDataModel dataModel = new PricingSheetTestDataFixture().pricingSheetSpecialBidTestData();
        when(mockPricingSheetDataModelFactory.create(any(String.class), any(String.class), any(String.class), any(Optional.class))).thenReturn(dataModel);

    }

    @Test
    public void shouldGetProductsForSheet() {
        Map<String, List<ProductIdentifier>> productsForSheet = productsBCMSheetFactory.getProductsForSheet();
        Assert.assertThat(productsForSheet.size(), is(4));
        Assert.assertThat(productsForSheet.get(SiteAgnostic.sheetName).size(), is(1));
        Assert.assertThat(productsForSheet.get(SiteInstallable.sheetName).size(), is(1));
        Assert.assertThat(productsForSheet.get(SpecialBid.sheetName).size(), is(1));
        Assert.assertThat(productsForSheet.get(Contract.sheetName).size(), is(1));
        Assert.assertThat(productsForSheet.get(SiteAgnostic.sheetName).get(0).getProductId(), is("caServiceScode"));
        Assert.assertThat(productsForSheet.get(SiteInstallable.sheetName).get(0).getProductId(), is("caSiteScode"));
        Assert.assertThat(productsForSheet.get(SpecialBid.sheetName).get(0).getProductId(), is("specialScode"));
        Assert.assertThat(productsForSheet.get(Contract.sheetName).get(0).getProductId(), is("aContractSCode"));
    }

    @Test
    public void shouldCreateSpecialBidSheet() {
        ProductsBCMSheetFactory productsBCMSheetFactory = new ProductsBCMSheetFactory(new BCMProductSheetGenerator(),
                                                                                      new HeaderRowModelFactory(mockPmrClient),
                                                                                      new BCMDataRowModelFactory(productInstanceClient),
                                                                                      mockPricingSheetDataModelFactory,
                                                                                      mockPmrClient, pricingClient);
        HSSFWorkbook workbook = ExcelMerge.merge("BCM-Details.xls", null, null);
        productsBCMSheetFactory.createProductSheets(workbook, "customerId", "projectId", "quoteOptionId");
        final HSSFSheet sheet = workbook.getSheet(BCMProductSheetProperty.SpecialBid.sheetName);
        assertNotNull(sheet.getRow(1));
    }

    @Test
    public void shouldCreateContractSheet() throws Exception {
        PricingSheetDataModel dataModel = new PricingSheetTestDataFixture().pricingSheetContractTestData();
        when(mockPricingSheetDataModelFactory.create("customerId", "projectId", "quoteOptionId", Optional.<String>absent())).thenReturn(dataModel);

        ProductsBCMSheetFactory productsBCMSheetFactory = new ProductsBCMSheetFactory(new BCMProductSheetGenerator(),
                                                                                      new HeaderRowModelFactory(mockPmrClient),
                                                                                      new BCMDataRowModelFactory(productInstanceClient),
                                                                                      mockPricingSheetDataModelFactory,
                                                                                      mockPmrClient, pricingClient);
        HSSFWorkbook workbook = ExcelMerge.merge("BCM-Details.xls", null, null);
        productsBCMSheetFactory.createProductSheets(workbook, "customerId", "projectId", "quoteOptionId");
        final HSSFSheet sheet = workbook.getSheet(BCMProductSheetProperty.Contract.sheetName);
        assertNotNull(sheet.getRow(1));
    }

    private List<ProductIdentifier> getRootProductIdentifiers() {
        List<ProductIdentifier> productIdentifiers = new ArrayList<ProductIdentifier>();
        productIdentifiers.add(new ProductIdentifier("caSiteScode", "Connection Acceleration Site"));
        productIdentifiers.add(new ProductIdentifier("caServiceScode", "Connection Acceleration Service"));
        productIdentifiers.add(new ProductIdentifier("specialScode", "Special Bid"));
        productIdentifiers.add(new ProductIdentifier("aContractSCode", "Contract"));
        return productIdentifiers;
    }
}
