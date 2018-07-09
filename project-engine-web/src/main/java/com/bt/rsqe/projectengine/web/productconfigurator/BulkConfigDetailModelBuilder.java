package com.bt.rsqe.projectengine.web.productconfigurator;


import com.bt.rsqe.projectengine.web.productconfigurator.model.BulkConfigAttribute;
import com.bt.rsqe.projectengine.web.productconfigurator.model.BulkConfigAttributeGroup;
import com.bt.rsqe.projectengine.web.productconfigurator.model.BulkConfigAttributeList;
import com.bt.rsqe.projectengine.web.productconfigurator.model.BulkConfigDataModel;
import com.bt.rsqe.projectengine.web.productconfigurator.model.BulkConfigDetailModel;
import com.bt.rsqe.projectengine.web.productconfigurator.model.BulkConfigSiteModel;
import com.bt.rsqe.projectengine.web.productconfigurator.model.BulkConfigSummaryModel;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;
import java.util.Map;

import static com.bt.rsqe.utils.AssertObject.*;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static org.apache.cxf.common.util.StringUtils.isEmpty;

public class BulkConfigDetailModelBuilder {

    public BulkConfigDataModel buildBulkSheetDataModel(Workbook bulkSheet) {
        BulkConfigSummaryModel headerModel = createHeaderModel(bulkSheet);
        List<BulkConfigDetailModel> detailModel = createDetailModel(bulkSheet);

        return new BulkConfigDataModel(headerModel, detailModel);
    }

    private List<BulkConfigDetailModel> createDetailModel(Workbook bulkSheet) {

        List<BulkConfigDetailModel> bulkConfigDetailModels = newArrayList();

        int numberOfSheets = bulkSheet.getNumberOfSheets() - 1;
        for (int sheetNumber = 1; sheetNumber <= numberOfSheets; sheetNumber++) {  //ignore header sheet & hidden sheet
            Sheet productSheet = bulkSheet.getSheetAt(sheetNumber);
            if (!productSheet.getSheetName().equals("Hidden")) {
                BulkConfigDetailModel bulkConfigDetailModel = new BulkConfigDetailModel(productSheet.getSheetName());

                Map<Integer, String> cellRangeMap = newHashMap();
                Row firstRow = productSheet.getRow(0);
                short lastCellNum = firstRow.getLastCellNum();
                for (int colNum = 0; colNum <= lastCellNum - 1; colNum++) {
                    Cell cell = firstRow.getCell(colNum);
                    String colValue = cell.getStringCellValue();
                    if (!isEmpty(colValue)) {
                        //BulkConfigAttributeGroup bulkConfigAttributeGroup = new BulkConfigAttributeGroup(colValue);
                        cellRangeMap.put(colNum, colValue);
                    }
                }

                Row attributeNameRow = productSheet.getRow(4);
                Row metaDataRow = productSheet.getRow(3);

                for (int rowNum = 5; rowNum <= productSheet.getLastRowNum(); rowNum++) {
                    Row detailRow = productSheet.getRow(rowNum);
                    BulkConfigSiteModel bulkConfigSiteModel = new BulkConfigSiteModel(detailRow.getCell(0).getStringCellValue(),
                                                                                      detailRow.getCell(1).getStringCellValue(),
                                                                                      detailRow.getCell(2).getStringCellValue(),
                                                                                      detailRow.getCell(3).getStringCellValue(),
                                                                                      detailRow.getCell(4).getStringCellValue());

                    BulkConfigAttributeGroup bulkConfigAttributeGroup = null;
                    for (int cellNum = 5; cellNum < attributeNameRow.getLastCellNum(); cellNum++) {
                        final String attributeGroupName = cellRangeMap.get(cellNum);
                        if (isNotNull(attributeGroupName)) {
                            bulkConfigAttributeGroup = new BulkConfigAttributeGroup(attributeGroupName, Enum.valueOf(BulkConfigAttributeGroup.GroupType.class, metaDataRow.getCell(cellNum).getStringCellValue()));
                            bulkConfigDetailModel.addAttributeGroupForSite(bulkConfigSiteModel, bulkConfigAttributeGroup);
                        }
                        final Cell cell = detailRow.getCell(cellNum);
                        BulkConfigAttribute bulkConfigAttribute = new BulkConfigAttribute(attributeNameRow.getCell(cellNum).getStringCellValue(), isNotNull(cell) ? cell.getStringCellValue() : null);
                        bulkConfigAttributeGroup.getAttributeList().addAttribute(bulkConfigAttribute);
                    }

                }

                bulkConfigDetailModels.add(bulkConfigDetailModel);
            }
        }

        return bulkConfigDetailModels;
    }

    private BulkConfigSummaryModel createHeaderModel(Workbook bulkSheet) {
        Sheet headerSheet = bulkSheet.getSheet("Header");
        BulkConfigAttributeList bulkConfigAttributeList = new BulkConfigAttributeList();

        int lastRowNum = headerSheet.getLastRowNum();
        for (int rowNum = 1; rowNum <= lastRowNum; rowNum++) {
            Row row = headerSheet.getRow(rowNum);
            String name = row.getCell(0).getStringCellValue();
            String value = row.getCell(1).getStringCellValue();
            BulkConfigAttribute bulkConfigAttribute = new BulkConfigAttribute(name, value);
            bulkConfigAttributeList.addAttribute(bulkConfigAttribute);
        }

        return new BulkConfigSummaryModel("Header", bulkConfigAttributeList);
    }

}
