package com.bt.rsqe.projectengine.web.view;

import com.bt.rsqe.domain.QuoteOptionItemStatus;
import com.bt.rsqe.projectengine.web.uri.UriFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CustomerProjectQuoteOptionsTab {

    private String customerName;
    private String quoteOptionDialogUri;
    private String viewConfigurationDialogUri;
    private List<QuoteOptionRow> quoteOptions;
    private UriFactory productConfiguratorUriFactory;
    private Set<String> validationMessages;
    private String notesDialogUri;
    private String deleteQuoteUri;
    private List<String> expedioQuoteId;
    private String customerId;
    private String contractId;
    private String projectId;
    private List<String> expedioQuoteName;



   

    public CustomerProjectQuoteOptionsTab(String customerName,
                                          String quoteOptionDialogUri,String viewConfigurationDialogUri, UriFactory productConfiguratorUriFactory, String notesDialogUri, String deleteQuoteOptionUri, List<String> expedioQuoteId,List<String> expedioQuoteName, String customerId, String contractId, String projectId) {
        this.customerName = customerName;
        this.quoteOptionDialogUri = quoteOptionDialogUri;
        this.productConfiguratorUriFactory = productConfiguratorUriFactory;
        this.viewConfigurationDialogUri = viewConfigurationDialogUri;
        this.notesDialogUri = notesDialogUri;
        this.expedioQuoteId = expedioQuoteId;
        this.expedioQuoteName= expedioQuoteName;
        this.customerId = customerId;
        this.contractId = contractId;
        this.projectId = projectId;
        quoteOptions = new ArrayList<QuoteOptionRow>();
        validationMessages = new HashSet<String>();
        this.deleteQuoteUri = deleteQuoteOptionUri;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getQuoteOptionDialogUri() {
        return quoteOptionDialogUri;
    }

    public String getNotesDialogUri() {
        return notesDialogUri;
    }

    public List<QuoteOptionRow> getQuoteOptions() {
        return quoteOptions;
    }

    public void addQuoteOption(QuoteOptionRow offerOption) {
        quoteOptions.add(offerOption);
    }

    public Set<String> getValidationMessages() {
        return validationMessages;
    }

    public void addValidationMessages(Set<String> validationMessages) {
        this.validationMessages = validationMessages;
    }

    public String getDeleteQuoteUri() {
        return deleteQuoteUri;
    }

    public List<String> getExpedioQuoteId() {
        return expedioQuoteId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getContractId() {
        return contractId;
    }

    public UriFactory getProductConfiguratorUriFactory() {
        return productConfiguratorUriFactory;
    }

    public String getProjectId() {
        return projectId;
    }

  public String getViewConfigurationDialogUri() {
        return viewConfigurationDialogUri;
    }

    public List<String> getExpedioQuoteName() {
        return expedioQuoteName;
    }

    public void setExpedioQuoteName(List<String> expedioQuoteName) {
        this.expedioQuoteName = expedioQuoteName;
    }

    public class QuoteOptionRow {

        private String id;
        private String friendlyId;
        private String name;
        private String currency;
        private String creationDate;
        private String createdBy;
        private String uri;
        private boolean isEditAllowed;
        private boolean hasQuoteOptionNotes;
        private boolean ifcPending;
        private boolean discountApprovalRequested;
        private String bcmUri;
        private String status;
        private String discountStatus;
        private boolean migrationQuote;

        public QuoteOptionRow(String id,
                              String friendlyId,
                              String name,
                              String currency,
                              String creationDate,
                              String createdBy,
                              String uri,
                              String bcmUri,
                              boolean isEditAllowed,
                              boolean hasQuoteOptionNotes,
                              boolean ifcPending,
                              boolean discountApprovalRequested,
                              String status,
                              boolean migrationQuote, String discountStatus) {
            this.id = id;
            this.friendlyId = friendlyId;
            this.name = name;
            this.currency = currency;
            this.creationDate = creationDate;
            this.createdBy = createdBy;
            this.uri = uri;
            this.bcmUri = bcmUri;
            this.isEditAllowed = isEditAllowed;
            this.hasQuoteOptionNotes = hasQuoteOptionNotes;
            this.ifcPending = ifcPending;
            this.discountApprovalRequested = discountApprovalRequested;
            this.status = status;
            this.migrationQuote = migrationQuote;
            this.discountStatus = discountStatus;
        }

        public String getId() {
            return id;
        }

        public String getFriendlyId() {
            return friendlyId;
        }

        public String getName() {
            return name;
        }

        public String getCurrency() {
            return currency;
        }

        public String getCreationDate() {
            return creationDate;
        }

        public String getCreatedBy() {
            return createdBy;
        }

        public String getUri() {
            return uri;
        }

        public String getBcmUri() {
            return bcmUri;
        }

        public String getBcmApproveUri() {
            return bcmUri + "/approve-discounts";
        }

        public String getBcmRejectUri(){
            return bcmUri+"/reject-discounts";
        }

        public boolean isEditAllowed() {
            return isEditAllowed;
        }

        public boolean isHasQuoteOptionNotes() {
            return hasQuoteOptionNotes;
        }

        public boolean isIfcPending() {
            return ifcPending;
        }

        public boolean isDiscountApprovalRequested() {
            return discountApprovalRequested;
        }

        public String getStatus() {
            return status;
        }

        public boolean getMigrationQuote() {
            return migrationQuote;
        }

        public String getDiscountStatus() {
            return discountStatus;
        }

        public boolean isDeletabled() {
            return this.status.equals(QuoteOptionItemStatus.INITIALIZING.getDescription()) || this.status.equals(QuoteOptionItemStatus.DRAFT.getDescription()) ||
                this.status.equals(QuoteOptionItemStatus.FAILED.getDescription()) || this.status.equals(QuoteOptionItemStatus.CANCELLED.getDescription());
        }
    }
}
