package com.bt.rsqe.customerinventory.service.evaluators;

import com.bt.rsqe.client.Pmr;
import com.bt.rsqe.customerinventory.parameter.ProductInstanceState;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristicValue;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetKey;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetOfferingDetail;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.product.parameters.RelationshipType;
import com.bt.rsqe.expressionevaluator.ContextualEvaluatorTypes;
import com.bt.rsqe.enums.AssetVersionStatus;
import com.bt.rsqe.expressionevaluator.ExpressionEvaluatorUnknownExpressionException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.bt.rsqe.customerinventory.service.client.fixtures.CIFAssetFixture.aCIFAsset;
import static com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension.CharacteristicAllowedValues;
import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CIFAssetEvaluatorTest{
    public static final String NON_CHARACTERISTIC_EXPRESSION = "LegacyIdentifier";
    public static final ArrayList<CIFAssetCharacteristicValue> ALLOWED_VALUES = newArrayList(new CIFAssetCharacteristicValue("AttributeOneAllowedValue1"),
                                                                        new CIFAssetCharacteristicValue("AttributeOneAllowedValue2"));
    private CIFAssetOrchestrator cifAssetOrchestrator = mock(CIFAssetOrchestrator.class);
    private Pmr pmrClient = mock(Pmr.class);
    private CIFAssetCharacteristicEvaluatorFactory evaluatorFactory = new CIFAssetCharacteristicEvaluatorFactory(pmrClient, null, null, null);

    @Test
    public void shouldGetSingleValueForPassedAttributeNameAndEmptyPath(){
        CIFAsset cifAsset = aCIFAsset().withCharacteristic("AttributeOne", "AttributeOneValue", newArrayList(new CIFAssetCharacteristicValue("AttributeOneValue")))
            .withCharacteristic("AttributeTwo", "AttributeTwoValue")
            .build();
        CIFAsset cifAssetWithAllowedValues = aCIFAsset().withCharacteristic("AttributeOne", "AttributeOneValue", newArrayList(new CIFAssetCharacteristicValue("AttributeOneValue")))
            .withCharacteristic("AttributeTwo", "AttributeTwoValue", newArrayList(new CIFAssetCharacteristicValue("AttributeTwoValue")))
                .build();
        CIFAssetEvaluator cifAssetEvaluator = new CIFAssetEvaluator(cifAsset, cifAssetOrchestrator, evaluatorFactory);
        when(cifAssetOrchestrator.extendAsset(cifAsset, newArrayList(CharacteristicAllowedValues))).thenReturn(cifAssetWithAllowedValues);

        List<Object> attributeOneValues = cifAssetEvaluator.getValues(new ArrayList<String>(), "AttributeOne#AllowedValues");
        List<Object> attributeTwoValues = cifAssetEvaluator.getValues(new ArrayList<String>(), "AttributeTwo");

        verify(cifAssetOrchestrator, times(1)).extendAsset(cifAsset, newArrayList(CharacteristicAllowedValues));

        assertThat(attributeOneValues.size(), is(1));
        @SuppressWarnings("unchecked")
        final ArrayList<String> valueList = (ArrayList<String>)attributeOneValues.get(0);
        assertThat(valueList, is(newArrayList("AttributeOneValue")));

        assertThat(attributeTwoValues.size(), is(1));
        assertThat((String)attributeTwoValues.get(0), is("AttributeTwoValue"));
    }

    @Test
    public void shouldReturnEmptyValueWhenUnknownCharacteristic(){
        AssetKey rootAssetKey = new AssetKey("1234", 1l);
        CIFAsset cifAsset = aCIFAsset().withCharacteristic("AttributeOne", "AttributeOneValue")
                                           .withCharacteristic("AttributeTwo", "AttributeTwoValue")
                                           .build();
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(rootAssetKey))).thenReturn(cifAsset);
        CIFAssetEvaluator cifAssetEvaluator = new CIFAssetEvaluator(rootAssetKey, cifAssetOrchestrator, evaluatorFactory);

        List<Object> attributeThreeValues = cifAssetEvaluator.getValues(new ArrayList<String>(), "AttributeThree");

        assertThat(attributeThreeValues.size(), is(1));
        assertThat((String)attributeThreeValues.get(0), is(""));
    }

    @Test
    public void shouldReturnEmptyValueWhenNullCharacteristicValue(){
        AssetKey rootAssetKey = new AssetKey("1234", 1l);
        CIFAsset cifAsset = aCIFAsset().withCharacteristic("AttributeOne", null).build();

        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(rootAssetKey))).thenReturn(cifAsset);
        CIFAssetEvaluator cifAssetEvaluator = new CIFAssetEvaluator(rootAssetKey, cifAssetOrchestrator, evaluatorFactory);

        List<Object> expectedValues = cifAssetEvaluator.getValues(new ArrayList<String>(), "AttributeOne");
        assertThat(expectedValues.size(), is(1));
        assertThat((String)expectedValues.get(0), is(""));
    }

    @Test
    public void shouldReturnEmptyValueWhenWhitespaceOnlyCharacteristicValue(){
        AssetKey rootAssetKey = new AssetKey("1234", 1l);
        CIFAsset cifAsset = aCIFAsset().withCharacteristic("AttributeOne", "\b \t").build();

        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(rootAssetKey))).thenReturn(cifAsset);
        CIFAssetEvaluator cifAssetEvaluator = new CIFAssetEvaluator(rootAssetKey, cifAssetOrchestrator, evaluatorFactory);

        List<Object> expectedValues = cifAssetEvaluator.getValues(new ArrayList<String>(), "AttributeOne");
        assertThat(expectedValues.size(), is(1));
        assertThat((String)expectedValues.get(0), is(""));
    }

    @Test
    public void shouldGetAllowedValues() {
        AssetKey rootAssetKey = new AssetKey("1234", 1l);
        CIFAsset cifAsset = aCIFAsset().withCharacteristic("AttributeOne", "AttributeOneValue2", ALLOWED_VALUES)
                                           .withCharacteristic("AttributeTwo", "AttributeTwoValue1", ALLOWED_VALUES)
                                           .build();
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(rootAssetKey, newArrayList(CharacteristicAllowedValues)))).thenReturn(cifAsset);
        CIFAssetEvaluator cifAssetEvaluator = new CIFAssetEvaluator(rootAssetKey, cifAssetOrchestrator, evaluatorFactory);

        List<Object> allowedValues = cifAssetEvaluator.getValues(new ArrayList<String>(), "AttributeTwo#AllowedValues");

        verify(cifAssetOrchestrator).getAsset(new CIFAssetKey(rootAssetKey, newArrayList(CharacteristicAllowedValues)));

        @SuppressWarnings("unchecked")
        List<Object> singleAllowedValues = (List<Object>) allowedValues.get(0);
        assertThat(singleAllowedValues, is((List<Object>) newArrayList((Object)ALLOWED_VALUES.get(0).getValue(), ALLOWED_VALUES.get(1).getValue())));
    }

    @Test
    public void shouldGetEmptyAllowedValuesFromNull() {
        AssetKey rootAssetKey = new AssetKey("1234", 1l);
        CIFAsset cifAsset = aCIFAsset().withCharacteristic("AttributeTwo", "AttributeTwoValue1", (List<CIFAssetCharacteristicValue>) null)
            .build();
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(rootAssetKey, newArrayList(CharacteristicAllowedValues)))).thenReturn(cifAsset);
        CIFAssetEvaluator cifAssetEvaluator = new CIFAssetEvaluator(rootAssetKey, cifAssetOrchestrator, evaluatorFactory);

        List<Object> allowedValues = cifAssetEvaluator.getValues(new ArrayList<String>(), "AttributeTwo#AllowedValues");

        verify(cifAssetOrchestrator).getAsset(new CIFAssetKey(rootAssetKey, newArrayList(CharacteristicAllowedValues)));

        @SuppressWarnings("unchecked")
        List<Object> singleAllowedValues = (List<Object>) allowedValues.get(0);
        assertThat(singleAllowedValues.size(), is(0));
    }

    @Test
    public void shouldGetSourcedAllowedValues() {
        AssetKey rootAssetKey = new AssetKey("1234", 1l);
        CIFAsset cifAsset = aCIFAsset().withCharacteristic("AttributeOne", "AttributeOneValue2", ALLOWED_VALUES)
                                           .withCharacteristic("AttributeTwo", "AttributeTwoValue1", ALLOWED_VALUES)
                                           .build();
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(rootAssetKey, newArrayList(CIFAssetExtension.CharacteristicAllowedValues)))).thenReturn(cifAsset);
        CIFAssetEvaluator cifAssetEvaluator = new CIFAssetEvaluator(rootAssetKey, cifAssetOrchestrator, evaluatorFactory);

        List<Object> allowedValues = cifAssetEvaluator.getValues(new ArrayList<String>(), "AttributeTwo#SourcedAllowedValues");

        verify(cifAssetOrchestrator).getAsset(new CIFAssetKey(rootAssetKey, newArrayList(CIFAssetExtension.CharacteristicAllowedValues)));

        @SuppressWarnings("unchecked")
        List<Object> singleSourcedAllowedValues = (List<Object>) allowedValues.get(0);
        assertThat(singleSourcedAllowedValues, is((List<Object>) newArrayList((Object)ALLOWED_VALUES.get(0).getValue(), ALLOWED_VALUES.get(1).getValue())));
    }

    @Test(expected = ExpressionEvaluatorUnknownExpressionException.class)
    public void shouldThrowExceptionForUnknownEvaluator(){
        AssetKey rootAssetKey = new AssetKey("1234", 1l);
        CIFAsset cifAsset = aCIFAsset().withCharacteristic("AttributeOne", "AttributeOneValue2", ALLOWED_VALUES)
                                       .withCharacteristic("AttributeTwo", "AttributeTwoValue1", ALLOWED_VALUES)
                                       .build();
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(rootAssetKey, newArrayList(CharacteristicAllowedValues)))).thenReturn(cifAsset);
        CIFAssetEvaluator cifAssetEvaluator = new CIFAssetEvaluator(rootAssetKey, cifAssetOrchestrator, evaluatorFactory);

        cifAssetEvaluator.getValues(null, "AttributeTwo#SomeWeirdThing");
    }

    @Test
    public void shouldGetNonCharacteristicExpression(){
        AssetKey rootAssetKey = new AssetKey("1234", 1l);
        CIFAsset cifAsset = aCIFAsset().build();
        cifAsset.loadOfferingDetail(new CIFAssetOfferingDetail("ProductName", "", "GroupName", "LegacyIdentifier", true, false, "proposition", true, true, null));
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(rootAssetKey, newArrayList(CIFAssetExtension.ProductOfferingDetail)))).thenReturn(cifAsset);
        CIFAssetEvaluator cifAssetEvaluator = new CIFAssetEvaluator(rootAssetKey, cifAssetOrchestrator, evaluatorFactory);

        assertThat(cifAssetEvaluator.getValues(new ArrayList<String>(), NON_CHARACTERISTIC_EXPRESSION).get(0), is((Object) "LegacyIdentifier"));
        verify(cifAssetOrchestrator).getAsset(new CIFAssetKey(rootAssetKey, newArrayList(CIFAssetExtension.ProductOfferingDetail)));
    }

    @Test
    public void shouldGetBaseConversion(){
        AssetKey rootAssetKey = new AssetKey("1234", 1l);
        CIFAssetEvaluator cifAssetEvaluator = new CIFAssetEvaluator(rootAssetKey, cifAssetOrchestrator, evaluatorFactory);

        assertThat(cifAssetEvaluator.getBaseConversion(), is(ContextualEvaluatorTypes.DOUBLE_EVALUATOR.getName()));
    }

    @Test
    public void shouldGetAllowedValuesForDeepCharacteristic(){
        CIFAsset grandChildAsset = aCIFAsset().withCharacteristic("AttributeOne", "AttributeOneValue", ALLOWED_VALUES).build();
        CIFAsset childAsset = aCIFAsset().withRelationship(grandChildAsset, "grandChildRelation", RelationshipType.Child).build();
        CIFAsset cifAsset = aCIFAsset().withRelationship(childAsset, "childRelation", RelationshipType.Child)
                                       .build();
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(cifAsset.getAssetKey(), newArrayList(CIFAssetExtension.Relationships)))).thenReturn(cifAsset);
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(grandChildAsset.getAssetKey(), newArrayList(CharacteristicAllowedValues)))).thenReturn(grandChildAsset);

        CIFAssetEvaluator cifAssetEvaluator = new CIFAssetEvaluator(cifAsset.getAssetKey(), cifAssetOrchestrator, evaluatorFactory);

        List<String> expressionPath = newArrayList("childRelation", "grandChildRelation");
        List<Object> allowedValuesList = cifAssetEvaluator.getValues(expressionPath, "AttributeOne#AllowedValues");

        verify(cifAssetOrchestrator).getAsset(new CIFAssetKey(cifAsset.getAssetKey(), newArrayList(CIFAssetExtension.Relationships)));
        @SuppressWarnings("unchecked")
        List<Object> allowedValues = (List<Object>) allowedValuesList.get(0);
        assertThat(allowedValues, is((List<Object>) (newArrayList((Object) "AttributeOneAllowedValue1", "AttributeOneAllowedValue2"))));
        assertThat(expressionPath.size(), is(2));
        assertThat(expressionPath.get(0), is("childRelation"));
        assertThat(expressionPath.get(1), is("grandChildRelation"));
    }

    @Test
    public void shouldGetAllowedValuesFromParent() {
        CIFAsset basicCifAsset = aCIFAsset().build();
        CIFAsset parentCifAsset = aCIFAsset().withCharacteristic("AttributeOne", "AttributeOneValue", ALLOWED_VALUES)
                                             .withRelationship(basicCifAsset, "relationshipName", RelationshipType.Child)
                                             .build();
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(basicCifAsset.getAssetKey()))).thenReturn(basicCifAsset);
        when(cifAssetOrchestrator.getParentAsset(new CIFAssetKey(basicCifAsset.getAssetKey(), newArrayList(CharacteristicAllowedValues)))).thenReturn(parentCifAsset);
        CIFAssetEvaluator cifAssetEvaluator = new CIFAssetEvaluator(basicCifAsset.getAssetKey(), cifAssetOrchestrator, evaluatorFactory);

        List<String> expressionPath = newArrayList("Parent");
        List<Object> allowedValuesList = cifAssetEvaluator.getValues(expressionPath, "AttributeOne#AllowedValues");

        @SuppressWarnings("unchecked")
        List<Object> allowedValues = (List<Object>) allowedValuesList.get(0);
        assertThat(allowedValues, is((List<Object>)(newArrayList((Object)"AttributeOneAllowedValue1", "AttributeOneAllowedValue2"))));
    }

    @Test
    public void shouldNotGetAllowedValuesFromParentWhenNoParent() {
        CIFAsset basicCifAsset = aCIFAsset().build();
        when(cifAssetOrchestrator.getParentAsset(new CIFAssetKey(basicCifAsset.getAssetKey()))).thenReturn(null);
        CIFAssetEvaluator cifAssetEvaluator = new CIFAssetEvaluator(basicCifAsset, cifAssetOrchestrator, evaluatorFactory);

        List<String> expressionPath = newArrayList("Parent");
        List<Object> allowedValues = cifAssetEvaluator.getValues(expressionPath, "AttributeOne#AllowedValues");

        assertThat(allowedValues.size(), is(0));
    }

    @Test
    public void shouldFilterRelationshipsWhenRequested() {
        String childOneValue = "Value1";
        String childTwoValue = "Value2";
        CIFAsset child1 = aCIFAsset().withCharacteristic("AttributeOne", childOneValue)
                                     .withCharacteristic("AttributeTwo", "On").build();
        CIFAsset child2 = aCIFAsset().withCharacteristic("AttributeOne", childTwoValue)
                                     .withCharacteristic("AttributeTwo", "On").build();
        CIFAsset child3 = aCIFAsset().withCharacteristic("AttributeOne", "Value3")
                                     .withCharacteristic("AttributeTwo", "Off").build();
        CIFAsset child4 = aCIFAsset().withCharacteristic("AttributeOne", "Value4")
                                     .withCharacteristic("AttributeTwo", "On").build();

        CIFAsset rootCifAsset = aCIFAsset().withRelationship(child1, "Relationship1", RelationshipType.Child)
                                           .withRelationship(child2, "Relationship1", RelationshipType.RelatedTo)
                                           .withRelationship(child3, "Relationship1", RelationshipType.Child)
                                           .withRelationship(child4, "Relationship2", RelationshipType.Child)
                                           .build();
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(rootCifAsset.getAssetKey(), newArrayList(CIFAssetExtension.Relationships)))).thenReturn(rootCifAsset);
        CIFAssetEvaluator cifAssetEvaluator = new CIFAssetEvaluator(rootCifAsset.getAssetKey(), cifAssetOrchestrator, evaluatorFactory);

        List<String> expressionPath = newArrayList("Relationship1[AttributeTwo='On']");
        List<Object> values = cifAssetEvaluator.getValues(expressionPath, "AttributeOne");

        assertThat(values, is((List<Object>) newArrayList((Object) childOneValue, childTwoValue)));
    }

    @Test
    public void shouldFilterRelationshipsWhereLiveStatusDoesNotMatchParent() {
        String child1Value = "Value1";
        CIFAsset child1 = aCIFAsset().withStatus(ProductInstanceState.LIVE).withCharacteristic("AttributeOne", child1Value).build();
        CIFAsset child2 = aCIFAsset().withStatus(ProductInstanceState.CEASED).withCharacteristic("AttributeOne", "Two").build();
        CIFAsset child3 = aCIFAsset().withStatus(ProductInstanceState.CANCELLED).withCharacteristic("AttributeOne", "Three").build();
        CIFAsset child4 = aCIFAsset().withStatus(ProductInstanceState.REMOVED).withCharacteristic("AttributeOne", "Four").build();

        CIFAsset rootCifAsset = aCIFAsset().withRelationship(child1, "Relationship1", RelationshipType.Child)
                                           .withRelationship(child2, "Relationship1", RelationshipType.RelatedTo)
                                           .withRelationship(child3, "Relationship1", RelationshipType.Child)
                                           .withRelationship(child4, "Relationship1", RelationshipType.Child)
                                           .build();

        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(rootCifAsset.getAssetKey(), newArrayList(CIFAssetExtension.Relationships)))).thenReturn(rootCifAsset);
        CIFAssetEvaluator cifAssetEvaluator = new CIFAssetEvaluator(rootCifAsset.getAssetKey(), cifAssetOrchestrator, evaluatorFactory);
        List<String> expressionPath = newArrayList("Relationship1");
        List<Object> values = cifAssetEvaluator.getValues(expressionPath, "AttributeOne");

        assertThat(values, is((List<Object>) newArrayList((Object) child1Value)));
    }

    @Test
    public void shouldNotFilterAnyRelationshipsWhereParentIsNotLive() {
        String child1Value = "Value1";
        String child2Value = "Value2";
        String child3Value = "Value3";
        String child4Value = "Value4";
        CIFAsset child1 = aCIFAsset().withStatus(ProductInstanceState.LIVE).withCharacteristic("AttributeOne", child1Value).build();
        CIFAsset child2 = aCIFAsset().withStatus(ProductInstanceState.CEASED).withCharacteristic("AttributeOne", child2Value).build();
        CIFAsset child3 = aCIFAsset().withStatus(ProductInstanceState.CANCELLED).withCharacteristic("AttributeOne", child3Value).build();
        CIFAsset child4 = aCIFAsset().withStatus(ProductInstanceState.REMOVED).withCharacteristic("AttributeOne", child4Value).build();

        CIFAsset rootCifAsset = aCIFAsset().withStatus(ProductInstanceState.CEASED)
                                           .withRelationship(child1, "Relationship1", RelationshipType.Child)
                                           .withRelationship(child2, "Relationship1", RelationshipType.RelatedTo)
                                           .withRelationship(child3, "Relationship1", RelationshipType.Child)
                                           .withRelationship(child4, "Relationship1", RelationshipType.Child)
                                           .build();

        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(rootCifAsset.getAssetKey(), newArrayList(CIFAssetExtension.Relationships)))).thenReturn(rootCifAsset);
        CIFAssetEvaluator cifAssetEvaluator = new CIFAssetEvaluator(rootCifAsset.getAssetKey(), cifAssetOrchestrator, evaluatorFactory);
        List<String> expressionPath = newArrayList("Relationship1");
        List<Object> values = cifAssetEvaluator.getValues(expressionPath, "AttributeOne");

        assertThat(values, is((List<Object>) newArrayList((Object) child1Value, child2Value, child3Value, child4Value)));
    }

    @Test
    public void shouldGetAllowedValuesFromOwner() {
        CIFAsset baseCifAsset = aCIFAsset().withQuoteOptionId("QO1").build();
        CIFAsset relatedToCifAsset = aCIFAsset().withCharacteristic("AttributeOne", "AttributeOneValue", ALLOWED_VALUES)
                                                .withQuoteOptionId("QO2")
                                                .withAssetVersionStatus(AssetVersionStatus.IN_SERVICE)
                                                .build();
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(baseCifAsset.getAssetKey()))).thenReturn(baseCifAsset);
        when(cifAssetOrchestrator.getOwnerAssets(new CIFAssetKey(baseCifAsset.getAssetKey(), newArrayList(CharacteristicAllowedValues)))).thenReturn(newArrayList(relatedToCifAsset));
        CIFAssetEvaluator cifAssetEvaluator = new CIFAssetEvaluator(baseCifAsset.getAssetKey(), cifAssetOrchestrator, evaluatorFactory);

        List<String> expressionPath = newArrayList("Owner");
        List<Object> allowedValues = cifAssetEvaluator.getValues(expressionPath, "AttributeOne#AllowedValues");

        assertThat(allowedValues.size(), is(1));
        @SuppressWarnings("unchecked")
        List<Object> firstAllowedValues = (List<Object>) allowedValues.get(0);
        assertThat(firstAllowedValues, is((List<Object>)(newArrayList((Object)"AttributeOneAllowedValue1", "AttributeOneAllowedValue2"))));
    }

    @Test(expected = ExpressionEvaluatorUnknownExpressionException.class)
    public void shouldThrowExceptionForUnknownCharacteristic() {
        CIFAsset baseCifAsset = aCIFAsset().build();
        CIFAsset relatedToCifAsset = aCIFAsset().withCharacteristics(1)
                                                .withRelationship(baseCifAsset, "relatedTo", RelationshipType.RelatedTo).build();
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(baseCifAsset.getAssetKey()))).thenReturn(baseCifAsset);
        when(cifAssetOrchestrator.getOwnerAssets(new CIFAssetKey(baseCifAsset.getAssetKey(), newArrayList(CharacteristicAllowedValues)))).thenReturn(newArrayList(relatedToCifAsset));
        CIFAssetEvaluator cifAssetEvaluator = new CIFAssetEvaluator(baseCifAsset.getAssetKey(), cifAssetOrchestrator, evaluatorFactory);

        List<String> expressionPath = newArrayList("Owner");
        cifAssetEvaluator.getValues(expressionPath, "AttributeTwo#AllowedValues");
    }
}