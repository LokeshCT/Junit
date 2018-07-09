package com.bt.rsqe.inlife.repository;

import com.bt.rsqe.inlife.entities.UserEntity;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.bt.rsqe.persistence.PersistenceManager;

import javax.persistence.NoResultException;
import java.util.List;

public class UserJPARepository implements UserRepository
{
    private static final Logger LOG = LogFactory.createDefaultLogger(Logger.class);

    private PersistenceManager persistenceManager;

    public UserJPARepository (PersistenceManager persistenceManager)
    {
        this.persistenceManager = persistenceManager;
    }

    @Override
    public int save(UserEntity entity)
    {
        return persistenceManager.entityManager().merge(entity).getId();
    }

    @Override
    public void persist(UserEntity entity)
    {
        persistenceManager.entityManager().persist(entity);
    }

    @Override
    public List<UserEntity> getUserEntity ()
    {
        List<UserEntity> results = persistenceManager.getAll(UserEntity.class);
        LOG.resultSet(results.toString());
        return results;
    }

    @Override
    public UserEntity getUserEntityByName(String value)
    {
        try
        {
            UserEntity result = persistenceManager.first(UserEntity.class, "select distinct entity from UserEntity entity where user_identifier = ?0", value);
            LOG.resultSet(result.toString());
            return result;
        }
        catch (NoResultException e)
        {
            LOG.noResultException(value, e.getCause());
            return null;
        }
    }

    private interface Logger
    {
        @Log(level = LogLevel.ERROR, format = "Error : %s")
        void error(Exception e);

        @Log(level = LogLevel.DEBUG, format = "Result returned: %s")
        void resultSet(String s);

        @Log(level = LogLevel.DEBUG, format = "User=%s, dose not exist in table. cause=%s")
        void noResultException(String value, Throwable cause);
    }
}
