package com.bt.rsqe.ape.source.scheduler;

import com.bt.rsqe.ape.config.SupplierCheckConfig;
import com.bt.rsqe.ape.dto.StatusResponse;
import com.bt.rsqe.ape.repository.APEQrefJPARepository;
import com.bt.rsqe.ape.repository.entities.SupplierCheckClientRequestEntity;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.bt.rsqe.persistence.JPAEntityManagerProvider;
import com.bt.rsqe.persistence.JPAPersistenceManager;
import com.bt.rsqe.persistence.JPATransactionUnit;
import com.bt.rsqe.persistence.JPATransactionalContext;
import com.bt.rsqe.web.rest.ProxyAwareRestRequestBuilder;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.util.List;

import static com.bt.rsqe.ape.config.RequestCompletionSchedulerConfig.*;
import static com.bt.rsqe.ape.constants.SupplierProductConstants.*;
import static com.bt.rsqe.ape.dto.SupplierStatus.*;
import static com.google.common.collect.Lists.*;

/**
 * Created by 605783162 on 13/08/2015.
 */
public class RequestCompletionNotifier implements Runnable {
    public static final String TRUE = "true";
    public static final String OK = "OK";
    private final JPATransactionalContext persistence;
    private boolean isEnabled;
    private Logger logger = LogFactory.createDefaultLogger(Logger.class);
    JPAEntityManagerProvider provider;

    public RequestCompletionNotifier(SupplierCheckConfig config, JPAEntityManagerProvider provider) {
        this.isEnabled = TRUE.equalsIgnoreCase(config.getSchedulerConfig().getRequestCompletionConfig(ENABLE).getEnable());
        this.provider = provider;
        this.persistence = new JPATransactionalContext(provider);
    }

    @Override
    public void run() {
        if (isEnabled) {
            List<SupplierCheckClientRequestEntity> requestEntityList = null;
            try {
                requestEntityList = getSupplierCheckRequest();
                if (requestEntityList.size() > 0) {
                    for (SupplierCheckClientRequestEntity requestEntity : requestEntityList) {
                        if (Completed.value().equalsIgnoreCase(requestEntity.getStatus())) {
                            notifyClient(new StatusResponse(requestEntity.getId(), SUCCESS, "Request is completed successfully..", SUCCESS_CODE), requestEntity.getCallbackUri() , requestEntity.getId());
                        }
                    }
                }
            } catch (Exception e) {
                logger.error(e);
            }
        }
    }

    public List<SupplierCheckClientRequestEntity> getSupplierCheckRequest() throws Exception {
        List<SupplierCheckClientRequestEntity> result = newArrayList();
        EntityManager entityManager = null;
        try {
            entityManager = provider.entityManager();
            result = entityManager.createNativeQuery("select * from supplier_check_client_request where status='" + Completed.value() + "' and CALLBACK_URI is not null", SupplierCheckClientRequestEntity.class).getResultList();
        } catch (NoResultException noResultException) {
            logger.noResultFoundForSupplierClientRequest();
            return null;
        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
       }
        return result;
    }

    public boolean updateClientRequestStatus(final String requestId, final String status) {
        try {
            persistence.execute(new JPATransactionUnit() {
                @Override
                public void execute(JPAPersistenceManager connection) {
                    APEQrefJPARepository repository = new APEQrefJPARepository(connection);
                    try {
                        logger.updatingStatusAsNotified(requestId);
                        repository.updateClientRequestStatus(requestId , status);
                        logger.updatedStatusAsNotified(requestId);
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

    private void notifyClient(final StatusResponse statusResponse, final String callbackUri, final String id) {
        try { logger.notifying(id, callbackUri);
            String result = new ProxyAwareRestRequestBuilder(UriBuilder.fromUri(callbackUri).build(), MediaType.APPLICATION_JSON_TYPE).build().post(statusResponse).getEntity(String.class);
            if (result.contains(OK)) {
                updateClientRequestStatus(id, Notified.value());
            }
        } catch (Exception e) {
            logger.failed(id, callbackUri);
        }
    }

    private interface Logger {
        @Log(level = LogLevel.INFO, format = "Notifying for request : '%s' on '%s'")
        void notifying(String requestId, String clientUrl);

        @Log(level = LogLevel.INFO, format = "Error '%s'")
        void error(Exception e);

        @Log(level = LogLevel.INFO, format = "Updating status as NOTIFIED for Supplier Check Client Request, Id :  '%s'")
        void updatingStatusAsNotified(String requestId);

        @Log(level = LogLevel.INFO, format = "Status successfully updated as NOTIFIED for Supplier Check Client Request, Id :  '%s'")
        void updatedStatusAsNotified(String requestId);

        @Log(level = LogLevel.INFO, format = "No request is in Completed state, to notify client")
        void noResultFoundForSupplierClientRequest();

        @Log(level = LogLevel.INFO, format = "Failed to notify for request : '%s' on '%s'")
        void failed(String requestId, String clientUrl);
    }
}
