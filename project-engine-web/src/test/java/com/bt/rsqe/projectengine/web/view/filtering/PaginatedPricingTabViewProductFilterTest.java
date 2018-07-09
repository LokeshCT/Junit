package com.bt.rsqe.projectengine.web.view.filtering;

import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.view.QuoteOptionPricingDTO;
import com.bt.rsqe.projectengine.web.view.pagination.DefaultPagination;
import com.bt.rsqe.utils.RSQEMockery;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;

@RunWith(JMock.class)
public class PaginatedPricingTabViewProductFilterTest {

    private JUnit4Mockery context = new RSQEMockery();
    private DataTableFilterValues filterValuesMock = context.mock(DataTableFilterValues.class);
    private ArrayList<LineItemModel> itemsToFilter;

    @Before
    public void before() {
        itemsToFilter = new ArrayList<LineItemModel>();
        for (int index = 0; index < 10; index++) {
            itemsToFilter.add(createFutureAssetPriceModel("Onevoice"));
            itemsToFilter.add(createFutureAssetPriceModel("IP Connect global"));
        }
    }

    @Test
    public void shouldReturnPaginatedFilteredItems() throws Exception {
        context.checking(new Expectations() {{
            allowing(filterValuesMock).getValue("product");
            will(returnValue(""));
            ignoring(filterValuesMock).getValue("country");
            ignoring(filterValuesMock).getValue("vendorDiscount");
        }});
        final PaginatedFilter paginatedFilter = new PaginatedPricingTabViewFilter(filterValuesMock, new DefaultPagination(1, 0, 10));

        final PaginatedFilterResult filterResult = paginatedFilter.applyTo(itemsToFilter);
        assertThat(filterResult.getItems().size(), is(10));
    }

    @Test
    public void shouldFilterProductAndPaginate() throws Exception {
        context.checking(new Expectations() {{
            allowing(filterValuesMock).getValue("product");
            will(returnValue("Onevoice"));
            ignoring(filterValuesMock).getValue("country");
            ignoring(filterValuesMock).getValue("vendorDiscount");
        }});
        final PaginatedFilter paginatedFilter = new PaginatedPricingTabViewFilter(filterValuesMock, new DefaultPagination(2, 0, 2));

        final PaginatedFilterResult filterResult = paginatedFilter.applyTo(itemsToFilter);
        final List<LineItemModel> paginatedFilteredItems = filterResult.getItems();

        assertThat(paginatedFilteredItems.size(), is(2));
        assertThat(paginatedFilteredItems.get(0).getProductName(), is("Onevoice"));
        assertThat(filterResult.getPageNumber(), is(2));
    }

    @Test
    public void shouldGetFilteredItemsSizeAndTotalRecords() throws Exception {
        context.checking(new Expectations() {{
            allowing(filterValuesMock).getValue("product");
            will(returnValue("IP Connect global"));
            ignoring(filterValuesMock).getValue("country");
            ignoring(filterValuesMock).getValue("vendorDiscount");
        }});
        final PaginatedFilter paginatedFilter = new PaginatedPricingTabViewFilter(filterValuesMock, new DefaultPagination(2, 0, 2));
        final PaginatedFilterResult filterResult = paginatedFilter.applyTo(itemsToFilter);
        final List<QuoteOptionPricingDTO.ItemRowDTO> paginatedFilteredItems = filterResult.getItems();
        assertThat(paginatedFilteredItems.size(), is(2));
        assertThat(filterResult.getFilteredSize(), is(10));
        assertThat(filterResult.getTotalRecords(), is(20));
    }

        private LineItemModel createFutureAssetPriceModel(final String productName) {
        final LineItemModel assetPricesModel = context.mock(LineItemModel.class);

        context.checking(new Expectations(){{
            allowing(assetPricesModel).getProductName();
            will(returnValue(productName));
            ignoring(assetPricesModel);
        }});

        return assetPricesModel;
    }
}
