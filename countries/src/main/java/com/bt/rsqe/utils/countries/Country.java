package com.bt.rsqe.utils.countries;

import com.bt.rsqe.utils.countries.xml.CountryAdapter;
import org.apache.commons.lang.builder.EqualsBuilder;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlJavaTypeAdapter(CountryAdapter.class)
public class Country {
    private final String isoCode;
    private final String alpha3IsoCode;
    private final String expedioName;
    private final String displayName;
    private final boolean stateRequired;
    private final boolean zipRequired;

    public Country(String isoCode, String alpha3IsoCode, String expedioName, String displayName,
                   boolean stateRequired, boolean zipRequired) {
        this.isoCode = isoCode;
        this.alpha3IsoCode = alpha3IsoCode;
        this.expedioName = expedioName;
        this.displayName = displayName;
        this.stateRequired = stateRequired;
        this.zipRequired = zipRequired;
    }

    public String getIsoCode() {
        return isoCode;
    }

    public String getAlpha3IsoCode() {
        return alpha3IsoCode;
    }

    public String getExpedioName() {
        return expedioName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isStateRequired() {
        return stateRequired;
    }

    public boolean isZipRequired() {
        return zipRequired;
    }

    public boolean equals(Object other) {
        return (other instanceof Country && isoCode.equals(((Country) other).getIsoCode()));
    }

    public int hashCode() {
        return isoCode.hashCode();
    }

    public String toString() {
        return isoCode;
    }
}
