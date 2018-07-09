package com.bt.rsqe.customerinventory.service.validation;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetKey;
import com.bt.rsqe.customerinventory.service.client.domain.ValidationNotification;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.evaluators.CIFAssetCharacteristicEvaluatorFactory;
import com.bt.rsqe.customerinventory.service.evaluators.CIFAssetEvaluator;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.product.extensions.RuleValidationCustomerAssetExpression;
import com.bt.rsqe.domain.product.extensions.RuleValidationExpression;
import com.bt.rsqe.domain.product.extensions.StructuredRule;
import com.bt.rsqe.domain.product.extensions.ValidationErrorType;
import com.bt.rsqe.expressionevaluator.ContextualEvaluatorMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;

import java.util.List;

import static com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension.*;
import static com.google.common.collect.Lists.*;
import static org.hamcrest.core.Is.*;
import static org.hamcrest.core.IsInstanceOf.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.*;

public class AssetValidatorTest {
    public static final String ERROR_1 = "Error1";
    public static final String WARNING_1 = "Warning1";

    @Captor
    private ArgumentCaptor<List<ContextualEvaluatorMap>> baseErrorContextualEvaluators, baseWarningContextualEvaluators;

    @Captor
    private ArgumentCaptor<List<ContextualEvaluatorMap>> baseErrorTextContextualEvaluators, baseWarningTextContextualEvaluators;

    private final AssetKey baseAssetKey = new AssetKey("ASSET_ID", 1l);
    private final AssetKey baseAssetKey1 = new AssetKey("ASSET_ID1", 1l);
    private AssetValidator validator;
    private final RuleValidationExpression errorRule = mock(RuleValidationExpression.class);
    private final RuleValidationExpression warningRule = mock(RuleValidationExpression.class);
    private final RuleValidationExpression satisfiedRule = mock(RuleValidationExpression.class);

    private final RuleValidationCustomerAssetExpression customerAssetErrorRule = mock(RuleValidationCustomerAssetExpression.class);
    private final RuleValidationCustomerAssetExpression customerAssetWarningRule = mock(RuleValidationCustomerAssetExpression.class);
    private final RuleValidationCustomerAssetExpression customerAssetSatisfiedRule = mock(RuleValidationCustomerAssetExpression.class);

    @Before
    public void setUp() throws Exception {
        initMocks(this);

        CIFAssetOrchestrator cifAssetOrchestrator = mock(CIFAssetOrchestrator.class);
        CIFAssetCharacteristicEvaluatorFactory evaluatorFactory = mock(CIFAssetCharacteristicEvaluatorFactory.class);
        validator = new AssetValidator(cifAssetOrchestrator, evaluatorFactory);

        CIFAsset cifAsset = mock(CIFAsset.class);
        when(cifAsset.hasExtension(CIFAssetExtension.ProductRules)).thenReturn(true);

        CIFAsset cifAsset1 = mock(CIFAsset.class);
        when(cifAsset.hasExtension(CIFAssetExtension.ProductRules)).thenReturn(true);

        when(errorRule.isValidationRule()).thenReturn(true);
        when(errorRule.getSatisfaction(anyListOf(ContextualEvaluatorMap.class))).thenReturn(ValidationErrorType.Error);
        when(errorRule.getErrorText(anyListOf(ContextualEvaluatorMap.class))).thenReturn(ERROR_1);

        when(customerAssetErrorRule.isValidationRule()).thenReturn(true);
        when(customerAssetErrorRule.getSatisfaction(anyListOf(ContextualEvaluatorMap.class))).thenReturn(ValidationErrorType.Error);
        when(customerAssetErrorRule.getErrorText(anyListOf(ContextualEvaluatorMap.class))).thenReturn(ERROR_1);

        when(warningRule.isValidationRule()).thenReturn(true);
        when(warningRule.getSatisfaction(anyListOf(ContextualEvaluatorMap.class))).thenReturn(ValidationErrorType.Warning);
        when(warningRule.getErrorText(anyListOf(ContextualEvaluatorMap.class))).thenReturn(WARNING_1);

        when(customerAssetWarningRule.isValidationRule()).thenReturn(true);
        when(customerAssetWarningRule.getSatisfaction(anyListOf(ContextualEvaluatorMap.class))).thenReturn(ValidationErrorType.Warning);
        when(customerAssetWarningRule.getErrorText(anyListOf(ContextualEvaluatorMap.class))).thenReturn(WARNING_1);

        when(satisfiedRule.isValidationRule()).thenReturn(true);
        when(satisfiedRule.getSatisfaction(anyListOf(ContextualEvaluatorMap.class))).thenReturn(ValidationErrorType.Satisfied);

        when(customerAssetSatisfiedRule.isValidationRule()).thenReturn(true);
        when(customerAssetSatisfiedRule.getSatisfaction(anyListOf(ContextualEvaluatorMap.class))).thenReturn(ValidationErrorType.Satisfied);

        StructuredRule nonValidationRule = mock(StructuredRule.class);
        when(nonValidationRule.isValidationRule()).thenReturn(false);

        List<StructuredRule> structuredRules = newArrayList(errorRule, warningRule, satisfiedRule, nonValidationRule);
        when(cifAsset.getProductRules()).thenReturn(structuredRules);

        List<StructuredRule> structuredRulesCustomerExpression = newArrayList(customerAssetErrorRule, customerAssetWarningRule, customerAssetSatisfiedRule, nonValidationRule);
        when(cifAsset1.getProductRules()).thenReturn(structuredRulesCustomerExpression);

        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(baseAssetKey, newArrayList(ProductRules, AsIsAsset)))).thenReturn(cifAsset);
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(baseAssetKey1, newArrayList(ProductRules, AsIsAsset)))).thenReturn(cifAsset1);
    }

    @Test
    public void shouldGetCorrectNotificationsFromRules() {
        ValidationNotification validationNotification1 = new ValidationNotification(ValidationErrorType.Error, ERROR_1);
        ValidationNotification validationNotification2 = new ValidationNotification(ValidationErrorType.Warning, WARNING_1);
        List<ValidationNotification> expectedNotification = newArrayList(validationNotification1, validationNotification2);

        List<ValidationNotification> notification = validator.validate(baseAssetKey);

        assertThat(notification, is(expectedNotification));
    }

    @Test
    public void shouldGetCorrectNotificationsFromCustomerAssetExpressionRules() {
        ValidationNotification validationNotification1 = new ValidationNotification(ValidationErrorType.Error, ERROR_1);
        ValidationNotification validationNotification2 = new ValidationNotification(ValidationErrorType.Warning, WARNING_1);
        List<ValidationNotification> expectedNotification = newArrayList(validationNotification1, validationNotification2);

        List<ValidationNotification> notification = validator.validate(baseAssetKey1);
        assertThat(notification, is(expectedNotification));
    }

    @Test
    public void shouldPassCIFAssetEvaluatorToRule() {
        validator.validate(baseAssetKey);

        // Check the correct ContextualEvaluatorMaps have been sent
        verify(errorRule).getSatisfaction(baseErrorContextualEvaluators.capture());
        List<List<ContextualEvaluatorMap>> baseErrorMapsCalls = baseErrorContextualEvaluators.getAllValues();
        assertThat(baseErrorMapsCalls.size(), is(1));
        ContextualEvaluatorMap baseErrorMap = baseErrorMapsCalls.get(0).get(0);
        assertThat(baseErrorMap.getEvaluatorIdentifier(), is(""));
        assertThat(baseErrorMap.getContextualEvaluator(), instanceOf(CIFAssetEvaluator.class));

        verify(warningRule).getSatisfaction(baseWarningContextualEvaluators.capture());
        List<List<ContextualEvaluatorMap>> baseWarningMapsCalls = baseWarningContextualEvaluators.getAllValues();
        assertThat(baseWarningMapsCalls.size(), is(1));
        ContextualEvaluatorMap baseWarningMap = baseWarningMapsCalls.get(0).get(0);
        assertThat(baseWarningMap.getEvaluatorIdentifier(), is(""));
        assertThat(baseWarningMap.getContextualEvaluator(), instanceOf(CIFAssetEvaluator.class));

        verify(errorRule).getErrorText(baseErrorTextContextualEvaluators.capture());
        List<List<ContextualEvaluatorMap>> baseErrorTextMapsCalls = baseErrorTextContextualEvaluators.getAllValues();
        assertThat(baseErrorTextMapsCalls.size(), is(1));
        ContextualEvaluatorMap baseErrorTextMap = baseErrorTextMapsCalls.get(0).get(0);
        assertThat(baseErrorTextMap.getEvaluatorIdentifier(), is(""));
        assertThat(baseErrorTextMap.getContextualEvaluator(), instanceOf(CIFAssetEvaluator.class));

        verify(warningRule).getErrorText(baseWarningTextContextualEvaluators.capture());
        List<List<ContextualEvaluatorMap>> baseWarningTextMapsCalls = baseWarningTextContextualEvaluators.getAllValues();
        assertThat(baseWarningTextMapsCalls.size(), is(1));
        ContextualEvaluatorMap baseWarningTextMap = baseWarningTextMapsCalls.get(0).get(0);
        assertThat(baseWarningTextMap.getEvaluatorIdentifier(), is(""));
        assertThat(baseWarningTextMap.getContextualEvaluator(), instanceOf(CIFAssetEvaluator.class));
    }
}