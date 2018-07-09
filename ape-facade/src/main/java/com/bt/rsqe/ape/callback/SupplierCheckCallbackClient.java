package com.bt.rsqe.ape.callback;

import com.bt.rsqe.web.rest.ProxyAwareRestRequestBuilder;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
///CLOVER:OFF
public class SupplierCheckCallbackClient {
    public void sendSupplierCheckUpdates(final String supplierCheckJson, final String callbackUri, final long waitTime) {
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                new ProxyAwareRestRequestBuilder(UriBuilder.fromUri(callbackUri).build(), MediaType.APPLICATION_JSON_TYPE).build().post(supplierCheckJson).getEntity(String.class);
            }
        }.start();
    }
}
///CLOVER:ON