package com.bt.rsqe;

import com.bt.rsqe.dataarchiving.DataArchivingConfig;
import com.bt.rsqe.inlife.config.InlifeConfig;
import com.bt.rsqe.taskscheduler.TaskSchedulerConfig;

public interface InlifeEnvironmentTestConfig {
     InlifeConfig getInlifeConfig();
    DataArchivingConfig getDataArchivingConfig();
    TaskSchedulerConfig getTaskSchedulerConfig();
}
