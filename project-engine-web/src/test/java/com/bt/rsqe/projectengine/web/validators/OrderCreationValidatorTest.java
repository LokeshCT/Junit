package com.bt.rsqe.projectengine.web.validators;

import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.inlife.client.ApplicationCapabilityProvider;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTOFixture;
import com.bt.rsqe.projectengine.QuoteOptionItemResource;
import com.bt.rsqe.projectengine.QuoteOptionResource;
import com.bt.rsqe.projectengine.web.OfferAndOrderValidationResult;
import com.google.common.base.Optional;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static com.bt.rsqe.inlife.client.ApplicationCapabilityProvider.Capability.*;
import static com.google.common.collect.Lists.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class OrderCreationValidatorTest {

    private ApplicationCapabilityProvider capabilityProvider = mock(ApplicationCapabilityProvider.class);
    private ProjectResource projectResource = mock(ProjectResource.class);
    private ProductInstanceClient productInstanceClient = mock(ProductInstanceClient.class);
    private OrderCreationValidator orderCreationValidator = new OrderCreationValidator(capabilityProvider, projectResource, productInstanceClient);

    private QuoteOptionResource quoteOptionResource = mock(QuoteOptionResource.class);
    private QuoteOptionItemResource quoteOptionItemResource = mock(QuoteOptionItemResource.class);
    private ProductInstance productInstance = mock(ProductInstance.class);

    private QuoteOptionItemDTO owner = QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().withId("bundleOwnerItemId").withBundleItemId("bundleOwnerItemId").build();
    private QuoteOptionItemDTO related = QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().withId("bundleRelatedItemId").withBundleItemId("bundleOwnerItemId").build();
    private QuoteOptionItemDTO unRelated = QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().withId("unRelatedItemId").withBundleItemId(null).build();
    private QuoteOptionItemDTO nonBundleOwner = QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().withId("nonBundleOwnerItemId").withBundleItemId(null).build();

    @Before
    public void setUp() {
        when(projectResource.quoteOptionResource("aProjectId")).thenReturn(quoteOptionResource);
        when(quoteOptionResource.quoteOptionItemResource("aQuoteOptionId")).thenReturn(quoteOptionItemResource);
    }

    @Test
    public void shouldValidateOrderCreationWhenBundleProductInvolved() {
        //Given
        when(capabilityProvider.isFunctionalityEnabled(USE_BUNDLE_PRODUCT_ORDER_VALIDATION, true, Optional.of("aQuoteOptionId"))).thenReturn(true);
        when(quoteOptionItemResource.get()).thenReturn(newArrayList(owner, related, unRelated));

        when(productInstanceClient.get(new LineItemId("bundleOwnerItemId"))).thenReturn(productInstance);
        when(productInstance.getCreatableCatalogueItems()).thenReturn(newArrayList("bundleRelatedItemId"));

        //When
        OfferAndOrderValidationResult result = orderCreationValidator.validate("aProjectId", "aQuoteOptionId", newArrayList("bundleOwnerItemId", "bundleRelatedItemId"));

        //Then
        assertThat(result.isValid(), is(true));
        assertThat(result.getErrorMessage(), is(""));
    }

    @Test
    public void shouldValidateOrderCreationWhenBundleProductNotInvolved() {
        //Given
        when(capabilityProvider.isFunctionalityEnabled(USE_BUNDLE_PRODUCT_ORDER_VALIDATION, true, Optional.of("aQuoteOptionId"))).thenReturn(true);
        when(quoteOptionItemResource.get()).thenReturn(newArrayList(nonBundleOwner, unRelated));

        //When
        OfferAndOrderValidationResult result = orderCreationValidator.validate("aProjectId", "aQuoteOptionId", newArrayList("nonBundleOwnerItemId"));

        //Then
        assertThat(result.isValid(), is(true));
        assertThat(result.getErrorMessage(), is(""));
    }

    @Test
    public void shouldNotValidateOrderCreationWhenInLifeSwitchNotEnabled() {
        //Given
        when(capabilityProvider.isFunctionalityEnabled(USE_BUNDLE_PRODUCT_ORDER_VALIDATION, true, Optional.of("aQuoteOptionId"))).thenReturn(false);

        //When
        OfferAndOrderValidationResult result = orderCreationValidator.validate("aProjectId", "aQuoteOptionId", newArrayList("bundleOwnerItemId", "bundleRelatedItemId"));

        //Then
        assertThat(result.isValid(), is(true));
        assertThat(result.getErrorMessage(), is(""));

    }

    @Test
    public void shouldReturnValidationMessageWhenBundleOwnerIsNotSelectedInOrderCreation() {
        //Given
        when(capabilityProvider.isFunctionalityEnabled(USE_BUNDLE_PRODUCT_ORDER_VALIDATION, true, Optional.of("aQuoteOptionId"))).thenReturn(true);
        when(quoteOptionItemResource.get()).thenReturn(newArrayList(owner, related));

        when(productInstanceClient.get(new LineItemId("bundleOwnerItemId"))).thenReturn(productInstance);
        when(productInstance.getCreatableCatalogueItems()).thenReturn(newArrayList("bundleRelatedItemId"));

        //When
        OfferAndOrderValidationResult result = orderCreationValidator.validate("aProjectId", "aQuoteOptionId", newArrayList("bundleRelatedItemId"));

        //Then
        assertThat(result.isValid(), is(false));
        assertThat(result.getErrorMessage(), is("Please select bundle/proposition product along with currently selected items for order creation and proceed"));
    }

    @Test
    public void shouldReturnValidationMessageWhenBundleRelatedIsNotSelectedInOrderCreation() {
        //Given
        when(capabilityProvider.isFunctionalityEnabled(USE_BUNDLE_PRODUCT_ORDER_VALIDATION, true, Optional.of("aQuoteOptionId"))).thenReturn(true);
        when(quoteOptionItemResource.get()).thenReturn(newArrayList(owner, related));

        when(productInstanceClient.get(new LineItemId("bundleOwnerItemId"))).thenReturn(productInstance);
        when(productInstance.getCreatableCatalogueItems()).thenReturn(newArrayList("bundleRelatedItemId"));

        //When
        OfferAndOrderValidationResult result = orderCreationValidator.validate("aProjectId", "aQuoteOptionId", newArrayList("bundleOwnerItemId"));

        //Then
        assertThat(result.isValid(), is(false));
        assertThat(result.getErrorMessage(), is("Please select all related orderable products along with bundle product for order creation and proceed"));

    }

    @Test
    public void shouldValidateMessageWhenBundleProductInvolvedInOrderCreation() {
        //Given
        when(capabilityProvider.isFunctionalityEnabled(USE_BUNDLE_PRODUCT_ORDER_VALIDATION, true, Optional.of("aQuoteOptionId"))).thenReturn(true);
        when(quoteOptionItemResource.get()).thenReturn(newArrayList(owner));

        when(productInstanceClient.get(new LineItemId("bundleOwnerItemId"))).thenReturn(productInstance);
        when(productInstance.getCreatableCatalogueItems()).thenReturn(new ArrayList<String>());

        //When
        OfferAndOrderValidationResult result = orderCreationValidator.validate("aProjectId", "aQuoteOptionId", newArrayList("bundleOwnerItemId"));

        //Then
        assertThat(result.isValid(), is(true));
        assertThat(result.getErrorMessage(), is(""));
    }

}