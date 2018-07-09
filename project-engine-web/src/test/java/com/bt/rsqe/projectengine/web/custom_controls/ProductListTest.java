package com.bt.rsqe.projectengine.web.custom_controls;

import com.bt.rsqe.domain.bom.fixtures.SellableProductFixture;
import com.bt.rsqe.domain.product.SellableProduct;
import com.bt.rsqe.projectengine.web.view.Products;
import com.bt.rsqe.web.View;
import org.dom4j.Document;
import org.dom4j.Element;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.bt.rsqe.web.XHtmlDocumentHelper.*;
import static java.lang.String.*;
import static java.util.Arrays.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class ProductListTest {
    private Document document;
    private SellableProduct productIdentifier1;
    private SellableProduct productIdentifier2;

    @Before
    public void setUp() throws Exception {
        productIdentifier1 = SellableProductFixture.aProduct().withId("id1").withName("prod1").build();
        productIdentifier2 = SellableProductFixture.aProduct().withId("id2").withName("prod2").build();
        final Products products = new Products(asList(
            productIdentifier1,
            productIdentifier2,
            SellableProductFixture.aProduct().withId("").withName("").build()
        ));

        document = parseView(View.viewUsingTemplate("/com/bt/rsqe/projectengine/web/custom_controls/products_test.ftl").withContext("model", products));
    }

    @Test
    public void shouldRenderProducts() {
        final List<Element> list = document.selectNodes("//select[@id='1']/option");
        assertThat(list, hasItems(forProd(productIdentifier1), forProd(productIdentifier2)));
    }

    @Test
    public void shouldPurgeSpacesInValues() {
        final List<Element> list = document.selectNodes("//select[not(@id)]/option");
        assertThat(list, hasItems(forProd(productIdentifier1, "%s"), forProd(productIdentifier2, "%s")));
    }

    @Test
    public void shouldSelectSpecifiedOption() {
        final Element option = (Element) document.selectSingleNode("//select[@id='2']/option[@selected]");
        assertThat(option, is(notNullValue()));
        assertThat(option.attributeValue("value"), is("prefix-\"id2"));

    }

    private Matcher<Element> forProd(final SellableProduct productIdentifier) {
        return forProd(productIdentifier, "prefix-\"%s");
    }

    private Matcher<Element> forProd(final SellableProduct productIdentifier,
                                     final String format) {
        return new TypeSafeMatcher<Element>() {
            @Override
            public boolean matchesSafely(Element element) {
                return element.attributeValue("value").equals(format(format, productIdentifier.getProductId())) && element.getText().contains(productIdentifier.getProductName());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("expected product");
                description.appendText(productIdentifier.toString());
            }
        };
    }

}
