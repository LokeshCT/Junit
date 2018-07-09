package com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model;

import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.parameter.LengthConstrainingProductInstanceId;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerrecord.AccountManagerDTO;
import com.bt.rsqe.customerrecord.AccountManagerResource;
import com.bt.rsqe.customerrecord.CustomerDTO;
import com.bt.rsqe.customerrecord.CustomerResource;
import com.bt.rsqe.customerrecord.ExpedioClientResources;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.customerrecord.SiteResource;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.bom.fixtures.ProductOfferingFixture;
import com.bt.rsqe.domain.product.DefaultProductInstanceFixture;
import com.bt.rsqe.domain.product.PriceType;
import com.bt.rsqe.domain.product.SimpleProductOfferingType;
import com.bt.rsqe.domain.product.chargingscheme.ProductChargingScheme;
import com.bt.rsqe.domain.product.fixtures.ProductChargingSchemeFixture;
import com.bt.rsqe.domain.project.PriceLine;
import com.bt.rsqe.domain.project.PricingStatus;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.expedio.project.ExpedioProjectResource;
import com.bt.rsqe.expedio.project.ProjectDTO;
import com.bt.rsqe.pricing.PricingClient;
import com.bt.rsqe.productinstancemerge.ChangeType;
import com.bt.rsqe.productinstancemerge.MergeResult;
import com.bt.rsqe.productinstancemerge.changetracker.ChangeTracker;
import com.bt.rsqe.dto.BidManagerCommentsDTO;
import com.bt.rsqe.projectengine.BidManagerCommentsResource;
import com.bt.rsqe.projectengine.CaveatResource;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTOFixture;
import com.bt.rsqe.projectengine.QuoteOptionItemResource;
import com.bt.rsqe.projectengine.QuoteOptionResource;
import com.bt.rsqe.projectengine.TpeRequestDTO;
import com.bt.rsqe.projectengine.TpeResponseDTO;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.PricingSheetTestDataFixture;
import com.bt.rsqe.quoteengine.domain.TpeRequestType;
import com.bt.rsqe.quoteengine.domain.TpeResponseAttributeNames;
import com.bt.rsqe.security.UserContext;
import com.bt.rsqe.security.UserContextManager;
import com.bt.rsqe.web.rest.exception.ResourceNotFoundException;
import com.google.common.base.Optional;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.List;

import static com.bt.rsqe.security.UserContextBuilder.*;
import static com.google.common.collect.Lists.*;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;
import static org.mockito.Mockito.*;

public class PricingSheetDataModelFactoryTest {

    public static final String aCustomerId = "aCustomerId";
    public static final String aProjectId = "aProjectId";
    public static final String aQuoteOptionId = "quoteOptionId";
    private PricingSheetDataModelFactory pricingSheetDataModelFactory;

    @Mock
    private ExpedioClientResources expedioClientResources;

    @Mock
    private ProjectResource projectResource;

    @Mock
    private ProductInstanceClient productInstanceClient;

    @Mock
    private CaveatResource caveatResource;

    @Mock
    private CustomerResource customerResource;

    @Mock
    private AccountManagerResource accountManagerResource;

    @Mock
    private ExpedioProjectResource expedioProjectResource;

    @Mock
    private QuoteOptionResource quoteOptionResource;

    @Mock
    private SiteResource siteResource;

    @Mock
    private QuoteOptionItemResource quoteOptionItemResource;

    private MergeResult mergeResult;
    @Mock
    private ChangeTracker changeQueryTracker;
    @Mock
    private PricingClient pricingClient;
    @Mock
    private BidManagerCommentsResource bidManagerCommentsResource;
    BidManagerCommentsDTO comments;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        pricingSheetDataModelFactory = new PricingSheetDataModelFactory(expedioClientResources, projectResource, productInstanceClient, caveatResource, pricingClient);
        UserContext userContext = aDirectUserContext().withToken("aToken").build();
        UserContextManager.setCurrent(userContext);
        when(quoteOptionResource.bidManagerCommentsResource(aQuoteOptionId)).thenReturn(bidManagerCommentsResource);
        comments = new BidManagerCommentsDTO("comments","caveats", DateTime.now(),"createdBy","email");
        when(bidManagerCommentsResource.getAll()).thenReturn(newArrayList(comments));
    }

    @Test
    public void shouldCreatePricingSheetModel() {

        PricingSheetTestDataFixture pricingSheetTestDataFixture = new PricingSheetTestDataFixture();
        CustomerDTO customerDTO = pricingSheetTestDataFixture.aCustomerDTO();
        AccountManagerDTO accountManagerDTO = pricingSheetTestDataFixture.anAccountManager();
        ProjectDTO projectDTO = pricingSheetTestDataFixture.aProjectDto();
        QuoteOptionDTO quoteOptionDTO = pricingSheetTestDataFixture.aQuoteOptionDto();
        SiteDTO centralSite = pricingSheetTestDataFixture.aCentralSite();
        QuoteOptionItemDTO aQuoteOptionItemDto = QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().withId("aLineItemId").build();
        QuoteOptionItemDTO anotherQuoteOptionItemDto = QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().withId("anotherLineItemId").build();
        ProductInstance aProductInstance = pricingSheetTestDataFixture.anInstallableRootProductWithAChild();
        ProductInstance anotherProductInstnace = pricingSheetTestDataFixture.aSiteAgnosticProductInstance();
        SiteDTO siteDTO = pricingSheetTestDataFixture.aSite();


        when(productInstanceClient.get(new LineItemId("aLineItemId"))).thenReturn(aProductInstance);
        when(productInstanceClient.get(new LineItemId("anotherLineItemId"))).thenReturn(anotherProductInstnace);
        when(productInstanceClient.getSourceAsset(any(LengthConstrainingProductInstanceId.class))).thenReturn(Optional.<ProductInstance>absent());
        when(expedioClientResources.getCustomerResource()).thenReturn(customerResource);
        when(customerResource.getByToken(aCustomerId, "aToken")).thenReturn(customerDTO);
        when(customerResource.accountManagerResource(aCustomerId, aProjectId)).thenReturn(accountManagerResource);
        when(customerResource.siteResource(aCustomerId)).thenReturn(siteResource);
        when(siteResource.getCentralSite(aProjectId)).thenReturn(centralSite);
        when(siteResource.get(aProductInstance.getSiteId(), aProjectId)).thenReturn(siteDTO);
        when(accountManagerResource.get()).thenReturn(accountManagerDTO);
        when(expedioClientResources.projectResource()).thenReturn(expedioProjectResource);
        when(expedioProjectResource.getProject(aProjectId)).thenReturn(projectDTO);
        when(projectResource.quoteOptionResource(aProjectId)).thenReturn(quoteOptionResource);
        when(quoteOptionResource.get()).thenReturn(newArrayList(quoteOptionDTO));
        when(quoteOptionResource.quoteOptionItemResource(aQuoteOptionId)).thenReturn(quoteOptionItemResource);
        when(quoteOptionItemResource.get()).thenReturn(newArrayList(aQuoteOptionItemDto, anotherQuoteOptionItemDto));

        //when

        mergeResult = new MergeResult(newArrayList(aProductInstance), changeQueryTracker);
        when(changeQueryTracker.changeFor(aProductInstance)).thenReturn(ChangeType.NONE);
        ProductInstance childInstance = aProductInstance.getChildren().iterator().next();
        when(changeQueryTracker.changeFor (childInstance)).thenReturn(ChangeType.ADD);
        when(productInstanceClient.getAssetsDiff(aProductInstance.getProductInstanceId().getValue(),
                                                 aProductInstance.getProductInstanceVersion(), null, null)).thenReturn(mergeResult);
        when(pricingClient.filterChargingSchemes(aProductInstance, ChangeType.ADD.getValue(), aProductInstance.getProductIdentifier().getProductId(), null, quoteOptionDTO.ifcPending)).thenReturn(aProductInstance.getChargingSchemes());
        when(pricingClient.filterChargingSchemes(childInstance, ChangeType.ADD.getValue(), childInstance.getProductIdentifier().getProductId(), "None", false)).thenReturn(childInstance.getChargingSchemes());
        when(pricingClient.filterChargingSchemes(anotherProductInstnace, ChangeType.ADD.getValue(), anotherProductInstnace.getProductIdentifier().getProductId(), null, anotherQuoteOptionItemDto.isIfc)).thenReturn(anotherProductInstnace.getChargingSchemes());
        PricingSheetProductModel productModel = new PricingSheetProductModel(siteDTO, aProductInstance, aQuoteOptionItemDto, mergeResult, caveatResource, pricingClient, null);
        mergeResult = new MergeResult(newArrayList(anotherProductInstnace), changeQueryTracker);
        when(changeQueryTracker.changeFor(anotherProductInstnace)).thenReturn(ChangeType.ADD);
        when(productInstanceClient.getAssetsDiff(anotherProductInstnace.getProductInstanceId().getValue(),
                                                 anotherProductInstnace.getProductInstanceVersion(), null, null)).thenReturn(mergeResult);
        PricingSheetDataModel pricingSheetDataModel = pricingSheetDataModelFactory.create(aCustomerId, aProjectId, aQuoteOptionId, Optional.<String>absent());
        PricingSheetProductModel anotherProductModel = new PricingSheetProductModel(siteDTO, anotherProductInstnace, anotherQuoteOptionItemDto, mergeResult, caveatResource, pricingClient, null);
        assertThat(pricingSheetDataModel.map().size(), is(20));
        assertThat(pricingSheetDataModel.getProducts(), hasItems(productModel, anotherProductModel));
        assertThat(pricingSheetDataModel.getBidDetails(), is(comments));
    }

    @Test
    public void shouldCreateOfferPricingSheetModelOnlyForThoseItemsAddedToOffer() {
        final String CURRENT_OFFER_ID = "offerId";

        PricingSheetTestDataFixture pricingSheetTestDataFixture = new PricingSheetTestDataFixture();
        CustomerDTO customerDTO = pricingSheetTestDataFixture.aCustomerDTO();
        AccountManagerDTO accountManagerDTO = pricingSheetTestDataFixture.anAccountManager();
        ProjectDTO projectDTO = pricingSheetTestDataFixture.aProjectDto();
        QuoteOptionDTO quoteOptionDTO = pricingSheetTestDataFixture.aQuoteOptionDto();
        SiteDTO centralSite = pricingSheetTestDataFixture.aCentralSite();
        QuoteOptionItemDTO aQuoteOptionItemDto = QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().withId("aLineItemId").withOfferId(CURRENT_OFFER_ID).build();
        QuoteOptionItemDTO anotherQuoteOptionItemDto = QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().withId("anotherLineItemId").withOfferId("notInSameOffer").build();
        ProductInstance aProductInstance = pricingSheetTestDataFixture.anInstallableRootProductWithAChild();
        ProductInstance anotherProductInstnace = pricingSheetTestDataFixture.aSiteAgnosticProductInstance();
        SiteDTO siteDTO = pricingSheetTestDataFixture.aSite();


        when(productInstanceClient.get(new LineItemId("aLineItemId"))).thenReturn(aProductInstance);
        when(productInstanceClient.get(new LineItemId("anotherLineItemId"))).thenReturn(anotherProductInstnace);
        when(productInstanceClient.getSourceAsset(any(LengthConstrainingProductInstanceId.class))).thenReturn(Optional.<ProductInstance>absent());
        when(expedioClientResources.getCustomerResource()).thenReturn(customerResource);
        when(customerResource.getByToken(aCustomerId, "aToken")).thenReturn(customerDTO);
        when(customerResource.accountManagerResource(aCustomerId, aProjectId)).thenReturn(accountManagerResource);
        when(customerResource.siteResource(aCustomerId)).thenReturn(siteResource);
        when(siteResource.getCentralSite(aProjectId)).thenReturn(centralSite);
        when(siteResource.get(aProductInstance.getSiteId(), aProjectId)).thenReturn(siteDTO);
        when(accountManagerResource.get()).thenReturn(accountManagerDTO);
        when(expedioClientResources.projectResource()).thenReturn(expedioProjectResource);
        when(expedioProjectResource.getProject(aProjectId)).thenReturn(projectDTO);
        when(projectResource.quoteOptionResource(aProjectId)).thenReturn(quoteOptionResource);
        when(quoteOptionResource.get()).thenReturn(newArrayList(quoteOptionDTO));
        when(quoteOptionResource.quoteOptionItemResource(aQuoteOptionId)).thenReturn(quoteOptionItemResource);
        when(quoteOptionItemResource.get()).thenReturn(newArrayList(aQuoteOptionItemDto, anotherQuoteOptionItemDto));

        //when

        mergeResult = new MergeResult(newArrayList(aProductInstance), changeQueryTracker);
        when(changeQueryTracker.changeFor(aProductInstance)).thenReturn(ChangeType.ADD);
        when(productInstanceClient.getAssetsDiff(aProductInstance.getProductInstanceId().getValue(),
                                                 aProductInstance.getProductInstanceVersion(), null, null)).thenReturn(mergeResult);
        when(pricingClient.filterChargingSchemes(aProductInstance, ChangeType.ADD.getValue(), aProductInstance.getProductIdentifier().getProductId(), null, quoteOptionDTO.ifcPending)).thenReturn(aProductInstance.getChargingSchemes());
        when(pricingClient.filterChargingSchemes(anotherProductInstnace, ChangeType.ADD.getValue(), anotherProductInstnace.getProductIdentifier().getProductId(), null, anotherQuoteOptionItemDto.isIfc)).thenReturn(anotherProductInstnace.getChargingSchemes());

        PricingSheetProductModel productModel = new PricingSheetProductModel(siteDTO, aProductInstance, aQuoteOptionItemDto, mergeResult, caveatResource, pricingClient, null);
        mergeResult = new MergeResult(newArrayList(anotherProductInstnace), changeQueryTracker);
        when(changeQueryTracker.changeFor(anotherProductInstnace)).thenReturn(ChangeType.ADD);
        when(productInstanceClient.getAssetsDiff(anotherProductInstnace.getProductInstanceId().getValue(),
                                                 anotherProductInstnace.getProductInstanceVersion(), null, null)).thenReturn(mergeResult);
        PricingSheetDataModel pricingSheetDataModel = pricingSheetDataModelFactory.create(aCustomerId, aProjectId, aQuoteOptionId, Optional.of(CURRENT_OFFER_ID));
        assertThat(pricingSheetDataModel.map().size(), is(20));
        assertThat(pricingSheetDataModel.getProducts().size(), is(1));
        assertThat(pricingSheetDataModel.getProducts(), hasItems(productModel));
    }

    @Test
    public void shouldCreatePricingSheetWhenNoAccountManagerDetailModel() {

        PricingSheetTestDataFixture pricingSheetTestDataFixture = new PricingSheetTestDataFixture();
        CustomerDTO customerDTO = pricingSheetTestDataFixture.aCustomerDTO();
        ProjectDTO projectDTO = pricingSheetTestDataFixture.aProjectDto();
        QuoteOptionDTO quoteOptionDTO = pricingSheetTestDataFixture.aQuoteOptionDto();
        SiteDTO centralSite = pricingSheetTestDataFixture.aCentralSite();
        QuoteOptionItemDTO aQuoteOptionItemDto = QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().withId("aLineItemId").build();
        QuoteOptionItemDTO anotherQuoteOptionItemDto = QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().withId("anotherLineItemId").build();
        ProductInstance aProductInstance = pricingSheetTestDataFixture.anInstallableRootProductWithAChild();
        ProductInstance anotherProductInstnace = pricingSheetTestDataFixture.aSiteAgnosticProductInstance();
        SiteDTO siteDTO = pricingSheetTestDataFixture.aSite();


        when(productInstanceClient.get(new LineItemId("aLineItemId"))).thenReturn(aProductInstance);
        when(productInstanceClient.get(new LineItemId("anotherLineItemId"))).thenReturn(anotherProductInstnace);
        when(productInstanceClient.getSourceAsset(any(LengthConstrainingProductInstanceId.class))).thenReturn(Optional.<ProductInstance>absent());
        when(expedioClientResources.getCustomerResource()).thenReturn(customerResource);
        when(customerResource.getByToken(aCustomerId, "aToken")).thenReturn(customerDTO);
        when(customerResource.accountManagerResource(aCustomerId, aProjectId)).thenReturn(accountManagerResource);
        when(customerResource.siteResource(aCustomerId)).thenReturn(siteResource);
        when(siteResource.getCentralSite(aProjectId)).thenReturn(centralSite);
        when(siteResource.get(aProductInstance.getSiteId(), aProjectId)).thenReturn(siteDTO);
        when(accountManagerResource.get()).thenThrow(new ResourceNotFoundException());
        when(expedioClientResources.projectResource()).thenReturn(expedioProjectResource);
        when(expedioProjectResource.getProject(aProjectId)).thenReturn(projectDTO);
        when(projectResource.quoteOptionResource(aProjectId)).thenReturn(quoteOptionResource);
        when(quoteOptionResource.get()).thenReturn(newArrayList(quoteOptionDTO));
        when(quoteOptionResource.quoteOptionItemResource(aQuoteOptionId)).thenReturn(quoteOptionItemResource);
        when(quoteOptionItemResource.get()).thenReturn(newArrayList(aQuoteOptionItemDto, anotherQuoteOptionItemDto));

        mergeResult = new MergeResult(newArrayList(aProductInstance), changeQueryTracker);
        when(changeQueryTracker.changeFor(aProductInstance)).thenReturn(ChangeType.ADD);
        when(productInstanceClient.getAssetsDiff(aProductInstance.getProductInstanceId().getValue(),
                                                 aProductInstance.getProductInstanceVersion(), null, null)).thenReturn(mergeResult);
        when(pricingClient.filterChargingSchemes(aProductInstance, ChangeType.ADD.getValue(), aProductInstance.getProductIdentifier().getProductId(), null, quoteOptionDTO.ifcPending)).thenReturn(aProductInstance.getChargingSchemes());
        when(pricingClient.filterChargingSchemes(anotherProductInstnace, ChangeType.ADD.getValue(), anotherProductInstnace.getProductIdentifier().getProductId(), null, anotherQuoteOptionItemDto.isIfc)).thenReturn(anotherProductInstnace.getChargingSchemes());
        PricingSheetProductModel productModel = new PricingSheetProductModel(siteDTO, aProductInstance, aQuoteOptionItemDto, mergeResult, caveatResource, pricingClient, null);
        mergeResult = new MergeResult(newArrayList(anotherProductInstnace), changeQueryTracker);
        when(changeQueryTracker.changeFor(anotherProductInstnace)).thenReturn(ChangeType.ADD);
        when(productInstanceClient.getAssetsDiff(anotherProductInstnace.getProductInstanceId().getValue(),
                                                 anotherProductInstnace.getProductInstanceVersion(), null, null)).thenReturn(mergeResult);
        PricingSheetProductModel anotherProductModel = new PricingSheetProductModel(siteDTO, anotherProductInstnace, anotherQuoteOptionItemDto, mergeResult, caveatResource, pricingClient, null);
        PricingSheetDataModel pricingSheetDataModel = pricingSheetDataModelFactory.create(aCustomerId, aProjectId, aQuoteOptionId, Optional.<String>absent());
        assertThat(pricingSheetDataModel.map().size(), is(20));
        assertThat(pricingSheetDataModel.getProducts(), hasItems(productModel, anotherProductModel));
    }

    @Test
    public void shouldCreatePricingSheetModelWithContractProducts() throws Exception {
        PricingSheetTestDataFixture pricingSheetTestDataFixture = new PricingSheetTestDataFixture();
        ProjectDTO projectDTO = pricingSheetTestDataFixture.aProjectDto();
        QuoteOptionDTO quoteOptionDTO = pricingSheetTestDataFixture.aQuoteOptionDto();
        CustomerDTO customerDTO = pricingSheetTestDataFixture.aCustomerDTO();
        QuoteOptionItemDTO aQuoteOptionItemDto = QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().withId("aLineItemId").build();

        ProductInstance contractProductInstance = DefaultProductInstanceFixture.aProductInstance()
                                                                               .withProductOffering(ProductOfferingFixture.aProductOffering()
                                                                                                                          .withChargingScheme(ProductChargingSchemeFixture.aChargingScheme().withPriceVisibility(ProductChargingScheme.PriceVisibility.Customer).build())
                                                                                                                          .withSimpleProductOfferingType(SimpleProductOfferingType.Contract))
                                                                               .withPricingStatus(PricingStatus.FIRM)
                                                                               .build();

        PriceLine oneTimePriceLine = pricingSheetTestDataFixture.aPriceLine("Root Product2 One time price", "M0302168", 100.00, PriceType.ONE_TIME, "Recommended Retail Price", 333.00, "A", "123");
        PriceLine recurringPriceLine = pricingSheetTestDataFixture.aPriceLine("Root Product2 Rental price", "M0302168", 100.00, PriceType.RECURRING, "Recommended Retail Price", 333.00, "A", "123");
        contractProductInstance.addPriceLines(newArrayList(oneTimePriceLine, recurringPriceLine));

        mergeResult = new MergeResult(newArrayList(contractProductInstance), changeQueryTracker);
        when(changeQueryTracker.changeFor(contractProductInstance)).thenReturn(ChangeType.ADD);
        when(expedioClientResources.getCustomerResource()).thenReturn(customerResource);
        when(customerResource.accountManagerResource(aCustomerId, aProjectId)).thenReturn(accountManagerResource);
        when(expedioClientResources.projectResource()).thenReturn(expedioProjectResource);
        when(expedioProjectResource.getProject(aProjectId)).thenReturn(projectDTO);
        when(projectResource.quoteOptionResource(aProjectId)).thenReturn(quoteOptionResource);
        when(quoteOptionResource.get()).thenReturn(newArrayList(quoteOptionDTO));
        when(quoteOptionResource.quoteOptionItemResource(aQuoteOptionId)).thenReturn(quoteOptionItemResource);
        when(quoteOptionResource.quoteOptionItemResource("aLineItemId")).thenReturn(quoteOptionItemResource);
        when(expedioClientResources.getCustomerResource()).thenReturn(customerResource);
        when(customerResource.getByToken(aCustomerId, "aToken")).thenReturn(customerDTO);
        when(customerResource.accountManagerResource(aCustomerId, aProjectId)).thenReturn(accountManagerResource);
        when(customerResource.siteResource(aCustomerId)).thenReturn(siteResource);
        when(quoteOptionItemResource.get()).thenReturn(newArrayList(aQuoteOptionItemDto));
        when(productInstanceClient.getSourceAsset(any(LengthConstrainingProductInstanceId.class))).thenReturn(Optional.<ProductInstance>absent());
        when(productInstanceClient.get(new LineItemId("aLineItemId"))).thenReturn(contractProductInstance);
        when(productInstanceClient.getAssetsDiff(contractProductInstance.getProductInstanceId().getValue(),
                                                 contractProductInstance.getProductInstanceVersion(), null, null)).thenReturn(mergeResult);
        when(pricingClient.filterChargingSchemes(contractProductInstance, ChangeType.ADD.getValue(), contractProductInstance.getProductIdentifier().getProductId(), null, quoteOptionDTO.ifcPending)).thenReturn(contractProductInstance.getChargingSchemes());


        PricingSheetDataModel pricingSheetDataModel = pricingSheetDataModelFactory.create(aCustomerId, aProjectId, aQuoteOptionId, Optional.<String>absent());
        assertThat(pricingSheetDataModel.getContractProducts().size(), is(1));
        assertThat(pricingSheetDataModel.getContractProducts().get(0).getProductInstance(), is(contractProductInstance));
    }

    @Test
    public void shouldCreatePricingSheetModelForSpecialBidProduct() {
        PricingSheetTestDataFixture pricingSheetTestDataFixture = new PricingSheetTestDataFixture();
        CustomerDTO customerDTO = pricingSheetTestDataFixture.aCustomerDTO();
        AccountManagerDTO accountManagerDTO = pricingSheetTestDataFixture.anAccountManager();
        ProjectDTO projectDTO = pricingSheetTestDataFixture.aProjectDto();
        QuoteOptionDTO quoteOptionDTO = pricingSheetTestDataFixture.aQuoteOptionDto();
        SiteDTO centralSite = pricingSheetTestDataFixture.aCentralSite();
        QuoteOptionItemDTO aQuoteOptionItemDto = QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().withId("aLineItemId").build();
        ProductInstance aProductInstance = pricingSheetTestDataFixture.aSpecialBidProduct().build();
        SiteDTO siteDTO = pricingSheetTestDataFixture.aSite();

        when(productInstanceClient.get(new LineItemId("aLineItemId"))).thenReturn(aProductInstance);
        when(productInstanceClient.getSourceAsset(any(LengthConstrainingProductInstanceId.class))).thenReturn(Optional.<ProductInstance>absent());
        when(expedioClientResources.getCustomerResource()).thenReturn(customerResource);
        when(customerResource.getByToken(aCustomerId, "aToken")).thenReturn(customerDTO);
        when(customerResource.accountManagerResource(aCustomerId, aProjectId)).thenReturn(accountManagerResource);
        when(customerResource.siteResource(aCustomerId)).thenReturn(siteResource);
        when(siteResource.getCentralSite(aProjectId)).thenReturn(centralSite);
        when(siteResource.get(aProductInstance.getSiteId(), aProjectId)).thenReturn(siteDTO);
        when(accountManagerResource.get()).thenReturn(accountManagerDTO);
        when(expedioClientResources.projectResource()).thenReturn(expedioProjectResource);
        when(expedioProjectResource.getProject(aProjectId)).thenReturn(projectDTO);
        when(projectResource.quoteOptionResource(aProjectId)).thenReturn(quoteOptionResource);
        when(quoteOptionResource.get()).thenReturn(newArrayList(quoteOptionDTO));
        when(quoteOptionResource.quoteOptionItemResource(anyString())).thenReturn(quoteOptionItemResource);
        when(quoteOptionItemResource.get()).thenReturn(newArrayList(aQuoteOptionItemDto));
        when(quoteOptionItemResource.getTpeRequest(aProductInstance.getProductInstanceId().getValue(), aProductInstance.getProductInstanceVersion())).thenReturn(new TpeRequestDTO());
        when(pricingClient.filterChargingSchemes(aProductInstance, ChangeType.ADD.getValue(), aProductInstance.getProductIdentifier().getProductId(), null, quoteOptionDTO.ifcPending)).thenReturn(aProductInstance.getChargingSchemes());
        mergeResult = new MergeResult(newArrayList(aProductInstance), changeQueryTracker);
        when(changeQueryTracker.changeFor(aProductInstance)).thenReturn(ChangeType.ADD);
        when(productInstanceClient.getAssetsDiff(aProductInstance.getProductInstanceId().getValue(),
                                                 aProductInstance.getProductInstanceVersion(), null, null)).thenReturn(mergeResult);
        PricingSheetDataModel pricingSheetDataModel = pricingSheetDataModelFactory.create(aCustomerId, aProjectId, aQuoteOptionId, Optional.<String>absent());
        assertThat(pricingSheetDataModel.map().size(), is(20));
        assertThat(pricingSheetDataModel.getSpecialBidProducts().get(0).getProductInstance(), is(aProductInstance));
    }

    @Test
    public void shouldGetTpeAttributeValue() {
        setUpSpecialBidData();
        when(projectResource.quoteOptionResource(any(String.class))).thenReturn(quoteOptionResource);
        when(quoteOptionResource.quoteOptionItemResource(any(String.class))).thenReturn(quoteOptionItemResource);
        TpeRequestDTO tpeRequestDTO = new TpeRequestDTO();
        tpeRequestDTO.detailedResponse = "detailedResponse";
        when(quoteOptionItemResource.getTpeRequest("productInstanceId",1L)).thenReturn(tpeRequestDTO);
        final String tpeAttributeValueFor = pricingSheetDataModelFactory.getDetailedResponse("projectId", anyString(), "productInstanceId",1L);
        assertThat(tpeAttributeValueFor, is("detailedResponse"));
    }

    @Test
    public void shouldGetNullIfNoValueFound() {
        when(projectResource.quoteOptionResource(any(String.class))).thenReturn(quoteOptionResource);
        when(quoteOptionResource.quoteOptionItemResource(any(String.class))).thenReturn(quoteOptionItemResource);
        when(quoteOptionItemResource.getTpeRequest("productInstanceId",1L)).thenReturn(new TpeRequestDTO());
        final String tpeAttributeValueFor = pricingSheetDataModelFactory.getDetailedResponse("projectId", anyString(), "productInstanceId", 1L);
        assertNull(tpeAttributeValueFor);
    }

    @Test
    public void shouldCreatePricingSheetAccessModel() {

        PricingSheetTestDataFixture pricingSheetTestDataFixture = new PricingSheetTestDataFixture();
        CustomerDTO customerDTO = pricingSheetTestDataFixture.aCustomerDTO();
        AccountManagerDTO accountManagerDTO = pricingSheetTestDataFixture.anAccountManager();
        ProjectDTO projectDTO = pricingSheetTestDataFixture.aProjectDto();
        QuoteOptionDTO quoteOptionDTO = pricingSheetTestDataFixture.aQuoteOptionDto();
        SiteDTO centralSite = pricingSheetTestDataFixture.aCentralSite();
        QuoteOptionItemDTO aQuoteOptionItemDto = QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().withId("aLineItemId").build();
        QuoteOptionItemDTO anotherQuoteOptionItemDto = QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().withId("anotherLineItemId").build();
        ProductInstance accessProductInstance = pricingSheetTestDataFixture.anAccessCircuitProduct().withLineItemId("aLineItemId").build();
        ProductInstance anotherProductInstance = pricingSheetTestDataFixture.aSiteAgnosticProductInstance();
        SiteDTO siteDTO = pricingSheetTestDataFixture.aSite();


        when(productInstanceClient.get(new LineItemId("aLineItemId"))).thenReturn(accessProductInstance);
        when(productInstanceClient.get(new LineItemId("anotherLineItemId"))).thenReturn(anotherProductInstance);
        when(productInstanceClient.getSourceAsset(any(LengthConstrainingProductInstanceId.class))).thenReturn(Optional.<ProductInstance>absent());
        when(expedioClientResources.getCustomerResource()).thenReturn(customerResource);
        when(customerResource.getByToken(aCustomerId, "aToken")).thenReturn(customerDTO);
        when(customerResource.accountManagerResource(aCustomerId, aProjectId)).thenReturn(accountManagerResource);
        when(customerResource.siteResource(aCustomerId)).thenReturn(siteResource);
        when(siteResource.getCentralSite(aProjectId)).thenReturn(centralSite);
        when(siteResource.get(accessProductInstance.getSiteId(), aProjectId)).thenReturn(siteDTO);
        when(accountManagerResource.get()).thenReturn(accountManagerDTO);
        when(expedioClientResources.projectResource()).thenReturn(expedioProjectResource);
        when(expedioProjectResource.getProject(aProjectId)).thenReturn(projectDTO);
        when(projectResource.quoteOptionResource(aProjectId)).thenReturn(quoteOptionResource);
        when(quoteOptionResource.get()).thenReturn(newArrayList(quoteOptionDTO));
        when(quoteOptionResource.quoteOptionItemResource(aQuoteOptionId)).thenReturn(quoteOptionItemResource);
        when(quoteOptionResource.quoteOptionItemResource("aLineItemId")).thenReturn(quoteOptionItemResource);
        Optional<TpeResponseDTO> tpeResponseDTO = mock(Optional.class);
        when(quoteOptionItemResource.getTpeResponse(accessProductInstance.getLineItemId(), TpeResponseAttributeNames.CONFIGURATION_CATEGORY.name(), TpeRequestType.SQE_TEMPLATE_DETAILS.name())).thenReturn(tpeResponseDTO);
        when(quoteOptionItemResource.getTpeResponse(accessProductInstance.getLineItemId(), "CAVEATS", TpeRequestType.STATUS_REFRESH.name())).thenReturn(tpeResponseDTO);
        when(tpeResponseDTO.isPresent()).thenReturn(false);
        when(quoteOptionItemResource.get()).thenReturn(newArrayList(aQuoteOptionItemDto, anotherQuoteOptionItemDto));

        //when
        when(changeQueryTracker.changeFor(accessProductInstance)).thenReturn(ChangeType.ADD);
        when(changeQueryTracker.changeFor(anotherProductInstance)).thenReturn(ChangeType.ADD);
        mergeResult = new MergeResult(newArrayList(accessProductInstance), changeQueryTracker);
        when(productInstanceClient.getAssetsDiff(accessProductInstance.getProductInstanceId().getValue(),
                                                 accessProductInstance.getProductInstanceVersion(), null, null)).thenReturn(mergeResult);
        when(pricingClient.filterChargingSchemes(accessProductInstance, ChangeType.ADD.getValue(), accessProductInstance.getProductIdentifier().getProductId(), null, quoteOptionDTO.ifcPending)).thenReturn(accessProductInstance.getChargingSchemes());
        when(pricingClient.filterChargingSchemes(anotherProductInstance, ChangeType.ADD.getValue(), anotherProductInstance.getProductIdentifier().getProductId(), null, quoteOptionDTO.ifcPending)).thenReturn(anotherProductInstance.getChargingSchemes());
        HashMap<String, String> attributes = new HashMap<String, String>();
        attributes.put(TpeResponseAttributeNames.CONFIGURATION_CATEGORY.name(), "");
        attributes.put(TpeResponseAttributeNames.CAVEATS.name(), "");
        mergeResult = new MergeResult(newArrayList(anotherProductInstance), changeQueryTracker);
        when(productInstanceClient.getAssetsDiff(anotherProductInstance.getProductInstanceId().getValue(),
                                                 anotherProductInstance.getProductInstanceVersion(), null, null)).thenReturn(mergeResult);
        when(pricingClient.filterChargingSchemes(accessProductInstance, ChangeType.ADD.getValue(), accessProductInstance.getProductIdentifier().getProductId(), null, quoteOptionDTO.ifcPending)).thenReturn(accessProductInstance.getChargingSchemes());
        PricingSheetDataModel pricingSheetDataModel = pricingSheetDataModelFactory.create(aCustomerId, aProjectId, aQuoteOptionId, Optional.<String>absent());
        PricingSheetProductModel anotherProductModel = new PricingSheetProductModel(siteDTO, anotherProductInstance, anotherQuoteOptionItemDto, mergeResult, caveatResource, pricingClient, null);
        PricingSheetProductModel accessProductModelProductModel = new PricingSheetProductModel(siteDTO, accessProductInstance, anotherQuoteOptionItemDto, mergeResult, caveatResource, pricingClient, null);
        assertThat(pricingSheetDataModel.map().size(), is(20));
        assertThat(pricingSheetDataModel.getProducts(), hasItems(anotherProductModel));
        assertThat(pricingSheetDataModel.getProductNames(),is("Internet Connect Reach"));
    }

    private void setChargingSchemeFor(ProductInstance parentProduct, ProductChargingScheme.PriceVisibility visibility) {
        List<ProductChargingScheme> chargingSchemes = newArrayList();
        ProductChargingScheme scheme = ProductChargingSchemeFixture.aChargingScheme().withPriceVisibility(visibility).build();
        chargingSchemes.add(scheme);
        when(parentProduct.getChargingSchemes()).thenReturn(chargingSchemes);
        when(parentProduct.getProductOffering()).thenReturn(new ProductOfferingFixture("parent").build());
    }

    private void setUpSpecialBidData() {
        PricingSheetTestDataFixture pricingSheetTestDataFixture = new PricingSheetTestDataFixture();
        CustomerDTO customerDTO = pricingSheetTestDataFixture.aCustomerDTO();
        ProjectDTO projectDTO = pricingSheetTestDataFixture.aProjectDto();
        QuoteOptionDTO quoteOptionDTO = pricingSheetTestDataFixture.aQuoteOptionDto();
        SiteDTO centralSite = pricingSheetTestDataFixture.aCentralSite();
        QuoteOptionItemDTO aQuoteOptionItemDto = QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().withId("aLineItemId").build();
        QuoteOptionItemDTO anotherQuoteOptionItemDto = QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().withId("anotherLineItemId").build();
        ProductInstance aProductInstance = pricingSheetTestDataFixture.aSpecialBidProduct().withLineItemId("aLineItemId").build();
        ProductInstance anotherProductInstance = pricingSheetTestDataFixture.aSpecialBidProduct().withLineItemId("anotherLineItemId").build();
        SiteDTO siteDTO = pricingSheetTestDataFixture.aSite();


        when(productInstanceClient.get(new LineItemId("aLineItemId"))).thenReturn(aProductInstance);
        when(productInstanceClient.get(new LineItemId("anotherLineItemId"))).thenReturn(anotherProductInstance);
        when(expedioClientResources.getCustomerResource()).thenReturn(customerResource);
        when(customerResource.getByToken(aCustomerId, "aToken")).thenReturn(customerDTO);
        when(customerResource.accountManagerResource(aCustomerId, aProjectId)).thenReturn(accountManagerResource);
        when(customerResource.siteResource(aCustomerId)).thenReturn(siteResource);
        when(siteResource.getCentralSite(aProjectId)).thenReturn(centralSite);
        when(siteResource.get(aProductInstance.getSiteId(), aProjectId)).thenReturn(siteDTO);
        when(accountManagerResource.get()).thenThrow(new ResourceNotFoundException());
        when(expedioClientResources.projectResource()).thenReturn(expedioProjectResource);
        when(expedioProjectResource.getProject(aProjectId)).thenReturn(projectDTO);
        when(projectResource.quoteOptionResource(aProjectId)).thenReturn(quoteOptionResource);
        when(quoteOptionResource.get()).thenReturn(newArrayList(quoteOptionDTO));
        when(quoteOptionResource.quoteOptionItemResource(aQuoteOptionId)).thenReturn(quoteOptionItemResource);
        when(quoteOptionItemResource.get()).thenReturn(newArrayList(aQuoteOptionItemDto, anotherQuoteOptionItemDto));
    }

    @Test
    public void shouldCreatePricingSheetModelForIfcItems() {
        PricingSheetTestDataFixture pricingSheetTestDataFixture = new PricingSheetTestDataFixture();
        CustomerDTO customerDTO = pricingSheetTestDataFixture.aCustomerDTO();
        AccountManagerDTO accountManagerDTO = pricingSheetTestDataFixture.anAccountManager();
        ProjectDTO projectDTO = pricingSheetTestDataFixture.aProjectDto();
        QuoteOptionDTO quoteOptionDTO = pricingSheetTestDataFixture.aQuoteOptionDto();
        SiteDTO centralSite = pricingSheetTestDataFixture.aCentralSite();
        QuoteOptionItemDTO aQuoteOptionItemDto = QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().withId("aLineItemId").withIFC(true).build();
        QuoteOptionItemDTO anotherQuoteOptionItemDto = QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().withId("anotherLineItemId").build();
        ProductInstance aProductInstance = pricingSheetTestDataFixture.aICGProductWithAccessCircuit();
        aProductInstance.setAssetSourceVersion(new Long("1"));
        ProductInstance anotherProductInstnace = pricingSheetTestDataFixture.aSiteAgnosticProductInstance();
        ProductInstance aParentProductInstance = pricingSheetTestDataFixture.anInstallableRootProductWithAChild();
        SiteDTO siteDTO = pricingSheetTestDataFixture.aSite();

        when(productInstanceClient.get(new LineItemId("aLineItemId"))).thenReturn(aProductInstance);
        when(productInstanceClient.get(new LineItemId("aLineItemId"))).thenReturn(aProductInstance);
        when(productInstanceClient.get(new LineItemId("anotherLineItemId"))).thenReturn(anotherProductInstnace);
        when(productInstanceClient.getSourceAsset(any(LengthConstrainingProductInstanceId.class))).thenReturn(Optional.<ProductInstance>absent());
        when(productInstanceClient.getByAssetKey(any(AssetKey.class))).thenReturn(aParentProductInstance);
        when(expedioClientResources.getCustomerResource()).thenReturn(customerResource);
        when(customerResource.getByToken(aCustomerId, "aToken")).thenReturn(customerDTO);
        when(customerResource.accountManagerResource(aCustomerId, aProjectId)).thenReturn(accountManagerResource);
        when(customerResource.siteResource(aCustomerId)).thenReturn(siteResource);
        when(siteResource.getCentralSite(aProjectId)).thenReturn(centralSite);
        when(siteResource.get(aProductInstance.getSiteId(), aProjectId)).thenReturn(siteDTO);
        when(accountManagerResource.get()).thenReturn(accountManagerDTO);
        when(expedioClientResources.projectResource()).thenReturn(expedioProjectResource);
        when(expedioProjectResource.getProject(aProjectId)).thenReturn(projectDTO);
        when(projectResource.quoteOptionResource(aProjectId)).thenReturn(quoteOptionResource);
        when(quoteOptionResource.get()).thenReturn(newArrayList(quoteOptionDTO));
        when(quoteOptionResource.quoteOptionItemResource(aQuoteOptionId)).thenReturn(quoteOptionItemResource);
        when(quoteOptionItemResource.get()).thenReturn(newArrayList(aQuoteOptionItemDto, anotherQuoteOptionItemDto));

        //when
        mergeResult = new MergeResult(newArrayList(aProductInstance), changeQueryTracker);
        when(changeQueryTracker.changeFor(aProductInstance)).thenReturn(ChangeType.ADD);
        when(productInstanceClient.getAssetsDiff(aProductInstance.getProductInstanceId().getValue(),
                                                 aProductInstance.getProductInstanceVersion(), null, null)).thenReturn(mergeResult);
        when(productInstanceClient.getAssetsDiff(aProductInstance.getProductInstanceId().getValue(),
                                                 aProductInstance.getProductInstanceVersion(), aParentProductInstance.getProductInstanceVersion(), null)).thenReturn(mergeResult);
        when(pricingClient.filterChargingSchemes(aProductInstance, ChangeType.ADD.getValue(), aProductInstance.getProductIdentifier().getProductId(), "Add", true)).thenReturn(aProductInstance.getChargingSchemes());
        when(pricingClient.filterChargingSchemes(anotherProductInstnace, ChangeType.ADD.getValue(), anotherProductInstnace.getProductIdentifier().getProductId(), null, anotherQuoteOptionItemDto.isIfc)).thenReturn(anotherProductInstnace.getChargingSchemes());

        PricingSheetProductModel productModel = new PricingSheetProductModel(siteDTO, aProductInstance, aQuoteOptionItemDto, mergeResult, caveatResource, pricingClient, null);
        mergeResult = new MergeResult(newArrayList(anotherProductInstnace), changeQueryTracker);
        when(changeQueryTracker.changeFor(anotherProductInstnace)).thenReturn(ChangeType.ADD);
        when(productInstanceClient.getAssetsDiff(anotherProductInstnace.getProductInstanceId().getValue(),
                                                 anotherProductInstnace.getProductInstanceVersion(), null, null)).thenReturn(mergeResult);
        PricingSheetDataModel pricingSheetDataModel = pricingSheetDataModelFactory.create(aCustomerId, aProjectId, aQuoteOptionId, Optional.<String>absent());
        PricingSheetProductModel anotherProductModel = new PricingSheetProductModel(siteDTO, anotherProductInstnace, anotherQuoteOptionItemDto, mergeResult, caveatResource, pricingClient, null);
        assertThat(pricingSheetDataModel.map().size(), is(20));
        assertThat(pricingSheetDataModel.getProducts(), hasItems(productModel, anotherProductModel));
        assertThat(pricingSheetDataModel.getBidDetails(), is(comments));
    }

}