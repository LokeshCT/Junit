package com.bt.cqm.handler;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.hamcrest.core.Is;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class UrlConfigurationTest {

    @Test
    public void shouldBuildUrlConfigurationJsonString() {
        String urlConfig = UrlConfiguration.build();
        JsonObject jsonObject = new JsonParser().parse(urlConfig).getAsJsonObject();

        assertThat(jsonObject.get("getSharedVpnDetailsUri").getAsString(), Is.is("/cqm/vpn/sharedCustomerId"));
        assertThat(jsonObject.get("headerImgUri").getAsString(), Is.is("/cqm/static/cqm/web/img/mx_banner.jpg"));
    }


    @Test
    public void should() {
       Map<String, String> urls = new HashMap<String, String>();

        urls.put("getCustomersByChannelUri", "../../../../test/resources/web/data/customers.json");
        urls.put("getSalesUserUri", "../../../../test/resources/web/data/salesUser.json");
        //urls.put("getSiteContactsUri", "/cqm/contacts");
        //urls.put("createContactUri", "/cqm/contacts/create");
        urls.put("updateContactUri", "../../../../test/resources/web/data/contracts.json");
        //urls.put("createSiteUri", "/cqm/siteUpdate/save");
        //urls.put("getCentralSiteUri", "/cqm/customer/centralSite");
        //urls.put("searchCustomerUri", "/cqm/customer/customersList");
        //urls.put("getCustomerContactsUri", "/cqm/contacts/customerContacts");
        //urls.put("getContractUri", "/cqm/customer/contract");
        //urls.put("getCountiesUri", "/cqm/customer/countries");
        //urls.put("createQuoteUri", "/cqm/quotes");
        //urls.put("searchQuoteUri", "/cqm/quotes");
        //urls.put("getBunldingAppEndpointUri", "/cqm/quotes/getBundlingAppUrl");
        //urls.put("getVpnUri", "/cqm/vpn/customerId");
        //urls.put("getSharedVpnDetailsUri", "/cqm/vpn/sharedCustomerId");
        //urls.put("createSharedVpnDetailsUri", "/cqm/vpn/createSharedVPNServiceId");
        //urls.put("deleteSharedVpnDetailsUri", "/cqm/vpn/createSharedVPNServiceId");
        //urls.put("getBranchSiteUri", "/cqm/branchSite/getBranchSite");
        //urls.put("getMatchingCustomersUri", "/cqm/customer/customersList");
        //urls.put("getSelectedCustomerContactsUri", "/cqm/searchCustomer/customerContacts");
        //urls.put("createNewCustomerUri", "/cqm/createCustomer");
        //urls.put("createBranchSiteUri", "/cqm/branchSite/create");
        //urls.put("updateBranchSiteUri", "/cqm/branchSite/save");
        //urls.put("createBranchSiteContactUri", "/cqm/branchContact/create");
        //urls.put("updateBranchSiteContactUri", "/cqm/branchContact/update");
        //urls.put("getParentAccountsUri", "/cqm/channelHierarchy/accountType");
        //urls.put("createChannelPartnerUri", "/cqm/channelHierarchy/createChannelPartnerCustomerId");
        //urls.put("getParentAccountNamesUri", "/cqm/channelHierarchy/accountType");
        //urls.put("getChannelCreationDetailsUri", "/cqm/channelHierarchy/getChannelCreationDetails");
        //urls.put("loadChannelPartnerDetailsUri", "/cqm/channelHierarchy/loadChannelPartnerDetails");
        //urls.put("getProductNamesUri", "/cqm/pricebook/getProductNames");
        //urls.put("getProductVersionsUri", "/cqm/pricebook/getProductVersions");
        //urls.put("getPriceBookDetailsUri", "/cqm/pricebook/getPriceBookDetailsCustomerId");
        //urls.put("savePriceBookUri", "/cqm/pricebook/savePriceBook");
        //urls.put("createActivityUri", "/cqm/activity/create");
        //urls.put("searchActivityUri", "/cqm/activity/searchActivity");
        //urls.put("getAssignedToListUri", "/cqm/activity/assignedToList");
        //urls.put("updateActivityUri", "/cqm/activity/update");
        //urls.put("searchOrdersUri", "/cqm/order/search");
        //urls.put("uploadAttachmentUri", "/cqm/order/uploadAttachment");
        //urls.put("getAttachmentUri", "/cqm/order/getAttachment");
        //urls.put("listAttachmentsUri", "/cqm/order/listAttachments");
        //urls.put("cancelOrderUri", "/cqm/order/cancel");
        //urls.put("cancelOrderUri", "/cqm/order/IFC");
        //urls.put("requestIFCUri", "/cqm/order/IFC");
        //urls.put("requestCancelUri", "/cqm/order/requestCancel");
        //urls.put("getUserInfoUri", "/cqm/userManagement/getUserInfo");
        //urls.put("updateUserInfoUri", "/cqm/userManagement/saveUserInfo");
        //urls.put("getBillingAccountsUri", "/cqm/getBillingAccounts");
        //urls.put("createBillingAccountUri", "/cqm/billing/createBilling");
        //urls.put("updateBillingAccountUri", "/cqm/billing/updateBilling");
        //urls.put("getTabsUri", "/cqm/tabs");
        //urls.put("headerImgUri", "/cqm/static/img/mx_banner.jpg");
        //urls.put("loginUri", "/cqm");

        String urlConfig = new GsonBuilder().disableHtmlEscaping().create().toJson(urls);
        System.out.println(urlConfig);
    }
}
