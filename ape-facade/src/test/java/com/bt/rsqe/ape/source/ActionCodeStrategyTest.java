package com.bt.rsqe.ape.source;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ActionCodeStrategyTest {
    @Test
    public void shouldReturnLegFlagsForActionCodeAddCorrectly() {
          assertThat(ActionCodeStrategy.Add.requiresAsIs(), is(false));
          assertThat(ActionCodeStrategy.Add.requiresToBe(), is(true));
    }

    @Test
    public void shouldReturnLegFlagsForActionCodeUpdateCorrectly() {
        assertThat(ActionCodeStrategy.Update.requiresAsIs(), is(true));
        assertThat(ActionCodeStrategy.Update.requiresToBe(), is(true));
    }

    @Test
    public void shouldReturnLegFlagsForActionCodeNoneCorrectly() {
        assertThat(ActionCodeStrategy.None.requiresAsIs(), is(true));
        assertThat(ActionCodeStrategy.None.requiresToBe(), is(false));
    }
}
