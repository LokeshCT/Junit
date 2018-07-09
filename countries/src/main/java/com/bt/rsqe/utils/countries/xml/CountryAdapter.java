package com.bt.rsqe.utils.countries.xml;

import com.bt.rsqe.utils.countries.Countries;
import com.bt.rsqe.utils.countries.Country;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class CountryAdapter extends XmlAdapter<AdaptedCountry, Country> {
    private final Countries countries;

    /*
     * For jaxb, need to figure out how to combine DI with jaxb....
     */
    @Deprecated
    public CountryAdapter() {
        this(new Countries());
    }

    public CountryAdapter(Countries countries) {
        this.countries = countries;
    }

    @Override
    public Country unmarshal(AdaptedCountry v) throws Exception {
        return countries.byIso(v.isoCode);
    }

    @Override
    public AdaptedCountry marshal(Country country) throws Exception {
        AdaptedCountry adaptedCountry = new AdaptedCountry();
        adaptedCountry.isoCode = country.getIsoCode();
        return adaptedCountry;
    }
}
