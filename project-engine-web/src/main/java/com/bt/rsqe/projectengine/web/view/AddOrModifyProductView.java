package com.bt.rsqe.projectengine.web.view;


import com.bt.rsqe.projectengine.web.uri.UriFactory;
import com.bt.rsqe.projectengine.web.uri.UriFactoryImpl;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.List;

import static com.bt.rsqe.utils.AssertObject.isNotNull;

public class AddOrModifyProductView {
    private static final String PRODUCT_SCODE = "(productSCode)";
    private Products products;
    private final String customerId;
    private final String contractId;
    private final String projectId;
    private final String quoteOptionId;
    private final String offersFormAction;
    private List<String> countries;
    private UriFactory productConfiguratorUriFactory;
    private String currency;
    private String name;
    private String revenueOwner;
    private boolean indirectUser;
    private String productAction;
    private String selectNewSiteDialogUri;
    private String quoteOptionItemsSize;
    private String bulkTemplateExportUri;
    private String userImportUri;
    private String importProductURL;
    private String validateAddProductImportUrl;
    private String userExportUri;
    private String userImportValidateUri;
    private String userImportStatusUri;
    private String orderType;
    private String subOrderType;

    public AddOrModifyProductView(String customerId,
                                  String contractId,
                                  String projectId,
                                  String quoteOptionId,
                                  List<String> countries,
                                  UriFactory uriFactory,
                                  String currency,
                                  String name, String salesOrganisation,
                                  boolean indirectUser, String productAction,
                                  String quoteOptionItemsSize,
                                  String orderType,
                                  String subOrderType) {
        this.customerId = customerId;
        this.contractId = contractId;
        this.projectId = projectId;
        this.quoteOptionId = quoteOptionId;
        this.countries = countries;
        this.productConfiguratorUriFactory = uriFactory;
        this.currency = currency;
        this.name = name;
        this.revenueOwner = salesOrganisation;
        this.indirectUser = indirectUser;
        this.productAction = productAction;
        this.offersFormAction = UriFactoryImpl.offers(customerId, contractId, projectId, quoteOptionId).toString();
        this.selectNewSiteDialogUri = UriFactoryImpl.selectNewSite(customerId, contractId, projectId, quoteOptionId).toString();
        this.quoteOptionItemsSize = quoteOptionItemsSize;
        this.bulkTemplateExportUri = UriFactoryImpl.getBulkTemplateExportUri(customerId, contractId, projectId, quoteOptionId, PRODUCT_SCODE).toString();
        this.userImportUri = UriFactoryImpl.getUserImportUri(customerId, contractId, projectId, quoteOptionId, PRODUCT_SCODE).toString();
        this.userImportValidateUri = UriFactoryImpl.getUserImportValidateUri(customerId, contractId, projectId, quoteOptionId, PRODUCT_SCODE).toString();
        this.userImportStatusUri = UriFactoryImpl.getUserImportStatusUri(customerId, contractId, projectId, quoteOptionId, PRODUCT_SCODE).toString();
        this.userExportUri = UriFactoryImpl.getUserExportUri(customerId, contractId, projectId, quoteOptionId, PRODUCT_SCODE).toString();
        this.importProductURL = UriFactoryImpl.addImportProductTargetUri(customerId, contractId, projectId, quoteOptionId).toString();
        this.validateAddProductImportUrl = UriFactoryImpl.validateAddProductImportUri(customerId, contractId, projectId, quoteOptionId).toString();
        this.orderType = orderType;
        this.subOrderType = subOrderType;
    }



    public String getProjectId() {
        return projectId;
    }

    public String getQuoteOptionId() {
        return quoteOptionId;
    }

    public String getOffersFormAction() {
        return offersFormAction;
    }

    public Products getProducts() {
        return products;
    }

    public void setProducts(Products products) {
        this.products = products;
    }

    public List<Category> getCategories() {
        return products.getCategories();
    }

    public List<Category> getCategoryGroups() {
        return products.getCategoryGroups();
    }

    public UriFactory getProductConfiguratorUriFactory() {
        return productConfiguratorUriFactory;
    }

    public String getCreateProductUrl(String productCode) {
        return getProductConfiguratorUriFactory().getLineItemCreationUri(productCode, customerId, contractId, projectId);
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getContractId() {
        return contractId;
    }

    public String getCurrency() {
        return currency;
    }

    public List<String> getCountries() {
        return countries;
    }

    public String getName() {
        return name;
    }

    public String getRevenueOwner() {
        return revenueOwner;
    }

    public String getOrderType() {
        return orderType;
    }

    public String getSubOrderType() {
        return subOrderType;
    }

    public String getRedirectUri() {
        return UriFactoryImpl.quoteOption(customerId, contractId, projectId, quoteOptionId).toString();
    }

    public String getGetSitesUri() {
        return UriFactoryImpl.sitesToAddURI(customerId, contractId, projectId, quoteOptionId).toString();
    }

    public String getGetServicesUri() {
        return UriFactoryImpl.servicesToAddURI(customerId, contractId, projectId, quoteOptionId).toString();
    }

    public String getGetProductAttributesUri() {
        return UriFactoryImpl.productAttributesUri(customerId, contractId, projectId, quoteOptionId).toString();
    }

    public String getGetLaunchStatusUri() {
        return UriFactoryImpl.getGetLaunchStatusURI(customerId, contractId, projectId, quoteOptionId).toString();
    }

    public String getCreateProductUri() {
        return UriFactoryImpl.createProductURI(customerId, contractId, projectId, quoteOptionId).toString();
    }

    public String getCardinalityCheckUri() {
        return UriFactoryImpl.cardinalityCheckUri(customerId, contractId, projectId, quoteOptionId).toString();
    }

    public String getSiteSelectedForProductCheckUri() {
        return UriFactoryImpl.siteSelectedForProductCheckUri(customerId, contractId, projectId, quoteOptionId).toString();
    }

    public String getProductAvailabilityUri() {
        return UriFactoryImpl.productAvailabilityUri(customerId, contractId, projectId, quoteOptionId, PRODUCT_SCODE).toString();
    }

    public String getProductAction() {
        return productAction;
    }

    public String getSelectNewSiteDialogUri() {
        return selectNewSiteDialogUri;
    }

    public String getUserImportValidateUri() {
        return userImportValidateUri;
    }

    public String getUserImportStatusUri() {
        return userImportStatusUri;
    }

    public String getEndOfLifeCheckUri() {
        return UriFactoryImpl.endOfLifeCheckUri(customerId, contractId, projectId, quoteOptionId).toString();
    }

    public String productJson(String productId, String categoryCode) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        Product product = products.getProduct(productId, categoryCode);

        jsonObject.put("sCode", productId);
        jsonObject.put("categoryGroupCode", isNotNull(product.getProductCategoryGroup()) ? product.getProductCategoryGroup().getId() : "");
        jsonObject.put("categoryCode", isNotNull(product.getProductCategory()) ? product.getProductCategory().getId() : "");
        jsonObject.put("productVersion", product.getVersion());
        jsonObject.put("creationUrl", getCreateProductUrl(productId));
        jsonObject.put("isSiteSpecific", product.isSiteSpecific());
        jsonObject.put("isComplianceCheckNeeded", isComplianceCheckNeeded(product));
        jsonObject.put("prerequisiteUrl", isComplianceCheckNeeded(product) ? prerequisiteUrlFor(product) : "");
        jsonObject.put("isImportable", products.getSellableProduct(productId).getImportable());
        jsonObject.put("moveConfigurationType", products.getSellableProduct(productId).getMoveConfigurationType());
        jsonObject.put("rollOnContractTermForMove", products.getSellableProduct(productId).getRollOnContractTermForMove());
        jsonObject.put("isUserImportable", products.getSellableProduct(productId).getUserImportable());
        jsonObject.put("userExportUri", userExportUri);
        jsonObject.put("userImportUri", userImportUri);
        jsonObject.put("userImportValidateUri", userImportValidateUri);
        jsonObject.put("userImportStatusUri", userImportStatusUri);
        jsonObject.put("contractCardinality", products.getSellableProduct(productId).getContractCardinality());

        return jsonObject.toString();
    }

    private boolean isComplianceCheckNeeded(Product product) {
        return (product.getPrerequisiteUrl() != null) && (product.getPrerequisiteUrl().getIndirectUrl() != null || product.getPrerequisiteUrl().getDirectUrl() != null);
    }

    private String prerequisiteUrlFor(Product product) {
        return indirectUser ? product.getPrerequisiteUrl().getIndirectUrl() : product.getPrerequisiteUrl().getDirectUrl();
    }

    public String getComplianceCheckMessage() {
        return "Customer Agreed to comply with Prerequisites";
    }

    public String getQuoteOptionItemsSize() {
        return quoteOptionItemsSize;
    }

    public String getGetBulkTemplateExportUri() {
        return bulkTemplateExportUri;
    }

    public String getImportProductURL() {
        return importProductURL;
    }

    public String getValidateImportProductURL(){
        return validateAddProductImportUrl;
    }

    public String getUserImportUri() {
        return userImportUri;
    }

    public String getUserExportUri() {
        return userExportUri;
    }
}
