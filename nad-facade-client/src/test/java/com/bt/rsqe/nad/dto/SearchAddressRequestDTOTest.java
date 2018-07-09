package com.bt.rsqe.nad.dto;

import com.bt.rsqe.utils.JSONSerializer;
import org.hamcrest.core.Is;
import org.junit.Test;

import static org.junit.Assert.assertThat;

public class SearchAddressRequestDTOTest {
    private JSONSerializer jsonSerializer = JSONSerializer.getInstance();

    @Test
    public void shouldBeAbleToConvertDTOToJson() {
        SearchAddressRequestDTO expected = SearchAddressRequestDTO.Builder.get().withCity("city").withCountry("country").withPostCode("postcode").build();
        SearchAddressRequestDTO actual = jsonSerializer.deSerialize(jsonSerializer.serialize(expected), SearchAddressRequestDTO.class);
        assertThat(actual, Is.is(expected));
    }
}
