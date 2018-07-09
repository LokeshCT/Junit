package com.bt.rsqe.projectengine.web.view.filtering;

import com.bt.rsqe.domain.QuoteOptionItemStatus;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.view.pagination.DefaultPagination;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.*;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PaginatedDetailsTabViewFilterTest {

    private DataTableFilterValues filterValuesMock = mock(DataTableFilterValues.class);
    private ArrayList<LineItemModel> itemsToFilter;

    @Before
    public void before() {
        itemsToFilter = newArrayList();

        for (QuoteOptionItemStatus status: QuoteOptionItemStatus.values()) {
            LineItemModel model = mock(LineItemModel.class);
            when(model.getLineItemStatus()).thenReturn(status);
            itemsToFilter.add(model);
        }

        when(filterValuesMock.getValue(LineItemGlobalSearchFilter.GLOBAL_SEARCH_FILTER)).thenReturn("");
    }

    @Test
    public void shouldReturnPaginatedFilteredItems() throws Exception {
        when(filterValuesMock.getValue("excludeFailed")).thenReturn("");
        int pageLength = 5;
        final PaginatedFilter paginatedFilter = new PaginatedDetailsTabViewFilter(filterValuesMock, new DefaultPagination(1, 0, pageLength));

        final PaginatedFilterResult filterResult = paginatedFilter.applyTo(itemsToFilter);
        assertThat(filterResult.getItems().size(), is(pageLength));
    }

    @Test
    public void shouldReturnSecondPage() throws Exception {
        when(filterValuesMock.getValue("excludeFailed")).thenReturn("");
        int pageNumber = 2;
        int pageLength = 3;
        final PaginatedFilter paginatedFilter = new PaginatedDetailsTabViewFilter(filterValuesMock, new DefaultPagination(pageNumber, 0, pageLength));

        final PaginatedFilterResult filterResult = paginatedFilter.applyTo(itemsToFilter);
        final List<LineItemModel> paginatedFilteredItems = filterResult.getItems();

        assertThat(paginatedFilteredItems.size(), is(pageLength));
        assertThat(filterResult.getPageNumber(), is(pageNumber));
    }

    @Test
    public void shouldGetFilteredItemsSizeAndTotalRecords() throws Exception {
        when(filterValuesMock.getValue("excludeFailed")).thenReturn("yes");
        int pageNumber = 2;
        int pageLength = 2;
        final PaginatedFilter paginatedFilter = new PaginatedDetailsTabViewFilter(filterValuesMock, new DefaultPagination(pageNumber, 0, pageLength));
        final PaginatedFilterResult filterResult = paginatedFilter.applyTo(itemsToFilter);
        final List<LineItemModel> paginatedFilteredItems = filterResult.getItems();
        assertThat(paginatedFilteredItems.size(), is(pageLength));
        assertThat(filterResult.getFilteredSize(), is(13));
        assertThat(filterResult.getTotalRecords(), is(14));
    }

  }
