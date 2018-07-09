package com.bt.rsqe.projectengine.web.view.sorting;

import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.expedio.fixtures.SiteDTOFixture;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.view.filtering.DataTableFilterValues;
import com.bt.rsqe.projectengine.web.view.filtering.PaginatedDetailsTabViewFilter;
import com.bt.rsqe.projectengine.web.view.filtering.PaginatedFilter;
import com.bt.rsqe.projectengine.web.view.filtering.PaginatedFilterResult;
import com.bt.rsqe.projectengine.web.view.filtering.QueryParamStub;
import com.bt.rsqe.projectengine.web.view.pagination.DefaultPagination;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static com.bt.rsqe.projectengine.QuoteOptionItemDTOFixture.*;
import static com.bt.rsqe.projectengine.web.fixtures.LineItemModelFixture.*;
import static com.google.common.collect.Lists.*;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PaginatedDetailsTabViewSortTest {

    private DataTableFilterValues filterValuesMock = mock(DataTableFilterValues.class);
    private ArrayList<LineItemModel> itemsToFilter;

    public static final String PARAM1 = "iSortCol_0";
    public static final String PARAM2 = "sSortDir_0";
    private QueryParamStub queryParams = new QueryParamStub();
    SortValues sortValues;
    private LineItemModel lineItemModel, lineItemModel2, lineItemModel3;

    @Before
    public void before() {
        itemsToFilter = newArrayList();
        //Given
        lineItemModel = spy(aLineItemModel().with(aQuoteOptionItemDTO().withAction("A")).build());
        lineItemModel3 = spy(aLineItemModel().with(aQuoteOptionItemDTO().withAction("C")).build());
        lineItemModel2 = spy(aLineItemModel().with(aQuoteOptionItemDTO().withAction("B")).build());

        itemsToFilter.add(lineItemModel);
        itemsToFilter.add(lineItemModel3);
        itemsToFilter.add(lineItemModel2);
    }

    @Test
    public void shouldReturnSortedItemsByActionTypeAcs() throws Exception {
        when(filterValuesMock.getValue(anyString())).thenReturn("");
        queryParams.putSingle(PARAM1, "4");
        queryParams.putSingle(PARAM2, "asc");

        sortValues = DataTableSortValues.parse(queryParams);

        int pageLength = 3;
        final PaginatedFilter paginatedFilter = new PaginatedDetailsTabViewFilter(filterValuesMock, new DefaultPagination(1, 0, pageLength));
        final PaginatedSort<LineItemModel> paginatedSort = new PaginatedDetailsTabViewSort(sortValues);

        final PaginatedFilterResult filterResult = paginatedFilter.applyTo(itemsToFilter);
        final PaginatedSortResult<LineItemModel> sortLineItems = paginatedSort.applyTo(filterResult);
        assertThat(sortLineItems.getPaginatedFilterResultItems().getItems().size(), is(pageLength));

        // check correct sorting order for action type
        assertThat(sortLineItems.getPaginatedFilterResultItems().getItems().get(0).getAction(), is("A"));
        assertThat(sortLineItems.getPaginatedFilterResultItems().getItems().get(1).getAction(), is("B"));
        assertThat(sortLineItems.getPaginatedFilterResultItems().getItems().get(2).getAction(), is("C"));
    }

    @Test
    public void shouldReturnSortedItemsByActionTypeDesc() throws Exception {
        when(filterValuesMock.getValue(anyString())).thenReturn("");
        queryParams.putSingle(PARAM1, "4");
        queryParams.putSingle(PARAM2, "desc");

        sortValues = DataTableSortValues.parse(queryParams);

        int pageLength = 3;
        final PaginatedFilter paginatedFilter = new PaginatedDetailsTabViewFilter(filterValuesMock, new DefaultPagination(1, 0, pageLength));
        final PaginatedSort<LineItemModel> paginatedSort = new PaginatedDetailsTabViewSort(sortValues);

        final PaginatedFilterResult filterResult = paginatedFilter.applyTo(itemsToFilter);
        final PaginatedSortResult<LineItemModel> sortLineItems = paginatedSort.applyTo(filterResult);
        assertThat(sortLineItems.getPaginatedFilterResultItems().getItems().size(), is(pageLength));

        // check correct sorting order for action type
        assertThat(sortLineItems.getPaginatedFilterResultItems().getItems().get(0).getAction(), is("C"));
        assertThat(sortLineItems.getPaginatedFilterResultItems().getItems().get(1).getAction(), is("B"));
        assertThat(sortLineItems.getPaginatedFilterResultItems().getItems().get(2).getAction(), is("A"));
    }

    @Test
    public void shouldSortProductsAlphabeticallyWhenSortingBySiteAscending() throws Exception {
        final SiteDTO s1 = SiteDTOFixture.aSiteDTO().withName("SITE 1").build();
        final SiteDTO s2 = SiteDTOFixture.aSiteDTO().withName("SITE 1").build();
        final SiteDTO s3 = SiteDTOFixture.aSiteDTO().withName("SITE 2").build();

        doReturn("B").when(lineItemModel).getDisplayName();
        doReturn("A").when(lineItemModel2).getDisplayName();
        doReturn("C").when(lineItemModel3).getDisplayName();

        doReturn(s1).when(lineItemModel).getSite();
        doReturn(s2).when(lineItemModel2).getSite();
        doReturn(s3).when(lineItemModel3).getSite();

        when(filterValuesMock.getValue(anyString())).thenReturn("");
        queryParams.putSingle(PARAM1, "1");
        queryParams.putSingle(PARAM2, "asc");

        sortValues = DataTableSortValues.parse(queryParams);

        int pageLength = 3;
        final PaginatedFilter paginatedFilter = new PaginatedDetailsTabViewFilter(filterValuesMock, new DefaultPagination(1, 0, pageLength));
        final PaginatedSort<LineItemModel> paginatedSort = new PaginatedDetailsTabViewSort(sortValues);

        final PaginatedFilterResult filterResult = paginatedFilter.applyTo(itemsToFilter);
        final PaginatedSortResult<LineItemModel> sortLineItems = paginatedSort.applyTo(filterResult);
        assertThat(sortLineItems.getPaginatedFilterResultItems().getItems().size(), is(pageLength));

        // check correct sorting order for action type
        assertThat(sortLineItems.getPaginatedFilterResultItems().getItems().get(0).getSite().name, is("SITE 1"));
        assertThat(sortLineItems.getPaginatedFilterResultItems().getItems().get(1).getSite().name, is("SITE 1"));
        assertThat(sortLineItems.getPaginatedFilterResultItems().getItems().get(2).getSite().name, is("SITE 2"));

        assertThat(sortLineItems.getPaginatedFilterResultItems().getItems().get(0).getDisplayName(), is("A"));
        assertThat(sortLineItems.getPaginatedFilterResultItems().getItems().get(1).getDisplayName(), is("B"));
        assertThat(sortLineItems.getPaginatedFilterResultItems().getItems().get(2).getDisplayName(), is("C"));
    }

    @Test
    public void shouldSortProductsAlphabeticallyWhenSortingBySiteDescending() throws Exception {
        final SiteDTO s1 = SiteDTOFixture.aSiteDTO().withName("SITE 1").build();
        final SiteDTO s2 = SiteDTOFixture.aSiteDTO().withName("SITE 1").build();
        final SiteDTO s3 = SiteDTOFixture.aSiteDTO().withName("SITE 2").build();

        doReturn("B").when(lineItemModel).getDisplayName();
        doReturn("A").when(lineItemModel2).getDisplayName();
        doReturn("C").when(lineItemModel3).getDisplayName();

        doReturn(s1).when(lineItemModel).getSite();
        doReturn(s2).when(lineItemModel2).getSite();
        doReturn(s3).when(lineItemModel3).getSite();

        when(filterValuesMock.getValue(anyString())).thenReturn("");
        queryParams.putSingle(PARAM1, "1");
        queryParams.putSingle(PARAM2, "desc");

        sortValues = DataTableSortValues.parse(queryParams);

        int pageLength = 3;
        final PaginatedFilter paginatedFilter = new PaginatedDetailsTabViewFilter(filterValuesMock, new DefaultPagination(1, 0, pageLength));
        final PaginatedSort<LineItemModel> paginatedSort = new PaginatedDetailsTabViewSort(sortValues);

        final PaginatedFilterResult filterResult = paginatedFilter.applyTo(itemsToFilter);
        final PaginatedSortResult<LineItemModel> sortLineItems = paginatedSort.applyTo(filterResult);
        assertThat(sortLineItems.getPaginatedFilterResultItems().getItems().size(), is(pageLength));

        // check correct sorting order for action type
        assertThat(sortLineItems.getPaginatedFilterResultItems().getItems().get(0).getSite().name, is("SITE 2"));
        assertThat(sortLineItems.getPaginatedFilterResultItems().getItems().get(1).getSite().name, is("SITE 1"));
        assertThat(sortLineItems.getPaginatedFilterResultItems().getItems().get(2).getSite().name, is("SITE 1"));

        assertThat(sortLineItems.getPaginatedFilterResultItems().getItems().get(0).getDisplayName(), is("C"));
        assertThat(sortLineItems.getPaginatedFilterResultItems().getItems().get(1).getDisplayName(), is("A"));
        assertThat(sortLineItems.getPaginatedFilterResultItems().getItems().get(2).getDisplayName(), is("B"));
    }
}


