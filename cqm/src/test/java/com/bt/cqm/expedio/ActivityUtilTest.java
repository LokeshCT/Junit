package com.bt.cqm.expedio;

import com.bt.rsqe.customerinventory.utils.Constants;
import com.bt.rsqe.expedio.services.ActivityDTO;
import com.bt.rsqe.expedio.services.GetActivityRequestDTO;
import javax.ws.rs.core.Form;
import org.hamcrest.core.Is;
import org.junit.Test;

import static com.bt.cqm.expedio.ActivityUtil.*;
import static com.google.common.collect.Lists.*;
import static org.apache.commons.lang.StringUtils.*;
import static org.junit.Assert.*;

public class ActivityUtilTest {


    @Test
    public void shouldBuildGetActivityListRequestDTO() {

        GetActivityRequestDTO.Builder getActivityListRequest = GetActivityRequestDTO.Builder.get()
                                                                                            .withActivityDescription("activity description")
                                                                                            .withActivityID("activityId")
                                                                                            .withBidManagerName("bid manager name")
                                                                                            .withFilterName("filter name")
                                                                                            .withProductName("product name")
                                                                                            .withSalesChannel("sales channel name")
                                                                                            .withSalesChannelType("sales channel type")
                                                                                            .withStatus("0");
        Form form = new Form();
        form.asMap().put("activityDesc[value]", newArrayList("activity description"));
        form.asMap().put("activityID", newArrayList("activityId"));
        form.asMap().put("bidManagerName", newArrayList("bid manager name"));
        form.asMap().put("filter[value]", newArrayList("filter name"));
        form.asMap().put("productName[value]", newArrayList("product name"));
        form.asMap().put("salesChannel", newArrayList("sales channel name"));
        form.asMap().put("salesChannelType[value]", newArrayList("sales channel type"));
        form.asMap().put("status[value]", newArrayList("Open"));

        GetActivityRequestDTO actual = ActivityUtil.createActivityListRequestDTO(form);
        assertThat(actual, Is.is(getActivityListRequest.build()));

        form.asMap().remove("status[value]");
        actual = ActivityUtil.createActivityListRequestDTO(form);
        assertThat("Null status should get converted to value '0'", actual, Is.is(getActivityListRequest.build()));

        form.asMap().put("status[value]", newArrayList("Closed"));
        actual = ActivityUtil.createActivityListRequestDTO(form);
        assertThat("Other than 'Open' status should get converted to value '1'", actual, Is.is(getActivityListRequest.withStatus("1").build()));
    }


    @Test
    public void shouldCreateActivityUpdateDTO() {
        ActivityDTO expected = ActivityDTO.Builder.get()
                                                     .withActivityID("activity Id")
                                                     .withApproverReason("reason")
                                                     .withQuoteRefID("")
                                                     .withQuoteVersion("QuoteVersion")
                                                     .withSourceSystem("SourceSystem")
                                                     .withSubStatus("SubStatus")
                                                     .build();

        Form form = new Form();
        form.asMap().put("ActivityID", newArrayList("activity Id"));
        form.asMap().put("BidManagersComments", newArrayList("reason"));
        form.asMap().put("QuoteRefID", newArrayList("Nil"));
        form.asMap().put("QuoteVersion", newArrayList("QuoteVersion"));
        form.asMap().put("SourceSystem", newArrayList("SourceSystem"));
        form.asMap().put("SubStatus", newArrayList("SubStatus"));

        ActivityDTO actual = ActivityUtil.createActivityUpdateDTO(form);
        assertThat(actual, Is.is(expected));
    }

    @Test
    public void shouldBuildCreateActivityDTO() {
        ActivityDTO expected = ActivityDTO.Builder.get()
                                                  .withActivityDescription("activity description")
                                                  .withAssignedtoEmailid("test@bt.com")
                                                  .withCreatorReason("approved")
                                                  .withExpedioReference(EMPTY)
                                                  .withOrderType(EMPTY)
                                                  .withQuoteRefID(NIL)
                                                  .withQuoteVersion(NIL)
                                                  .withSubStatus(EMPTY)
                                                  .withSourceSystem(Constants.RSQE_SYSTEM)
                                                  .withGroupEmailID(EMPTY)
                                                  .withCreatedbyEmailid(EMPTY)
                                                  .withRole(EMPTY)
                                                  .withProductName(EMPTY)
                                                  .withSalesChannel(EMPTY)
                                                  .withBidManagerName(EMPTY)
                                                  .withStatus(EMPTY)
                                                  .withActivityType(EMPTY)
                                                  .withCreator(EMPTY)
                                                  .withBfgCustomerId(EMPTY)
                                                  .withCustomerName("customer name")
                                                  .build();

        Form form = new Form();
        form.asMap().put("activityDesc[value]", newArrayList("activity description"));
        form.asMap().put("assignedTo[emailAddress]", newArrayList("test@bt.com"));
        form.asMap().put("salesUserComments", newArrayList("approved"));
        form.asMap().put("customerName", newArrayList("customer name"));

        ActivityDTO actual = ActivityUtil.createCreateActivityDTO(form);

        assertThat(actual.getCustomerName(), Is.is(expected.getCustomerName()));
        assertThat(actual.getActivityDescription(), Is.is(expected.getActivityDescription()));
        assertThat(actual.getSalesUsersComments(), Is.is(expected.getSalesUsersComments()));
        assertThat(actual.getAssignedtoEmailid(), Is.is(expected.getAssignedtoEmailid()));
    }
}
