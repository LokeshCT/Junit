package com.bt.rsqe.projectengine.web.view.filtering;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;

public class DataTableFilterValuesTest {

    public static final String PARAM1 = "param1";
    public static final String PARAM2 = "param2";
    public static final String S_SEARCH = "sSearch";
    private QueryParamStub queryParams = new QueryParamStub();

    @Before
    public void before() {
    }

    @Test
    public void shouldGetAllFilterValuesInAGivenQueryParamList() {
        queryParams.putSingle(S_SEARCH, "param1=value1|param2=value2");

        FilterValues filterValues = DataTableFilterValues.parse(queryParams);

        assertThat(filterValues.isThereAnyFilterValues(), is(true));
        assertThat(filterValues.getValue(PARAM1), is("value1"));
        assertThat(filterValues.getValue(PARAM2), is("value2"));

    }

    @Test
    public void shouldReturnFalseIfThereAreNoFilterValuesSet() {
        queryParams.putSingle(S_SEARCH, "param1=|param2=");

        FilterValues filterValues = DataTableFilterValues.parse(queryParams);

        assertFalse(filterValues.isThereAnyFilterValues());
        assertThat(filterValues.getValue(PARAM1), is(""));
        assertThat(filterValues.getValue(PARAM2), is(""));
    }

    @Test
    public void shouldGetGlobalSearchFilterCriteria() throws Exception {
        queryParams.putSingle(LineItemGlobalSearchFilter.GLOBAL_SEARCH_FILTER, "someCriteria");
        FilterValues filterValues = DataTableFilterValues.parse(queryParams);
        assertThat(filterValues.getValue(LineItemGlobalSearchFilter.GLOBAL_SEARCH_FILTER), is("someCriteria"));
    }

    @Test
    public void shouldGetEmptyGlobalSearchFilterCriteriaWhenParamIsEmpty() throws Exception {
        queryParams.putSingle(LineItemGlobalSearchFilter.GLOBAL_SEARCH_FILTER, "");
        FilterValues filterValues = DataTableFilterValues.parse(queryParams);
        assertThat(filterValues.getValue(LineItemGlobalSearchFilter.GLOBAL_SEARCH_FILTER), is(""));
    }
}

