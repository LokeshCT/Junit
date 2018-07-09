package com.bt.rsqe.utils.countries;

import com.google.common.collect.Maps;

import java.util.Map;

public class Countries {

    private Map<String, Country> countriesByIso;
    private Map<String, Country> countriesByExpedioName;
    private Map<String, Country> countriesByAlpha3;
    private Map<String, Country> countriesByDisplayName;

    public Countries() {
        this(new EnumCountryProvider());
    }

    public Countries(CountryProvider countryProvider) {
        countriesByIso = Maps.newHashMap();
        countriesByExpedioName = Maps.newHashMap();
        countriesByAlpha3 = Maps.newHashMap();
        countriesByDisplayName = Maps.newHashMap();

        for(Country c : countryProvider.loadCountries()) {
            countriesByIso.put(c.getIsoCode().toUpperCase(), c);
            countriesByExpedioName.put(c.getExpedioName().toUpperCase(), c);
            countriesByAlpha3.put(c.getAlpha3IsoCode().toUpperCase(), c);
            countriesByDisplayName.put(c.getDisplayName().toUpperCase(), c);
        }
    }

    public Country byIso(String isoCode) {
        if(isoCode == null) {
            return null;
        }
        return countriesByIso.get(isoCode.toUpperCase());
    }

    public Country byExpedioName(String expedioName) {
        if(expedioName == null) {
            return null;
        }
        return countriesByExpedioName.get(expedioName.toUpperCase());
    }

    public Country byAlpha3IsoCode(String isoAlpha3Code) {
        return countriesByAlpha3.get(isoAlpha3Code.toUpperCase());
    }

    public Country byDisplayName(String displayName) {
        return countriesByDisplayName.get(displayName.toUpperCase());
    }

    private static final Countries instance = new Countries();

    /*
     * @deprecated Static access here to support older code which cannot be easily modified to use dependency injection.
     * Dont use this - inject an instance of Countries and use the instance methods instead.
     */
    @Deprecated
    public static Country byIsoStatic(String isoCode) {
        return instance.byIso(isoCode);
    }

    /*
     * @deprecated Static access here to support older code which cannot be easily modified to use dependency injection.
     * Dont use this - inject an instance of Countries and use the instance methods instead.
     */
    @Deprecated
    public static Country byExpedioNameStatic(String isoCode) {
        return instance.byExpedioName(isoCode);
    }
}
