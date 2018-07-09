package com.bt.dsl.excel;

import com.bt.dsl.excel.constant.ApplicabilityEnum;
import com.bt.rsqe.ape.dto.sac.SacSupplierProdDetailDTO;

/**
 * Created with IntelliJ IDEA.
 * User: 607937181
 * Date: 31/08/15
 * Time: 19:20
 * To change this template use File | Settings | File Templates.
 */
public class SacXlSupplierProductDto implements Comparable<SacXlSupplierProductDto> {
    private String spacId;
    private String accessSpeed;
    private String accessSpeedUnit;
    private String supplierName;
    private String noOfCopperPairs;
    private String supplierProductName;
    private String supplierProductDispName;
    private ApplicabilityEnum applicability;

    public SacXlSupplierProductDto() {
    }

    public SacXlSupplierProductDto(String supplierName, String spacId, String supplierProductName, String supplierProductDispName, String accessSpeed, String accessSpeedUnit, String noOfCopperPairs, ApplicabilityEnum applicability) {
        this.accessSpeed = accessSpeed;
        this.noOfCopperPairs = noOfCopperPairs;
        this.spacId = spacId;
        this.supplierName = supplierName;
        this.supplierProductName = supplierProductName;
        this.supplierProductDispName = supplierProductDispName;
        this.applicability = applicability;
        this.accessSpeedUnit = accessSpeedUnit;
    }

    public SacXlSupplierProductDto(SacSupplierProdDetailDTO dto) {
        this(dto.getSupplierName(), dto.getSpacId(), dto.getSupplierProductName(), dto.getSupplierProductDispName(), dto.getAccessSpeed(), dto.getAccessSpeedUnit(), dto.getNoOfCopperPairs(),
             ApplicabilityEnum.getApplicabilityEnum(dto.getApplicability()));
    }

    public String getSpacId() {
        return spacId;
    }

    public void setSpacId(String spacId) {
        this.spacId = spacId;
    }

    public String getAccessSpeed() {
        return accessSpeed;
    }

    public void setAccessSpeed(String accessSpeed) {
        this.accessSpeed = accessSpeed;
    }

    public String getNoOfCopperPairs() {
        return noOfCopperPairs;
    }

    public void setNoOfCopperPairs(String noOfCopperPairs) {
        this.noOfCopperPairs = noOfCopperPairs;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getSupplierProductName() {
        return supplierProductName;
    }

    public void setSupplierProductName(String supplierProductName) {
        this.supplierProductName = supplierProductName;
    }

    public ApplicabilityEnum getApplicability() {
        return applicability;
    }

    public void setApplicability(ApplicabilityEnum applicabilityEnum) {
        this.applicability = applicabilityEnum;
    }

    public String getSupplierProductDispName() {
        return supplierProductDispName;
    }

    public void setAccessSpeedUnit(String accessSpeedUnit) {
        this.accessSpeedUnit = accessSpeedUnit;
    }

    public void setSupplierProductDispName(String supplierProductDispName) {
        this.supplierProductDispName = supplierProductDispName;
    }

    public String getAccessSpeedUnit() {
        return accessSpeedUnit;
    }

    @Override
    public int compareTo(SacXlSupplierProductDto o) {
        int compVal = 0;
        String otherSpeedUnits = o.getAccessSpeedUnit();
        String otherSpeeds = o.getAccessSpeed();
        String otherCopperPairs = o.getNoOfCopperPairs();

        String unit = accessSpeedUnit.split("/")[0].trim();
        String speed = accessSpeed.split("/")[0].trim();
        String otherUnit = otherSpeedUnits.split("/")[0].trim();
        String otherSpeed = otherSpeeds.split("/")[0].trim();

        compVal = this.getSupplierName().compareTo(o.getSupplierName());
        if (compVal == 0) {
            compVal = AccessSpeedUnit.get(otherUnit).getValue() - AccessSpeedUnit.get(unit).getValue();
            if (compVal == 0) {
                try {
                    if (Integer.parseInt(otherSpeed) > Integer.parseInt(speed)) {
                        compVal = 1;
                    } else if (Integer.parseInt(otherSpeed) == Integer.parseInt(speed)) {
                        compVal = 0;
                    }else{
                        compVal = -1;
                    }

                    if (compVal == 0) {
                        if(Integer.parseInt(noOfCopperPairs.trim()) > Integer.parseInt(otherCopperPairs.trim())){
                            compVal = 1;
                        }else if (Integer.parseInt(noOfCopperPairs.trim()) == Integer.parseInt(otherCopperPairs.trim())) {
                            compVal = 0;
                        }else{
                            compVal = -1;
                        }
                    }

                    if(compVal ==0){
                        compVal = this.getSupplierProductDispName().compareTo(o.getSupplierProductDispName());
                    }

                } catch (Exception ex) {
                    compVal = 0;
                }
            }
        }

        return compVal;

    }

}
