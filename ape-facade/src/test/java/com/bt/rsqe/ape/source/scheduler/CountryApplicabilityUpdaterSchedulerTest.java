package com.bt.rsqe.ape.source.scheduler;

import com.bt.rsqe.ape.config.ApeServiceEndPointConfig;
import com.bt.rsqe.ape.repository.APEQrefJPARepository;
import com.bt.rsqe.ape.source.processor.RequestBuilder;
import com.bt.rsqe.persistence.JPAEntityManagerProvider;
import com.bt.rsqe.persistence.JPAPersistenceManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleTrigger;
import org.quartz.impl.StdSchedulerFactory;

import javax.persistence.EntityManager;
import java.util.Date;

import static org.mockito.Mockito.*;

public class CountryApplicabilityUpdaterSchedulerTest {

    SchedulerFactory sf;
    Scheduler scheduler;
    APEQrefJPARepository repository;
    RequestBuilder requestProcessor;
    ApeServiceEndPointConfig endpoint;
    JPAPersistenceManager persistenceManager;
    JPAEntityManagerProvider provider;


    @Before
    public void setup() throws SchedulerException {

        persistenceManager = mock(JPAPersistenceManager.class);
        repository = new APEQrefJPARepository(persistenceManager);
        EntityManager entityManager = provider.entityManager();
        persistenceManager.bind(entityManager);
        persistenceManager.start();
        sf = new StdSchedulerFactory();
        scheduler = sf.getScheduler();
    }

    @Test
    public void shouldBeAbleToGetCountryApplicability() throws SchedulerException {
        SchedulerFactory sf = new StdSchedulerFactory();
        Scheduler scheduler = sf.getScheduler();
        scheduler.start();
        JobDetail jobDetail = new JobDetail("CountryApplicabilityUpdaterJob", scheduler.DEFAULT_GROUP, CountryApplicabilityUpdater.class);
        SimpleTrigger simpleTrigger = new SimpleTrigger("CountryApplicabilityUpdaterTrigger", scheduler.DEFAULT_GROUP, new Date(),
                new Date(), SimpleTrigger.REPEAT_INDEFINITELY, 5000L);
        scheduler.scheduleJob(jobDetail, simpleTrigger);
    }

    @After
    public void tearDown() throws Exception {
        scheduler.shutdown();

    }
}
