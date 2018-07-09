'use strict';

describe('UrlConfigurationTest - ', function() {

    var doc;

    beforeEach(angular.mock.module('cqm.services'));

    beforeEach(inject(function($document) {
        doc = $document;
        spyOn(doc, 'find').andReturn(
            {
                text : function() {
                    return '{"createContactUri":"/cqm/contacts/create","getBunldingAppEndpointUri":"/cqm/quotes/bundlingAppUrl","requestCancelUri":"/cqm/order/requestCancel","updateActivityUri":"/cqm/activity/update","cancelOrderUri":"/cqm/order/IFC","getPriceBookDetailsUri":"/cqm/pricebook/getPriceBookDetailsCustomerId","uploadAttachmentUri":"/cqm/order/uploadAttachment","updateBillingAccountUri":"/cqm/billing/updateBilling","createBillingAccountUri":"/cqm/billing/createBilling","getUserInfoUri":"/cqm/userManagement/getUserInfo","searchOrdersUri":"/cqm/order/search","deleteSharedVpnDetailsUri":"/cqm/vpn/createSharedVPNServiceId","searchAddressUri":"/cqm/addresses","requestIFCUri":"/cqm/order/IFC","createSiteUri":"/cqm/siteUpdate/save","getProductVersionsUri":"/cqm/pricebook/getProductVersions","getChannelCreationDetailsUri":"/cqm/channelHierarchy/getChannelCreationDetails","getContractUri":"/cqm/customer/contract","listAttachmentsUri":"/cqm/order/listAttachments","searchAddressByGeoCodeUri":"/cqm/addresses/geo-code","createSharedVpnDetailsUri":"/cqm/vpn/createSharedVPNServiceId","createChannelPartnerUri":"/cqm/channelHierarchy/createChannelPartnerCustomerId","loadChannelPartnerDetailsUri":"/cqm/channelHierarchy/loadChannelPartnerDetails","getTabsUri":"/cqm/tabs","createQuoteUri":"/cqm/quotes","getSalesUserUri":"/cqm/salesUser","getCustomerContactsUri":"/cqm/contacts/customerContacts","getBillingAccountsUri":"/cqm/getBillingAccounts","updateUserInfoUri":"/cqm/userManagement/saveUserInfo","savePriceBookUri":"/cqm/pricebook/savePriceBook","updateContactUri":"/cqm/contacts/update","getParentAccountsUri":"/cqm/channelHierarchy/accountType","getParentAccountNamesUri":"/cqm/channelHierarchy/accountType","searchActivityUri":"/cqm/activity/searchActivity","searchQuoteUri":"/cqm/quotes","getSharedVpnDetailsUri":"/cqm/vpn/sharedCustomerId","getCentralSiteUri":"/cqm/customer/centralSite","searchCustomerUri":"/cqm/customer/customersList","getCustomersByChannelUri":"/cqm/customer/salesChannel","getProductNamesUri":"/cqm/pricebook/getProductNames","getActionsTreeUri":"/cqm/user-action-tree","getCountiesUri":"/cqm/customer/countries","getVpnUri":"/cqm/vpn/customerId","createActivityUri":"/cqm/activity/create","getAttachmentUri":"/cqm/order/getAttachment","getAssignedToListUri":"/cqm/activity/assignedToList"}';
                }
            }
        );
    }));


    it('should provide url configuration details', inject(function(UrlConfiguration) {
        expect(UrlConfiguration.requestCancelUri).toBe("/cqm/order/requestCancel");
        expect(doc.find).toHaveBeenCalledWith("#urlConfig");
    }));
});