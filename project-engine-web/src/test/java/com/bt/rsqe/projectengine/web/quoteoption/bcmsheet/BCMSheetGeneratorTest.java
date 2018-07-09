package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;


import com.bt.rsqe.pmr.client.PmrClient;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class BCMSheetGeneratorTest {

    private BCMSheetGenerator bcmSheetGenerator;
    private HeaderRowModelFactory headerRowModelFactory;
    private HSSFWorkbook workbook;

    private final Mockery context = new JUnit4Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};

    @Before
    public void setUp(){
        bcmSheetGenerator = new BCMSheetGenerator();
        PmrClient pmrClient = context.mock(PmrClient.class);
        headerRowModelFactory = new HeaderRowModelFactory(pmrClient);
        workbook = new HSSFWorkbook();
    }

    @Test
    public void shouldGenerateBidInfoSheet(){
        HeaderRowModel headerRowModel = headerRowModelFactory.createBidInfoHeader();
        assertThat(headerRowModel.getHeaderRow().size(), is(12));
        Map<String, String> bidInfoData = new HashMap<String, String>();
        bidInfoData.put(BidInfoStaticColumn.QUOTE_ID.retrieveValueFrom,"12345");
        bidInfoData.put(BidInfoStaticColumn.BID_NUMBER.retrieveValueFrom, "123");
        bidInfoData.put(BidInfoStaticColumn.SHEET_VERSION_NO.retrieveValueFrom,"31.0");
        bidInfoData.put(BidInfoStaticColumn.OFFER_NAME.retrieveValueFrom,"Offer Name");
        List<Map<String,String>> dataRowModel =  newArrayList();
        dataRowModel.add(bidInfoData);
        bcmSheetGenerator.createBCMSheet(workbook, headerRowModel, dataRowModel, BCMConstants.BCM_BID_INFO_SHEET);
        HSSFRow row = workbook.getSheet(BCMConstants.BCM_BID_INFO_SHEET).getRow(1);
        assertThat(row.getCell(0).getStringCellValue(), is("12345"));
        assertThat(((HSSFRow) row).getCell(row.getLastCellNum()-1).getStringCellValue(), is("Offer Name"));
    }

    @Test
    public void shouldGenerateProductPerSiteSheet(){
        HeaderRowModel headerRowModel = headerRowModelFactory.createProductPerSiteHeader();
        assertThat(headerRowModel.getHeaderRow().size(), is(14));
        Map<String, String> ppsData = new HashMap<String, String>();
        ppsData.put(ProductPerSiteStaticColumn.SITE_ID.retrieveValueFrom, "123456");
        ppsData.put(ProductPerSiteStaticColumn.CITY.retrieveValueFrom, "aCity");
        ppsData.put(ProductPerSiteStaticColumn.COUNTRY.retrieveValueFrom, "aCountry");
        ppsData.put(ProductPerSiteStaticColumn.SITE_NAME.retrieveValueFrom, "aSite");
        ppsData.put(RootProductsCategory.CONNECT_INTELLIGENCE.retrieveValueFrom,"Y");
        List<Map<String,String>> dataRowModel =  newArrayList();
        dataRowModel.add(ppsData);
        bcmSheetGenerator.createBCMSheet(workbook, headerRowModel,dataRowModel, BCMConstants.BCM_PRODUCT_PER_SITE_SHEET);
        assertThat(workbook.getSheet(BCMConstants.BCM_PRODUCT_PER_SITE_SHEET).getRow(1).getCell(0).getNumericCellValue(), is(new Double(123456)));
        assertThat(workbook.getSheet(BCMConstants.BCM_PRODUCT_PER_SITE_SHEET).getRow(1).getCell(1).getStringCellValue(), is("aSite"));
    }


    @Test
    public void shouldGenerateSpecialBidInfoSheet(){
        HeaderRowModel headerRowModel = headerRowModelFactory.createSpecialBidInfoSheetHeader();
        assertThat(headerRowModel.getHeaderRow().size(), is(25));
        Map<String, String> sbrData = new HashMap<String, String>();
        sbrData.put(SpecialBidInfoStaticColumn.SITE_ID.retrieveValueFrom, "123456");
        sbrData.put(SpecialBidInfoStaticColumn.EUP_ONE_TIME.retrieveValueFrom, "20");
        sbrData.put(SpecialBidInfoStaticColumn.EUP_MONTHLY.retrieveValueFrom, "10");
        sbrData.put(SpecialBidInfoStaticColumn.DISCOUNT_ONE_TIME.retrieveValueFrom, "5.02");
        sbrData.put(SpecialBidInfoStaticColumn.DISCOUNT_MONTHLY.retrieveValueFrom, "2");
        sbrData.put(SpecialBidInfoStaticColumn.LINE_ITEM_ID.retrieveValueFrom, "aLineItem");
        sbrData.put(SpecialBidInfoStaticColumn.PRICING_STATUS.retrieveValueFrom, "Progressing");
        List<Map<String,String>> dataRowModel =  newArrayList();
        dataRowModel.add(sbrData);
        bcmSheetGenerator.createBCMSheet(workbook, headerRowModel,dataRowModel, BCMConstants.BCM_SPECIAL_BID_INFO_SHEET);
        assertThat(workbook.getSheet(BCMConstants.BCM_SPECIAL_BID_INFO_SHEET).getRow(1).getCell(0).getNumericCellValue(), is(new Double(123456)));
        assertThat(workbook.getSheet(BCMConstants.BCM_SPECIAL_BID_INFO_SHEET).getRow(1).getCell(1).getStringCellValue(), is("aLineItem"));
        assertThat(workbook.getSheet(BCMConstants.BCM_SPECIAL_BID_INFO_SHEET).getRow(1).getCell(24).getStringCellValue(), is("Progressing"));
    }

    @Test
    public void shouldGenerateSiteBasedRootProductSheet(){
        String sheetName = "CI Site";
        HeaderRowModel headerRowModel = headerRowModelFactory.createSiteBasedRootProductSheetHeader(sheetName);
        List<Map<String,String>> dataRowModel =  newArrayList();
        bcmSheetGenerator.createSiteBasedBCMSheet(workbook, headerRowModel,dataRowModel, sheetName);
        assertNotNull(workbook.getSheet(sheetName).getRow(0));
        bcmSheetGenerator.createSiteBasedBCMSheet(workbook, headerRowModel,dataRowModel, sheetName);
        assertThat(workbook.getNumberOfSheets(),is(2));

    }


    @Test
    public void shouldGenerateWPMOServiceSheet(){
        String sheetName = ServiceProductScode.ConnectIntelligenceWPMoService.getShortServiceName();
        HeaderRowModel headerRowModel = headerRowModelFactory.createServiceBasedRootProductSheetHeader(sheetName);
        List<Map<String,String>> dataRowModel =  newArrayList();
        bcmSheetGenerator.createServiceBasedBCMSheet(workbook, headerRowModel, dataRowModel, sheetName);
        assertNotNull(workbook.getSheet(sheetName).getRow(0));
    }

    @Test
    public void shouldGenerateAPMOServiceSheet(){
        String sheetName = ServiceProductScode.ConnectIntelligenceAPMoService.getShortServiceName();
        HeaderRowModel headerRowModel = headerRowModelFactory.createServiceBasedRootProductSheetHeader(sheetName);
        List<Map<String,String>> dataRowModel =  newArrayList();
        bcmSheetGenerator.createServiceBasedBCMSheet(workbook, headerRowModel, dataRowModel, sheetName);
        assertNotNull(workbook.getSheet(sheetName).getRow(0));
    }

    @Test
    public void shouldGenerateSiteManagementSheet(){
        String sheetName = "CI Site Management";
        HeaderRowModel headerRowModel = headerRowModelFactory.createSiteManagementSheetHeader(sheetName);
        List<Map<String,String>> dataRowModel =  newArrayList();
        bcmSheetGenerator.createSiteManagementBCMSheet(workbook, headerRowModel, dataRowModel, sheetName);
        assertNotNull(workbook.getSheet(sheetName).getRow(0));

        bcmSheetGenerator.createSiteManagementBCMSheet(workbook, headerRowModel, dataRowModel, sheetName);
        assertThat(workbook.getSheetName(1),is(sheetName+1));
    }

    @Test
    public void shouldGenerateSheetWithRestrictedLength(){
        String sheetName = "Central Management Controller Virtual Engine Steelhead Management Licence Pack";
        HeaderRowModel headerRowModel = headerRowModelFactory.createSiteManagementSheetHeader(sheetName);
        List<Map<String,String>> dataRowModel =  newArrayList();
        bcmSheetGenerator.createSiteManagementBCMSheet(workbook, headerRowModel, dataRowModel, sheetName);
        assertThat(workbook.getSheetName(0).length(),is(27));

        bcmSheetGenerator.createSiteManagementBCMSheet(workbook, headerRowModel, dataRowModel, sheetName);
        assertThat(workbook.getSheetName(1).length(),is(27));
        assertThat(workbook.getSheetName(1),is(sheetName.substring(0,26)+1));

    }

}
