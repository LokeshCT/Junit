package com.bt.rsqe.ape.source.scheduler;

import com.bt.rsqe.ape.config.SchedulerConfig;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static com.bt.rsqe.ape.config.CountryApplicabilityConfig.*;
import static java.util.concurrent.TimeUnit.*;

/**
 * Created by 605783162 on 13/08/2015.
 */
public class CountryApplicabilityUpdaterScheduler {
    private static Logger logger = LogFactory.createDefaultLogger(Logger.class);

    private int delay;
    private int initialDelay;

    public CountryApplicabilityUpdaterScheduler(SchedulerConfig schedulerConfig, CountryApplicabilityUpdater updater) {
        this.delay = Integer.parseInt(schedulerConfig.getCountryApplicabilityConfig(DELAY_IN_HR).getDelayInHr());
        this.initialDelay = Integer.parseInt(schedulerConfig.getCountryApplicabilityConfig(INITIAL_DELAY_IN_HR).getInitialDelayInHr());
        try {
            logger.startedScheduler(String.valueOf(initialDelay));
            ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            scheduledExecutorService.scheduleAtFixedRate(updater,
                    initialDelay,
                    delay,
                    HOURS);
            logger.scheduled();
        } catch (Exception e) {
            logger.error(e);
        }
    }

    private interface Logger {
        @Log(level = LogLevel.INFO, format = "Scheduling CountryApplicabilityUpdaterScheduler which will be executed at '%s' hours")
        void startedScheduler(String triggerTime);

        @Log(level = LogLevel.INFO, format = "CountryApplicabilityUpdaterScheduler scheduled")
        void scheduled();

        @Log(level = LogLevel.INFO, format = "CountryApplicabilityUpdaterScheduler Error :  '%s'")
        void error(Exception e);
    }
}
