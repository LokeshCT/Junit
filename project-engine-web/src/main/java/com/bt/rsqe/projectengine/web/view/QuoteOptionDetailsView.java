package com.bt.rsqe.projectengine.web.view;


import com.bt.rsqe.projectengine.AttachmentViewDTO;
import com.bt.rsqe.projectengine.web.uri.UriFactory;
import com.bt.rsqe.projectengine.web.uri.UriFactoryImpl;
import com.google.common.base.Predicate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Iterables.*;
import static com.google.common.collect.Lists.*;

public class QuoteOptionDetailsView {
    private Products products = new Products();
    private final String customerId;
    private final String contractId;
    private final String projectId;
    private final String quoteOptionId;
    private String categoryId;
    private final String offersFormAction;
    private String currency;
    private final String quoteOptionTargetForCopyUri;
    private String revenueOwner;
    private final UriFactory uriFactory;
    private boolean allowCopyOptions;
    private int maxConfigurableLineItems;
    private boolean removeLineItemAllowed;
    private Map<String, String> queryParams = new HashMap<String, String>();
    private String baseQuoteOptionsUri;
    private String raiseIfcUrl;
    private String pricingUrl;
    private String lineItemsUrl;
    private String attachmentUrl;
    private String importProductURL;
    private String validateImportURL;

    private String viewConfigurationDialogUri;

    public QuoteOptionDetailsView(String customerId,
                                  String contractId,
                                  String projectId,
                                  String quoteOptionId,
                                  String categoryId,
                                  String currency,
                                  String salesOrganisation,
                                  UriFactory uriFactory,
                                  boolean allowCopyOptions,
                                  int maxConfigurableLineItems,
                                  boolean removeLineItemAllowed, String viewConfigurationDialogUri) {
        this.customerId = customerId;
        this.contractId = contractId;
        this.projectId = projectId;
        this.quoteOptionId = quoteOptionId;
        this.categoryId = categoryId;
        this.currency = currency;
        this.revenueOwner = salesOrganisation;
        this.uriFactory = uriFactory;
        this.allowCopyOptions = allowCopyOptions;
        this.maxConfigurableLineItems = maxConfigurableLineItems;
        this.removeLineItemAllowed = removeLineItemAllowed;
        this.offersFormAction = UriFactoryImpl.offers(customerId, contractId, projectId, quoteOptionId).toString();
        this.quoteOptionTargetForCopyUri = UriFactoryImpl.cloneTargetOptions(customerId, contractId, projectId, quoteOptionId).toString();
        this.baseQuoteOptionsUri = UriFactoryImpl.quoteOptions(customerId, contractId, projectId).toString();
        this.raiseIfcUrl = UriFactoryImpl.raiseIfcUrl(customerId, contractId, projectId, quoteOptionId).toString();
        pricingUrl = UriFactoryImpl.pricingUrl(customerId, contractId, projectId, quoteOptionId).toString();
        lineItemsUrl = UriFactoryImpl.lineItemsURI(customerId, contractId, projectId, quoteOptionId).toString();
        attachmentUrl = UriFactoryImpl.attachmentDialogForm(customerId, contractId, projectId, quoteOptionId, false).toString();
        importProductURL = UriFactoryImpl.importProductTargetUri(customerId, contractId, projectId, quoteOptionId).toString();
        validateImportURL = UriFactoryImpl.validateImportProductUri(customerId, contractId, projectId, quoteOptionId).toString();
        this.viewConfigurationDialogUri = viewConfigurationDialogUri;
    }

    public Products getProducts() {
        return products;
    }

    public void setProducts(Products products) {
        this.products = products;
    }


    public String getProjectId() {
        return projectId;
    }
    public String getQuoteOptionId() {
        return quoteOptionId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getContractId() {
        return contractId;
    }

    public String getOffersFormAction() {
        return offersFormAction;
    }

    public UriFactory getUriFactory() {
        return uriFactory;
    }

    public String getBulkTemplateUri() {
        return UriFactoryImpl.bulkTemplateUri(customerId, contractId, projectId, quoteOptionId).toString();
    }

    public String getBulkUploadTargetUri() {
        return UriFactoryImpl.bulkUploadTargetUri(customerId, contractId, projectId).toString();
    }


    public String getCurrency() {
        return currency;
    }

    public String getQuoteOptionTargetForCopyUri() {
        return quoteOptionTargetForCopyUri;
    }

    public String getRevenueOwner() {
        return revenueOwner;
    }

    public int getMaxConfigurableLineItems() {
        return maxConfigurableLineItems;
    }

    public String getRemoveLineItemAllowed() {
        return String.valueOf(removeLineItemAllowed);
    }

    public String getBaseQuoteOptionsUri() {
        return baseQuoteOptionsUri;
    }

    public String getRaiseIfcUrl() {
        return raiseIfcUrl;
    }

    public String getCreateProductUrl(String productCode) {
        return getUriFactory().getLineItemCreationUri(productCode, customerId, contractId, projectId);
    }

    public String getPricingUrl() {
        return pricingUrl;
    }

    public String getLineItemsUrl() {
        return lineItemsUrl;
    }

    public String getAttachmentUrl() {
        return attachmentUrl;
    }

    public String getImportProductURL() {
        return importProductURL;
    }

    public String getValidateImportProductUrl() {
        return validateImportURL;
    }

    public boolean isAllowCopyOptions() {
        return allowCopyOptions;
    }

    public String getViewConfigurationDialogUri() {
        return viewConfigurationDialogUri;
    }

    private List<AttachmentViewDTO.ItemRowDTO> getFilterdCategoryList(List<AttachmentViewDTO.ItemRowDTO> itemRowDTOs, final String categorySelected) {
        return newArrayList(filter(itemRowDTOs, new Predicate<AttachmentViewDTO.ItemRowDTO>() {
            @Override
            public boolean apply(AttachmentViewDTO.ItemRowDTO itemRowDTO) {
                return itemRowDTO.uploadAppliesTo.equalsIgnoreCase(categorySelected);
            }
        }));
    }
}
