package com.bt.rsqe.inlife.repository;

import com.bt.rsqe.inlife.entities.ExceptionPointEntity;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.bt.rsqe.persistence.PersistenceManager;

import javax.persistence.NoResultException;
import java.util.List;

public class ExceptionPointJPARepository implements ExceptionPointRepository
{
    private static final Logger LOG = LogFactory.createDefaultLogger(Logger.class);

    private PersistenceManager persistenceManager;

    private static final String GET_ERROR_FACT_ENTITY = "select entity from ExceptionPointEntity entity where entity.EXCEPTION_POINT = :value";

    public ExceptionPointJPARepository (PersistenceManager persistenceManager)
    {
        this.persistenceManager = persistenceManager;
    }

    @Override
    public int save(ExceptionPointEntity entity)
    {
        //persistenceManager.saveAndFlush(entity);
        //return entity.getId();
        return persistenceManager.entityManager().merge(entity).getId();
    }

    @Override
    public void persist(ExceptionPointEntity entity)
    {
        persistenceManager.entityManager().persist(entity);
    }

    @Override
    public List<ExceptionPointEntity> getExceptionPointEntity()
    {
        return persistenceManager.getAll(ExceptionPointEntity.class);
    }

    @Override
    public ExceptionPointEntity getExceptionPointEntityByName(String value)
    {
        try
        {
            return persistenceManager.first(ExceptionPointEntity.class, "select distinct entity from ExceptionPointEntity entity where exception_point = ?0", value);
        }
        catch (NoResultException e)
        {
            return null;
        }
    }

    private interface Logger
    {
        @Log(level = LogLevel.ERROR, format = "%s")
        void error(Exception e);
    }
}
