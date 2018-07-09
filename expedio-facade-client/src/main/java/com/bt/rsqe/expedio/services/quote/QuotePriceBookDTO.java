package com.bt.rsqe.expedio.services.quote;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class QuotePriceBookDTO {


    @XmlElement
    private String tradeLevelEntity;

    @XmlElement
    private String tradeLevel;

    @XmlElement
    private String productName;

    @XmlElement
    private String rrpPriceBook;

    @XmlElement
    private String ptpPriceBook;

    @XmlElement
    private String scode;

    @XmlElement
    private String hcode;



    /**
     * Default constructor needed by JAXB
     */
    public QuotePriceBookDTO(){

    }


    public String getTradeLevelEntity() {
        return tradeLevelEntity;
    }

    public void setTradeLevelEntity(String tradeLevelEntity) {
        this.tradeLevelEntity = tradeLevelEntity;
    }

    public String getTradeLevel() {
        return tradeLevel;
    }

    public void setTradeLevel(String tradeLevel) {
        this.tradeLevel = tradeLevel;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getRrpPriceBook() {
        return rrpPriceBook;
    }

    public void setRrpPriceBook(String rrpPriceBook) {
        this.rrpPriceBook = rrpPriceBook;
    }

    public String getPtpPriceBook() {
        return ptpPriceBook;
    }

    public void setPtpPriceBook(String ptpPriceBook) {
        this.ptpPriceBook = ptpPriceBook;
    }

    public String getScode() {
        return scode;
    }

    public void setScode(String scode) {
        this.scode = scode;
    }

    public String getHcode() {
        return hcode;
    }

    public void setHcode(String hcode) {
        this.hcode = hcode;
    }



}
