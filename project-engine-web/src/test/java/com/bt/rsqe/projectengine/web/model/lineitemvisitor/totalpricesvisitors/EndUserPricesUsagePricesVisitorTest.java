package com.bt.rsqe.projectengine.web.model.lineitemvisitor.totalpricesvisitors;

import com.bt.rsqe.Money;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.model.ProjectedUsageModel;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.Is.*;
import static org.mockito.Mockito.*;

public class EndUserPricesUsagePricesVisitorTest {

    private UsagePriceVisitor usagePricesVisitor;
    private LineItemModel lineItem;
    private ProjectedUsageModel offNetProjectedUsage;
    private ProjectedUsageModel onNetProjectedUsage;

    @Before
    public void setUp() throws Exception {
        usagePricesVisitor = new EndUserPricesUsagePricesVisitor();
        lineItem = mock(LineItemModel.class);
        offNetProjectedUsage = mock(ProjectedUsageModel.class);
        onNetProjectedUsage = mock(ProjectedUsageModel.class);
    }

    @Test
    public void shouldProperlyCalculateTotalUsagePricesForTheEUPCategory() throws Exception {
        when(onNetProjectedUsage.getEUPOnNetChargePerMonth()).thenReturn(Money.from("1.00"));
        when(onNetProjectedUsage.getEUPOffNetChargePerMonth()).thenReturn(Money.ZERO);
        when(offNetProjectedUsage.getEUPOffNetChargePerMonth()).thenReturn(Money.from("3.00"));
        when(offNetProjectedUsage.getEUPOnNetChargePerMonth()).thenReturn(Money.ZERO);

        when(lineItem.getContractTerm()).thenReturn("2");

        usagePricesVisitor.visit(lineItem);
        usagePricesVisitor.visit(onNetProjectedUsage);
        usagePricesVisitor.visit(offNetProjectedUsage);

        assertThat(usagePricesVisitor.getTotalUsageCharge(), is(Money.from("8")));
    }

}
