package com.bt.rsqe.projectengine.web.view;

import com.bt.rsqe.util.Assertions;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class CategoryTest {
    @Test
    public void shouldHaveWorkingAccessorMethods() throws Exception {
        Category category = new Category("anId", "aValue");
        assertThat(category.getId(), is("anId"));
        assertThat(category.getName(), is("aValue"));
    }

    @Test
    public void shouldHaveAWorkingEqualsAndHashCode() throws Exception {
        Category category1 = new Category("anId", "aValue");
        Category category2 = new Category("anId", "aValue");
        Category category3 = new Category("diffId", "diffValue");
        Category category4 = new Category("anId", "aValue", 0, "anOrderPreRequisiteUrl");

        Assertions.assertThatEqualsAndHashcodeWork(category1, category2, category3, category4);
        assertThat(category4.getOrderPreRequisiteUrl(), is("anOrderPreRequisiteUrl"));

    }
}
