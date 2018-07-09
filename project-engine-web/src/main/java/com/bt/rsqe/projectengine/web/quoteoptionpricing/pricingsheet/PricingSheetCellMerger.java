package com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet;

import org.apache.poi.ss.usermodel.Sheet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.XlsFileUtils.*;
import static com.google.common.collect.Lists.*;

public abstract class PricingSheetCellMerger {
    public int serviceNameCount=0;
    public void mergeCellsForSheet(Sheet sheet){
         if(canIMerge(sheet)){
             mergeCells(sheet);
         }
    }

    boolean isValidForMerge(Sheet sheet, int rowFrom, int rowTo, int cellIndex) {
        String cellValue = getStringCellValue(sheet, rowFrom++, cellIndex);
        if(rowTo >= rowFrom){
        for(int rowIndex = rowFrom; rowIndex <= rowTo; rowIndex++) {
            if( !cellValue.equals(getStringCellValue(sheet, rowIndex, cellIndex) )) {
                return false;
            }
        }

        return true;
        }else{
            return false;
        }

    }
    boolean isValidForMergeForServiceName(Sheet sheet, int rowFrom, int rowTo, int cellIndex) {
           String cellValue = getStringCellValue(sheet, rowFrom++, cellIndex);
           if(rowTo >= rowFrom){
           for(int rowIndex = rowFrom; rowIndex <= rowTo; rowIndex++) {
               serviceNameCount=rowIndex;
               if(! cellValue.equals(getStringCellValue(sheet, rowIndex, cellIndex) )) {
                   serviceNameCount=rowIndex;
                   return true;

               }
           }
               serviceNameCount=serviceNameCount+1;
           return true;
           }else{
               return false;
           }

       }

    List<FromTo> rowsToMerge(Sheet sheet, int fromRow, int toRow, int cellIndex) {
        Map<String, FromTo> map = new HashMap<String, FromTo>();

        String previousCellValue = getStringCellValue(sheet, fromRow, cellIndex);
        map.put(previousCellValue, new FromTo(fromRow, fromRow));
        ++fromRow;

        for (int rowIndex = fromRow; rowIndex <= toRow; rowIndex++) {
            String currentCellValue = getStringCellValue(sheet,rowIndex,cellIndex);

            if (previousCellValue.equals(currentCellValue)) {
                if (!map.containsKey(currentCellValue)) {
                    map.put(currentCellValue, new FromTo(rowIndex-1, rowIndex));
                }
                map.get(currentCellValue).to(rowIndex);
            }
            previousCellValue = currentCellValue;
        }

        return newArrayList(map.values());
    }

    abstract void mergeCells(Sheet sheet);
    abstract boolean canIMerge(Sheet sheet);
}


