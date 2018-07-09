package com.bt.cqm.handler;

import com.bt.rsqe.nad.client.AddressSearchResource;
import com.bt.rsqe.nad.dto.NadAddressDTO;
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
    private static final String BLACK_COLOR="Black";
    private static final String RED_COLOR="Red";
    private static final String AMBER_COLOR="Amber";
    private static final String GREEN_COLOR="Green";
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
        result = getColorCode(result);
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

    private SearchAddressResponseDTO getColorCode(SearchAddressResponseDTO searchAddressResponseDTO) {
        //Return the Color in the the below order.
        //black-red-amber-green
        String componentStatusOnValidation;
        String failLevel;
        NadAddressDTO nadAddressDTO = null;
        String colorCode = BLACK_COLOR;
        List<NadAddressDTO> addressDTOListUpdated = null;
        try {
            List<NadAddressDTO> addressDTOList = searchAddressResponseDTO.getAddressDTOList();
            addressDTOListUpdated = new ArrayList<NadAddressDTO>();
            if (null != addressDTOList && addressDTOList.size() > 0) {
                Iterator it = addressDTOList.listIterator();
                while (it.hasNext()) {
                    nadAddressDTO = (NadAddressDTO) it.next();
                    componentStatusOnValidation=nadAddressDTO.getColorCode();
                    failLevel=nadAddressDTO.getFailLevel();
                    colorCode = BLACK_COLOR;

                    if (null != componentStatusOnValidation && componentStatusOnValidation.trim().length() > 1) {
                        String addressFields[] = componentStatusOnValidation.split("-");
                        if (null != addressFields && addressFields.length > 0) {
                            for (int i = 0; i < addressFields.length; i++) {
                                char mpcCode = addressFields[i].charAt(0);
                                String matchColor = null;
                                if (mpcCode != 'T' && mpcCode != 'S' && mpcCode != 'Z') {
                                    matchColor = (String) CQMBasePageResourceHandler.colorCodeHashMap.get(addressFields[i] + "-" + 100);
                                } else {
                                    matchColor = (String) CQMBasePageResourceHandler.colorCodeHashMap.get(addressFields[i] + "-" + failLevel);
                                }
                                if (null != matchColor) {
                                    if (BLACK_COLOR.equals(colorCode)) {
                                        colorCode = matchColor;
                                    } else {
                                        if (matchColor.equals(RED_COLOR)) {
                                            colorCode = RED_COLOR;
                                        } else if (matchColor.equals(AMBER_COLOR) && colorCode.equals(GREEN_COLOR)) {
                                            colorCode = AMBER_COLOR;
                                        }
                                    }
                                }
                            }
                        }

                    }
                    nadAddressDTO.setColorCode(colorCode);
                    addressDTOListUpdated.add(nadAddressDTO);
                }
            }
        } catch (Exception e) {
        }
        searchAddressResponseDTO.setAddressDTOList(addressDTOListUpdated);
        return searchAddressResponseDTO;
    }

}
