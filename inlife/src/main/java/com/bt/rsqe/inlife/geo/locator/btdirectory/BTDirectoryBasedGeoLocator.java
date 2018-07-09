package com.bt.rsqe.inlife.geo.locator.btdirectory;

import com.bt.rsqe.inlife.config.ServiceEndPointConfig;
import com.bt.rsqe.inlife.geo.locator.GeoLocator;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.bt.rsqe.web.rest.RestRequestBuilder;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import static org.apache.commons.lang.StringUtils.*;

public class BTDirectoryBasedGeoLocator implements GeoLocator {

    private static final Logger LOG = LogFactory.createDefaultLogger(Logger.class);

    private RestRequestBuilder restRequestBuilder;
    private DocumentBuilder documentBuilder;
    private Cache<String, String> geoLocationCache = CacheBuilder.newBuilder().build();
    private CountryNameParser workAddressCountryNameParser;
    private CountryNameParser wfhAddressCountryNameParser;

    public BTDirectoryBasedGeoLocator(ServiceEndPointConfig serviceEndPointConfig) throws URISyntaxException {
        this(new RestRequestBuilder(new URI(serviceEndPointConfig.getUri())));
    }

    public BTDirectoryBasedGeoLocator(RestRequestBuilder restRequestBuilder) {
        try {
            this.restRequestBuilder = restRequestBuilder;
            this.documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            workAddressCountryNameParser = new WorkAddressCountryNameParser();
            wfhAddressCountryNameParser = new WFHAddressCountryNameParser();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public String locateCountry(final String ein) {
        try {
            return geoLocationCache.get(ein, new Callable<String>() {
                @Override
                public String call() throws Exception {
                    return getFromServer(ein);
                }
            });
        } catch (ExecutionException e) {
            LOG.couldNotFindLocation(ein, e);
            return UNKNOWN_COUNTRY;
        }
    }

    private String getFromServer(final String ein) {
        try {
            LOG.invokeBtDirectory(ein);
            String response = restRequestBuilder.build(new HashMap<String, String>() {{
                put("ein", ein);
            }}).get().getEntity(String.class);

            return parseCountry(response);
        } catch (Exception e) {
            LOG.couldNotFindLocation(ein, e);
            return UNKNOWN_COUNTRY;
        }
    }


    private String parseCountry(String response) {
        String country = null;
        try {
            Document document = documentBuilder.parse(new ByteArrayInputStream(response.getBytes()));
            country = workAddressCountryNameParser.parse(document);
            if (isEmpty(country)) {
                country = wfhAddressCountryNameParser.parse(document);
            }

            if( isEmpty(country)) {
                LOG.couldNotFindLocation(response);
            }
        } catch (Exception e) {
            LOG.errorParsingResponse(e);
        }

        return isEmpty(country) ? UNKNOWN_COUNTRY : country;
    }


    interface Logger {
        @Log(level = LogLevel.INFO, format = "Fetching location for ein %s")
        void invokeBtDirectory(String ein);

        @Log(level = LogLevel.ERROR, format = "Location not found for EIN %s")
        void couldNotFindLocation(String ein, Exception e);

        @Log(level = LogLevel.ERROR, format = "Couldn't locate country for response : %s")
        void couldNotFindLocation(String response);

        @Log(level = LogLevel.ERROR, format = "Error parsing response")
        void errorParsingResponse(Exception e);
    }
}
