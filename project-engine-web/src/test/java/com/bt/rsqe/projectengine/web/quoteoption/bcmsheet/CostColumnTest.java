package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;

import org.junit.Test;

import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;

public class CostColumnTest {

    @Test
    public void validateAllColumnProperties() {
        assertThat(CostColumn.COST_DESCRIPTION.columnIndex, is(22));
        assertThat(CostColumn.ONE_TIME_COST.columnIndex, is(23));
        assertThat(CostColumn.RECURRING_COST.columnIndex, is(24));

        assertThat(CostColumn.COST_DESCRIPTION.columnName, is("Cost Description"));
        assertThat(CostColumn.ONE_TIME_COST.columnName, is("One Time Cost"));
        assertThat(CostColumn.RECURRING_COST.columnName, is("Recurring Cost"));

        assertThat(CostColumn.COST_DESCRIPTION.retrieveValueFrom, is("PriceDescription"));
        assertThat(CostColumn.ONE_TIME_COST.retrieveValueFrom, is("RecurringPrice"));
        assertThat(CostColumn.RECURRING_COST.retrieveValueFrom, is("NonRecurringPrice"));

        assertThat(CostColumn.COST_DESCRIPTION.visible, is(true));
        assertThat(CostColumn.ONE_TIME_COST.visible, is(true));
        assertThat(CostColumn.RECURRING_COST.visible, is(true));
    }
}
