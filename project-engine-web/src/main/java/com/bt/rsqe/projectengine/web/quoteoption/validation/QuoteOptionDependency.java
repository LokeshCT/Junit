package com.bt.rsqe.projectengine.web.quoteoption.validation;

import java.util.Set;

public interface QuoteOptionDependency {
    Set<String> validate(String customerId, BillAccountCurrencyValidator billAccountCurrencyValidator);
}
