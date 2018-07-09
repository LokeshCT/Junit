package com.bt.rsqe.ape.source.extractor;

import com.bt.rsqe.ape.repository.APEQrefJPARepository;
import com.bt.rsqe.customerrecord.SiteResource;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import org.w3c.dom.Element;

/**
 * Created by 605783162 on 14/08/2015.
 */
public abstract class ResponseExtractorStrategy {
    transient protected static Logger logger = LogFactory.createDefaultLogger(Logger.class);

    public abstract void extractResponse(String response, SiteResource siteResource) throws Exception;
    public String getElementValue(Element element, String xpath) {
        return element.getElementsByTagName(xpath).item(0).getTextContent();
    }

    public interface Logger {
        @Log(level = LogLevel.INFO, format = "Info : '%s'")
        void info(String response);

        @Log(level = LogLevel.ERROR, format = "Error : '%s'")
        void error(Exception e);

        @Log(level = LogLevel.INFO, format = "'%s'")
        void startedExtractingAvailabilityResponseForRequest(String requestId);

        @Log(level = LogLevel.INFO, format = "Request '%s'")
        void extractedResponseNowStoringDataFor(String requestId);

        @Log(level = LogLevel.INFO, format = "'%s'")
        void startedStoringAvailabilityResponse(String requestId);

        @Log(level = LogLevel.INFO, format = "'%s'")
        void seemsThisIsTimedOut(String spacId);

        @Log(level = LogLevel.INFO, format = "'%s'")
        void persistenceFinishedForAvailabilityResponseFor(String requestId);
    }
}
