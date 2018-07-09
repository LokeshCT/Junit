package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;


import com.bt.rsqe.customerinventory.parameter.RandomSiteId;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.bom.fixtures.ProductOfferingFixture;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.project.DefaultProductInstance;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.enums.ProductCodes;
import com.bt.rsqe.security.UserContext;
import com.bt.rsqe.security.UserContextBuilder;
import com.bt.rsqe.security.UserContextManager;
import com.bt.rsqe.util.excel.ExcelSheetComparator;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.*;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;

public class BCMProductSheetGeneratorTest {
    private static String[] STANDARD_GROUPS = {Groups.site.name(), Groups.common.name(), Groups.price.name()};
    private SiteDTO site;
    private ProductInstance productInstance;
    private BCMProductSheetGenerator bcmProductSheetGenerator;
    private BCMProductsSheetTestDataFixture fixture;
    HSSFWorkbook workbook;
    UserContext userContext;

    @Before
    public void setUp() throws IOException {
        bcmProductSheetGenerator = new BCMProductSheetGenerator();
        site = new SiteDTO(new RandomSiteId().value(),"siteName");
        ProductOffering productOffering = new ProductOfferingFixture().withProductIdentifier(ProductCodes.ConnectAccelerationService.productCode()).build();
        productInstance = new DefaultProductInstance(productOffering);
        fixture = new BCMProductsSheetTestDataFixture();
        InputStream resourceAsStream = getClass().getResourceAsStream("BCM-Products-Test.xls");
        workbook = new HSSFWorkbook(resourceAsStream);
        userContext = UserContextBuilder.aDirectUserContext().build();
        UserContextManager.setCurrent(userContext);
    }

    @Test
    public void shouldRetrieveValueFromGivenObjectWithGivenString(){
        Object name = bcmProductSheetGenerator.retrieveValue("name", site);
        String site1 = name == null ? "" : name.toString() ;
        String code = bcmProductSheetGenerator.retrieveValue("ProductOffering.ProductIdentifier.ProductId", productInstance).toString();
        assertThat(site1, is("siteName"));
        assertThat(code, is("S0308491"));
    }

    @Test
    public void shouldCreateHeader() throws Exception{
        HeaderRowModel headerModel = fixture.createHeaderModel();
        headerModel.requiredSheets.add(workbook.getSheet("Create Header"));
        bcmProductSheetGenerator.createHeader(headerModel);
        ExcelSheetComparator excelSheetComparator = new ExcelSheetComparator(workbook.getSheet("Create Header"), workbook.getSheet("Expected Create Header"), new ArrayList<Integer>(), true, false, true);
        List<String> errors = new ArrayList<String>();
        errors.addAll(excelSheetComparator.compare());
        Assert.assertThat(errors.size(), is(0));
    }

    @Ignore("Refactored")
    @Test
    public void shouldCreateHeaderWithProduct() throws Exception{
        HeaderRowModel headerModel = fixture.aHeaderModelWithOneProduct();
        headerModel.requiredSheets.add(workbook.getSheet("Header with Product"));
        bcmProductSheetGenerator.createHeader(headerModel);
        ExcelSheetComparator excelSheetComparator = new ExcelSheetComparator(workbook.getSheet("Header with Product"), workbook.getSheet("Header with Product-Ex"), new ArrayList<Integer>(), true, false, true);
        List<String> errors = new ArrayList<String>();
        errors.addAll(excelSheetComparator.compare());
        Assert.assertThat(errors.size(), is(0));
    }

    @Test
    public void shouldCreateSheetWithOneSiteAndPrice() throws Exception{
        BCMDataRowModel rowModel = fixture.aDataModelWithOneAggregatedPrice();
        HeaderRowModel headerRowModel = fixture.aHeaderModelWithOneSiteAndAggregatedPrice();
        HSSFSheet sheet = bcmProductSheetGenerator.createSheet(workbook, headerRowModel, newArrayList(rowModel), "shouldCreateSheetWithSingleSite", 4, STANDARD_GROUPS);

        assertThat(sheet.getLastRowNum(), is(1));
        assertThat((int)sheet.getRow(0).getLastCellNum(), is(5));
        assertThat(sheet.getRow(0).getCell(0).getStringCellValue(), is("SiteID"));
        assertThat(sheet.getRow(0).getCell(1).getStringCellValue(), is("Site Name"));
        assertThat(sheet.getRow(0).getCell(2).getStringCellValue(), is("Price description"));
        assertThat(sheet.getRow(0).getCell(3).getStringCellValue(), is("One time EUP price"));
        assertThat(sheet.getRow(0).getCell(4).getStringCellValue(), is("Monthly Recurring EUP price"));
        assertThat(sheet.getRow(1).getCell(0).getStringCellValue(), is(fixture.siteId()));
        assertThat(sheet.getRow(1).getCell(1).getStringCellValue(), is("sitename"));
        assertThat(sheet.getRow(1).getCell(2).getStringCellValue(), is("Root Product One time price"));
        assertThat(sheet.getRow(1).getCell(3).getStringCellValue(), is("333.00"));
        assertThat(sheet.getRow(1).getCell(4).getStringCellValue(), is("331.00"));
    }

    @Test
    public void shouldCreateSheetWithOneSiteAndCost() throws Exception{
        BCMDataRowModel rowModel = fixture.aDataModelWithOneCostPrice();
        HeaderRowModel headerRowModel = fixture.aHeaderModelWithOneSiteAndCostPrice();
        HSSFSheet sheet = bcmProductSheetGenerator.createSheet(workbook, headerRowModel, newArrayList(rowModel), "ConnectAccelerationSite", 4, STANDARD_GROUPS);

        assertThat(sheet.getLastRowNum(), is(1));
        assertThat((int)sheet.getRow(0).getLastCellNum(), is(12));
        assertThat(sheet.getRow(0).getCell(0).getStringCellValue(), is("SiteID"));
        assertThat(sheet.getRow(0).getCell(1).getStringCellValue(), is("Site Name"));
        assertThat(sheet.getRow(0).getCell(2).getStringCellValue(), is("Price description"));
        assertThat(sheet.getRow(0).getCell(3).getStringCellValue(), is("One time EUP price"));
        assertThat(sheet.getRow(0).getCell(4).getStringCellValue(), is("One time PTP price"));
        assertThat(sheet.getRow(0).getCell(5).getStringCellValue(), is("One time Discount"));
        assertThat(sheet.getRow(0).getCell(6).getStringCellValue(), is("Monthly Recurring EUP price"));
        assertThat(sheet.getRow(0).getCell(7).getStringCellValue(), is("Monthly Recurring PTP price"));
        assertThat(sheet.getRow(1).getCell(0).getStringCellValue(), is(fixture.siteId()));
        assertThat(sheet.getRow(1).getCell(1).getStringCellValue(), is("sitename"));
        assertThat(sheet.getRow(1).getCell(2).getStringCellValue(), is("Root Product One time price"));
        assertThat(sheet.getRow(1).getCell(3).getStringCellValue(), is("332.00"));
        assertThat(sheet.getRow(1).getCell(5).getNumericCellValue(), is(0.0));
        assertThat(sheet.getRow(1).getCell(6).getStringCellValue(), is("334.00"));
        assertThat(sheet.getRow(1).getCell(7).getStringCellValue(), is("50.00"));

    }

    @Test
    public void shouldCreateSheetWithProducts() throws Exception{
        workbook = new HSSFWorkbook(getClass().getResourceAsStream("BCM-Products-Test.xls"));
        BCMDataRowModel rowModel = fixture.aDataModelWithOneProduct();
        BCMDataRowModel rowModel1 = fixture.aDataModelWithAnotherProduct();
        HeaderRowModel headerRowModel = fixture.aHeaderModelWithOneProduct();
        bcmProductSheetGenerator.createSheet(workbook, headerRowModel, newArrayList(rowModel,rowModel1), "ProductSheet", 4, STANDARD_GROUPS);
        workbook.write(new FileOutputStream(getClass().getResource("BCM-Products-TestFile.xls").getFile()));
        HSSFWorkbook bookToCompare = new HSSFWorkbook(getClass().getResourceAsStream("BCM-Products-TestFile.xls"));
        ExcelSheetComparator comparator = new ExcelSheetComparator(workbook.getSheet("ProductSheet"),bookToCompare.getSheet("ProductSheet"),new ArrayList<Integer>(),true, false, true);
        List<String> errors = comparator.compare();
        assertThat(errors.size(),is(0));
    }
}
