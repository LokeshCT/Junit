package com.bt.rsqe.utils.countries;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class CountryTest {

    private Country country;

    @Before
    public void setup() {
        country = new Country("UT", "UTO", "UTOPIA", "Utopia", false, false);
    }

    @Test
    public void shouldUseIsoHashcode() {
        Assert.assertEquals(country.hashCode(), "UT".hashCode());
    }

    @Test
    public void shouldReturnIsoForToString() {
        Assert.assertEquals(country.toString(), "UT");
    }

    @Test
    public void shouldUseIsoCodeForEquality() {
        Country c1 = new Country("UT", null, null, null, false, false);
        Assert.assertEquals(c1, country);

        Country c2 = new Country("anotherIsoCode", null, null, null, false, false);
        assertThat(c2, is(not(equalTo(country))));
    }
}
