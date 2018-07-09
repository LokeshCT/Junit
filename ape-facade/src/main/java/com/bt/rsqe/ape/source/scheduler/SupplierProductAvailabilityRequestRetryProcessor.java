package com.bt.rsqe.ape.source.scheduler;

import com.bt.rsqe.ape.config.ApeFacadeConfig;
import com.bt.rsqe.ape.config.SupplierCheckConfig;
import com.bt.rsqe.ape.config.TimeoutConfig;
import com.bt.rsqe.ape.dto.SupplierCheckRequest;
import com.bt.rsqe.ape.dto.SupplierSite;
import com.bt.rsqe.ape.repository.APEQrefJPARepository;
import com.bt.rsqe.ape.repository.entities.SupplierProductEntity;
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
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Date;
import java.util.List;

import static com.bt.rsqe.ape.config.TimeoutConfig.*;
import static com.bt.rsqe.ape.constants.SupplierProductConstants.*;
import static com.bt.rsqe.ape.dto.SupplierStatus.*;
import static com.bt.rsqe.ape.source.SupplierProductHelper.*;
import static com.google.common.collect.Lists.*;
import static org.apache.commons.lang.time.DateUtils.*;

/**
 * Created by 605875089 on 22/02/2016.
 */
public class SupplierProductAvailabilityRequestRetryProcessor implements Runnable {
    private Logger logger = LogFactory.createDefaultLogger(Logger.class);
    SupplierCheckConfig config;
    JPAEntityManagerProvider provider;
    private final JPATransactionalContext persistence;
    SupplierCheckRequestProcessor requestProcessor;
    private RestRequestBuilder restRequestBuilder;

    public SupplierProductAvailabilityRequestRetryProcessor(ApeFacadeConfig apeFacadeConfig, JPAEntityManagerProvider provider, SupplierCheckRequestProcessor requestProcessor) {
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
            processFailedRequests();
        } catch (Exception e) {
            logger.error(e);
        }
    }

    private void processFailedRequests() throws Exception {
        //get all the site Ids which having the status as retry,timedout,Failed

        List<SupplierSiteEntity> supplierSiteEntityList = getFilteredSupplierSiteList(getSupplierSiteEntityList());

        //prepare request object
        List<SupplierCheckRequest> request = buildSupplierCheckRequest(supplierSiteEntityList);

        postRequest(request, supplierSiteEntityList);

    }

    private List<SupplierCheckRequest> buildSupplierCheckRequest(final List<SupplierSiteEntity> supplierSiteEntityList) throws Exception {
        List<SupplierCheckRequest> requestList = newArrayList();
        for (SupplierSiteEntity entity : supplierSiteEntityList) {

            SupplierCheckRequest request = new SupplierCheckRequest("", null, entity.getCustomerId().toString(), null,
                                                                    null, null, null, SITE, "No", "User", "rSQEScheduler", null, null, null);
            request.setSupplierSites(convertToSupplierSite(entity));

            requestList.add(request);
        }


        return requestList;
    }


    private List<SupplierSite> convertToSupplierSite(SupplierSiteEntity siteEntity) {
        List<SupplierSite> sites = newArrayList();
        SupplierSite site = siteEntity.toDto();
        if (siteEntity.getSupplierProductEntityList() != null) {
            site.setSupplierList(convertToSupplier(siteEntity.getSupplierProductEntityList()));
        }
        sites.add(site);

        return sites;
    }

    private List<SupplierSiteEntity> getSupplierSiteEntityList() throws Exception {
        List<SupplierSiteEntity> siteEntities = null;
        EntityManager entityManager = null;
        try {
            entityManager = provider.entityManager();
            siteEntities = entityManager.createNativeQuery("select * from SUPPLIER_SITE where SITE_ID in (select SITE_ID from SUPPLIER_PRODUCT where status in ('Failed','Retry','Timeout'))", SupplierSiteEntity.class).getResultList();
        } catch (NoResultException noResultException) {
            return null;
        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }

        return siteEntities;
    }

    private void updateFailedRequestStatus(final List<SupplierSiteEntity> supplierSiteEntityList) {
        try {
            persistence.execute(new JPATransactionUnit() {
                @Override
                public void execute(JPAPersistenceManager connection) {
                    APEQrefJPARepository repository = new APEQrefJPARepository(connection);
                    try {
                        for (SupplierSiteEntity site : supplierSiteEntityList) {
                            for (SupplierProductEntity product : site.getSupplierProductEntityList()) {
                                List<String> productList = repository.getProductsBySiteAndSupplier(site.getSiteId(), Lists.newArrayList(product.getSpacId()));
                                if (productList.size() > 0) {
                                    Date requestedTime = timestamp();
                                    repository.updateSupplierProduct(productList, requestedTime, addMinutes(requestedTime, getTimeout(product.getAvailabilityCheckType())), InProgress.value(), "", "", getRetryCount(product.getRetryCount()));
                                }

                            }
                        }
                    } catch (Exception e) {
                        logger.error(e);
                    }
                }
            });

        } catch (Exception ex) {
            logger.error(ex);

        }
    }

    private Integer getRetryCount(Integer retry) {
        return retry == null ? null : retry + 1;

    }

    private List<SupplierSiteEntity> getFilteredSupplierSiteList(final List<SupplierSiteEntity> sites) {
        List<SupplierSiteEntity> siteToProcess = newArrayList();
        for (SupplierSiteEntity input : sites) {
            List<SupplierProductEntity> failedSupplierProducts = getFailedSupplierProducts(input.getSupplierProductEntityList());
            if (failedSupplierProducts.size() > 0) {
                SupplierSiteEntity supplierSiteEntity = new SupplierSiteEntity(input.getSiteId(), input.getCustomerId(), input.getSiteName(), input.getCountryISOCode(), input.getCountryName(),
                                                                               input.getExpiryDate(), input.getAvailabilityTypeId(), input.getAvailabilityTelephoneNumber(), input.getErrorDescription(),
                                                                               failedSupplierProducts, input.getTimeout());
                siteToProcess.add(supplierSiteEntity);
            }
        }
        return siteToProcess;
    }

    private List<SupplierProductEntity> getFailedSupplierProducts(List<SupplierProductEntity> items) {
        return newArrayList(Iterables.filter(items, new Predicate<SupplierProductEntity>() {
            @Override
            public boolean apply(SupplierProductEntity input) {
                return isAvailabilityCheckNeeded(input);
            }
        }));
    }

    private boolean isAvailabilityCheckNeeded(SupplierProductEntity entity) {
        return (Failed.value().equalsIgnoreCase(entity.getStatus())
                || Retry.value().equalsIgnoreCase(entity.getStatus())
                || Timeout.value().equalsIgnoreCase(entity.getStatus()))
               && (entity.getRetryCount() == null ? true : entity.getRetryCount() < 3);
    }

    private int getTimeout(String availCheckType) {
        return AUTO.equalsIgnoreCase(availCheckType) ? config.getServiceConfig().getTimeoutConfig(AVAILABILITY_AUTO).getValue() : config.getServiceConfig().getTimeoutConfig(TimeoutConfig.AVAILABILITY_MANUAL).getValue();
    }

    private void postRequest(final List<SupplierCheckRequest> requestList, List<SupplierSiteEntity> supplierSiteEntityList) {
        try {
            boolean success = false;
            for (SupplierCheckRequest request : requestList) {
                success = false;
                Integer result = restRequestBuilder.build("supplier-check-service", "initiate-availability", "request").post(request).getStatus();
                if (result != null && result.equals(Response.Status.OK.getStatusCode())) {
                    success = true;
                }
            }

            if (success) {
                updateFailedRequestStatus(supplierSiteEntityList);
            }

        } catch (Exception e) {
            logger.error(e);
        }

    }

    private interface Logger {
        @Log(level = LogLevel.INFO, format = "'%s'")
        void info(String message);

        @Log(level = LogLevel.ERROR, format = "'%s'")
        void error(Exception message);
    }
}
