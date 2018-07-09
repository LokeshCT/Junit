package com.bt.rsqe.cleanordervalidation;

public interface CleanOrderValidationResource<T> {
    static final String BASE_PATH = "/rsqe/expedio/";
    static final String PATH = "clean-order-validation";


    T validateExpedioAccount(int bfgSiteId,
                             int billingAccountId,
                             String salesChannelType,
                             String quoteId) throws CleanOrderValidationException;
}
