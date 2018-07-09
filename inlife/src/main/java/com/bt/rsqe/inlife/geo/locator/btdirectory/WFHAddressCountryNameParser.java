package com.bt.rsqe.inlife.geo.locator.btdirectory;

import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;

import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

//Work From Home Address
public class WFHAddressCountryNameParser implements CountryNameParser {
    private static final Logger LOG = LogFactory.createDefaultLogger(Logger.class);
    private static final String WFH_ADDRESS_COUNTRY_XPATH = "/people/person/address/btBuildingCode";

    private XPathExpression xPath;


    public WFHAddressCountryNameParser() {
        try {
            this.xPath = XPathFactory.newInstance().newXPath().compile(WFH_ADDRESS_COUNTRY_XPATH);
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public String parse(Document document) {
        try {
            String btBuildingCode = xPath.evaluate(document);
            return btBuildingCode.substring( btBuildingCode.lastIndexOf("-") + 1);
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
