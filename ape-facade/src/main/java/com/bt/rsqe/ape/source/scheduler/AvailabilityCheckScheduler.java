package com.bt.rsqe.ape.source.scheduler;

import com.bt.rsqe.ape.config.AvailabilityCheckSchedulerConfig;
import com.bt.rsqe.ape.config.SchedulerConfig;
import com.bt.rsqe.ape.constants.SupplierProductConstants;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.bt.rsqe.ape.config.AvailabilityCheckSchedulerConfig.DELAY_IN_MIN;
import static com.bt.rsqe.ape.config.AvailabilityCheckSchedulerConfig.ENABLE;

/**
 * Created by 605783162 on 14/08/2015.
 */
public class AvailabilityCheckScheduler {
    private static Logger logger = LogFactory.createDefaultLogger(Logger.class);
    private boolean isEnabled = false;
    private int delayInMin;

    public AvailabilityCheckScheduler(SchedulerConfig schedulerConfig, AvailabilityRequestProcessor queueProcessor) {
        try {
            this.isEnabled= SupplierProductConstants.TRUE.equalsIgnoreCase(schedulerConfig.getAvailabilityCheckSchedulerConfig(ENABLE).getEnable());
            this.delayInMin = Integer.parseInt(schedulerConfig.getAvailabilityCheckSchedulerConfig(DELAY_IN_MIN).getDelayInMin());

            if (isEnabled) {
            logger.startedScheduler(String.valueOf(delayInMin));
            ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            scheduledExecutorService.scheduleAtFixedRate(queueProcessor,
                    delayInMin,
                    delayInMin,
                    TimeUnit.MINUTES);
            logger.scheduled();
            }

        } catch (Exception e) {
            logger.error(e);
        }
    }

    private interface Logger {
        @Log(level = LogLevel.INFO, format = "Scheduling supplier status updater service, will be executed after every '%s' min.")
        void startedScheduler(String delay);

        @Log(level = LogLevel.INFO, format = "Supplier supplier status updater service scheduled..")
        void scheduled();

        @Log(level = LogLevel.INFO, format = "Error while scheduling service : '%s'")
        void error(Exception e);
    }
}
