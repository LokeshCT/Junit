package com.bt.rsqe.projectengine.web.view;

import com.bt.rsqe.emppal.attachmentresource.AttachmentCategoryEnum;
import com.bt.rsqe.projectengine.web.uri.UriFactoryImpl;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class AttachmentDialogView {

    private String customerId;
    private String contractId;
    private String projectId;
    private String quoteOptionId;
    private boolean isCostAttachment;

    public AttachmentDialogView(String customerId, String contractId, String projectId, String quoteOptionId, boolean isCostAttachment) {
        this.customerId = customerId;
        this.contractId = contractId;
        this.projectId = projectId;
        this.quoteOptionId = quoteOptionId;
        this.isCostAttachment = isCostAttachment;
    }

    public String getLoadAttachmentUri() {
        return UriFactoryImpl.loadAttachmentUri(customerId, contractId, projectId, quoteOptionId).toString();
    }

    public String getUploadAttachmentUri() {
        return UriFactoryImpl.uploadAttachmentUri(customerId, contractId, projectId, quoteOptionId).toString();
    }

    public String getDownloadAttachmentUri() {
        return UriFactoryImpl.downloadAttachmentUri(customerId, contractId, projectId, quoteOptionId).toString();
    }

    public String getDeleteAttachmentUri() {
        return UriFactoryImpl.deleteAttachmentUri(customerId, contractId, projectId, quoteOptionId).toString();
    }

    public List<String> getTierList() {
        if (this.isCostAttachment) {
            return newArrayList(AttachmentCategoryEnum.BidManager.getValue());
        }
        return newArrayList(AttachmentCategoryEnum.Sales.getValue(),AttachmentCategoryEnum.ServiceDelivery.getValue());
    }
}
