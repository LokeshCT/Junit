package com.bt.rsqe.projectengine.web.custom_controls;

import org.dom4j.Document;
import org.junit.Before;
import org.junit.Test;

import static com.bt.rsqe.web.XHtmlDocumentHelper.*;
import static org.hamcrest.core.Is.*;
import static org.hamcrest.core.IsNot.*;
import static org.hamcrest.core.IsNull.*;
import static org.junit.Assert.*;
import static org.junit.matchers.StringContains.*;

@SuppressWarnings("PMD.TooManyStaticImports")      // Test class
public class DialogTest {
    private Document document;

    @Before
    public void setUp() throws Exception {
        document = parseTemplate("/com/bt/rsqe/projectengine/web/custom_controls/dialog_test.ftl");
    }

    @Test
    public void shouldCreateDialogWithoutDefaultCancelButton(){
        assertThat(document.selectSingleNode("//div[@id='']//input").asXML(),is("<input type=\"button\" class=\"cancel button\" value=\"Cancel\"/>"));
    }

    @Test
    public void shouldRenderDialogContent(){
        assertThat(document.selectSingleNode("//div[@id='']").getText(),containsString("dialog1 content"));
        assertThat(document.selectSingleNode("//div[@id='dialog2']").getText(),containsString("dialog2 content"));
    }

    @Test
    public void shouldRenderOtherButtons(){
        assertThat(document.selectSingleNode("//div[@id='dialog2']//button"),is(not(nullValue())));
    }
}
