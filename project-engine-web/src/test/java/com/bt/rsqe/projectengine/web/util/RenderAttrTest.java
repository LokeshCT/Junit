package com.bt.rsqe.projectengine.web.util;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.junit.Test;

import static com.bt.rsqe.web.XHtmlDocumentHelper.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class RenderAttrTest {
    @Test
    public void shouldRenderLayout() throws DocumentException {
        Document document = parseTemplate("/com/bt/rsqe/projectengine/web/util/render_attr_test.ftl");
        Element inputId1 = (Element) document.selectSingleNode("//input[@id='id1']");
        assertThat(inputId1, is(not(nullValue())));
        assertThat(inputId1.attributeValue("type"), is("text"));

        Element inputIdBlahId = (Element) document.selectSingleNode("//input[@id='blah-id']");
        assertThat(inputIdBlahId, is(not(nullValue())));
        assertThat(inputIdBlahId.attributeValue("type"), is("submit"));
        assertThat(inputIdBlahId.attributeValue("name"), is("blah-name"));
    }
}
