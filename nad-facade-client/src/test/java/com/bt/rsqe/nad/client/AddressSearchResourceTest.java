package com.bt.rsqe.nad.client;

import com.bt.rsqe.ContainerUtils;
import com.bt.rsqe.container.Application;
import com.bt.rsqe.container.ApplicationConfig;
import com.bt.rsqe.container.StubApplicationConfig;
import com.bt.rsqe.nad.dto.NadAddressDTO;
import com.bt.rsqe.nad.dto.SearchAddressRequestDTO;
import com.bt.rsqe.nad.dto.SearchAddressResponseDTO;
import com.bt.rsqe.utils.UriBuilder;
import org.hamcrest.core.Is;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

import static com.google.common.collect.Lists.*;
import static org.junit.Assert.*;

public class AddressSearchResourceTest {

    private static Application application;
    private static AddressSearchResource addressSearchResource;

    @BeforeClass
    public static void beforeClass() throws IOException {
        ApplicationConfig applicationConfig = StubApplicationConfig.defaultTestConfig();
        application = ContainerUtils.startContainer(applicationConfig, new Handler());
        addressSearchResource = new AddressSearchResource(UriBuilder.buildUri(applicationConfig), null);
    }

    @Test
    public void shouldInvokeServerToSearchAddress() {
        SearchAddressResponseDTO addressResponseDTO = addressSearchResource.searchAddress(SearchAddressRequestDTO.Builder.get().withCountry("INDIA").build());

        assertThat(addressResponseDTO.getStateCode(), Is.is("Ok"));
        assertThat(addressResponseDTO.getAddressDTOList().size(), Is.is(1));
        assertThat(addressResponseDTO.getAddressDTOList().get(0).getAccuracyLevel(), Is.is("1"));
    }

    @Test
    public void shouldInvokeServerToMatchAddress() {
        SearchAddressResponseDTO addressResponseDTO = addressSearchResource.matchAddress(SearchAddressRequestDTO.Builder.get().withCountry("INDIA").build());

        assertThat(addressResponseDTO.getStateCode(), Is.is("Ok"));
        assertThat(addressResponseDTO.getAddressDTOList().size(), Is.is(1));
        assertThat(addressResponseDTO.getAddressDTOList().get(0).getBuildingNumber(), Is.is("1A"));
    }

    @AfterClass
    public static void afterClass() throws IOException {
        ContainerUtils.stop(application);
    }


    @Path("/rsqe/nad")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public static class Handler {

        @PUT
        @Path("search-address")
        public Response searchAddress(SearchAddressRequestDTO searchAddrRequest) {
            assertThat(searchAddrRequest.getCountry(), Is.is("INDIA"));
            NadAddressDTO nadAddressDTO = NadAddressDTO.Builder.get().withAccuracyLevel("1").build();
            return Response.ok(new SearchAddressResponseDTO("Ok", null, null, null, newArrayList(nadAddressDTO))).build();
        }

        @PUT
        @Path("match-address")
        public Response matchAddress(SearchAddressRequestDTO searchAddrRequest) {
            assertThat(searchAddrRequest.getCountry(), Is.is("INDIA"));
            NadAddressDTO nadAddressDTO = NadAddressDTO.Builder.get().withBuildingNumber("1A").build();
            return Response.ok(new SearchAddressResponseDTO("Ok", null, null, null, newArrayList(nadAddressDTO))).build();
        }
    }
}
