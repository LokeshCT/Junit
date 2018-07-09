package com.bt.rsqe.projectengine.web.layouts;

import com.bt.rsqe.web.Presenter;
import com.bt.rsqe.web.View;
import org.dom4j.Document;
import org.dom4j.Element;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static com.bt.rsqe.web.XHtmlDocumentHelper.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class DefaultLayoutTest {

    private static Document document;

    @BeforeClass
    public static void setUp() throws Exception {
        String html = new Presenter().render(View.viewUsingTemplate("/com/bt/rsqe/projectengine/web/layouts/default_layout_test.ftl"));
        document = parseHtml(html.replaceAll("<!DOCTYPE.*", ""))  ;
    }

    @Test
    public void shouldContainScriptsAndCss(){
        List<Element> list = scriptsAndCss().selectNodes("/*/*");
        for (Element element : list) {
            for (Object head : document.selectNodes("//head")) {
                assertThat(((Element) head).asXML(),containsString(element.asXML()));
            }
        }
    }

    @Test
    public void shouldRenderTitleHeadAndBodyContent(){
      assertThat(document.selectSingleNode("//div[@id='with-content']/html/head/title").getText(),containsString("blah-title"));
        assertThat(document.selectSingleNode("//div[@id='with-content']/html/head").getText(),containsString("blah-head"));
        assertThat(document.selectSingleNode("//div[@id='with-content']/html/body/div[@id='container']").getText(),containsString("blah-body"));
    }

    public Document scriptsAndCss(){
        return parseTemplate("/com/bt/rsqe/projectengine/web/ScriptsAndCss.ftl");
    }


}
