package com.bt.rsqe.utils.countries;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class CountriesXmlMarshallLoadTest {

    private static final Countries countries = new Countries();
    private JAXBContext jaxbContext;
    private Marshaller marshaller;
    private Unmarshaller unmarshaller;
    private FooDto foo;
    private Country expectedCountry;
    private BufferedReader reader;
    private BufferedOutputStream out;

    @XmlRootElement
    public static class FooDto {
        public Country country;
    }

    @Before
    public void setup() throws Exception {
        PipedInputStream pipeInput = new PipedInputStream();
        reader = new BufferedReader(new InputStreamReader(pipeInput));
        out = new BufferedOutputStream(new PipedOutputStream(pipeInput));

        jaxbContext = JAXBContext.newInstance(FooDto.class);
        marshaller = jaxbContext.createMarshaller();
        unmarshaller = jaxbContext.createUnmarshaller();
        expectedCountry = countries.byIso("IE");

        foo = new FooDto();
        foo.country = expectedCountry;
    }

    @Test
    public void shouldMarshall() throws Exception {
        marshaller.marshal(foo, out);
        out.close();
    }

    @Test
    public void shouldUnmarshall() throws Exception {
        marshaller.marshal(foo, out);
        out.close();

        FooDto loadedFoo = (FooDto) unmarshaller.unmarshal(reader);
        Assert.assertEquals(countries.byIso("IE"), loadedFoo.country);
    }
}
