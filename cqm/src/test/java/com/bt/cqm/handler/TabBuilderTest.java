package com.bt.cqm.handler;

import com.bt.cqm.web.config.UIConfig;
import org.hamcrest.core.Is;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertThat;

public class TabBuilderTest {

    @Test
    public void shouldBuildTabs() {
        List<Tab> tabs = new TabBuilder().build();
        assertThat(tabs.size(), Is.is(7));
        assertTabSequence(tabs);
    }

    private void assertTabSequence(List<Tab> tabs) {
        assertThat(tabs.get(0).getId(), Is.is(UIConfig.CUSTOMER_TAB_ID));
        assertThat(tabs.get(1).getId(), Is.is(UIConfig.SITE_TAB_ID));
        assertThat(tabs.get(2).getId(), Is.is(UIConfig.QUOTE_TAB_ID));
        assertThat(tabs.get(3).getId(), Is.is(UIConfig.ACTIVITY_TAB_ID));
        assertThat(tabs.get(4).getId(), Is.is(UIConfig.ORDER_TAB_ID));
        assertThat(tabs.get(5).getId(), Is.is(UIConfig.AUDIT_TRAIL_TAB_ID));
        assertThat(tabs.get(6).getId(), Is.is(UIConfig.USER_MANAGEMENT_TAB_ID));
    }
}
