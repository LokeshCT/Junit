package com.bt.rsqe.customerinventory.service.extenders;

import com.bt.rsqe.customerinventory.parameter.ProductInstanceState;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristic;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetRelationship;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.externals.PmrHelper;
import com.bt.rsqe.domain.product.Attribute;
import com.bt.rsqe.domain.product.AttributeName;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.parameters.RelationshipType;
import com.google.common.base.Optional;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;

import static com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension.*;
import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.*;

public class CIFAssetExtenderTest {
    public static final String REL_NAME_1 = "RelName1";
    public static final String USER_TOKEN = "userToken";
    public static final String LOGIN_NAME = "loginName";
    private final PmrHelper pmrHelper = mock(PmrHelper.class);
    private final CIFAsset baseAsset = mock(CIFAsset.class);
    private final CIFAsset childAsset = mock(CIFAsset.class);
    private final CIFAssetCharacteristic childCharacteristic1 = mock(CIFAssetCharacteristic.class);
    private final CIFAssetCharacteristic baseCharacteristic1 = mock(CIFAssetCharacteristic.class);
    private final CharacteristicExtender characteristicExtender = mock(CharacteristicExtender.class);
    private final ProductOfferingExtender productOfferingExtender = mock(ProductOfferingExtender.class);
    private final QuoteOptionDetailExtender quoteOptionDetailExtender = mock(QuoteOptionDetailExtender.class);
    private final StencilDetailExtender stencilDetailExtender = mock(StencilDetailExtender.class);
    private final SiteDetailExtender siteDetailExtender = mock(SiteDetailExtender.class);
    private final ValidationExtender validationExtender = mock(ValidationExtender.class);
    private final AsIsAssetExtender asIsAssetExtender = mock(AsIsAssetExtender.class);
    private final SalesRelationshipExtender salesRelationshipExtender = mock(SalesRelationshipExtender.class);
    private final AccessDetailExtender accessDetailExtender = mock(AccessDetailExtender.class);
    private final SpecialBidExtender specialBidExtender = mock(SpecialBidExtender.class);
    private final ActionExtender actionExtender = mock(ActionExtender.class);
    private final JourneySpecificDetailExtender journeySpecificDetailExtender = mock(JourneySpecificDetailExtender.class);
    private final ProductCategoryExtender categoryExtender = mock(ProductCategoryExtender.class);
    private final CIFAssetExtender extender = new CIFAssetExtender(pmrHelper,
                                                                   characteristicExtender,
                                                                   productOfferingExtender,
                                                                   quoteOptionDetailExtender,
                                                                   stencilDetailExtender,
                                                                   siteDetailExtender,
                                                                   validationExtender,
                                                                   asIsAssetExtender,
                                                                   salesRelationshipExtender,
                                                                   accessDetailExtender,
                                                                   specialBidExtender,
                                                                   actionExtender,
                                                                   journeySpecificDetailExtender,
                                                                   categoryExtender);
    private final ArrayList<CIFAssetExtension> emptyExtensionsList = new ArrayList<CIFAssetExtension>();

    @Before
    public void setUp() throws Exception {
        when(childAsset.getCharacteristics()).thenReturn(newArrayList(childCharacteristic1));
        when(baseAsset.getCharacteristics()).thenReturn(newArrayList(baseCharacteristic1));

        CIFAssetRelationship childRelationship = new CIFAssetRelationship(childAsset, REL_NAME_1, RelationshipType.Child, ProductInstanceState.LIVE);
        when(baseAsset.getRelationships()).thenReturn(newArrayList(childRelationship));
        when(baseAsset.getRelationships(REL_NAME_1)).thenReturn(newArrayList(childRelationship));
    }

    @Test
    public void shouldCallSpecificExtendersWhenNoExtensionsPassed() {
        extender.extend(baseAsset, USER_TOKEN, LOGIN_NAME, emptyExtensionsList);

        InOrder inOrder1 = inOrder(quoteOptionDetailExtender, characteristicExtender, stencilDetailExtender, categoryExtender, specialBidExtender);
        inOrder1.verify(quoteOptionDetailExtender, times(1)).extend(emptyExtensionsList, baseAsset);
        inOrder1.verify(characteristicExtender, times(1)).extend(emptyExtensionsList, baseAsset, null, baseAsset.getCharacteristics());
        inOrder1.verify(stencilDetailExtender, times(1)).extend(emptyExtensionsList, baseAsset);
        inOrder1.verify(categoryExtender, times(1)).extend(emptyExtensionsList, baseAsset);
        inOrder1.verify(specialBidExtender, times(1)).extend(emptyExtensionsList, baseAsset, USER_TOKEN, LOGIN_NAME);

        InOrder inOrder2 = inOrder(quoteOptionDetailExtender, asIsAssetExtender);
        inOrder2.verify(quoteOptionDetailExtender, times(1)).extend(emptyExtensionsList, baseAsset);
        inOrder2.verify(asIsAssetExtender, times(1)).extend(emptyExtensionsList, baseAsset);

        verify(productOfferingExtender, times(1)).extend(emptyExtensionsList, baseAsset, null);
        verify(siteDetailExtender, times(1)).extend(emptyExtensionsList, baseAsset);
        verify(validationExtender, times(1)).extend(emptyExtensionsList, baseAsset);
        verify(accessDetailExtender, times(1)).extend(emptyExtensionsList, baseAsset);
        verify(actionExtender, times(1)).extend(emptyExtensionsList, baseAsset);
        verify(journeySpecificDetailExtender, times(1)).extend(emptyExtensionsList, baseAsset);
        verify(salesRelationshipExtender, times(1)).extend(emptyExtensionsList, baseAsset, null);

        verifyNoMoreInteractions(characteristicExtender);
        verifyNoMoreInteractions(productOfferingExtender);
        verifyNoMoreInteractions(quoteOptionDetailExtender);
        verifyNoMoreInteractions(siteDetailExtender);
        verifyNoMoreInteractions(stencilDetailExtender);
        verifyNoMoreInteractions(validationExtender);
        verifyNoMoreInteractions(asIsAssetExtender);
        verifyNoMoreInteractions(accessDetailExtender);
        verifyNoMoreInteractions(salesRelationshipExtender);
        verifyNoMoreInteractions(specialBidExtender);
        verifyNoMoreInteractions(accessDetailExtender);
        verifyNoMoreInteractions(journeySpecificDetailExtender);
        verifyNoMoreInteractions(categoryExtender);
    }

    @Test
    public void shouldCallExtensionsForChildrenWhenRelationshipsExtensionPassed() {
        final ArrayList<CIFAssetExtension> relationshipsOnlyExtension = newArrayList(Relationships);
        extender.extend(baseAsset, USER_TOKEN, LOGIN_NAME, relationshipsOnlyExtension);

        verify(characteristicExtender, times(1)).extend(relationshipsOnlyExtension, baseAsset, null, baseAsset.getCharacteristics());
        verify(productOfferingExtender, times(1)).extend(relationshipsOnlyExtension, baseAsset, null);
        verify(quoteOptionDetailExtender, times(1)).extend(relationshipsOnlyExtension, baseAsset);
        verify(stencilDetailExtender, times(1)).extend(relationshipsOnlyExtension, baseAsset);
        verify(siteDetailExtender, times(1)).extend(relationshipsOnlyExtension, baseAsset);
        verify(validationExtender, times(1)).extend(relationshipsOnlyExtension, baseAsset);
        verify(asIsAssetExtender, times(1)).extend(relationshipsOnlyExtension, baseAsset);
        verify(accessDetailExtender, times(1)).extend(relationshipsOnlyExtension, baseAsset);
        verify(salesRelationshipExtender, times(1)).extend(relationshipsOnlyExtension, baseAsset, null);
        verify(categoryExtender, times(1)).extend(relationshipsOnlyExtension, baseAsset);
        verify(specialBidExtender, times(1)).extend(relationshipsOnlyExtension, baseAsset, USER_TOKEN, LOGIN_NAME);
        verify(actionExtender, times(1)).extend(relationshipsOnlyExtension, baseAsset);
        verify(journeySpecificDetailExtender, times(1)).extend(relationshipsOnlyExtension, baseAsset);

        verify(characteristicExtender, times(1)).extend(relationshipsOnlyExtension, childAsset, null, childAsset.getCharacteristics());
        verify(productOfferingExtender, times(1)).extend(relationshipsOnlyExtension, childAsset, null);
        verify(quoteOptionDetailExtender, times(1)).extend(relationshipsOnlyExtension, childAsset);
        verify(stencilDetailExtender, times(1)).extend(relationshipsOnlyExtension, childAsset);
        verify(siteDetailExtender, times(1)).extend(relationshipsOnlyExtension, childAsset);
        verify(validationExtender, times(1)).extend(relationshipsOnlyExtension, childAsset);
        verify(asIsAssetExtender, times(1)).extend(relationshipsOnlyExtension, childAsset);
        verify(accessDetailExtender, times(1)).extend(relationshipsOnlyExtension, childAsset);
        verify(salesRelationshipExtender, times(1)).extend(relationshipsOnlyExtension, childAsset, null);
        verify(categoryExtender, times(1)).extend(relationshipsOnlyExtension, childAsset);
        verify(specialBidExtender, times(1)).extend(relationshipsOnlyExtension, childAsset, USER_TOKEN, LOGIN_NAME);
        verify(actionExtender, times(1)).extend(relationshipsOnlyExtension, childAsset);
        verify(journeySpecificDetailExtender, times(1)).extend(relationshipsOnlyExtension, childAsset);

        verifyNoMoreInteractions(characteristicExtender);
        verifyNoMoreInteractions(productOfferingExtender);
        verifyNoMoreInteractions(quoteOptionDetailExtender);
        verifyNoMoreInteractions(stencilDetailExtender);
        verifyNoMoreInteractions(siteDetailExtender);
        verifyNoMoreInteractions(validationExtender);
        verifyNoMoreInteractions(asIsAssetExtender);
        verifyNoMoreInteractions(accessDetailExtender);
        verifyNoMoreInteractions(salesRelationshipExtender);
        verifyNoMoreInteractions(categoryExtender);
        verifyNoMoreInteractions(specialBidExtender);
        verifyNoMoreInteractions(actionExtender);
        verifyNoMoreInteractions(journeySpecificDetailExtender);
    }

    @Test
    public void shouldNotLoadProductOfferingIfNotNeededForExtension() {
        extender.extend(baseAsset, emptyExtensionsList);

        verify(pmrHelper, times(0)).getProductOffering(baseAsset);
        verify(pmrHelper, times(0)).getProductOffering(childAsset);
    }

    @Test
    public void shouldLoadProductOfferingIfNeededForExtension() {
        extender.extend(baseAsset, newArrayList(Relationships, ProductOfferingDetail));

        verify(pmrHelper, times(1)).getProductOffering(baseAsset);
        verify(pmrHelper, times(1)).getProductOffering(childAsset);
    }

    @Test
    public void shouldNotRecurseForeverTryingToFetchAssetAllowedValues() {
        ProductOffering baseOffering = mock(ProductOffering.class);
        Attribute baseAttribute1 = mock(Attribute.class);
        CIFAssetCharacteristic realCharacteristic = new CIFAssetCharacteristic("CharName", "CharValue", true);
        when(baseOffering.getAttribute(new AttributeName(realCharacteristic.getName()))).thenReturn(baseAttribute1);
        when(pmrHelper.getProductOffering(baseAsset)).thenReturn(baseOffering);
        when(baseAsset.getCharacteristics()).thenReturn(newArrayList(realCharacteristic));

        final CIFAssetExtender localExtender = new CIFAssetExtender(pmrHelper,
                                                                    new CharacteristicExtender(pmrHelper),
                                                               productOfferingExtender,
                                                               quoteOptionDetailExtender, stencilDetailExtender, siteDetailExtender, validationExtender, asIsAssetExtender, salesRelationshipExtender, accessDetailExtender, specialBidExtender, actionExtender, journeySpecificDetailExtender, categoryExtender);
        doAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) {
                localExtender.extend((CIFAsset)invocation.getArguments()[0], newArrayList(CharacteristicAllowedValues));
                return Optional.of(newArrayList("One", "Two"));
            }
        }).when(pmrHelper).getAllowedValues(baseAsset, baseAttribute1);

        localExtender.extend(baseAsset, newArrayList(CharacteristicAllowedValues));
        verify(pmrHelper, times(1)).getAllowedValues(baseAsset, baseAttribute1);
    }
}