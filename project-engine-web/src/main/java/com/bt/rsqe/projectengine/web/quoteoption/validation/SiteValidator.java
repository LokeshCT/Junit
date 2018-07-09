package com.bt.rsqe.projectengine.web.quoteoption.validation;

import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.utils.countries.Countries;
import com.bt.rsqe.utils.countries.Country;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import java.lang.reflect.Field;
import java.util.List;

import static com.bt.rsqe.utils.AssertObject.isNull;

public class SiteValidator {

    private Countries countries;

    public SiteValidator(Countries countries) {
        this.countries = countries;
    }

    public List<String> validateSite(SiteDTO site) {
        List<String> messages = Lists.newArrayList();

        validateUniversallyRequiredFields(site, messages);
        validateZipCode(site, messages);
        validateState(site, messages);

        return messages;
    }

    public List<String> validate(List<SiteDTO> sites) {
        List<String> messagesBySite = Lists.newArrayList();

        for(SiteDTO site : sites) {
            List<String> messages = validateSite(site);
            if(!messages.isEmpty()) {
                String errorMessage = "Invalid address for site '" + site.name  + "': " + StringUtils.join(messages, ", ");
                messagesBySite.add(errorMessage);
            }
        }

        return messagesBySite;
    }

    private void validateState(SiteDTO site, List<String> messages) {
        Country country = countries.byIso(site.countryISOCode);

        if(stateIsEmptyAndRequired(site, country)) {
            messages.add("State/County/Province is required for country " + country.getDisplayName());
        }
    }

    private boolean stateIsEmptyAndRequired(SiteDTO site, Country country) {
        return !fieldIsEmpty(site, "countryISOCode") && country.isStateRequired()
               && fieldIsEmpty(site, "stateCountySProvince");
    }

    private void validateZipCode(SiteDTO site, List<String> messages) {
        Country country = countries.byIso(site.countryISOCode);
        if(zipIsEmptyAndRequired(site, country)) {
            messages.add("Zip is required for country " + country.getDisplayName());
        }
    }

    private boolean zipIsEmptyAndRequired(SiteDTO site, Country country) {
        return !fieldIsEmpty(site, "countryISOCode") && country.isZipRequired()
               && fieldIsEmpty(site, "postCode");
    }

    private void validateUniversallyRequiredFields(SiteDTO site, List<String> messages) {
        addValidationMessageIfFieldIsEmpty(site, messages, "name", "Site name is required");
        addValidationMessageIfFieldIsEmpty(site, messages, "city", "City is required");
        addValidationMessageIfFieldIsEmpty(site, messages, "country", "Country is required");
        addValidationMessageIfFieldIsEmpty(site, messages, "longitude", "Longitude is required");
        addValidationMessageIfFieldIsEmpty(site, messages, "latitude", "Latitude is required");

        if( isNull(countries.byExpedioName(site.country)) ) {
            messages.add("Country Iso Code is required");
        }

        if(fieldIsEmpty(site, "building") && fieldIsEmpty(site, "buildingNumber")) {
            messages.add("Building Name or Building Number is required");
        }

        if(fieldIsEmpty(site, "locality") && fieldIsEmpty(site, "streetName")) {
            messages.add("Locality or Street is required");
        }
    }

    private void addValidationMessageIfFieldIsEmpty(SiteDTO site, List<String> messages, String fieldName, String message) {
        if(fieldIsEmpty(site, fieldName)) {
            messages.add(message);
        }
    }

    private boolean fieldIsEmpty(SiteDTO site, String fieldName) {
        String value = getFieldValue(site, fieldName);
        return value == null || value.isEmpty();
    }

    private String getFieldValue(SiteDTO site, String fieldName) {
        try {
            Field f = SiteDTO.class.getField(fieldName);
            String value = (String) f.get(site);
            return value;
        }
        catch(Exception e) {
            throw new RuntimeException("Unable to validate site", e);
        }
    }
}
