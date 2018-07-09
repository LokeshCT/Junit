package com.bt.cqm.repository.channelhierarchy;

import com.bt.cqm.dto.PriceBookDTO;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(schema = "ARUSER_OM", name = "BFG_PRICEBOOK_DETAILS")
/*@NamedQuery(name = "BFG_PRICEBOOK_DETAILS", QUERY = "SELECT PB_ID AS PB_ID," +
        "PB_CUS_ID            AS PB_CUS_ID," +
        "PB_PRODUCT_ID        AS PB_PRODUCT_ID," +
        "PB_PRODUCT_NAME         AS PB_PRODUCT_NAME," +
        "PB_EUP_VERSION         AS PB_EUP_VERSION," +
        "PB_PTP_VERSION             AS PB_PTP_VERSION," +
        "PB_ORD_SUB_FLAG      AS PB_ORD_SUB_FLAG," +
        "PB_START_DATE     AS PB_START_DATE, " +
        "PB_PRODCATEGORY_ID      AS PB_PRODCATEGORY_ID" +
        "FROM ARUSER_OM.BFG_PRICEBOOK_DETAILS  C  ")*/
public class PriceBookEntity {

    @Column(name = "PB_ID")
    private Long pbId;
    @Column(name = "PB_CUS_ID")
    private Long pbCustomerId;
    @Id
    @Column(name = "PB_PRODUCT_ID")
    private String pbProductId;
    @Column(name = "PB_PRODUCT_NAME")
    private String pbProductName;
    @Column(name = "PB_EUP_VERSION")
    private String pbEUPVersion;
    @Column(name = "PB_PTP_VERSION")
    private String pbPTPVersion;
    @Column(name = "PB_ORD_SUB_FLAG")
    private String pbOrderSubFlag;
    @Column(name = "PB_START_DATE")
    private String pbStartDate;
   @Column(name = "PB_PRODCATEGORY_ID")
    private String pbProductCategoryId;

    public PriceBookEntity() {

    }
    public PriceBookEntity(Long pbId, Long pbCustomerId, String  pbProductId,String pbProductName,
                     String pbEUPVersion, String pbPTPVersion,
                     String pbOrderSubFlag, String pbStartDate, String pbProductCategoryId) {
        this.pbId = pbId;
        this.pbCustomerId = pbCustomerId;
        this.pbProductId = pbProductId;
        this.pbProductName = pbProductName;
        this.pbEUPVersion = pbEUPVersion;
        this.pbPTPVersion = pbPTPVersion;
        this.pbOrderSubFlag = pbOrderSubFlag;
        this.pbStartDate = pbStartDate;
        this.pbProductCategoryId=pbProductCategoryId;
    }



    public PriceBookDTO dto(Long customerID) {
        return new PriceBookDTO(pbId, customerID, pbProductId,
                pbProductName, pbEUPVersion, pbPTPVersion, pbOrderSubFlag, pbStartDate,pbProductCategoryId);
    }

    public PriceBookDTO dto(String pbProductName) {
        return new PriceBookDTO(pbId, pbCustomerId, pbProductId,
                pbProductName, pbEUPVersion, pbPTPVersion, pbOrderSubFlag, pbStartDate,pbProductCategoryId);
    }

    ///CLOVER:OFF

    public Long getPbId() {
        return pbId;
    }

    public void setPbId(Long pbId) {
        this.pbId = pbId;
    }

    public Long getPbCustomerId() {
        return pbCustomerId;
    }

    public void setPbCustomerId(Long pbCustomerId) {
        this.pbCustomerId = pbCustomerId;
    }

    public String getPbProductId() {
        return pbProductId;
    }

    public void setPbProductId(String pbProductId) {
        this.pbProductId = pbProductId;
    }

    public String getPbProductName() {
        return pbProductName;
    }

    public void setPbProductName(String pbProductName) {
        this.pbProductName = pbProductName;
    }

    public String getPbEUPVersion() {
        return pbEUPVersion;
    }

    public void setPbEUPVersion(String pbEUPVersion) {
        this.pbEUPVersion = pbEUPVersion;
    }

    public String getPbPTPVersion() {
        return pbPTPVersion;
    }

    public void setPbPTPVersion(String pbPTPVersion) {
        this.pbPTPVersion = pbPTPVersion;
    }

    public String getPbOrderSubFlag() {
        return pbOrderSubFlag;
    }

    public void setPbOrderSubFlag(String pbOrderSubFlag) {
        this.pbOrderSubFlag = pbOrderSubFlag;
    }

    public String getPbStartDate() {
        return pbStartDate;
    }

    public void setPbStartDate(String pbStartDate) {
        this.pbStartDate = pbStartDate;
    }

    public String getPbProductCategoryId() {
        return pbProductCategoryId;
    }

    public void setPbProductCategoryId(String pbProductCategoryId) {
        this.pbProductCategoryId = pbProductCategoryId;
    }


///CLOVER:ON

}







