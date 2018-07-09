package com.bt.rsqe.inlife.web;

import com.bt.rsqe.ape.ApeFacade;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.keys.PriceUpdateKey;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.bt.rsqe.pricing.config.dto.BillingTariffRulesetConfig;
import com.bt.rsqe.pricing.config.dto.ChargingSchemeConfig;
import com.bt.rsqe.pricing.config.dto.LocalRuleConfig;
import com.bt.rsqe.pricing.config.dto.LocalRuleParam;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.bt.rsqe.utils.AssertObject.*;
import static com.google.common.collect.Maps.newHashMap;

public  class LocaRuleBasedCurrencyConverter implements CurrencyConverter {
    private static final Logger LOG = LogFactory.createDefaultLogger(Logger.class);


    private ApeFacade apeFacade;
    private List<ChargingSchemeConfig> chargingSchemeConfigs;

    public LocaRuleBasedCurrencyConverter(ApeFacade apeFacade, List<ChargingSchemeConfig> chargingSchemeConfigs) {
        this.apeFacade = apeFacade;
        this.chargingSchemeConfigs = chargingSchemeConfigs;
    }


    public Map<PriceUpdateKey, BigDecimal> getConvertedPrices(ProductInstance productInstance, String currency) {
        final BillingTariffRulesetConfig billingTariff = getBillingTariff();
        LocalRuleConfig ruleConfig = billingTariff.getLocalRuleConfig();
        Map<PriceUpdateKey, BigDecimal> convertedPrices = newHashMap();
        LOG.convertCostForRule(ruleConfig.getId());
        if(isNotNull(ruleConfig)){
             if("AccessCircuitCostline".equalsIgnoreCase(ruleConfig.getId())){
                 convertedPrices = new AccessCircuitCostlineCurrencyConverter(apeFacade, productInstance).getConvertedPrices(transformParams(ruleConfig), currency, billingTariff.getId());
             }
        }
        return convertedPrices;
    }

    public String getPmfid(){
       return getBillingTariff().getId();
    }

    private BillingTariffRulesetConfig getBillingTariff() {
        final ChargingSchemeConfig chargingSchemeConfig = chargingSchemeConfigs.get(0);
        final List<BillingTariffRulesetConfig> billingTariffRuleSets = chargingSchemeConfig.getBillingTariffRuleSets();
        final Optional<BillingTariffRulesetConfig> billingTariffRulesetConfigOptional = Iterables.tryFind(billingTariffRuleSets, new Predicate<BillingTariffRulesetConfig>() {
            @Override
            public boolean apply(BillingTariffRulesetConfig input) {
                return !isEmpty(input.getLocalRuleConfig().getId());
            }
        });
        if(billingTariffRulesetConfigOptional.isPresent()){
            return billingTariffRulesetConfigOptional.get();
        }
       else return null;
    }

    private Map<String, String> transformParams(LocalRuleConfig rule) {
        Map<String, String> params = newHashMap();

        for(LocalRuleParam localRuleParam : rule.getLocalRuleParamList()) {
            params.put(localRuleParam.getName(), localRuleParam.getValue());
        }

        return params;
    }
    private interface Logger {

        @Log(level = LogLevel.INFO, format = "Converting cost for Rule Id: %s ")
        void convertCostForRule(String ruleId);
    }
}

