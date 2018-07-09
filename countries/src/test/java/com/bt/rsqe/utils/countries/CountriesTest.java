package com.bt.rsqe.utils.countries;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CountriesTest {

    private Countries countries;

    private Country ireland = new Country("IE", "IRE", "IRELAND", "Ireland", false, false);
    private Country uk = new Country("GB", "GBR", "UNITED KINGDOM", "United Kingdom", false, true);

    @Before
    public void setup() {
        CountryProvider countryProvider = mock(CountryProvider.class);
        when(countryProvider.loadCountries()).thenReturn(new Country[] {ireland, uk});
        countries = new Countries(countryProvider);
    }

    @Test
    public void shouldReturnCountryEvenWhenNameContainsSpecialCharacters() throws Exception {
        countries = new Countries();

        Country country = countries.byExpedioName("KOREA, REPUBLIC OF");
        assertThat(country.getDisplayName(), is("Korea, Republic of"));

        country = countries.byDisplayName("Virgin Islands, British");
        assertThat(country.getExpedioName(), is("VIRGIN ISLANDS, BRITISH"));
    }

    @Test
    public void shouldReturnCountryByIsoCode() {
        assertEquals(ireland, countries.byIso("IE"));
        assertEquals(uk, countries.byIso("GB"));
    }

    @Test
    public void shouldGetByIsoCodeCaseInsensitive() {
        assertEquals(ireland, countries.byIso("ie"));
    }

    @Test
    public void shouldReturnNullForNullIso() {
        assertNull(countries.byIso(null));
    }

    @Test
    public void shouldReturnCountryByAlpha3() {
        assertEquals(ireland, countries.byAlpha3IsoCode("IRE"));
        assertEquals(uk, countries.byAlpha3IsoCode("GBR"));
    }

    @Test
    public void shouldGetByAlpha3CaseInsensitive() {
        assertEquals(ireland, countries.byAlpha3IsoCode("ire"));
    }

    @Test
    public void shouldReturnCountryByExpedioName() {
        assertEquals(ireland, countries.byExpedioName("IRELAND"));
        assertEquals(uk, countries.byExpedioName("UNITED KINGDOM"));
    }

    @Test
    public void shouldGetByExpedioNameCaseInsensitive() {
        assertEquals(ireland, countries.byExpedioName("Ireland"));
    }

    @Test
    public void shouldReturnNullForNullExpedioName() {
        assertNull(countries.byExpedioName(null));
    }

    @Test
    public void shouldReturnCountryByDisplayName() {
        assertEquals(ireland, countries.byDisplayName("Ireland"));
        assertEquals(uk, countries.byDisplayName("United Kingdom"));
    }

    @Test
    public void shouldGetByDisplayNameCaseInsensitive() {
        assertEquals(ireland, countries.byDisplayName("ireland"));
    }

    @Test
    public void shouldGetByIsoStatic() {
        assertEquals("IE", Countries.byIsoStatic("IE").getIsoCode());
    }

    @Test
    public void shouldGetByExpedioStatic() {
        assertEquals("IE", Countries.byExpedioNameStatic("IRELAND").getIsoCode());
    }
}
