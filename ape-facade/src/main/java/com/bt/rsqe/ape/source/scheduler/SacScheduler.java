package com.bt.rsqe.ape.source.scheduler;

import com.bt.cqm.client.SACAvailabilityCheckerClient;
import com.bt.rsqe.ape.SupplierProductResourceClient;
import com.bt.rsqe.ape.config.SACRequestReSubmitConfig;
import com.bt.rsqe.ape.config.SchedulerConfig;
import com.bt.rsqe.ape.repository.APEQrefJPARepository;
import com.bt.rsqe.persistence.JPAEntityManagerProvider;
import com.bt.rsqe.utils.AssertObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: 608026723
 * Date: 15/10/15
 * Time: 13:02
 * To change this template use File | Settings | File Templates.
 */
public class SacScheduler {
    private static SACAvailabilityCheckerClient sacAvailabilityCheckerClient;
    private static SupplierProductResourceClient supplierProductResourceClient;
    //private static ScheduledExecutorService scheduledExecutorService;
    private static JPAEntityManagerProvider provider;
    private static Logger LOG = LoggerFactory.getLogger(SacScheduler.class);
    private static Integer reSubmitInterval;
    private static boolean isEnabled;

    public SacScheduler(SACAvailabilityCheckerClient sacAvailabilityCheckerClient, SupplierProductResourceClient supplierProductResourceClient,JPAEntityManagerProvider provider,SchedulerConfig schedConfig) {
        this.sacAvailabilityCheckerClient = sacAvailabilityCheckerClient;
        this.supplierProductResourceClient = supplierProductResourceClient;
        this.provider = provider;
        this.isEnabled = Boolean.parseBoolean(schedConfig.getSACRequestReSubmitConfig(SACRequestReSubmitConfig.RESUBMIT_INTERVAL).getEnable());
        if(isEnabled){
            this.reSubmitInterval = Integer.parseInt(schedConfig.getSACRequestReSubmitConfig(SACRequestReSubmitConfig.RESUBMIT_INTERVAL).getInterval());
            //scheduledExecutorService = Executors.newScheduledThreadPool(10);
        }
    }

    public static void trackApeResponse(String fileName,Integer scheduleTime,TimeUnit timeUnit){
        if(!AssertObject.isEmpty(fileName) && isEnabled){
            TimeUnit schdTimeUnit = TimeUnit.HOURS;
            int iterationCountIn24Hrs = 24/reSubmitInterval;
            int nextFireInterval = reSubmitInterval;
            if(scheduleTime!=null){
                nextFireInterval = scheduleTime;
            }
            if(timeUnit!=null){
                schdTimeUnit = timeUnit;
            }

            LOG.info(String.format("Going to Schedule SAC APE Response Thread. Next Fire Time after %s %s. File Name :%s",nextFireInterval,schdTimeUnit,fileName));
            Executors.newSingleThreadScheduledExecutor().schedule(new SacApeResponseCheckTask(fileName,iterationCountIn24Hrs,reSubmitInterval,sacAvailabilityCheckerClient,supplierProductResourceClient,provider),nextFireInterval,schdTimeUnit);
            LOG.info("Scheduled SAC APE Response Thread.");
        }
    }

    public static void trackApeResponse(String fileName){
        trackApeResponse(fileName,null,null);
    }

}
