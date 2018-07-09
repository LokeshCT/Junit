package com.bt.rsqe.projectengine.web.quoteoption.validation;

import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.projectengine.web.quoteoption.validation.SiteValidator;
import com.bt.rsqe.utils.countries.Countries;
import com.bt.rsqe.utils.countries.Country;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SiteValidatorTest {
    private static final Country noRequirementCountry = new Country("IE", "IRE", "IRELAND", "Ireland", false, false);
    private static final Country stateRequirementCountry = new Country("IE", "IRE", "IRELAND", "Ireland", true, false);
    private static final Country zipRequirementCountry = new Country("IE", "IRE", "IRELAND", "Ireland", false, true);

    private SiteDTO site;
    private SiteValidator siteValidator;
    private Countries countries;
    private Country country = new Country("IE", "IRL", "Ireland", "Ireland", false, false);

    @Before
    public void setup() {
        countries = mock(Countries.class);
        when(countries.byIso("IE")).thenReturn(noRequirementCountry);
        when(countries.byExpedioName("Ireland")).thenReturn(country);
        site = new SiteDTO();
        site.name = "Site Name";
        site.city = "City";
        site.country = "Ireland";
        site.countryISOCode = "IE";
        site.latitude = "1234";
        site.longitude = "1234";
        site.buildingNumber = "123";
        site.streetName = "Fake Street";


        siteValidator = new SiteValidator(countries);
    }

    @Test
    public void validShouldHaveNoMessages() {
        List<String> validationMessages = siteValidator.validateSite(site);
        assertThat(validationMessages.size(), is(0));
    }

    @Test
    public void shouldValidateSiteName() {
        assertValidatesFieldNotEmpty("name", "Site name is required");
    }

    @Test
    public void shouldValidateCity() {
        assertValidatesFieldNotEmpty("city", "City is required");
    }

    @Test
    public void shouldValidateCountry() {
        assertValidatesFieldNotEmpty("country", "Country is required");
    }

    @Test
    public void shouldValidateLatitude() {
        assertValidatesFieldNotEmpty("latitude", "Latitude is required");
    }

    @Test
    public void shouldValidateLongitude() {
        assertValidatesFieldNotEmpty("longitude", "Longitude is required");
    }

    @Test
    public void shouldValidateBuildingNameOrNumber() {
        site.building = "";
        site.buildingNumber = "";
        assertExpectedValidationMessageIsReturned("Building Name or Building Number is required");
    }

    @Test
    public void shouldValidateStreetOrLocality() {
        site.locality = "";
        site.streetName = "";
        assertExpectedValidationMessageIsReturned("Locality or Street is required");
    }

    @Test
    public void shouldValidateZipCodeIfRequired() {
        when(countries.byIso("IE")).thenReturn(zipRequirementCountry);
        site.postCode = "";
        assertExpectedValidationMessageIsReturned("Zip is required for country Ireland");
    }

    @Test
    public void shouldValidateStateIfRequired() {
        when(countries.byIso("IE")).thenReturn(stateRequirementCountry);
        site.stateCountySProvince = "";
        assertExpectedValidationMessageIsReturned("State/County/Province is required for country Ireland");
    }

    private void assertValidatesFieldNotEmpty(String fieldName, String expectedMessage) {
        try {
            Field f = SiteDTO.class.getField(fieldName);
            f.set(site, null);
            assertExpectedValidationMessageIsReturned(expectedMessage);

            f.set(site, "");
            assertExpectedValidationMessageIsReturned(expectedMessage);
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void assertExpectedValidationMessageIsReturned(String expectedMessage) {
        List<String> validationMessages = siteValidator.validateSite(site);
        assertThat(validationMessages, hasItem(expectedMessage));
    }



}
