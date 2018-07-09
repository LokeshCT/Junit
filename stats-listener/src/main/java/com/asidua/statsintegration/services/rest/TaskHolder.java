package com.asidua.statsintegration.services.rest;

import java.util.concurrent.Future;

public class TaskHolder {

    private Future future;
    private TestProgressListener listener;


    public TaskHolder(TestProgressListener listener) {
        this.listener = listener;
    }

    Future getFuture() {
        return future;
    }

    private void setFuture(Future future) {
        this.future = future;
    }

    TestProgressListener getListener() {
        return listener;
    }

    private void setListener(TestProgressListener listener) {
        this.listener = listener;
    }
}
