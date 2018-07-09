package com.bt.rsqe.projectengine.web.quoteoption;

import org.junit.Test;

import static com.bt.rsqe.enums.PriceType.*;
import static com.bt.rsqe.projectengine.web.model.OneVoicePriceTariff.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.Is.*;
import static org.junit.matchers.IsCollectionContaining.*;

public class OneVoicePriceTariffTest {
    @Test
    public void shouldRetrieveNonChannelBasedPpsrIds() throws Exception {
        assertThat(NON_CHANNEL_BASED_PPSR_IDS, hasItems(VPN_CONFIG,
                                                        VPN_SUBSCRIPTION,
                                                        DIAL_PLAN_CHANGE_CONFIG,
                                                        MAJOR_MOVE_ADDS_OR_CHANGE_CONFIG,
                                                        AMENDMENT_CHARGE,
                                                        CANCELLATION_CHARGE));
    }

    @Test
    public void shouldRetrieveChannelBasedPpsrIds() throws Exception {
        assertThat(CHANNEL_BASED_PPSR_IDS, hasItems(GLOBAL_DIRECT_SINGLE_CHANNEL_CONFIG,
                                                    GLOBAL_DIRECT_SINGLE_CHANNEL_SUBSCRIPTION,
                                                    GLOBAL_INCLUSIVE_SINGLE_CHANNEL_CONFIG,
                                                    GLOBAL_INCLUSIVE_SINGLE_CHANNEL_SUBSCRIPTION,
                                                    GLOBAL_DIRECT_LITE_CHANNEL_CONFIG,
                                                    GLOBAL_DIRECT_LITE_CHANNEL_SUBSCRIPTION));
    }

    @Test
    public void shouldRetrieveExpectedPpsrIdValue() throws Exception {
        assertThat(VPN_CONFIG.ppsrId(), is(13449L));
        assertThat(VPN_SUBSCRIPTION.ppsrId(), is(13450L));
        assertThat(DIAL_PLAN_CHANGE_CONFIG.ppsrId(), is(13451L));
        assertThat(MAJOR_MOVE_ADDS_OR_CHANGE_CONFIG.ppsrId(), is(13452L));
        assertThat(AMENDMENT_CHARGE.ppsrId(), is(13453L));
        assertThat(CANCELLATION_CHARGE.ppsrId(), is(13454L));

        assertThat(GLOBAL_DIRECT_SINGLE_CHANNEL_CONFIG.ppsrId(), is(13445L));
        assertThat(GLOBAL_DIRECT_SINGLE_CHANNEL_SUBSCRIPTION.ppsrId(), is(13446L));
        assertThat(GLOBAL_INCLUSIVE_SINGLE_CHANNEL_CONFIG.ppsrId(), is(13447L));
        assertThat(GLOBAL_INCLUSIVE_SINGLE_CHANNEL_SUBSCRIPTION.ppsrId(), is(13448L));
        assertThat(GLOBAL_DIRECT_LITE_CHANNEL_CONFIG.ppsrId(), is(13442L));
        assertThat(GLOBAL_DIRECT_LITE_CHANNEL_SUBSCRIPTION.ppsrId(), is(13443L));
    }

    @Test
    public void shouldRetrieveExpectedPriceType() throws Exception {
        assertThat(VPN_CONFIG.priceType(), is(ONE_TIME));
        assertThat(VPN_SUBSCRIPTION.priceType(), is(RECURRING));
        assertThat(DIAL_PLAN_CHANGE_CONFIG.priceType(), is(ONE_TIME));
        assertThat(MAJOR_MOVE_ADDS_OR_CHANGE_CONFIG.priceType(), is(ONE_TIME));
        assertThat(AMENDMENT_CHARGE.priceType(), is(ONE_TIME));
        assertThat(CANCELLATION_CHARGE.priceType(), is(ONE_TIME));

        assertThat(GLOBAL_DIRECT_SINGLE_CHANNEL_CONFIG.priceType(), is(ONE_TIME));
        assertThat(GLOBAL_DIRECT_SINGLE_CHANNEL_SUBSCRIPTION.priceType(), is(RECURRING));
        assertThat(GLOBAL_INCLUSIVE_SINGLE_CHANNEL_CONFIG.priceType(), is(ONE_TIME));
        assertThat(GLOBAL_INCLUSIVE_SINGLE_CHANNEL_SUBSCRIPTION.priceType(), is(RECURRING));
        assertThat(GLOBAL_DIRECT_LITE_CHANNEL_CONFIG.priceType(), is(ONE_TIME));
        assertThat(GLOBAL_DIRECT_LITE_CHANNEL_SUBSCRIPTION.priceType(), is(RECURRING));
    }
}
