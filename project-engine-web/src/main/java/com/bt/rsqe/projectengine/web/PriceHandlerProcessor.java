package com.bt.rsqe.projectengine.web;

import java.util.concurrent.ExecutorService;

/**
 * Created by IntelliJ IDEA.
 * User: 605908589
 * Date: 28/08/14
 * Time: 15:46
 * To change this template use File | Settings | File Templates.
 */
public class PriceHandlerProcessor {

    private final PricingTaskFactory pricingTaskFactory;
    private final ExecutorService executorService;

    public PriceHandlerProcessor(ExecutorService executorService, PricingTaskFactory pricingTaskFactory) {
        this.executorService = executorService;
        this.pricingTaskFactory = pricingTaskFactory;
    }

    public void startPricing(String lineItems, String customerId, String projectId, String quoteOptionId, boolean indirectUser, String userToken) {
        executorService.submit(pricingTaskFactory.getScopePricingTask(lineItems, customerId, projectId, quoteOptionId, indirectUser, userToken));
    }

}
