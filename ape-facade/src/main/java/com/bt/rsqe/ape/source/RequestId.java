package com.bt.rsqe.ape.source;

import com.bt.rsqe.domain.bom.parameters.IdGenerator;

import static java.lang.String.valueOf;

public class RequestId {
    private String id;

    private RequestId(String id) {
        this.id = id;
    }

    public String value() {
        return id;
    }

    public static RequestId newInstance() {
        return newInstance(valueOf(new IdGenerator().value()));
    }

    public static RequestId newInstance(String id) {
        return new RequestId(id);
    }
}
