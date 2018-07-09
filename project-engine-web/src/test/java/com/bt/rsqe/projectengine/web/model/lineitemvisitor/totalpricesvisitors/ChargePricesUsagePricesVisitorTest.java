package com.bt.rsqe.projectengine.web.model.lineitemvisitor.totalpricesvisitors;

import com.bt.rsqe.Money;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.model.ProjectedUsageModel;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.Is.*;
import static org.mockito.Mockito.*;

public class ChargePricesUsagePricesVisitorTest {

    private UsagePriceVisitor usagePricesVisitor;
    private LineItemModel lineItem;
    private ProjectedUsageModel offNetProjectedUsage;
    private ProjectedUsageModel onNetProjectedUsage;

    @Before
    public void setUp() throws Exception {
        usagePricesVisitor = new ChargePricesUsagePricesVisitor();
        lineItem = mock(LineItemModel.class);
        onNetProjectedUsage = mock(ProjectedUsageModel.class);
        offNetProjectedUsage = mock(ProjectedUsageModel.class);
    }

    @Test
    public void shouldProperlyCalculateTotalUsagePricesForTheEUPCategory() throws Exception {
        when(onNetProjectedUsage.getChargeOnNetChargePerMonth()).thenReturn(Money.from("1.00"));
        when(onNetProjectedUsage.getChargeOffNetChargePerMonth()).thenReturn(Money.ZERO);
        when(offNetProjectedUsage.getChargeOffNetChargePerMonth()).thenReturn(Money.from("3.00"));
        when(offNetProjectedUsage.getChargeOnNetChargePerMonth()).thenReturn(Money.ZERO);

        when(lineItem.getContractTerm()).thenReturn("2");

        usagePricesVisitor.visit(lineItem);
        usagePricesVisitor.visit(offNetProjectedUsage);
        usagePricesVisitor.visit(onNetProjectedUsage);

        assertThat(usagePricesVisitor.getTotalUsageCharge(), is(Money.from("8")));
    }

}
