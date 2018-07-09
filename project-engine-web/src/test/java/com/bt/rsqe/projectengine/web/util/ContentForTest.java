package com.bt.rsqe.projectengine.web.util;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.junit.Test;

import static com.bt.rsqe.web.XHtmlDocumentHelper.*;
import static org.hamcrest.text.StringContains.*;
import static org.junit.Assert.*;

public class ContentForTest {
    @Test
    public void shouldRenderLayout() throws DocumentException {
        Document document = parseTemplate("/com/bt/rsqe/projectengine/web/util/content_for_test.ftl");
        assertThat(document.selectSingleNode("//head").getText(),containsString("head content"));
        assertThat(document.selectSingleNode("//body").getText(),containsString("body content"));
    }
}
