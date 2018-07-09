package com.bt.rsqe.projectengine.web.uri;

import java.util.Map;

public interface UriFactory {
    String getLineItemCreationUri(String sCode, String customerId, String contractId, String projectId);
    String getConfigurationUri(String sCode, String customerId, String contractId, String projectId, String quoteOptionId, String lineItemId, Map<String, String> parameters);
    String getBulkUploadUri(String sCode);
    String getBulkTemplateUri(String sCode, String customerId, String projectId, String quoteOptionId, String currency);
    String getBulkViewUri(String customerId, String contractId, String projectId, String quoteOptionId);
    String getLocateOnGoogleMapsViewUri(String customerId, String contractId, String projectId, String quoteOptionId);
    String getQuoteLaunchUri(String customerId, String contractId, String projectId);
}
