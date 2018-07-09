package com.bt.rsqe.projectengine.web.view.filtering;

import com.bt.rsqe.domain.project.PricingStatus;
import com.bt.rsqe.expedio.fixtures.SiteDTOFixture;
import com.bt.rsqe.projectengine.web.fixtures.LineItemModelFixture;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

public class LineItemGlobalSearchFilterTest {
    @Test
    public void shouldAllowAllLineItemsIfNoCriteriaHasBeenSet() throws Exception {
        LineItemGlobalSearchFilter filter = new LineItemGlobalSearchFilter(null);
        assertThat(filter.apply(LineItemModelFixture.aLineItemModel().build()), is(true));
    }

    @Test
    public void shouldAllowLineItemIfSiteNameMatchesCriteria() throws Exception {
        LineItemGlobalSearchFilter filter = new LineItemGlobalSearchFilter(" ite 1 ");

        LineItemModel model1 = seedModel("SITE 1", null, null, PricingStatus.NOT_PRICED);
        LineItemModel model2 = seedModel("SITE 2", null, null, PricingStatus.NOT_PRICED);

        assertThat(filter.apply(model1), is(true));
        assertThat(filter.apply(model2), is(false));
    }

    @Test
    public void shouldAllowLineItemIfProductNameMatchesCriteria() throws Exception {
        LineItemGlobalSearchFilter filter = new LineItemGlobalSearchFilter("od 1");

        LineItemModel model1 = seedModel("SITE 1", "Prod 1", null, PricingStatus.NOT_PRICED);
        LineItemModel model2 = seedModel("SITE 2", "Prod 2", null, PricingStatus.NOT_PRICED);

        assertThat(filter.apply(model1), is(true));
        assertThat(filter.apply(model2), is(false));
    }

    @Test
    public void shouldAllowLineItemIfPricingStatusMatchesCriteria() throws Exception {
        LineItemGlobalSearchFilter filter = new LineItemGlobalSearchFilter("FiRm");

        LineItemModel model1 = seedModel("SITE 1", "Prod 1", null, PricingStatus.FIRM);
        LineItemModel model2 = seedModel("SITE 2", "Prod 2", null, PricingStatus.NOT_PRICED);

        assertThat(filter.apply(model1), is(true));
        assertThat(filter.apply(model2), is(false));
    }

    @Test
    public void shouldAllowLineItemIfSummaryMatchesCriteria() throws Exception {
        LineItemGlobalSearchFilter filter = new LineItemGlobalSearchFilter("mary 1");

        LineItemModel model1 = seedModel("SITE 1", "Prod 1", "Summary 1", PricingStatus.FIRM);
        LineItemModel model2 = seedModel("SITE 2", "Prod 2", "Summary 2", PricingStatus.NOT_PRICED);

        assertThat(filter.apply(model1), is(true));
        assertThat(filter.apply(model2), is(false));
    }

    @Test
    public void shouldNotAllowLineItemWhenNoDetailsMatchCriteria() throws Exception {
        LineItemGlobalSearchFilter filter = new LineItemGlobalSearchFilter("unmatchedCriteria");

        LineItemModel model1 = seedModel("SITE 1", "Prod 1", null, PricingStatus.FIRM);
        LineItemModel model2 = seedModel("SITE 2", "Prod 2", null, PricingStatus.NOT_PRICED);

        assertThat(filter.apply(model1), is(false));
        assertThat(filter.apply(model2), is(false));
    }

    @Test
    public void shouldAllowSearchCriteriaChaining() throws Exception {
        LineItemGlobalSearchFilter filter = new LineItemGlobalSearchFilter("SItE 1 &&FiRm");

        LineItemModel model1 = seedModel("SITE 1", "Prod 1", null, PricingStatus.FIRM);
        LineItemModel model2 = seedModel("SITE 2", "Prod 2", null, PricingStatus.NOT_PRICED);
        LineItemModel model3 = seedModel("SITE 1", "Prod 3", null, PricingStatus.FIRM);

        assertThat(filter.apply(model1), is(true));
        assertThat(filter.apply(model2), is(false));
        assertThat(filter.apply(model3), is(true));
    }

    private LineItemModel seedModel(String siteName, String productName, String summary, PricingStatus pricingStatus) {
        LineItemModel model = mock(LineItemModel.class);
        when(model.getSite()).thenReturn(SiteDTOFixture.aSiteDTO().withName(siteName).build());
        when(model.getProductName()).thenReturn(productName);
        when(model.getSummary()).thenReturn(summary);
        when(model.getPricingStatusOfTree()).thenReturn(pricingStatus);
        return model;
    }
}
