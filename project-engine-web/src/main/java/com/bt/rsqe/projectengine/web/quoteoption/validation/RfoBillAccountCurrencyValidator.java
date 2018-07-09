package com.bt.rsqe.projectengine.web.quoteoption.validation;

import com.bt.rsqe.customerrecord.BillingAccountDTO;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Sets.*;

public class RfoBillAccountCurrencyValidator implements BillAccountCurrencyValidator {

    private String quoteOptionCurrency;

    public RfoBillAccountCurrencyValidator(String quoteOptionCurrency) {
        this.quoteOptionCurrency = quoteOptionCurrency;
    }

    @Override
    public Set<String> validateBillAccount(List<BillingAccountDTO> billingAccounts) {
        Optional<BillingAccountDTO> billingAccountDTOOptional = Iterables.tryFind(billingAccounts, new Predicate<BillingAccountDTO>() {
            @Override
            public boolean apply(BillingAccountDTO input) {
                return quoteOptionCurrency.equals(input.getCurrencyCode());
            }
        });

        if (!billingAccountDTOOptional.isPresent()) {
            return newHashSet(String.format("No billing account with the currency [%s] of the quote associated with the customer account.", quoteOptionCurrency));
        }
        return Collections.emptySet();
    }
}
