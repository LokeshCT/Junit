package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;

public class HeaderRowModelTest {

    private HeaderRowModel headerRowModel;

    @Before
    public void setup(){
        List<HeaderCell> headerCells = new ArrayList<HeaderCell>();
        headerCells.add(new HeaderCell(0,"column1",true,"site","",0, 1));
        headerCells.add(new HeaderCell(1,"column2",true,"site","",0, 1));
        headerCells.add(new HeaderCell(2,"column3",true,"price","",0, 1));
        headerCells.add(new HeaderCell(3,"column4",true,"site","",0, 1));
        headerCells.add(new HeaderCell(4,"column5",true,"product","",0, 1));
        headerRowModel = new HeaderRowModel(headerCells);
    }

    @Test
    public void shouldReturnHeaderCellBasedOnGroupName(){
        List<HeaderCell> columnsBySiteAndPrice = headerRowModel.getColumnsByGroupName("site", "price");
        List<HeaderCell> columnsByProduct = headerRowModel.getColumnsByGroupName("product");
        List<HeaderCell> columnsByAttribute = headerRowModel.getColumnsByGroupName("attribute");
        Assert.assertThat(columnsBySiteAndPrice.size(), is(4));
        Assert.assertThat(columnsByProduct.size(), is(1));
        Assert.assertThat(columnsByAttribute.size(), is(0));
    }

    @Test
    public void shouldGetCorrectColumnIndex(){
        int column4 = headerRowModel.getCellIndexFor("column4");
        int column1 = headerRowModel.getCellIndexFor("column1");
        int column6 = headerRowModel.getCellIndexFor("column6");
        assertThat(column4, is(3));
        assertThat(column1, is(0));
        assertThat(column6, is(-1));
    }

    @Test
    public void shouldReturnEmptyListOfSheets(){
        assertThat(headerRowModel.getRequiredSheets().size(), is(0));
    }

    @Test
    public void shouldReturnNewRowOfSheets(){
        HSSFWorkbook workbook = new HSSFWorkbook();
        headerRowModel.requiredSheets.add(workbook.createSheet());
        assertNotNull(headerRowModel.getRowFor(0,0));
    }

    @Test
    public void shouldGenerateSheetsBasedOnHeaderSize() throws Exception{
        List<HeaderCell> headerCells = new ArrayList<HeaderCell>();
        HSSFWorkbook workbook = new HSSFWorkbook(getClass().getResourceAsStream("BCM-Products-Test.xls"));
        int currentSheetSize = workbook.getNumberOfSheets();
        headerRowModel.generateSheetsBasedOnHeaderSize(workbook, "test");
        assertThat(workbook.getNumberOfSheets(), is(currentSheetSize));

        for(int i=0; i<257;i++){
            headerCells.add(new HeaderCell(i,"column1",true,"site","",0, 1));
        }
        headerRowModel = new HeaderRowModel(headerCells);
        headerRowModel.generateSheetsBasedOnHeaderSize(workbook, "test");
        assertThat(workbook.getNumberOfSheets(), is(currentSheetSize+1));
        assertNotNull(workbook.getSheet("test_1"));
    }
}
