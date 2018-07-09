package com.bt.cqm.handler;


import com.bt.rsqe.customerinventory.client.resource.ContractResourceClient;

import com.bt.rsqe.customerinventory.client.resource.SiteResourceClient;
import com.bt.rsqe.customerinventory.dto.SiteLocationDTO;
import com.bt.rsqe.customerinventory.dto.contract.ContractDTO;
import com.bt.rsqe.customerinventory.dto.customer.CustomerDTO;
import com.bt.rsqe.customerinventory.dto.site.SiteUpdateDTO;
import com.bt.rsqe.customerinventory.resources.CustomerResource;
import com.bt.rsqe.customerinventory.resources.SiteLocationResource;
import com.bt.rsqe.domain.product.Identifier;
import com.bt.rsqe.emppal.attachmentresource.EmpPalResource;
import com.bt.rsqe.expedio.services.ActivityResource;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.apache.commons.lang.StringUtils;
import org.hamcrest.core.Is;
import org.junit.Test;
import java.util.List;

import javax.ws.rs.core.Response;

import static com.bt.rsqe.utils.GsonUtil.*;
import static com.google.common.collect.Lists.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CustomerResourceHandlerTest {
    private static final String SALES_CHANNEL = "BT INDIA";
    private static final Long CUSTOMER_ID = 2345678L;
    private ContractResourceClient contractResource = mock(ContractResourceClient.class);
    private CustomerResource customerResourceMock = mock(CustomerResource.class);
    private SiteResourceClient siteResourceClientMock = mock(SiteResourceClient.class);
    private SiteLocationResource siteLocationResourceMock = mock(SiteLocationResource.class);
    private ActivityResource activityResourceMock = mock(ActivityResource.class);
    private EmpPalResource empPalResourceMock = mock(EmpPalResource.class);
    private com.bt.rsqe.customerrecord.CustomerResource expedioCustomerResourceMock = mock(com.bt.rsqe.customerrecord.CustomerResource.class);
    private CustomerResourceHandler customerResourceHandler = new CustomerResourceHandler(expedioCustomerResourceMock,customerResourceMock, siteResourceClientMock, siteLocationResourceMock, contractResource,activityResourceMock,empPalResourceMock);
    private JsonParser jsonParser = new JsonParser();
    private static final String USER ="USER_ID";


    @Test
    public void shouldGetContractsForACustomer() {
        when(contractResource.getContracts(SALES_CHANNEL, CUSTOMER_ID)).thenReturn(newArrayList(new ContractDTO()));

        Response response = customerResourceHandler.getContractsForCustomer(SALES_CHANNEL, CUSTOMER_ID);

        assertThat(response.getStatus(), Is.is(Response.Status.OK.getStatusCode()));
        JsonArray asJsonArray = jsonParser.parse(response.getEntity().toString()).getAsJsonArray();
        assertThat(asJsonArray.size(), Is.is(1));
    }


    @Test
    public void shouldReturnEmptyArrayIfNoContractFound() {
        List<ContractDTO> contractDTOs =Lists.<ContractDTO>newArrayList();
        ContractDTO contractDTO = new ContractDTO();
        contractDTOs.add(contractDTO);

        when(contractResource.getContracts(anyString(), anyLong())).thenReturn(contractDTOs);

        Response response = customerResourceHandler.getContractsForCustomer(SALES_CHANNEL, CUSTOMER_ID);

        JsonArray asJsonArray = jsonParser.parse(response.getEntity().toString()).getAsJsonArray();
        assertThat(response.getStatus(), Is.is(Response.Status.OK.getStatusCode()));
        assertThat(asJsonArray.size(), Is.is(1));
    }


    @Test
    public void shouldGetContractsForCustomerHandleException() {
        doThrow(Exception.class).when(contractResource).getContracts(SALES_CHANNEL, CUSTOMER_ID);

        Response response = customerResourceHandler.getContractsForCustomer(SALES_CHANNEL, CUSTOMER_ID);

        assertThat(response.getStatus(), Is.is(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()));
    }

    @Test
    public void shouldReturnCustomerIdentifiersForGivenSalesChannel() {
        when(customerResourceMock.getAvailableCustomer("some sales channel")).thenReturn(newArrayList(new Identifier("customerId", "customer name")));

        Response response = customerResourceHandler.getCustomerForChannel("some sales channel","boatId");

        assertThat(response.getStatus(), Is.is(Response.Status.OK.getStatusCode()));

        JsonArray jsonArray = jsonParser.parse(response.getEntity().toString()).getAsJsonArray();
        assertThat(jsonArray.size(), Is.is(1));
        assertThat(getStringValueFromPath(jsonArray.get(0).getAsJsonObject(), "cusId"), Is.is("customerId"));
        assertThat(getStringValueFromPath(jsonArray.get(0).getAsJsonObject(), "cusName"), Is.is("customer name"));
    }

    @Test
    public void shouldReturnBadRequestIfSalesChannelIsEmpty() {
        Response response = customerResourceHandler.getCustomerForChannel(StringUtils.EMPTY,"");
        assertThat(response.getStatus(), Is.is(Response.Status.BAD_REQUEST.getStatusCode()));
    }

    @Test
    public void shouldUpdateContract() {
        ContractDTO contractDTO = new ContractDTO();
        String userID = "JUNIT";

        when(contractResource.updateContract(any(ContractDTO.class), anyString())).thenReturn(true);

        Response resp = customerResourceHandler.updateContract(contractDTO, userID);

        assert (resp.getStatus() == Response.Status.OK.getStatusCode());
    }

    @Test
    public void shouldUpdateContractHandleFailure() {
        ContractDTO contractDTO = new ContractDTO();
        String userID = "JUNIT";

        when(contractResource.updateContract(any(ContractDTO.class), anyString())).thenReturn(false);

        Response resp = customerResourceHandler.updateContract(contractDTO, userID);

        assert (resp.getStatus() == Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    public void shouldUpdateContractHandleInvalidInput() {
        ContractDTO contractDTO = new ContractDTO();
        String userID = null;

        when(contractResource.updateContract(any(ContractDTO.class), anyString())).thenReturn(false);

        Response resp = customerResourceHandler.updateContract(contractDTO, userID);

        assert (resp.getStatus() == Response.Status.BAD_REQUEST.getStatusCode());
    }


    @Test
    public void shouldCreateCustomer() throws Exception{
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setCusName("CU");

        CustomerDTO customerDTORet = new CustomerDTO();
        customerDTORet.setCusId(10001L);

        ContractDTO retContractDTO = new ContractDTO();

        when(customerResourceMock.createCustomer(anyString(),any(CustomerDTO.class))).thenReturn(customerDTORet);
        when(siteResourceClientMock.createSite(anyString(),any(SiteUpdateDTO.class))).thenReturn(100L);
        when((siteLocationResourceMock.createLocation(anyString(),any(SiteLocationDTO.class)))).thenReturn("101");
        when(contractResource.createContract(any(ContractDTO.class),anyString(),anyString())).thenReturn(retContractDTO);

        Response resp= customerResourceHandler.createCustomer(USER,customerDTO,null,null,null,"xxx");

        assert (resp.getStatus() == Response.Status.OK.getStatusCode());
    }

    @Test
    public void shouldCreateCustomerHandleInvalidInput() throws Exception{

        Response resp1= customerResourceHandler.createCustomer(null,null,null,null,null,null);
        Response resp2= customerResourceHandler.createCustomer("",null,null,null,null,null);
        assert (Response.Status.BAD_REQUEST.getStatusCode() == resp1.getStatus());
        assert (Response.Status.BAD_REQUEST.getStatusCode() == resp2.getStatus());
    }

    @Test(expected = RuntimeException.class)
    public void shouldCreateCustomerHandleException() throws Exception{
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setCusName("CU");
        doThrow(new RuntimeException()).when(customerResourceMock).createCustomer(anyString(),any(CustomerDTO.class));

        customerResourceHandler.createCustomer(USER,customerDTO,null,null,null,null);
    }

}
