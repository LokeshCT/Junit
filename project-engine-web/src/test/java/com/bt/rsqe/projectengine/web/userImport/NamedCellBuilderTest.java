package com.bt.rsqe.projectengine.web.userImport;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Test;

import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.List;

public class NamedCellBuilderTest {

/*    @Test
    public void constructNamedCell() {
        HSSFWorkbook workbook = new HSSFWorkbook();
        ListValidationBuilder builder = new ListValidationBuilder(workbook);
        List<String> listOfStringValues = Arrays.asList("a", "b", "c", "d", "e", "f", "g");
        List<String> listOfIntegerValues = Arrays.asList("1", "2", "3", "4", "5", "6", "7");
        List<String> listOfBooleanValues = Arrays.asList("Yes", "No");
        builder.buildHiddenCell(listOfStringValues, "Strings");
        builder.buildHiddenCell(listOfIntegerValues, "Integers");
        builder.buildHiddenCell(listOfBooleanValues, "Booleans");
        write(workbook, "namedComplete.xls");
    }*/

    @SuppressWarnings("unused")
    private void write(Workbook workbook, String fileName) {
        FileOutputStream out;
        try {
            out = new FileOutputStream("C:\\Users\\607118528\\Suyambu\\" + fileName+String.valueOf(System.currentTimeMillis()));
            workbook.write(out);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
