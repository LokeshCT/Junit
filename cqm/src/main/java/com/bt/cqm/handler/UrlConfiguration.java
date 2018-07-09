package com.bt.cqm.handler;

import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.Map;

public class UrlConfiguration {

    public static String build() {

        Map<String, String> urls = new HashMap<String, String>();

        urls.put("getCustomersByChannelUri", "/cqm/customer/identifiers");
        urls.put("createCustomerUri", "/cqm/customer");
        urls.put("updateCustomerAddDetUri", "/cqm/customer/updateCustomerAdditionalDetail");
        urls.put("getCustomerAddDetUri", "/cqm/customer/customerAdditionalDetail");
        urls.put("associateCustomerUri", "/cqm/customer/associate");
        urls.put("findCustomerUri", "/cqm/customer/new");
        urls.put("getAllAvailableSalesChannelUri", "/cqm/customer/allAvailableSalesChannel");
        urls.put("associateSalesChannelToContractUri", "/cqm/customer/associateSalesChannelToContract");
        urls.put("getPortDistributorUri", "/cqm/customer/getPortDistributor");
        urls.put("getContractByIdUri", "/cqm/customer/getContractById");
        urls.put("updateContractUri", "/cqm/customer/updateContract");
        urls.put("getClientGroupsUri", "/cqm/customer/clientGroups");

        urls.put("getSalesUserUri", "/cqm/salesUser");
        urls.put("searchAddressUri", "/cqm/addresses");
        urls.put("searchAddressByGeoCodeUri", "/cqm/addresses/geo-code");
        urls.put("getSiteContactsUri", "/cqm/contacts");
        urls.put("createContactUri", "/cqm/contacts/create");
        urls.put("updateContactUri", "/cqm/contacts/update");
        urls.put("searchCustomerUri", "/cqm/customer/customersList");
        urls.put("getCustomerDetailUri", "/cqm/customer/getCustomerDetail");
        urls.put("similarCustomersListUri", "/cqm/customer/similarCustomersList");
        urls.put("getCustomerContactsUri", "/cqm/contacts/customer");
        urls.put("getContractUri", "/cqm/customer/contract");
        urls.put("getCustomerContractsUri", "/cqm/customer/contractsForCustomer");
        urls.put("getCountiesUri", "/cqm/customer/countries");
        urls.put("getAllSalesChannelWithGfrUri", "/cqm/customer/allSalesChannelWithGfr");
        urls.put("createQuoteUri", "/cqm/quotes");
        urls.put("updateQuoteUri", "/cqm/quotes");
        urls.put("searchQuoteUri", "/cqm/quotes");
        urls.put("bundlingAppUrl", "/cqm/quotes/bundlingAppUrl");
        urls.put("sqeAppUrl", "/cqm/quotes/sqeAppUrl");
        urls.put("getVpnUri", "/cqm/vpn/customerId");
        urls.put("getSharedVpnDetailsUri", "/cqm/vpn/sharedCustomerId");
        urls.put("createSharedVpnDetailsUri", "/cqm/vpn/createSharedVPNServiceId");
        urls.put("deleteSharedVpnDetailsUri", "/cqm/vpn/createSharedVPNServiceId");

        urls.put("createSiteUri", "/cqm/site/updateCentralSite");
        urls.put("associateCentralSiteUri", "/cqm/site/associateCentralSite");
        urls.put("getCentralSiteUri", "/cqm/site/centralSite");
        urls.put("getBranchSiteUri", "/cqm/site/getBranchSite");
        urls.put("getBranchSiteNamesIdsUri", "/cqm/site/getBranchSiteNamesIds");
        urls.put("getBranchSiteCountUri", "/cqm/site/getBranchSiteCount");
        urls.put("createBranchSiteUri", "/cqm/site/createBranchSite");
        urls.put("updateBranchSiteUri", "/cqm/site/updateBranchSite");
        urls.put("getAPOPsUri", "/cqm/site/APOPS");
        urls.put("getGPOPsUri", "/cqm/site/GPOPS");
        urls.put("createLocationUri", "/cqm/site/createLocation");
        urls.put("getSiteStatus", "/cqm/site/isSiteActive");
        urls.put("siteUpdateNotification", "/cqm/site/notifySiteUpdate");
        urls.put("getSiteRegions", "/cqm/site/getSiteRegions");
        urls.put("createSiteRegion", "/cqm/site/createSiteRegion");
        urls.put("getMatchingCustomersUri", "/cqm/customer/customersList");
        urls.put("createNewCustomerUri", "/cqm/createCustomer");
        urls.put("createBranchSiteContactUri", "/cqm/branchContact/create");
        urls.put("updateBranchSiteContactUri", "/cqm/branchContact/update");
        urls.put("getParentAccountsUri", "/cqm/channelHierarchy/accountType");
        urls.put("createChannelPartnerUri", "/cqm/channelHierarchy/createChannelPartnerCustomerId");
        urls.put("getParentAccountNamesUri", "/cqm/channelHierarchy/accountType");
        urls.put("getChannelCreationDetailsUri", "/cqm/channelHierarchy/getChannelCreationDetails");
        urls.put("loadChannelPartnerDetailsUri", "/cqm/channelHierarchy/loadChannelPartnerDetails");
        urls.put("getProductNamesUri", "/cqm/pricebook/getProductNames");
        urls.put("getProductVersionsUri", "/cqm/pricebook/getProductVersions");
        urls.put("getPriceBookDetailsUri", "/cqm/pricebook/getPriceBookDetailsCustomerId");
        urls.put("getPriceBookCodes", "/cqm/pricebook/getPriceBookCodes");
        urls.put("savePriceBookUri", "/cqm/pricebook/savePriceBook");
        urls.put("updatePriceBookUri", "/cqm/pricebook/updatePricebook");
        urls.put("getPriceBookExtn", "/cqm/pricebook/getPricebookExtension");
        urls.put("createActivityUri", "/cqm/activity/create");
        urls.put("searchActivityUri", "/cqm/activity/searchActivity");
        urls.put("getAssignedToListUri", "/cqm/activity/assignedToList");
        urls.put("assignActiviyToUri", "/cqm/activity/assignTo");
        urls.put("approveActivityUri", "/cqm/activity/approve");
        urls.put("updateActivityStatusUri", "/cqm/activity/updateActivityStatus");
        urls.put("rejectActivityUri", "/cqm/activity/reject");
        urls.put("searchOrdersUri", "/cqm/order/search");
        urls.put("getOrderLineItemsUri", "/cqm/order/orderLineItems");
        urls.put("uploadAttachmentUri", "/cqm/order/uploadAttachment");
        urls.put("getAttachmentUri", "/cqm/order/getAttachment");
        urls.put("listAttachmentsUri", "/cqm/order/listAttachments");
        urls.put("cancelOrderUri", "/cqm/order/cancel");
        urls.put("cancelOrderUri", "/cqm/order/IFC");
        urls.put("requestIFCUri", "/cqm/order/IFC");
        urls.put("requestCancelUri", "/cqm/order/requestCancel");
        urls.put("getUserInfoUri", "/cqm/userManagement/getUserInfo");
        urls.put("getUserSubGroupsUri", "/cqm/userManagement/getUserSubGroups");
        urls.put("getSubGroupsUri", "/cqm/userManagement/getSubGroups");
        urls.put("addUserSubGroupsUri", "/cqm/userManagement/addUserSubGroup");
        urls.put("deleteUserSubGroupUri", "/cqm/userManagement/deleteUserSubGroup");
        urls.put("addSubGroupUri", "/cqm/userManagement/addSubGroup");


        urls.put("getAllRoleUri", "/cqm/userManagement/getAllRoles");
        urls.put("getAllSalesChannelUri", "/cqm/userManagement/getAllSalesChannels");
        urls.put("updateUserInfoUri", "/cqm/userManagement/saveUserInfo");
        urls.put("getBillingAccountsUri", "/cqm/billing/getBillingAccounts");
        urls.put("createBillingAccountUri", "/cqm/billing/createBilling");
        urls.put("updateBillingAccountUri", "/cqm/billing/updateBilling");
        urls.put("getTabsUri", "/cqm/tabs");
        urls.put("headerImgUri", "/cqm/static/cqm/web/img/mx_banner.jpg");
        urls.put("loginUri", "/cqm");
        urls.put("logoutUri", "/cqm/logout");
        urls.put("getLegalEntityUri", "/cqm/customer/legal-entities");
        urls.put("createLegalEntityUri", "/cqm/legalEntity/create");
        urls.put("updateLegalEntityUri", "/cqm/legalEntity/update");
        urls.put("mapLeBillingAccountUri", "/cqm/legalEntity/updatelinktocustomer");
        urls.put("vatPrefixUri", "/cqm/legalEntity/vatnumbervalidation");
        urls.put("getCurrencyCodes", "/cqm/billing/currencyCodes");
        urls.put("pinImgUri", "/cqm/static/cqm/web/img/pin.png");
        urls.put("redMarkerImgUri", "/cqm/static/cqm/web/img/red-dot-marker.png");
        urls.put("blueMarkerImgUri", "/cqm/static/cqm/web/img/blue-dot-marker.png");
        urls.put("greenMarkerImgUri", "/cqm/static/cqm/web/img/green-dot-marker.png");
        urls.put("webMetricsUri", "/rsqe/web-metrics");

        urls.put("auditQuoteSummaryUri", "/cqm/audit/quoteSummary");
        urls.put("auditOrderSummaryUri", "/cqm/audit/orderSummary");
        urls.put("auditQuoteDetailUri", "/cqm/audit/quoteDetail");
        urls.put("auditOrderDetailUri", "/cqm/audit/orderDetail");

        urls.put("reassignActivityUri", "/cqm/activity/reassign");
        urls.put("acceptDelegateUri", "/cqm/activity/acceptDelegation");
        urls.put("rejectDelegateUri", "/cqm/activity/rejectDelegation");
        urls.put("withdrawApprovalUri", "/cqm/activity/withdrawApproval");

        urls.put("generateQrefGuid", "/cqm/quotes/generateQrefGuid");

        urls.put("getSACTemplateUri", "/cqm/dslchecker/downloadTemplate");
        urls.put("importSacFileUri", "/cqm/dslchecker/uploadFile");
        urls.put("userUploadedFileListUri", "/cqm/dslchecker/userUploadedFileList");
        urls.put("allUploadedFileListUri", "/cqm/dslchecker/allUploadedFileList");
        urls.put("deleteBulkUploadUri", "/cqm/dslchecker/deleteBulkUpload");
        urls.put("clarityProjectCodeUri", "/cqm/billing/clarityProject");

        urls.put("recentQuoteStatusUrl", "/cqm/user/recent-quotes/status");
        urls.put("allUserQuoteStatusUrl", "/cqm/user/quotes/status");
        urls.put("getUserQuoteMetricsUrl", "/cqm/user/quotes/statistics");


        return new GsonBuilder().disableHtmlEscaping().create().toJson(urls);
    }

}
