package com.bt.dsl.excel;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Font;


/**
 * Created with IntelliJ IDEA.
 * User: 607937181
 * Date: 27/08/15
 * Time: 18:01
 * To change this template use File | Settings | File Templates.
 */
public class UploadFailureRow extends DslImportRow {
    @ExcelColumnAnnotation(columnName = "Validation  Result", columnSize = 20, columnPosition = 4)
    public String validationStatus;
    @ExcelColumnAnnotation(columnName = "Reason for Failure",columnSize = 100, columnPosition = 5)
    public String failureReason;

    public UploadFailureRow() {
    }

    public UploadFailureRow(DslImportRow dslImportRow) {
        this.setSrNo(dslImportRow.getSrNo());
        this.setSiteName(dslImportRow.getSiteName());
        this.setTelephoneNo(dslImportRow.getTelephoneNo());
        this.setCountry(dslImportRow.getCountry());
    }


    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public String getValidationStatus() {
        return validationStatus;
    }

    public void setValidationStatus(String validationStatus) {
        this.validationStatus = validationStatus;
    }

    @Override
    public XLCellStyle getCellFormat(int colIndx, String value) {
        if(colIndx == 4 && value != null && value.equalsIgnoreCase("Failed")  ){
            XLCellStyle xlCellStyle = new XLCellStyle();
            xlCellStyle.setFontColor(HSSFColor.RED.index);
            xlCellStyle.setFontStyle(Font.BOLDWEIGHT_BOLD);
            return xlCellStyle;
            }
        if(colIndx == 5 && value != null && !value.trim().equals("") && !value.trim().equals("Reason for Failure".trim())  ){
            XLCellStyle xlCellStyle = new XLCellStyle();
            xlCellStyle.setFontColor(HSSFColor.RED.index);
            return xlCellStyle;
        }
        return null;
    }

    @Override
    public boolean equals(Object o){
        return this.telephoneNo.equals(((UploadFailureRow)o).getTelephoneNo());
    }
}
