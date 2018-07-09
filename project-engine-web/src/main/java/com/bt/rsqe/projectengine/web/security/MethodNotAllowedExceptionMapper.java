package com.bt.rsqe.projectengine.web.security;

import com.bt.rsqe.error.RsqeApplicationException;
import com.bt.rsqe.error.web.ErrorResourceHandler;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.bt.rsqe.web.Presenter;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.UUID;

import static javax.ws.rs.core.Response.Status.*;


@Provider
public class MethodNotAllowedExceptionMapper implements ExceptionMapper<WebAuthenticationRequestFilter.MethodNotAllowedException> {

    private static final RsqeErrorLog LOG = LogFactory.createDefaultLogger(RsqeErrorLog.class);
    private static final String SEPARATOR = ":";
    protected Presenter presenter;

    public MethodNotAllowedExceptionMapper() {
        this(new Presenter());
    }

    private MethodNotAllowedExceptionMapper(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Response toResponse(WebAuthenticationRequestFilter.MethodNotAllowedException exception) {
        final String errorId = UUID.randomUUID().toString();
        String errorMessage = getErrorMessage(exception);
        LOG.logError(errorId + SEPARATOR + (errorMessage),
                     exception
        );

        Response response = new ErrorResourceHandler(presenter)
            .showErrorPage("Method Not Allowed", errorId, errorMessage);
        return Response.fromResponse(response).header("content-type", "text/html").status(OK).build();
    }

    private String getErrorMessage(Exception exception) {
        StringBuilder sb = new StringBuilder("Error : ");
        if (exception instanceof RsqeApplicationException) {
            sb.append(((RsqeApplicationException) exception).getUserMessage());
        } else {
            sb.append(exception.getMessage());
        }
        return sb.toString();
    }

    private interface RsqeErrorLog {
        @Log(level = LogLevel.INFO, loggerName = "RsqeWebError", format = "%s")
        void logError(String userMessage, Throwable cause);
    }


}
