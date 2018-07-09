package com.bt.rsqe.utils.countries;

import com.google.common.collect.Lists;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @deprecated
 * this class does not use a CSV Parser to read in the data and therefore
 * we cannot escape special characters such as ','.
 * Please use EnumCountryProvider going forward.
 * @see EnumCountryProvider
 */
public class CsvCountryProvider implements CountryProvider {

    private static final String COUNTRIES_CSV_NAME = "countries.csv";
    private static final Pattern PARSE_PATTERN = Pattern.compile("([A-Z]{2}),([A-Z]{3}),(.*?),(.*?),([01]),([01])");

    @Override
    public Country[] loadCountries() {
        try {
            InputStream in = getClass().getResourceAsStream(COUNTRIES_CSV_NAME);

            ///CLOVER:OFF
            if(in == null) {
                throw new NullPointerException("File not found: " + COUNTRIES_CSV_NAME);
            }
            //CLOVER:ON

            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            List<Country> countries = Lists.newArrayList();
            String line;

            while ((line = reader.readLine()) != null) {
                if(line.startsWith("#")) {
                    continue;
                }
                Country c = parseCsvLine(line);
                countries.add(c);
            }

            Country[] countriesArr = new Country[countries.size()];
            return countries.toArray(countriesArr);
        }
        catch(Exception e) {
            ///CLOVER:OFF
            throw new RuntimeException("Unable to load countries configuration", e);
            //CLOVER:ON
        }
    }

    public Country parseCsvLine(String line) {
        Matcher matcher = PARSE_PATTERN.matcher(line);

        if(!matcher.matches()) {
            throw new RuntimeException("Invalid line in country configuration: " + line);
        }

        String isoCode = matcher.group(1);
        String alpha3IsoCode = matcher.group(2);
        String expedioName = matcher.group(3);
        String displayName = matcher.group(4).replace("\\,", ",");
        boolean stateRequired = "1".equals(matcher.group(5));
        boolean zipRequired = "1".equals(matcher.group(6));


        return new Country(isoCode, alpha3IsoCode, expedioName, displayName, stateRequired, zipRequired);
    }
}
