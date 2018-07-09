package com.bt.rsqe.ape;

import com.bt.rsqe.ape.client.APEClient;
import com.bt.rsqe.ape.dto.ApeInterimSiteDTO;
import com.bt.rsqe.ape.dto.ApeQrefRequestDTO;
import com.bt.rsqe.customerrecord.CustomerDTO;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.security.UserDTO;
import com.bt.rsqe.security.UserType;
import org.junit.Before;
import org.junit.Test;

import static com.bt.rsqe.utils.AssertObject.isEmpty;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class APEInterimSiteResourceHandlerTest {

    private APEClient apeClient;
    private ApeInterimSiteResourceHandler apeInterimSiteResourceHandler;
    private final static String INTERIM_SITE_ID = "interim123";
    private SiteDTO siteDTO;
    private UserDTO userDTO;
    private CustomerDTO customerDTO;
    private ApeQrefRequestDTO apeQrefRequestDTO;

    @Before
    public void setup() {
        apeClient = mock(APEClient.class);
        apeInterimSiteResourceHandler = new ApeInterimSiteResourceHandler(apeClient);

        siteDTO = new SiteDTO("112", "aSiteName");
        siteDTO.country = "United Kingdom";

        userDTO = new UserDTO("f1", "s1", "e@a.b", UserType.DIRECT, "01", "fs1", "1");

        customerDTO = new CustomerDTO("CustomerId", "someName", "BT GERMANY", "123", "ContractId");

        apeQrefRequestDTO = new ApeQrefRequestDTO("", customerDTO, siteDTO, userDTO, null, null, null, null, null, null,
                                                                    null, null, null, null, "","1234", "5678");
    }

    @Test
    public void shouldCallProvideQuoteForGlobalPricingForInterimSite() throws Exception {

        MultisiteResponse apeResponse = new MultisiteResponse();
        apeResponse.setRequestId(INTERIM_SITE_ID);
        apeResponse.setNoOfSites(1);
        apeResponse.setComments("SUCCESS");

        Sites sites = new Sites(Integer.parseInt(siteDTO.getSiteId().toString()), siteDTO.getSiteName(), "SUCCESS","");
        apeResponse.setErrorMessages(new ErrorMessages(new Sites[]{sites}));

        when(apeClient.provideQuoteForGlobalPricing(any(SqeAccessInputDetails.class))).thenReturn(apeResponse);

        ApeInterimSiteDTO apeInterimSiteDTO = (ApeInterimSiteDTO)apeInterimSiteResourceHandler.getInterimSite(apeQrefRequestDTO).getEntity();

        assertThat(apeInterimSiteDTO.getInterimSiteId(),is(INTERIM_SITE_ID));
    }

    @Test
    public void shouldCallProvideQuoteForGlobalPricingForFailedInterimSite() throws Exception {

        MultisiteResponse apeResponse = new MultisiteResponse();
        apeResponse.setRequestId("");
        apeResponse.setComments("FAILURE");

        Sites sites = new Sites(Integer.parseInt(siteDTO.getSiteId().toString()), siteDTO.getSiteName(), "FAILURE","");
        apeResponse.setErrorMessages(new ErrorMessages(new Sites[]{sites}));

        when(apeClient.provideQuoteForGlobalPricing(any(SqeAccessInputDetails.class))).thenReturn(apeResponse);

        ApeInterimSiteDTO apeInterimSiteDTO = (ApeInterimSiteDTO)apeInterimSiteResourceHandler.getInterimSite(apeQrefRequestDTO).getEntity();

        assertTrue(isEmpty(apeInterimSiteDTO.getInterimSiteId()));
    }
}
