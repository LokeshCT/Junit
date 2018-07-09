package com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet;

import net.sf.jxls.parser.Cell;
import net.sf.jxls.transformer.XLSTransformer;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Test;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.*;
import static org.junit.Assert.*;

public class PricingSheetCellProcessorTest {
    
    PricingSheetCellProcessor pricingSheetCellProcessor;
    Workbook workbook;

    Cell cell;
    

    @Test
    public void shouldGrayOutCells()throws Exception {
        Map<String, Object> beanNames = new HashMap<String, Object>();
        beanNames.put("CustomerName","");
        beanNames.put("BtSubsidiaryName","");
        beanNames.put("Address","some address");
        beanNames.put("city","some city");

        Map<String,Object> siteDTO = new HashMap<String, Object>();
        siteDTO.put("SiteName", "value1");
        siteDTO.put("SiteAddress", "value2");
        siteDTO.put("SiteCity", "value3");

        Map<String,Object> siteDTO1 = new HashMap<String, Object>();
        siteDTO1.put("SiteName", "value1");
        siteDTO1.put("SiteAddress", "value2");
        siteDTO1.put("SiteCity", "value3");

        Map<String,Object> siteDTO2 = new HashMap<String, Object>();
        siteDTO2.put("SiteName", "value1");
        siteDTO2.put("SiteAddress", "value2");
        siteDTO2.put("SiteCity", "value3");

        List<Map<String,Object>> sites = newArrayList();
        beanNames.put("Sites",sites);

        XLSTransformer transformer = new XLSTransformer();
        URL resource = getClass().getClassLoader().getResource("PricingSheetGrayCellTest.xls");
        transformer.registerCellProcessor(new PricingSheetCellProcessor(beanNames));
        workbook = transformer.transformXLS(resource.openStream(),beanNames);

        assertEquals(workbook.getSheetAt(0).getRow(0).getCell(0).getCellStyle().getFillBackgroundColor(), 64);
        
    }



}
