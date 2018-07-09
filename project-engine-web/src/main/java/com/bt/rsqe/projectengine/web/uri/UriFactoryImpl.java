package com.bt.rsqe.projectengine.web.uri;

import com.bt.rsqe.configuration.ProductConfiguratorConfig;
import com.bt.rsqe.projectengine.server.ProjectEngineWebConfig;
import com.google.common.base.Joiner;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * This class builds URIs for each of the web pages.
 */
public class UriFactoryImpl implements UriFactory
{
    private static final Logger LOGGER = LoggerFactory.getLogger(UriFactoryImpl.class);
    public static final String DEFAULT_PROJECT_SCODE = "default";
    private static final String LINE_ITEMS = "line-items";
    private static final String ATTACHMENTS = "attachments";
    private static final String S_CODE = "sCode";
    public static final String SQE_TEST_T3_URL = "http://sqe.t3.nat.bt.com/PROD_SQE_iVPN/homePage/homePage.html?initQH=y&productName=BT IVPN2 GLOBAL&internal=y&guid=IDG2242249136AN4YZ8GO4K0TSBCPE000000000196628&orderType=Provide&quoteHeaderId=863633#/quoteDetails";
    public static String SQE_END_URL;


    public static URI projects(String customerId, String contractId, String projectId) {
        return UriBuilder.fromUri("/rsqe/customers")
                         .path(customerId)
                         .path("contracts")
                         .path(contractId)
                         .path("projects")
                         .path(projectId).build();
    }

    public static URI quoteOptions(String customerId, String contractId, String projectId) {
        return UriBuilder.fromUri(projects(customerId, contractId, projectId))
                         .path("quote-options").build();
    }

    public static URI validation(String customerId, String contractId, String projectId, String quoteOptionId) {
        return UriBuilder.fromUri(quoteOptions(customerId, contractId, projectId))
                         .path(quoteOptionId)
                         .path("validation").build();
    }

    public static URI contract(String customerId, String contractId, String projectId, String quoteOptionId, String quoteOptionItemId) {
        return UriBuilder.fromUri(quoteOptions(customerId, contractId, projectId))
                         .path(quoteOptionId)
                         .path(LINE_ITEMS)
                         .path(quoteOptionItemId)
                         .path("contract").build();
    }

    public static URI productAgreementsUri(String customerId, String contractId, String projectId, String quoteOptionId, String quoteOptionItemId,String lineItemArray) {
        return UriBuilder.fromUri(quoteOptions(customerId, contractId, projectId))
                                                           .path(quoteOptionId)
                                                           .path("line-items")
                                                           .path(quoteOptionItemId)
                                                           .path("lineItem-array")
                                                           .path(lineItemArray)
                                                           .path("productAgreements")
                                                           .build();
    }
    public static URI saveRevenueUri(String customerId, String contractId, String projectId, String quoteOptionId) {
         return UriBuilder.fromUri(quoteOptions(customerId, contractId, projectId))
                         .path(quoteOptionId)
                         .path(LINE_ITEMS).build();
    }
    public static URI quoteOptionDialog(String customerId, String contractId, String projectId) {
        return UriBuilder.fromUri(quoteOptions(customerId, contractId, projectId))
                         .path("form").build();
    }

    public static URI quoteOptionNotesDialog(String customerId, String contractId, String projectId) {
        return UriBuilder.fromUri(quoteOptions(customerId, contractId, projectId))
                         .path("notes").build();
    }

    public static URI viewConfigurationDialog(String customerId, String contractId, String projectId) {
        return UriBuilder.fromUri(quoteOptions(customerId, contractId, projectId))
                .path("viewConfiguration").build();
    }

    public static URI viewConfigurationURI(String customerId, String contractId, String projectId) {
        return UriBuilder.fromUri(projects(customerId, contractId, projectId)).path("buildConfigurationTree").build();
    }

    public static URI offerApprove(String customerId, String contractId, String projectId, String quoteOptionId, String offerId) {
        return UriBuilder.fromUri(offers(customerId, contractId, projectId, quoteOptionId)).path(offerId).path("approve").build();

    }

    public static URI offerReject(String customerId, String projectId, String quoteOptionId, String offerId, String contractId) {
        return UriBuilder.fromUri(offers(customerId, contractId, projectId, quoteOptionId)).path(offerId).path("reject").build();
    }

    public static URI quoteOptionsTab(String customerId, String contractId, String projectId) {
        return UriBuilder.fromUri(projects(customerId, contractId, projectId))
                         .path("quote-options-tab").build();
    }

    /**
     * Build the URL for the Pricing Tab.
     * This is the existing Pricing Tab. This tab will be removed once the Pricing re-write is complete.
     * @param customerId ID of the Customer requesting the page.
     * @param contractId ID of the Customer's contract.
     * @param projectId ID of the project.
     * @param quoteOptionId ID of the quote option.
     * @return The old Pricing Tab URI.
     */
    public static URI quoteOptionPricingTab(String customerId, String contractId, String projectId, String quoteOptionId)
    {
        URI uri = quoteOptionUri(customerId, contractId, projectId, quoteOptionId)
                .path("pricing-tab").build();
        LOGGER.info("Returning Quote Option Pricing Tab URI = {}", uri);
        return uri;
    }

    /**
     * Build the URL for the new Pricing Tab. This is the URL that will be hit to retrieve the the Pricing tab's contents when viewing the
     * Quote Option Details Page.
     * This is the new Pricing Tab added as part of the Pricing re-write.
     * @param customerId ID of the Customer requesting the page.
     * @param contractId ID of the Customer's contract.
     * @param projectId ID of the project.
     * @param quoteOptionId ID of the quote option.
     * @return The Pricing Summary Tab URI.
     */
    public static URI quoteOptionPricingTabNew(String customerId, String contractId, String projectId, String quoteOptionId)
    {
        URI uri = quoteOptionUri(customerId, contractId, projectId, quoteOptionId)
                    .path("pricing-tab-new").build();
        LOGGER.info("Returning Quote Option Pricing Tab (new) URI = {}", uri);
        return uri;
    }

    public static URI quoteOptionDetailsTab(String customerId, String contractId, String projectId, String quoteOptionId) {
        return quoteOptionUri(customerId, contractId, projectId, quoteOptionId)
            .path("details-tab").build();
    }

    public static URI quoteOptionOffersTab(String customerId, String contractId, String projectId, String quoteOptionId) {
        return quoteOptionUri(customerId, contractId, projectId, quoteOptionId)
            .fragment("QuoteOptionOffersTab").build();
    }

    public static URI sitesToAddURI(String customerId, String contractId, String projectId, String quoteOptionId) {
        return addProduct(customerId, contractId, projectId, quoteOptionId).path("sites").build();
    }

    public static URI servicesToAddURI(String customerId, String contractId, String projectId, String quoteOptionId) {
        return addProduct(customerId, contractId, projectId, quoteOptionId).path("services").build();
    }

    public static URI productAttributesUri(String customerId, String contractId, String projectId, String quoteOptionId) {
        return addProduct(customerId, contractId, projectId, quoteOptionId).path("service-attributes").build();
    }

    public static URI getGetLaunchStatusURI(String customerId, String contractId, String projectId, String quoteOptionId) {
        return addProduct(customerId, contractId, projectId, quoteOptionId).path("getLaunched").build();
    }

    public static URI createProductURI(String customerId, String contractId, String projectId, String quoteOptionId) {
        return addProduct(customerId, contractId, projectId, quoteOptionId).path("createProduct").build();
    }

    public static URI cardinalityCheckUri(String customerId, String contractId, String projectId, String quoteOptionId) {
        return addProduct(customerId, contractId, projectId, quoteOptionId).path("cardinalityCheck").build();
    }

    public static URI siteSelectedForProductCheckUri(String customerId, String contractId, String projectId, String quoteOptionId) {
        return addProduct(customerId, contractId, projectId, quoteOptionId).path("siteSelectedForProductCheck").build();
    }

    public static UriBuilder addProduct(String customerId, String contractId, String projectId, String quoteOptionId) {
        return quoteOptionUri(customerId, contractId, projectId, quoteOptionId).path("add-product");
    }

    public static URI productTabURI(String customerId, String contractId, String projectId, String quoteOptionId, String productAction) {
        return quoteOptionUri(customerId, contractId, projectId, quoteOptionId).path("add-product").path("product-tab").path(productAction).build();
    }

    public static URI quoteOptionNotes(String customerId, String contractId, String projectId, String quoteOptionId) {
        return quoteOptionUri(customerId, contractId, projectId, quoteOptionId)
            .path("notes").build();
    }

    public static URI lineItemNotes(String customerId, String contractId, String projectId, String quoteOptionId, String itemId) {
        return quoteOptionUri(customerId, contractId, projectId, quoteOptionId)
            .path(LINE_ITEMS)
            .path(itemId)
            .path("notes").build();
    }

    public static URI offers(String customerId, String contractId, String projectId, String quoteOptionId) {
        return quoteOptionUri(customerId, contractId, projectId, quoteOptionId)
            .path("offers").build();
    }

    public static URI offerDetails(String customerId, String contractId, String projectId, String quoteOptionId,
                                   String offerId) {
        return UriBuilder.fromUri(offers(customerId, contractId, projectId, quoteOptionId)).path(offerId).build();
    }

    public static String productDetails(String SQE_BASE_URI, String orderType, String productName, String guId) {
        SQE_END_URL = SQE_BASE_URI;
        String SUB_URL = "homePage/homePage.html?initQH=y&productName="+"BT IVPN2 GLOBAL"+
                "&internal=y&guid="+guId+"&orderType="+orderType+
                "#/quoteDetails";

        String finalURL = SQE_END_URL.substring(0, SQE_END_URL.indexOf("service")) + SUB_URL;

        return finalURL;
    }

    public static URI offerDetailsTab(String customerId, String contractId, String projectId, String quoteOptionId,
                                      String offerId) {
        return UriBuilder.fromUri(offerDetails(customerId, contractId, projectId, quoteOptionId, offerId)).path("offer-details-tab").build();
    }

    public static URI orders(String customerId, String contractId, String projectId, String quoteOptionId) {
        return quoteOptionUri(customerId, contractId, projectId, quoteOptionId)
            .path("orders").build();
    }

    public static URI ordersTab(String customerId, String contractId, String projectId, String quoteOptionId) {
        return quoteOptionUri(customerId, contractId, projectId, quoteOptionId)
            .fragment("OrdersTab").build();
    }

    public static UriBuilder quoteOptionUri(String customerId, String contractId, String projectId, String quoteOptionId) {
        return UriBuilder.fromUri(quoteOption(customerId, contractId, projectId, quoteOptionId));
    }

    public static URI quoteOption(String customerId, String contractId, String projectId, String quoteOptionId) {
        return UriBuilder.fromUri(quoteOptions(customerId, contractId, projectId))
                         .path(quoteOptionId).build();
    }

    public static URI quoteOptionBcm(String customerId, String contractId, String projectId, String quoteOptionId) {
        return UriBuilder.fromUri(quoteOption(customerId, contractId, projectId, quoteOptionId))
                         .path("bcm").build();
    }

    public static UriBuilder orderUri(String customerId, String contractId, String projectId, String quoteOptionId, String orderId) {
        return UriBuilder.fromUri(orders(customerId, contractId, projectId, quoteOptionId)).path(orderId);
    }

    public static URI orderSubmit(String customerId, String contractId, String projectId, String quoteOptionId, String orderId) {
        return orderUri(customerId, contractId, projectId, quoteOptionId, orderId).path("submit").build();
    }

    public static URI orderStatus(String customerId, String contractId, String projectId, String quoteOptionId, String orderId) {
        return orderUri(customerId, contractId, projectId, quoteOptionId, orderId).path("status").build();
    }

    public static URI cancelOrder(String customerId, String contractId, String projectId, String quoteOptionId, String orderId) {
        return orderUri(customerId, contractId, projectId, quoteOptionId, orderId).path("cancel").build();
    }

    public static URI orderSubmittedTabUri(String customerId, String contractId, String projectId, String quoteOptionId, String orderId) {
        return orderUri(customerId, contractId, projectId, quoteOptionId, orderId).fragment("OrderSubmittedTab").build();
    }

    public static URI linkRFO(String customerId, String contractId, String projectId, String quoteOptionId, String id) {
        return orderUri(customerId, contractId, projectId, quoteOptionId, id).path("rfo").build();
    }

    public static URI exportPricingSheet(String customerId, String contractId, String projectId, String quoteOptionId, String offerId) {
        return quoteOptionUri(customerId, contractId, projectId, quoteOptionId)
            .path("pricing-sheet").queryParam("offerId", offerId).build();
    }

    public static URI bulkTemplateUri(String customerId, String contractId, String projectId, String quoteOptionId) {
        return quoteOptionUri(customerId, contractId, projectId, quoteOptionId).path("dialogs").path("bulk-template").build();
    }

    public static URI loadAttachmentUri(String customerId, String contractId, String projectId, String quoteOptionId) {
        return quoteOptionUri(customerId, contractId, projectId, quoteOptionId).path(ATTACHMENTS).path("load-attachment").build();
    }

    public static URI deleteAttachmentUri(String customerId, String contractId, String projectId, String quoteOptionId) {
        return quoteOptionUri(customerId, contractId, projectId, quoteOptionId).path(ATTACHMENTS).path("delete-attachment").build();
    }

     public static URI uploadAttachmentUri(String customerId, String contractId, String projectId, String quoteOptionId) {
        return quoteOptionUri(customerId, contractId, projectId, quoteOptionId).path(ATTACHMENTS).path("upload-attachment")
                                                                                                   .build();
    }

    public static URI downloadAttachmentUri(String customerId, String contractId, String projectId, String quoteOptionId) {
        return quoteOptionUri(customerId, contractId, projectId, quoteOptionId).path(ATTACHMENTS).path("download-attachment").build();
    }

    public static URI pricingActionsUri(String customerId, String contractId, String projectId, String quoteOptionId) {
        return quoteOptionUri(customerId, contractId, projectId, quoteOptionId).path("pricing-actions").build();
    }

    public static URI cloneTargetOptions(String customerId, String contractId, String projectId, String quoteOptionId) {
        return quoteOptionUri(customerId, contractId, projectId, quoteOptionId).path("clone-target-options").build();
    }

    public static URI lineItemValidationUri(String customerId, String contractId, String projectId, String quoteOptionId, String lineItemId) {
        return lineItemUri(customerId, contractId, projectId, quoteOptionId, lineItemId).path("validate").build();
    }

    public static URI lineItemsURI(String customerId, String contractId, String projectId, String quoteOptionId) {
        return quoteOptionUri(customerId, contractId, projectId, quoteOptionId).path(LINE_ITEMS).build();
    }

    public static URI attachmentDialogForm(String customerId, String contractId, String projectId, String quoteOptionId, boolean isCostAttachmentDialog) {
        return quoteOptionUri(customerId, contractId, projectId, quoteOptionId).path(ATTACHMENTS).path("form").queryParam("isCostAttachmentDialog", isCostAttachmentDialog).build();
    }

    public static UriBuilder lineItemUri(String customerId, String contractId, String projectId, String quoteOptionId, String lineItemId) {
        return quoteOptionUri(customerId, contractId, projectId, quoteOptionId).path(LINE_ITEMS).path(lineItemId);
    }

    public static URI raiseIfcUrl(String customerId, String contractId, String projectId, String quoteOptionId) {
        return quoteOptionUri(customerId, contractId, projectId, quoteOptionId).path("ifc-line-items").build();
    }

    public static URI bulkUploadTargetUri(String customerId, String contractId, String projectId) {
        return UriBuilder.fromUri(quoteOptions(customerId, contractId, projectId)).path("bulk-upload").build();
    }

    public static URI filterLineItemsUri(String customerId, String contractId, String projectId) {
        return UriBuilder.fromUri(quoteOptions(customerId, contractId, projectId)).path("filter-line-items").build();
    }

    public static URI pricingUrl(String customerId, String contractId, String projectId, String quoteOptionId) {
        return quoteOptionUri(customerId, contractId, projectId, quoteOptionId).path("line-item-prices").build();
    }

    public static URI selectNewSite(String customerId, String contractId, String projectId, String quoteOptionId) {
        return quoteOptionUri(customerId, contractId, projectId, quoteOptionId)
            .path("add-product").path("selectNewSiteForm").build();
    }

    public static URI importProductTargetUri(String customerId, String contractId, String projectId, String quoteOptionId) {
        return quoteOptionUri(customerId, contractId, projectId, quoteOptionId).path(LINE_ITEMS)
            .path("import-product-configuration").build();
    }

    public static URI validateImportProductUri(String customerId, String contractId, String projectId, String quoteOptionId) {
        return UriBuilder.fromUri(validation(customerId, contractId, projectId, quoteOptionId))
            .path("validate-import-with-line-item").build();
    }

    public static URI validateAddProductImportUri(String customerId, String contractId, String projectId, String quoteOptionId) {
        return UriBuilder.fromUri(validation(customerId, contractId, projectId, quoteOptionId))
            .path("validate-import-with-product-code").build();
    }

    public static URI addImportProductTargetUri(String customerId, String contractId, String projectId, String quoteOptionId) {
        return quoteOptionUri(customerId, contractId, projectId, quoteOptionId).path(LINE_ITEMS)
            .path("add-import-product-configuration").build();
    }

    public static URI deleteQuoteOptionUri(String customerId, String contractId, String projectId) {
        return UriBuilder.fromUri(quoteOptions(customerId, contractId, projectId))
                         .path("delete").build();
    }

    public static URI endOfLifeCheckUri(String customerId, String contractId, String projectId, String quoteOptionId) {
        return addProduct(customerId, contractId, projectId, quoteOptionId).path("endOfLifeValidation").build();
    }

    public static URI downloadBomXml(String customerId, String contractId, String projectId, String quoteOptionId, String orderId) {
        return downloadBomXmlUri(customerId, contractId, projectId, quoteOptionId, orderId).path("downloadBomXml").build();
    }

    public static UriBuilder downloadBomXmlUri(String customerId, String contractId, String projectId, String quoteOptionId, String orderId) {
        return UriBuilder.fromUri(orders(customerId, contractId, projectId, quoteOptionId)).path(orderId);
    }

    private ProjectEngineWebConfig projectEngineWebConfiguration;

    public UriFactoryImpl(ProjectEngineWebConfig projectEngineWebConfiguration) {
        this.projectEngineWebConfiguration = projectEngineWebConfiguration;
    }

    @Override
    public String getLineItemCreationUri(String sCode, String customerId, String contractId, String projectId) {
        ConfiguratorProduct configuratorProduct = getConfiguratorProduct(sCode);
        return configuratorProduct.isDefault() ? configuratorProduct.getCreateUrl(customerId, contractId, projectId) : configuratorProduct.getCreateUrl();
    }

    @Override
    public String getConfigurationUri(String sCode, String customerId, String contractId, String projectId, String quoteOptionId, String lineItemId, Map<String, String> parameters) {
        ConfiguratorProduct configuratorProduct = getConfiguratorProduct(sCode);
        String configureUri = configuratorProduct.isDefault() ? configuratorProduct.getConfigUrl(customerId, contractId, projectId, quoteOptionId, lineItemId, lineItemId) : configuratorProduct.getConfigUrl(lineItemId);
        return addQueryParams(parameters, configureUri);
    }

    @Override
    public String getBulkUploadUri(String sCode) {
        return getConfiguratorProduct(sCode).getBulkUrl();
    }


    @Override
    public String getBulkTemplateUri(String sCode, String customerId, String projectId, String quoteOptionId, String currency) {
        return getConfiguratorProduct(sCode).getBulkTemplateUrl(customerId, projectId, quoteOptionId, currency);
    }

    @Override
    public String getBulkViewUri(String customerId, String contractId, String projectId, String quoteOptionId) {
        return getConfiguratorProduct(DEFAULT_PROJECT_SCODE).getBulkViewUrl(customerId, contractId, projectId, quoteOptionId);
    }

    @Override
    public String getLocateOnGoogleMapsViewUri(String customerId, String contractId, String projectId, String quoteOptionId) {
        return getConfiguratorProduct(DEFAULT_PROJECT_SCODE).getLocateOnGoogleMapsUrl(customerId, contractId, projectId, quoteOptionId);
    }

    @Override
    public String getQuoteLaunchUri(String customerId, String contractId, String projectId) {
        return projects(customerId, contractId, projectId).toString();
    }

    public static URI productAvailabilityUri(String customerId, String contractId, String projectId, String quoteOptionId, String productSCode) {
        return addProduct(customerId, contractId, projectId, quoteOptionId).path(S_CODE).path(productSCode).path("availability-check").build();
    }

    private Map<String, ConfiguratorProduct> urlMapping() {
        Map<String, ConfiguratorProduct> productMap = new HashMap<String, ConfiguratorProduct>();

        ProductConfiguratorConfig.Product[] products = projectEngineWebConfiguration.getProductConfiguratorConfig().getProducts();
        for (ProductConfiguratorConfig.Product product : products) {
            productMap.put(product.getSCode(), ConfiguratorProductFactory.getProduct(product.getSCode(), product.getUrls()));
        }

        return productMap;
    }

    private String addQueryParams(Map<String, String> queryParams, String uri) {
        if (StringUtils.isEmpty(uri)) {
            return uri;
        }
        StringBuilder configureUriBuilder = new StringBuilder(uri);
        configureUriBuilder.append("?");
        String joinedParameter = Joiner.on("&amp;").withKeyValueSeparator("=").join(queryParams);
        return configureUriBuilder.append(joinedParameter).toString();
    }

    private ConfiguratorProduct getConfiguratorProduct(String sCode) {
        final Map<String, ConfiguratorProduct> productMap = urlMapping();
        ConfiguratorProduct configuratorProduct;
        if (productMap.containsKey(sCode)) {
            configuratorProduct = productMap.get(sCode);
        } else {
            configuratorProduct = productMap.get(DEFAULT_PROJECT_SCODE);
        }
        return configuratorProduct;
    }

    public static URI getBulkTemplateExportUri(String customerId, String contractId, String projectId, String quoteOptionId, String sCode) {
        return UriBuilder.fromUri(quoteOption(customerId, contractId, projectId, quoteOptionId))
                         .path("bulk-template-export").path(S_CODE).path(sCode).build();
    }

    public static URI bidManagerCommentsAndCaveats(String customerId, String contractId, String projectId, String quoteOptionId) {
        return UriBuilder.fromUri(quoteOptionBcm(customerId, contractId, projectId, quoteOptionId))
            .path("bidmanagercomments").build();
    }

    public static URI cancelOfferApproval(String customerId, String contractId, String projectId, String quoteOptionId, String offerId) {
        return UriBuilder.fromUri(offers(customerId, contractId, projectId, quoteOptionId)).path(offerId).path("cancel-approval").build();
    }

    public static URI getUserImportUri(String customerId, String contractId, String projectId, String quoteOptionId, String sCode) {
        return UriBuilder.fromUri(quoteOption(customerId, contractId, projectId, quoteOptionId))
                         .path(S_CODE).path(sCode).path("user-import").build();
    }

    public static URI getUserImportValidateUri(String customerId, String contractId, String projectId, String quoteOptionId, String sCode) {
        return UriBuilder.fromUri(quoteOption(customerId, contractId, projectId, quoteOptionId))
                         .path(S_CODE).path(sCode).path("user-import-validate").build();
    }

    public static URI getUserImportStatusUri(String customerId, String contractId, String projectId, String quoteOptionId, String sCode) {
        return UriBuilder.fromUri(quoteOption(customerId, contractId, projectId, quoteOptionId))
                         .path(S_CODE).path(sCode).path("user-import-status").build();
    }

    public static URI getUserExportUri(String customerId, String contractId, String projectId, String quoteOptionId, String sCode) {
        return UriBuilder.fromUri(quoteOption(customerId, contractId, projectId, quoteOptionId))
                         .path(S_CODE).path(sCode).path("user-export").build();
    }
}