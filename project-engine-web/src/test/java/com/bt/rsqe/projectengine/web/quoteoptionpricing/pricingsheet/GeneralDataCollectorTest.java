package com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet;

import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerrecord.AccountManagerDTO;
import com.bt.rsqe.customerrecord.CustomerDTO;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.project.PricingStatus;
import com.bt.rsqe.security.UserContext;
import com.bt.rsqe.security.UserContextManager;
import com.bt.rsqe.utils.countries.Countries;
import com.bt.rsqe.utils.countries.Country;
import com.bt.rsqe.enums.ProductCodes;
import com.bt.rsqe.expedio.project.ExpedioProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.bt.rsqe.projectengine.web.facades.AccountManagerFacade;
import com.bt.rsqe.projectengine.web.facades.CustomerFacade;
import com.bt.rsqe.projectengine.web.facades.FlattenedProductStructure;
import com.bt.rsqe.projectengine.web.facades.FutureAssetPricesFacade;
import com.bt.rsqe.projectengine.web.facades.FutureProductInstanceFacade;
import com.bt.rsqe.projectengine.web.facades.LineItemFacade;
import com.bt.rsqe.projectengine.web.facades.QuoteOptionFacade;
import com.bt.rsqe.projectengine.web.facades.SiteFacade;
import com.bt.rsqe.expedio.fixtures.SiteDTOFixture;
import com.bt.rsqe.projectengine.web.model.FutureAssetPricesModel;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.model.OneVoiceConfiguration;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.PriceSuppressStrategy;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.PricingSheetKeys;
import com.bt.rsqe.web.rest.exception.ResourceNotFoundException;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bt.rsqe.expedio.fixtures.ProjectDTOFixture.*;
import static com.bt.rsqe.security.UserContextBuilder.aDirectUserContext;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Maps.*;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class GeneralDataCollectorTest {

    private static final String PROJECT_ID = "projectId";
    private static final String QUOTE_OPTION_ID = "quoteOptionId";
    private static final String CUSTOMER_ID = "customerId";
    private static final String TOKEN = "aToken";
    protected static final String CURRENCY = "currency";
    protected static final String FIRST_CONTRACT_TERM = "12";
    private static final String SECOND_CONTRACT_TERM = "24";
    private static final String SITE_ONE_CONFIG_TYPE = "config type one";
    private static final String SITE_ONE_TARIFF_OPTIONS = "tariff options one";
    private static final String SITE_ONE_NUMBER_OF_CHANNELS = "2";
    private static final String SITE_TWO_CONFIG_TYPE = "config type two";
    private static final String SITE_TWO_TARIFF_OPTIONS = "tariff options two";
    private static final String SITE_TWO_NUMBER_OF_CHANNELS = "5";

    private static final String EUP_VERSION = "2.0";
    private static final String CHARGED_VERSION = "2.0";

    private CustomerFacade mockCustomerFacade;
    private SiteFacade mockSiteFacade;
    private LineItemFacade mockLineItemFacade;
    private FutureAssetPricesFacade futureAssetPricesFacade;
    private GeneralDataCollector dataCollector;
    private FutureAssetPricesModel futureAssetPricesModel1;

    private FutureAssetPricesModel futureAssetPricesModel2;
    private LineItemModel lineItemModel1;
    private LineItemModel lineItemModel2;
    private QuoteOptionFacade mockQuoteOptionFacade;
    private FutureProductInstanceFacade futureProductInstanceProxyFacade;
    private ExpedioProjectResource expedioProjectsResource;
    private CustomerDTO customerDTO;
    private SiteDTO siteDTO;
    private ArrayList<LineItemModel> lineItemModels;


    private SiteDTO siteDTO1;
    private SiteDTO siteDTO2;
    private AccountManagerFacade accountManagerFacade;
    private AccountManagerDTO accountManagerDTO;

    @Before
    public void before() {
        mockCustomerFacade = mock(CustomerFacade.class);
        mockSiteFacade = mock(SiteFacade.class);
        mockLineItemFacade = mock(LineItemFacade.class);
        mockQuoteOptionFacade = mock(QuoteOptionFacade.class);
        futureAssetPricesFacade = mock(FutureAssetPricesFacade.class);
        futureAssetPricesModel1 = mock(FutureAssetPricesModel.class);
        futureAssetPricesModel2 = mock(FutureAssetPricesModel.class);
        futureProductInstanceProxyFacade = mock(FutureProductInstanceFacade.class);
        expedioProjectsResource = mock(ExpedioProjectResource.class);
        accountManagerFacade = mock(AccountManagerFacade.class);

        lineItemModel1 = mockLineItem();
        lineItemModel2 = mockLineItem();
        lineItemModels = new ArrayList<LineItemModel>() {{
            add(lineItemModel1);
            add(lineItemModel2);
        }};
        dataCollector = new GeneralDataCollector(mockCustomerFacade, mockSiteFacade, expedioProjectsResource, mockQuoteOptionFacade, accountManagerFacade);

        customerDTO = new CustomerDTO("id", "name", "salesChannel");

        Country originCountry = Countries.byIsoStatic("GB");
        siteDTO = SiteDTOFixture.aSiteDTO().withBfgSiteId("100").withSubBuilding("someSubBuilding").withBuilding("someBuilding")
                                                          .withBuildingNumber("someBuildingNumber").withSubStreet("someSubStreet").withStreet("someStreet")
                                                          .withSubLocality("someSubLocality").withLocality("someLocality").withCity("someCity")
                                                          .withSubStateCountyProvince("someSubStateCountyProvince").withStateCountyProvince("someStateCountyProvince")
                                                          .withCountry("someCountry").withPostCode("614704").withPostBox("somePOBox").build();

        accountManagerDTO = new AccountManagerDTO(CUSTOMER_ID, "account", "manager", "123456789", "987654321", "account@email.com", "aRole", "123");

        siteDTO1 = SiteDTOFixture.aSiteDTO().withBfgSiteId("SID1").withName("Site1").withCity("City1").withCountry(originCountry.getDisplayName()).build();
        siteDTO2 = SiteDTOFixture.aSiteDTO().withBfgSiteId("SID2").withName("Site2").withCity("City2").withCountry(Countries.byIsoStatic("CH").getDisplayName()).build();

        UserContext userContext = aDirectUserContext().withToken("aToken").build();
        UserContextManager.setCurrent(userContext);

    }

    private LineItemModel mockLineItem() {
        final LineItemModel lineItemModel = mock(LineItemModel.class);
        when(lineItemModel.customerId()).thenReturn(CUSTOMER_ID);
        when(lineItemModel.quoteOptionId()).thenReturn(QUOTE_OPTION_ID);
        when(lineItemModel.projectId()).thenReturn(PROJECT_ID);
        return lineItemModel;
    }


    @Test
    public void shouldRetrieveCustomerDetails() throws Exception {
        mockCentralSiteAndSalesChannel();
        generalInfoAndSiteExpectations();
        mockAccountManagerFacade();

        final Map<String, Object> valuesMap = process();
        checkKeyValue(valuesMap, PricingSheetKeys.BT_SUBSIDIARY_NAME, "salesChannel");
        checkKeyValue(valuesMap, PricingSheetKeys.CUSTOMER_NAME, "name");
    }

    @Test
    public void shouldNotSetAccountManagerInfoIfCannotFindIt() throws Exception {
        mockCentralSiteAndSalesChannel();
        generalInfoAndSiteExpectations();
        mockAccountManagerFacadeToThrowException();

        final Map<String, Object> valuesMap = process();
        assertFalse(valuesMap.containsKey(PricingSheetKeys.ACCOUNT_MANAGER_NAME));
        assertFalse(valuesMap.containsKey(PricingSheetKeys.ACCOUNT_MANAGER_EMAIL));
        assertFalse(valuesMap.containsKey(PricingSheetKeys.ACCOUNT_MANAGER_FAX));
        assertFalse(valuesMap.containsKey(PricingSheetKeys.ACCOUNT_MANAGER_PHONE));
    }

    @Test
    public void shouldRetrieveGeneralValuesForQuoteOption() throws Exception {
        mockCentralSiteAndSalesChannel();
        generalInfoAndSiteExpectations();
        mockAccountManagerFacade();

        final Map<String, Object> valuesMap = process();

        checkKeyValue(valuesMap, CURRENCY, "USD");
        checkKeyValue(valuesMap, PricingSheetKeys.QUOTE_ID, PROJECT_ID);
        checkKeyValue(valuesMap, PricingSheetKeys.QUOTE_VERSION, QUOTE_OPTION_ID);
        checkKeyValue(valuesMap, PricingSheetKeys.QUOTE_NAME, "quoteName");
        checkKeyValue(valuesMap, PricingSheetKeys.CONTRACT_TERM, "5");

        checkKeyValue(valuesMap, PricingSheetKeys.BID_NUMBER, "34");
        checkKeyValue(valuesMap, PricingSheetKeys.SALES_USER_NAME, "Brian");
        checkKeyValue(valuesMap, PricingSheetKeys.CONTRACT_ID, "contractId");

        checkKeyValue(valuesMap, PricingSheetKeys.ACCOUNT_MANAGER_NAME, "account manager");
        checkKeyValue(valuesMap, PricingSheetKeys.ACCOUNT_MANAGER_EMAIL, "account@email.com");
        checkKeyValue(valuesMap, PricingSheetKeys.ACCOUNT_MANAGER_FAX, "987654321");
        checkKeyValue(valuesMap, PricingSheetKeys.ACCOUNT_MANAGER_PHONE, "123456789");
    }

    private Map<String, Object> process() {
        return process(lineItemModels);
    }

    private Map<String, Object> process(List<LineItemModel> lineItems) {
        final HashMap<String, Object> sheetModel = newHashMap();
        dataCollector.process(lineItems, sheetModel);
        return sheetModel;
    }

    @Test
    public void shouldRetrieveCentralSiteDetails() throws Exception {
        mockCentralSiteAndSalesChannel();
        generalInfoAndSiteExpectations();
        mockAccountManagerFacade();

        final Map<String, Object> valuesMap = process();
        checkKeyValue(valuesMap, PricingSheetKeys.BUILDING_NUMBER, "someSubBuilding, someBuilding");
        checkKeyValue(valuesMap, PricingSheetKeys.ADDRESS_LINE_1, "someBuildingNumber, someSubStreet, someStreet, somePOBox");
        checkKeyValue(valuesMap, PricingSheetKeys.ADDRESS_LINE_2, "someSubLocality, someLocality");
        checkKeyValue(valuesMap, PricingSheetKeys.CITY, "someCity");
        checkKeyValue(valuesMap, PricingSheetKeys.COUNTY_STATE, "someSubStateCountyProvince, someStateCountyProvince");
        checkKeyValue(valuesMap, PricingSheetKeys.COUNTRY_POSTCODE, "614704");
    }


    @Test
    public void shouldHavePricingStatusAsFirmIfAllLineItemsAreFirm() throws Exception {
        assertPricingStatus(PricingStatus.FIRM, PricingStatus.FIRM);
    }

    @Test
    public void shouldHavePricingStatusAsBudgetaryIfAnyLineItemsAreNotFirm() throws Exception {
        assertPricingStatus(PricingStatus.BUDGETARY, PricingStatus.BUDGETARY);
    }


    private void assertPricingStatus(final PricingStatus lineItemPricingStatus, PricingStatus quoteOptionPricingStatus) {
        mockCentralSiteAndSalesChannel();
        mockAccountManagerFacade();

        final LineItemModel lineItemModel3 = mockLineItem();
        final List<LineItemModel> lineItemModelList = asList(lineItemModel1, lineItemModel2, lineItemModel3);
        when(mockLineItemFacade.fetchLineItems(CUSTOMER_ID, "aContractId", PROJECT_ID, QUOTE_OPTION_ID, PriceSuppressStrategy.None)).thenReturn(lineItemModelList);
        when(lineItemModel3.getPricingStatusOfTree()).thenReturn(lineItemPricingStatus);

        final Map<String, Object> valuesMap = process(lineItemModelList);
        checkKeyValue(valuesMap, PricingSheetKeys.PRICING_STATUS, quoteOptionPricingStatus.getDescription());
    }

    private void checkKeyValue(Map<String, Object> valuesMap, String key, Object value) {
        assertTrue(valuesMap.containsKey(key));
        assertThat(valuesMap.get(key), is(value));
    }

    private void generalInfoAndSiteExpectations() {
        final LineItemId lineItemId1 = new LineItemId("1");
        final LineItemId lineItemId2 = new LineItemId("2");

        final List<LineItemId> lineItemIds = newArrayList(lineItemId1, lineItemId2);
        final FlattenedProductStructure productInstances1 = mock(FlattenedProductStructure.class, "productInstances1");
        final FlattenedProductStructure productInstances2 = mock(FlattenedProductStructure.class, "productInstances2");


        when(mockLineItemFacade.fetchLineItems(CUSTOMER_ID, "aContractId", PROJECT_ID, QUOTE_OPTION_ID, PriceSuppressStrategy.None)).thenReturn(asList(lineItemModel1, lineItemModel2));

        when(lineItemModel1.getFutureAssetPricesModel()).thenReturn(futureAssetPricesModel1);

        when(lineItemModel2.getFutureAssetPricesModel()).thenReturn(futureAssetPricesModel2);

        when(futureAssetPricesFacade.getForLineItems(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, lineItemIds)).thenReturn(newArrayList(futureAssetPricesModel1, futureAssetPricesModel2));

        when(lineItemModel1.getId()).thenReturn("1");

        when(lineItemModel2.getId()).thenReturn("2");

        when(lineItemModel1.getContractTerm()).thenReturn(String.valueOf(FIRST_CONTRACT_TERM));

        when(lineItemModel2.getContractTerm()).thenReturn(String.valueOf(SECOND_CONTRACT_TERM));

        when(futureAssetPricesModel1.getSite()).thenReturn(siteDTO1);

        when(futureAssetPricesModel1.isForLineItem("1")).thenReturn(true);

        when(futureAssetPricesModel1.isForLineItem("2")).thenReturn(false);


        when(futureAssetPricesModel2.getSite()).thenReturn(siteDTO2);

        when(futureAssetPricesModel2.isForLineItem("2")).thenReturn(true);


        when(futureProductInstanceProxyFacade.getProductInstances(lineItemId1)).thenReturn(productInstances1);
        when(futureProductInstanceProxyFacade.getProductInstances(lineItemId2)).thenReturn(productInstances2);

        when(productInstances1.firstAttributeValueFor(ProductCodes.OnevoiceOptions.productCode(), OneVoiceConfiguration.BasicMPLS.OneVoiceOptions.ONEVOICE_TYPE)).thenReturn(SITE_ONE_CONFIG_TYPE);

        when(productInstances1.firstAttributeValueFor(ProductCodes.BTPriceLine.productCode(), OneVoiceConfiguration.BasicMPLS.BTPriceLine.TARIFF_OPTIONS)).thenReturn(SITE_ONE_TARIFF_OPTIONS);

        when(productInstances1.firstAttributeValueFor(ProductCodes.OnevoiceOptions.productCode(), OneVoiceConfiguration.BasicMPLS.OneVoiceOptions.NUMBER_VOICE_CHANNELS)).thenReturn(SITE_ONE_NUMBER_OF_CHANNELS);

        when(productInstances1.firstAttributeValueFor(ProductCodes.BTPriceLine.productCode(), OneVoiceConfiguration.BasicMPLS.BTPriceLine.EUP_PRICEBOOK_VERSION)).thenReturn(EUP_VERSION);

        when(productInstances1.firstAttributeValueFor(ProductCodes.BTPriceLine.productCode(), OneVoiceConfiguration.BasicMPLS.BTPriceLine.CHARGED_PRICEBOOK_VERSION)).thenReturn(CHARGED_VERSION);

        when(productInstances2.firstAttributeValueFor(ProductCodes.OnevoiceOptions.productCode(), OneVoiceConfiguration.BasicMPLS.OneVoiceOptions.ONEVOICE_TYPE)).thenReturn(SITE_TWO_CONFIG_TYPE);

        when(productInstances2.firstAttributeValueFor(ProductCodes.BTPriceLine.productCode(), OneVoiceConfiguration.BasicMPLS.BTPriceLine.TARIFF_OPTIONS)).thenReturn(SITE_TWO_TARIFF_OPTIONS);

        when(productInstances2.firstAttributeValueFor(ProductCodes.OnevoiceOptions.productCode(), OneVoiceConfiguration.BasicMPLS.OneVoiceOptions.NUMBER_VOICE_CHANNELS)).thenReturn(SITE_TWO_NUMBER_OF_CHANNELS);

        when(productInstances2.firstAttributeValueFor(ProductCodes.BTPriceLine.productCode(), OneVoiceConfiguration.BasicMPLS.BTPriceLine.EUP_PRICEBOOK_VERSION)).thenReturn(EUP_VERSION);

        when(productInstances2.firstAttributeValueFor(ProductCodes.BTPriceLine.productCode(), OneVoiceConfiguration.BasicMPLS.BTPriceLine.CHARGED_PRICEBOOK_VERSION)).thenReturn(CHARGED_VERSION);

    }

    private void mockAccountManagerFacade() {
        when(accountManagerFacade.get(CUSTOMER_ID, PROJECT_ID)).thenReturn(accountManagerDTO);
    }

    private void mockAccountManagerFacadeToThrowException() {
        when(accountManagerFacade.get(CUSTOMER_ID, PROJECT_ID)).thenThrow(new ResourceNotFoundException());
    }


    private void mockCentralSiteAndSalesChannel() {

        when(mockCustomerFacade.getByToken(CUSTOMER_ID, TOKEN)).thenReturn(customerDTO);

        when(mockSiteFacade.getCentralSite(CUSTOMER_ID, PROJECT_ID)).thenReturn(siteDTO);

        when(mockQuoteOptionFacade.get(PROJECT_ID, QUOTE_OPTION_ID)).thenReturn(QuoteOptionDTO.newInstance("friendlyId", "quoteName", "USD", "5", "user"));

        when(lineItemModel1.getPricingStatusOfTree()).thenReturn(PricingStatus.FIRM);
        when(lineItemModel2.getPricingStatusOfTree()).thenReturn(PricingStatus.FIRM);

        when(expedioProjectsResource.getProject(PROJECT_ID)).thenReturn(aProjectDTO().build());
    }

}
