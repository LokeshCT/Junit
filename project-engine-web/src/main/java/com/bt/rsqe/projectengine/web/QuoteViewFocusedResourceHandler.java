package com.bt.rsqe.projectengine.web;

import com.bt.rsqe.error.web.WebExceptionMapper;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.bt.rsqe.web.Presenter;
import com.bt.rsqe.web.ViewFocusedResourceHandler;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

public abstract class QuoteViewFocusedResourceHandler extends ViewFocusedResourceHandler {


    @Context
    HttpHeaders httpHeaders;

    protected static final QuoteViewFocusedResourceHandlerLog logger = LogFactory.createDefaultLogger(QuoteViewFocusedResourceHandlerLog.class);

    public QuoteViewFocusedResourceHandler(final Presenter presenter) {
        super(presenter);
    }

    public interface QuoteViewFocusedResourceHandlerLog {
        @Log(level = LogLevel.ERROR, format = "An error occurred building a view for Quote Option with id: id=%s")
        void unableToBuildViewError(String quoteId, Exception e);
    }

    /*
     * Wraps common error handling behaviour
     */
    public abstract class HandlerActionAttempt {

        private boolean ajaxFileUpload;

        public HandlerActionAttempt() {
            this(false);
        }

        public HandlerActionAttempt(boolean ajaxFileUpload) {
            this.ajaxFileUpload = ajaxFileUpload;
        }

        public Response tryToPerformAction(String contextIdentifier) {
            try {
                return action();
            } catch (Exception e) {
                logger.unableToBuildViewError(contextIdentifier, e);
                return new WebExceptionMapper(httpHeaders, ajaxFileUpload).toResponse(e);
            }
        }

        protected abstract Response action() throws Exception;
    }
}
