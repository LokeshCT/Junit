package com.bt.rsqe.expedio.product;

import com.bt.rsqe.ContainerUtils;
import com.bt.rsqe.container.Application;
import com.bt.rsqe.container.ApplicationConfig;
import com.bt.rsqe.container.StubApplicationConfig;
import com.bt.rsqe.expedio.pricebook.PriceBookDTO;
import com.bt.rsqe.rest.ResponseBuilder;
import com.bt.rsqe.soap.WebServiceConfigException;
import com.bt.rsqe.utils.UriBuilder;
import com.bt.rsqe.web.rest.exception.BadRequestException;
import org.hamcrest.core.Is;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static com.google.common.collect.Lists.*;
import static org.junit.Assert.*;

/**
 * Created with IntelliJ IDEA.
 * User: 607982105
 * Date: 19/08/14
 * Time: 14:50
 * To change this template use File | Settings | File Templates.
 */
public class ProductResourceTest {

    private static ApplicationConfig applicationConfig = StubApplicationConfig.defaultTestConfig();
    private static Application application;
    private static ProductResource productResource;
    @BeforeClass
    public static void startContainer() throws IOException {
        application = ContainerUtils.startContainer(applicationConfig, new TestHandler());
        productResource = new ProductResource(UriBuilder.buildUri(applicationConfig), null);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void shouldGetPriceBookDetails() throws Exception {
        String customerId = "1234";
        List<PriceDetails> priceDetailsList = productResource.getPriceBookDetails(customerId);
        assertThat(priceDetailsList.size(), Is.is(1));
    }

    @Test
    public void shouldGetPriceBookDetailsForNullCustomerId() throws Exception {

        List<PriceDetails> priceDetailsList = productResource.getPriceBookDetails(null);
        assertEquals(priceDetailsList, null);
    }


    @Test
    public void shouldSaveBookDetails() throws Exception {
        PriceBookDTO priceBookDTO = new PriceBookDTO();
        priceBookDTO.setCustomerId("1222");

        boolean result = productResource.saveBookDetails(priceBookDTO);
        assertEquals(result, true);
    }

    @Test(expected = BadRequestException.class)
    public void shouldSaveBookDetailsForFailCase() throws Exception {
        PriceBookDTO priceBookDTO = new PriceBookDTO();
        priceBookDTO.setCustomerId(null);
        boolean result = productResource.saveBookDetails(priceBookDTO);
    }

    @Path("/rsqe/expedio/pricebook")
    public static class TestHandler {


        @GET
        @Path("/getPriceBookDetails")
        @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
        public Response getPriceBookDetails(@QueryParam("customerID") String customerID) throws URISyntaxException, WebServiceConfigException {
            GenericEntity<List<PriceDetails>> entity = new GenericEntity<List<PriceDetails>>(newArrayList(new PriceDetails("TestObject")
            )) {
            };
            return ResponseBuilder.anOKResponse().withEntity(entity).build();
        }

        @POST
        @Path("/savePriceBookDetails")
        public Response savePriceBookDetails(PriceBookDTO priceBookDTO) throws URISyntaxException, WebServiceConfigException {
            if (priceBookDTO.getCustomerId() == null) {
                return ResponseBuilder.badRequest().build();
            } else {
                return ResponseBuilder.anOKResponse().build();
            }


        }
    }
}
