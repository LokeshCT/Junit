package com.bt.rsqe.inlife.web;

import com.bt.rsqe.ape.ApeFacade;
import com.bt.rsqe.domain.product.chargingscheme.PricingStrategy;
import com.bt.rsqe.pricing.PricingClient;
import com.bt.rsqe.pricing.config.dto.ChargingSchemeConfig;

import java.util.List;

public class PricingStrategyLookup {
    private PricingClient pricingClient;
    private ApeFacade apeFacade;

    public PricingStrategyLookup(PricingClient pricingClient, ApeFacade apeFacade) {
        this.pricingClient = pricingClient;
        this.apeFacade = apeFacade;
    }

    public CurrencyConverter findConverter(String chargingSchemeName) {
        final List<ChargingSchemeConfig> chargingSchemeConfigs = pricingClient.getPricingConfig().chargingSchemes().forName(chargingSchemeName).search();
        // Assuming that for a charging scheme name only one config will be present.
        if(!chargingSchemeConfigs.isEmpty()) {
            final PricingStrategy pricingStrategy = PricingStrategy.valueOf(chargingSchemeConfigs.get(0).getPricingStrategy());
            switch (pricingStrategy){
               case LocalRuleBasedPricing:
                  return new LocaRuleBasedCurrencyConverter(apeFacade,chargingSchemeConfigs);
            }
        }

        return null;
    }
}
