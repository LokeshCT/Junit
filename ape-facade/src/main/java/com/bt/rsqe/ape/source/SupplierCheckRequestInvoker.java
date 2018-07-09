package com.bt.rsqe.ape.source;

import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.charset.Charset;

import static com.bt.rsqe.ape.constants.SupplierProductConstants.SOAP_ACTION;

/**
 * Created by 605783162 on 09/08/2015.
 */
public class SupplierCheckRequestInvoker {

    protected static Logger logger = LogFactory.createDefaultLogger(Logger.class);

    public static String sendMessage(String endpointUri, String soapAction, String xmlRequest) {
        SOAPMessage soapResponse = null;
        String xmlResponse = null;
        try {
            // Create SOAP Connection
            SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
            SOAPConnection soapConnection = soapConnectionFactory.createConnection();

            MessageFactory factory = MessageFactory.newInstance();
            logger.info("Request message : " + xmlRequest);
            SOAPMessage requestMessage = factory.createMessage(new MimeHeaders(), new ByteArrayInputStream(xmlRequest.getBytes(Charset.forName("UTF-8"))));

            // Setting SOAPAction header line
            MimeHeaders headers = requestMessage.getMimeHeaders();
            headers.addHeader(SOAP_ACTION, soapAction);

            // Send SOAP Message to SOAP Server
            URL url = new URL(null,endpointUri,new URLStreamHandler() {
                @Override
                protected URLConnection openConnection(URL url) throws IOException {
                    URLConnection connection = new URL(url.toString()).openConnection();
                    // Connection settings
                    connection.setConnectTimeout(10000); // 10 sec
                    connection.setReadTimeout(60000); // 1 min
                    return(connection);
                }
            });

            soapResponse = soapConnection.call(requestMessage, url);
            xmlResponse = printSOAPResponse(soapResponse);
            soapConnection.close();

        }  catch (Exception ex) {
            logger.error(ex);
            throw new RuntimeException("Error in handling SOAP message", ex);
        }
        return xmlResponse;
    }

    private static String printSOAPResponse(SOAPMessage soapResponse) throws Exception {
        TransformerFactory tff = TransformerFactory.newInstance();
        Transformer tf = tff.newTransformer();

        // Set formatting
        tf.setOutputProperty(OutputKeys.INDENT, "yes");
        tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        Source sc = soapResponse.getSOAPPart().getContent();
        ByteArrayOutputStream streamOut = new ByteArrayOutputStream();
        StreamResult result = new StreamResult(streamOut);
        tf.transform(sc, result);

        String strMessage = streamOut.toString();
        logger.info("Request Message : " + strMessage);
        return  strMessage;
    }

    public interface Logger {
        @Log(level = LogLevel.INFO, format = "Info: '%s'")
        void info(String message);

        @Log(level = LogLevel.ERROR, format = "Error: '%s'")
        void error(Exception message);
    }
}
