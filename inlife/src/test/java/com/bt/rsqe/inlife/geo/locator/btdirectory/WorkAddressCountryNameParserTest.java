package com.bt.rsqe.inlife.geo.locator.btdirectory;

import com.bt.rsqe.domain.ClassPathResource;
import com.bt.rsqe.utils.RsqeCharset;
import org.hamcrest.core.Is;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.apache.commons.lang.StringUtils.*;
import static org.junit.Assert.*;

public class WorkAddressCountryNameParserTest {


    @Test
    public void shouldParseCountryForValidResponse() throws IOException, SAXException, ParserConfigurationException {
        String country = new WorkAddressCountryNameParser().parse(validResponse());
        assertThat(country, Is.is("India"));
    }

    @Test
    public void shouldReturnEmptyStringForWorkFromHomeAddressResponse() throws IOException, SAXException, ParserConfigurationException {
        String country = new WorkAddressCountryNameParser().parse(validWFHResponse());
        assertTrue(isEmpty(country));
    }

    @Test
    public void shouldReturnEmptyStringForEinNotFoundResponse() throws IOException, SAXException, ParserConfigurationException {
        String country = new WorkAddressCountryNameParser().parse(invalidEinResponse());
        assertTrue(isEmpty(country));
    }

    @Test
    public void shouldReturnEmptyStringForInValidResponse() throws IOException, SAXException, ParserConfigurationException {
        String country = new WorkAddressCountryNameParser().parse(inValidResponse());
        assertTrue(isEmpty(country));
    }

    private Document validResponse() throws IOException, SAXException, ParserConfigurationException {
        String validResponse = new ClassPathResource("com/bt/rsqe/inlife/web/geo/locator/btdirectory/people.xml").textContent(RsqeCharset.defaultCharset());
        return toDocument(validResponse);
    }

    private Document validWFHResponse() throws IOException, SAXException, ParserConfigurationException {
        String validResponse = new ClassPathResource("com/bt/rsqe/inlife/web/geo/locator/btdirectory/wfh-address-response.xml").textContent(RsqeCharset.defaultCharset());
        return toDocument(validResponse);
    }

    private Document invalidEinResponse() throws IOException, SAXException, ParserConfigurationException {
        return toDocument("<people></people>");
    }

    private Document inValidResponse() throws IOException, SAXException, ParserConfigurationException {
        return null;
    }

    private Document toDocument(String response) throws ParserConfigurationException, IOException, SAXException {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(response.getBytes()));
    }
}
