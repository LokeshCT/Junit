package com.bt.cqm.handler;

import com.bt.cqm.dto.ChannelHierarchyDTO;
import com.bt.rsqe.customerinventory.client.CustomerInventoryClientConfig;
import com.bt.rsqe.web.rest.ProxyAwareRestRequestBuilder;
import com.bt.rsqe.web.rest.RestRequestBuilder;
import com.bt.rsqe.web.rest.RestResponse;
import javax.ws.rs.core.GenericType;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 607937181 on 06/06/2014.
 */
public class ChannelHierarchyResource {

    private static final String CUSTOMER_ID = "customerId";
    private static final String LOADCHANNELPARTNER = "loadchannelpartner";
    private RestRequestBuilder restRequestBuilder;

    public ChannelHierarchyResource(URI baseURI, String secret) {
        URI uri = com.bt.rsqe.utils.UriBuilder.buildUri(baseURI, "rsqe", "customer-inventory", "channelHierarchy");
        this.restRequestBuilder = new ProxyAwareRestRequestBuilder(uri).withSecret(secret);
    }

    public ChannelHierarchyResource(CustomerInventoryClientConfig customerInventoryClientConfig) {
        this(com.bt.rsqe.utils.UriBuilder.buildUri(customerInventoryClientConfig.getApplicationConfig()),
             customerInventoryClientConfig.getRestAuthenticationClientConfig().getSecret());
    }

    public  List<ChannelHierarchyDTO> getParentAccountNames(String accountType, String salesChannel) throws Exception {
        List<String> list = null;
        List<ChannelHierarchyDTO> channelHierarchyDTO = null;
        Map<String, String> qParam = new HashMap<String, String>();

        if (accountType != null && salesChannel != null) {
            qParam.put("accountType", accountType);
            qParam.put("salesChannel", salesChannel);
        } else {
            return null;
        }
        RestResponse restResponse = this.restRequestBuilder.build("parentaccount", qParam).get();
        channelHierarchyDTO = restResponse.getEntity(new GenericType<List<ChannelHierarchyDTO>>() {
        });
        return channelHierarchyDTO;
    }

    public ChannelHierarchyDTO loadChannelPartnerDetailsOfCustomer(String customerId) throws Exception {
        ChannelHierarchyDTO channelHierarchyDTO = null;
        Map<String, String> qParam = new HashMap<String, String>();

        if (customerId != null) {
            qParam.put(CUSTOMER_ID, customerId);
        } else {
            return null;
        }
        RestResponse restResponse = this.restRequestBuilder.build(LOADCHANNELPARTNER, qParam).get();
        channelHierarchyDTO = restResponse.getEntity(new GenericType<ChannelHierarchyDTO>() {
        });
        return channelHierarchyDTO;

    }

    public ChannelHierarchyDTO getChannelCreationDetails(String parentAccountName, String customerId) {
        ChannelHierarchyDTO channelHierarchyDTO = null;

        Map<String, String> qParam = new HashMap<String, String>();

        if (parentAccountName != null && customerId != null) {
            qParam.put("parentAccountName", parentAccountName);
            qParam.put(CUSTOMER_ID, customerId);
        } else {
            return null;
        }
        RestResponse restResponse = this.restRequestBuilder.build("channelcreation", qParam).get();
        channelHierarchyDTO = restResponse.getEntity(new GenericType<ChannelHierarchyDTO>() {
        });
        return channelHierarchyDTO;
    }

    public ChannelHierarchyDTO getChannelPartnerDetails(String customerId) {
        ChannelHierarchyDTO channelHierarchyDTO = null;

        Map<String, String> qParam = new HashMap<String, String>();
        if (customerId != null) {
            qParam.put(CUSTOMER_ID, customerId);
        } else {
            return null;
        }
        RestResponse restResponse = this.restRequestBuilder.build(LOADCHANNELPARTNER, qParam).get();
        channelHierarchyDTO = restResponse.getEntity(new GenericType<ChannelHierarchyDTO>() {
        });

        return channelHierarchyDTO;
    }

    public ChannelHierarchyDTO getBillingAccount(String customerId) {
        ChannelHierarchyDTO channelHierarchyDTO = null;

        Map<String, String> qParam = new HashMap<String, String>();

        if (customerId != null) {
            qParam.put(CUSTOMER_ID, customerId);
        } else {
            return null;
        }
        RestResponse restResponse = this.restRequestBuilder.build("billingaccount", qParam).get();
        channelHierarchyDTO = restResponse.getEntity(new GenericType<ChannelHierarchyDTO>() {
        });
        return channelHierarchyDTO;
    }

    public String createChannelPartner(String customerId, String accountType, String parentCustomerName, String parentAccountReference, String billingAccount, String yearlyCommittedRev, String salesChannelType, String tradeLevel, String salesChannelOrg,
                                       String customerName) throws Exception {

        Map<String, String> qParam = new HashMap<String, String>();
        String response = null;
        qParam.put(CUSTOMER_ID, customerId);
        qParam.put("accountType", accountType);
        qParam.put("parentCustomerName", parentCustomerName);
        qParam.put("parentAccountReference", parentAccountReference);
        qParam.put("billingAccount", billingAccount);
        qParam.put("yearlyCommittedRev", yearlyCommittedRev);
        qParam.put("salesChannelType", salesChannelType);
        qParam.put("tradeLevel", tradeLevel);
        qParam.put("salesChannelOrg", salesChannelOrg);
        qParam.put("customerName", customerName);

        RestResponse restResponse = this.restRequestBuilder.build("createChannelPartnerCustomerId", qParam).get();
        response = restResponse.getEntity(new GenericType<String>() {
        });
        return response;
    }

}
