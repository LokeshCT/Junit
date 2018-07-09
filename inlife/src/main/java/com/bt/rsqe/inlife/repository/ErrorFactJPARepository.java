package com.bt.rsqe.inlife.repository;

import com.bt.rsqe.inlife.entities.ErrorFactEntity;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.bt.rsqe.persistence.PersistenceManager;
import com.bt.rsqe.util.SqlStringBuilder;

import java.util.List;

public class ErrorFactJPARepository implements ErrorFactRepository
{
    private static final Logger LOG = LogFactory.createDefaultLogger(Logger.class);

    private PersistenceManager persistenceManager;
    private static final String GET_ERROR_FACT_ENTITY = "SELECT COUNT(efe) FROM ErrorFactEntity efe WHERE efe.quoteLineItemId = :quoteLineItemId";

    public ErrorFactJPARepository (PersistenceManager persistenceManager)
    {
        this.persistenceManager = persistenceManager;
    }

    @Override
    public int save(ErrorFactEntity entity)
    {
        return persistenceManager.entityManager().merge(entity).getId();
    }

    @Override
    public ErrorFactEntity getErrorFactEntityById(int id)
    {
        return persistenceManager.get(ErrorFactEntity.class, id);
    }

    @Override
    public long getErrorFactEntityByOptionLineItemId(String id)
    {
        return persistenceManager.entityManager().createQuery(GET_ERROR_FACT_ENTITY, Long.class).setParameter("quoteLineItemId", id).getSingleResult();
    }

    @Override
    public List<ErrorFactEntity> getErrorFactsWithinTimeFrame(String startTime, String endTime)
    {
        return persistenceManager.entityManager().createQuery(SqlStringBuilder.aQuery().withSelectStatement("SELECT efe FROM ErrorFactEntity efe").filterBetweenDates("efe.timestamp", startTime, endTime).build(), ErrorFactEntity.class).getResultList();
    }

    private interface Logger
    {
        @Log(level = LogLevel.ERROR, format = "%s")
        void error(Exception e);
    }
}
