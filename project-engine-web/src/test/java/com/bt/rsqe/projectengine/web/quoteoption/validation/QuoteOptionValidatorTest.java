package com.bt.rsqe.projectengine.web.quoteoption.validation;

import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.internal.matchers.IsCollectionContaining.hasItem;
import static org.mockito.Mockito.*;

public class QuoteOptionValidatorTest {
    public static final String CUSTOMER_ID = "a_customer_id";
    
    private QuoteOptionDependencyValidator validator;
    private QuoteOptionDependency rule;
    private QuoteOptionDependency anotherRule;
    private BillAccountCurrencyValidator billAccountCurrencyValidator = mock(BillAccountCurrencyValidator.class);

    @Before
    public void setup() {
        rule = mock(QuoteOptionDependency.class);
        anotherRule = mock(QuoteOptionDependency.class);

        validator = new QuoteOptionDependencyValidator(new QuoteOptionDependency[] {
            rule,
            anotherRule
        });
    }

    @Test
    public void shouldValidateWithoutExceptions() {
        validator.validate(CUSTOMER_ID, billAccountCurrencyValidator);
    }

    @Test
    public void shouldCallValidators() {
        validator.validate(CUSTOMER_ID, billAccountCurrencyValidator);

        verify(rule).validate(CUSTOMER_ID, billAccountCurrencyValidator);
    }
    
    @Test
    public void shouldReturnMessagesFromRules() {
        when(rule.validate(CUSTOMER_ID, billAccountCurrencyValidator)).thenReturn(Sets.newHashSet("an error message"));
        
        Set<String> messages = validator.validate(CUSTOMER_ID, billAccountCurrencyValidator);

        assertThat(messages, hasItem("an error message"));
    }

    @Test
    public void shouldReturnMessagesFromMultipleRules() {
        when(rule.validate(CUSTOMER_ID, billAccountCurrencyValidator)).thenReturn(Sets.newHashSet("an error message"));
        when(anotherRule.validate(CUSTOMER_ID, billAccountCurrencyValidator)).thenReturn(Sets.newHashSet("another error message"));

        Set<String> messages = validator.validate(CUSTOMER_ID, billAccountCurrencyValidator);

        assertThat(messages, hasItem("an error message"));
        assertThat(messages, hasItem("another error message"));
    }
    

}

