package com.bt.rsqe.inlife.geo.locator;

public interface GeoLocator {
    public static final String UNKNOWN_COUNTRY = "UNKNOWN";
    String locateCountry(String ein);
}
