package com.bt.rsqe.expedio.services.quote;

import com.bt.rsqe.ContainerUtils;
import com.bt.rsqe.container.Application;
import com.bt.rsqe.container.ApplicationConfig;
import com.bt.rsqe.container.StubApplicationConfig;
import com.bt.rsqe.rest.ResponseBuilder;
import com.bt.rsqe.utils.UriBuilder;
import com.bt.rsqe.web.rest.exception.InternalServerErrorException;
import org.hamcrest.core.Is;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertThat;

public class QuoteResourceTest {
    private static Application application;
    private static ApplicationConfig applicationConfig = StubApplicationConfig.defaultTestConfig();
    private static QuoteResource quoteResource;
    private static final String SALES_CHANNEL = "BT INDIA";
    private static final String CUSTOMER_ID = "customerId";
    private static final String CONTRACT_ID = "204768";
    private static final String GUID = "12345";
    private static final String QUOTE_ID = "12345";
    private static final String RELATIVE_PATH = "/rsqe/expedio/quotes";

    @BeforeClass
    public static void beforeClass() throws IOException {
        application = ContainerUtils.startContainer(applicationConfig, new Handler());
        quoteResource = new QuoteResource(UriBuilder.buildUri(applicationConfig), null);
    }

    @Test
    public void shouldCreateQuote() throws Exception {
        final QuoteCreationDTO quoteCreationDTO = new QuoteCreationDTO();
        quoteCreationDTO.setSalesChannel(SALES_CHANNEL);
        quoteCreationDTO.setBfgContractID(110L);
        quoteCreationDTO.setBfgCustomerID("bfgCustomerId");
        quoteCreationDTO.setBidNumber("bidNumber");
        quoteCreationDTO.setBoatID("boatId");
        quoteCreationDTO.setContractTerm("contractTerm");
        quoteCreationDTO.setCurrency("currency");
        quoteCreationDTO.setEIN("ein");
        quoteCreationDTO.setOpportunityReferenceNumber("opportunityReferenceNumber");
        quoteCreationDTO.setOrderType("orderType");
        quoteCreationDTO.setQuoteIndicativeFlag("quoteIndicativeFlag");
        quoteCreationDTO.setQuoteName("quoteName");
        quoteCreationDTO.setRoleType("roleType");
        quoteCreationDTO.setSubOrderType("subOrderType");
        quoteCreationDTO.setTradeLevel("tradeLevel");
        quoteCreationDTO.setUserEmailId("userEmailId");
        quoteCreationDTO.setSalesRepName("salesRepresentativeName");
        String newQuoteGUID = quoteResource.createQuote(quoteCreationDTO);
        assert (newQuoteGUID != null);
        assert (newQuoteGUID.equals(GUID));
    }

    @Test
    public void shouldUpdateQuote() {
        final QuoteUpdateDTO quoteUpdateDTO = new QuoteUpdateDTO();
        quoteUpdateDTO.setQuoteName("TEST");
        Boolean isSuccess = quoteResource.updateQuote(quoteUpdateDTO);
        assert (isSuccess);
    }

    @Test(expected = InternalServerErrorException.class)
    public void shouldUpdateQuoteThrowRestException() {
        final QuoteUpdateDTO quoteUpdateDTO = new QuoteUpdateDTO();
        Boolean isSuccess = quoteResource.updateQuote(quoteUpdateDTO);
    }

    @Test
    public void shouldGenerateGUID() throws Exception {
        QuoteLaunchConfiguratorDTO quoteLaunchConfiguratorDTO = QuoteLaunchConfiguratorDTO.builder().withQuoteID(QUOTE_ID).build();
        String generatedGUID = quoteResource.generateGUID(quoteLaunchConfiguratorDTO);
        assert (generatedGUID != null);
        assert (generatedGUID.equals(GUID));
    }

    @Test
    public void shouldGetQuotes() throws Exception {
        List<QuoteDetailsDTO> quoteDetailsDTOList = quoteResource.getQuotes(SALES_CHANNEL, CUSTOMER_ID,CONTRACT_ID);
        assert (quoteDetailsDTOList != null);
        assertThat(quoteDetailsDTOList.size(), Is.is(1));
        assertThat(((QuoteDetailsDTO) quoteDetailsDTOList.get(0)).getQuoteReferenceId(), Is.is(QUOTE_ID));
    }

    @Test
    public void shouldGenerateQrefGuid(){
        QrefGenGuidDTO qrefGenGuidDTO = new QrefGenGuidDTO();
        qrefGenGuidDTO.setSalesChannel("BT AMERICAS");
        String guid =quoteResource.generateQrefGuid(qrefGenGuidDTO);
        assert("1001011111".equals(guid));
    }

    @AfterClass
    public static void afterClass() throws IOException {
        ContainerUtils.stop(application);
    }

    @Path(RELATIVE_PATH)
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public static class Handler {
        @POST
        public Response createQuote(QuoteCreationDTO quoteCreationDTO) throws QuoteNotFoundException {
            GUID guid = new GUID();
            guid.setGUID(GUID);
            assertThat(quoteCreationDTO.getSalesChannel(), Is.is(SALES_CHANNEL));
            return ResponseBuilder.anOKResponse().withEntity(guid.getGUID()).build();
        }

        @PUT
        public Response updateQuote(QuoteUpdateDTO quoteUpdateDTO) throws QuoteNotFoundException {
            GUID guid = new GUID();
            guid.setGUID(GUID);
            if(quoteUpdateDTO.getQuoteName()!=null){
            return ResponseBuilder.anOKResponse().build();
            }else{
                return ResponseBuilder.internalServerError().build();
            }
        }

        @POST
        @Path("guid")
        public Response generateGUID(QuoteLaunchConfiguratorDTO quoteLaunchConfiguratorDTO) {
            assertThat(quoteLaunchConfiguratorDTO.getQuoteID(), Is.is(QUOTE_ID));
            GUID guid = new GUID();
            guid.setGUID(GUID);
            return ResponseBuilder.anOKResponse().withEntity(guid.getGUID()).build();
        }

        @POST
        @Path("/generateQrefGuid")
        public Response generateQrefGuid(QrefGenGuidDTO qrefGenGuidDTO){

            if("BT AMERICAS".equals(qrefGenGuidDTO.getSalesChannel())){
                return  ResponseBuilder.anOKResponse().withEntity("1001011111").build();
            }else{
                return  ResponseBuilder.internalServerError().withEntity("Some Error!!").build();
            }

        }

        @GET
        @Path("direct")
        public Response getQuotes(@QueryParam("salesChannel") String salesChannel, @QueryParam("customerID") String customerID,@QueryParam("contractID") String contractId) {
            assertThat(salesChannel, Is.is(SALES_CHANNEL));
            assertThat(customerID, Is.is(CUSTOMER_ID));

            QuoteDetailsDTO quoteDetailsDTO = new QuoteDetailsDTO();
            quoteDetailsDTO.setQuoteReferenceId(QUOTE_ID);
            quoteDetailsDTO.setContractId(CONTRACT_ID);
            List<QuoteDetailsDTO> quoteDDTOList = newArrayList(quoteDetailsDTO);

            QuoteSearchResult quoteSearchResult = new QuoteSearchResult();
            quoteSearchResult.setQuoteList(quoteDDTOList);

            return ResponseBuilder.anOKResponse().withEntity(quoteSearchResult).build();
        }
    }
}
