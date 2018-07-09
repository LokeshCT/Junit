package com.bt.cqm.config;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XmlConfigurationParser implements ConfigurationParser {
    private final InputStream configStream;
    private final Class resourceLoadingContext;

    public XmlConfigurationParser(InputStream configStream, Class resourceLoadingContext) {

        this.configStream = configStream;
        this.resourceLoadingContext = resourceLoadingContext;
    }

    @Override
    public ConfigurationElement parse() throws ConfigurationException, IOException {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            SAXParser parser = factory.newSAXParser();
            XmlHandler handler = new XmlHandler(resourceLoadingContext);
            parser.parse(configStream, handler);
            return handler.getRootElement();
        } catch (SAXException e) {
            throw new ConfigurationException(e);
        } catch (ParserConfigurationException e) {
            throw new ConfigurationException(e);
        }
    }

    private static class XmlHandler extends DefaultHandler {
        private Deque<XmlConfigurationElement> elements = new ArrayDeque<XmlConfigurationElement>();
        private XmlConfigurationElement theRoot;
        private static final String INCLUDE_ELEMENT = "INCLUDE";
        private static final String INCLUDED_RESOURCE_ATTR_NAME = "resource";
        private final Class resourceLoadingContext;

        public XmlHandler(Class resourceLoadingContext) {
            this.resourceLoadingContext = resourceLoadingContext;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            handleIncludedContent(localName, attributes);
            elements.push(new XmlConfigurationElement(localName, attributeMap(attributes)));
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            XmlConfigurationElement element = elements.pop();
            if (elements.isEmpty()) {
                theRoot = element;
            } else {
                XmlConfigurationElement parentElement = elements.peek();
                parentElement.addChildElement(element);
            }
        }

        public XmlConfigurationElement getRootElement() {
            return theRoot;
        }

        private void handleIncludedContent(String localName, Attributes attributes) {
            if (INCLUDE_ELEMENT.equals(localName)) {
                String resource = attributes.getValue(INCLUDED_RESOURCE_ATTR_NAME);
                XmlConfigurationElement parentElement = elements.peek();
                for (XmlConfigurationElement includedElement : getIncludedElements(resource)) {
                    parentElement.addChildElement(includedElement);
                }
            }
        }

        private List<XmlConfigurationElement> getIncludedElements(String resource) {
            if (isEmpty(resource)) {
                throw new ConfigurationException("Missing value for included " + INCLUDED_RESOURCE_ATTR_NAME);
            }

            try {
                InputStream resourceAsStream = resourceLoadingContext.getResourceAsStream(resource);
                if (isEmpty(resourceAsStream)) {
                    throw new ConfigurationException("Could not find the included resource: " + resource);
                }
                XmlConfigurationParser parser = new XmlConfigurationParser(resourceAsStream, resourceLoadingContext);

                XmlConfigurationElement includedParent = (XmlConfigurationElement) parser.parse();
                return includedParent.getChildAllElements();
            } catch (IOException e) {
                throw new ConfigurationException("Failed to process the Included resource " + resource, e);
            }
        }

        private boolean isEmpty(Object value) {
            return (value == null || value.toString().trim().equals(""));
        }

        private Map<String, String> attributeMap(Attributes attributes) {
            Map<String, String> attributeMap = new HashMap<String, String>(attributes.getLength(), 1.0F);
            for (int i = 0; i < attributes.getLength(); i++) {
                attributeMap.put(attributes.getLocalName(i), attributes.getValue(i));
            }

            return attributeMap;
        }
    }
}
