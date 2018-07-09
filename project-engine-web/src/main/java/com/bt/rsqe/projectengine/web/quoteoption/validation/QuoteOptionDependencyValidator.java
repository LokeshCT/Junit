package com.bt.rsqe.projectengine.web.quoteoption.validation;

import com.google.common.collect.Sets;

import java.util.Set;

public class QuoteOptionDependencyValidator {

    private final QuoteOptionDependency[] rules;

    public QuoteOptionDependencyValidator(QuoteOptionDependency[] rules) {
        this.rules = rules;
    }

    public Set<String> validate(String customerId, BillAccountCurrencyValidator billAccountCurrencyValidator) {
        Set<String> messages = Sets.newLinkedHashSet();
        for(QuoteOptionDependency rule : rules) {
            Set<String> ruleMessages = rule.validate(customerId, billAccountCurrencyValidator);
            messages.addAll(ruleMessages);
        }
        return messages;
    }
}
