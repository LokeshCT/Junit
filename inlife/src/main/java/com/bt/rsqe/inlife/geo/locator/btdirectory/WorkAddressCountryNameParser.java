package com.bt.rsqe.inlife.geo.locator.btdirectory;

import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;

import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class WorkAddressCountryNameParser implements CountryNameParser {
    private static final Logger LOG = LogFactory.createDefaultLogger(Logger.class);
    private static final String WORK_ADDRESS_COUNTRY_XPATH = "/people/person/address/country";

    private final XPathExpression xPath;

    public WorkAddressCountryNameParser() {
        try {
            this.xPath = XPathFactory.newInstance().newXPath().compile(WORK_ADDRESS_COUNTRY_XPATH);
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public String parse(Document document) {
        try {
            return xPath.evaluate(document);
        } catch (XPathExpressionException e) {
            LOG.errorEvaluatingXpath(e);
            return StringUtils.EMPTY;
        }
    }


    interface Logger {
        @Log(level = LogLevel.ERROR, format = "Error evaluating xpath for response %s")
        void errorEvaluatingXpath(Exception e);
    }
}
