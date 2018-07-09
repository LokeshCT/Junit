package com.bt.rsqe.projectengine.web.view;

import com.bt.rsqe.domain.bom.fixtures.SellableProductFixture;
import com.bt.rsqe.domain.product.PrerequisiteUrl;
import com.bt.rsqe.domain.product.SellableProduct;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static com.bt.rsqe.matchers.ReflectionEqualsMatcher.*;
import static com.google.common.collect.Lists.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class ProductsTest {

    public static final String S_CODE_1 = "sCode1";

    @Test
    public void shouldRetrieveAllSCodes() {
        PrerequisiteUrl prerequisiteUrl = new PrerequisiteUrl("direct", "indirect");
        Products products = new Products(Arrays.asList(SellableProductFixture.aProduct()
                                                                             .withId(S_CODE_1)
                                                                             .withName("Product1")
                                                                             .withFamily("F1", "F1 Group Category")
                                                                             .withCategory("H1", "H1 Category")
                                                                             .withSiteInstallable(false)
                                                                             .withPrerequisiteUrl(prerequisiteUrl)
                                                                             .build(),
                                                       SellableProductFixture.aProduct()
                                                                             .withId("sCode2")
                                                                             .withName("Product2")
                                                                             .withFamily("F1", "F1 Group Category")
                                                                             .withCategory("H2", "H2 Category")
                                                                             .build()));
        assertThat(products.products(), hasItems(reflectionEquals(new Product(S_CODE_1, "1.0", "Product1", false, prerequisiteUrl, new Category("F1", "F1 Group Category"), new Category("H1", "H1 Category", -1, null))),
                                                 reflectionEquals(new Product("sCode2", "1.0", "Product2", false, null, new Category("F1", "F1 Group Category"), new Category("H2", "H2 Category", -1, null)))));
    }

    @Test
    public void shouldReturnProductName() {
        Products products = new Products(Arrays.asList(SellableProductFixture.aProduct().withId(S_CODE_1).withName("Product1").build(), SellableProductFixture.aProduct().withId("sCode2").withName("Product2").build()));
        assertThat(products.getName(S_CODE_1), is("Product1"));
        assertThat(products.getName("foo"), is(nullValue()));
        assertThat(products.getName(null), is(nullValue()));
    }

    @Test
    public void shouldReturnOrderPreRequisiteUrl() {
        Products products = new Products(Arrays.asList(SellableProductFixture.aProduct()
                                                                             .withId(S_CODE_1)
                                                                             .withName("Product1")
                                                                             .withOrderPreRequisiteUrl("orderPreRequisiteUrl1")
                                                                             .build(),
                                                       SellableProductFixture.aProduct()
                                                                             .withId("sCode2")
                                                                             .withName("Product2")
                                                                             .withOrderPreRequisiteUrl("orderPreRequisiteUrl2")
                                                                             .build()));
        assertThat(products.getCategories().get(0).getOrderPreRequisiteUrl(), is("orderPreRequisiteUrl1"));
        assertThat(products.getCategories().get(1).getOrderPreRequisiteUrl(), is("orderPreRequisiteUrl2"));
    }

    @Test
    public void shouldSortCategoriesByDisplayIndex() throws Exception {
        SellableProduct p1 = SellableProductFixture.aProduct().withId("P1").withCategory("C1", "C1", 3).build();
        SellableProduct p2 = SellableProductFixture.aProduct().withId("P2").withCategory("C2", "C2", 5).build();
        SellableProduct p3 = SellableProductFixture.aProduct().withId("P3").withCategory("C3", "C3", 1).build();

        Products products = new Products(newArrayList(p1, p2, p3));

        List<Category> categories = products.getCategories();
        assertThat(categories.size(), is(3));
        assertThat(categories.get(0).getId(), is("C3"));
        assertThat(categories.get(1).getId(), is("C1"));
        assertThat(categories.get(2).getId(), is("C2"));
    }

    @Test
    public void shouldGetCategoryGroupsOrderedAlphabetically() throws Exception {
        SellableProduct p1 = SellableProductFixture.aProduct().withId("P1").withFamily("B1", "B1").build();
        SellableProduct p2 = SellableProductFixture.aProduct().withId("P2").withFamily("B1", "B1").build();
        SellableProduct p3 = SellableProductFixture.aProduct().withId("P3").withFamily("A1", "A1").build();

        Products products = new Products(newArrayList(p1, p2, p3));

        List<Category> categories = products.getCategoryGroups();
        assertThat(categories.size(), is(2));
        assertThat(categories.get(0).getId(), is("A1"));
        assertThat(categories.get(1).getId(), is("B1"));
    }
}
