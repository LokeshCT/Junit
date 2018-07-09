package com.bt.rsqe.customerinventory.service.evaluators;

import com.bt.rsqe.customerinventory.parameter.ProductCode;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristic;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetRelationship;
import com.bt.rsqe.customerinventory.service.repository.CIFAssetJPARepository;
import com.bt.rsqe.domain.AssetKey;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;

public class CIFAttributeMatchEvaluatorTest {

    private CIFAsset ownerAsset;
    private CIFAsset cifAsset;
    private CIFAsset relatedAsset;
    private CIFAsset relatedAsset1;
    private CIFNewGroupIdCalculator cifNewGroupIdCalculator;
    private CIFAssetJPARepository cifAssetJPARepository;
    private CIFAssetCharacteristic cifAssetCharacteristic;
    private CIFAssetCharacteristic cifAssetCharacteristic1;
    private CIFAssetCharacteristic cifAssetCharacteristic2;
    private CIFAssetRelationship cifAssetRelationship;
    private CIFAssetRelationship cifAssetRelationship1;
    CIFAttributeMatchEvaluator cifAttributeMatchEvaluator;
    private static final String siblingCountExpression = "CountMySiblings[ATTR TO MATCH,ATTR TO UPDATE]";
    private static final String customGroupIdExpression = "CustomGroupIdValue[ATTR TO MATCH,ATTR TO GET,ATTR TO UPDATE]";
    private List expressionPath = newArrayList();

    @Before
    public void setup() {
        ownerAsset = mock(CIFAsset.class);
        cifAsset = mock(CIFAsset.class);
        relatedAsset = mock(CIFAsset.class);
        relatedAsset1 = mock(CIFAsset.class);
        cifNewGroupIdCalculator = mock(CIFNewGroupIdCalculator.class);
        cifAssetJPARepository = mock(CIFAssetJPARepository.class);
        cifAssetCharacteristic = mock(CIFAssetCharacteristic.class);
        cifAssetCharacteristic1 = mock(CIFAssetCharacteristic.class);
        cifAssetCharacteristic2 = mock(CIFAssetCharacteristic.class);
        cifAssetRelationship = mock(CIFAssetRelationship.class);
        cifAssetRelationship1 = mock(CIFAssetRelationship.class);

        when(cifAsset.getAssetKey()).thenReturn(AssetKey.newInstance("assetId", 1l));
        when(cifAssetJPARepository.getOwnerAssets(cifAsset.getAssetKey(), true)).thenReturn(newArrayList(ownerAsset));
        when(ownerAsset.getRelationships()).thenReturn(newArrayList(cifAssetRelationship, cifAssetRelationship1));
        when(cifAssetRelationship.getRelated()).thenReturn(relatedAsset);
        when(cifAssetRelationship1.getRelated()).thenReturn(relatedAsset1);
        when(cifAsset.getProductCode()).thenReturn("aProductCode");
        when(relatedAsset.getProductCode()).thenReturn("aProductCode");
        when(relatedAsset1.getProductCode()).thenReturn("aProductCode");

        when(cifAsset.getCharacteristic("ATTR TO MATCH")).thenReturn(cifAssetCharacteristic);
        when(relatedAsset.getCharacteristic("ATTR TO MATCH")).thenReturn(cifAssetCharacteristic1);
        when(relatedAsset1.getCharacteristic("ATTR TO MATCH")).thenReturn(cifAssetCharacteristic1);
        when(cifAssetCharacteristic.getValue()).thenReturn("someValue");
        when(cifAssetCharacteristic1.getValue()).thenReturn("someValue");

        when(cifAsset.getCharacteristic("ATTR TO GET")).thenReturn(cifAssetCharacteristic2);
        when(cifAsset.getCharacteristic("ATTR TO UPDATE")).thenReturn(cifAssetCharacteristic2);
        when(relatedAsset.getCharacteristic("ATTR TO GET")).thenReturn(cifAssetCharacteristic2);

        cifAttributeMatchEvaluator = new CIFAttributeMatchEvaluator(cifAsset, cifAssetJPARepository, cifNewGroupIdCalculator);
    }

    @Test
    public void shouldEvaluateSiblingUpdateExpression() {
        final List<Object> values = cifAttributeMatchEvaluator.getValues(expressionPath, siblingCountExpression);

        verify(cifAsset, times(1)).updateCharacteristicValue("ATTR TO UPDATE", "2");
        verify(relatedAsset, times(1)).updateCharacteristicValue("ATTR TO UPDATE", "2");
        verify(cifAssetJPARepository, times(1)).saveAsset(relatedAsset);

        assertThat(values.size(), is(1));
        assertThat((Integer) values.get(0), is(2));
    }

    @Test
    public void shouldReturnEmptyResponseWhenNoOwnerFound() {
        //Given
        List<CIFAsset> owners = newArrayList();
        when(cifAssetJPARepository.getOwnerAssets(cifAsset.getAssetKey(), true)).thenReturn(owners);
        List expressionPath = newArrayList();
        final List<Object> values = cifAttributeMatchEvaluator.getValues(expressionPath, siblingCountExpression);

        assertThat(values.size(), is(0));
    }

    @Test
    public void shouldNotConsiderCancelledAssetForEvaluation() {
        when(relatedAsset.isCancelledAsset()).thenReturn(true);

        final List<Object> values = cifAttributeMatchEvaluator.getValues(expressionPath, siblingCountExpression);

        verify(cifAsset, times(1)).updateCharacteristicValue("ATTR TO UPDATE", "1");
        verify(relatedAsset1, times(1)).updateCharacteristicValue("ATTR TO UPDATE", "1");
        verify(cifAssetJPARepository,times(0)).saveAsset(cifAsset);
        verify(cifAssetJPARepository, times(1)).saveAsset(relatedAsset1);


        assertThat(values.size(), is(1));
        assertThat((Integer) values.get(0), is(1));
    }

    @Test
    public void shouldReturnResponseAsZeroIfProductCodeDoesNotMatchWithRelatedAssets() {
        //Given
        when(relatedAsset.getProductCode()).thenReturn("someCode");
        when(relatedAsset1.getProductCode()).thenReturn("someCode");

        List expressionPath = newArrayList();
        final List<Object> values = cifAttributeMatchEvaluator.getValues(expressionPath, siblingCountExpression);
        assertThat(values.size(), is(1));
        assertThat((Integer) values.get(0), is(0));
    }

    @Test
    public void shouldReturnResponseAsOneIfAttrToMatchNotMatchesWithRelatedAssets() {
        //Given
        when(cifAssetCharacteristic1.getValue()).thenReturn("someDifferentValue");

        //then
        List expressionPath = newArrayList();
        final List<Object> values = cifAttributeMatchEvaluator.getValues(expressionPath, siblingCountExpression);
        assertThat(values.size(), is(1));
        assertThat((Integer) values.get(0), is(0));
    }

    @Test
    public void shouldEvaluateGroupIdExpression() {
        List expressionPath = newArrayList();

        //Given
        when(cifAssetCharacteristic2.getValue()).thenReturn("2");

        //When
        cifAttributeMatchEvaluator.getValues(expressionPath, customGroupIdExpression);

        //then
        verify(cifNewGroupIdCalculator, times(1)).calculate(newArrayList(relatedAsset, relatedAsset1), "2", "ATTR TO UPDATE");
    }

    @Test
    public void shouldNotConsiderCancelledAssetForCustomGroupId() {
        List expressionPath = newArrayList();

        when(cifAssetCharacteristic2.getValue()).thenReturn("2");
        when(relatedAsset.isCancelledAsset()).thenReturn(true);

        cifAttributeMatchEvaluator.getValues(expressionPath, customGroupIdExpression);

        //then
        verify(cifNewGroupIdCalculator, times(1)).calculate(newArrayList(relatedAsset1), "2", "ATTR TO UPDATE");
    }

    @Test
    public void shouldEvaluateGroupIdExpressionForCurrentAssetIfRelatedAssetHasNoAttrToMatch() {
        List expressionPath = newArrayList();

        when(relatedAsset1.getProductCode()).thenReturn("someHCode");
        when(cifAssetCharacteristic2.getValue()).thenReturn("1");

        cifAttributeMatchEvaluator.getValues(expressionPath, customGroupIdExpression);

        verify(cifNewGroupIdCalculator, times(1)).calculate(newArrayList(relatedAsset), "1", "ATTR TO UPDATE");
    }

    @Test
    public void shouldReturnEmptyResponseWhenNoOwnerFoundForCustomExpression() {
        //Given
        List<CIFAsset> owners = newArrayList();
        when(cifAssetJPARepository.getOwnerAssets(cifAsset.getAssetKey(),true)).thenReturn(owners);
        List expressionPath = newArrayList();
        final List<Object> values = cifAttributeMatchEvaluator.getValues(expressionPath, customGroupIdExpression);

        assertThat(values.size(), is(0));
    }

    @Test
    public void shouldReturnResponseIfAttrToMatchDoesNotMatchWithRelatedAssetsForCustomExpression() {
        //Given
        when(cifAssetCharacteristic1.getValue()).thenReturn("someValue1");
        when(cifAssetCharacteristic2.getValue()).thenReturn("1");

        List expressionPath = newArrayList();
        final List<Object> values = cifAttributeMatchEvaluator.getValues(expressionPath, customGroupIdExpression);

        verify(cifNewGroupIdCalculator, times(1)).calculate(expressionPath , "1", "ATTR TO UPDATE");

        assertThat(values.size(), is(1));
    }


}
