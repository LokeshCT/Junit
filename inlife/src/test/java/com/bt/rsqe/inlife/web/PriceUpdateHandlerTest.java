package com.bt.rsqe.inlife.web;

import com.bt.rsqe.ape.ApeFacade;
import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.domain.product.chargingscheme.PricingStrategy;
import com.bt.rsqe.pricing.PricingClient;
import com.bt.rsqe.pricing.config.dto.BillingTariffRulesetConfig;
import com.bt.rsqe.pricing.config.dto.ChargingSchemeConfig;
import com.bt.rsqe.pricing.config.dto.LocalRuleConfig;
import com.bt.rsqe.pricing.config.dto.LocalRuleParam;
import com.bt.rsqe.pricing.config.dto.PricingConfig;
import com.bt.rsqe.pricing.fixture.ChargingSchemeConfigFixture;
import com.bt.rsqe.projectengine.ProjectResource;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class PriceUpdateHandlerTest {
    @Mock
    private PricingClient pricingClient;
    @Mock
    private ProjectResource projectResource;
    @Mock
    private ApeFacade apeFacade;
    @Mock
    private ProductInstanceClient instanceClient;
    String sCode = "someCode";
    String chargingScheme = "scheme";
    @Mock
    private PricingConfig pricingConfig;

    @Before
    public void setUp(){
        when(pricingClient.getPricingConfig()).thenReturn(pricingConfig);
        PricingConfig.ChargingSchemeFilterCriteria chargingSchemeFilterCriteria = mock(PricingConfig.ChargingSchemeFilterCriteria.class);
        List<ChargingSchemeConfig> chargingSchemeConfigs = newArrayList();
        List<BillingTariffRulesetConfig> billingTariffRuleset = newArrayList();
        List<LocalRuleParam> localParamRuleList = newArrayList();
        localParamRuleList.add(new LocalRuleParam("nonRecurringQrefAttributeName","Base Install Cost"));
        localParamRuleList.add(new LocalRuleParam("recurringQrefAttributeName","Base Monthly Cost"));
        LocalRuleConfig localRuleConfig = new LocalRuleConfig("AccessCircuitCostline",localParamRuleList);
        billingTariffRuleset.add(BillingTariffRulesetConfig.Builder.get().withId("MCode").withLocalRuleConfigDTO(localRuleConfig).build());
        chargingSchemeConfigs.add(ChargingSchemeConfig.Builder.get().withPricingStrategy(PricingStrategy.LocalRuleBasedPricing.name()).withBillingTariffRuleSets(billingTariffRuleset).build());
        when(chargingSchemeFilterCriteria.search()).thenReturn(chargingSchemeConfigs);
        when(pricingConfig.chargingSchemes()).thenReturn(chargingSchemeFilterCriteria);
        when(chargingSchemeFilterCriteria.forName(anyString())).thenReturn(chargingSchemeFilterCriteria);
    }


    @Test
    @Ignore("Work In Progress")
    public void shouldUpdatePricelines() {
        try {
            PriceUpdateHandler handler = new PriceUpdateHandler(instanceClient, pricingClient, projectResource, apeFacade);
            handler.upliftPrices(sCode, chargingScheme, "01-10-2015", "31-10-2015");
        }catch (Exception e){
               //Handle Exception
        }
    }

}
