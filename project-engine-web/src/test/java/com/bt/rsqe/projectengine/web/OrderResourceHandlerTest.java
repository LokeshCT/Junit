package com.bt.rsqe.projectengine.web;

import com.bt.rsqe.cleanordervalidation.CleanOrderValidationException;
import com.bt.rsqe.cleanordervalidation.CleanOrderValidationResourceClient;
import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.client.resource.SiteResourceClient;
import com.bt.rsqe.customerinventory.dto.AssetDTO;
import com.bt.rsqe.customerinventory.fixtures.AssetDTOFixture;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerrecord.UserResource;
import com.bt.rsqe.domain.QuoteOptionItemStatus;
import com.bt.rsqe.domain.SqeUniqueId;
import com.bt.rsqe.domain.bom.parameters.ProductInstanceId;
import com.bt.rsqe.domain.product.Attribute;
import com.bt.rsqe.domain.product.AttributeDataType;
import com.bt.rsqe.domain.product.AttributeName;
import com.bt.rsqe.domain.product.AttributeOwner;
import com.bt.rsqe.domain.product.Behaviour;
import com.bt.rsqe.domain.product.DefaultValue;
import com.bt.rsqe.domain.product.InstanceCharacteristic;
import com.bt.rsqe.domain.product.InstanceCharacteristicValue;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.extensions.RuleAttributeSource;
import com.bt.rsqe.domain.product.parameters.ProductCategoryCode;
import com.bt.rsqe.domain.project.PricingStatus;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.expedio.project.ExpedioProjectResource;
import com.bt.rsqe.inlife.client.ApplicationCapabilityProvider;
import com.bt.rsqe.pc.client.ConfiguratorCloneToClient;
import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.projectengine.IfcAction;
import com.bt.rsqe.projectengine.LineItemDiscountStatus;
import com.bt.rsqe.projectengine.LineItemIcbApprovalStatus;
import com.bt.rsqe.projectengine.LineItemOrderStatus;
import com.bt.rsqe.projectengine.LineItemValidationResultDTO;
import com.bt.rsqe.projectengine.OrderDTO;
import com.bt.rsqe.projectengine.OrderItemStatus;
import com.bt.rsqe.projectengine.OrderStatus;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemResource;
import com.bt.rsqe.projectengine.QuoteOptionResource;
import com.bt.rsqe.projectengine.TpeRequestDTO;
import com.bt.rsqe.projectengine.server.ProjectEngineWebConfig;
import com.bt.rsqe.projectengine.web.facades.FutureProductInstanceFacade;
import com.bt.rsqe.projectengine.web.quoteoptionorders.QuoteOptionOrdersOrchestrator;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.QuoteOptionPricingOrchestrator;
import com.bt.rsqe.projectengine.web.tpe.TpeStatusManager;
import com.bt.rsqe.projectengine.web.validators.BundleProductValidator;
import com.bt.rsqe.security.ExpedioUserContextResolver;
import com.bt.rsqe.security.PermissionsDTO;
import com.bt.rsqe.security.UserContext;
import com.bt.rsqe.security.UserDTO;
import com.bt.rsqe.security.UserType;
import com.bt.rsqe.tpe.client.PricingTpeClient;
import com.bt.rsqe.tpe.multisite.SQETppStatusChange;
import com.bt.rsqe.tpe.multisite.SQETppStatusRequest;
import com.bt.rsqe.tpe.multisite.StatusChangeResponse;
import com.bt.rsqe.tpe.multisite.TppOverallResponse;
import com.bt.rsqe.tpe.multisite.TppStatusResponse_Bulk;
import com.bt.rsqe.web.Presenter;
import com.google.common.base.Optional;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.bt.rsqe.inlife.client.ApplicationCapabilityProvider.Capability.*;
import static com.google.common.collect.Lists.*;
import static java.lang.String.format;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.*;

public class OrderResourceHandlerTest {
    private OrderResourceHandler resourceHandler;
    private Presenter presenter;
    private UserResource userResource;
    private ExpedioUserContextResolver expedioUserContextResolver;
    private ProjectEngineWebConfig projectEngineWebConfig;
    private QuoteOptionPricingOrchestrator quoteOptionPricingOrchestrator;
    private QuoteOptionOrdersOrchestrator quoteOptionOrdersOrchestrator;
    private PricingTpeClient pricingTpeClient;
    private ProductInstanceClient productInstanceClient;
    private UserContext userContext;
    private QuoteOptionItemDTO orderItem;
    private QuoteOptionItemDTO mopOrderItem;
    private QuoteOptionItemDTO ceasedOrderItem;
    private List<QuoteOptionItemDTO> orderItemList;
    private UserDTO userDTO;
    private SQETppStatusChange sqeTppStatusChange;
    private ProductInstance orderInstance;
    private OrderDTO orderDTO;
    private ConfiguratorCloneToClient cloneToClient;
    private static final String PROJECT_ID = "ProjectId";
    private static final String ORDER_ID = "OrderId";
    private static final String CUSTOMER_ID = "CUSTOMER_ID";
    private static final String CONTRACT_ID = "ContractId";
    private static final String QUOTE_OPTION_ID = "QuoteOptionId";
    private static final String USER_TOKEN = "aUserToken";
    private static final String S_CODE = "anSCode";
    private static final String MOP_S_CODE = "mopSCode";
    private static final String MOP_LINE_ITEM_ID = "aMopLineItemId";
    private static final String LINE_ITEM_ID = "aLineItemId";
    private static final String CEASED_LINE_ITEM_ID = "ceasedLineItemId";
    private static final String LOGIN_NAME = "aLoginName";
    private static final String USER_EIN = "anEin";
    private static final ProductInstanceId PI_ID = new ProductInstanceId("aPId");
    private static final Long PI_VERSION = 1L;
    private static final String TPP_ID = "aTppId";
    private TpeStatusManager tpeStatusManager;
    private ProjectResource projectResource;
    private QuoteOptionResource quoteOptionResource;
    private QuoteOptionItemResource quoteOptionItemResource;
    private CleanOrderValidationResourceClient cleanOrderValidationResourceClient;
    private ApplicationCapabilityProvider capabilityProvider;
    private FutureProductInstanceFacade futureProductInstanceFacade;
    private ExecutorService executorService;
    private PmrClient pmrClient;
    private ExpedioProjectResource expedioProjectsResource;
    private SiteResourceClient siteResourceClient;
    private BundleProductValidator bundleProductValidator;

    @Before
    public void before() throws Exception {
        presenter = mock(Presenter.class);
        userResource = mock(UserResource.class);
        userContext = mock(UserContext.class);
        productInstanceClient = mock(ProductInstanceClient.class);
        userDTO = mock(UserDTO.class);
        expedioUserContextResolver = mock(ExpedioUserContextResolver.class);
        quoteOptionOrdersOrchestrator = mock(QuoteOptionOrdersOrchestrator.class);
        orderInstance = mock(ProductInstance.class);
        pricingTpeClient = mock(PricingTpeClient.class);
        projectResource = mock(ProjectResource.class);
        tpeStatusManager = new TpeStatusManager(pricingTpeClient, productInstanceClient, projectResource);
        cloneToClient = mock(ConfiguratorCloneToClient.class);
        cleanOrderValidationResourceClient = mock(CleanOrderValidationResourceClient.class);
        capabilityProvider = mock(ApplicationCapabilityProvider.class);
        futureProductInstanceFacade = mock(FutureProductInstanceFacade.class);
        pmrClient = mock(PmrClient.class);
        bundleProductValidator = mock(BundleProductValidator.class);
        executorService = Executors.newFixedThreadPool(1);

        when(userContext.getPermissions()).thenReturn(PermissionsDTO.noPermissions());
        when(userContext.getLoginName()).thenReturn(LOGIN_NAME);
        when(expedioUserContextResolver.resolve(anyString(), anyString())).thenReturn(userContext);
        when(userDTO.getEin()).thenReturn(USER_EIN);
        when(userDTO.getUserType()).thenReturn(UserType.DIRECT);
        when(userResource.findUser(LOGIN_NAME)).thenReturn(userDTO);
        orderItem = new QuoteOptionItemDTO(LINE_ITEM_ID, S_CODE, "", null, null, null, "", QuoteOptionItemStatus.INITIALIZING,
                                           LineItemDiscountStatus.NOT_APPLICABLE, LineItemIcbApprovalStatus.NOT_APPLICABLE, null,
                                           new LineItemValidationResultDTO(LineItemValidationResultDTO.Status.PENDING),
                                           LineItemOrderStatus.NOT_APPLICABLE, IfcAction.NOT_APPLICABLE, "1", null, null,
                                           false, false, false, null, null, true, ProductCategoryCode.NIL, null, false);
        ceasedOrderItem = new QuoteOptionItemDTO(CEASED_LINE_ITEM_ID, S_CODE, "", null, null, null, "", QuoteOptionItemStatus.INITIALIZING,
                                           LineItemDiscountStatus.NOT_APPLICABLE, LineItemIcbApprovalStatus.NOT_APPLICABLE, null,
                                           new LineItemValidationResultDTO(LineItemValidationResultDTO.Status.PENDING),
                                           LineItemOrderStatus.NOT_APPLICABLE, IfcAction.NOT_APPLICABLE, "3", null, null,
                                           false, false, false, null, null, true, ProductCategoryCode.NIL, null, false);
        mopOrderItem = new QuoteOptionItemDTO(MOP_LINE_ITEM_ID, MOP_S_CODE, "", null, null, null, "", QuoteOptionItemStatus.INITIALIZING,
                                           LineItemDiscountStatus.NOT_APPLICABLE, LineItemIcbApprovalStatus.NOT_APPLICABLE, null,
                                           new LineItemValidationResultDTO(LineItemValidationResultDTO.Status.PENDING),
                                           LineItemOrderStatus.NOT_APPLICABLE, IfcAction.NOT_APPLICABLE, "2", null, null,
                                           false, false, false, null, null, true, ProductCategoryCode.NIL, null, false);
        orderItemList = newArrayList();
        orderItemList.add(orderItem);
        orderItemList.add(mopOrderItem);
        orderItemList.add(ceasedOrderItem);
        orderDTO = OrderDTO.newInstance("name", "created", orderItemList);
        orderDTO.status = OrderStatus.SUBMITTED.getValue();
        when(quoteOptionOrdersOrchestrator.getOrder(PROJECT_ID, QUOTE_OPTION_ID, ORDER_ID)).thenReturn(orderDTO);
        when(orderInstance.isSpecialBid()).thenReturn(true);
        when(orderInstance.getProductInstanceId()).thenReturn(PI_ID);
        when(orderInstance.getProductInstanceVersion()).thenReturn(PI_VERSION);
        when(orderInstance.getSqeUniqueId()).thenReturn(new SqeUniqueId(PI_ID, PI_VERSION));
        when(orderInstance.getSpecialBidId()).thenReturn(TPP_ID);
        sqeTppStatusChange = new SQETppStatusChange();
        sqeTppStatusChange.setUser_EIN(USER_EIN);
        sqeTppStatusChange.setSQE_Unique_Id(PI_ID + "_" + PI_VERSION);
        sqeTppStatusChange.setTPP_Id(TPP_ID);
        sqeTppStatusChange.setAction(PricingStatus.WON.getSubStatus());
        StatusChangeResponse statusChangeResponse =  new StatusChangeResponse();
        when(pricingTpeClient.change_Status(sqeTppStatusChange)).thenReturn(statusChangeResponse);

        quoteOptionResource = mock(QuoteOptionResource.class);
        quoteOptionItemResource = mock(QuoteOptionItemResource.class);
        when(projectResource.quoteOptionResource(anyString())).thenReturn(quoteOptionResource);
        when(quoteOptionResource.quoteOptionItemResource(anyString())).thenReturn(quoteOptionItemResource);
        when(quoteOptionItemResource.getTpeRequest(anyString(), anyLong())).thenReturn(new TpeRequestDTO());
         OfferAndOrderValidationResult result = new OfferAndOrderValidationResult(true,"");
        when(quoteOptionOrdersOrchestrator.checkValidation(anyListOf(String.class), anyString(), anyString(), anyString(), anyString())).thenReturn(result);
        when(bundleProductValidator.validate(anyString(), anyString(), anyList())).thenReturn(OfferAndOrderValidationResult.SUCCESS);
    }

    @Test
    public void shouldMakeCallToTpeChangeStatusWithActivateWhenBidIsInRespondedState() {
        SQETppStatusRequest sqeTppStatusRequest = new SQETppStatusRequest();
        sqeTppStatusRequest.setSQE_Unique_Id(QUOTE_OPTION_ID);
        sqeTppStatusRequest.setTPP_Id(TPP_ID);
        TppStatusResponse_Bulk tppStatusResponse_bulk = new TppStatusResponse_Bulk();
        TppOverallResponse tppOverallResponse = new TppOverallResponse();
        tppOverallResponse.setStatus("Responded");
        tppStatusResponse_bulk.setTppOverallResponse(tppOverallResponse);
        when(pricingTpeClient.status_Refresh(sqeTppStatusRequest)).thenReturn(tppStatusResponse_bulk);
        List<InstanceCharacteristic> instanceCharacteristics = new ArrayList();
        final InstanceCharacteristic.ValueChangeListener DO_NOTHING_LISTENER = new InstanceCharacteristic.ValueChangeListener() {
            @Override
            public void valueChanged(InstanceCharacteristic instanceCharacteristic, InstanceCharacteristicValue oldValue, InstanceCharacteristicValue newValue) {
                //do nothing.
            }
        };
        Behaviour behaviour = new Behaviour(null, null, null, ProductOffering.SPECIAL_BID_ID_ATTRIBUTE_NAME);

        InstanceCharacteristic instanceCharacteristic = new InstanceCharacteristic(
            new Attribute(new AttributeName(ProductOffering.SPECIAL_BID_ID_ATTRIBUTE_NAME),
                            null,
                            newArrayList(behaviour),
                            null,
                            AttributeDataType.STRING,
                            DefaultValue.NOT_SET,
                            false,
                            AttributeOwner.Offering,
                            new ArrayList<RuleAttributeSource>()),
            DO_NOTHING_LISTENER);

        instanceCharacteristic.setValue(TPP_ID);
        instanceCharacteristics.add(instanceCharacteristic);
        when(orderInstance.getInstanceCharacteristics()).thenReturn(instanceCharacteristics);
        when(orderInstance.getQuoteOptionId()).thenReturn(QUOTE_OPTION_ID);
        when(orderInstance.hasInstanceCharacteristic(new AttributeName(ProductOffering.SPECIAL_BID_ID_ATTRIBUTE_NAME))).thenReturn(true);
        when(productInstanceClient.get(new LineItemId(LINE_ITEM_ID))).thenReturn(orderInstance);
        final AssetDTO asset = AssetDTOFixture.anAsset().withAssetCharacteristic(ProductOffering.SPECIAL_BID_ATTRIBUTE_INDICATOR, "Yes").build();
        when(productInstanceClient.getAssetDTO(new LineItemId(LINE_ITEM_ID))).thenReturn(asset);
        when(productInstanceClient.convertAssetToLightweightInstance(asset)).thenReturn(orderInstance);

        orderDTO.status = OrderStatus.CREATED.getValue();
        when(quoteOptionOrdersOrchestrator.getOrder(PROJECT_ID, QUOTE_OPTION_ID, ORDER_ID)).thenReturn(orderDTO);

        when(quoteOptionOrdersOrchestrator.buildOrder("ORDER_NAME", PROJECT_ID, QUOTE_OPTION_ID, LINE_ITEM_ID)).thenReturn(orderDTO);

        sqeTppStatusChange.setAction(PricingStatus.ACTIVATE.getSubStatus());
        sqeTppStatusChange.setSQE_Unique_Id(orderInstance.getProductInstanceId() + "_" + orderInstance.getProductInstanceVersion());
        StatusChangeResponse statusChangeResponse =  new StatusChangeResponse();
        when(pricingTpeClient.change_Status(sqeTppStatusChange)).thenReturn(statusChangeResponse);

        resourceHandler = new OrderResourceHandler(presenter, productInstanceClient, tpeStatusManager, quoteOptionOrdersOrchestrator,
                                                   quoteOptionPricingOrchestrator, projectEngineWebConfig, expedioUserContextResolver,
                                                   userResource, cloneToClient, cleanOrderValidationResourceClient, capabilityProvider, futureProductInstanceFacade, executorService, pmrClient,expedioProjectsResource,siteResourceClient, bundleProductValidator);
        resourceHandler.createOrder(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, "ORDER_NAME", LINE_ITEM_ID, USER_TOKEN);
        verify(pricingTpeClient).change_Status(sqeTppStatusChange);
        verify(quoteOptionItemResource).putTpeRequest(Matchers.<TpeRequestDTO>any());
    }

    @Test
    public void shouldRecursivelyMakeCallToTpeChangeStatusWithActivateWhenBidIsInRespondedState() {
        SQETppStatusRequest sqeTppStatusRequest = new SQETppStatusRequest();
        sqeTppStatusRequest.setSQE_Unique_Id(QUOTE_OPTION_ID);
        sqeTppStatusRequest.setTPP_Id(TPP_ID);
        TppStatusResponse_Bulk tppStatusResponse_bulk = new TppStatusResponse_Bulk();
        TppOverallResponse tppOverallResponse = new TppOverallResponse();
        tppOverallResponse.setStatus("Responded");
        tppStatusResponse_bulk.setTppOverallResponse(tppOverallResponse);
        when(pricingTpeClient.status_Refresh(sqeTppStatusRequest)).thenReturn(tppStatusResponse_bulk);
        List<InstanceCharacteristic> instanceCharacteristics = new ArrayList();
        final InstanceCharacteristic.ValueChangeListener DO_NOTHING_LISTENER = new InstanceCharacteristic.ValueChangeListener() {
            @Override
            public void valueChanged(InstanceCharacteristic instanceCharacteristic, InstanceCharacteristicValue oldValue, InstanceCharacteristicValue newValue) {
                //do nothing.
            }
        };
        Behaviour behaviour = new Behaviour(null, null, null, ProductOffering.SPECIAL_BID_ID_ATTRIBUTE_NAME);

        InstanceCharacteristic instanceCharacteristic = new InstanceCharacteristic(
            new Attribute(new AttributeName(ProductOffering.SPECIAL_BID_ID_ATTRIBUTE_NAME),
                          null,
                          newArrayList(behaviour),
                          null,
                          AttributeDataType.STRING,
                          DefaultValue.NOT_SET,
                          false,
                          AttributeOwner.Offering,
                          new ArrayList<RuleAttributeSource>()),
            DO_NOTHING_LISTENER);

        instanceCharacteristic.setValue(TPP_ID);
        instanceCharacteristics.add(instanceCharacteristic);
        when(orderInstance.getInstanceCharacteristics()).thenReturn(instanceCharacteristics);
        when(orderInstance.getQuoteOptionId()).thenReturn(QUOTE_OPTION_ID);
        when(orderInstance.hasInstanceCharacteristic(new AttributeName(ProductOffering.SPECIAL_BID_ID_ATTRIBUTE_NAME))).thenReturn(true);
        when(productInstanceClient.get(new LineItemId(LINE_ITEM_ID))).thenReturn(orderInstance);
        final AssetDTO asset = AssetDTOFixture.anAsset().withAssetCharacteristic(ProductOffering.SPECIAL_BID_ATTRIBUTE_INDICATOR, "Yes").build();
        final AssetDTO childAsset = AssetDTOFixture.anAsset().withAssetCharacteristic(ProductOffering.SPECIAL_BID_ATTRIBUTE_INDICATOR, "Yes").build();
        asset.addChild(childAsset);
        when(productInstanceClient.getAssetDTO(new LineItemId(LINE_ITEM_ID))).thenReturn(asset);
        when(productInstanceClient.convertAssetToLightweightInstance(any(AssetDTO.class))).thenReturn(orderInstance);

        orderDTO.status = OrderStatus.CREATED.getValue();
        when(quoteOptionOrdersOrchestrator.getOrder(PROJECT_ID, QUOTE_OPTION_ID, ORDER_ID)).thenReturn(orderDTO);

        when(quoteOptionOrdersOrchestrator.buildOrder("ORDER_NAME", PROJECT_ID, QUOTE_OPTION_ID, LINE_ITEM_ID)).thenReturn(orderDTO);

        sqeTppStatusChange.setAction(PricingStatus.ACTIVATE.getSubStatus());
        sqeTppStatusChange.setSQE_Unique_Id(orderInstance.getProductInstanceId() + "_" + orderInstance.getProductInstanceVersion());
        StatusChangeResponse statusChangeResponse =  new StatusChangeResponse();
        when(pricingTpeClient.change_Status(sqeTppStatusChange)).thenReturn(statusChangeResponse);

        resourceHandler = new OrderResourceHandler(presenter, productInstanceClient, tpeStatusManager, quoteOptionOrdersOrchestrator,
                                                   quoteOptionPricingOrchestrator, projectEngineWebConfig, expedioUserContextResolver,
                                                   userResource, cloneToClient, cleanOrderValidationResourceClient, capabilityProvider, futureProductInstanceFacade, executorService, pmrClient,expedioProjectsResource,siteResourceClient, bundleProductValidator);
        resourceHandler.createOrder(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, "ORDER_NAME", LINE_ITEM_ID, USER_TOKEN);
        verify(pricingTpeClient, times(2)).change_Status(sqeTppStatusChange);
        verify(quoteOptionItemResource, times(2)).putTpeRequest(Matchers.<TpeRequestDTO>any());
    }

    @Test
    public void shouldSkipCallToTpeChangeStatusWithActivateWhenWhenBidIsInCommittedState() {
        SQETppStatusRequest sqeTppStatusRequest = new SQETppStatusRequest();
        sqeTppStatusRequest.setSQE_Unique_Id(QUOTE_OPTION_ID);
        sqeTppStatusRequest.setTPP_Id(TPP_ID);
        TppStatusResponse_Bulk tppStatusResponse_bulk = new TppStatusResponse_Bulk();
        TppOverallResponse tppOverallResponse = new TppOverallResponse();
        tppOverallResponse.setStatus("Committed");
        tppStatusResponse_bulk.setTppOverallResponse(tppOverallResponse);
        when(pricingTpeClient.status_Refresh(sqeTppStatusRequest)).thenReturn(tppStatusResponse_bulk);
        List<InstanceCharacteristic> instanceCharacteristics = new ArrayList();
        final InstanceCharacteristic.ValueChangeListener DO_NOTHING_LISTENER = new InstanceCharacteristic.ValueChangeListener() {
            @Override
            public void valueChanged(InstanceCharacteristic instanceCharacteristic, InstanceCharacteristicValue oldValue, InstanceCharacteristicValue newValue) {
                //do nothing.
            }
        };
        Behaviour behaviour = new Behaviour(null, null, null, ProductOffering.SPECIAL_BID_ID_ATTRIBUTE_NAME);

        InstanceCharacteristic instanceCharacteristic = new InstanceCharacteristic(
            new Attribute(new AttributeName(ProductOffering.SPECIAL_BID_ID_ATTRIBUTE_NAME),
                            null,
                            newArrayList(behaviour),
                            null,
                            AttributeDataType.STRING,
                            DefaultValue.NOT_SET,
                            false,
                            AttributeOwner.Offering,
                            new ArrayList<RuleAttributeSource>()),
            DO_NOTHING_LISTENER);

        instanceCharacteristic.setValue(TPP_ID);
        instanceCharacteristics.add(instanceCharacteristic);
        when(orderInstance.getInstanceCharacteristics()).thenReturn(instanceCharacteristics);
        when(orderInstance.getQuoteOptionId()).thenReturn(QUOTE_OPTION_ID);
        when(orderInstance.hasInstanceCharacteristic(new AttributeName(ProductOffering.SPECIAL_BID_ID_ATTRIBUTE_NAME))).thenReturn(true);
        when(productInstanceClient.get(new LineItemId(LINE_ITEM_ID))).thenReturn(orderInstance);

        orderDTO.status = OrderStatus.CREATED.getValue();
        when(quoteOptionOrdersOrchestrator.getOrder(PROJECT_ID, QUOTE_OPTION_ID, ORDER_ID)).thenReturn(orderDTO);

        when(quoteOptionOrdersOrchestrator.buildOrder("ORDER_NAME", PROJECT_ID, QUOTE_OPTION_ID, LINE_ITEM_ID)).thenReturn(orderDTO);

        sqeTppStatusChange.setAction(PricingStatus.ACTIVATE.getSubStatus());
        sqeTppStatusChange.setSQE_Unique_Id(orderInstance.getProductInstanceId() + "_" + orderInstance.getProductInstanceVersion());
        StatusChangeResponse statusChangeResponse =  new StatusChangeResponse();
        when(pricingTpeClient.change_Status(sqeTppStatusChange)).thenReturn(statusChangeResponse);

        resourceHandler = new OrderResourceHandler(presenter, productInstanceClient, tpeStatusManager, quoteOptionOrdersOrchestrator,
                                                   quoteOptionPricingOrchestrator, projectEngineWebConfig, expedioUserContextResolver,
                                                   userResource, cloneToClient, cleanOrderValidationResourceClient, capabilityProvider, futureProductInstanceFacade, executorService, pmrClient, expedioProjectsResource,siteResourceClient,bundleProductValidator);
        resourceHandler.createOrder(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, "ORDER_NAME", LINE_ITEM_ID, USER_TOKEN);
        verify(pricingTpeClient, never()).change_Status(sqeTppStatusChange);
        verify(quoteOptionItemResource, never()).putTpeRequest(Matchers.<TpeRequestDTO>any());
    }

    @Test
    public void shouldSkipCallToTpeChangeStatusWithActivateWhenWhenBidIsInWonState() {
        SQETppStatusRequest sqeTppStatusRequest = new SQETppStatusRequest();
        sqeTppStatusRequest.setSQE_Unique_Id(QUOTE_OPTION_ID);
        sqeTppStatusRequest.setTPP_Id(TPP_ID);
        TppStatusResponse_Bulk tppStatusResponse_bulk = new TppStatusResponse_Bulk();
        TppOverallResponse tppOverallResponse = new TppOverallResponse();
        tppOverallResponse.setStatus("Won");
        tppStatusResponse_bulk.setTppOverallResponse(tppOverallResponse);
        when(pricingTpeClient.status_Refresh(sqeTppStatusRequest)).thenReturn(tppStatusResponse_bulk);
        List<InstanceCharacteristic> instanceCharacteristics = new ArrayList();
        final InstanceCharacteristic.ValueChangeListener DO_NOTHING_LISTENER = new InstanceCharacteristic.ValueChangeListener() {
            @Override
            public void valueChanged(InstanceCharacteristic instanceCharacteristic, InstanceCharacteristicValue oldValue, InstanceCharacteristicValue newValue) {
                //do nothing.
            }
        };
        Behaviour behaviour = new Behaviour(null, null, null, ProductOffering.SPECIAL_BID_ID_ATTRIBUTE_NAME);

        InstanceCharacteristic instanceCharacteristic = new InstanceCharacteristic(
            new Attribute(new AttributeName(ProductOffering.SPECIAL_BID_ID_ATTRIBUTE_NAME),
                            null,
                            newArrayList(behaviour),
                            null,
                            AttributeDataType.STRING,
                            DefaultValue.NOT_SET,
                            false,
                            AttributeOwner.Offering,
                            new ArrayList<RuleAttributeSource>()),
            DO_NOTHING_LISTENER);

        instanceCharacteristic.setValue(TPP_ID);
        instanceCharacteristics.add(instanceCharacteristic);
        when(orderInstance.getInstanceCharacteristics()).thenReturn(instanceCharacteristics);
        when(orderInstance.getQuoteOptionId()).thenReturn(QUOTE_OPTION_ID);
        when(orderInstance.hasInstanceCharacteristic(new AttributeName(ProductOffering.SPECIAL_BID_ID_ATTRIBUTE_NAME))).thenReturn(true);
        when(productInstanceClient.get(new LineItemId(LINE_ITEM_ID))).thenReturn(orderInstance);

        orderDTO.status = OrderStatus.CREATED.getValue();
        when(quoteOptionOrdersOrchestrator.getOrder(PROJECT_ID, QUOTE_OPTION_ID, ORDER_ID)).thenReturn(orderDTO);

        when(quoteOptionOrdersOrchestrator.buildOrder("ORDER_NAME", PROJECT_ID, QUOTE_OPTION_ID, LINE_ITEM_ID)).thenReturn(orderDTO);

        sqeTppStatusChange.setAction(PricingStatus.ACTIVATE.getSubStatus());
        sqeTppStatusChange.setSQE_Unique_Id(orderInstance.getProductInstanceId() + "_" + orderInstance.getProductInstanceVersion());
        StatusChangeResponse statusChangeResponse =  new StatusChangeResponse();
        when(pricingTpeClient.change_Status(sqeTppStatusChange)).thenReturn(statusChangeResponse);

        resourceHandler = new OrderResourceHandler(presenter, productInstanceClient, tpeStatusManager, quoteOptionOrdersOrchestrator,
                                                   quoteOptionPricingOrchestrator, projectEngineWebConfig, expedioUserContextResolver,
                                                   userResource, cloneToClient, cleanOrderValidationResourceClient, capabilityProvider, futureProductInstanceFacade, executorService, pmrClient,expedioProjectsResource,siteResourceClient,bundleProductValidator);
        resourceHandler.createOrder(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, "ORDER_NAME", LINE_ITEM_ID, USER_TOKEN);
        verify(pricingTpeClient, never()).change_Status(sqeTppStatusChange);
        verify(quoteOptionItemResource, never()).putTpeRequest(Matchers.<TpeRequestDTO>any());
    }

    @Test
    public void shouldSkipCallToTpeChangeStatusWithActivateWhenWhenBidIsInDeliveredState() {
        SQETppStatusRequest sqeTppStatusRequest = new SQETppStatusRequest();
        sqeTppStatusRequest.setSQE_Unique_Id(QUOTE_OPTION_ID);
        sqeTppStatusRequest.setTPP_Id(TPP_ID);
        TppStatusResponse_Bulk tppStatusResponse_bulk = new TppStatusResponse_Bulk();
        TppOverallResponse tppOverallResponse = new TppOverallResponse();
        tppOverallResponse.setStatus("Delivered");
        tppStatusResponse_bulk.setTppOverallResponse(tppOverallResponse);
        when(pricingTpeClient.status_Refresh(sqeTppStatusRequest)).thenReturn(tppStatusResponse_bulk);
        List<InstanceCharacteristic> instanceCharacteristics = new ArrayList();
        final InstanceCharacteristic.ValueChangeListener DO_NOTHING_LISTENER = new InstanceCharacteristic.ValueChangeListener() {
            @Override
            public void valueChanged(InstanceCharacteristic instanceCharacteristic, InstanceCharacteristicValue oldValue, InstanceCharacteristicValue newValue) {
                //do nothing.
            }
        };
        Behaviour behaviour = new Behaviour(null, null, null, ProductOffering.SPECIAL_BID_ID_ATTRIBUTE_NAME);

        InstanceCharacteristic instanceCharacteristic = new InstanceCharacteristic(
            new Attribute(new AttributeName(ProductOffering.SPECIAL_BID_ID_ATTRIBUTE_NAME),
                            null,
                            newArrayList(behaviour),
                            null,
                            AttributeDataType.STRING,
                            DefaultValue.NOT_SET,
                            false,
                            AttributeOwner.Offering,
                            new ArrayList<RuleAttributeSource>()),
            DO_NOTHING_LISTENER);

        instanceCharacteristic.setValue(TPP_ID);
        instanceCharacteristics.add(instanceCharacteristic);
        when(orderInstance.getInstanceCharacteristics()).thenReturn(instanceCharacteristics);
        when(orderInstance.getQuoteOptionId()).thenReturn(QUOTE_OPTION_ID);
        when(orderInstance.hasInstanceCharacteristic(new AttributeName(ProductOffering.SPECIAL_BID_ID_ATTRIBUTE_NAME))).thenReturn(true);
        when(productInstanceClient.get(new LineItemId(LINE_ITEM_ID))).thenReturn(orderInstance);

        orderDTO.status = OrderStatus.CREATED.getValue();
        when(quoteOptionOrdersOrchestrator.getOrder(PROJECT_ID, QUOTE_OPTION_ID, ORDER_ID)).thenReturn(orderDTO);

        when(quoteOptionOrdersOrchestrator.buildOrder("ORDER_NAME", PROJECT_ID, QUOTE_OPTION_ID, LINE_ITEM_ID)).thenReturn(orderDTO);

        sqeTppStatusChange.setAction(PricingStatus.ACTIVATE.getSubStatus());
        sqeTppStatusChange.setSQE_Unique_Id(orderInstance.getProductInstanceId() + "_" + orderInstance.getProductInstanceVersion());
        StatusChangeResponse statusChangeResponse =  new StatusChangeResponse();
        when(pricingTpeClient.change_Status(sqeTppStatusChange)).thenReturn(statusChangeResponse);

        resourceHandler = new OrderResourceHandler(presenter, productInstanceClient, tpeStatusManager, quoteOptionOrdersOrchestrator,
                                                   quoteOptionPricingOrchestrator, projectEngineWebConfig, expedioUserContextResolver,
                                                   userResource, cloneToClient, cleanOrderValidationResourceClient, capabilityProvider, futureProductInstanceFacade, executorService, pmrClient,expedioProjectsResource,siteResourceClient,bundleProductValidator);
        resourceHandler.createOrder(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, "ORDER_NAME", LINE_ITEM_ID, USER_TOKEN);
        verify(pricingTpeClient, never()).change_Status(sqeTppStatusChange);
        verify(quoteOptionItemResource, never()).putTpeRequest(Matchers.<TpeRequestDTO>any());
    }

    @Test
    public void shouldMakeCallToTpeChangeStatusWithWonWhenBidIsInCommittedState() {
        SQETppStatusRequest sqeTppStatusRequest = new SQETppStatusRequest();
        sqeTppStatusRequest.setSQE_Unique_Id(QUOTE_OPTION_ID);
        sqeTppStatusRequest.setTPP_Id(TPP_ID);
        TppStatusResponse_Bulk tppStatusResponse_bulk = new TppStatusResponse_Bulk();
        TppOverallResponse tppOverallResponse = new TppOverallResponse();
        tppOverallResponse.setStatus("Committed");
        tppStatusResponse_bulk.setTppOverallResponse(tppOverallResponse);
        when(pricingTpeClient.status_Refresh(sqeTppStatusRequest)).thenReturn(tppStatusResponse_bulk);
        List<InstanceCharacteristic> instanceCharacteristics = new ArrayList();
        final InstanceCharacteristic.ValueChangeListener DO_NOTHING_LISTENER = new InstanceCharacteristic.ValueChangeListener() {
            @Override
            public void valueChanged(InstanceCharacteristic instanceCharacteristic, InstanceCharacteristicValue oldValue, InstanceCharacteristicValue newValue) {
                //do nothing.
            }
        };
        Behaviour behaviour = new Behaviour(null, null, null, ProductOffering.SPECIAL_BID_ID_ATTRIBUTE_NAME);
        InstanceCharacteristic instanceCharacteristic = new InstanceCharacteristic(
            new Attribute(new AttributeName(ProductOffering.SPECIAL_BID_ID_ATTRIBUTE_NAME),
                            null,
                            newArrayList(behaviour),
                            null,
                            AttributeDataType.STRING,
                            DefaultValue.NOT_SET,
                            false,
                            AttributeOwner.Offering,
                            new ArrayList<RuleAttributeSource>()),
            DO_NOTHING_LISTENER);
        instanceCharacteristic.setValue(TPP_ID);
        instanceCharacteristics.add(instanceCharacteristic);
        when(orderInstance.getQuoteOptionId()).thenReturn(QUOTE_OPTION_ID);
        when(orderInstance.hasInstanceCharacteristic(new AttributeName(ProductOffering.SPECIAL_BID_ID_ATTRIBUTE_NAME))).thenReturn(true);
        when(orderInstance.getInstanceCharacteristics()).thenReturn(instanceCharacteristics);
        when(productInstanceClient.get(new LineItemId(LINE_ITEM_ID))).thenReturn(orderInstance);
        final AssetDTO asset = AssetDTOFixture.anAsset().withAssetCharacteristic(ProductOffering.SPECIAL_BID_ATTRIBUTE_INDICATOR, "Yes").build();
        when(productInstanceClient.getAssetDTO(new LineItemId(LINE_ITEM_ID))).thenReturn(asset);
        when(productInstanceClient.convertAssetToLightweightInstance(asset)).thenReturn(orderInstance);
        resourceHandler = new OrderResourceHandler(presenter, productInstanceClient, tpeStatusManager, quoteOptionOrdersOrchestrator,
                                                   quoteOptionPricingOrchestrator, projectEngineWebConfig, expedioUserContextResolver,
                                                   userResource, cloneToClient, cleanOrderValidationResourceClient, capabilityProvider, futureProductInstanceFacade, executorService, pmrClient,expedioProjectsResource,siteResourceClient,bundleProductValidator);
        resourceHandler.submitOrder(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, ORDER_ID, USER_TOKEN);
        verify(pricingTpeClient).change_Status(sqeTppStatusChange);
        verify(quoteOptionItemResource).putTpeRequest(Matchers.<TpeRequestDTO>any());
    }

    @Test
    public void shouldRecursivelyMakeCallToTpeChangeStatusWithWonWhenBidIsInCommittedState() {
        SQETppStatusRequest sqeTppStatusRequest = new SQETppStatusRequest();
        sqeTppStatusRequest.setSQE_Unique_Id(QUOTE_OPTION_ID);
        sqeTppStatusRequest.setTPP_Id(TPP_ID);
        TppStatusResponse_Bulk tppStatusResponse_bulk = new TppStatusResponse_Bulk();
        TppOverallResponse tppOverallResponse = new TppOverallResponse();
        tppOverallResponse.setStatus("Committed");
        tppStatusResponse_bulk.setTppOverallResponse(tppOverallResponse);
        when(pricingTpeClient.status_Refresh(sqeTppStatusRequest)).thenReturn(tppStatusResponse_bulk);
        List<InstanceCharacteristic> instanceCharacteristics = new ArrayList();
        final InstanceCharacteristic.ValueChangeListener DO_NOTHING_LISTENER = new InstanceCharacteristic.ValueChangeListener() {
            @Override
            public void valueChanged(InstanceCharacteristic instanceCharacteristic, InstanceCharacteristicValue oldValue, InstanceCharacteristicValue newValue) {
                //do nothing.
            }
        };
        Behaviour behaviour = new Behaviour(null, null, null, ProductOffering.SPECIAL_BID_ID_ATTRIBUTE_NAME);
        InstanceCharacteristic instanceCharacteristic = new InstanceCharacteristic(
            new Attribute(new AttributeName(ProductOffering.SPECIAL_BID_ID_ATTRIBUTE_NAME),
                          null,
                          newArrayList(behaviour),
                          null,
                          AttributeDataType.STRING,
                          DefaultValue.NOT_SET,
                          false,
                          AttributeOwner.Offering,
                          new ArrayList<RuleAttributeSource>()),
            DO_NOTHING_LISTENER);
        instanceCharacteristic.setValue(TPP_ID);
        instanceCharacteristics.add(instanceCharacteristic);
        when(orderInstance.getQuoteOptionId()).thenReturn(QUOTE_OPTION_ID);
        when(orderInstance.hasInstanceCharacteristic(new AttributeName(ProductOffering.SPECIAL_BID_ID_ATTRIBUTE_NAME))).thenReturn(true);
        when(orderInstance.getInstanceCharacteristics()).thenReturn(instanceCharacteristics);
        when(productInstanceClient.get(new LineItemId(LINE_ITEM_ID))).thenReturn(orderInstance);
        final AssetDTO asset = AssetDTOFixture.anAsset().withAssetCharacteristic(ProductOffering.SPECIAL_BID_ATTRIBUTE_INDICATOR, "Yes").build();
        final AssetDTO childAsset = AssetDTOFixture.anAsset().withAssetCharacteristic(ProductOffering.SPECIAL_BID_ATTRIBUTE_INDICATOR, "Yes").build();
        asset.addChild(childAsset);
        when(productInstanceClient.getAssetDTO(new LineItemId(LINE_ITEM_ID))).thenReturn(asset);
        when(productInstanceClient.convertAssetToLightweightInstance(any(AssetDTO.class))).thenReturn(orderInstance);
        resourceHandler = new OrderResourceHandler(presenter, productInstanceClient, tpeStatusManager, quoteOptionOrdersOrchestrator,
                                                   quoteOptionPricingOrchestrator, projectEngineWebConfig, expedioUserContextResolver,
                                                   userResource, cloneToClient, cleanOrderValidationResourceClient, capabilityProvider, futureProductInstanceFacade, executorService, pmrClient,expedioProjectsResource,siteResourceClient,bundleProductValidator);
        resourceHandler.submitOrder(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, ORDER_ID, USER_TOKEN);
        verify(pricingTpeClient, times(2)).change_Status(sqeTppStatusChange);
        verify(quoteOptionItemResource, times(2)).putTpeRequest(Matchers.<TpeRequestDTO>any());
    }

    @Test
    public void shouldSkipCallToTpeChangeStatusWithWonWhenBidIsInWonState() {
        SQETppStatusRequest sqeTppStatusRequest = new SQETppStatusRequest();
        sqeTppStatusRequest.setSQE_Unique_Id(QUOTE_OPTION_ID);
        sqeTppStatusRequest.setTPP_Id(TPP_ID);
        TppStatusResponse_Bulk tppStatusResponse_bulk = new TppStatusResponse_Bulk();
        TppOverallResponse tppOverallResponse = new TppOverallResponse();
        tppOverallResponse.setStatus("Won");
        tppStatusResponse_bulk.setTppOverallResponse(tppOverallResponse);
        when(pricingTpeClient.status_Refresh(sqeTppStatusRequest)).thenReturn(tppStatusResponse_bulk);
        List<InstanceCharacteristic> instanceCharacteristics = new ArrayList();
        final InstanceCharacteristic.ValueChangeListener DO_NOTHING_LISTENER = new InstanceCharacteristic.ValueChangeListener() {
            @Override
            public void valueChanged(InstanceCharacteristic instanceCharacteristic, InstanceCharacteristicValue oldValue, InstanceCharacteristicValue newValue) {
                //do nothing.
            }
        };
        Behaviour behaviour = new Behaviour(null, null, null, ProductOffering.SPECIAL_BID_ID_ATTRIBUTE_NAME);
        InstanceCharacteristic instanceCharacteristic = new InstanceCharacteristic(
            new Attribute(new AttributeName(ProductOffering.SPECIAL_BID_ID_ATTRIBUTE_NAME),
                            null,
                            newArrayList(behaviour),
                            null,
                            AttributeDataType.STRING,
                            DefaultValue.NOT_SET,
                            false,
                            AttributeOwner.Offering,
                            new ArrayList<RuleAttributeSource>()),
            DO_NOTHING_LISTENER);
        instanceCharacteristic.setValue(TPP_ID);
        instanceCharacteristics.add(instanceCharacteristic);
        when(orderInstance.getQuoteOptionId()).thenReturn(QUOTE_OPTION_ID);
        when(orderInstance.hasInstanceCharacteristic(new AttributeName(ProductOffering.SPECIAL_BID_ID_ATTRIBUTE_NAME))).thenReturn(true);
        when(orderInstance.getInstanceCharacteristics()).thenReturn(instanceCharacteristics);
        when(productInstanceClient.get(new LineItemId(LINE_ITEM_ID))).thenReturn(orderInstance);
        resourceHandler = new OrderResourceHandler(presenter, productInstanceClient, tpeStatusManager, quoteOptionOrdersOrchestrator,
                                                   quoteOptionPricingOrchestrator, projectEngineWebConfig, expedioUserContextResolver,
                                                   userResource, cloneToClient, cleanOrderValidationResourceClient, capabilityProvider, futureProductInstanceFacade, executorService, pmrClient,expedioProjectsResource,siteResourceClient,bundleProductValidator);
        resourceHandler.submitOrder(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, ORDER_ID, USER_TOKEN);
        verify(pricingTpeClient, never()).change_Status(sqeTppStatusChange);
        verify(quoteOptionItemResource, never()).putTpeRequest(Matchers.<TpeRequestDTO>any());
    }

    @Test
    public void shouldSkipCallToTpeChangeStatusWithWonWhenBidIsInDeliveredState() {
        SQETppStatusRequest sqeTppStatusRequest = new SQETppStatusRequest();
        sqeTppStatusRequest.setSQE_Unique_Id(QUOTE_OPTION_ID);
        sqeTppStatusRequest.setTPP_Id(TPP_ID);
        TppStatusResponse_Bulk tppStatusResponse_bulk = new TppStatusResponse_Bulk();
        TppOverallResponse tppOverallResponse = new TppOverallResponse();
        tppOverallResponse.setStatus("Delivered");
        tppStatusResponse_bulk.setTppOverallResponse(tppOverallResponse);
        when(pricingTpeClient.status_Refresh(sqeTppStatusRequest)).thenReturn(tppStatusResponse_bulk);
        List<InstanceCharacteristic> instanceCharacteristics = new ArrayList();
        final InstanceCharacteristic.ValueChangeListener DO_NOTHING_LISTENER = new InstanceCharacteristic.ValueChangeListener() {
            @Override
            public void valueChanged(InstanceCharacteristic instanceCharacteristic, InstanceCharacteristicValue oldValue, InstanceCharacteristicValue newValue) {
                //do nothing.
            }
        };
        Behaviour behaviour = new Behaviour(null, null, null, ProductOffering.SPECIAL_BID_ID_ATTRIBUTE_NAME);
        InstanceCharacteristic instanceCharacteristic = new InstanceCharacteristic(
            new Attribute(new AttributeName(ProductOffering.SPECIAL_BID_ID_ATTRIBUTE_NAME),
                            null,
                            newArrayList(behaviour),
                            null,
                            AttributeDataType.STRING,
                            DefaultValue.NOT_SET,
                            false,
                            AttributeOwner.Offering,
                            new ArrayList<RuleAttributeSource>()),
            DO_NOTHING_LISTENER);
        instanceCharacteristic.setValue(TPP_ID);
        instanceCharacteristics.add(instanceCharacteristic);
        when(orderInstance.getQuoteOptionId()).thenReturn(QUOTE_OPTION_ID);
        when(orderInstance.hasInstanceCharacteristic(new AttributeName(ProductOffering.SPECIAL_BID_ID_ATTRIBUTE_NAME))).thenReturn(true);
        when(orderInstance.getInstanceCharacteristics()).thenReturn(instanceCharacteristics);
        when(productInstanceClient.get(new LineItemId(LINE_ITEM_ID))).thenReturn(orderInstance);
        resourceHandler = new OrderResourceHandler(presenter, productInstanceClient, tpeStatusManager, quoteOptionOrdersOrchestrator,
                                                   quoteOptionPricingOrchestrator, projectEngineWebConfig, expedioUserContextResolver,
                                                   userResource, cloneToClient, cleanOrderValidationResourceClient, capabilityProvider, futureProductInstanceFacade, executorService, pmrClient,expedioProjectsResource,siteResourceClient,bundleProductValidator);
        resourceHandler.submitOrder(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, ORDER_ID, USER_TOKEN);
        verify(pricingTpeClient, never()).change_Status(sqeTppStatusChange);
        verify(quoteOptionItemResource, never()).putTpeRequest(Matchers.<TpeRequestDTO>any());
    }

    @Test
    public void shouldSendEmailAndTriggerStatusChangeCallWithSpecialBidIdToTpeOnSubmitOrderWhenOrderStatusIsSubmitted() throws Exception {
        SQETppStatusRequest sqeTppStatusRequest = new SQETppStatusRequest();
        sqeTppStatusRequest.setSQE_Unique_Id(QUOTE_OPTION_ID);
        sqeTppStatusRequest.setTPP_Id(TPP_ID);
        TppStatusResponse_Bulk tppStatusResponse_bulk = new TppStatusResponse_Bulk();
        TppOverallResponse tppOverallResponse = new TppOverallResponse();
        tppOverallResponse.setStatus("Committed");
        tppStatusResponse_bulk.setTppOverallResponse(tppOverallResponse);
        when(pricingTpeClient.status_Refresh(sqeTppStatusRequest)).thenReturn(tppStatusResponse_bulk);
        List<InstanceCharacteristic> instanceCharacteristics = new ArrayList();
        final InstanceCharacteristic.ValueChangeListener DO_NOTHING_LISTENER = new InstanceCharacteristic.ValueChangeListener() {
            @Override
            public void valueChanged(InstanceCharacteristic instanceCharacteristic, InstanceCharacteristicValue oldValue, InstanceCharacteristicValue newValue) {
                //do nothing.
            }
        };
        Behaviour behaviour = new Behaviour(null, null, null, ProductOffering.SPECIAL_BID_ID_ATTRIBUTE_NAME);
        InstanceCharacteristic instanceCharacteristic = new InstanceCharacteristic(
            new Attribute(new AttributeName(ProductOffering.SPECIAL_BID_ID_ATTRIBUTE_NAME),
                            null,
                            newArrayList(behaviour),
                            null,
                            AttributeDataType.STRING,
                            DefaultValue.NOT_SET,
                            false,
                            AttributeOwner.Offering,
                            new ArrayList<RuleAttributeSource>()),
            DO_NOTHING_LISTENER);
        instanceCharacteristic.setValue(TPP_ID);
        instanceCharacteristics.add(instanceCharacteristic);
        when(orderInstance.getQuoteOptionId()).thenReturn(QUOTE_OPTION_ID);
        when(orderInstance.hasInstanceCharacteristic(new AttributeName(ProductOffering.SPECIAL_BID_ID_ATTRIBUTE_NAME))).thenReturn(true);
        when(orderInstance.getInstanceCharacteristics()).thenReturn(instanceCharacteristics);
        when(productInstanceClient.get(new LineItemId(LINE_ITEM_ID))).thenReturn(orderInstance);
        final AssetDTO asset = AssetDTOFixture.anAsset().withAssetCharacteristic(ProductOffering.SPECIAL_BID_ATTRIBUTE_INDICATOR, "Yes").build();
        when(productInstanceClient.getAssetDTO(new LineItemId(LINE_ITEM_ID))).thenReturn(asset);
        when(productInstanceClient.convertAssetToLightweightInstance(asset)).thenReturn(orderInstance);
        resourceHandler = new OrderResourceHandler(presenter, productInstanceClient, tpeStatusManager, quoteOptionOrdersOrchestrator,
                                                   quoteOptionPricingOrchestrator, projectEngineWebConfig, expedioUserContextResolver,
                                                   userResource, cloneToClient, cleanOrderValidationResourceClient, capabilityProvider, futureProductInstanceFacade, executorService, pmrClient,expedioProjectsResource,siteResourceClient,bundleProductValidator);
        resourceHandler.submitOrder(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, ORDER_ID, USER_TOKEN);
        verify(quoteOptionOrdersOrchestrator).sendOrderSubmissionEmail(PROJECT_ID, QUOTE_OPTION_ID, ORDER_ID, userDTO, orderDTO.status);
        verify(pricingTpeClient).change_Status(sqeTppStatusChange);
        verify(quoteOptionItemResource).putTpeRequest(Matchers.<TpeRequestDTO>any());
    }

    @Test
    public void shouldSendEmailAndTriggerStatusChangeCallWithCpeSpecialBidIdToTpeOnSubmitOrderWhenOrderStatusIsSubmitted() throws Exception {
        SQETppStatusRequest sqeTppStatusRequest = new SQETppStatusRequest();
        sqeTppStatusRequest.setSQE_Unique_Id(QUOTE_OPTION_ID);
        sqeTppStatusRequest.setTPP_Id(TPP_ID);
        TppStatusResponse_Bulk tppStatusResponse_bulk = new TppStatusResponse_Bulk();
        TppOverallResponse tppOverallResponse = new TppOverallResponse();
        tppOverallResponse.setStatus("Committed");
        tppStatusResponse_bulk.setTppOverallResponse(tppOverallResponse);
        when(pricingTpeClient.status_Refresh(sqeTppStatusRequest)).thenReturn(tppStatusResponse_bulk);
        List<InstanceCharacteristic> instanceCharacteristics = new ArrayList();
        final InstanceCharacteristic.ValueChangeListener DO_NOTHING_LISTENER = new InstanceCharacteristic.ValueChangeListener() {
            @Override
            public void valueChanged(InstanceCharacteristic instanceCharacteristic, InstanceCharacteristicValue oldValue, InstanceCharacteristicValue newValue) {
                //do nothing.
            }
        };
        Behaviour behaviour = new Behaviour(null, null, null, ProductOffering.CPE_SPECIAL_BID_ID_ATTRIBUTE_NAME);
        InstanceCharacteristic instanceCharacteristic = new InstanceCharacteristic(
            new Attribute(new AttributeName(ProductOffering.CPE_SPECIAL_BID_ID_ATTRIBUTE_NAME),
                            null,
                            newArrayList(behaviour),
                            null,
                            AttributeDataType.STRING,
                            DefaultValue.NOT_SET,
                            false,
                            AttributeOwner.Offering,
                            new ArrayList<RuleAttributeSource>()),
            DO_NOTHING_LISTENER);
        instanceCharacteristic.setValue(TPP_ID);
        instanceCharacteristics.add(instanceCharacteristic);
        when(orderInstance.getQuoteOptionId()).thenReturn(QUOTE_OPTION_ID);
        when(orderInstance.hasInstanceCharacteristic(new AttributeName(ProductOffering.CPE_SPECIAL_BID_ID_ATTRIBUTE_NAME))).thenReturn(true);
        when(orderInstance.getInstanceCharacteristics()).thenReturn(instanceCharacteristics);
        when(orderInstance.isCpe()).thenReturn(true);
        when(orderInstance.getInstanceCharacteristic(ProductOffering.CPE_SPECIAL_BID_ID_ATTRIBUTE_NAME)).thenReturn(instanceCharacteristic);
        when(productInstanceClient.get(new LineItemId(LINE_ITEM_ID))).thenReturn(orderInstance);
        final AssetDTO asset = AssetDTOFixture.anAsset().withAssetCharacteristic(ProductOffering.SPECIAL_BID_ATTRIBUTE_INDICATOR, "Yes").build();
        when(productInstanceClient.getAssetDTO(new LineItemId(LINE_ITEM_ID))).thenReturn(asset);
        when(productInstanceClient.convertAssetToLightweightInstance(asset)).thenReturn(orderInstance);
        resourceHandler = new OrderResourceHandler(presenter, productInstanceClient, tpeStatusManager, quoteOptionOrdersOrchestrator,
                                                   quoteOptionPricingOrchestrator, projectEngineWebConfig, expedioUserContextResolver,
                                                   userResource, cloneToClient, cleanOrderValidationResourceClient, capabilityProvider, futureProductInstanceFacade, executorService, pmrClient,expedioProjectsResource,siteResourceClient, bundleProductValidator);
        resourceHandler.submitOrder(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, ORDER_ID, USER_TOKEN);
        verify(quoteOptionOrdersOrchestrator).sendOrderSubmissionEmail(PROJECT_ID, QUOTE_OPTION_ID, ORDER_ID, userDTO, orderDTO.status);
        verify(pricingTpeClient).change_Status(sqeTppStatusChange);
        verify(quoteOptionItemResource).putTpeRequest(Matchers.<TpeRequestDTO>any());
    }

    @Test
    public void shouldSendEmailAndNotTriggerStatusChangeCallToTpeOnSubmitOrderWhenOrderIsNotSpecialBid() throws Exception {
        when(pricingTpeClient.change_Status(sqeTppStatusChange)).thenReturn(new StatusChangeResponse());
        when(orderInstance.isSpecialBid()).thenReturn(false);
        when(productInstanceClient.get(new LineItemId(LINE_ITEM_ID))).thenReturn(orderInstance);
        final AssetDTO asset = AssetDTOFixture.anAsset().withAssetCharacteristic(ProductOffering.SPECIAL_BID_ATTRIBUTE_INDICATOR, "Yes").build();
        when(productInstanceClient.getAssetDTO(new LineItemId(LINE_ITEM_ID))).thenReturn(asset);
        when(productInstanceClient.convertAssetToLightweightInstance(asset)).thenReturn(orderInstance);
        resourceHandler = new OrderResourceHandler(presenter, productInstanceClient, tpeStatusManager, quoteOptionOrdersOrchestrator,
                                                   quoteOptionPricingOrchestrator, projectEngineWebConfig, expedioUserContextResolver,
                                                   userResource, cloneToClient, cleanOrderValidationResourceClient, capabilityProvider, futureProductInstanceFacade, executorService, pmrClient,expedioProjectsResource,siteResourceClient, bundleProductValidator);
        resourceHandler.submitOrder(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, ORDER_ID, USER_TOKEN);
        verify(quoteOptionOrdersOrchestrator).sendOrderSubmissionEmail(PROJECT_ID, QUOTE_OPTION_ID, ORDER_ID, userDTO, orderDTO.status);
        verify(pricingTpeClient, never()).change_Status(any(SQETppStatusChange.class));
        verify(quoteOptionItemResource, never()).putTpeRequest(Matchers.<TpeRequestDTO>any());
    }

    @Test
    public void shouldNotSendEmailAndNotTriggerStatusChangeCallToTpeOnSubmitOrderWhenOrderStatusIsNotSubmitted() throws Exception {
        orderDTO.status = OrderStatus.CREATED.getValue();
        when(pricingTpeClient.change_Status(sqeTppStatusChange)).thenReturn(new StatusChangeResponse());
        when(orderInstance.isSpecialBid()).thenReturn(true);
        when(productInstanceClient.get(new LineItemId(LINE_ITEM_ID))).thenReturn(orderInstance);
        resourceHandler = new OrderResourceHandler(presenter, productInstanceClient, tpeStatusManager, quoteOptionOrdersOrchestrator,
                                                   quoteOptionPricingOrchestrator, projectEngineWebConfig, expedioUserContextResolver,
                                                   userResource, cloneToClient, cleanOrderValidationResourceClient, capabilityProvider, futureProductInstanceFacade, executorService, pmrClient,expedioProjectsResource,siteResourceClient, bundleProductValidator);
        resourceHandler.submitOrder(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, ORDER_ID, USER_TOKEN);
        verify(quoteOptionOrdersOrchestrator, never()).sendOrderSubmissionEmail(any(String.class), any(String.class), any(String.class), any(UserDTO.class), any(String.class));
        verify(pricingTpeClient, never()).change_Status(any(SQETppStatusChange.class));
        verify(quoteOptionItemResource, never()).putTpeRequest(Matchers.<TpeRequestDTO>any());
    }

    @Test
    public void shouldExerciseCloneToRelationshipsBeforeSubmittingOrder() throws Exception {
        resourceHandler = new OrderResourceHandler(presenter,
                                                   productInstanceClient,
                                                   tpeStatusManager,
                                                   quoteOptionOrdersOrchestrator,
                                                   quoteOptionPricingOrchestrator,
                                                   projectEngineWebConfig,
                                                   expedioUserContextResolver,
                                                   userResource,
                                                   cloneToClient,
                                                   cleanOrderValidationResourceClient,
                                                   capabilityProvider,
                                                   futureProductInstanceFacade,
                                                   executorService, pmrClient,expedioProjectsResource,siteResourceClient, bundleProductValidator);

        resourceHandler.submitOrder(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, ORDER_ID, USER_TOKEN);

        verify(cloneToClient).exerciseCloneToRelationships(LINE_ITEM_ID);
    }

    @Test
    public void shouldUpdateOrderStatusToInProgressBeforeSubmittingOrder() throws Exception {
        resourceHandler = new OrderResourceHandler(presenter,
                                                   productInstanceClient,
                                                   tpeStatusManager,
                                                   quoteOptionOrdersOrchestrator,
                                                   quoteOptionPricingOrchestrator,
                                                   projectEngineWebConfig,
                                                   expedioUserContextResolver,
                                                   userResource,
                                                   cloneToClient,
                                                   cleanOrderValidationResourceClient,
                                                   capabilityProvider,
                                                   futureProductInstanceFacade,
                                                   executorService, pmrClient,expedioProjectsResource,siteResourceClient, bundleProductValidator);

        resourceHandler.submitOrder(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, ORDER_ID, USER_TOKEN);

        verify(quoteOptionOrdersOrchestrator, times(1)).updateOrderStatus(PROJECT_ID, QUOTE_OPTION_ID, ORDER_ID, OrderItemStatus.IN_PROGRESS);
    }

    @Test
    public void shouldUpdateOrderStatusToOrderSubmissionFailedUponAnyException() throws Exception {
        resourceHandler = new OrderResourceHandler(presenter,
                                                   productInstanceClient,
                                                   tpeStatusManager,
                                                   quoteOptionOrdersOrchestrator,
                                                   quoteOptionPricingOrchestrator,
                                                   projectEngineWebConfig,
                                                   expedioUserContextResolver,
                                                   userResource,
                                                   cloneToClient,
                                                   cleanOrderValidationResourceClient,
                                                   capabilityProvider,
                                                   futureProductInstanceFacade,
                                                   executorService, pmrClient,expedioProjectsResource,siteResourceClient, bundleProductValidator);

        when(quoteOptionOrdersOrchestrator.getOrder(PROJECT_ID, QUOTE_OPTION_ID, ORDER_ID)).thenThrow(new RuntimeException());

        resourceHandler.submitOrder(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, ORDER_ID, USER_TOKEN);

        verify(quoteOptionOrdersOrchestrator).updateOrderStatus(PROJECT_ID, QUOTE_OPTION_ID, ORDER_ID, OrderItemStatus.ORDER_SUBMISSION_FAILED);
    }

    @Test
    public void shouldNotSendCleanOrderValidationCallDuringMigrationQuote() throws Exception {
        resourceHandler = new OrderResourceHandler(presenter,
                                                   productInstanceClient,
                                                   tpeStatusManager,
                                                   quoteOptionOrdersOrchestrator,
                                                   quoteOptionPricingOrchestrator,
                                                   projectEngineWebConfig,
                                                   expedioUserContextResolver,
                                                   userResource,
                                                   cloneToClient,
                                                   cleanOrderValidationResourceClient,
                                                   capabilityProvider,
                                                   futureProductInstanceFacade,
                                                   executorService, pmrClient,expedioProjectsResource,siteResourceClient, bundleProductValidator);

        when(futureProductInstanceFacade.getSiteId(new LineItemId(orderItem.id))).thenReturn("1");
        when(futureProductInstanceFacade.getSiteId(new LineItemId(mopOrderItem.id))).thenReturn("2");
        when(capabilityProvider.isFunctionalityEnabled(IS_CLEAN_ORDER_VALIDATION_ENABLED, false, Optional.of(QUOTE_OPTION_ID))).thenReturn(true);
        when(quoteOptionOrdersOrchestrator.isMigrationQuote(PROJECT_ID, QUOTE_OPTION_ID)).thenReturn(true);

        resourceHandler.submitOrder(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, ORDER_ID, USER_TOKEN);

        verify(cleanOrderValidationResourceClient, never()).validateExpedioAccount(Integer.parseInt("1"), Integer.parseInt("1"), UserType.DIRECT.properCase(), PROJECT_ID);
        verify(quoteOptionOrdersOrchestrator, never()).sendOrderSubmissionFailedEmail(PROJECT_ID,
                                                                                      QUOTE_OPTION_ID,
                                                                                      ORDER_ID,
                                                                                      userDTO,
                                                                                      "\nBFG Site Id: 1\n" +
                                                                                      "Billing Account Id: 1\n" +
                                                                                      "Error: com.bt.rsqe.cleanordervalidation.CleanOrderValidationException: COV Error\n");
    }

    @Test
    public void shouldUpdateOrderStatusToOrderSubmissionFailedUponAnyCleanOrderValidationFailure() throws Exception {
        resourceHandler = new OrderResourceHandler(presenter,
                                                   productInstanceClient,
                                                   tpeStatusManager,
                                                   quoteOptionOrdersOrchestrator,
                                                   quoteOptionPricingOrchestrator,
                                                   projectEngineWebConfig,
                                                   expedioUserContextResolver,
                                                   userResource,
                                                   cloneToClient,
                                                   cleanOrderValidationResourceClient,
                                                   capabilityProvider,
                                                   futureProductInstanceFacade,
                                                   executorService, pmrClient,expedioProjectsResource,siteResourceClient, bundleProductValidator);
        when(futureProductInstanceFacade.getSiteId(new LineItemId(orderItem.id))).thenReturn("1");
        when(futureProductInstanceFacade.getSiteId(new LineItemId(mopOrderItem.id))).thenReturn("2");
        when(capabilityProvider.isFunctionalityEnabled(IS_CLEAN_ORDER_VALIDATION_ENABLED, false, Optional.of(QUOTE_OPTION_ID))).thenReturn(true);
        doThrow(new CleanOrderValidationException("COV Error")).when(cleanOrderValidationResourceClient).validateExpedioAccount(Integer.parseInt("1"), Integer.parseInt("1"), UserType.DIRECT.properCase(), PROJECT_ID);

        resourceHandler.submitOrder(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, ORDER_ID, USER_TOKEN);

        verify(quoteOptionOrdersOrchestrator).sendOrderSubmissionFailedEmail(PROJECT_ID,
                                                                             QUOTE_OPTION_ID,
                                                                             ORDER_ID,
                                                                             userDTO,
                                                                             "\nBFG Site Id: 1\n" +
                                                                             "Billing Account Id: 1\n" +
                                                                             "Error: com.bt.rsqe.cleanordervalidation.CleanOrderValidationException: COV Error\n");
        verify(quoteOptionOrdersOrchestrator).updateOrderStatus(PROJECT_ID, QUOTE_OPTION_ID, ORDER_ID, OrderItemStatus.ORDER_SUBMISSION_FAILED);
    }

    @Test
    public void shouldExemptMopOrderItemSiteFromCleanOrderValidation() throws Exception {
        resourceHandler = new OrderResourceHandler(presenter,
                                                   productInstanceClient,
                                                   tpeStatusManager,
                                                   quoteOptionOrdersOrchestrator,
                                                   quoteOptionPricingOrchestrator,
                                                   projectEngineWebConfig,
                                                   expedioUserContextResolver,
                                                   userResource,
                                                   cloneToClient,
                                                   cleanOrderValidationResourceClient,
                                                   capabilityProvider,
                                                   futureProductInstanceFacade,
                                                   executorService, pmrClient,expedioProjectsResource,siteResourceClient, bundleProductValidator);

        when(pmrClient.mopRequiringProductCodes()).thenReturn(newArrayList(MOP_S_CODE));

        when(futureProductInstanceFacade.getSiteId(new LineItemId(orderItem.id))).thenReturn("1");
        when(futureProductInstanceFacade.getSiteId(new LineItemId(mopOrderItem.id))).thenReturn("2");

        when(capabilityProvider.isFunctionalityEnabled(IS_CLEAN_ORDER_VALIDATION_ENABLED, false, Optional.of(QUOTE_OPTION_ID))).thenReturn(true);

        resourceHandler.submitOrder(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, ORDER_ID, USER_TOKEN);

        verify(futureProductInstanceFacade, times(1)).getSiteId(new LineItemId(orderItem.id));
        verify(futureProductInstanceFacade, never()).getSiteId(new LineItemId(mopOrderItem.id));

        verify(cleanOrderValidationResourceClient, times(1)).validateExpedioAccount(Integer.parseInt("1"), Integer.parseInt("1"), UserType.DIRECT.properCase(), PROJECT_ID);
        verify(cleanOrderValidationResourceClient, never()).validateExpedioAccount(Integer.parseInt("2"), Integer.parseInt("1"), UserType.DIRECT.properCase(), PROJECT_ID);
    }

    @Test
    public void shouldExemptCeasedOrderItemSiteFromCleanOrderValidation() throws Exception {
        resourceHandler = new OrderResourceHandler(presenter,
                                                   productInstanceClient,
                                                   tpeStatusManager,
                                                   quoteOptionOrdersOrchestrator,
                                                   quoteOptionPricingOrchestrator,
                                                   projectEngineWebConfig,
                                                   expedioUserContextResolver,
                                                   userResource,
                                                   cloneToClient,
                                                   cleanOrderValidationResourceClient,
                                                   capabilityProvider,
                                                   futureProductInstanceFacade,
                                                   executorService, pmrClient,expedioProjectsResource,siteResourceClient, bundleProductValidator);
        when(pmrClient.mopRequiringProductCodes()).thenReturn(newArrayList(MOP_S_CODE));

        when(futureProductInstanceFacade.getSiteId(new LineItemId(orderItem.id))).thenReturn("1");
        when(futureProductInstanceFacade.getSiteId(new LineItemId(ceasedOrderItem.id))).thenReturn("3");
        when(futureProductInstanceFacade.getSiteId(new LineItemId(mopOrderItem.id))).thenReturn("2");
        when(futureProductInstanceFacade.isCeased(new LineItemId(orderItem.id))).thenReturn(false);
        when(futureProductInstanceFacade.isCeased(new LineItemId(ceasedOrderItem.id))).thenReturn(true);
        when(futureProductInstanceFacade.isCeased(new LineItemId(mopOrderItem.id))).thenReturn(false);

        when(capabilityProvider.isFunctionalityEnabled(IS_CLEAN_ORDER_VALIDATION_ENABLED, false, Optional.of(QUOTE_OPTION_ID))).thenReturn(true);

        resourceHandler.submitOrder(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, ORDER_ID, USER_TOKEN);

        verify(futureProductInstanceFacade, times(1)).getSiteId(new LineItemId(orderItem.id));
        verify(futureProductInstanceFacade, times(1)).getSiteId(new LineItemId(ceasedOrderItem.id));
        verify(futureProductInstanceFacade, never()).getSiteId(new LineItemId(mopOrderItem.id));
        verify(futureProductInstanceFacade, times(1)).isCeased(new LineItemId(orderItem.id));
        verify(futureProductInstanceFacade, times(1)).isCeased(new LineItemId(ceasedOrderItem.id));
        verify(futureProductInstanceFacade, never()).isCeased(new LineItemId(mopOrderItem.id));

        verify(cleanOrderValidationResourceClient, times(1)).validateExpedioAccount(Integer.parseInt("1"), Integer.parseInt("1"), UserType.DIRECT.properCase(), PROJECT_ID);
        verify(cleanOrderValidationResourceClient, never()).validateExpedioAccount(Integer.parseInt("2"), Integer.parseInt("1"), UserType.DIRECT.properCase(), PROJECT_ID);
        verify(cleanOrderValidationResourceClient, never()).validateExpedioAccount(Integer.parseInt("3"), Integer.parseInt("3"), UserType.DIRECT.properCase(), PROJECT_ID);
    }

    @Test
    public void shouldNotSendOrderSubmissionEmailNotificationWhenMopOrderItemInvolved() throws Exception {
        //Given
        resourceHandler = new OrderResourceHandler(presenter,
                                                   productInstanceClient,
                                                   tpeStatusManager,
                                                   quoteOptionOrdersOrchestrator,
                                                   quoteOptionPricingOrchestrator,
                                                   projectEngineWebConfig,
                                                   expedioUserContextResolver,
                                                   userResource,
                                                   cloneToClient,
                                                   cleanOrderValidationResourceClient,
                                                   capabilityProvider,
                                                   futureProductInstanceFacade,
                                                   executorService, pmrClient,expedioProjectsResource,siteResourceClient, bundleProductValidator);

        when(pmrClient.mopRequiringProductCodes()).thenReturn(newArrayList(MOP_S_CODE));

        //When
        resourceHandler.submitOrder(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, ORDER_ID, USER_TOKEN);

        //Then
        verify(quoteOptionOrdersOrchestrator, times(0)).sendOrderSubmissionEmail(eq(PROJECT_ID), eq(QUOTE_OPTION_ID), eq(ORDER_ID), eq(userDTO), eq(OrderStatus.SUBMITTED.getValue()));
    }

    @Test
    public void shouldSendOrderSubmissionEmailNotificationWhenMopOrderItemNotInvolved() throws Exception {
        //Given
        resourceHandler = new OrderResourceHandler(presenter,
                                                   productInstanceClient,
                                                   tpeStatusManager,
                                                   quoteOptionOrdersOrchestrator,
                                                   quoteOptionPricingOrchestrator,
                                                   projectEngineWebConfig,
                                                   expedioUserContextResolver,
                                                   userResource,
                                                   cloneToClient,
                                                   cleanOrderValidationResourceClient,
                                                   capabilityProvider,
                                                   futureProductInstanceFacade,
                                                   executorService, pmrClient,expedioProjectsResource,siteResourceClient, bundleProductValidator);

        when(pmrClient.mopRequiringProductCodes()).thenReturn(newArrayList(MOP_S_CODE));
        orderDTO = OrderDTO.newInstance("name", "created", newArrayList(orderItem));
        orderDTO.status = OrderStatus.SUBMITTED.getValue();
        when(quoteOptionOrdersOrchestrator.getOrder(PROJECT_ID, QUOTE_OPTION_ID, ORDER_ID)).thenReturn(orderDTO);

        //When
        resourceHandler.submitOrder(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, ORDER_ID, USER_TOKEN);

        //Then
        verify(quoteOptionOrdersOrchestrator, times(1)).sendOrderSubmissionEmail(eq(PROJECT_ID), eq(QUOTE_OPTION_ID), eq(ORDER_ID), eq(userDTO), eq(OrderStatus.SUBMITTED.getValue()));
    }

    @Test
    public void shouldSendFailedOrderSubmissionEmailNotificationEvenMopOrderItemInvolved() throws Exception {
        //Given
        resourceHandler = new OrderResourceHandler(presenter,
                                                   productInstanceClient,
                                                   tpeStatusManager,
                                                   quoteOptionOrdersOrchestrator,
                                                   quoteOptionPricingOrchestrator,
                                                   projectEngineWebConfig,
                                                   expedioUserContextResolver,
                                                   userResource,
                                                   cloneToClient,
                                                   cleanOrderValidationResourceClient,
                                                   capabilityProvider,
                                                   futureProductInstanceFacade,
                                                   executorService, pmrClient,expedioProjectsResource,siteResourceClient, bundleProductValidator);

        when(pmrClient.mopRequiringProductCodes()).thenReturn(newArrayList(MOP_S_CODE));
        doThrow(RuntimeException.class).when(quoteOptionOrdersOrchestrator).submitOrderAndCreateAssets(PROJECT_ID, QUOTE_OPTION_ID, ORDER_ID, CUSTOMER_ID, false, LOGIN_NAME);

        //When
        resourceHandler.submitOrder(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, ORDER_ID, USER_TOKEN);

        //Then
        verify(quoteOptionOrdersOrchestrator, times(1)).sendOrderSubmissionFailedEmail(eq(PROJECT_ID), eq(QUOTE_OPTION_ID), eq(ORDER_ID), eq(userDTO), anyString());
    }

    @Test
    public void shouldNotUpdateOrderStatusToOrderSubmissionFailedUponTPEStatusCallFailure() throws Exception {
        resourceHandler = new OrderResourceHandler(presenter,
                                                   productInstanceClient,
                                                   tpeStatusManager,
                                                   quoteOptionOrdersOrchestrator,
                                                   quoteOptionPricingOrchestrator,
                                                   projectEngineWebConfig,
                                                   expedioUserContextResolver,
                                                   userResource,
                                                   cloneToClient,
                                                   cleanOrderValidationResourceClient,
                                                   capabilityProvider,
                                                   futureProductInstanceFacade,
                                                   executorService, pmrClient, expedioProjectsResource,siteResourceClient,bundleProductValidator);

        when(capabilityProvider.isFunctionalityEnabled(IS_CLEAN_ORDER_VALIDATION_ENABLED, true, Optional.of(QUOTE_OPTION_ID))).thenReturn(true);
        when(futureProductInstanceFacade.getSiteId(new LineItemId(orderItem.id))).thenReturn("1");

        AssetDTO assetDTO = mock(AssetDTO.class);
        ProductInstance productInstance = mock(ProductInstance.class);

        when(productInstanceClient.getAssetDTO(new LineItemId(anyString()))).thenReturn(assetDTO);
        when(assetDTO.isSpecialBid()).thenReturn(true);
        when(productInstanceClient.convertAssetToLightweightInstance(assetDTO)).thenReturn(productInstance);
        doThrow(new RuntimeException()).when(pricingTpeClient).status_Refresh(any(SQETppStatusRequest.class));

        resourceHandler.submitOrder(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, ORDER_ID, USER_TOKEN);

        verify(quoteOptionOrdersOrchestrator, never()).sendOrderSubmissionFailedEmail(PROJECT_ID, QUOTE_OPTION_ID, ORDER_ID, userDTO, null);
        verify(quoteOptionOrdersOrchestrator, never()).updateOrderStatus(PROJECT_ID, QUOTE_OPTION_ID, ORDER_ID, OrderItemStatus.ORDER_SUBMISSION_FAILED);
    }
    @Test
    public void shouldGetErrorResponseIfValidationFailed(){
        resourceHandler = new OrderResourceHandler(presenter,
                                                   productInstanceClient,
                                                   tpeStatusManager,
                                                   quoteOptionOrdersOrchestrator,
                                                   quoteOptionPricingOrchestrator,
                                                   projectEngineWebConfig,
                                                   expedioUserContextResolver,
                                                   userResource,
                                                   cloneToClient,
                                                   cleanOrderValidationResourceClient,
                                                   capabilityProvider,
                                                   futureProductInstanceFacade,
                                                   executorService, pmrClient,expedioProjectsResource,siteResourceClient, bundleProductValidator);
        OfferAndOrderValidationResult result = new OfferAndOrderValidationResult(false,"The discount status is invalid.");
        when(quoteOptionOrdersOrchestrator.checkValidation(anyListOf(String.class), eq(PROJECT_ID), eq(QUOTE_OPTION_ID), eq(CUSTOMER_ID), eq(CONTRACT_ID))).thenReturn(result);
        Response response = resourceHandler.createOrder(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, "", "aLineItem1,aLineItem2", "");
        assertThat(response.getEntity().toString(), is("The discount status is invalid."));
    }

}
