package com.bt.rsqe.ape.source.scheduler;

import com.bt.rsqe.ape.config.SchedulerConfig;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static com.bt.rsqe.ape.config.RequestCompletionSchedulerConfig.*;
import static java.util.concurrent.TimeUnit.*;

/**
 * Created by 605783162 on 13/08/2015.
 */
public class RequestCompletionNotifierScheduler {
    public static final String TRUE = "true";
    private static Logger logger = LogFactory.createDefaultLogger(Logger.class);
    private boolean isEnabled;
    private int delayInMin;

    public RequestCompletionNotifierScheduler(SchedulerConfig config, RequestCompletionNotifier notifier) {
        try {
            this.isEnabled = TRUE.equalsIgnoreCase(config.getRequestCompletionConfig(ENABLE).getEnable());
            this.delayInMin = Integer.parseInt(config.getRequestCompletionConfig(DELAY_IN_MIN).getDelayInMin());

            if (isEnabled) {
                logger.startedScheduler(String.valueOf(delayInMin));
                ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
                scheduledExecutorService.scheduleAtFixedRate(notifier,
                        delayInMin,
                        delayInMin,
                        MINUTES);
                logger.scheduled();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private interface Logger {
        @Log(level = LogLevel.INFO, format = "Scheduling RequestCompletionNotifierScheduler, which will be triggered after every '%s' min")
        void startedScheduler(String delay);

        @Log(level = LogLevel.INFO, format = "RequestCompletionNotifierScheduler scheduled..")
        void scheduled();

        @Log(level = LogLevel.INFO, format = "Error while scheduling RequestCompletionNotifierScheduler '%s'")
        void error(Exception e);
    }
}
