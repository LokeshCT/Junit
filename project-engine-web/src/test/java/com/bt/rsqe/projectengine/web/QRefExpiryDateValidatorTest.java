package com.bt.rsqe.projectengine.web;

import com.bt.rsqe.ProjectEngineWebEnvironmentTestConfig;
import com.bt.rsqe.configuration.ConfigurationProvider;
import com.bt.rsqe.configuration.RestClientConfig;
import com.bt.rsqe.container.ApplicationConfig;
import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.domain.bom.parameters.ProductInstanceId;
import com.bt.rsqe.domain.project.PricingStatus;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.pricing.PriceResponseDTO;
import com.bt.rsqe.pricing.PricesDTO;
import com.bt.rsqe.pricing.QRefDTO;
import com.bt.rsqe.pricing.ShrubAssetDTO;
import com.bt.rsqe.pricing.config.PricingFacadeClientConfig;
import com.bt.rsqe.projectengine.server.ProjectEngineWebConfig;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.QuoteOptionPricingOrchestrator;
import com.bt.rsqe.utils.Environment;
import com.bt.rsqe.web.rest.RestRequestBuilder;
import com.bt.rsqe.web.rest.RestResource;
import com.bt.rsqe.web.rest.RestResponse;
import com.bt.rsqe.web.rest.dto.types.JaxbDateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

//import static com.bt.rsqe.pricing.fixture.PriceDTOFixture.aPriceDTO;
//import com.bt.rsqe.pricing.fixture.PriceDTOFixture.*;
import static com.bt.rsqe.projectengine.web.fixtures.LineItemModelFixture.aLineItemModel;
import static com.bt.rsqe.utils.Uuid.randomUuid;
import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class QRefExpiryDateValidatorTest {
    private RestRequestBuilder restRequestBuilder;
    private QuoteOptionPricingOrchestrator pricingOrchestrator;
    private ProjectEngineWebConfig mockProjectEngineWebConfiguration;
    private PricingFacadeClientConfig pricingFacadeClientConfig;
    private RestClientConfig.RestAuthenticationClientConfig restAuthenticationClientConfig;
    private ProductInstanceClient productInstanceClient;
    private List<String> productInstanceIdList;
    private ProjectEngineWebConfig config;
    private static final String customerId = "customerId";
    private static final String contractId = "contractId";
    private static final String projectId = "projectId";
    private static final String quoteOptionId = "quoteOptionId";
    private static final String orderId = "orderId";
    private static final String productInstanceId = "productInstanceId";
    private static final String quoteOptionItemDTOId = "quoteOptionItemDTOId";
    private QRefExpiryDateValidator qRefExpiryDateValidator;
    private RestResource restResource;
    private RestResponse restResponse;
    private List<QRefDTO> validQrefDtos;
    private List<QRefDTO> expiredQrefDtos;
    private PriceResponseDTO validPriceResponseDTO;
    private PriceResponseDTO expiredPriceResponseDTO;
    private List<PriceResponseDTO> priceResponseDTOList;
    private List<LineItemModel> lineItemModelList;
    private LineItemModel lineItemModel;
    ApplicationConfig applicationConfig;

    @Before
    public void setUp() throws Exception {
        pricingOrchestrator = mock(QuoteOptionPricingOrchestrator.class);
        productInstanceClient = mock(ProductInstanceClient.class);
        lineItemModel = aLineItemModel().withCustomerId(customerId).withQuoteOptionItemDTOId(quoteOptionItemDTOId)
            .withQuoteOptionItemDTOOrderId(orderId).build();
        lineItemModelList = new ArrayList<LineItemModel>();
        lineItemModelList.add(lineItemModel);
        priceResponseDTOList = new ArrayList<PriceResponseDTO>();
        mockProjectEngineWebConfiguration = mock(ProjectEngineWebConfig.class);
        applicationConfig = mock(ApplicationConfig.class);
        pricingFacadeClientConfig = mock(PricingFacadeClientConfig.class);
        restAuthenticationClientConfig = mock(RestClientConfig.RestAuthenticationClientConfig.class);
        config = ConfigurationProvider.provide(ProjectEngineWebEnvironmentTestConfig.class, Environment.env()).getProjectEngineWebConfig();
        restRequestBuilder = mock(RestRequestBuilder.class);
        restResource = mock(RestResource.class);
        restResponse = mock(RestResponse.class);
        validQrefDtos = newArrayList(
            new QRefDTO(randomUuid(), JaxbDateTime.valueOf("2050-01-01T01:01:01.000Z"), new PricesDTO(), new ShrubAssetDTO(), PricingStatus.FIRM)
        );
        expiredQrefDtos = newArrayList(
            new QRefDTO(randomUuid(), JaxbDateTime.valueOf("2013-01-01T01:01:01.000Z"), new PricesDTO(), new ShrubAssetDTO(), PricingStatus.FIRM)
        );


    }


    @Test
    public void shouldGetProductInstanceIdsForQuote() throws Exception {
        ProductInstance productInstance = mock(ProductInstance.class);
        when(pricingOrchestrator.getLineItemModels(customerId, contractId, projectId, quoteOptionId)).thenReturn(lineItemModelList);
        when(productInstanceClient.get(lineItemModel.getLineItemId())).thenReturn(productInstance);
        when(productInstance.getProductInstanceId()).thenReturn(new ProductInstanceId(productInstanceId));
        qRefExpiryDateValidator = new QRefExpiryDateValidator(mock(ProjectEngineWebConfig.class),
                                                              pricingOrchestrator,
                                                              productInstanceClient);
        productInstanceIdList = qRefExpiryDateValidator.getProductInstancesForQuote(customerId, contractId, projectId, quoteOptionId, orderId);
        assertThat(productInstanceIdList.get(0), is("productInstanceId"));
    }


    @Test
    public void shouldReturnRestRequestBuilder() throws Exception {
        qRefExpiryDateValidator = new QRefExpiryDateValidator(config,
                                                              pricingOrchestrator,
                                                              productInstanceClient);
        restRequestBuilder = qRefExpiryDateValidator.getRestRequestBuilder(config);
        assertNotNull(restRequestBuilder);
    }


    @Test
    public void shouldGetPriceResponseDTOsForQuote() throws Exception {
        validPriceResponseDTO = new PriceResponseDTO("issued", "resultId", validQrefDtos, "chargingSchemeName", "id", 1L, null);
        priceResponseDTOList.add(validPriceResponseDTO);
        productInstanceIdList = newArrayList();
        productInstanceIdList.add(0, productInstanceId);
        when(mockProjectEngineWebConfiguration.getPricingFacadeClientConfig()).thenReturn(pricingFacadeClientConfig);
        when(pricingFacadeClientConfig.getApplicationConfig()).thenReturn(applicationConfig);
        when(pricingFacadeClientConfig.getRestAuthenticationClientConfig()).thenReturn(restAuthenticationClientConfig);
        when(restAuthenticationClientConfig.getSecret()).thenReturn("secret");
        when(restResource.get()).thenReturn(restResponse);
        when(restRequestBuilder.build(productInstanceId)).thenReturn(restResource);
        when(restRequestBuilder.build(productInstanceId).get().getEntity(PriceResponseDTO.class)).thenReturn(validPriceResponseDTO);

        qRefExpiryDateValidator = new QRefExpiryDateValidator(mockProjectEngineWebConfiguration,
                                                              pricingOrchestrator,
                                                              productInstanceClient);
        List<PriceResponseDTO> priceResponseDTOList = qRefExpiryDateValidator.getPriceResponseDTOsForQuote(productInstanceIdList, restRequestBuilder);
        assertNotNull(priceResponseDTOList);
        assertThat(priceResponseDTOList.get(0).getChargingSchemeName(), is("chargingSchemeName"));
    }


    @Test
    public void shouldReturnExceptionForGetPriceResponseDTOsForQuote() throws Exception {
        validPriceResponseDTO = new PriceResponseDTO("issued", "resultId", validQrefDtos, "chargingSchemeName", "id", 1L, null);
        priceResponseDTOList.add(validPriceResponseDTO);
        productInstanceIdList = newArrayList();
        productInstanceIdList.add(0, productInstanceId);
        qRefExpiryDateValidator = new QRefExpiryDateValidator(mockProjectEngineWebConfiguration,
                                                              pricingOrchestrator,
                                                              productInstanceClient);
        restRequestBuilder = qRefExpiryDateValidator.getRestRequestBuilder(config);
        List<PriceResponseDTO> priceResponseDTOList = qRefExpiryDateValidator.getPriceResponseDTOsForQuote(productInstanceIdList, restRequestBuilder);
        assertNotNull(priceResponseDTOList);
        assertTrue(priceResponseDTOList.isEmpty());
    }


    @Test
    public void shouldReturnQRefsStillValid() throws Exception {
        validPriceResponseDTO = new PriceResponseDTO("issued", "resultId", validQrefDtos, "chargingSchemeName", "id", 1L, null);
        priceResponseDTOList.add(validPriceResponseDTO);
        qRefExpiryDateValidator = new QRefExpiryDateValidator(mockProjectEngineWebConfiguration,
                                                              pricingOrchestrator,
                                                              productInstanceClient);
        qRefExpiryDateValidator.offerExpired(priceResponseDTOList);
    }


    @Test
    public void shouldReturnQRefsHaveExpired() throws Exception {
        expiredPriceResponseDTO = new PriceResponseDTO("issued", "resultId", expiredQrefDtos, "chargingSchemeName", "id", 1L, null);
        priceResponseDTOList.add(expiredPriceResponseDTO);
        qRefExpiryDateValidator = new QRefExpiryDateValidator(mockProjectEngineWebConfiguration,
                                                              pricingOrchestrator,
                                                              productInstanceClient);
        qRefExpiryDateValidator.offerExpired(priceResponseDTOList);
    }
}
