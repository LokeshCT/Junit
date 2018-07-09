package com.bt.rsqe.projectengine.web;

import org.junit.Test;

import javax.ws.rs.core.Response;

import static com.bt.rsqe.matchers.ResponseMatcher.*;
import static javax.ws.rs.core.Response.Status.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;

public class QuoteViewFocusedResourceHandlerTest {
    private class HappyQuoteViewFocusedResourceHandler extends QuoteViewFocusedResourceHandler {
        public HappyQuoteViewFocusedResourceHandler() { super(null); }

        public Response viewAction() {
            return new HandlerActionAttempt() {
                @Override protected Response action() throws Exception {
                    return responseOk("");
                }
            }.tryToPerformAction("NOT-USED");
        }
    };

    private class UnhappyQuoteViewFocusedResourceHandler extends HappyQuoteViewFocusedResourceHandler {

        @Override public Response viewAction() {
            return new HandlerActionAttempt() {
                @Override protected Response action() throws Exception {
                    throw new Exception("TEST-EXCEPTION-MESSAGE");
                }
            }.tryToPerformAction("TEST-IDENTIFIER");
        }
    }

    private class AjaxKnownResourceHandler extends QuoteViewFocusedResourceHandler {
        private boolean ajaxFileUpload;

        public AjaxKnownResourceHandler(boolean ajaxFileUpload) { super(null);
            this.ajaxFileUpload = ajaxFileUpload;
        }

         public Response viewAction() {
            return new HandlerActionAttempt(ajaxFileUpload) {
                @Override protected Response action() throws Exception {
                    throw new Exception("AJAX ERROR");
                }
            }.tryToPerformAction("AJAX");
        }
    }

    @Test
    public void shouldReturnTheResultOfTheActionWhenNoExceptionIsThrown() throws Exception {
        HappyQuoteViewFocusedResourceHandler happyHandler = new HappyQuoteViewFocusedResourceHandler();
        assertThat(happyHandler.viewAction().getStatus(), is(Response.Status.OK.getStatusCode()));
    }

    @Test
    public void shouldHandleAnExceptionWhenThrown() throws Exception {
        UnhappyQuoteViewFocusedResourceHandler unhappyHandler = new UnhappyQuoteViewFocusedResourceHandler();
        final Response response = unhappyHandler.viewAction();
        assertThat(response, is(aResponse().withStatus(OK)));
        assertTrue(response.getEntity().toString().contains("RSQE Error"));
    }

    @Test
    public void shouldReturnErrorMessageWhenAjaxFileUpload() throws Exception {
        AjaxKnownResourceHandler unhappyHandler = new AjaxKnownResourceHandler(true);
        final Response response = unhappyHandler.viewAction();
        assertThat(response, is(aResponse().withStatusOK().withErrorMessage("AJAX ERROR")));
}

    @Test
    public void shouldReturnErrorPageWhenNotAjaxFileUpload() throws Exception {
        AjaxKnownResourceHandler unhappyHandler = new AjaxKnownResourceHandler(false);
        final Response response = unhappyHandler.viewAction();
        assertThat(response, is(aResponse().withStatus(OK)));
        assertTrue(response.getEntity().toString().contains("RSQE Error"));
    }
}
