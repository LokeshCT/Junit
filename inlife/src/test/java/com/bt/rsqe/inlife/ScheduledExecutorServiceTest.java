package com.bt.rsqe.inlife;

import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ScheduledExecutorServiceTest {
    static int exeCount = 1;
    private final Integer lock = new Integer(0);

    @Test
    public void should() throws InterruptedException, ExecutionException {

        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

        Runnable task = new Runnable(){
            @Override
            public void run() {
                synchronized (lock) {
                    System.out.println(String.format("%s - Execution %s - started", System.currentTimeMillis(),exeCount));
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println(String.format("%s - Execution %s - completed", System.currentTimeMillis(), exeCount++));
                }
            }
        };
        ScheduledFuture<?> scheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(task, 1, 4, TimeUnit.SECONDS);

        Thread.sleep(15000);
        System.out.println(String.format("%s - Stopping", System.currentTimeMillis()));
        synchronized (lock) {
            scheduledExecutorService.shutdownNow();
        }
        System.out.println(String.format("%s - Stopped", System.currentTimeMillis()));
    }
}
