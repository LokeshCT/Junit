package com.bt.rsqe.inlife.web;

import com.bt.rsqe.ape.ApeFacade;
import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.enums.AssetVersionStatus;
import com.bt.rsqe.keys.PriceUpdateKey;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.bt.rsqe.pricing.PricingClient;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionDTO;
import org.joda.time.DateTime;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.bt.rsqe.mis.client.WebMetricsResource.DATE_FORMAT;
import static com.bt.rsqe.utils.AssertObject.*;
import static org.apache.commons.lang.StringUtils.isEmpty;

@Path("/rsqe/inlife/priceuplift")
public class PriceUpdateHandler {
    private static final Logger LOG = LogFactory.createDefaultLogger(Logger.class);
    private final PricingClient pricingClient;
    private ProjectResource projectResource;
    private ApeFacade apeFacade;
    private ProductInstanceClient instanceClient;
    private static final String FROM_DATE = "fromDate";
    private static final String TO_DATE = "toDate";
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");


    public PriceUpdateHandler(ProductInstanceClient instanceClient, PricingClient pricingClient, ProjectResource projectResource, ApeFacade apeFacade) {
        this.instanceClient = instanceClient;
        this.pricingClient = pricingClient;
        this.projectResource = projectResource;
        this.apeFacade = apeFacade;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("scode/{sCode}/chargingScheme/{chargingScheme}")
    public Response upliftPrices(@PathParam("sCode") String sCode, @PathParam("chargingScheme") String chargingScheme,@QueryParam(FROM_DATE) String fromDate,
                                 @QueryParam(TO_DATE) String toDate) throws  Exception{
        if (isEmpty(sCode) || isEmpty(chargingScheme)) {
            LOG.priceArgNotValid(sCode, chargingScheme);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        final PricingStrategyLookup pricingStrategyLookup = new PricingStrategyLookup(pricingClient, apeFacade);
        final CurrencyConverter converter = pricingStrategyLookup.findConverter(chargingScheme);
        if (isNull(converter)) {
            LOG.priceArgNotValid(sCode, chargingScheme);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
         try{
        List<AssetKey> assets = instanceClient.getAssetKeysByDate(com.bt.rsqe.domain.product.ProductSCode.newInstance(sCode), DATE_FORMAT.parse(fromDate), DATE_FORMAT.parse(toDate), AssetVersionStatus.DRAFT.name());
        for (AssetKey assetKey : assets) {
            try {
                final ProductInstance productInstance = instanceClient.getByAssetKey(assetKey);
                String currency = getQuoteCurrencyFor(productInstance);
                final String productInstanceId = productInstance.getProductInstanceId().getValue();
                final Long productInstanceVersion = productInstance.getProductInstanceVersion();
                LOG.priceUpliftStarted(productInstanceId, productInstanceVersion, currency);
                final Map<PriceUpdateKey, BigDecimal> convertedPrices = converter.getConvertedPrices(productInstance, currency);
                LOG.convertedPrice(convertedPrices);
                String pmfId = converter.getPmfid();
                if (!convertedPrices.isEmpty()) {
                    new PriceUpdater(instanceClient).updatePrices(pmfId, productInstance, convertedPrices);
                }
            } catch (Exception e) {
                LOG.PriceUpdateError(assetKey);
            }
        }
        }catch(Exception pe){
                LOG.invalidDateFormat(fromDate, toDate);
             }

        return Response.ok().build();
    }

    private String getQuoteCurrencyFor(ProductInstance productInstance) {
        final QuoteOptionDTO quoteOptionDTO = projectResource.quoteOptionResource(productInstance.getProjectId()).get(productInstance.getQuoteOptionId());
        return quoteOptionDTO.getCurrency();
    }

    private interface Logger {

        @Log(level = LogLevel.INFO, format = "The given scode: %s : or chargingScheme :%s : is not valid")
        void priceArgNotValid(String sCode, String chargingScheme);

        @Log(level = LogLevel.INFO, format = "Price uplift started for asset : %s    - %s. Quote Currency : %s ")
        void priceUpliftStarted(String asset, long assetVersion, String quoteCurrency);

        @Log(level = LogLevel.INFO, format = "Error while updating the asset id : %s")
        void PriceUpdateError(AssetKey assetKey);

        @Log(level =LogLevel.INFO, format = "Date is not valid expected [dd-MM-yyyy] format: %s")
        void invalidDateFormat(String fromDate, String toDate);

        @Log(level =LogLevel.INFO, format = "Converted Price for to update: %s")
        void convertedPrice(Map<PriceUpdateKey, BigDecimal> convertedPrices);

    }

}
