package com.bt.nrm.handler.interfaces;

import com.bt.nrm.repository.QuoteOptionRequestRepository;
import com.bt.pms.resources.PMSResource;
import com.bt.rsqe.utils.RSQEMockery;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by 608143048 on 14/12/2015.
 */
public class NonStandardRequestResponseHandlerTest {

    private PMSResource pmsResource;
    private QuoteOptionRequestRepository requestRepository;
    private NonStandardRequestResponseHandler nonStandardRequestResponseHandler;

    private final Mockery context = new RSQEMockery();

    @Before
    public void setUp(){
        try {
            pmsResource = context.mock(PMSResource.class);
            requestRepository = context.mock(QuoteOptionRequestRepository.class);
            //nonStandardRequestResponseHandler = new NonStandardRequestResponseHandler(pmsResource,requestRepository);
        } catch (Throwable ex) {
            System.out.println("Fail to create setup for NonStandardRequestResponseHandlerTest." + ex);
        }
    }

    @Test
    public void shouldNotCreateNonStandardRequestWhenRequestDTOisNull() throws Exception{
        /*NonStandardRequestDTO requestDTO = null;
        Response res = nonStandardRequestResponseHandler.createNonStandardRequest(requestDTO);
        assertEquals(res.getStatus(),400);*/
    }

    @Test
    public void shouldNotCreateNonStandardRequestWhenRequestDTO() throws Exception{
        //Fill Test Data
       /* List<NonStandardRequestAttributeDTO> commonDetails = new ArrayList<NonStandardRequestAttributeDTO>();
        commonDetails.add(new NonStandardRequestAttributeDTO());
        commonDetails.add(new NonStandardRequestAttributeDTO());
        List<NonStandardRequestSiteDTO> sites = new ArrayList<NonStandardRequestSiteDTO>();
        NonStandardRequestDTO requestDTO = new NonStandardRequestDTO(
                null, "Request Name Test", "30/01/2016", "SB00001", "V1.1", "False", "15", "Test BidManager Name",
                new QuoteDTO("1", "1", "QuoteName", "1", "QuoteOptionName", "GBP", "12 Months", "", "90%", "rSQE", "CustomerId1", "CustomerName", "CustomerMoreInfo", "OppRefNo", "AlternateContact",
                        "SalesChannelId1", "SalesChannelName", "Direct", "100000", "USD","Mittal Patel",new Date(),"608143048"),
                new ProductDTO("H0301101", "Internet Connect Global", "S0319798", "CPE"),
                new UserDTO("608143048", "mittal.2.patel@bt.com", "Mittal", "Patel"),
                commonDetails, sites);
        //Calling the interfaces method to create request
        Response response = nonStandardRequestResponseHandler.createNonStandardRequest(requestDTO);
        assertEquals(response.getStatus(), 200);
        //Assert if the request got created and request id got generated
        String requireId = (String) response.getEntity();
        assertNotNull(requireId);*/
    }


}
