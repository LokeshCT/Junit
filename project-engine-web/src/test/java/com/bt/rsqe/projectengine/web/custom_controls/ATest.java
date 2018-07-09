package com.bt.rsqe.projectengine.web.custom_controls;

import org.dom4j.Document;
import org.dom4j.Element;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.bt.rsqe.web.XHtmlDocumentHelper.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class ATest {
    private Document document;

    @Before
    public void setUp() throws Exception {
        document = parseTemplate("/com/bt/rsqe/projectengine/web/custom_controls/a_test.ftl");
    }

    @Test
    public void shouldRenderDisabledLinkAsASpan() {
        Element span = (Element) document.selectSingleNode("//span[@id='5']");
        assertThat(span.getText(), containsString("some text"));
        assertThat(span.attributeValue("class"), containsString("disabled"));
        assertNull(span.attributeValue("display"));
    }

    @Test
    public void shouldRenderLinkWithAllPossibleAttributesAndInnerText() {
        Element a = (Element) document.selectSingleNode("//a[@id='4']");
        assertThat(a.getText(), containsString("some text"));
        assertThat(a.attributeValue("href"), containsString("link"));
        assertThat(a.attributeValue("class"), containsString("blah-class"));
        assertThat(a.attributeValue("target"), containsString("foo"));
    }

    @Test
    public void shouldRenderWithOptionalAttrs() {
        Element a = (Element) document.selectSingleNode("//a[@id='6']");
        assertThat(a.getText(), containsString("some text"));
        assertThat(a.attributeValue("href"), containsString("link"));
        assertThat(a.attributeValue("class"), containsString("blah-class"));
        assertThat(a.attributeValue("target"), containsString("foo"));
    }

    @Test
    public void shouldRenderLinkWithNoParams() {
        assertThat(document.selectSingleNode("//a[@href='#']"), is(not(nullValue())));
    }

    @Test
    public void shouldRenderSwitchDisabledAsALinkAndHiddenSpan() throws Exception {
        final List list = document.selectNodes("//a");
        Element a = (Element) document.selectSingleNode("//a[@id='7']");
        assertThat(a.getText(), containsString("some text"));
        Element span = (Element) document.selectSingleNode("//span[@id='7Disabled']");
        assertThat(span.getText(), containsString("some text"));
        assertThat(span.attributeValue("class"), containsString("hidden"));
    }

    @Test
    public void shouldRenderSwitchDisabledAndDisabledAsASpan() throws Exception {
        Element span = (Element) document.selectSingleNode("//span[@id='8']");
        assertThat(span.getText(), containsString("some text"));
        assertThat(span.attributeValue("class"), containsString("disabled"));
    }
}
