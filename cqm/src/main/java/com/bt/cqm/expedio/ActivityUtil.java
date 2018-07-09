package com.bt.cqm.expedio;

import com.bt.rsqe.customerinventory.utils.Constants;
import com.bt.rsqe.expedio.services.GetActivityRequestDTO;

import javax.ws.rs.core.Form;

import static com.google.common.base.Strings.*;
import static org.apache.commons.lang.StringUtils.*;

/**
 * Utility class for Activity create and view/update WS request and response.
 * User: Ranjit Roykrishna
 * Date: 23/01/14
 * Time: 15:13
 * To change this template use File | Settings | File Templates.
 */
public class ActivityUtil {

    static final String NIL = "Nil";

    private ActivityUtil() {

    }

    public static GetActivityRequestDTO createActivityListRequestDTO(Form form) {
        return GetActivityRequestDTO.Builder.get()
                                            .withActivityDescription(form.asMap().getFirst("activityDesc[value]"))
                                            .withActivityID(form.asMap().getFirst("activityID"))
                                            .withBidManagerName(form.asMap().getFirst("bidManagerName"))
                                            .withFilterName(form.asMap().getFirst("filter[value]"))
                                            .withProductName(form.asMap().getFirst("productName[value]"))
                                            .withSalesChannel(form.asMap().getFirst("salesChannel"))
                                            .withSalesChannelType(form.asMap().getFirst("salesChannelType[value]"))
                                            .withStatus(getStatusCode(form.asMap().getFirst("status[value]")))
                                            .build();
    }

    private static String getStatusCode(String statusValue) {
        if (isEmpty(statusValue) || "Open".equalsIgnoreCase(statusValue)) {
            return "0";
        }
        return "1";
    }

    public static com.bt.rsqe.expedio.services.ActivityDTO createCreateActivityDTO(Form form) {

        return com.bt.rsqe.expedio.services.ActivityDTO.Builder.get()
                                                               .withActivityDescription(form.asMap().getFirst("activityDesc[value]"))
                                                               .withAssignedTo(form.asMap().getFirst("assignedTo[fullName]"))
                                                               .withAssignedtoEmailid(form.asMap().getFirst("assignedTo[emailAddress]"))
                                                               .withCreatorReason(form.asMap().getFirst("salesUserComments"))
                                                               .withExpedioReference(nullToEmpty(form.asMap().getFirst("expedioReference")))
                                                               .withOrderType(nullToEmpty(form.asMap().getFirst("orderType")))
                                                               .withQuoteRefID(nullToEmpty(form.asMap().getFirst("quoteRefID")))
                                                               .withQuoteVersion(nullToEmpty(form.asMap().getFirst("quoteVersion")))
                                                               .withSubStatus(nullToEmpty(form.asMap().getFirst("subStatus")))
                                                               .withSourceSystem(Constants.RSQE_SYSTEM)
                                                               .withGroupEmailID(nullToEmpty(form.asMap().getFirst("assignedTo[groupEmailID]")))
                                                               .withCreatedbyEmailid(nullToEmpty(form.asMap().getFirst("createdByEmailId")))
                                                               .withRole(nullToEmpty(form.asMap().getFirst("role")))
                                                               .withProductName(nullToEmpty(form.asMap().getFirst("productName")))
                                                               .withSalesChannel(nullToEmpty(form.asMap().getFirst("salesChannel")))
                                                               .withBidManagerName(nullToEmpty(form.asMap().getFirst("bidManagerName")))
                                                               .withStatus(nullToEmpty(form.asMap().getFirst("status")))
                                                               .withActivityType(nullToEmpty(form.asMap().getFirst("activityType")))
                                                               .withCreator(nullToEmpty(form.asMap().getFirst("createdByName")))
                                                               .withBfgCustomerId(nullToEmpty(form.asMap().getFirst("bfgCustomerId")))
                                                               .withCustomerName(nullToEmpty(form.asMap().getFirst("customerName")))
                                                               .build();

    }

    public static com.bt.rsqe.expedio.services.ActivityDTO createActivityUpdateDTO(Form form) {
        return com.bt.rsqe.expedio.services.ActivityDTO.Builder.get()
                                                               .withActivityID(form.asMap().getFirst("ActivityID"))
                                                               .withApproverReason(form.asMap().getFirst("BidManagersComments"))
                                                               .withQuoteRefID(nilToEmpty(form.asMap().getFirst("QuoteRefID")))
                                                               .withQuoteVersion(nilToEmpty(form.asMap().getFirst("QuoteVersion")))
                                                               .withSourceSystem(nilToEmpty(form.asMap().getFirst("SourceSystem")))
                                                               .withSubStatus(form.asMap().getFirst("SubStatus"))
                                                               .build();
    }

    private static String nilToEmpty(String input) {
        return NIL.equalsIgnoreCase(input) ? EMPTY : input;
    }
}
