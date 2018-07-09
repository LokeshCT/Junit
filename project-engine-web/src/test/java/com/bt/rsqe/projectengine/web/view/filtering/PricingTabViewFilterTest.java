package com.bt.rsqe.projectengine.web.view.filtering;

import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.projectengine.web.fixtures.PriceLineDTOFixture;
import com.bt.rsqe.projectengine.web.model.FutureAssetPricesModel;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.model.PriceLineModel;
import com.bt.rsqe.utils.RSQEMockery;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.*;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;

@RunWith(JMock.class)
public class PricingTabViewFilterTest {

    private JUnit4Mockery context = new RSQEMockery();

    private DataTableFilterValues filterValuesMock = context.mock(DataTableFilterValues.class);
    private List<LineItemModel> itemsToFilter;

    @Before
    public void before() {

        itemsToFilter = new ArrayList<LineItemModel>() {{
            add(createFutureAssetPriceModel("Onevoice", null, "UK", null));
            add(createFutureAssetPriceModel("IP Connect global", null, "UK", "aVendorDiscount"));
            add(createFutureAssetPriceModel("Onevoice", null, "India", null));
            add(createFutureAssetPriceModel("IP Connect global", null, "India", null));
        }};
    }

    @Test
    public void shouldFilterItemsAgainstTheProduct() throws Exception {
        context.checking(new Expectations() {{
            allowing(filterValuesMock).getValue("product");
            will(returnValue("Onevoice"));
            ignoring(filterValuesMock).getValue("country");
            ignoring(filterValuesMock).getValue("vendorDiscount");
        }});
        PricingTabViewFilter filter = new PricingTabViewFilter(filterValuesMock);

        final List<LineItemModel> filterResult = filter.filter(itemsToFilter);
        final List<LineItemModel> filteredItems = filterResult;

        assertThat(filteredItems.size(), is(2));
        assertThat(filteredItems.get(0).getProductName(), is("Onevoice"));
        assertThat(filteredItems.get(1).getProductName(), is("Onevoice"));
    }

    @Test
    public void shouldFilterAgainstDisplayName() throws Exception {
        itemsToFilter = new ArrayList<LineItemModel>() {{
            add(createFutureAssetPriceModel("Onevoice", "Onevoice Display Name", "UK", null));
            add(createFutureAssetPriceModel("Onevoice", "Onevoice", "India", null));
        }};

        context.checking(new Expectations() {{
            allowing(filterValuesMock).getValue("product");
            will(returnValue("Onevoice Display Name"));
            ignoring(filterValuesMock).getValue("country");
            ignoring(filterValuesMock).getValue("vendorDiscount");
        }});
        PricingTabViewFilter filter = new PricingTabViewFilter(filterValuesMock);

        final List<LineItemModel> filterResult = filter.filter(itemsToFilter);
        final List<LineItemModel> filteredItems = filterResult;

        assertThat(filteredItems.size(), is(1));
        assertThat(filteredItems.get(0).getDisplayName(), is("Onevoice Display Name"));
    }

    @Test
    public void shouldReturnAllItemsIfFilterIsBlank() throws Exception {
        context.checking(new Expectations() {{
            allowing(filterValuesMock).getValue("product");
            will(returnValue(""));
            allowing(filterValuesMock).getValue("country");
            will(returnValue(""));
            allowing(filterValuesMock).getValue("vendorDiscount");
            will(returnValue(""));
        }});
        PricingTabViewFilter filter = new PricingTabViewFilter(filterValuesMock);

        final List<LineItemModel> filterResult = filter.filter(itemsToFilter);
        final List<LineItemModel> filteredItems = filterResult;

        assertThat(filteredItems.size(), is(4));
        assertTrue(filteredItems.containsAll(itemsToFilter));
    }

    @Test
    public void shouldReturnNoItemsIfNoProductsMatch() throws Exception {
        context.checking(new Expectations() {{
            allowing(filterValuesMock).getValue("product");
            will(returnValue("No Match"));
            ignoring(filterValuesMock).getValue("country");
            ignoring(filterValuesMock).getValue("vendorDiscount");
        }});
        PricingTabViewFilter filter = new PricingTabViewFilter(filterValuesMock);

        final List<LineItemModel> filterResult = filter.filter(itemsToFilter);
        final List<LineItemModel> filteredItems = filterResult;

        assertTrue(filteredItems.isEmpty());
    }

    @Test
    public void shouldFilterItemsAgainstTheCountry() throws Exception {
        context.checking(new Expectations() {{
            allowing(filterValuesMock).getValue("country");
            will(returnValue("UK"));
            ignoring(filterValuesMock).getValue("product");
            ignoring(filterValuesMock).getValue("vendorDiscount");
        }});

        PricingTabViewFilter filter = new PricingTabViewFilter(filterValuesMock);

        final List<LineItemModel> filterResult = filter.filter(itemsToFilter);
        assertThat(filterResult.size(), is(2));

    }

    @Test
    public void shouldReturnNoItemsIfNoCountriesMatch() throws Exception {
        context.checking(new Expectations() {{
            allowing(filterValuesMock).getValue("country");
            will(returnValue("No Match"));
            ignoring(filterValuesMock).getValue("product");
            ignoring(filterValuesMock).getValue("vendorDiscount");
        }});
        PricingTabViewFilter filter = new PricingTabViewFilter(filterValuesMock);

        final List<LineItemModel> filterResult = filter.filter(itemsToFilter);
        final List<LineItemModel> filteredItems = filterResult;

        assertTrue(filteredItems.isEmpty());
    }

    @Test
    public void shouldFilterItemsByVendorDiscountReference() throws Exception {
        context.checking(new Expectations() {{
            ignoring(filterValuesMock).getValue("country");
            ignoring(filterValuesMock).getValue("product");
            allowing(filterValuesMock).getValue("vendorDiscount");
            will(returnValue("aVendorDiscount"));
        }});

        PricingTabViewFilter filter = new PricingTabViewFilter(filterValuesMock);

        final List<LineItemModel> filterResult = filter.filter(itemsToFilter);
        assertThat(filterResult.size(), is(1));
        assertThat(filterResult.get(0).getProductName(), is("IP Connect global"));
    }

    @Test
    public void shouldNotFilterByVendorDiscountWhenItIsEmpty() throws Exception {
        context.checking(new Expectations() {{
            ignoring(filterValuesMock).getValue("country");
            ignoring(filterValuesMock).getValue("product");
            allowing(filterValuesMock).getValue("vendorDiscount");
            will(returnValue(""));
        }});

        PricingTabViewFilter filter = new PricingTabViewFilter(filterValuesMock);

        final List<LineItemModel> filterResult = filter.filter(itemsToFilter);
        assertThat(filterResult.size(), is(4));
    }

    private LineItemModel createFutureAssetPriceModel(final String productName, final String displayName, final String siteCountry, final String vendorDiscount) {
        final LineItemModel assetPricesModel = context.mock(LineItemModel.class);
        final FutureAssetPricesModel futureAssetPricesModel = context.mock(FutureAssetPricesModel.class);
        final List<PriceLineModel> priceModes = newArrayList(new PriceLineModel(PriceLineDTOFixture.aPriceLineDTO().withVendorDiscountRef(vendorDiscount).build(), null, null, null, true));

        final SiteDTO site = new SiteDTO();
        site.country = siteCountry;
        context.checking(new Expectations() {{
            allowing(assetPricesModel).getProductName();
            will(returnValue(productName));
            allowing(assetPricesModel).getDisplayName();
            will(returnValue(displayName));
            allowing(assetPricesModel).getSite();
            will(returnValue(site));
            allowing(assetPricesModel).getFutureAssetPricesModel();
            will(returnValue(futureAssetPricesModel));
            allowing(futureAssetPricesModel).getDeepFlattenedPriceLines();
            will(returnValue(priceModes));
        }});

        return assetPricesModel;
    }
}
