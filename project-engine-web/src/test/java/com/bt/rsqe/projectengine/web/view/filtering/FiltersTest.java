package com.bt.rsqe.projectengine.web.view.filtering;

import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;

public class FiltersTest {

    private static final String MATCH = "Match";
    private static final String NOT_MATCH = "Not Match";
    private static final String TEST_1 = "Test1";
    private static final String TEST_2 = "Test2";
    private static final String TEST_3 = "Test3";

    @Test
    public void shouldReturnOnlyItemsMatchingFilter() {
        Filters<String> filters = createStringFilters(MATCH);
        List itemsToFilter = Arrays.asList(MATCH, NOT_MATCH, NOT_MATCH, MATCH);

        List<String> filteredItems = filters.apply(itemsToFilter);

        assertThat(filteredItems.size(), is(2));
        Iterator<String> filterIterator = filteredItems.iterator();
        assertThat(filterIterator.next(), is(MATCH));
        assertThat(filterIterator.next(), is(MATCH));
    }

    @Test
    public void shouldReturnAllItemsIfTheFiltersMatch() {
        Filters<String> filters = createStringFilters(MATCH);
        List itemsToFilter = Arrays.asList(MATCH, MATCH, MATCH);

        List<String> filteredItems = filters.apply(itemsToFilter);

        assertThat(filteredItems.size(), is(3));
        Iterator<String> filterIterator = filteredItems.iterator();
        assertThat(filterIterator.next(), is(MATCH));
        assertThat(filterIterator.next(), is(MATCH));
        assertThat(filterIterator.next(), is(MATCH));
    }

    @Test
    public void shouldReturnNoItemsIfTheFiltersDoNotMatch() {
        Filters<String> filters = createStringFilters(NOT_MATCH);
        List itemsToFilter = Arrays.asList(MATCH, MATCH, MATCH, MATCH);

        List<String> filteredItems = filters.apply(itemsToFilter);

        assertThat(filteredItems.size(), is(0));
    }

    @Test
    public void shouldReturnAllItemsIfThereAreNoFilters() {
        final Filters<String> filters = new Filters<String>();
        List itemsToFilter = Arrays.asList(MATCH, NOT_MATCH, NOT_MATCH, MATCH);
        List<String> filteredItems = filters.apply(itemsToFilter);

        assertThat(filteredItems.size(), is(4));
    }

    @Test
    public void shouldReturnOnlyItemsMatchingMultipleFilters() {
        Filters<TestItem> filters = createTestItemFilters();

        List<TestItem> itemsToFilter = Arrays.asList(
            new TestItem(TEST_1, TEST_2),
            new TestItem(TEST_2, TEST_3),
            new TestItem(TEST_1, TEST_2)
        );

        List<TestItem> filteredItems = filters.apply(itemsToFilter);

        assertThat(filteredItems.size(), is(1));
        TestItem testItem = filteredItems.iterator().next();
        assertThat(testItem.getItemText1(), is(TEST_2));
        assertThat(testItem.getItemText2(), is(TEST_3));
    }

    private Filters<TestItem> createTestItemFilters() {
        return new Filters<TestItem>() {{
            add(
                new Filter<TestItem>() {
                    public boolean apply(TestItem items) {
                        return TEST_2.equals(items.getItemText1());
                    }
                });

            add(
                new Filter<TestItem>() {
                    public boolean apply(TestItem items) {
                        return TEST_3.equals(items.getItemText2());
                    }
                });
        }};
    }

    private Filters<String> createStringFilters(final String text) {
        return new Filters<String>() {{
            add(
                new Filter<String>() {
                    public boolean apply(String items) {
                        return text.equals(items);
                    }
                });
        }};
    }

    private class TestItem {
        private String itemText1;
        private String itemText2;

        private TestItem(String itemText1, String itemText2) {
            this.itemText1 = itemText1;
            this.itemText2 = itemText2;
        }

        public String getItemText1() {
            return itemText1;
        }

        public String getItemText2() {
            return itemText2;
        }
    }

}
