package com.bt.cqm.nad;

import com.bt.rsqe.nad.client.AddressSearchResource;
import com.bt.rsqe.nad.dto.SearchAddressRequestDTO;
import com.bt.rsqe.nad.dto.SearchAddressResponseDTO;
import com.bt.rsqe.rest.ResponseBuilder;
import com.google.gson.Gson;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.apache.commons.lang.StringUtils.*;

/**
 * This class handles the search address requests.
 * 
 * @author Ranjit Roykrishna
 */

@Path("/cqm/addresses")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NADSearchAddressHandler {
    private AddressSearchResource addressSearchResource;
    private Gson jsonSerializer;

	public NADSearchAddressHandler(AddressSearchResource addressSearchResource) {
        this.addressSearchResource = addressSearchResource;
        this.jsonSerializer = new Gson();
	}


	@GET
	public Response searchAddress(@QueryParam("q") String jsonQueryParam) {
		SearchAddressRequestDTO searchAddressRequestDTO;
		try {
            searchAddressRequestDTO = jsonSerializer.fromJson(jsonQueryParam, SearchAddressRequestDTO.class);
		} catch (Exception e) {
			return ResponseBuilder.badRequest().withEntity(jsonQueryParam).build();
		}

		if (isEmpty(searchAddressRequestDTO.getCity()) || isEmpty(searchAddressRequestDTO.getCountry())) {
			return ResponseBuilder.badRequest().build();
		}
        SearchAddressResponseDTO result = addressSearchResource.searchAddress(searchAddressRequestDTO);
        return Response.ok(result).build();
	}

	@GET
	@Path("geo-code")
	public Response getGeoCode(@QueryParam("q") String jsonQueryParam) {
		SearchAddressRequestDTO searchAddressRequestDTO;
		try {
            searchAddressRequestDTO = jsonSerializer.fromJson(jsonQueryParam, SearchAddressRequestDTO.class);
		} catch (Exception e) {
			return ResponseBuilder.badRequest().withEntity(jsonQueryParam).build();
		}

		if (isEmpty(searchAddressRequestDTO.getCity()) || isEmpty(searchAddressRequestDTO.getCountry()) || isEmpty(searchAddressRequestDTO.getPostCode())) {
			return ResponseBuilder.badRequest().withEntity("Required search attribute(s) missing. Please provide and try again!").build();
		}
        SearchAddressResponseDTO searchAddressResponseDTO = addressSearchResource.matchAddress(searchAddressRequestDTO);
        return Response.ok(searchAddressResponseDTO).build();
	}

}
