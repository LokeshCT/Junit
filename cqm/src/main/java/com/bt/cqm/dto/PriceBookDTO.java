package com.bt.cqm.dto;



import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created with IntelliJ IDEA.
 * User: 607982105
 * Date: 12/03/14
 * Time: 20:45
 * To change this template use File | Settings | File Templates.      */


@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class PriceBookDTO {

 @XmlElement
 private Long priceBookId;
 @XmlElement
 private Long pbCustomerId;
 @XmlElement
 private String productID;
 @XmlElement
 private String productName;
 @XmlElement
 private String EUPVersion;
 @XmlElement
 private String PTPversion;
 @XmlElement
 private String orderSubmitFlag;
 @XmlElement
 private String startDate;
 @XmlElement
 private String productCategoryId;

    public PriceBookDTO(Long customerID) {
        this.pbCustomerId = customerID;

    }
    public PriceBookDTO(Long priceBookId, Long pbCustomerId, String  productID,String productName,
                        String EUPVersion, String PTPversion,
                        String orderSubmitFlag, String startDate, String productCategoryId) {
        this.priceBookId = priceBookId;
        this.pbCustomerId = pbCustomerId;
        this.productID = productID;
        this.productName = productName;
        this.EUPVersion = EUPVersion;
        this.PTPversion = PTPversion;
        this.orderSubmitFlag = orderSubmitFlag;
        this.startDate = startDate;
        this.productCategoryId=productCategoryId;

    }
    public PriceBookDTO(String productName)
    {
        this.productName = productName;
    }
    public PriceBookDTO() {
        // required by jaxb
    }

    ///CLOVER:OFF

    public Long getPbId() {
        return priceBookId;
    }

    public void setPbId(Long priceBookId) {
        this.priceBookId = priceBookId;
    }

    public Long getPbCustomerId() {
        return pbCustomerId;
    }

    public void setPbCustomerId(Long pbCustomerId) {
        this.pbCustomerId = pbCustomerId;
    }

    public String getPbProductId() {
        return productID;
    }

    public void setPbProductId(String productID) {
        this.productID = productID;
    }

    public String getPbProductName() {
        return productName;
    }

    public void setPbProductName(String productName) {
        this.productName = productName;
    }

    public String getPbEUPVersion() {
        return EUPVersion;
    }

    public void setPbEUPVersion(String EUPVersion) {
        this.EUPVersion = EUPVersion;
    }

    public String getPbPTPVersion() {
        return PTPversion;
    }

    public void setPbPTPVersion(String PTPversion) {
        this.PTPversion = PTPversion;
    }

    public String getPbOrderSubFlag() {
        return orderSubmitFlag;
    }

    public void setPbOrderSubFlag(String orderSubmitFlag) {
        this.orderSubmitFlag = orderSubmitFlag;
    }

    public String getPbStartDate() {
        return startDate;
    }

    public void setPbStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getPbProductCategoryId() {
        return productCategoryId;
    }

    public void setPbProductCategoryId(String productCategoryId) {
        this.productCategoryId = productCategoryId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PriceBookDTO that = (PriceBookDTO) o;

        if (productName != null ? !productName.equals(that.productName) : that.productName != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return productName != null ? productName.hashCode() : 0;
    }
///CLOVER:ON


}
