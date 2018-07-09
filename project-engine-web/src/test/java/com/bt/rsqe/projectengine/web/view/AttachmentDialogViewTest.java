package com.bt.rsqe.projectengine.web.view;

import com.bt.rsqe.emppal.attachmentresource.AttachmentCategoryEnum;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;

public class AttachmentDialogViewTest {
    private AttachmentDialogView attachmentDialogView;
    private AttachmentDialogView costAttachmentDialogView;
    private String CUSTOMER_ID = "customerId";
    private String CONTRACT_ID = "contractId";
    private String PROJECT_ID = "projectId";
    private String QUOTE_OPTION_ID = "quoteOptionId";
    private String SALES = AttachmentCategoryEnum.Sales.getValue();
    private String SERVICE_DELIVERY = AttachmentCategoryEnum.ServiceDelivery.getValue();
    private String BID_MANAGER = AttachmentCategoryEnum.BidManager.getValue();

    @Before
    public void setup() {
        attachmentDialogView = new AttachmentDialogView(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, false);
        costAttachmentDialogView = new AttachmentDialogView(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, true);
    }

    @Test
    public void shouldGetLoadAttachmentUri() {
        String expectedLoadAttachmentUri = "/rsqe/customers/customerId/contracts/contractId/projects/projectId/quote-options/quoteOptionId/attachments/load-attachment";
        String actualLoadAttachmentUri = attachmentDialogView.getLoadAttachmentUri();
        assertThat(actualLoadAttachmentUri, is(expectedLoadAttachmentUri));
    }

    @Test
    public void shouldGetDeleteAttachmentUri() {
        String expectedDeleteAttachmentUri = "/rsqe/customers/customerId/contracts/contractId/projects/projectId/quote-options/quoteOptionId/attachments/delete-attachment";
        String actualDeleteAttachmentUri = attachmentDialogView.getDeleteAttachmentUri();
        assertThat(actualDeleteAttachmentUri, is(expectedDeleteAttachmentUri));
    }

    @Test
    public void shouldGetDownloadAttachmentUri() {
        String expectedDownloadAttachmentUri = "/rsqe/customers/customerId/contracts/contractId/projects/projectId/quote-options/quoteOptionId/attachments/download-attachment";
        String actualDownloadAttachmentUri = attachmentDialogView.getDownloadAttachmentUri();
        assertThat(actualDownloadAttachmentUri, is(expectedDownloadAttachmentUri));
    }

    @Test
    public void shouldGetUploadAttachmentUri() {
        String expectedUploadAttachmentUri = "/rsqe/customers/customerId/contracts/contractId/projects/projectId/quote-options/quoteOptionId/attachments/upload-attachment";
        String actualUploadAttachmentUri = attachmentDialogView.getUploadAttachmentUri();
        assertThat(actualUploadAttachmentUri, is(expectedUploadAttachmentUri));
    }

    @Test
    public void shouldGetTierListForAttachmentDialogView() {
        List<String> expectedTierList = newArrayList(SALES, SERVICE_DELIVERY);
        List<String> actualTierList = attachmentDialogView.getTierList();
        assertThat(actualTierList.size(), is(2));
        assertThat(actualTierList.get(0), is(expectedTierList.get(0)));
        assertThat(actualTierList.get(1), is(expectedTierList.get(1)));
    }

    @Test
    public void shouldGetTierListForCostAttachmentDialogView() {
        List<String> expectedTierList = newArrayList(BID_MANAGER);
        List<String> actualTierList = costAttachmentDialogView.getTierList();
        assertThat(actualTierList.size(), is(1));
        assertThat(actualTierList.get(0), is(expectedTierList.get(0)));
    }
}
