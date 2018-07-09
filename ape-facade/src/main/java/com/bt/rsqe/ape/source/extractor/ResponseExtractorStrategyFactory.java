package com.bt.rsqe.ape.source.extractor;

import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.bt.rsqe.utils.AssertObject;

/**
 * Created by 605783162 on 14/08/2015.
 */
public class ResponseExtractorStrategyFactory {

    public static final String SUPPLIER_PRODUCT_LIST_RESPONSE = "SupplierProductListResponse";
    public static final String AVAILABILTY_RESPONSE = "AvailabiltyResponse";
    private static final String TELE_PARAM = "Telephone";

    transient private static Logger logger = LogFactory.createDefaultLogger(Logger.class);

    public ResponseExtractorStrategy getExtractorStrategy(String operation, String checkParam) {
        ResponseExtractorStrategy responseExtractorStrategy = null;
        try {
            if (AssertObject.isEmpty(checkParam)) {
                if (SUPPLIER_PRODUCT_LIST_RESPONSE.equalsIgnoreCase(operation)) {
                    responseExtractorStrategy = new SupplierProductResponseExtractor();
                } else if (AVAILABILTY_RESPONSE.equalsIgnoreCase(operation)) {
                    responseExtractorStrategy = new SupplierAvailabilityResponseExtractor();
                } else {
                    logger.unknownSoapAction(operation);
                    throw new RuntimeException(String.format("Unknown SOAP action for APE call:%s", operation));
                }
            } else if (TELE_PARAM.equals(checkParam)) {
                if (SUPPLIER_PRODUCT_LIST_RESPONSE.equalsIgnoreCase(operation)) {
                    responseExtractorStrategy = new SacSupplierProductResponseExtractor();
                } else if (AVAILABILTY_RESPONSE.equalsIgnoreCase(operation)) {
                    responseExtractorStrategy = new SacSupplierAvailabilityResponseExtractor();
                } else {
                    logger.unknownSoapAction(operation);
                    throw new RuntimeException(String.format("Unknown SOAP action for APE call:%s", operation));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return responseExtractorStrategy;
    }

    public interface Logger {
        @Log(level = LogLevel.ERROR, format = "Unhandled SOAPAction: %s")
        void unknownSoapAction(String soapAction);
    }
}
