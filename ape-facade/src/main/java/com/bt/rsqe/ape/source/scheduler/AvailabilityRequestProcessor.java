package com.bt.rsqe.ape.source.scheduler;

import com.bt.rsqe.ape.ApeFacade;
import com.bt.rsqe.ape.config.ApeFacadeConfig;
import com.bt.rsqe.ape.config.SupplierCheckConfig;
import com.bt.rsqe.ape.dto.SupplierCheckRequest;
import com.bt.rsqe.ape.repository.APEQrefJPARepository;
import com.bt.rsqe.ape.repository.entities.AvailabilityRequestQueue;
import com.bt.rsqe.ape.repository.entities.SupplierCheckApeRequestEntity;
import com.bt.rsqe.ape.repository.entities.SupplierCheckClientRequestEntity;
import com.bt.rsqe.ape.repository.entities.SupplierSiteEntity;
import com.bt.rsqe.ape.source.processor.SupplierCheckRequestProcessor;
import com.bt.rsqe.container.ApplicationConfig;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.bt.rsqe.persistence.JPAEntityManagerProvider;
import com.bt.rsqe.persistence.JPAPersistenceManager;
import com.bt.rsqe.persistence.JPATransactionUnit;
import com.bt.rsqe.persistence.JPATransactionalContext;
import com.bt.rsqe.utils.UriBuilder;
import com.bt.rsqe.web.rest.ProxyAwareRestRequestBuilder;
import com.bt.rsqe.web.rest.RestRequestBuilder;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

import static com.bt.rsqe.ape.constants.SupplierProductConstants.*;
import static com.bt.rsqe.ape.source.SupplierProductHelper.*;
import static com.bt.rsqe.ape.source.SupplierProductStore.*;
import static com.google.common.collect.Lists.*;

/**
 * Created by 605783162 on 14/08/2015.
 */
public class AvailabilityRequestProcessor implements Runnable {
    private ApeFacade ape;
    private Logger logger = LogFactory.createDefaultLogger(Logger.class);
    SupplierCheckConfig config;
    JPAEntityManagerProvider provider;
    private final JPATransactionalContext persistence;
    SupplierCheckRequestProcessor requestProcessor;
    private RestRequestBuilder restRequestBuilder;

    public AvailabilityRequestProcessor(ApeFacadeConfig apeFacadeConfig, JPAEntityManagerProvider provider, SupplierCheckRequestProcessor requestProcessor) {
        this.config = apeFacadeConfig.getSupplierCheckConfig();
        this.provider = provider;
        this.persistence = new JPATransactionalContext(provider);
        this.requestProcessor = requestProcessor;
        ApplicationConfig applicationConfig = apeFacadeConfig.getApplicationConfig();
        URI baseUri = new UriBuilder().scheme(applicationConfig.getScheme())
                .host(applicationConfig.getHost())
                .port(applicationConfig.getPort())
                .segment("rsqe", "ape-facade")
                .build();
        restRequestBuilder = new ProxyAwareRestRequestBuilder(baseUri);
    }

    @Override
    public void run() {
        try {
            processQueuedRequests();
        } catch (Exception e) {
            logger.error(e);
        }
    }

    private void processQueuedRequests() throws Exception {
        //get all the queued reqs
        List<AvailabilityRequestQueue> queueList = getQueuedRequests();
        for (AvailabilityRequestQueue queuedRequest : queueList) {
            //prepare request object
            SupplierCheckRequest request = buildSupplierCheckRequest(queuedRequest);
            queuedRequest.setStatus("InProgress");
            updateQueuedRequestStatus(queuedRequest);
            //initiate availability call
            postRequest(request, queuedRequest);
        }
    }

    private SupplierCheckRequest buildSupplierCheckRequest(AvailabilityRequestQueue queuedRequest) throws Exception {
        SupplierCheckApeRequestEntity apeRequest = getApeRequestByApeRequestId(queuedRequest.getApeRequestId());
        SupplierCheckRequest request = null;
        if (apeRequest != null) {
            SupplierCheckClientRequestEntity clientReq = apeRequest.getSupplierCheckClientRequestEntity();
            request = new SupplierCheckRequest("", clientReq.getId(), String.valueOf(clientReq.getCustomerId()), clientReq.getUser(),
                    null, null, null, SITE, clientReq.getAutoTrigger(), clientReq.getTriggerType(), clientReq.getSourceSystemName(), null, null, null);
            List<Long> siteIds = newArrayList(Long.parseLong(queuedRequest.getSiteId()));
            List<SupplierSiteEntity> siteEntities = getSupplierSiteRequestObjectBySiteId(siteIds);
            request.setSupplierSites(convertToSupplierSite(siteEntities));
        }
        return request;
    }

    private List<AvailabilityRequestQueue> getQueuedRequests() throws Exception {
        List<AvailabilityRequestQueue> result = newArrayList();
        EntityManager entityManager = null;
        try {
            entityManager = provider.entityManager();
            result = entityManager.createNativeQuery("select * from availability_request_queue where status = 'Queued' and task = 'AvailabilityCheck'", AvailabilityRequestQueue.class).getResultList();
        } catch (NoResultException noResultException) {
            return null;
        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }
        return result;
    }

    private SupplierCheckApeRequestEntity getApeRequestByApeRequestId(String requestId) throws Exception {
        EntityManager entityManager = null;
        try {
            entityManager = provider.entityManager();
            return entityManager.find(SupplierCheckApeRequestEntity.class, requestId);
        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }
    }

    private boolean deleteRequestFromAvailabilityQueue(final AvailabilityRequestQueue queuedRequest) {
        try {
            persistence.execute(new JPATransactionUnit() {
                @Override
                public void execute(JPAPersistenceManager connection) {
                    APEQrefJPARepository repository = new APEQrefJPARepository(connection);
                    try {
                        repository.deleteRequestFromAvailabilityQueue(queuedRequest);
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

    private boolean updateQueuedRequestStatus(final AvailabilityRequestQueue queuedRequest) {
        try {
            persistence.execute(new JPATransactionUnit() {
                @Override
                public void execute(JPAPersistenceManager connection) {
                    APEQrefJPARepository repository = new APEQrefJPARepository(connection);
                    try {
                        repository.updateQueuedRequestStatus(queuedRequest);
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

    private boolean postRequest(final SupplierCheckRequest request, final AvailabilityRequestQueue queuedRequest) {
        try {
            Integer result = restRequestBuilder.build("supplier-check-service", "initiate-availability", "request").post(request).getStatus();;
            if (result!= null && result.equals(Response.Status.OK.getStatusCode())){
                deleteRequestFromAvailabilityQueue(queuedRequest);
            }
        } catch (Exception e) {
            queuedRequest.setStatus("Queued");
            updateQueuedRequestStatus(queuedRequest);
            return false;
        }

        return true;
    }

    private interface Logger {
        @Log(level = LogLevel.INFO, format = "'%s'")
        void info(String message);

        @Log(level = LogLevel.ERROR, format = "'%s'")
        void error(Exception message);
    }
}
