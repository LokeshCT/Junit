package com.bt.rsqe.expedio.services.quote;

import com.bt.rsqe.customerrecord.client.ExpedioFacadeConfig;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.bt.rsqe.web.rest.ProxyAwareRestRequestBuilder;
import com.bt.rsqe.web.rest.RestRequestBuilder;
import com.bt.rsqe.web.rest.RestResponse;

import javax.ws.rs.core.GenericType;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Strings.*;

public class QuoteResource {

    private static final String REL_PATH_CREATE_CHANNELCONTACT = "createQuoteChannelContact";
    private static final String REL_PATH_UPDATE_CHANNELCONTACT = "updateQuoteChannelContact";
    private static final String REL_PATH_SELECT_CHANNELCONTACT = "getChannelContacts";
    private static final String REL_PATH_DELETE_CHANNELCONTACT = "deleteQuoteChannelContact";
    private static final String REL_PATH_GEN_QREF_GUID = "generateQrefGuid";
    private static final String SALES_CHANNEL_QUERY_PARAM = "salesChannel";
    private static final String CUSTOMER_ID_QUERY_PARAM = "customerID";
    private static final String CONTRACT_ID_QUERY_PARAM = "contractID";
    private static final String GUID = "guid";
    private static final String SUCCESS = "SUCCESS";
    private static final String FAIL = "FAIL";

    private RestRequestBuilder restRequestBuilder;
    private static QuoteResourceLogger LOG = LogFactory.createDefaultLogger(QuoteResourceLogger.class);


    public QuoteResource(URI baseURI, String secret) {
        URI uri = com.bt.rsqe.utils.UriBuilder.buildUri(baseURI, "rsqe", "expedio", "quotes");
        this.restRequestBuilder = new ProxyAwareRestRequestBuilder(uri).withSecret(secret);
    }

    public QuoteResource(ExpedioFacadeConfig clientConfig) {
        this(com.bt.rsqe.utils.UriBuilder.buildUri(clientConfig.getApplicationConfig()), clientConfig.getRestAuthenticationClientConfig().getSecret());
    }

    public String createQuote(QuoteCreationDTO quoteCreationDTO) throws QuoteNotFoundException {
        String guID = null;
        RestResponse restResponse = this.restRequestBuilder.build().post(quoteCreationDTO);
        if (Response.Status.OK.getStatusCode() == restResponse.getStatus()) {
            guID = restResponse.getEntity(String.class);

            if (isNullOrEmpty(guID)) {
                throw new QuoteNotFoundException("Error while creating quote. Reason: Expedio is not able to create quote");
            }
        } else {
            throw new QuoteNotFoundException("Error while creating quote. Reason: " + restResponse.getClientResponseStatus().toString());
        }
        return guID;
    }

    public Boolean updateQuote(QuoteUpdateDTO quoteUpdateDTO) {
        RestResponse restResponse = this.restRequestBuilder.build().put(quoteUpdateDTO);
        if (restResponse.getStatus() == Response.Status.OK.getStatusCode()) {
            return true;
        } else {
            return false;
        }
    }

    public String generateGUID(QuoteLaunchConfiguratorDTO quoteLaunchConfiguratorDTO) throws Exception {
        String guID = null;
        try {
            RestResponse restResponse = this.restRequestBuilder.build(GUID).post(quoteLaunchConfiguratorDTO);
            if (Response.Status.OK.getStatusCode() == restResponse.getStatus()) {
                guID = restResponse.getEntity(String.class);
                if (isNullOrEmpty(guID)) {
                    throw new QuoteNotFoundException("Error while fetching GUID.");
                }
            }
        } catch (Exception e) {
            LOG.error(e);
            throw e;
        }
        return guID;
    }


    public List<QuoteDetailsDTO> getQuotes(final String salesChannel, final String customerID, final String contractId) throws QuoteNotFoundException {
        RestResponse restResponse = this.restRequestBuilder.build("direct",new HashMap<String, String>() {{
            put(SALES_CHANNEL_QUERY_PARAM, salesChannel);
            put(CUSTOMER_ID_QUERY_PARAM, customerID);
            put(CONTRACT_ID_QUERY_PARAM, contractId);
        }}).get();
        QuoteSearchResult quoteSearchResult = null;
        if (Response.Status.OK.getStatusCode() == restResponse.getStatus()) {
            quoteSearchResult = restResponse.getEntity(new GenericType<QuoteSearchResult>() {
            });
            if (quoteSearchResult == null || quoteSearchResult.getQuoteList() == null) {
                throw new QuoteNotFoundException("No Quotes available.");
            }
        } else {
            throw new QuoteNotFoundException("Error while fetching quote. Reason: " + restResponse.getClientResponseStatus().toString());
        }
        return quoteSearchResult.getQuoteList();
    }

    public String createQuoteChannelContact(ChannelContactCreateDTO quoteChannelContactDTO) {
        RestResponse restResponse = this.restRequestBuilder.build(REL_PATH_CREATE_CHANNELCONTACT).post(quoteChannelContactDTO);
        String message = restResponse.getEntity(new GenericType<String>() {
        });
        if (restResponse.getStatus() == Response.Status.OK.getStatusCode()) {
            return SUCCESS;
        } else {
            LOG.exception(quoteChannelContactDTO.getQuoteID() + ":" + message);
            return FAIL;
        }
    }

    public String updateQuoteChannelContact(QuoteChannelContactDTO quoteChannelContactDTO) {
        RestResponse restResponse = this.restRequestBuilder.build(REL_PATH_UPDATE_CHANNELCONTACT).post(quoteChannelContactDTO);
        String message = restResponse.getEntity(new GenericType<String>() {
        });
        if (restResponse.getStatus() == Response.Status.OK.getStatusCode()) {
            return SUCCESS;
        } else {
            LOG.exception(quoteChannelContactDTO.getChannelContactID() + " : " + message);
            return FAIL;
        }
    }

    public String deleteQuoteChannelContact(List<DeleteChannelContactDTO> deleteList) {
        DeleteChannelContactList deleteChannelContactList = new DeleteChannelContactList();
        deleteChannelContactList.setDeleteContacts(deleteList);
        RestResponse restResponse = this.restRequestBuilder.build(REL_PATH_DELETE_CHANNELCONTACT).post(deleteChannelContactList);
        String message = restResponse.getEntity(new GenericType<String>() {
        });
        if (restResponse.getStatus() == Response.Status.OK.getStatusCode()) {
            return SUCCESS;
        } else {
            LOG.exception(deleteList.get(0).getChannelContactID() + " : " + message);
            return FAIL;
        }
    }

    public List<QuoteChannelContactDTO> getQuoteChannelContacts(String quoteId) {
        Map<String, String> qParam = new HashMap<String, String>();
        qParam.put("quoteId", quoteId);
        RestResponse restResponse = this.restRequestBuilder.build(REL_PATH_SELECT_CHANNELCONTACT, qParam).get();

        ChannelContactDetails channelContactDetails = restResponse.getEntity(new GenericType<ChannelContactDetails>() {
        });
        if (channelContactDetails != null) {
            return channelContactDetails.getContacts();
        } else {
            return null;
        }
    }

    public String generateQrefGuid(QrefGenGuidDTO qrefGenGuidDTO) {
        if (qrefGenGuidDTO != null) {
            return this.restRequestBuilder.build(REL_PATH_GEN_QREF_GUID).post(qrefGenGuidDTO).getEntity(String.class);
        } else {
            throw new NullPointerException("Invalid input.");
        }
    }


    private interface QuoteResourceLogger {
        @Log(level = LogLevel.INFO)
        void info(long endTimeId);

        @Log(level = LogLevel.ERROR)
        void error(Exception faultDesc);

        @Log(level = LogLevel.DEBUG)
        void exception(String faultDesc);

    }
}
