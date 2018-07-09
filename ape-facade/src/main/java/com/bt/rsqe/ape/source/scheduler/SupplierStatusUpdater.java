package com.bt.rsqe.ape.source.scheduler;

import com.bt.rsqe.ape.config.SupplierCheckConfig;
import com.bt.rsqe.ape.repository.APEQrefJPARepository;
import com.bt.rsqe.ape.repository.entities.SupplierCheckClientRequestEntity;
import com.bt.rsqe.ape.repository.entities.SupplierRequestSiteEntity;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.bt.rsqe.persistence.JPAEntityManagerProvider;
import com.bt.rsqe.persistence.JPAPersistenceManager;
import com.bt.rsqe.persistence.JPATransactionUnit;
import com.bt.rsqe.persistence.JPATransactionalContext;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.List;

import static com.bt.rsqe.ape.dto.SupplierStatus.*;
import static com.google.common.collect.Lists.*;

/**
 * Created by 605783162 on 14/08/2015.
 */
public class SupplierStatusUpdater implements Runnable {
    private Logger logger = LogFactory.createDefaultLogger(Logger.class);
    SupplierCheckConfig config;
    JPAEntityManagerProvider provider;
    private final JPATransactionalContext persistence;

    public SupplierStatusUpdater(SupplierCheckConfig config, JPAEntityManagerProvider provider) {
        this.config = config;
        this.provider = provider;
        this.persistence = new JPATransactionalContext(provider);
    }

    @Override
    public void run() {
        try {
            updateSupplierProductTimeout();
            updateSupplierSiteAsGreenIfAnyProductIsAvailable();
            updateSupplierSiteStatusFailedIfTimedOut();
            updateSupplierSiteStatusExpired();
            updateStatusAndMarkClientRequestAsCompleted();
        } catch (Exception e) {
            logger.error(e);
        }
    }

    private void updateStatusAndMarkClientRequestAsCompleted() throws Exception {
        List<SupplierCheckClientRequestEntity> clientRequestEntities = getClientRequests();
        List<SupplierCheckClientRequestEntity> checkClientRequestEntityList;
        for (SupplierCheckClientRequestEntity clientRequestEntity : clientRequestEntities) {
            for (SupplierRequestSiteEntity siteEntity : clientRequestEntity.getSupplierRequestSiteEntities()) {
                //get all the supplier product status if its not having null or InPorgress mark siteEntity as completed
                if (isProductAvailableForTheSite(siteEntity.getSiteId())) {
                    siteEntity.setStatus(Completed.value());
                } else {
                    siteEntity.setStatus(InProgress.value());
                }
            }
            //if all the child is completed then mark request status as completed
            if (isSiteStatusCompleted(clientRequestEntity.getSupplierRequestSiteEntities())) {
                clientRequestEntity.setStatus(Completed.value());
            } else {
                clientRequestEntity.setStatus(InProgress.value());
            }
        }
        checkClientRequestEntityList = getClientRequestWhichIsCompleted(clientRequestEntities);
        if (checkClientRequestEntityList.size() > 0) {
            updateClientRequestStatus(checkClientRequestEntityList);
        }
    }

    public List<SupplierCheckClientRequestEntity> getClientRequestWhichIsCompleted(List<SupplierCheckClientRequestEntity> items) {
        return newArrayList(Iterables.filter(items, new Predicate<SupplierCheckClientRequestEntity>() {
            @Override
            public boolean apply(SupplierCheckClientRequestEntity input) {
                return Completed.value().equalsIgnoreCase(input.getStatus());
            }
        }));
    }

    public boolean isSiteStatusCompleted(List<SupplierRequestSiteEntity> siteList) {
        List<String> siteStatuses = getRequestedSiteStatus(siteList);
        return (siteStatuses.contains(null) || siteStatuses.contains(InProgress.value())) ? false : true;
    }

    public static List<String> getRequestedSiteStatus(List<SupplierRequestSiteEntity> siteList) {
        return Lists.transform(siteList, new Function<SupplierRequestSiteEntity, String>() {
            @Override
            public String apply(@Nullable SupplierRequestSiteEntity input) {
                return input.getStatus();
            }
        });
    }

    public boolean isProductAvailableForTheSite(String siteId) throws Exception {
        List<String> result = newArrayList();
        EntityManager entityManager = null;
        try {
            entityManager = provider.entityManager();
            result = entityManager.createNativeQuery("select distinct status from supplier_product where site_id = " + siteId).getResultList();
        } catch (NoResultException noResultException) {
            return false;
        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }
        return (result.contains(null) || result.contains(InProgress.value())) ? false : true;
    }

    public boolean updateSupplierSiteStatusExpired() {
        try {
            persistence.execute(new JPATransactionUnit() {
                @Override
                public void execute(JPAPersistenceManager connection) {
                    APEQrefJPARepository repository = new APEQrefJPARepository(connection);
                    try {
                        repository.updateSupplierSiteStatusExpired();
                    } catch (Exception e) {
                        logger.error(e);
                    }
                }
            });
            return true;
        } catch (Exception ex) {
            logger.error(ex);
            return false;
        }
    }

    public boolean updateSupplierSiteStatusFailedIfTimedOut() {
        try {
            persistence.execute(new JPATransactionUnit() {
                @Override
                public void execute(JPAPersistenceManager connection) {
                    APEQrefJPARepository repository = new APEQrefJPARepository(connection);
                    try {
                        repository.updateSupplierSiteStatusFailedIfTimedOut();
                    } catch (Exception e) {
                        logger.error(e);
                    }
                }
            });
            return true;
        } catch (Exception ex) {
            logger.error(ex);
            return false;
        }
    }

    public boolean updateSupplierSiteAsGreenIfAnyProductIsAvailable() {
        try {
            persistence.execute(new JPATransactionUnit() {
                @Override
                public void execute(JPAPersistenceManager connection) {
                    APEQrefJPARepository repository = new APEQrefJPARepository(connection);
                    try {
                        repository.updateSupplierSiteAsGreenIfAnyProductIsAvailable();
                    } catch (Exception e) {
                        logger.error(e);
                    }
                }
            });
            return true;
        } catch (Exception ex) {
            logger.error(ex);
            return false;
        }
    }

    public boolean updateSupplierProductTimeout() {
        try {
            persistence.execute(new JPATransactionUnit() {
                @Override
                public void execute(JPAPersistenceManager connection) {
                    APEQrefJPARepository repository = new APEQrefJPARepository(connection);
                    try {
                        repository.updateSupplierProductTimeout();
                    } catch (Exception e) {
                        logger.error(e);
                    }
                }
            });
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean updateClientRequestStatus(final List<SupplierCheckClientRequestEntity> entityList) {
        try {
            persistence.execute(new JPATransactionUnit() {
                @Override
                public void execute(JPAPersistenceManager connection) {
                    APEQrefJPARepository repository = new APEQrefJPARepository(connection);
                    try {
                        repository.saveClientRequestList(entityList);
                    } catch (Exception e) {
                        logger.error(e);
                    }
                }
            });
            return true;
        } catch (Exception ex) {
            logger.error(ex);
            return false;
        }
    }

    public List<SupplierCheckClientRequestEntity> getClientRequests() throws Exception {
        List<SupplierCheckClientRequestEntity> result = newArrayList();
        EntityManager entityManager = null;
        try {

            entityManager = provider.entityManager();
            result = entityManager.createQuery("select entity from SupplierCheckClientRequestEntity entity where status=:status and entity.callbackUri is not null")
                    .setMaxResults(20)
                    .setParameter("status", InProgress.value())
                    .getResultList();
        } catch (NoResultException noResultException) {
            //do nothing
        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }
        return result;
    }

    private interface Logger {
        @Log(level = LogLevel.INFO, format = "Error updating timeout : '%s'")
        void error(Exception e);

    }
}
