package com.bt.rsqe.ape.source.scheduler;

import com.bt.rsqe.ape.config.EndpointUriConfig;
import com.bt.rsqe.ape.config.SupplierCheckConfig;
import com.bt.rsqe.ape.constants.SupplierProductConstants;
import com.bt.rsqe.ape.dto.SupplierCheckRequest;
import com.bt.rsqe.ape.dto.SupplierSite;
import com.bt.rsqe.ape.repository.APEQrefJPARepository;
import com.bt.rsqe.ape.repository.entities.DslEfmSupportedCountriesEntity;
import com.bt.rsqe.ape.repository.entities.SupplierCheckLogEntity;
import com.bt.rsqe.ape.source.RequestId;
import com.bt.rsqe.ape.source.processor.RequestBuilder;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.bt.rsqe.persistence.JPAEntityManagerProvider;
import com.bt.rsqe.persistence.JPAPersistenceManager;
import com.bt.rsqe.persistence.JPATransactionUnit;
import com.bt.rsqe.persistence.JPATransactionalContext;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.persistence.EntityManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.List;

import static com.bt.rsqe.ape.config.CountryApplicabilityConfig.*;
import static com.bt.rsqe.ape.constants.SupplierProductConstants.*;
import static com.bt.rsqe.ape.source.SupplierCheckRequestInvoker.*;
import static com.bt.rsqe.ape.source.SupplierProductHelper.*;
import static com.google.common.collect.Lists.*;

/**
 * Created by 605783162 on 13/08/2015.
 */
public class CountryApplicabilityUpdater implements Runnable {

    private Logger logger = LogFactory.createDefaultLogger(Logger.class);

    public static final String COUNTRY_APPLICABILITY_UPDATER = "CountryApplicabilityUpdater";

    public static final String XPATH_ISOCODE = "a:countryISOCode";
    public static final String XPATH_APPLICABLE = "a:applicable";
    public static final String XPATH_RESPONSE_DETAILS = "a:ResponseDetails";

    private final JPATransactionalContext persistence;

    RequestBuilder requestBuilder;
    EndpointUriConfig endpoint;
    JPAEntityManagerProvider provider;

    boolean isEnabled = false;

    public CountryApplicabilityUpdater(SupplierCheckConfig supplierCheckConfig, JPAEntityManagerProvider provider) {
        this.isEnabled =  SupplierProductConstants.TRUE.equalsIgnoreCase(supplierCheckConfig.getSchedulerConfig().getCountryApplicabilityConfig(ENABLE).getEnable());
        this.endpoint = supplierCheckConfig.getEndpointUriConfig();
        requestBuilder = new RequestBuilder();
        this.provider = provider;
        this.persistence = new JPATransactionalContext(provider);
    }

    @Override
    public void run() {
        logger.started();
        List<DslEfmSupportedCountriesEntity> dslEfmCountries;
        try {
            if (isEnabled) {
                logger.jobStarted();
               dslEfmCountries = getAllCountries();
                SupplierCheckRequest request = new SupplierCheckRequest();
                request.setRequestId(RequestId.newInstance().value());
                request.setSupplierSites(getSupplierSites(dslEfmCountries));
                String requestMessage = requestBuilder.build(request, APPLICABILITY_TEMPLATE);
                logRequest(request, requestMessage);
                String responseMessage = processRequest(requestMessage, endpoint.getUri(), SOAP_ACTION_GET_DSL_APPLICABILITY);
                logResponse(request, responseMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
        }
    }

    public String processRequest(String requestMessage, String endpoint, String soapAction) {
        String responseMessage = sendMessage(endpoint, soapAction, requestMessage);
        extractResponse(responseMessage);
        return responseMessage;
    }

    private List<SupplierSite> getSupplierSites(List<DslEfmSupportedCountriesEntity> list) {
        return newArrayList(Lists.transform(list, new Function<DslEfmSupportedCountriesEntity, SupplierSite>() {
            @Override
            public SupplierSite apply(DslEfmSupportedCountriesEntity input) {
                return new SupplierSite(input.getIsoCode(), input.getCountry());
            }
        }));
    }

    private List<DslEfmSupportedCountriesEntity> getAllCountries() {
        List<DslEfmSupportedCountriesEntity> result = newArrayList();
        EntityManager entityManager = null;
        try{
            entityManager = provider.entityManager();
            result = entityManager.createQuery("select entity from DslEfmSupportedCountriesEntity entity").getResultList();
        }catch (Exception e) {
            //do nothing
        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }
        return result;
    }

    private void extractResponse(String response) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new InputSource(new ByteArrayInputStream(response.getBytes("utf-8"))));
            doc.getDocumentElement().normalize();

            List<DslEfmSupportedCountriesEntity> countriesEntityList = newArrayList();
            DslEfmSupportedCountriesEntity countriesEntity = null;
            NodeList inputNodes = doc.getElementsByTagName(XPATH_RESPONSE_DETAILS);

            for (int inputNodeIndex = 0; inputNodeIndex < inputNodes.getLength(); inputNodeIndex++) {
                Node requestNode = inputNodes.item(inputNodeIndex);
                if (requestNode.getNodeType() == Node.ELEMENT_NODE) {
                    countriesEntity = new DslEfmSupportedCountriesEntity();
                    Element element = (Element) requestNode;
                    countriesEntity.setIsoCode(getElementValue(element, XPATH_ISOCODE));
                    countriesEntity.setDslEfmSupported(getElementValue(element, XPATH_APPLICABLE));
                    countriesEntity.setUpdatedOn(timestamp());
                    countriesEntityList.add(countriesEntity);
                }
            }
            updateCountryData(countriesEntityList);
        } catch (Exception e) {
            logger.error(e);
        }
    }

    private void logRequest(SupplierCheckRequest request, String payload) throws Exception {
        SupplierCheckLogEntity supplierCheckLogEntity = new SupplierCheckLogEntity(request.getRequestId(), REQUEST, OPERATION_GET_DSL_APPLICABILITY, payload, COUNTRY_APPLICABILITY_UPDATER, timestamp(), "");
        logRequestResponse(supplierCheckLogEntity);
    }

    private void logResponse(SupplierCheckRequest request, String payload) {
        SupplierCheckLogEntity supplierCheckLogEntity = new SupplierCheckLogEntity(request.getRequestId(), RESPONSE, OPERATION_GET_DSL_APPLICABILITY, payload, COUNTRY_APPLICABILITY_UPDATER, timestamp(), "");
        logRequestResponse(supplierCheckLogEntity);
    }

    private interface Logger {
        @Log(level = LogLevel.INFO, format = "Starting country applicability retrieval service..")
        void started();

        @Log(level = LogLevel.INFO, format = "Country Applicability Updater job started")
        void jobStarted();

        @Log(level = LogLevel.ERROR, format = "CountryApplicabilityUpdater Error : %s")
        void error(Exception ex);
    }


    public boolean logRequestResponse(final Object entity) {
        try {
            persistence.execute(new JPATransactionUnit() {
                @Override
                public void execute(JPAPersistenceManager connection) {
                    APEQrefJPARepository repository = new APEQrefJPARepository(connection);
                    repository.save((SupplierCheckLogEntity) entity);
                }
            });
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean updateCountryData(final List<DslEfmSupportedCountriesEntity> entities) {
        try {
            persistence.execute(new JPATransactionUnit() {
                @Override
                public void execute(JPAPersistenceManager connection) {
                    APEQrefJPARepository repository = new APEQrefJPARepository(connection);
                    repository.save(entities);
                }
            });
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
