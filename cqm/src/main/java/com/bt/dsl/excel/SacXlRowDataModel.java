package com.bt.dsl.excel;

import com.bt.rsqe.ape.dto.sac.SacSiteDTO;

/**
 * Created with IntelliJ IDEA.
 * User: 607937181
 * Date: 01/09/15
 * Time: 13:05
 * To change this template use File | Settings | File Templates.
 */
public class SacXlRowDataModel {
   private String country;
   private String siteName;
   private String phoneNo;
   private SacXlAccessTechAndProductsDetailsMap sacXlAccessAndProductsMapDto;

    public SacXlRowDataModel() {
    }

    public SacXlRowDataModel(String country, String phoneNo, String siteName, SacXlAccessTechAndProductsDetailsMap accessTechnologyReportDto) {
        this.sacXlAccessAndProductsMapDto = accessTechnologyReportDto;
        this.country = country;
        this.phoneNo = phoneNo;
        this.siteName = siteName;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public SacXlAccessTechAndProductsDetailsMap getSacXlAccessAndProductsMapDto() {
        return sacXlAccessAndProductsMapDto;
    }

    public void setSacXlAccessAndProductsMapDto(SacXlAccessTechAndProductsDetailsMap sacXlAccessAndProductsMapDto) {
        this.sacXlAccessAndProductsMapDto = sacXlAccessAndProductsMapDto;
    }

  public static SacXlRowDataModel getInstance(SacSiteDTO sacSiteDTO){
      SacXlAccessTechAndProductsDetailsMap sacXlAcctechMapDto= SacXlAccessTechAndProductsDetailsMap.getInstance(sacSiteDTO.getSacSupplierProdAvailDTOs());
      return new SacXlRowDataModel(sacSiteDTO.getCountryName(), sacSiteDTO.getTelephoneNo(), sacSiteDTO.getSiteName(), sacXlAcctechMapDto);
  }
}
