package com.bt.rsqe.inlife.web;

import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.dto.AssetDTO;
import com.bt.rsqe.customerinventory.dto.FutureAssetRelationshipDTO;
import com.bt.rsqe.customerinventory.dto.PriceLineDTO;
import com.bt.rsqe.customerinventory.parameter.LengthConstrainingProductInstanceId;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerinventory.parameter.ProductInstanceVersion;
import com.bt.rsqe.customerinventory.parameter.ProjectId;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.PriceBookDTO;
import com.bt.rsqe.domain.bom.parameters.QuoteOptionId;
import com.bt.rsqe.domain.product.PriceType;
import com.bt.rsqe.domain.product.ProductSCode;
import com.bt.rsqe.domain.product.parameters.RelationshipName;
import com.bt.rsqe.domain.project.PriceLine;
import com.bt.rsqe.domain.project.PricingStatus;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.enums.AssetVersionStatus;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.bt.rsqe.pricing.AutoPriceAggregator;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemResource;
import com.bt.rsqe.projectengine.QuoteOptionResource;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.joda.time.DateTime;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static com.bt.rsqe.enums.AssetVersionStatus.*;
import static com.bt.rsqe.utils.AssertObject.*;
import static com.google.common.collect.Lists.*;
import static org.apache.commons.lang.StringUtils.*;

//This is just a Data uplift handler as the uplift cannot be done using db scripts, because price lines of current instance (cpe) needs to be aggregated to owner's parent (ICG topology)
//This should be done for both cpe and secondary CPEs.
//This service should be called from client (JMeter, java code) to trigger uplift in Test and Live environments and its an one time activity.
///CLOVER:OFF
@Path("/rsqe/inlife/uplift/cpe")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CPEUpliftResourceHandler {

    private static final Logger LOG = LogFactory.createDefaultLogger(Logger.class);
    private static final String PRODUCT_CODE = "productCode";
    private static final String CPE_CEASE = "CPE Cease";
    private final ProductInstanceClient productInstanceClient;
    private final AutoPriceAggregator autoPriceAggregator;
    private ProjectResource projectResource;
    private static final String ASSET_ID = "assetId";
    private static final String TOTAL_ICG_CPE_PRICE = "Total ICg CPE Price";
    private static final String TOTAL_CPE_CEASE = "Total CPE Cease";
    private static final String TOTAL_ICG_CPE_CANCELLATION_CHARGE = "Total ICg CPE Cancellation Charge";

    public CPEUpliftResourceHandler(ProductInstanceClient productInstanceClient, AutoPriceAggregator autoPriceAggregator, ProjectResource projectResource) {
        this.productInstanceClient = productInstanceClient;
        this.autoPriceAggregator = autoPriceAggregator;
        this.projectResource = projectResource;
    }

    @PUT
    @Path("products/{productCode}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response uplift(@PathParam(PRODUCT_CODE) String productCode, @QueryParam("days") String days, @QueryParam(ASSET_ID) String assetId, @QueryParam("assetVersion") String assetVersion) {
        try {
            int noOfDays = 365;
            if (isNotEmpty(days)) {
                noOfDays = Integer.parseInt(days);
            }

            LOG.upliftRequestReceived(noOfDays);

            Date forThisYear = new DateTime().minusDays(noOfDays).toDate();
            List<AssetKey> assetKeysByProduct = newArrayList();

            if (isNotEmpty(assetId) && isNotEmpty(assetVersion)) {
                assetKeysByProduct.add(AssetKey.newInstance(assetId, Long.parseLong(assetVersion)));
            } else {
                assetKeysByProduct = productInstanceClient.getAssetKeysByProduct(ProductSCode.newInstance(productCode), forThisYear, "");
            }

            Collections.sort(assetKeysByProduct, new AssetKeyComparator());

            LOG.noOfEligibleAssets(assetKeysByProduct.size(), assetKeysByProduct);

            for (AssetKey assetKey : assetKeysByProduct) {
                try {
                    AssetDTO assetDTO = productInstanceClient.getAssetDtoByAssetKey(new LengthConstrainingProductInstanceId(assetKey.getAssetId()), new ProductInstanceVersion(assetKey.getAssetVersion()));
                    if (isValidAsset(assetDTO)) {
                        List<AssetDTO> priceApplicableCpe = getPriceApplicableCpes(assetDTO);
                        boolean isChangeInCpeAsset = false;

                        if (!priceApplicableCpe.isEmpty()) {
                            LOG.cpeUpliftStarted(assetKey.getAssetId(), assetKey.getAssetVersion());
                            isChangeInCpeAsset = renameAndRemovePriceLines(priceApplicableCpe);

                            if (isChangeInCpeAsset) {
                                save(priceApplicableCpe);
                            }
                        }

                        if (isChangeInCpeAsset) {
                            aggregatePriceLines(assetKey);
                            LOG.cpeUpliftCompleted(assetKey.getAssetId(), assetKey.getAssetVersion());
                        } else {
                            LOG.cpeUpliftNotRequired(assetKey.getAssetId(), assetKey.getAssetVersion());
                        }
                    } else {
                        LOG.cpeUpliftNotRequired(assetKey.getAssetId(), assetKey.getAssetVersion());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    LOG.cpeUpliftFailed(assetKey.getAssetId(), assetKey.getAssetVersion(), e);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        LOG.upliftDone();
        return Response.ok().build();
    }

    @PUT
    @Path("ICG/Prices/products/{productCode}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response upliftCPEICGPrices(@PathParam(PRODUCT_CODE) String productCode, @QueryParam("days") String days, @QueryParam(ASSET_ID) String assetId, @QueryParam("assetVersion") String assetVersion) {

        JsonArray jsonICGArray = new JsonArray();
        try {
            int noOfDays = 365;
            if (isNotEmpty(days)) {
                noOfDays = Integer.parseInt(days);
            }

            LOG.upliftRequestReceived(noOfDays);

            Date forThisYear = new DateTime().minusDays(noOfDays).toDate();
            List<AssetKey> assetKeysByProduct = newArrayList();

            if (isNotEmpty(assetId) && isNotEmpty(assetVersion)) {
                assetKeysByProduct.add(AssetKey.newInstance(assetId, Long.parseLong(assetVersion)));
            } else {
                assetKeysByProduct = productInstanceClient.getAssetKeysByProduct(ProductSCode.newInstance(productCode), forThisYear, "");
            }

            Collections.sort(assetKeysByProduct, new AssetKeyComparator());

            LOG.noOfEligibleAssets(assetKeysByProduct.size(), assetKeysByProduct);

            for (AssetKey assetKey : assetKeysByProduct) {
                try {
                    AssetDTO assetDTO = productInstanceClient.getAssetDtoByAssetKey(new LengthConstrainingProductInstanceId(assetKey.getAssetId()), new ProductInstanceVersion(assetKey.getAssetVersion()));
                    if (isValidAsset(assetDTO)) {
                        JsonObject jsonObject = new JsonObject();
                        JsonArray jsonArray = new JsonArray();
                        assetDTO.removePriceLines(getICGCPEPriceLines(assetDTO.getPriceLines()));
                        productInstanceClient.putAsset(assetDTO);
                        List<AssetDTO> priceApplicableCpe = getPriceApplicableCpes(assetDTO);
                        for(AssetDTO cpeAsset : priceApplicableCpe){
                            if(isEligibleForCpePriceLineUplift(cpeAsset)){
                                LOG.cpeUpliftStarted(cpeAsset.getId(), cpeAsset.getVersion());
                                aggregatePriceLines(AssetKey.newInstance(cpeAsset.getId(), cpeAsset.getVersion()));
                                LOG.cpeUpliftCompleted(assetKey.getAssetId(), assetKey.getAssetVersion());
                                jsonArray.add(getJsonElements(cpeAsset));
                                updateProductInstance(cpeAsset);
                            }
                        }
                        if(jsonArray.size()>0){
                            jsonObject.add("ICG ASSET", getJsonElements(assetDTO));
                            jsonObject.add("CPE ASSETS", jsonArray);
                            jsonICGArray.add(jsonObject);
                            updateProductInstance(assetDTO);
                        }
                    } else {
                        LOG.cpeUpliftNotRequired(assetKey.getAssetId(), assetKey.getAssetVersion());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    LOG.cpeUpliftFailed(assetKey.getAssetId(), assetKey.getAssetVersion(), e);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        LOG.upliftDone();
        LOG.upliftedAssets(jsonICGArray.toString());
        return Response.ok(jsonICGArray.toString()).build();
    }

    @PUT
    @Path("web-vpn/Prices/products/{productCode}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response upliftWepVpnPrices(@PathParam(PRODUCT_CODE) String productCode, @QueryParam("days") String days, @QueryParam(ASSET_ID) String assetId, @QueryParam("assetVersion") String assetVersion) {
        try {
            int noOfDays = 365;
            if (isNotEmpty(days)) {
                noOfDays = Integer.parseInt(days);
            }

            LOG.upliftRequestReceived(noOfDays);

            Date forThisYear = new DateTime().minusDays(noOfDays).toDate();
            List<AssetKey> assetKeysByProduct = newArrayList();

            if (isNotEmpty(assetId) && isNotEmpty(assetVersion)) {
                assetKeysByProduct.add(AssetKey.newInstance(assetId, Long.parseLong(assetVersion)));
            } else {
                assetKeysByProduct = productInstanceClient.getAssetKeysByProduct(ProductSCode.newInstance(productCode), forThisYear, "");
            }

            Collections.sort(assetKeysByProduct, new AssetKeyComparator());

            LOG.noOfEligibleAssets(assetKeysByProduct.size(), assetKeysByProduct);

            for (AssetKey assetKey : assetKeysByProduct) {
                try {
                    AssetDTO assetDTO = productInstanceClient.getAssetDtoByAssetKey(new LengthConstrainingProductInstanceId(assetKey.getAssetId()), new ProductInstanceVersion(assetKey.getAssetVersion()));
                    if (isValidAsset(assetDTO)) {
                            LOG.webVpnUpliftStarted(assetKey.getAssetId(), assetKey.getAssetVersion());
                            aggregatePriceLines(assetKey);
                            LOG.webVpnUpliftCompleted(assetKey.getAssetId(), assetKey.getAssetVersion());
                    } else {
                        LOG.webVpnUpliftNotRequired(assetKey.getAssetId(), assetKey.getAssetVersion());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    LOG.webVpnUpliftFailed(assetKey.getAssetId(), assetKey.getAssetVersion(), e);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        LOG.upliftDone();
        return Response.ok().build();
    }

    @PUT
    @Path("assetId/{assetId}/alignPriceLineId")
    @Produces(MediaType.APPLICATION_JSON)           //This service is added to uplift the price line Id.   //TODO - In progress
    public Response upliftPriceLineID(@PathParam(ASSET_ID) String assetId) {
        try {
            List<ProductInstance> productInstances = productInstanceClient.getAssets(new LengthConstrainingProductInstanceId(assetId));
            Collections.sort(productInstances, new ProductInstanceComparator());

            Optional<ProductInstance> productInstanceOptional = getFirstVersion(productInstances);

            LOG.priceLineUpliftRequestReceived(assetId);

            if (productInstanceOptional.isPresent()) {
                ProductInstance initialAsset = productInstanceOptional.get();
                PriceLine cpeOneTime_IA = getPriceLine(initialAsset, TOTAL_ICG_CPE_PRICE, PriceType.ONE_TIME);
                PriceLine cpeRec_IA = getPriceLine(initialAsset, TOTAL_ICG_CPE_PRICE, PriceType.RECURRING);
                PriceLine cpeCeaseOneTime_IA = getPriceLine(initialAsset, TOTAL_CPE_CEASE, PriceType.ONE_TIME);
                PriceLine cpeCeaseRec_IA = getPriceLine(initialAsset, TOTAL_CPE_CEASE, PriceType.RECURRING);
                PriceLine cpeCanOneTime_IA = getPriceLine(initialAsset, TOTAL_ICG_CPE_CANCELLATION_CHARGE, PriceType.ONE_TIME);
                PriceLine cpeCanRec_IA = getPriceLine(initialAsset, TOTAL_ICG_CPE_CANCELLATION_CHARGE, PriceType.RECURRING);

                for (ProductInstance productInstance : productInstances) {
                    if (productInstance.getProductInstanceVersion() != 1) {
                        boolean isPriceLineIdChanged = false;
                        PriceLine cpeOneTime = getPriceLine(productInstance, TOTAL_ICG_CPE_PRICE, PriceType.ONE_TIME);
                        PriceLine cpeRec = getPriceLine(productInstance, TOTAL_ICG_CPE_PRICE, PriceType.RECURRING);
                        PriceLine cpeCeaseOneTime = getPriceLine(productInstance, TOTAL_CPE_CEASE, PriceType.ONE_TIME);
                        PriceLine cpeCeaseRec = getPriceLine(productInstance, TOTAL_CPE_CEASE, PriceType.RECURRING);
                        PriceLine cpeCanOneTime = getPriceLine(productInstance, TOTAL_ICG_CPE_CANCELLATION_CHARGE, PriceType.ONE_TIME);
                        PriceLine cpeCanRec = getPriceLine(productInstance, TOTAL_ICG_CPE_CANCELLATION_CHARGE, PriceType.RECURRING);

                        if (isDiffers(cpeOneTime_IA, cpeOneTime)) {
                            cpeOneTime.updatePricesLineId(cpeOneTime_IA.getId());
                            isPriceLineIdChanged = true;
                        }

                        if (isDiffers(cpeRec_IA, cpeRec)) {
                            cpeRec.updatePricesLineId(cpeRec_IA.getId());
                            isPriceLineIdChanged = true;
                        }

                        if (isDiffers(cpeCeaseOneTime_IA, cpeCeaseOneTime)) {
                            cpeCeaseOneTime.updatePricesLineId(cpeCeaseOneTime_IA.getId());
                            isPriceLineIdChanged = true;
                        }

                        if (isDiffers(cpeCeaseRec_IA, cpeCeaseRec)) {
                            cpeCeaseRec.updatePricesLineId(cpeCeaseRec_IA.getId());
                            isPriceLineIdChanged = true;
                        }

                        if (isDiffers(cpeCanOneTime_IA, cpeCanOneTime)) {
                            cpeCanOneTime.updatePricesLineId(cpeCanOneTime_IA.getId());
                            isPriceLineIdChanged = true;
                        }

                        if (isDiffers(cpeCanRec_IA, cpeCanRec)) {
                            cpeCanRec.updatePricesLineId(cpeCanRec_IA.getId());
                            isPriceLineIdChanged = true;
                        }

                        if (isPriceLineIdChanged) {
                            LOG.priceLineUpliftDone(assetId, productInstance.getProductInstanceVersion());
                            productInstanceClient.put(productInstance);
                        } else {
                            LOG.priceLineUpliftNotRequired(assetId, productInstance.getProductInstanceVersion());
                        }

                    }

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        return Response.ok().build();
    }

    private boolean isDiffers(PriceLine cpeOneTimeInitialAsset, PriceLine cpeOneTime) {
        return isNotNull(cpeOneTimeInitialAsset) && isNotNull(cpeOneTime) && !cpeOneTimeInitialAsset.getId().equals(cpeOneTime.getId());
    }

    private PriceLine getPriceLine(ProductInstance productInstance, String name, PriceType type) {
        for (PriceLine priceLine : productInstance.getPriceLines()) {
            if (priceLine.getChargingSchemeName().equals(name) && priceLine.getPriceType().equals(type)) {
                return priceLine;
            }
        }
        return null;
    }

    private Optional<ProductInstance> getFirstVersion(List<ProductInstance> productInstances) {
        return Iterables.tryFind(productInstances, new Predicate<ProductInstance>() {
            @Override
            public boolean apply(ProductInstance input) {
                return input.getProductInstanceVersion() == 1;
            }
        });
    }

    private void aggregatePriceLines(AssetKey assetKey) {
        ProductInstance productInstance = productInstanceClient.getByAssetKey(assetKey);
        PriceBookDTO priceBook = getPriceBook(new ProjectId(productInstance.getProjectId()),
                                              new QuoteOptionId(productInstance.getQuoteOptionId()),
                                              new LineItemId(productInstance.getLineItemId()));
        autoPriceAggregator.aggregatePricesOf(priceBook, new LineItemId(productInstance.getLineItemId()));
    }

    private void save(List<AssetDTO> priceApplicableCpe) {
        for (AssetDTO dto : priceApplicableCpe) {
            productInstanceClient.putAsset(dto);
        }
    }

    private boolean renameAndRemovePriceLines(List<AssetDTO> priceApplicableCpe) {
        boolean changeInCpeAsset = false;
        for (AssetDTO dto : priceApplicableCpe) {
            List<PriceLineDTO> removablePriceLines = newArrayList();
            for (PriceLineDTO priceLineDTO : dto.getPriceLines()) {
                if (priceLineDTO.getChargingSchemeName().equals(TOTAL_CPE_CEASE)) {
                    priceLineDTO.setChargingSchemeName(CPE_CEASE);
                    priceLineDTO.setPriceLineName(CPE_CEASE);
                    priceLineDTO.setPriceLineDesc(CPE_CEASE);
                    changeInCpeAsset = true;
                }

                if (priceLineDTO.getChargingSchemeName().equals("Total CPE Price")
                    || priceLineDTO.getChargingSchemeName().equals("Total CPE Cancellation Charge")) {
                    removablePriceLines.add(priceLineDTO);
                    changeInCpeAsset = true;
                }
            }
            dto.removePriceLines(removablePriceLines);
        }

        return changeInCpeAsset;
    }

    private List<AssetDTO> getPriceApplicableCpes(AssetDTO assetDTO) {
        List<AssetDTO> assetDTOs = newArrayList();
        FutureAssetRelationshipDTO accessRequirements = assetDTO.getRelationship(RelationshipName.newInstance("AccessRequirements"));
        if(accessRequirements == null) {
           return assetDTOs;
        }
        AssetDTO requirementAsset = accessRequirements.getRelatedAsset();
        FutureAssetRelationshipDTO cpe = requirementAsset.getRelationship(RelationshipName.newInstance("CPE"));
        FutureAssetRelationshipDTO secondaryCPE = requirementAsset.getRelationship(RelationshipName.newInstance("SecondaryCPE"));
        if (cpe != null) {
            if (!cpe.getRelatedAsset().getPriceLines().isEmpty() && isValidAsset(cpe.getRelatedAsset())) {
                AssetDTO cpeAsset = productInstanceClient.getAssetDtoByAssetKey(new LengthConstrainingProductInstanceId(cpe.getRelatedAsset().getId()), new ProductInstanceVersion(cpe.getRelatedAsset().getVersion()));
                assetDTOs.add(cpeAsset);
            }
        }

        if (secondaryCPE != null) {
            if (!secondaryCPE.getRelatedAsset().getPriceLines().isEmpty() && isValidAsset(secondaryCPE.getRelatedAsset())) {
                AssetDTO cpeAsset = productInstanceClient.getAssetDtoByAssetKey(new LengthConstrainingProductInstanceId(secondaryCPE.getRelatedAsset().getId()), new ProductInstanceVersion(secondaryCPE.getRelatedAsset().getVersion()));
                assetDTOs.add(cpeAsset);
            }
        }
        return assetDTOs;
    }

    private boolean isValidAsset(AssetDTO assetDTO) {
        List<AssetVersionStatus> inValidStatus = newArrayList(CANCELLED, CEASED, OBSOLETE, REJECTED);
        return !assetDTO.isCancelled() && !assetDTO.isCeased() && !inValidStatus.contains(assetDTO.getAssetVersionStatus());
    }

    public PriceBookDTO getPriceBook(ProjectId projectId, QuoteOptionId quoteOptionId, LineItemId lineItemId) {

        QuoteOptionResource quoteOptionResource = projectResource.quoteOptionResource(projectId.value());
        QuoteOptionItemResource quoteOptionItemResource = quoteOptionResource.quoteOptionItemResource(quoteOptionId.getValue());
        QuoteOptionItemDTO quoteOptionItemDTO = quoteOptionItemResource.get(lineItemId.value());

        //Null check is not required as price book is mandatory for a line item.
        return quoteOptionItemDTO.contractDTO.priceBooks.get(0);
    }

    private List<PriceLineDTO> getICGCPEPriceLines(List<PriceLineDTO> priceLines) {
        return newArrayList(Iterables.filter(priceLines, new Predicate<PriceLineDTO>() {
            @Override
            public boolean apply(PriceLineDTO input) {
                return input.getChargingSchemeName().equals(TOTAL_ICG_CPE_PRICE)
                       || input.getChargingSchemeName().equals(TOTAL_CPE_CEASE)
                       || input.getChargingSchemeName().equals(TOTAL_ICG_CPE_CANCELLATION_CHARGE);
            }
        }));
    }

    private boolean isEligibleForCpePriceLineUplift(AssetDTO assetDTO) {
        return getICGCPEPriceLines(assetDTO.getPriceLines()).size()>0 ? false : true;
    }

    private JsonElement getJsonElements(AssetDTO assetDTO) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("ASSET_ID",assetDTO.getId());
        jsonObject.addProperty("ASSET_VERSION",assetDTO.getVersion());
        return jsonObject;
    }

    public static class AssetKeyComparator implements Comparator<AssetKey> {
        @Override
        public int compare(AssetKey scheme1, AssetKey scheme2) {
            if (scheme2.getAssetVersion() > scheme1.getAssetVersion()) {
                return -1;
            }
            if (scheme2.getAssetVersion() < scheme1.getAssetVersion()) {
                return 1;
            }
            return 0;
        }
    }

    private void updateProductInstance(AssetDTO asset) {
        ProductInstance productInstance = productInstanceClient.getByAssetKey(new AssetKey(asset.getId(), asset.getVersion()));
        productInstance.setPricingStatus(asset.getPricingStatus());
        productInstance.setInitialBillingStartDate(asset.getInitialBillingStartDate());

        for (PriceLine priceLine : productInstance.getPriceLines()) {
            if (PricingStatus.EXPIRED.getDescription().equals(priceLine.getStatus().getDescription())) {
                priceLine.setAsFirm();
            }
        }

        productInstanceClient.put(productInstance);
    }


    private interface Logger {

        @Log(level = LogLevel.INFO, format = "CPE uplift started for asset : %s    - %s")
        void cpeUpliftStarted(String asset, long assetVersion);

        @Log(level = LogLevel.INFO, format = "WebVpn uplift started for asset : %s    - %s")
        void webVpnUpliftStarted(String asset, long assetVersion);

        @Log(level = LogLevel.INFO, format = "CPE uplift completed for asset : %s    - %s")
        void cpeUpliftCompleted(String asset, long assetVersion);

        @Log(level = LogLevel.INFO, format = "web vpn uplift completed for asset : %s    - %s")
        void webVpnUpliftCompleted(String asset, long assetVersion);

        @Log(level = LogLevel.ERROR, format = "CPE uplift failed for asset : %s   - %s")
        void cpeUpliftFailed(String assetId, long assetVersion, Exception ex);

        @Log(level = LogLevel.ERROR, format = "CPE uplift failed for asset : %s   - %s")
        void webVpnUpliftFailed(String assetId, long assetVersion, Exception ex);

        @Log(level = LogLevel.INFO, format = "CPE uplift request received for days - %s")
        void upliftRequestReceived(int noOfDays);

        @Log(level = LogLevel.INFO, format = "CPE uplift done")
        void upliftDone();

        @Log(level = LogLevel.INFO, format = "No of eligible assets - %s, asset details - %s")
        void noOfEligibleAssets(int assets, List<AssetKey> assetKeysByProduct);

        @Log(level = LogLevel.INFO, format = "CPE uplift not required - %s     - %s")
        void cpeUpliftNotRequired(String assetId, long assetVersion);

        @Log(level = LogLevel.INFO, format = "Web vpn uplift not required - %s     - %s")
        void webVpnUpliftNotRequired(String assetId, long assetVersion);

        @Log(level = LogLevel.INFO, format = "ICG Price line uplift request received - %s")
        void priceLineUpliftRequestReceived(String assetId);

        @Log(level = LogLevel.INFO, format = "ICG Price line uplift done for - %s - %s")
        void priceLineUpliftDone(String assetId, Long productInstanceVersion);

        @Log(level = LogLevel.INFO, format = "ICG Price line uplift not required for - %s - %s")
        void priceLineUpliftNotRequired(String assetId, Long productInstanceVersion);

        @Log(level = LogLevel.INFO, format = "ICG CPE Price line uplift Data - %s")
        void upliftedAssets(String assetsInfo);
    }

    private class ProductInstanceComparator implements Comparator<ProductInstance> {
        @Override
        public int compare(ProductInstance o1, ProductInstance o2) {
            if (o2.getProductInstanceVersion() > o1.getProductInstanceVersion()) {
                return -1;
            }
            if (o2.getProductInstanceVersion() < o1.getProductInstanceVersion()) {
                return 1;
            }
            return 0;
        }
    }
}

///CLOVER:ON