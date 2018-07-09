package com.bt.nrm.logging;

import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogLevel;

/**
 * Created with IntelliJ IDEA.
 * User: 607349960
 * Date: 29/05/13
 * Time: 12:40
 * To change this template use File | Settings | File Templates.
 */
public interface ApplicationLog {

    String NRM = "NRM";

    @Log(level = LogLevel.INFO, loggerName = NRM)
    void environment(String environment);

    @Log(level = LogLevel.INFO, loggerName = NRM)
    void serverStarting(String serverName);

    @Log(level = LogLevel.INFO, loggerName = NRM, format = "%s on %s")
    void serverRunning(String serverName, String url);

    @Log(level = LogLevel.INFO, loggerName = NRM)
    void serverStopped(String serverName);

    @Log(level = LogLevel.INFO, loggerName = NRM)
    void pingFailure(String url);

    @Log(level = LogLevel.INFO, loggerName = NRM, format = "%s server started at %s")
    void serverStarted(String serverName, long time);

    @Log(level = LogLevel.ERROR, loggerName = NRM)
    void commandLineCouldNotBeParsed(Throwable t);

    @Log(level = LogLevel.ERROR, loggerName = NRM)
    void serverCouldNotStart(Throwable t);

    @Log(level = LogLevel.ERROR, loggerName = NRM)
    void serverStopFailure(String serverName, Throwable t);

    @Log(level = LogLevel.INFO, loggerName = NRM, format = "The optional component %s is not configured and will not be started")
    void skippingUnconfiguredOptionalComponent(String componentName);

    @Log(level = LogLevel.INFO, loggerName = NRM, format = "A check to see if %s is already running will be made")
    void checkingIfServerAlreadyRunning(String serverName);

    @Log(level = LogLevel.INFO, loggerName = NRM, format = "A check to see if ExpedioSalesGUI is already running will not be made")
    void skippingAlreadyRunningCheck();

    @Log(level = LogLevel.WARN, loggerName = NRM, format = "No proxy configuration for this environment.  Resilience and caching will not be available")
    void proxyUnconfigured();
}
