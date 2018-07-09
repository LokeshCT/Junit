package com.bt.rsqe.projectengine.web.view.filtering;

import com.bt.rsqe.domain.project.PricingStatus;
import com.bt.rsqe.expedio.fixtures.SiteDTOFixture;
import com.bt.rsqe.domain.QuoteOptionItemStatus;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.bt.rsqe.projectengine.web.view.filtering.DetailsTabViewFilter.*;
import static com.google.common.collect.Lists.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.Is.*;
import static org.junit.internal.matchers.IsCollectionContaining.*;
import static org.mockito.Mockito.*;


public class DetailsTabViewFilterTest {

    private FilterValues filterValuesMock = mock(FilterValues.class);
    private DetailsTabViewFilter detailsTabViewFilter;
    private LineItemModel failedItem = mock(LineItemModel.class);
    private LineItemModel approvedItem = mock(LineItemModel.class);
    private List<LineItemModel> items = newArrayList(failedItem, approvedItem);

    @Before
    public void setup() {
        when(filterValuesMock.getValue(LineItemGlobalSearchFilter.GLOBAL_SEARCH_FILTER)).thenReturn("");
        when(filterValuesMock.getValue(EXCLUDE_FAILED)).thenReturn("");
    }

    @Test
    public void shouldReturnFailedItemsIfExcludeFailedItemsIsNo() throws Exception {
        when(filterValuesMock.getValue(EXCLUDE_FAILED)).thenReturn("no");
        mockLineItems();
        detailsTabViewFilter = new DetailsTabViewFilter(filterValuesMock);

        final List<LineItemModel> filteredItems = detailsTabViewFilter.filter(items);
        assertThat(filteredItems.size(), is(2));
    }

    @Test
    public void shouldNotReturnFailedItemsIfExcludeFailedItemsIsYes() throws Exception {
        when(filterValuesMock.getValue(EXCLUDE_FAILED)).thenReturn("yes");
        mockLineItems();
        detailsTabViewFilter = new DetailsTabViewFilter(filterValuesMock);

        final List<LineItemModel> filteredItems = detailsTabViewFilter.filter(items);
        assertThat(filteredItems.size(), is(1));
        assertThat(filteredItems.get(0), is(approvedItem));
    }

    @Test
    public void shouldReturnFailedItemsIfExcludeFailedItemsIsEmpty() throws Exception {
        when(filterValuesMock.getValue(EXCLUDE_FAILED)).thenReturn("");
        mockLineItems();
        detailsTabViewFilter = new DetailsTabViewFilter(filterValuesMock);

        final List<LineItemModel> filteredItems = detailsTabViewFilter.filter(items);
        assertThat(filteredItems.size(), is(2));
    }

    @Test
    public void shouldAddLineItemGlobalSearchFilterWhenCriteriaExists() throws Exception {
        when(filterValuesMock.getValue(LineItemGlobalSearchFilter.GLOBAL_SEARCH_FILTER)).thenReturn("SITE 1");
        detailsTabViewFilter = new DetailsTabViewFilter(filterValuesMock);

        LineItemModel model1 = seedModel("SITE 1", "P1", PricingStatus.FIRM);
        LineItemModel model2 = seedModel("SITE 2", "P1", PricingStatus.FIRM);
        LineItemModel model3 = seedModel("SITE 1", "P1", PricingStatus.FIRM);

        final List<LineItemModel> filteredItems = detailsTabViewFilter.filter(newArrayList(model1, model2, model3));
        assertThat(filteredItems.size(), is(2));
        assertThat(filteredItems, hasItems(model1, model3));
    }

    private void mockLineItems() {
        when(failedItem.getLineItemStatus()).thenReturn(QuoteOptionItemStatus.FAILED);
        when(approvedItem.getLineItemStatus()).thenReturn(QuoteOptionItemStatus.CUSTOMER_APPROVED);
    }

    private LineItemModel seedModel(String siteName, String productName, PricingStatus pricingStatus) {
        LineItemModel model = mock(LineItemModel.class);
        when(model.getSite()).thenReturn(SiteDTOFixture.aSiteDTO().withName(siteName).build());
        when(model.getProductName()).thenReturn(productName);
        when(model.getPricingStatusOfTree()).thenReturn(pricingStatus);
        return model;
    }
}
