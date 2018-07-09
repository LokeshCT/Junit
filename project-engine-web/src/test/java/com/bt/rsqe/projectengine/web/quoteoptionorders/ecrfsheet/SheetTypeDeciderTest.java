package com.bt.rsqe.projectengine.web.quoteoptionorders.ecrfsheet;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.After;
import org.junit.Test;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static junit.framework.Assert.*;
import static org.mockito.Mockito.*;

public class SheetTypeDeciderTest {
    SheetTypeDecider testDecider;
    @Test
    public void shouldReturnOnlyIsParentAsTrue(){
        Map<String, String> sheetNameToSCode = newHashMap();
        sheetNameToSCode.put("Root Product", "sCode");

        Sheet sheet = mock(Sheet.class);
        Row headerRow = mock(Row.class);
        Cell cell = mock(Cell.class);
        when(sheet.getRow(0)).thenReturn(headerRow);
        when(sheet.getSheetName()).thenReturn("Root Product");
        when(headerRow.getLastCellNum()).thenReturn(new Short(String.valueOf(1)));
        when(headerRow.getCell(0)).thenReturn(cell);
        when(cell.toString()).thenReturn(ECRFSheetModelRow.SHEET_ID);

        SheetTypeDecider decider = new SheetTypeDecider(sheet, sheetNameToSCode);
        assertTrue(decider.getSheetType().equals(SheetTypeStrategy.Parent));
        assertFalse(decider.getSheetType().equals(SheetTypeStrategy.Child));
        assertFalse(decider.getSheetType().equals(SheetTypeStrategy.NonProduct));
        assertFalse(decider.getSheetType().equals(SheetTypeStrategy.Related));
        this.testDecider = decider;
    }

    @Test
    public void shouldReturnOnlyIsChildAsTrue(){
        Map<String, String> sheetNameToSCode = newHashMap();
        sheetNameToSCode.put("Root Product", "sCode");

        Sheet sheet = mock(Sheet.class);
        Row headerRow = mock(Row.class);
        Cell cell = mock(Cell.class);
        when(sheet.getRow(0)).thenReturn(headerRow);
        when(sheet.getSheetName()).thenReturn("Root Product");
        when(headerRow.getLastCellNum()).thenReturn(new Short(String.valueOf(1)));
        when(headerRow.getCell(0)).thenReturn(cell);
        when(cell.toString()).thenReturn(ECRFSheetModelRow.SHEET_PARENT_ID);

        SheetTypeDecider decider = new SheetTypeDecider(sheet, sheetNameToSCode);
        assertFalse(decider.getSheetType().equals(SheetTypeStrategy.Parent));
        assertTrue(decider.getSheetType().equals(SheetTypeStrategy.Child));
        assertFalse(decider.getSheetType().equals(SheetTypeStrategy.NonProduct));
        assertFalse(decider.getSheetType().equals(SheetTypeStrategy.Related));
        this.testDecider = decider;
    }

    @Test
    public void shouldReturnOnlyIsRelatedAsTrue(){
        Map<String, String> sheetNameToSCode = newHashMap();
        sheetNameToSCode.put(ECRFSheet.RELATED_TO_PRODUCT_SHEET, "sCode");

        Sheet sheet = mock(Sheet.class);
        Row headerRow = mock(Row.class);
        Cell cell = mock(Cell.class);
        when(sheet.getRow(0)).thenReturn(headerRow);
        when(sheet.getSheetName()).thenReturn(ECRFSheet.RELATED_TO_PRODUCT_SHEET);
        when(headerRow.getLastCellNum()).thenReturn(new Short(String.valueOf(1)));
        when(headerRow.getCell(0)).thenReturn(cell);
        when(cell.toString()).thenReturn("someRow");

        SheetTypeDecider decider = new SheetTypeDecider(sheet, sheetNameToSCode);
        assertFalse(decider.getSheetType().equals(SheetTypeStrategy.Parent));
        assertFalse(decider.getSheetType().equals(SheetTypeStrategy.Child));
        assertFalse(decider.getSheetType().equals(SheetTypeStrategy.NonProduct));
        assertTrue(decider.getSheetType().equals(SheetTypeStrategy.Related));
        this.testDecider = decider;
    }

    @Test
    public void shouldReturnOnlyIsNonProductSheetAsTrue(){
        Map<String, String> sheetNameToSCode = newHashMap();
        sheetNameToSCode.put("someId", "sCode");

        Sheet sheet = mock(Sheet.class);
        Row headerRow = mock(Row.class);
        Cell cell = mock(Cell.class);
        when(sheet.getRow(0)).thenReturn(headerRow);
        when(sheet.getSheetName()).thenReturn("someSheet");
        when(headerRow.getLastCellNum()).thenReturn(new Short(String.valueOf(1)));
        when(headerRow.getCell(0)).thenReturn(cell);
        when(cell.toString()).thenReturn("someRow");

        SheetTypeDecider decider = new SheetTypeDecider(sheet, sheetNameToSCode);
        assertFalse(decider.getSheetType().equals(SheetTypeStrategy.Parent));
        assertFalse(decider.getSheetType().equals(SheetTypeStrategy.Child));
        assertTrue(decider.getSheetType().equals(SheetTypeStrategy.NonProduct));
        assertFalse(decider.getSheetType().equals(SheetTypeStrategy.Related));
        this.testDecider = decider;
    }

    @Test
    public void shouldNotReturnAnySheetStrategyWhenAllScenarioPassed(){
        Map<String, String> sheetNameToSCode = newHashMap();
        sheetNameToSCode.put("Root Product", "sCode");

        Sheet sheet = mock(Sheet.class);
        Row headerRow = mock(Row.class);
        Cell cell = mock(Cell.class);
        Cell cell2 = mock(Cell.class);
        when(sheet.getRow(0)).thenReturn(headerRow);
        when(sheet.getSheetName()).thenReturn(ECRFSheet.RELATED_TO_PRODUCT_SHEET);
        when(headerRow.getLastCellNum()).thenReturn(new Short(String.valueOf(2)));
        when(headerRow.getCell(0)).thenReturn(cell);
        when(headerRow.getCell(1)).thenReturn(cell2);
        when(cell.toString()).thenReturn(ECRFSheetModelRow.SHEET_PARENT_ID);
        when(cell2.toString()).thenReturn(ECRFSheetModelRow.SHEET_ID);

        SheetTypeDecider decider = new SheetTypeDecider(sheet, sheetNameToSCode);
        assertNull(decider.getSheetType());
    }

    @Test
    public void shouldNotThrowExceptionButReturnNullSheetStrategyWhenSheetIsNull(){
        Map<String, String> sheetNameToSCode = null;
        Sheet sheet = null;

        SheetTypeDecider decider = new SheetTypeDecider(sheet, sheetNameToSCode);
        assertNull(decider.getSheetType());
    }

    @After
    public void shouldNotReturnTrueForMoreThanOneAttributeForASpecificScenario() {
        int trueConditions = 0;
        if(null != testDecider){
        if (testDecider.getSheetType().equals(SheetTypeStrategy.Parent)) {
            trueConditions++;
        }
        if (testDecider.getSheetType().equals(SheetTypeStrategy.Child)) {
            trueConditions++;
        }
        if (testDecider.getSheetType().equals(SheetTypeStrategy.NonProduct)) {
            trueConditions++;
        }
        if (testDecider.getSheetType().equals(SheetTypeStrategy.Related)) {
            trueConditions++;
        }
        assertTrue(trueConditions <= 1);
        }
    }
}
