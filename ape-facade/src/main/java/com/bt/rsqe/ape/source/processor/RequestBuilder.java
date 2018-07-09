package com.bt.rsqe.ape.source.processor;

import com.bt.rsqe.ape.dto.SupplierCheckRequest;
import com.bt.rsqe.web.Presenter;
import com.bt.rsqe.web.View;
import com.bt.rsqe.web.ViewFocusedResourceHandler;

import java.io.IOException;

/**
 * Created by 605783162 on 09/08/2015.
 */
public class RequestBuilder extends ViewFocusedResourceHandler {

    public RequestBuilder() {
        super(new Presenter());
    }

    public String build(SupplierCheckRequest request, String requestTemplate) throws IOException {
        final View view = view(requestTemplate);
        view.withContext("request", request);
        return presenter.render(view);
    }

}
