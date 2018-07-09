package com.bt.rsqe.expedio.fixtures;

import com.bt.rsqe.domain.bom.parameters.BFGOrganisationDetails;
import com.bt.rsqe.expedio.project.ProjectDTO;

import java.util.Date;

public class ProjectDTOFixture {

    public static Builder aProjectDTO() {
        return new Builder();
    }

    public static class Builder {

        private String tradeLevel = "tradeLevel";
        private String projectId = "projectId";
        private String customerId = "7789";
        private String customerName = "Azco Nobel";
        private String quoteVersion = "1.0";
        private String orderType = "orderType";
        private String subOrderType = "subOrderType";
        private String salesOrganization = "BT Germany";
        private String quoteStatus = "Open";
        private String quoteName = "quoteName";
        private String currency = "GBP";
        private String salesRepName = "Brian";
        private String salesRepLoginId = "73412";
        private String expRef = "EPC00014410";
        private String contractId = "contractId";
        private String contractTerm = "contractTerm";
        private String orderId = "orderId";
        private String bidNumber = "34";
        private String siebelId = "siebelId";
        private String orderStatus;
        private String modifiedBy;
        private String isMNCContractUpdated;
        private String quoteOptionName = "quoteOptionName";
        private Date modifiedDate;
        private Date expiryDate;
        private Date createdDate;
        private String quoteIndicativeFlag = "Firm";
        private String salesOrganizationId;

        private Builder() {
        }

        public ProjectDTO build() {
            return new ProjectDTO(projectId, customerId, customerName, quoteVersion, orderType, new BFGOrganisationDetails(salesOrganization, salesOrganizationId), quoteStatus, quoteName, currency, salesRepName,
                                  salesRepLoginId, expRef, contractId, contractTerm, orderId, bidNumber, siebelId, tradeLevel, orderStatus, modifiedBy, isMNCContractUpdated,
                                  modifiedDate, quoteOptionName, expiryDate, createdDate, quoteIndicativeFlag, subOrderType);
        }

        public Builder withProjectId(String projectId) {
            this.projectId = projectId;
            return this;
        }

        public Builder withCustomerId(String customerId) {
            this.customerId = customerId;
            return this;
        }

        public Builder withCustomerName(String customerName) {
            this.customerName = customerName;
            return this;
        }

        public Builder withSalesRepName(String salesRepName) {
            this.salesRepName = salesRepName;
            return this;
        }

        public Builder withBidNumber(String bidNumber) {
            this.bidNumber = bidNumber;
            return this;
        }

        public Builder withSiebelId(String siebleId) {
            this.siebelId = siebleId;
            return this;
        }

        public Builder withTradeLevel(String tradeLevel) {
            this.tradeLevel = tradeLevel;
            return this;
        }

        public Builder withSalesOrganization(String salesOrganization) {
            this.salesOrganization = salesOrganization;
            return this;
        }

        public Builder withSalesOrganizationId(String salesOrganizationId) {
            this.salesOrganizationId = salesOrganizationId;
            return this;
        }

        public Builder withCurrency(String currency) {
            this.currency = currency;
            return this;
        }

        public Builder withContractTerm(String contractTerm) {
            this.contractTerm = contractTerm;
            return this;
        }

        public Builder withContractID(String contractID) {
            this.contractId = contractID;
            return this;
        }

        public Builder withQuoteVersion(String quoteVersion) {
            this.quoteVersion = quoteVersion;
            return this;
        }

        public Builder withQuoteStatus(String quoteStatus) {
            this.quoteStatus = quoteStatus;
            return this;
        }

        public Builder withOrderStatus(String orderStatus) {
            this.orderStatus = orderStatus;
            return this;
        }

        public Builder withModifiedBy(String modifiedBy) {
            this.modifiedBy = modifiedBy;
            return this;
        }

        public Builder withExpedioRef(String expRef) {
            this.expRef = expRef;
            return this;
        }

        public Builder withQuoteName(String quoteName) {
            this.quoteName = quoteName;
            return this;
        }

        public Builder withOrderType(String orderType) {
            this.orderType = orderType;
            return this;
        }

        public Builder withModifiedDate(Date modifiedDate) {
            this.modifiedDate = modifiedDate;
            return this;
        }

        public Builder withExpiryDate(Date expiryDate) {
            this.expiryDate = expiryDate;
            return this;
        }

        public Builder withCreatedDate(Date createdDate) {
            this.createdDate = createdDate;
            return this;
        }
    }
}