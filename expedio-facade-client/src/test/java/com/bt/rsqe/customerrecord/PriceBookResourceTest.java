package com.bt.rsqe.customerrecord;

import com.bt.rsqe.domain.PriceBookDTO;
import com.bt.rsqe.web.rest.RestRequestBuilderFixture;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.GenericEntity;
import java.util.ArrayList;
import java.util.List;

import static com.bt.rsqe.web.rest.RestRequestBuilderFixture.*;
import static java.util.Arrays.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class PriceBookResourceTest {

    private RestRequestBuilderFixture restRequestBuilderFixture;

    @Before
    public void setUp() throws Exception {
        restRequestBuilderFixture = aRequest();

    }

    @Test
    public void testDefaultPriceBook() throws Exception {
        final PriceBookDTO priceBookDTO = new PriceBookDTO("id", "someRequestId", "blah-eup", "blah-ptp", null, null);
        restRequestBuilderFixture.forPath("productCategory", "blah-scode").withEntity(new GenericEntity<List<PriceBookDTO>>(asList(priceBookDTO)) {
                });
        PriceBookResource priceBookResource = new PriceBookResource(restRequestBuilderFixture.build());
        assertThat(priceBookResource.defaultPriceBook("blah-scode"),is(priceBookDTO));
    }

    @Test
    public void testDefaultPriceBookNull() throws Exception {
        restRequestBuilderFixture.withEntity(new GenericEntity<List<PriceBookDTO>>(new ArrayList<PriceBookDTO>()) {
        });

        PriceBookResource priceBookResource = new PriceBookResource(restRequestBuilderFixture.build());
        assertThat(priceBookResource.defaultPriceBook("blah-scode"),is(nullValue()));
    }
}
