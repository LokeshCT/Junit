package com.bt.dsl.excel;

import com.bt.rsqe.ape.dto.sac.SacSiteDTO;
import com.bt.rsqe.utils.AssertObject;

/**
 * Created with IntelliJ IDEA.
 * User: 607937181
 * Date: 26/08/15
 * Time: 21:14
 * To change this template use File | Settings | File Templates.
 */
public class DslImportRow extends ExcelRow {

    @ExcelColumnAnnotation(columnName = "Sr. No.",columnSize = 10, columnPosition = 0)
    public String srNo;

    @ExcelColumnAnnotation(columnName = "Site Name (Optional)",columnSize = 10, columnPosition = 1)
    public String siteName;
    @ExcelColumnAnnotation(columnName = "Telephone Number (Mandatory)",columnSize = 10, columnPosition = 2)
    public String telephoneNo;
    @ExcelColumnAnnotation(columnName = "Country",columnSize = 10, columnPosition = 3)
    public String country;




    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getSrNo() {
        return srNo;
    }

    public void setSrNo(String srNo) {
        this.srNo = srNo;
    }

    public String getTelephoneNo() {
        return telephoneNo;
    }

    public void setTelephoneNo(String telephoneNo) {
        this.telephoneNo = telephoneNo;
    }


    @Override
    public XLCellStyle getCellFormat(int colIndx, String value) {
       return null;
    }

    public SacSiteDTO toDto(){
      return new SacSiteDTO(siteName,country,telephoneNo);
    }

    @Override
    public boolean isRowEmpty() {
        return AssertObject.areEmpty(srNo,siteName,telephoneNo,country);
    }
}
