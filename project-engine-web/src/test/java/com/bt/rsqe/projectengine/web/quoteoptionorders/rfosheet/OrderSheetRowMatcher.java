package com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet;

import com.bt.rsqe.domain.bom.parameters.OrderFormSignDate;
import com.bt.rsqe.matchers.CompositeMatcher;
import com.bt.rsqe.matchers.DateTimeMatcher;
import com.google.common.base.Predicate;
import org.apache.commons.lang.time.DateUtils;
import org.hamcrest.Matcher;

import javax.annotation.Nullable;
import java.util.Date;

public class OrderSheetRowMatcher extends CompositeMatcher<OrderSheetModel.OrderSheetRow> {
    public static OrderSheetRowMatcher anOrderRow(){
        return new OrderSheetRowMatcher();
    }
    public OrderSheetRowMatcher withId(final String id){
        return expect(new Predicate<OrderSheetModel.OrderSheetRow>() {
            @Override
            public boolean apply(@Nullable OrderSheetModel.OrderSheetRow input) {
                return id.equals(input.lineItemId());
            }
        });
    }
    public OrderSheetRowMatcher withSiteId(final String id){
        return expect(new Predicate<OrderSheetModel.OrderSheetRow>() {
            @Override
            public boolean apply(@Nullable OrderSheetModel.OrderSheetRow input) {
                return id.equals(input.siteId());
            }
        });
    }
    public OrderSheetRowMatcher withSiteName(final String siteName){
        return expect(new Predicate<OrderSheetModel.OrderSheetRow>() {
            @Override
            public boolean apply(@Nullable OrderSheetModel.OrderSheetRow input) {
                return siteName.equals(input.siteName());
            }
        });
    }
    public OrderSheetRowMatcher withProduct(final String name){
        return expect(new Predicate<OrderSheetModel.OrderSheetRow>() {
            @Override
            public boolean apply(@Nullable OrderSheetModel.OrderSheetRow input) {
                return name.equals(input.productName());
            }
        });
    }
    public OrderSheetRowMatcher withBillingId(final Object billingId){
        return expect(new Predicate<OrderSheetModel.OrderSheetRow>() {
            @Override
            public boolean apply(@Nullable OrderSheetModel.OrderSheetRow input) {
                return billingId instanceof Matcher? ((Matcher) billingId).matches(input.billingId()) : billingId.equals(input.billingId());
            }
        });
    }
    public OrderSheetRowMatcher withOrderSignedDate(final DateTimeMatcher matcher){
        return expect(new Predicate<OrderSheetModel.OrderSheetRow>() {
            @Override
            public boolean apply(@Nullable OrderSheetModel.OrderSheetRow input) {
                return matcher.matchesSafely(input.orderSignedDate().getValue());
            }
        });
    }

    public Matcher<? extends OrderSheetModel.OrderSheetRow> withOrderSignedDate(final OrderFormSignDate orderFormSignDate) {
        return expect(new Predicate<OrderSheetModel.OrderSheetRow>() {
            @Override
            public boolean apply(@Nullable OrderSheetModel.OrderSheetRow input) {
                return orderFormSignDate.equals(input.orderSignedDate());
            }
        });
    }

    public OrderSheetRowMatcher withCustomerRequiredDate(final Date matcher) {
        return expect(new Predicate<OrderSheetModel.OrderSheetRow>() {
            @Override
            public boolean apply(OrderSheetModel.OrderSheetRow input) {
                return matcher.toString().equals(input.getCustomerRequiredDate().toString());
            }
        });
    }

    public OrderSheetRowMatcher withInitialBillingStartDate(final Date matcher) {
        return expect(new Predicate<OrderSheetModel.OrderSheetRow>() {
            @Override
            public boolean apply(OrderSheetModel.OrderSheetRow input) {
                return DateUtils.isSameDay(matcher, input.initialBillingStartDate().get());
            }
        });
    }
}
