package com.bt.rsqe.projectengine.web.view.sorting;

import com.bt.rsqe.projectengine.web.view.filtering.QueryParamStub;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class DataTableSortValuesTest {

    public static final String PARAM1 = "iSortCol_0";
    public static final String PARAM2 = "sSortDir_0";
    private QueryParamStub queryParams = new QueryParamStub();


    @Test
    public void shouldGetAllFilterValuesInAGivenQueryParamList() {
        queryParams.putSingle(PARAM1, "0");
        queryParams.putSingle(PARAM2, "asc");

        SortValues sortValues = DataTableSortValues.parse(queryParams);

        assertThat(sortValues.getSortColumnIndexString(), is(PARAM1));
        assertThat(sortValues.getSortDirectionString(), is(PARAM2));
        assertThat(sortValues.getValue(PARAM1), is("0"));
        assertThat(sortValues.getValue(PARAM2), is("asc"));

    }
}
