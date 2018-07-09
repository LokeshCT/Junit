package com.bt.rsqe.projectengine.web.quoteoption.validation;

import com.bt.rsqe.customerrecord.BillingAccountDTO;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Sets.*;

public class QuoteOptionsBillAccountCurrencyValidator implements BillAccountCurrencyValidator {

    private Set<String> quoteOptionCurrencies;

    public QuoteOptionsBillAccountCurrencyValidator(Set<String> quoteOptionCurrencies) {
        this.quoteOptionCurrencies = quoteOptionCurrencies;
    }

    @Override
    public Set<String> validateBillAccount(List<BillingAccountDTO> billingAccounts) {
        Set<String> validationMessages = newHashSet();

        Set<String> billAccountCurrencies = newHashSet(Iterables.transform(billingAccounts, new Function<BillingAccountDTO, String>() {
            @Override
            public String apply(BillingAccountDTO input) {
                return input.getCurrencyCode();
            }
        }));

        for (String quoteOptionCurrency : quoteOptionCurrencies) {
            if(!billAccountCurrencies.contains(quoteOptionCurrency))  {
                validationMessages.add(String.format("No %s billing accounts associated with the customer", quoteOptionCurrency));
            }
        }
        return validationMessages;
    }
}
