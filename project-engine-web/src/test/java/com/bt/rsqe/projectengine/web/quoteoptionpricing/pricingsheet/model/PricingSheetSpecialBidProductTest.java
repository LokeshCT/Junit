package com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model;

import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.pricing.PricingClient;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;
import static org.mockito.MockitoAnnotations.*;

public class PricingSheetSpecialBidProductTest {
    PricingSheetSpecialBidProduct specialBidProduct;
    @Mock
    private SiteDTO site;
    @Mock
    private ProductInstance productInstance;
    @Mock
    private QuoteOptionItemDTO quoteOptionIem;
    private HashMap<String, String> attributes;

    private PricingClient pricingClient;

    @Before
    public void setUp() {
        initMocks(this);
        attributes = new HashMap<String, String>();
        attributes.put("CAVEATS", "Description For Caveats");
        attributes.put("CONFIGURATION_CATEGORY", "category");
        specialBidProduct = new PricingSheetSpecialBidProduct(site, quoteOptionIem, attributes, null, productInstance, pricingClient, null);
    }

    @Test
    public void shouldGetAttributesFromModel() {
        Map<String, String> attributes1 = specialBidProduct.getAttributes();
        assertThat(attributes1.size(), is(2));
        assertThat(specialBidProduct.getAttributeValueFor("CAVEATS"), is("Description For Caveats"));
        assertThat(specialBidProduct.getAttributeValueFor("no key"), is(""));
    }
}
