package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;

public class BCMBidInfo {

    private String quoteId,
    quoteOptionId,
    quoteCurrency,
    opportunityId,
    bidNumber,
    userName,
    tradeLevel,
    customerName,
    salesChannel, offerName,salesChannelType, expedioReference;
    private int contractTerm;


    public BCMBidInfo(String quoteId, String quoteOptionId, String quoteCurrency, String opportunityId, String bidNumber, String userName,
                      String tradeLevel, String customerName, String salesChannel, int contractTerm, String offerName, String salesChannelType, String expedioReference) {
        this.quoteId = quoteId;
        this.quoteOptionId = quoteOptionId;
        this.quoteCurrency = quoteCurrency;
        this.opportunityId = opportunityId;
        this.bidNumber = bidNumber;
        this.userName = userName;
        this.tradeLevel = tradeLevel;
        this.customerName = customerName;
        this.salesChannel = salesChannel;
        this.contractTerm = contractTerm;
        this.offerName = offerName;
        this.salesChannelType=salesChannelType;
        this.expedioReference = expedioReference;
    }

    public Object getQuoteId() {
        try {
            return Integer.parseInt(quoteId);
        } catch (Exception e) {
            // do nothing
        }
        return quoteId;
    }

    public String getQuoteOptionId() {
        return quoteOptionId;
    }

    public String getQuoteCurrency() {
        return quoteCurrency;
    }

    public String getOpportunityId() {
        return opportunityId;
    }

    public Object getBidNumber() {
        try {
            return Integer.parseInt(bidNumber);
        } catch (Exception e) {
            // do nothing
        }
        return bidNumber;
    }

    public String getUsername() {
        return userName;
    }

    public String getTradeLevel() {
        return tradeLevel;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getSalesChannel() {
        if (salesChannelType.equals("Direct")) {
            return salesChannel;
        }
        return salesChannelType;
    }

    public int getContractTerm() {
        return contractTerm;
    }

    public String getOfferName() {
        return offerName;
    }

    public String getExpedioReference() {
        return expedioReference;
    }
}
