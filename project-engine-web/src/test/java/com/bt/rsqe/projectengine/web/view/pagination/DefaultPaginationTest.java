package com.bt.rsqe.projectengine.web.view.pagination;

import org.junit.Test;

import java.util.List;

import static com.google.common.collect.Lists.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class DefaultPaginationTest {
    @Test
    public void shouldReturnAllItemsWhenEndIndexIsNegativeOne() throws Exception {
        DefaultPagination pagination = new DefaultPagination(5, 0, -1);

        List<String> items = newArrayList("1", "2", "3", "4");

        assertThat(pagination.paginate(items).size(), is(4));
    }
}
