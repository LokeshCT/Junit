package com.bt.rsqe.projectengine.web.quoteoptionorders.ecrfsheet;


import com.bt.rsqe.client.InstanceClient;
import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.filter.AssetFilter;
import com.bt.rsqe.customerinventory.parameter.ContractId;
import com.bt.rsqe.customerinventory.parameter.CustomerId;
import com.bt.rsqe.customerinventory.parameter.ProductCode;
import com.bt.rsqe.customerinventory.parameter.ProductVersion;
import com.bt.rsqe.customerinventory.parameter.SiteId;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.AvailableAsset;
import com.bt.rsqe.domain.bom.fixtures.ProductOfferingFixture;
import com.bt.rsqe.domain.product.Cardinality;
import com.bt.rsqe.domain.product.DefaultProductInstanceFixture;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.extensions.ExpressionExpectedResultType;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.expedio.fixtures.SiteDTOFixture;
import com.bt.rsqe.factory.ServiceLocator;
import com.bt.rsqe.projectengine.web.facades.SiteFacade;
import com.bt.rsqe.domain.product.extensions.Expression;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;

import static com.bt.rsqe.factory.ServiceLocator.serviceLocatorInstance;
import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class CardinalityValidatorTest {
    public static final String CUSTOMER_ID = "aCustomerId";
    public static final String CONTRACT_ID = "aContractId";
    public static final String QUOTE_OPTION_ID = "aQuoteOptionId";
    public static final String ASSET_ID = "anAssetId";
    public static final String SITE_ID = "1";
    public static final String CENTRAL_SITE_ID = "2";
    public static final String PROJECT_ID = "aProjectId";
    public static final AssetKey ASSET_KEY = new AssetKey("newAssetId", 1L);
    private ProductInstanceClient productInstanceClient;
    private CardinalityValidator cardinalityValidator;
    private SiteFacade siteFacade;
    private InstanceClient instanceClient;

    @Before
    public void setUp() {
        instanceClient = mock(InstanceClient.class);
        serviceLocatorInstance().register(instanceClient);
        productInstanceClient = mock(ProductInstanceClient.class);
        siteFacade = mock(SiteFacade.class);
        cardinalityValidator = new CardinalityValidator(productInstanceClient, siteFacade);
    }

    @Test
    public void shouldValidateContractCardinalityAndReturnSuccessMessage() {
        //Given
        ProductOffering offering = new ProductOfferingFixture("S1").withContractCardinality(new Cardinality(0, 2, null)).build();
        when(productInstanceClient.getContractAssets(eq(new CustomerId(CUSTOMER_ID)), eq(new ContractId(CONTRACT_ID)),
                                                     eq(new ProductCode(offering.getProductIdentifier().getProductId())),
                                                     eq(new ProductVersion(offering.getProductIdentifier().getVersionNumber())),
                                                     any(AssetFilter.class), any(AssetFilter.class))).thenReturn(newArrayList(new AvailableAsset(ASSET_ID, 1L)));
        //When
        CardinalityValidationResult cardinalityValidationResult = cardinalityValidator.validateContractCardinality(CUSTOMER_ID, CONTRACT_ID, QUOTE_OPTION_ID, offering, 1, ASSET_KEY);

        //Then
        assertThat(cardinalityValidationResult, is(new CardinalityValidationResult(true, "")));
    }

    @Test
    public void shouldValidateContractCardinalityUSingExpressionAndReturnSuccessMessage() {

        ProductInstance productInstance = DefaultProductInstanceFixture.aProductInstance().withAttributes(new HashMap<String, Object>(){{put("name","value");}}).build();
        ProductInstance productInstance1 = DefaultProductInstanceFixture.aProductInstance().withAttributes(new HashMap<String, Object>(){{put("name","value");}}).build();

        //Given
        ProductOffering offering = new ProductOfferingFixture("S1").withContractCardinality(new Cardinality(0, 0, new Expression("count(SCode.name)", ExpressionExpectedResultType.Double))).build();

        when(productInstanceClient.getContractAssets(eq(new CustomerId(CUSTOMER_ID)), eq(new ContractId(CONTRACT_ID)),
                                                     eq(new ProductCode(offering.getProductIdentifier().getProductId())),
                                                     eq(new ProductVersion(offering.getProductIdentifier().getVersionNumber())),
                                                     any(AssetFilter.class), any(AssetFilter.class))).thenReturn(newArrayList(new AvailableAsset(ASSET_ID, 1L)));

        when(instanceClient.getCustomerAssets(CUSTOMER_ID, "SCode")).thenReturn(newArrayList(productInstance, productInstance1));

        //When
        CardinalityValidationResult cardinalityValidationResult = cardinalityValidator.validateContractCardinality(CUSTOMER_ID, CONTRACT_ID, QUOTE_OPTION_ID, offering, 1, ASSET_KEY);

        //Then
        verify(siteFacade, times(0)).getCentralSite(CUSTOMER_ID, PROJECT_ID);
        assertThat(cardinalityValidationResult, is(new CardinalityValidationResult(true, "")));

    }

    @Test
    public void shouldValidateContractCardinalityUSingExpressionAndReturnFailureMessage() {

        ProductInstance productInstance = DefaultProductInstanceFixture.aProductInstance().withAttributes(new HashMap<String, Object>(){{put("name","value");}}).build();
        ProductInstance productInstance1 = DefaultProductInstanceFixture.aProductInstance().withAttributes(new HashMap<String, Object>(){{put("name","value");}}).build();

        //Given
        ProductOffering offering = new ProductOfferingFixture("S1").withContractCardinality(new Cardinality(0, 0, new Expression("count(SCode.name)", ExpressionExpectedResultType.Double))).build();

        when(productInstanceClient.getContractAssets(eq(new CustomerId(CUSTOMER_ID)), eq(new ContractId(CONTRACT_ID)),
                                                     eq(new ProductCode(offering.getProductIdentifier().getProductId())),
                                                     eq(new ProductVersion(offering.getProductIdentifier().getVersionNumber())),
                                                     any(AssetFilter.class), any(AssetFilter.class))).thenReturn(newArrayList(new AvailableAsset(ASSET_ID, 1L), new AvailableAsset(ASSET_ID, 2L)));

        when(instanceClient.getCustomerAssets(CUSTOMER_ID, "SCode")).thenReturn(newArrayList(productInstance, productInstance1));

        //When
        CardinalityValidationResult cardinalityValidationResult = cardinalityValidator.validateContractCardinality(CUSTOMER_ID, CONTRACT_ID, QUOTE_OPTION_ID, offering, 1, ASSET_KEY);

        //Then
        verify(siteFacade, times(0)).getCentralSite(CUSTOMER_ID, PROJECT_ID);
        assertThat(cardinalityValidationResult, is(new CardinalityValidationResult(false, "Contract Cardinality Failed - test can have only 0 instance(s) for the Customer.")));

    }

    @Test
    public void shouldValidateContractCardinalityAndReturnSuccessMessageIfAvailableAssetIsNull() {
        //Given
        ProductOffering offering = new ProductOfferingFixture("S1").withContractCardinality(new Cardinality(0, 2, null)).build();
        when(productInstanceClient.getContractAssets(eq(new CustomerId(CUSTOMER_ID)), eq(new ContractId(CONTRACT_ID)),
                                                     eq(new ProductCode(offering.getProductIdentifier().getProductId())),
                                                     eq(new ProductVersion(offering.getProductIdentifier().getVersionNumber())),
                                                     any(AssetFilter.class), any(AssetFilter.class))).thenReturn(newArrayList(new AvailableAsset(ASSET_ID, 1L)));
        //When
        CardinalityValidationResult cardinalityValidationResult = cardinalityValidator.validateContractCardinality(CUSTOMER_ID, CONTRACT_ID, QUOTE_OPTION_ID, offering, 1, null);

        //Then
        assertThat(cardinalityValidationResult, is(new CardinalityValidationResult(true, "")));
    }

    @Test
    public void shouldValidateContractCardinalityAndReturnErrorMessageIfFailed() {
        //Given
        ProductOffering offering = new ProductOfferingFixture("S1").withContractCardinality(new Cardinality(0, 1, null)).build();
        when(productInstanceClient.getContractAssets(eq(new CustomerId(CUSTOMER_ID)), eq(new ContractId(CONTRACT_ID)),
                                                     eq(new ProductCode(offering.getProductIdentifier().getProductId())),
                                                     eq(new ProductVersion(offering.getProductIdentifier().getVersionNumber())),
                                                     any(AssetFilter.class), any(AssetFilter.class))).thenReturn(newArrayList(new AvailableAsset(ASSET_ID, 1L)));
        //When
        CardinalityValidationResult cardinalityValidationResult = cardinalityValidator.validateContractCardinality(CUSTOMER_ID, CONTRACT_ID, QUOTE_OPTION_ID, offering, 1, ASSET_KEY);

        //Then
        assertThat(cardinalityValidationResult, is(new CardinalityValidationResult(false, "Contract Cardinality Failed - test can have only 1 instance(s) for the Customer.")));
        assertThat(cardinalityValidationResult.isFailed(), is(true));

    }

    @Test
    public void shouldPassContractCardinalityAfterIgnoringTheCurrentAssetFromContractAssets() {
        //Case: During ecrf import, the asset which ill be imported will be part of asset being returned as contract Assets, so it should be ignored and validated.
        //Given
        ProductOffering offering = new ProductOfferingFixture("S1").withContractCardinality(new Cardinality(0, 1, null)).build();
        when(productInstanceClient.getContractAssets(eq(new CustomerId(CUSTOMER_ID)), eq(new ContractId(CONTRACT_ID)),
                                                     eq(new ProductCode(offering.getProductIdentifier().getProductId())),
                                                     eq(new ProductVersion(offering.getProductIdentifier().getVersionNumber())),
                                                     any(AssetFilter.class), any(AssetFilter.class))).thenReturn(newArrayList(new AvailableAsset(ASSET_ID, 1L)));
        //When
        CardinalityValidationResult cardinalityValidationResult = cardinalityValidator.validateContractCardinality(CUSTOMER_ID, CONTRACT_ID, QUOTE_OPTION_ID, offering, 1, new AssetKey("anAssetId", 1L));

        //Then
        assertThat(cardinalityValidationResult, is(new CardinalityValidationResult(true, "")));
    }


    @Test
    public void shouldFailValidateContractCardinalityAfterIgnoringTheCurrentAssetFromContractAssets() {
        //Case: During ecrf import, the asset which ill be imported will be part of asset being returned as contract Assets, so it should be ignored and validated.
        //Given
        ProductOffering offering = new ProductOfferingFixture("S1").withContractCardinality(new Cardinality(0, 1, null)).build();
        when(productInstanceClient.getContractAssets(eq(new CustomerId(CUSTOMER_ID)), eq(new ContractId(CONTRACT_ID)),
                                                     eq(new ProductCode(offering.getProductIdentifier().getProductId())),
                                                     eq(new ProductVersion(offering.getProductIdentifier().getVersionNumber())),
                                                     any(AssetFilter.class), any(AssetFilter.class))).thenReturn(newArrayList(new AvailableAsset(ASSET_ID, 1L)));
        //When
        CardinalityValidationResult cardinalityValidationResult = cardinalityValidator.validateContractCardinality(CUSTOMER_ID, CONTRACT_ID, QUOTE_OPTION_ID, offering, 1, new AssetKey("newAssetId", 1L));

        //Then
        assertThat(cardinalityValidationResult, is(new CardinalityValidationResult(false, "Contract Cardinality Failed - test can have only 1 instance(s) for the Customer.")));
        assertThat(cardinalityValidationResult.isFailed(), is(true));

    }

    @Test
    public void shouldValidateSiteCardinalityAndReturnSuccessMessage() {
        //Given
        ProductOffering offering = new ProductOfferingFixture("S1").withSiteCardinality(new Cardinality(0, 3, null)).build();
        when(productInstanceClient.getApprovedAssets(eq(new SiteId(SITE_ID)), eq(new ProductCode(offering.getProductIdentifier().getProductId())), eq(new ProductVersion(offering.getProductIdentifier().getVersionNumber())))).thenReturn(newArrayList(new AvailableAsset(ASSET_ID, 1L)));
        when(productInstanceClient.getDraftAssets(eq(new SiteId(SITE_ID)), eq(new ProductCode(offering.getProductIdentifier().getProductId())), eq(new ProductVersion(offering.getProductIdentifier().getVersionNumber())), eq(QUOTE_OPTION_ID))).thenReturn(newArrayList(new AvailableAsset(ASSET_ID, 1L)));

        //When
        CardinalityValidationResult cardinalityValidationResult = cardinalityValidator.validateSiteCardinality(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, SITE_ID, offering, 1, ASSET_KEY);

        //Then
        verify(siteFacade, times(0)).getCentralSite(CUSTOMER_ID, PROJECT_ID);
        assertThat(cardinalityValidationResult, is(new CardinalityValidationResult(true, "")));

    }


    @Test
    public void shouldValidateSiteCardinalityUSingExpressionAndReturnSuccessMessage() {

        ProductInstance productInstance = DefaultProductInstanceFixture.aProductInstance().withAttributes(new HashMap<String, Object>(){{put("name","value");}}).build();
        ProductInstance productInstance1 = DefaultProductInstanceFixture.aProductInstance().withAttributes(new HashMap<String, Object>(){{put("name","value");}}).build();
        ProductInstance productInstance2 = DefaultProductInstanceFixture.aProductInstance().withAttributes(new HashMap<String, Object>(){{put("name","value");}}).build();
        ProductInstance productInstance3 = DefaultProductInstanceFixture.aProductInstance().withAttributes(new HashMap<String, Object>(){{put("name","value");}}).build();

        when(instanceClient.getCustomerAssets(CUSTOMER_ID, "SCode")).thenReturn(newArrayList(productInstance, productInstance1, productInstance2, productInstance3));

        //Given
        ProductOffering offering = new ProductOfferingFixture("S1").withSiteCardinality(new Cardinality(0, 0, new Expression("count(SCode.name)", ExpressionExpectedResultType.Double))).build();

        when(productInstanceClient.getApprovedAssets(eq(new SiteId(SITE_ID)), eq(new ProductCode(offering.getProductIdentifier().getProductId())), eq(new ProductVersion(offering.getProductIdentifier().getVersionNumber())))).thenReturn(newArrayList(new AvailableAsset(ASSET_ID, 1L)));
        when(productInstanceClient.getDraftAssets(eq(new SiteId(SITE_ID)), eq(new ProductCode(offering.getProductIdentifier().getProductId())), eq(new ProductVersion(offering.getProductIdentifier().getVersionNumber())), eq(QUOTE_OPTION_ID))).thenReturn(newArrayList(new AvailableAsset(ASSET_ID, 1L)));

        //When
        CardinalityValidationResult cardinalityValidationResult = cardinalityValidator.validateSiteCardinality(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, SITE_ID, offering, 1, ASSET_KEY);

        //Then
        verify(siteFacade, times(0)).getCentralSite(CUSTOMER_ID, PROJECT_ID);
        assertThat(cardinalityValidationResult, is(new CardinalityValidationResult(true, "")));

    }

    @Test
    public void shouldValidateSiteCardinalityUSingExpressionAndReturnFailureMessage() {

        ProductInstance productInstance = DefaultProductInstanceFixture.aProductInstance().withAttributes(new HashMap<String, Object>(){{put("name","value");}}).build();
        ProductInstance productInstance1 = DefaultProductInstanceFixture.aProductInstance().withAttributes(new HashMap<String, Object>(){{put("name","value");}}).build();

        when(instanceClient.getCustomerAssets(CUSTOMER_ID, "SCode")).thenReturn(newArrayList(productInstance, productInstance1));

        //Given
        ProductOffering offering = new ProductOfferingFixture("S1").withSiteCardinality(new Cardinality(0, 0, new Expression("count(SCode.name)", ExpressionExpectedResultType.Double))).build();

        when(productInstanceClient.getApprovedAssets(eq(new SiteId(SITE_ID)), eq(new ProductCode(offering.getProductIdentifier().getProductId())), eq(new ProductVersion(offering.getProductIdentifier().getVersionNumber())))).thenReturn(newArrayList(new AvailableAsset(ASSET_ID, 1L)));
        when(productInstanceClient.getDraftAssets(eq(new SiteId(SITE_ID)), eq(new ProductCode(offering.getProductIdentifier().getProductId())), eq(new ProductVersion(offering.getProductIdentifier().getVersionNumber())), eq(QUOTE_OPTION_ID))).thenReturn(newArrayList(new AvailableAsset(ASSET_ID, 1L)));

        //When
        CardinalityValidationResult cardinalityValidationResult = cardinalityValidator.validateSiteCardinality(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, SITE_ID, offering, 1, ASSET_KEY);

        //Then
        verify(siteFacade, times(0)).getCentralSite(CUSTOMER_ID, PROJECT_ID);
        assertThat(cardinalityValidationResult, is(new CardinalityValidationResult(false, "Site Cardinality Failed - test can have only 2 instance(s) for the Customer.")));
    }


    @Test
    public void shouldValidateSiteCardinalityAndReturnSuccessMessageIfAvailableAssetIsNull() {
        //Given
        ProductOffering offering = new ProductOfferingFixture("S1").withSiteCardinality(new Cardinality(0, 3, null)).build();
        when(productInstanceClient.getApprovedAssets(eq(new SiteId(SITE_ID)), eq(new ProductCode(offering.getProductIdentifier().getProductId())), eq(new ProductVersion(offering.getProductIdentifier().getVersionNumber())))).thenReturn(newArrayList(new AvailableAsset(ASSET_ID, 1L)));
        when(productInstanceClient.getDraftAssets(eq(new SiteId(SITE_ID)), eq(new ProductCode(offering.getProductIdentifier().getProductId())), eq(new ProductVersion(offering.getProductIdentifier().getVersionNumber())), eq(QUOTE_OPTION_ID))).thenReturn(newArrayList(new AvailableAsset(ASSET_ID, 1L)));

        //When
        CardinalityValidationResult cardinalityValidationResult = cardinalityValidator.validateSiteCardinality(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, SITE_ID, offering, 1, null);

        //Then
        verify(siteFacade, times(0)).getCentralSite(CUSTOMER_ID, PROJECT_ID);
        assertThat(cardinalityValidationResult, is(new CardinalityValidationResult(true, "")));
    }

    @Test
    public void shouldRetrieveCentralSiteIdAndValidateSiteCardinality() {
        //Given
        ProductOffering offering = new ProductOfferingFixture("S1").withSiteCardinality(new Cardinality(0, 3, null)).build();
        when(siteFacade.getCentralSite(CUSTOMER_ID, PROJECT_ID)).thenReturn(SiteDTOFixture.aSiteDTO().withBfgSiteId(CENTRAL_SITE_ID).build());
        when(productInstanceClient.getApprovedAssets(eq(new SiteId(CENTRAL_SITE_ID)), eq(new ProductCode(offering.getProductIdentifier().getProductId())), eq(new ProductVersion(offering.getProductIdentifier().getVersionNumber())))).thenReturn(newArrayList(new AvailableAsset(ASSET_ID, 1L)));
        when(productInstanceClient.getDraftAssets(eq(new SiteId(CENTRAL_SITE_ID)), eq(new ProductCode(offering.getProductIdentifier().getProductId())), eq(new ProductVersion(offering.getProductIdentifier().getVersionNumber())), eq(QUOTE_OPTION_ID))).thenReturn(newArrayList(new AvailableAsset(ASSET_ID, 1L)));

        //When
        CardinalityValidationResult cardinalityValidationResult = cardinalityValidator.validateSiteCardinality(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, null, offering, 1, ASSET_KEY);

        //Then
        verify(siteFacade, times(1)).getCentralSite(CUSTOMER_ID, PROJECT_ID);
        assertThat(cardinalityValidationResult, is(new CardinalityValidationResult(true, "")));

    }

    @Test
    public void shouldValidateSiteCardinalityAndReturnErrorMessageIfFailed() {
        //Given
        ProductOffering offering = new ProductOfferingFixture("S1").withSiteCardinality(new Cardinality(0, 2, null)).build();
        when(productInstanceClient.getApprovedAssets(eq(new SiteId(SITE_ID)), eq(new ProductCode(offering.getProductIdentifier().getProductId())), eq(new ProductVersion(offering.getProductIdentifier().getVersionNumber())))).thenReturn(newArrayList(new AvailableAsset(ASSET_ID, 1L)));
        when(productInstanceClient.getDraftAssets(eq(new SiteId(SITE_ID)), eq(new ProductCode(offering.getProductIdentifier().getProductId())), eq(new ProductVersion(offering.getProductIdentifier().getVersionNumber())), eq(QUOTE_OPTION_ID))).thenReturn(newArrayList(new AvailableAsset(ASSET_ID, 1L)));

        //When
        CardinalityValidationResult cardinalityValidationResult = cardinalityValidator.validateSiteCardinality(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, SITE_ID, offering, 1, ASSET_KEY);

        //Then
        verify(siteFacade, times(0)).getCentralSite(CUSTOMER_ID, PROJECT_ID);
        assertThat(cardinalityValidationResult, is(new CardinalityValidationResult(false, "Site Cardinality Failed - test can have only 2 instance(s) for the Customer.")));
        assertThat(cardinalityValidationResult.isFailed(), is(true));

    }

    @Test
    public void shouldPassSiteCardinalityWhenCurrentAssetIsPartOfApprovedAssets() {
        //Case: During ecrf import, the asset which ill be imported will be part of asset being returned as contract Assets, so it should be ignored and validated.
        //Given
        ProductOffering offering = new ProductOfferingFixture("S1").withSiteCardinality(new Cardinality(0, 1, null)).build();
        when(productInstanceClient.getApprovedAssets(eq(new SiteId(SITE_ID)), eq(new ProductCode(offering.getProductIdentifier().getProductId())), eq(new ProductVersion(offering.getProductIdentifier().getVersionNumber())))).thenReturn(newArrayList(new AvailableAsset(ASSET_ID, 1L)));
        when(productInstanceClient.getDraftAssets(eq(new SiteId(SITE_ID)), eq(new ProductCode(offering.getProductIdentifier().getProductId())), eq(new ProductVersion(offering.getProductIdentifier().getVersionNumber())), eq(QUOTE_OPTION_ID))).thenReturn(Collections.<AvailableAsset>emptyList());

        //When
        CardinalityValidationResult cardinalityValidationResult = cardinalityValidator.validateSiteCardinality(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, SITE_ID, offering, 1, new AssetKey("anAssetId", 1L));

        //Then
        verify(siteFacade, times(0)).getCentralSite(CUSTOMER_ID, PROJECT_ID);
        assertThat(cardinalityValidationResult, is(new CardinalityValidationResult(true, "")));
    }

    @Test
    public void shouldFailSiteCardinalityWhenCurrentAssetIsPartOfApprovedAssets() {
        //Case: During ecrf import, the asset which ill be imported will be part of asset being returned as contract Assets, so it should be ignored and validated.
        //Given
        ProductOffering offering = new ProductOfferingFixture("S1").withSiteCardinality(new Cardinality(0, 1, null)).build();
        when(productInstanceClient.getApprovedAssets(eq(new SiteId(SITE_ID)), eq(new ProductCode(offering.getProductIdentifier().getProductId())), eq(new ProductVersion(offering.getProductIdentifier().getVersionNumber())))).thenReturn(newArrayList(new AvailableAsset(ASSET_ID, 1L)));
        when(productInstanceClient.getDraftAssets(eq(new SiteId(SITE_ID)), eq(new ProductCode(offering.getProductIdentifier().getProductId())), eq(new ProductVersion(offering.getProductIdentifier().getVersionNumber())), eq(QUOTE_OPTION_ID))).thenReturn(Collections.<AvailableAsset>emptyList());

        //When
        CardinalityValidationResult cardinalityValidationResult = cardinalityValidator.validateSiteCardinality(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, SITE_ID, offering, 1, ASSET_KEY);

        //Then
        verify(siteFacade, times(0)).getCentralSite(CUSTOMER_ID, PROJECT_ID);
        assertThat(cardinalityValidationResult, is(new CardinalityValidationResult(false, "Site Cardinality Failed - test can have only 1 instance(s) for the Customer.")));
        assertThat(cardinalityValidationResult.isFailed(), is(true));
    }

    @After
    public void shutdown() {
        serviceLocatorInstance().unRegister(instanceClient);
    }
}
