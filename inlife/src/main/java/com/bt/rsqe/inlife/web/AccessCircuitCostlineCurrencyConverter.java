package com.bt.rsqe.inlife.web;

import com.bt.rsqe.ape.ApeFacade;
import com.bt.rsqe.ape.dto.ApeQref;
import com.bt.rsqe.ape.dto.ApeQrefAttributeDetail;
import com.bt.rsqe.domain.ExchangeRates;
import com.bt.rsqe.domain.product.PriceType;
import com.bt.rsqe.domain.project.PriceLine;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.keys.PriceUpdateKey;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.google.common.base.Optional;
import com.google.common.base.Strings;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static com.bt.rsqe.utils.AssertObject.*;

public class AccessCircuitCostlineCurrencyConverter {
    private static final Logger LOG = LogFactory.createDefaultLogger(Logger.class);
    public static final String RECURRING_QREF_ATTRIBUTE_NAME_PARAM = "recurringQrefAttributeName";
    public static final String NON_RECURRING_QREF_ATTRIBUTE_NAME_PARAM = "nonRecurringQrefAttributeName";
    public static final String USD_EXCHANGE_RATE = "USD Exchange rate";
    public static final String GBP_EXCHANGE_RATE = "GBP Exchange rate";
    public static final String EUR_EXCHANGE_RATE = "EUR Exchange rate";
    private final ApeFacade apeFacade;
    private final ProductInstance productInstance;
    Map<PriceUpdateKey, BigDecimal> convertedPrices = new HashMap<PriceUpdateKey, BigDecimal>();

    public AccessCircuitCostlineCurrencyConverter(ApeFacade apeFacade, ProductInstance productInstance) {
        this.apeFacade = apeFacade;
        this.productInstance = productInstance;
    }

    public Map<PriceUpdateKey, BigDecimal> getConvertedPrices(Map<String, String> params, String currency, String pmfId){
        checkParams(params,RECURRING_QREF_ATTRIBUTE_NAME_PARAM, NON_RECURRING_QREF_ATTRIBUTE_NAME_PARAM);
        final String stencilId = productInstance.getStencilId();
        ApeQref apeQref = apeFacade.qrefResource(stencilId).get();
        addPriceForCurrency(apeQref,params.get(NON_RECURRING_QREF_ATTRIBUTE_NAME_PARAM), PriceType.ONE_TIME,currency, pmfId);
        addPriceForCurrency(apeQref,params.get(RECURRING_QREF_ATTRIBUTE_NAME_PARAM), PriceType.RECURRING,currency, pmfId);
        return convertedPrices;
    }

    private void addPriceForCurrency(ApeQref apeQref, String attributeName,
                                     PriceType chargeType,
                                     String currency, String pmfId)  {

        LOG.addingCurrency(apeQref.getQrefId(), attributeName, chargeType, currency, pmfId);
        Optional<ApeQrefAttributeDetail> costAttribute = apeQref.getAttribute(attributeName);
        String price = "0";
        if(costAttribute.isPresent() && !Strings.isNullOrEmpty(costAttribute.get().getAttributeValue())) {
            price = costAttribute.get().getAttributeValue();
        }
        ExchangeRates exchangeRates = exchangeRateFromApeResponse(apeQref);
        LOG.forExchangeRates(exchangeRates);
        final BigDecimal exchangeRate = getExchangeRateFor(currency, exchangeRates);
        PriceUpdateKey key = buildPriceUpdateKey(pmfId,chargeType);
        if(isNotNull(key)){
        convertedPrices.put(key,new BigDecimal(price).multiply(exchangeRate));
        }
    }

    private PriceUpdateKey buildPriceUpdateKey(String pmfId, PriceType chargeType) {
        for(PriceLine priceline:productInstance.getPriceLines()){
            if(pmfId.equalsIgnoreCase(priceline.getPmfId()) && chargeType.equals(priceline.getPriceType())){
                 return new PriceUpdateKey(chargeType,priceline.getPpsrId(),pmfId);
            }
        }
        return null;
    }

    private BigDecimal getExchangeRateFor(String currency,ExchangeRates exchangeRates) {
        if("USD".equalsIgnoreCase(currency)){
           return exchangeRates.toUSD();
        }else if("GBP".equalsIgnoreCase(currency)){
           return exchangeRates.toGBP();
        } else {
            return exchangeRates.toEUR();
        }
    }

    void checkParams(Map<String, String> params, String... expectedParams){
        for(String expectedParam : expectedParams) {
            if(!params.containsKey(expectedParam) || Strings.isNullOrEmpty(params.get(expectedParam))) {
                 //LOG
            }
        }
    }
    private ExchangeRates exchangeRateFromApeResponse(final ApeQref qref) {
        return new ExchangeRates() {
            @Override
            public BigDecimal toUSD() {
                final Optional<ApeQrefAttributeDetail> attribute = qref.getAttribute(USD_EXCHANGE_RATE);
                if(attribute.isPresent())
                    return isNull(attribute.get()) ? new BigDecimal(0) : new BigDecimal(attribute.get().getAttributeValue());
                else
                    return  new BigDecimal(0);
            }

            @Override
            public BigDecimal toGBP() {
                final Optional<ApeQrefAttributeDetail> attribute = qref.getAttribute(GBP_EXCHANGE_RATE);
                if(attribute.isPresent())
                    return isNull(attribute.get()) ? new BigDecimal(0) : new BigDecimal(attribute.get().getAttributeValue());
                else
                    return  new BigDecimal(0);
            }

            @Override
            public BigDecimal toEUR() {
                final Optional<ApeQrefAttributeDetail> attribute = qref.getAttribute(EUR_EXCHANGE_RATE);
                if(attribute.isPresent())
                    return isNull(attribute.get()) ? new BigDecimal(0) : new BigDecimal(attribute.get().getAttributeValue());
                else
                    return  new BigDecimal(0);
            }
        };
    }
    private interface Logger {

        @Log(level = LogLevel.INFO, format = "Adding currency Qref: %s, attributeName Rec or Non Recurring: %s, chargeType: %s, currency:%s for the pmfId: %s")
        void addingCurrency(String apeQref, String attributeName,PriceType chargeType,String currency, String pmfId);

        @Log(level = LogLevel.INFO, format = "Exchange Rate currency is: %s")
        void forExchangeRates(ExchangeRates exchangeRates);

    }
}
