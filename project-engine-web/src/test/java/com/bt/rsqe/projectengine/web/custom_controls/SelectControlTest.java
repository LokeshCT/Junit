package com.bt.rsqe.projectengine.web.custom_controls;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.junit.Before;
import org.junit.Test;

import static com.bt.rsqe.web.XHtmlDocumentHelper.*;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;

public class SelectControlTest {

    private Document document;

    @Before
    public void setUp() throws Exception {
        document = parseTemplate("/com/bt/rsqe/projectengine/web/custom_controls/select_test.ftl");
    }

    @Test
    public void shouldGenerateSelectWithDefaultOption() throws DocumentException {
        assertThat(document.selectSingleNode("//select[@name='select-name']/option[@value='']").getText(), is("--Please Select--"));
    }

    @Test
    public void shouldCustomizeDefaultText(){
        assertThat(document.selectSingleNode("//select[@id='with-custom-text']/option[@value='']").getText(),is("custom text"));
    }

    @Test
    public void shouldIncludePassedInOptions(){
        assertThat(document.selectSingleNode("//select[@name='select-name']/option[@value='blah-value1']").getText(),is("blah text1"));
        assertThat(document.selectSingleNode("//select[@name='select-name']/option[@value='blah-value2']").getText(),is("blah text2"));
    }

    @Test
    public void shouldTakeInOtherAttrs(){
        assertThat(((Element) document.selectSingleNode("//select[@id='attr-id']")).attributeValue("name"),is("attr-name"));

    }
}
