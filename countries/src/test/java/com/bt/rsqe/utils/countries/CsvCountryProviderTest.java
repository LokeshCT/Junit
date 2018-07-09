package com.bt.rsqe.utils.countries;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CsvCountryProviderTest {

    private static final String LINE = "IE,IRE,IRELAND,Ireland,1,1";
    private CsvCountryProvider csvCountryProvider;
    private Country parsedCountry;

    @Before
    public void setup() {
        csvCountryProvider = new CsvCountryProvider();
        parsedCountry = csvCountryProvider.parseCsvLine(LINE);
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowErrorIfInvalidInput() {
        csvCountryProvider.parseCsvLine("blah blah blah");
    }

    @Test
    public void shouldLoadIsoCode() {
        assertEquals("IE", parsedCountry.getIsoCode());
    }

    @Test
    public void shouldLoadAlpha3IsoCode() {
        assertEquals("IRE", parsedCountry.getAlpha3IsoCode());
    }

    @Test
    public void shouldLoadExpedioName() {
        assertEquals("IRELAND", parsedCountry.getExpedioName());
    }

    @Test
    public void shouldLoadDisplayName() {
        assertEquals("Ireland", parsedCountry.getDisplayName());
    }

    @Test
    public void shouldLoadStateRequired() {
        assertTrue(parsedCountry.isStateRequired());

        Country country = csvCountryProvider.parseCsvLine("IE,IRE,IRELAND,Ireland,0,1");
        assertFalse(country.isStateRequired());
    }

    @Test
    public void shouldLoadZipRequired() {
        assertTrue(parsedCountry.isZipRequired());

        Country country = csvCountryProvider.parseCsvLine("IE,IRE,IRELAND,Ireland,1,0");
        assertFalse(country.isZipRequired());
    }

    @Test
    public void shouldLoadFromCsv() {
        Country[] countries = csvCountryProvider.loadCountries();
        assertTrue(countries.length > 0);
    }

    @Test
    public void shouldHandleEscapedCommas() {
        Country country = csvCountryProvider.parseCsvLine("IE,IRE,IRELAND,Ireland\\, Republic Of,1,1");
        assertTrue(country.isStateRequired());
        assertEquals("Ireland, Republic Of", country.getDisplayName());
    }
}
