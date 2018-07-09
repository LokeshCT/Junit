package com.bt.rsqe.expedio.pricebook;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created with IntelliJ IDEA.
 * User: 608026723
 * Date: 3/10/15
 * Time: 4:53 PM
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class PriceBookDTO {
    private String salesChannelName ;
    private String customerId;
    private String customerName;
    private String productName;
    private String rrpVersion;
    private String ptpVersion;
    private String productScode;
    private String productKey;
    private String pmfcategoryID;
    private String packageProductname;

    public String getSalesChannelName() {
        return salesChannelName;
    }

    public void setSalesChannelName(String salesChannelName) {
        this.salesChannelName = salesChannelName;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getRrpVersion() {
        return rrpVersion;
    }

    public void setRrpVersion(String rrpVersion) {
        this.rrpVersion = rrpVersion;
    }

    public String getPtpVersion() {
        return ptpVersion;
    }

    public void setPtpVersion(String ptpVersion) {
        this.ptpVersion = ptpVersion;
    }

    public String getProductScode() {
        return productScode;
    }

    public void setProductScode(String productScode) {
        this.productScode = productScode;
    }

    public String getProductKey() {
        return productKey;
    }

    public void setProductKey(String productKey) {
        this.productKey = productKey;
    }

    public String getPmfcategoryID() {
        return pmfcategoryID;
    }

    public void setPmfcategoryID(String pmfcategoryID) {
        this.pmfcategoryID = pmfcategoryID;
    }

    public String getPackageProductname() {
        return packageProductname;
    }

    public void setPackageProductname(String packageProductname) {
        this.packageProductname = packageProductname;
    }
}
