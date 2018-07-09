package com.bt.rsqe.ape.source.scheduler;

import com.bt.rsqe.ape.config.SchedulerConfig;
import com.bt.rsqe.ape.constants.SupplierProductConstants;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.bt.rsqe.ape.config.SupplierProductAvailabilityRequestRetrySchedulerConfig.*;

/**
 * Created by 605875089 on 21/02/2016.
 */
public class SupplierProductAvailabilityRequestRetryScheduler {
    private static Logger logger = LogFactory.createDefaultLogger(Logger.class);
    private boolean isEnabled = false;
    private int delayInMin;
    private int interval;

    public SupplierProductAvailabilityRequestRetryScheduler(SchedulerConfig schedulerConfig, SupplierProductAvailabilityRequestRetryProcessor requestRetryProcessor) {
        try {
            this.isEnabled = SupplierProductConstants.TRUE.equalsIgnoreCase(schedulerConfig.getAvailabilityRetrySchedulerConfig(ENABLE).getEnable());
            this.delayInMin = Integer.parseInt(schedulerConfig.getAvailabilityRetrySchedulerConfig(DELAY_IN_MIN).getDelayInMin());
            this.interval = Integer.parseInt(schedulerConfig.getAvailabilityRetrySchedulerConfig(INTERVAL).getInterval());

            if (isEnabled) {
                logger.startedScheduler(String.valueOf(delayInMin));
                logger.retryInterval(String.valueOf(interval));

                ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
                scheduledExecutorService.scheduleAtFixedRate(requestRetryProcessor,
                                                             delayInMin,
                                                             interval,
                                                             TimeUnit.MINUTES);
                logger.scheduled();
            }

        } catch (Exception e) {
            logger.error(e);
        }
    }

    private interface Logger {
        @Log(level = LogLevel.INFO, format = "Scheduling supplier product availability retry service started.")
        void startedScheduler(String delay);

        @Log(level = LogLevel.INFO, format = "Scheduling supplier product availability retry service, will be executed after every '%s' min.")
        void retryInterval(String delay);

        @Log(level = LogLevel.INFO, format = "supplier product availability retry service scheduled..")
        void scheduled();

        @Log(level = LogLevel.INFO, format = "Error while scheduling service : '%s'")
        void error(Exception e);
    }
}
