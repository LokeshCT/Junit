package com.bt.rsqe.customerinventory.service.evaluators;

import com.bt.rsqe.client.CustomerClient;
import com.bt.rsqe.client.ExpedioClient;
import com.bt.rsqe.client.Pmr;
import com.bt.rsqe.client.SiteClient;
import com.bt.rsqe.customerinventory.parameter.ProductInstanceState;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristic;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetOfferingDetail;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetRelationship;
import com.bt.rsqe.customerinventory.service.repository.CIFAssetJPARepository;
import com.bt.rsqe.customerrecord.CustomerDTO;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.product.AssetProcessType;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.product.parameters.RelationshipType;
import com.bt.rsqe.productinstancemerge.ChangeType;
import com.bt.rsqe.projectengine.OrderDTO;
import com.bt.rsqe.projectengine.OrderResource;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.QuoteOptionResource;
import com.google.common.base.Optional;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.bt.rsqe.customerinventory.service.evaluators.NonCharacteristicEvaluableExpressions.ProductName;
import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class NonCharacteristicExpressionEvaluatorTest {
    public static final String COUNTRY_ISO_CODE_2 = "COUNTRY_ISO_CODE2";
    public static final String COUNTRY_ISO_CODE_1 = "COUNTRY_ISO_CODE1";
    public static final String COUNTRY_NAME_1 = "COUNTRY_NAME1";
    public static final String SITE_NAME_1 = "SITE_NAME1";
    public static final String COUNTRY_NAME_2 = "COUNTRY_NAME2";
    public static final String SITE_NAME_2 = "SITE_NAME2";
    private AssetKey assetKey1 = new AssetKey("ASSET_ID1", 1l);
    public static final String SITE_1 = "SITE_1";
    public static final String SITE_2 = "SITE_2";
    private static final String PROJECT_ID1 = "PROJECT_ID";
    public static final String QUOTE_OPTION_ID1 = "QUOTE_OPTION_ID";
    public static final String LINE_ITEM_ID2 = "OTHER_LINE_ITEM_ID";
    public static final String LINE_ITEM_ID1 = "LINE_ITEM_ID";
    public static final String PRODUCT_CODE = "PRODUCT_CODE";
    private Pmr pmrClient;
    private ExpedioClient expedioClient;
    private ProjectResource projectResource;
    CIFAssetJPARepository cifAssetJPARepository;

    @Before
    public void setup(){
        pmrClient = mock(Pmr.class);
        expedioClient = mock(ExpedioClient.class);
        projectResource = mock(ProjectResource.class);
        cifAssetJPARepository = mock(CIFAssetJPARepository.class);
    }

    @Test
    public void shouldKnowWhichExpressionsAreSupported() {
        String assetUniqueIdExpression = "AssetUniqueId";
        NonCharacteristicExpressionEvaluator evaluator = new NonCharacteristicExpressionEvaluator(assetUniqueIdExpression, pmrClient, expedioClient, projectResource, cifAssetJPARepository);

        assertThat(evaluator.handlesNonCharacteristicExpression(assetUniqueIdExpression), is(true));
    }

    @Test
    public void shouldGetAssetUniqueId() {
        String assetUniqueIdExpression = "AssetUniqueId";
        String someUniqueId = "SomeUniqueId";

        CIFAsset mockAsset = mock(CIFAsset.class);

        when(mockAsset.getAssetUniqueId()).thenReturn(someUniqueId);

        NonCharacteristicExpressionEvaluator evaluator = new NonCharacteristicExpressionEvaluator(assetUniqueIdExpression, pmrClient, expedioClient, projectResource, cifAssetJPARepository);
        Object assetUniqueId = evaluator.evaluate(mockAsset);

        assertThat(assetUniqueId, is((Object)someUniqueId));
    }

    @Test
    public void shouldGetDevideRoleAttr() {
        String deviceRoleEnum = "DeviceRoleAttr";
        String deviceRole = "DEVICE ROLE";

        CIFAsset mockAsset = mock(CIFAsset.class);
        CIFAssetCharacteristic cifAssetCharacteristic = mock(CIFAssetCharacteristic.class);

        when(mockAsset.getCharacteristic(deviceRole)).thenReturn(cifAssetCharacteristic);
        when(cifAssetCharacteristic.getValue()).thenReturn("someValue");

        NonCharacteristicExpressionEvaluator evaluator = new NonCharacteristicExpressionEvaluator(deviceRoleEnum, pmrClient, expedioClient, projectResource, cifAssetJPARepository);
        Object value = evaluator.evaluate(mockAsset);
        assertThat(value, is((Object)"someValue"));

        when(mockAsset.getCharacteristic(deviceRole)).thenReturn(null);
        evaluator = new NonCharacteristicExpressionEvaluator(deviceRoleEnum, pmrClient, expedioClient, projectResource, cifAssetJPARepository);
        value = evaluator.evaluate(mockAsset);
        assertThat(value,is((Object)""));

    }

    @Test
    public void shouldGetAssetUniqueIdAsSubstringOfIdWhenItIscifAssetJPARepositoryInTheCIFAsset() {
        String assetUniqueIdExpression = "AssetUniqueId";

        CIFAsset mockAsset = mock(CIFAsset.class);

        when(mockAsset.getAssetUniqueId()).thenReturn(null);
        when(mockAsset.getAssetKey()).thenReturn(new AssetKey("AssetId12345", 1l));

        NonCharacteristicExpressionEvaluator evaluator = new NonCharacteristicExpressionEvaluator(assetUniqueIdExpression, pmrClient, expedioClient, projectResource, cifAssetJPARepository);
        Object assetUniqueId = evaluator.evaluate(mockAsset);

        assertThat(assetUniqueId, is((Object)"12345"));
    }

    @Test
    public void shouldGetRelationshipNameInParent() {
        String myRelationshipInParentExpression = "MyRelationshipWithParent";

        CIFAsset mockAsset = mock(CIFAsset.class);
        CIFAsset ownerAsset = mock(CIFAsset.class);
        CIFAssetRelationship cifAssetRelationship = new CIFAssetRelationship(mockAsset, "name", RelationshipType.Child, ProductInstanceState.LIVE);

        final AssetKey assetId = new AssetKey("AssetId12345", 1l);
        when(mockAsset.getAssetKey()).thenReturn(assetId);
        when(cifAssetJPARepository.getOwnerAssets(assetId,true)).thenReturn(newArrayList(ownerAsset));
        when(ownerAsset.getRelationships()).thenReturn(newArrayList(cifAssetRelationship));

        NonCharacteristicExpressionEvaluator evaluator = new NonCharacteristicExpressionEvaluator(myRelationshipInParentExpression, pmrClient, expedioClient, projectResource, cifAssetJPARepository);
        Object myRelationshipInParent = evaluator.evaluate(mockAsset);

        assertThat(myRelationshipInParent, is((Object)"name"));

        when(cifAssetJPARepository.getOwnerAssets(assetId,true)).thenReturn(null);
        myRelationshipInParent = evaluator.evaluate(mockAsset);
        assertThat(myRelationshipInParent, is((Object)""));
    }

    @Test
    public void shouldGetAssetId() {
        String assetIdExpression = "AssetId";

        CIFAsset mockAsset = mock(CIFAsset.class);

        when(mockAsset.getAssetKey()).thenReturn(assetKey1);

        NonCharacteristicExpressionEvaluator evaluator = new NonCharacteristicExpressionEvaluator(assetIdExpression, pmrClient, expedioClient, projectResource, cifAssetJPARepository);
        Object assetId = evaluator.evaluate(mockAsset);

        assertThat(assetId, is((Object)assetKey1.getAssetId()));
    }

    @Test
    public void shouldGetProductCategoryCode(){
        String productCategoryCodeExpression = "ProductCategoryCode";
        String someCatCode = "SomeCatCode";
        String someOtherCatCode = "SomeOtherCatCode";
        String sCode1 = "S_CODE1";
        String sCode2 = "S_CODE2";
        ProductIdentifier productIdentifier1 = new ProductIdentifier(someCatCode, "V1");
        ProductIdentifier productIdentifier2 = new ProductIdentifier(someOtherCatCode, "V1");

        CIFAsset mockAsset1 = mock(CIFAsset.class);
        CIFAsset mockAsset2 = mock(CIFAsset.class);

        when(mockAsset1.getProductCode()).thenReturn(sCode1);
        when(pmrClient.getProductHCode(sCode1)).thenReturn(Optional.of(productIdentifier1));
        when(mockAsset2.getProductCode()).thenReturn(sCode2);
        when(pmrClient.getProductHCode(sCode2)).thenReturn(Optional.of(productIdentifier2));

        NonCharacteristicExpressionEvaluator evaluator = new NonCharacteristicExpressionEvaluator(productCategoryCodeExpression, pmrClient, expedioClient, projectResource, cifAssetJPARepository);
        Object productCategoryCode = evaluator.evaluate(mockAsset1);
        verify(pmrClient, times(1)).getProductHCode(anyString());
        assertThat(productCategoryCode, is((Object)someCatCode));

        // Call again - should not call pmr client again
        productCategoryCode = evaluator.evaluate(mockAsset1);
        verify(pmrClient, times(2)).getProductHCode(anyString());
        assertThat(productCategoryCode, is((Object)someCatCode));

        // Call again with a different asset with a different sCode - SHOULD call pmr client again
        productCategoryCode = evaluator.evaluate(mockAsset2);
        verify(pmrClient, times(3)).getProductHCode(anyString());
        assertThat(productCategoryCode, is((Object)someOtherCatCode));
    }

    @Test
    public void shouldGetProductCategoryName(){
        String productCategoryNameExpression = "ProductCategoryName";
        String someCatName = "SomeCatName";
        String someOtherCatName = "SomeOtherCatName";
        String sCode1 = "S_CODE1";
        String sCode2 = "S_CODE2";
        ProductIdentifier productIdentifier1 = new ProductIdentifier("C1", someCatName, "V1");
        ProductIdentifier productIdentifier2 = new ProductIdentifier("C1", someOtherCatName, "V1");

        CIFAsset mockAsset1 = mock(CIFAsset.class);
        CIFAsset mockAsset2 = mock(CIFAsset.class);

        when(mockAsset1.getProductCode()).thenReturn(sCode1);
        when(pmrClient.getProductHCode(sCode1)).thenReturn(Optional.of(productIdentifier1));
        when(mockAsset2.getProductCode()).thenReturn(sCode2);
        when(pmrClient.getProductHCode(sCode2)).thenReturn(Optional.of(productIdentifier2));

        NonCharacteristicExpressionEvaluator evaluator = new NonCharacteristicExpressionEvaluator(productCategoryNameExpression, pmrClient, expedioClient, projectResource, cifAssetJPARepository);
        Object productCategoryCode = evaluator.evaluate(mockAsset1);
        verify(pmrClient, times(1)).getProductHCode(anyString());
        assertThat(productCategoryCode, is((Object)someCatName));

        // Call again - should not call pmr client again
        productCategoryCode = evaluator.evaluate(mockAsset1);
        verify(pmrClient, times(2)).getProductHCode(anyString());
        assertThat(productCategoryCode, is((Object)someCatName));

        // Call again with a different asset with a different sCode - SHOULD call pmr client again
        productCategoryCode = evaluator.evaluate(mockAsset2);
        verify(pmrClient, times(3)).getProductHCode(anyString());
        assertThat(productCategoryCode, is((Object)someOtherCatName));
    }

    @Test
    public void shouldGetSalesChannelAndCustomerName() {
        String salesChannelExpression = "SalesChannel";
        String salesChannelTypeExpression = "SalesChannelType";
        String customerNameExpression = "CustomerName";
        String salesChannelValue = "SomeSalesChannel";
        String salesChannelTypeValue = "SomeSalesChannelType";
        String customerNameValue = "SomeCustomerName";
        String customerId = "CUSTOMER_ID";
        String contractId = "CONTRACT_ID";

        CustomerClient customerClient = mock(CustomerClient.class);
        CustomerDTO customerDTO = mock(CustomerDTO.class);
        CIFAsset mockAsset1 = mock(CIFAsset.class);

        when(customerDTO.getSalesChannel()).thenReturn(salesChannelValue);
        when(customerDTO.getSalesChannelType()).thenReturn(salesChannelTypeValue);
        when(customerDTO.getName()).thenReturn(customerNameValue);
        when(customerClient.get(customerId, contractId)).thenReturn(customerDTO);
        when(expedioClient.getCustomerResource()).thenReturn(customerClient);
        when(mockAsset1.getCustomerId()).thenReturn(customerId);
        when(mockAsset1.getContractId()).thenReturn(contractId);

        NonCharacteristicExpressionEvaluator salesChannelEvaluator = new NonCharacteristicExpressionEvaluator(salesChannelExpression, pmrClient, expedioClient, projectResource, cifAssetJPARepository);
        NonCharacteristicExpressionEvaluator salesChannelTypeEvaluator = new NonCharacteristicExpressionEvaluator(salesChannelTypeExpression, pmrClient, expedioClient, projectResource, cifAssetJPARepository);
        NonCharacteristicExpressionEvaluator customerNameEvaluator = new NonCharacteristicExpressionEvaluator(customerNameExpression, pmrClient, expedioClient, projectResource, cifAssetJPARepository);

        Object salesChannel = salesChannelEvaluator.evaluate(mockAsset1);
        Object salesChannelType = salesChannelTypeEvaluator.evaluate(mockAsset1);
        Object customerName = customerNameEvaluator.evaluate(mockAsset1);

        assertThat(salesChannel, is((Object) salesChannelValue));
        assertThat(salesChannelType, is((Object) salesChannelTypeValue));
        assertThat(customerName, is((Object)customerNameValue));
    }

    @Test
    public void shouldGetSiteBasedDetailsAndCache() {
        CustomerClient customerClient = mock(CustomerClient.class);
        SiteClient siteClient = mock(SiteClient.class);

        SiteDTO site1DTO = mock(SiteDTO.class);
        when(site1DTO.getCountryISOCode()).thenReturn(COUNTRY_ISO_CODE_1);
        when(site1DTO.getCountryName()).thenReturn(COUNTRY_NAME_1);
        when(site1DTO.getSiteName()).thenReturn(SITE_NAME_1);
        SiteDTO site2DTO = mock(SiteDTO.class);
        when(site2DTO.getCountryISOCode()).thenReturn(COUNTRY_ISO_CODE_2);
        when(site2DTO.getCountryName()).thenReturn(COUNTRY_NAME_2);
        when(site2DTO.getSiteName()).thenReturn(SITE_NAME_2);

        String countryISOCodeExpression = "CountryISOCode";
        String countryNameExpression = "CountryName";
        String siteNameExpression = "SiteName";

        NonCharacteristicExpressionEvaluator countryISOCodeEvaluator = new NonCharacteristicExpressionEvaluator(countryISOCodeExpression, pmrClient, expedioClient, projectResource, cifAssetJPARepository);
        NonCharacteristicExpressionEvaluator countryNameEvaluator = new NonCharacteristicExpressionEvaluator(countryNameExpression, pmrClient, expedioClient, projectResource, cifAssetJPARepository);
        NonCharacteristicExpressionEvaluator siteNameEvaluator = new NonCharacteristicExpressionEvaluator(siteNameExpression, pmrClient, expedioClient, projectResource, cifAssetJPARepository);


        CIFAsset mockAsset1 = mock(CIFAsset.class);
        when(mockAsset1.getSiteId()).thenReturn(SITE_1);
        when(mockAsset1.getProjectId()).thenReturn(PROJECT_ID1);
        CIFAsset mockAsset2 = mock(CIFAsset.class);
        when(mockAsset2.getSiteId()).thenReturn(SITE_1);
        when(mockAsset2.getProjectId()).thenReturn(PROJECT_ID1);
        CIFAsset mockAsset3 = mock(CIFAsset.class);
        when(mockAsset3.getSiteId()).thenReturn(SITE_2);
        when(mockAsset3.getProjectId()).thenReturn(PROJECT_ID1);

        when(expedioClient.getCustomerResource()).thenReturn(customerClient);
        when(customerClient.siteResource(anyString())).thenReturn(siteClient);

        when(siteClient.get(eq(mockAsset1.getSiteId()), anyString())).thenReturn(site1DTO);
        when(siteClient.get(eq(mockAsset2.getSiteId()), anyString())).thenReturn(site1DTO);
        when(siteClient.get(eq(mockAsset3.getSiteId()), anyString())).thenReturn(site2DTO);

        Object countryISOCode1 = countryISOCodeEvaluator.evaluate(mockAsset1);
        // Different asset on same site
        Object countryISOCode2 = countryISOCodeEvaluator.evaluate(mockAsset2);
        // Different asset on different site
        Object countryISOCode3 = countryISOCodeEvaluator.evaluate(mockAsset3);

        assertThat((String)countryISOCode1, is(site1DTO.getCountryISOCode()));
        assertThat((String)countryISOCode2, is(site1DTO.getCountryISOCode()));
        assertThat((String)countryISOCode3, is(site2DTO.getCountryISOCode()));
        verify(siteClient, times(2)).get(SITE_1, PROJECT_ID1);
        verify(siteClient, times(1)).get(SITE_2, PROJECT_ID1);

        Object countryName = countryNameEvaluator.evaluate(mockAsset1);
        assertThat((String)countryName, is(site1DTO.getCountryName()));
        verify(siteClient, times(3)).get(SITE_1, PROJECT_ID1);
        verify(siteClient, times(1)).get(SITE_2, PROJECT_ID1);

        Object siteName = siteNameEvaluator.evaluate(mockAsset1);
        assertThat((String)siteName, is(site1DTO.getSiteName()));
        verify(siteClient, times(4)).get(SITE_1, PROJECT_ID1);
        verify(siteClient, times(1)).get(SITE_2, PROJECT_ID1);
    }

    @Test
    public void shouldGetSiteId() {
        CIFAsset mockAsset1 = mock(CIFAsset.class);
        when(mockAsset1.getSiteId()).thenReturn(SITE_1);

        String siteIdExpression = "AssetSiteId";
        NonCharacteristicExpressionEvaluator siteIdEvaluator = new NonCharacteristicExpressionEvaluator(siteIdExpression, pmrClient, expedioClient, projectResource, cifAssetJPARepository);

        Object siteIdValue = siteIdEvaluator.evaluate(mockAsset1);
        assertThat(siteIdValue, is((Object) SITE_1));
    }

    @Test
    public void shouldGetFixedAction() {
        CIFAsset mockAsset1 = mock(CIFAsset.class);
        when(mockAsset1.getStatus()).thenReturn(ProductInstanceState.CEASED);
        CIFAsset mockAsset2 = mock(CIFAsset.class);
        when(mockAsset2.getStatus()).thenReturn(ProductInstanceState.CANCELLED);
        CIFAsset mockAsset3 = mock(CIFAsset.class);
        when(mockAsset3.getStatus()).thenReturn(ProductInstanceState.LIVE);
        CIFAsset mockAsset4 = mock(CIFAsset.class);
        when(mockAsset4.getStatus()).thenReturn(ProductInstanceState.REMOVED);

        String fixedActionExpression = "FixedAction";
        NonCharacteristicExpressionEvaluator fixedActionEvaluator1 = new NonCharacteristicExpressionEvaluator(fixedActionExpression, pmrClient, expedioClient, projectResource, cifAssetJPARepository);

        Object fixedAssetValue1 = fixedActionEvaluator1.evaluate(mockAsset1);
        Object fixedAssetValue2 = fixedActionEvaluator1.evaluate(mockAsset2);
        Object fixedAssetValue3 = fixedActionEvaluator1.evaluate(mockAsset3);
        Object fixedAssetValue4 = fixedActionEvaluator1.evaluate(mockAsset4);
        assertThat(fixedAssetValue1, is((Object) FixedAction.Delete));
        assertThat(fixedAssetValue2, is((Object) FixedAction.Add));
        assertThat(fixedAssetValue3, is((Object) FixedAction.Add));
        assertThat(fixedAssetValue4, is((Object) FixedAction.Add));
    }

    @Test
    public void shouldGetAction() {
        CIFAsset mockAsIsAsset = mock(CIFAsset.class);
        CIFAsset mockAsset1 = mock(CIFAsset.class);
        when(mockAsset1.getStatus()).thenReturn(ProductInstanceState.CEASED);
        when(mockAsset1.getAsIsAsset()).thenReturn(mockAsIsAsset);
        CIFAsset mockAsset2 = mock(CIFAsset.class);
        when(mockAsset2.getStatus()).thenReturn(ProductInstanceState.CANCELLED);
        when(mockAsset2.getAsIsAsset()).thenReturn(null);
        CIFAsset mockAsset3 = mock(CIFAsset.class);
        when(mockAsset3.getStatus()).thenReturn(ProductInstanceState.LIVE);
        when(mockAsset3.getAsIsAsset()).thenReturn(mockAsIsAsset);
        CIFAsset mockAsset4 = mock(CIFAsset.class);
        when(mockAsset4.getStatus()).thenReturn(ProductInstanceState.LIVE);
        when(mockAsset4.getAsIsAsset()).thenReturn(null);

        String actionExpression = "Action";
        NonCharacteristicExpressionEvaluator actionEvaluator = new NonCharacteristicExpressionEvaluator(actionExpression, pmrClient, expedioClient, projectResource, cifAssetJPARepository);

        Object action1 = actionEvaluator.evaluate(mockAsset1);
        Object action2 = actionEvaluator.evaluate(mockAsset2);
        Object action3 = actionEvaluator.evaluate(mockAsset3);
        Object action4 = actionEvaluator.evaluate(mockAsset4);
        assertThat(action1, is((Object) ChangeType.DELETE.getValue()));
        assertThat(action2, is((Object) ChangeType.ADD.getValue()));
        assertThat(action3, is((Object) ChangeType.UPDATE.getValue()));
        assertThat(action4, is((Object) ChangeType.ADD.getValue()));
    }

    @Test
    public void shouldGetSubscriberId() {
        CIFAsset mockAsset1 = mock(CIFAsset.class);
        when(mockAsset1.getAssetUniqueId()).thenReturn("1234567890"); // Length 10
        CIFAsset mockAsset2 = mock(CIFAsset.class);
        when(mockAsset2.getAssetUniqueId()).thenReturn("12345678901"); // Length 11
        CIFAsset mockAsset3 = mock(CIFAsset.class);
        when(mockAsset3.getAssetUniqueId()).thenReturn("123456789012"); // Length 12

        String subscriberIdAction = "SubscriberId";
        NonCharacteristicExpressionEvaluator subscriberIdEvaluator = new NonCharacteristicExpressionEvaluator(subscriberIdAction, pmrClient, expedioClient, projectResource, cifAssetJPARepository);

        Object subscriberIdValue1 = subscriberIdEvaluator.evaluate(mockAsset1);
        Object subscriberIdValue2 = subscriberIdEvaluator.evaluate(mockAsset2);
        Object subscriberIdValue3 = subscriberIdEvaluator.evaluate(mockAsset3);
        assertThat(subscriberIdValue1, is((Object) "01234567890"));
        assertThat(subscriberIdValue2, is((Object) "12345678901"));
        assertThat(subscriberIdValue3, is((Object) "12345678901"));
    }

    @Test
    public void shouldGetEmptyBillingIdWhenNoOrders() {
        String billingIdValue = "";
        List<OrderDTO> orderDTOs = new ArrayList<OrderDTO>();
        QuoteOptionResource quoteOptionResource = mock(QuoteOptionResource.class);
        OrderResource orderResource = mock(OrderResource.class);
        when(projectResource.quoteOptionResource(PROJECT_ID1)).thenReturn(quoteOptionResource);
        when(quoteOptionResource.quoteOptionOrderResource(QUOTE_OPTION_ID1)).thenReturn(orderResource);
        when(orderResource.getAll()).thenReturn(orderDTOs);

        CIFAsset mockAsset1 = mock(CIFAsset.class);
        when(mockAsset1.getProjectId()).thenReturn(PROJECT_ID1);
        when(mockAsset1.getQuoteOptionId()).thenReturn(QUOTE_OPTION_ID1);
        when(mockAsset1.getLineItemId()).thenReturn(LINE_ITEM_ID1);

        String subscriberIdExpression = "BillingId";
        NonCharacteristicExpressionEvaluator billingIdEvaluator = new NonCharacteristicExpressionEvaluator(subscriberIdExpression, pmrClient, expedioClient, projectResource, cifAssetJPARepository);

        Object billingId = billingIdEvaluator.evaluate(mockAsset1);
        assertThat(billingId, is((Object)billingIdValue));
    }

    @Test
    public void shouldGetEmptyBillingIdWhenNoOrderItems() {
        String billingIdValue = "";
        OrderDTO orderDTO = mock(OrderDTO.class);
        when(orderDTO.getOrderItems()).thenReturn(new ArrayList<QuoteOptionItemDTO>());
        List<OrderDTO> orderDTOs = newArrayList(orderDTO);
        QuoteOptionResource quoteOptionResource = mock(QuoteOptionResource.class);
        OrderResource orderResource = mock(OrderResource.class);
        when(projectResource.quoteOptionResource(PROJECT_ID1)).thenReturn(quoteOptionResource);
        when(quoteOptionResource.quoteOptionOrderResource(QUOTE_OPTION_ID1)).thenReturn(orderResource);
        when(orderResource.getAll()).thenReturn(orderDTOs);

        CIFAsset mockAsset1 = mock(CIFAsset.class);
        when(mockAsset1.getProjectId()).thenReturn(PROJECT_ID1);
        when(mockAsset1.getQuoteOptionId()).thenReturn(QUOTE_OPTION_ID1);
        when(mockAsset1.getLineItemId()).thenReturn(LINE_ITEM_ID1);

        String subscriberIdExpression = "BillingId";
        NonCharacteristicExpressionEvaluator billingIdEvaluator = new NonCharacteristicExpressionEvaluator(subscriberIdExpression, pmrClient, expedioClient, projectResource, cifAssetJPARepository);

        Object billingId = billingIdEvaluator.evaluate(mockAsset1);
        assertThat(billingId, is((Object)billingIdValue));
    }

    @Test
    public void shouldGetEmptyBillingIdWhenNoOrderItemWithMatchingLineItemId() {
        String billingIdValue = "";
        OrderDTO orderDTO = mock(OrderDTO.class);
        QuoteOptionItemDTO quoteOptionItem = mock(QuoteOptionItemDTO.class);
        when(quoteOptionItem.getId()).thenReturn(LINE_ITEM_ID2);
        when(orderDTO.getOrderItems()).thenReturn(newArrayList(quoteOptionItem));
        List<OrderDTO> orderDTOs = newArrayList(orderDTO);
        QuoteOptionResource quoteOptionResource = mock(QuoteOptionResource.class);
        OrderResource orderResource = mock(OrderResource.class);
        when(projectResource.quoteOptionResource(PROJECT_ID1)).thenReturn(quoteOptionResource);
        when(quoteOptionResource.quoteOptionOrderResource(QUOTE_OPTION_ID1)).thenReturn(orderResource);
        when(orderResource.getAll()).thenReturn(orderDTOs);

        CIFAsset mockAsset1 = mock(CIFAsset.class);
        when(mockAsset1.getProjectId()).thenReturn(PROJECT_ID1);
        when(mockAsset1.getQuoteOptionId()).thenReturn(QUOTE_OPTION_ID1);
        when(mockAsset1.getLineItemId()).thenReturn(LINE_ITEM_ID1);

        String subscriberIdExpression = "BillingId";
        NonCharacteristicExpressionEvaluator billingIdEvaluator = new NonCharacteristicExpressionEvaluator(subscriberIdExpression, pmrClient, expedioClient, projectResource, cifAssetJPARepository);

        Object billingId = billingIdEvaluator.evaluate(mockAsset1);
        assertThat(billingId, is((Object)billingIdValue));
    }

    @Test
    public void shouldGetBillingIdWhenOrderItemWithMatchingLineItemId() {
        String billingIdValue = "BillingId";
        OrderDTO orderDTO = mock(OrderDTO.class);
        QuoteOptionItemDTO quoteOptionItem = mock(QuoteOptionItemDTO.class);
        when(quoteOptionItem.getId()).thenReturn(LINE_ITEM_ID1);
        when(quoteOptionItem.getBillingId()).thenReturn(billingIdValue);
        when(orderDTO.getOrderItems()).thenReturn(newArrayList(quoteOptionItem));
        List<OrderDTO> orderDTOs = newArrayList(orderDTO);
        QuoteOptionResource quoteOptionResource = mock(QuoteOptionResource.class);
        OrderResource orderResource = mock(OrderResource.class);
        when(projectResource.quoteOptionResource(PROJECT_ID1)).thenReturn(quoteOptionResource);
        when(quoteOptionResource.quoteOptionOrderResource(QUOTE_OPTION_ID1)).thenReturn(orderResource);
        when(orderResource.getAll()).thenReturn(orderDTOs);

        CIFAsset mockAsset1 = mock(CIFAsset.class);
        when(mockAsset1.getProjectId()).thenReturn(PROJECT_ID1);
        when(mockAsset1.getQuoteOptionId()).thenReturn(QUOTE_OPTION_ID1);
        when(mockAsset1.getLineItemId()).thenReturn(LINE_ITEM_ID1);

        String subscriberIdExpression = "BillingId";
        NonCharacteristicExpressionEvaluator billingIdEvaluator = new NonCharacteristicExpressionEvaluator(subscriberIdExpression, pmrClient, expedioClient, projectResource, cifAssetJPARepository);

        Object billingId = billingIdEvaluator.evaluate(mockAsset1);
        assertThat(billingId, is((Object)billingIdValue));
    }

    @Test
    public void shouldGetLegacyIdentifier(){
        String legacyIdVal = "LegacyIdValue";

        CIFAsset mockAsset1 = mock(CIFAsset.class);
        when(mockAsset1.getOfferingDetail()).thenReturn(new CIFAssetOfferingDetail("PRODUCT_NAME", "DisplayName", "GroupName", legacyIdVal, true, false, "PROPOSITION", true, true, null));

        String legacyIdExpression = "LegacyIdentifier";
        NonCharacteristicExpressionEvaluator legacyIdEvaluator = new NonCharacteristicExpressionEvaluator(legacyIdExpression, pmrClient, expedioClient, projectResource, cifAssetJPARepository);

        Object legacyId = legacyIdEvaluator.evaluate(mockAsset1);
        assertThat(legacyId, is((Object)legacyIdVal));
    }

    @Test
    public void shouldGetProductName(){
        CIFAsset mockAsset1 = mock(CIFAsset.class);
        when(mockAsset1.getOfferingDetail()).thenReturn(new CIFAssetOfferingDetail("PRODUCT_NAME", "DisplayName", "GroupName", null, true, false, "PROPOSITION", true, true, null));

        NonCharacteristicExpressionEvaluator legacyIdEvaluator = new NonCharacteristicExpressionEvaluator(ProductName.name(), pmrClient, expedioClient, projectResource, cifAssetJPARepository);

        Object legacyId = legacyIdEvaluator.evaluate(mockAsset1);
        assertThat(legacyId, is((Object)"PRODUCT_NAME"));
    }

    @Test
    public void shouldGetProposition(){
        String propositionVal = "PropositionValue";

        CIFAsset mockAsset1 = mock(CIFAsset.class);
        when(mockAsset1.getOfferingDetail()).thenReturn(new CIFAssetOfferingDetail("PRODUCT_NAME", "DisplayName", "GroupName", "LEGACY_ID", true, false, propositionVal, true, true, null));

        String propositionExpression = "Proposition";
        NonCharacteristicExpressionEvaluator propositionEvaluator = new NonCharacteristicExpressionEvaluator(propositionExpression, pmrClient, expedioClient, projectResource, cifAssetJPARepository);

        Object legacyId = propositionEvaluator.evaluate(mockAsset1);
        assertThat(legacyId, is((Object)propositionVal));
    }

    @Test
    public void shouldGetMoveType() {
        CIFAsset mockAsset1 = mock(CIFAsset.class);
        CIFAsset mockAsset2 = mock(CIFAsset.class);
        CIFAsset mockAsset3 = mock(CIFAsset.class);
        when(mockAsset1.getAssetSubProcessType()).thenReturn(AssetProcessType.SAME_SITE);
        when(mockAsset2.getAssetSubProcessType()).thenReturn(AssetProcessType.DIFFERENT_SITE);
        when(mockAsset3.getAssetSubProcessType()).thenReturn(AssetProcessType.CHANGE);

        String moveTypeExpression = "MoveType";
        NonCharacteristicExpressionEvaluator moveTypeEvaluator = new NonCharacteristicExpressionEvaluator(moveTypeExpression, pmrClient, expedioClient, projectResource, cifAssetJPARepository);

        Object moveType1 = moveTypeEvaluator.evaluate(mockAsset1);
        assertThat(moveType1, is((Object)AssetProcessType.MOVE_IN_CAMPUS.value()));
        Object moveType2 = moveTypeEvaluator.evaluate(mockAsset2);
        assertThat(moveType2, is((Object)AssetProcessType.MOVE_IN_COUNTRY.value()));
        Object moveType3 = moveTypeEvaluator.evaluate(mockAsset3);
        assertThat(moveType3, is((Object)AssetProcessType.NOT_APPLICABLE.value()));
    }

    @Test
    public void shouldGetProductCode() {
        CIFAsset cifAsset = mock(CIFAsset.class);
        String characteristicName = "ProductCode";
        NonCharacteristicExpressionEvaluator evaluator = new NonCharacteristicExpressionEvaluator(characteristicName, pmrClient, expedioClient, projectResource, cifAssetJPARepository);
        when(cifAsset.getProductCode()).thenReturn(PRODUCT_CODE);

        Object productCode = evaluator.evaluate(cifAsset);

        assertThat((String)productCode, is(PRODUCT_CODE));
    }

    @Test
    public void shouldGetProjectId() {
        CIFAsset cifAsset = mock(CIFAsset.class);
        String characteristicName = "ProjectId";
        NonCharacteristicExpressionEvaluator evaluator = new NonCharacteristicExpressionEvaluator(characteristicName, pmrClient, expedioClient, projectResource, cifAssetJPARepository);
        when(cifAsset.getProjectId()).thenReturn(PROJECT_ID1);

        Object projectId = evaluator.evaluate(cifAsset);

        assertThat((String)projectId, is(PROJECT_ID1));
    }

    @Test
    public void shouldGetCustomerId() {
        CIFAsset cifAsset = mock(CIFAsset.class);
        String characteristicName = "CustomerId";
        NonCharacteristicExpressionEvaluator evaluator = new NonCharacteristicExpressionEvaluator(characteristicName, pmrClient, expedioClient, projectResource, cifAssetJPARepository);
        when(cifAsset.getCustomerId()).thenReturn("aCustomerId");

        Object projectId = evaluator.evaluate(cifAsset);

        assertThat((String)projectId, is("aCustomerId"));
    }

    @Test
    public void shouldGetOutOfOfficeInstallationHoursFroNonMoveAsset() {
        CIFAsset cifAsset = mock(CIFAsset.class);
        String characteristicName = "InstallationHours";
        NonCharacteristicExpressionEvaluator evaluator = new NonCharacteristicExpressionEvaluator(characteristicName, pmrClient, expedioClient, projectResource, cifAssetJPARepository);
        when(cifAsset.getAssetProcessType()).thenReturn(AssetProcessType.NOT_APPLICABLE);

        Object installationHours = evaluator.evaluate(cifAsset);

        assertThat((String)installationHours, is("Out of Office Hours"));
    }

    @Test
    public void shouldGetOfficeInstallationHoursForMoveAsset() {
        CIFAsset cifAsset = mock(CIFAsset.class);
        String characteristicName = "InstallationHours";
        NonCharacteristicExpressionEvaluator evaluator = new NonCharacteristicExpressionEvaluator(characteristicName, pmrClient, expedioClient, projectResource, cifAssetJPARepository);
        when(cifAsset.getAssetProcessType()).thenReturn(AssetProcessType.MOVE);

        Object installationHours = evaluator.evaluate(cifAsset);

        assertThat((String)installationHours, is("Business Hours"));
    }
}