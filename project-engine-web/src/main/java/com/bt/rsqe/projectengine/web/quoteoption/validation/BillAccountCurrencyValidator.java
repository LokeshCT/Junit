package com.bt.rsqe.projectengine.web.quoteoption.validation;

import com.bt.rsqe.customerrecord.BillingAccountDTO;

import java.util.List;
import java.util.Set;

public interface BillAccountCurrencyValidator {
    Set<String> validateBillAccount(List<BillingAccountDTO> billingAccounts);
}
