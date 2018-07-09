package com.bt.cqm.nad;

import com.bt.cqm.handler.NADSearchAddressHandler;
import com.bt.rsqe.nad.client.AddressSearchResource;
import com.bt.rsqe.nad.dto.NadAddressDTO;
import com.bt.rsqe.nad.dto.SearchAddressRequestDTO;
import com.bt.rsqe.nad.dto.SearchAddressResponseDTO;
import org.hamcrest.core.Is;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static com.google.common.collect.Lists.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class NADSearchAddressHandlerTest {

    private AddressSearchResource addressSearchResource = mock(AddressSearchResource.class);
    private NADSearchAddressHandler nadSearchAddressHandler = new NADSearchAddressHandler(addressSearchResource);

    @Test
    public void shouldReturnBadRequestForMalformedJson() {
        Response response = nadSearchAddressHandler.searchAddress("bla bla");
        assertThat(response.getStatus(), Is.is(Response.Status.BAD_REQUEST.getStatusCode()));

        response = nadSearchAddressHandler.getGeoCode("bla bla");
        assertThat(response.getStatus(), Is.is(Response.Status.BAD_REQUEST.getStatusCode()));
    }

    @Test
    public void shouldReturnBadRequestIfCityOrCountryMissingForSearchAddress() {
        Response response = nadSearchAddressHandler.searchAddress("{city:'city'}");
        assertThat(response.getStatus(), Is.is(Response.Status.BAD_REQUEST.getStatusCode()));

        response = nadSearchAddressHandler.searchAddress("{country:'country'}");
        assertThat(response.getStatus(), Is.is(Response.Status.BAD_REQUEST.getStatusCode()));
    }

    @Test
    public void shouldReturnBadRequestIfCityOrCountryOrPostcodeMissingToGetGeoCode() {
        Response response = nadSearchAddressHandler.getGeoCode("{city:'city', country:'country'}");
        assertThat(response.getStatus(), Is.is(Response.Status.BAD_REQUEST.getStatusCode()));

        nadSearchAddressHandler.getGeoCode("{city:'city', postCode:'postCode'}");
        assertThat(response.getStatus(), Is.is(Response.Status.BAD_REQUEST.getStatusCode()));

        nadSearchAddressHandler.getGeoCode("{country:'country', postCode:'postCode'}");
        assertThat(response.getStatus(), Is.is(Response.Status.BAD_REQUEST.getStatusCode()));
    }

    @Test
    public void shouldSearchAddressForValidInput() {
        SearchAddressRequestDTO request = SearchAddressRequestDTO.Builder.get().withPostCode("postCode").withCity("city").withCountry("country").build();
        NadAddressDTO nadAddress = NadAddressDTO.Builder.get().withCity("city").withCountry("country").withPoBox("po box").build();
        when(addressSearchResource.matchAddress(request)).thenReturn(new SearchAddressResponseDTO(null, null, null, null, newArrayList(nadAddress)));

        Response response = nadSearchAddressHandler.getGeoCode("{city:'city', country:'country', postCode:'postCode'}");

        assertThat(response.getStatus(), Is.is(Response.Status.OK.getStatusCode()));
        SearchAddressResponseDTO responseDTO = (SearchAddressResponseDTO) response.getEntity();
        assertThat(responseDTO.getAddressDTOList().size(), Is.is(1));
        assertThat(responseDTO.getAddressDTOList().get(0).getCity(), Is.is("city"));
        assertThat(responseDTO.getAddressDTOList().get(0).getCountry(), Is.is("country"));
        assertThat(responseDTO.getAddressDTOList().get(0).getPoBox(), Is.is("po box"));
    }

    @Test
    public void shouldGetGeoCodeForValidInput() {
        SearchAddressRequestDTO request = SearchAddressRequestDTO.Builder.get().withPostCode("postCode").withCity("city").withCountry("country").build();
        NadAddressDTO nadAddress = NadAddressDTO.Builder.get().withCity("city").withCountry("country").withPoBox("po box").build();
        when(addressSearchResource.matchAddress(request)).thenReturn(new SearchAddressResponseDTO(null, null, null, null, newArrayList(nadAddress)));

        Response response = nadSearchAddressHandler.getGeoCode("{city:'city', country:'country', postCode:'postCode'}");

        assertThat(response.getStatus(), Is.is(Response.Status.OK.getStatusCode()));
        SearchAddressResponseDTO responseDTO = (SearchAddressResponseDTO) response.getEntity();
        assertThat(responseDTO.getAddressDTOList().size(), Is.is(1));
        assertThat(responseDTO.getAddressDTOList().get(0).getCity(), Is.is("city"));
        assertThat(responseDTO.getAddressDTOList().get(0).getCountry(), Is.is("country"));
        assertThat(responseDTO.getAddressDTOList().get(0).getPoBox(), Is.is("po box"));
    }
}
