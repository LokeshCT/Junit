package com.bt.rsqe.projectengine.web.view;


import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerrecord.CustomerResource;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.customerrecord.SiteResource;
import com.bt.rsqe.domain.bom.parameters.ProductSCode;
import com.bt.rsqe.domain.product.InstanceCharacteristic;
import com.bt.rsqe.domain.product.PriceType;
import com.bt.rsqe.domain.project.PriceLine;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.domain.project.TariffType;
import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.web.facades.QuoteOptionFacade;
import com.bt.rsqe.utils.GsonUtil;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Created by 608876182 on 01/02/2016.
 */
public class ViewConfigurationTreeBuilder {
    private QuoteOptionFacade quoteOptionFacade;
    private CustomerResource customerResource;
    private ProductInstanceClient productInstanceClient;
    private PmrClient pmrClient;
    Map<String, String> params;

    List<SiteDetails> siteDetailsList;
    private static final String CUSTOMER_ID = "customerId";
    private static final String PROJECT_ID = "projectId";
    private static final String QUOTE_OPTION_ID = "quoteOptionId";
    private static final String OFFER_ID = "offerId";
    private static final String ORDER_ID = "orderId";

    private static final Logger LOG = LoggerFactory.getLogger(ViewConfigurationTreeBuilder.class);

    public ViewConfigurationTreeBuilder(QuoteOptionFacade quoteOptionFacade, CustomerResource customerResource, ProductInstanceClient productInstanceClient, PmrClient pmrClient,
                                        Map<String, String> params) {
        this.quoteOptionFacade = quoteOptionFacade;
        this.customerResource = customerResource;
        this.productInstanceClient = productInstanceClient;
        this.pmrClient = pmrClient;
        this.params = params;
    }

    public JsonObject buildProductsBySiteTree(){
        LOG.info("buildProductsBySiteTree() called with params, QUOTE_OPTION_ID --> "+params.get(QUOTE_OPTION_ID)+" ORDER_ID --> "+params.get(ORDER_ID)+" OFFER_ID --> "+params.get(OFFER_ID));

        List<QuoteOptionItemDTO> quoteOptionItemDTOs = quoteOptionFacade.getAllQuoteOptionItem(params.get(PROJECT_ID), params.get(QUOTE_OPTION_ID));

        if(!Strings.isNullOrEmpty(params.get(ORDER_ID))){
            quoteOptionItemDTOs = filterLineItemsByOrder(quoteOptionItemDTOs, params.get(ORDER_ID));
        }
        else if(!Strings.isNullOrEmpty(params.get(OFFER_ID))){
            quoteOptionItemDTOs = filterLineItemsByOffer(quoteOptionItemDTOs, params.get(OFFER_ID));
        }

        Set<SiteDTO> allSites = new HashSet<>();
        ProductInstance instance;
        JsonObject jsonObject = null;

        try {
            //Get all the front catalogue products
            List<QuoteOptionItemDTO> frontCatalogueProducts = getFrontCatalogueProducts(quoteOptionItemDTOs);
            LOG.info("Front Catalog items obtained are "+frontCatalogueProducts.size());


            Map<String, List<ProductDetails>> catalogProductMap = new HashMap(frontCatalogueProducts.size());

            //Get all the site using front catalogue products
            SiteResource siteResource = customerResource.siteResource(params.get(CUSTOMER_ID));
            LOG.info("Obtained the Site Resource for the customer --> "+params.get(CUSTOMER_ID));

            List<ProductDetails> items;

            for(QuoteOptionItemDTO item : frontCatalogueProducts){
                LOG.info("Getting product instance for the item id " + item.getId());
                instance = productInstanceClient.get(new LineItemId(item.getId()));

                /**
                 * If the site Id is null then get the central site info.
                 */
                SiteDTO siteDTO = Strings.isNullOrEmpty(instance.getSiteId()) ? siteResource.getCentralSite(params.get(PROJECT_ID)) : siteResource.getSiteDetails(instance.getSiteId());
                String siteId = Strings.isNullOrEmpty(item.siteId) ? siteDTO.getSiteId().toString() : item.siteId;
                allSites.add(siteDTO);

                //Add product to catalogProductMap
                if(catalogProductMap.containsKey(siteId)) {
                    catalogProductMap.get(siteId).add(getProductDetails(instance));
                }
                else{
                    items = new ArrayList<>();
                    items.add(getProductDetails(instance));
                    catalogProductMap.put(siteId, items);
                }
            }

            SiteDetails siteDetails;
            siteDetailsList = new ArrayList<>(allSites.size());
            for(SiteDTO site : allSites){
                siteDetails = new SiteDetails(site.getSiteName(), site.getCountry(), catalogProductMap.get(site.getSiteId().toString()));
                siteDetailsList.add(siteDetails);
            }

            jsonObject = new JsonObject();
            jsonObject.add("sites", getSitesAsJson(siteDetailsList));
        }
        catch(Exception e){
            e.printStackTrace();
            LOG.error("Error while building ProductsBySiteTree.", e);
        }

        LOG.info("In buildProductsBySiteTree(). JSON created is --->" + jsonObject.toString());
        return jsonObject;
    }

    private List<QuoteOptionItemDTO> filterLineItemsByOffer(List<QuoteOptionItemDTO> quoteOptionItemDTOs, final String offerId) {
        return newArrayList(Iterables.filter(quoteOptionItemDTOs, new Predicate<QuoteOptionItemDTO>() {
            @Override
            public boolean apply(QuoteOptionItemDTO input) {
                return offerId.equals(input.getOfferId());
            }
        }));
    }

    private List<QuoteOptionItemDTO> filterLineItemsByOrder(List<QuoteOptionItemDTO> quoteOptionItemDTOs, final String orderId) {
        return newArrayList(Iterables.filter(quoteOptionItemDTOs, new Predicate<QuoteOptionItemDTO>() {
            @Override
            public boolean apply(QuoteOptionItemDTO input) {
                return orderId.equals(input.getOrderId());
            }
        }));
    }

    private ProductDetails getProductDetails(ProductInstance instance){
        Set<ProductInstance> relatedProductInstances;
        List<ProductDetails> relatedProducts = newArrayList();
        List<InstanceCharacteristic> attributes = instance.getInstanceCharacteristics();
        Collections.sort(attributes, new Comparator<InstanceCharacteristic>() {
            @Override
            public int compare(InstanceCharacteristic o1, InstanceCharacteristic o2) {
                return o1.getSpecifiedBy().getName().toString().compareTo(o2.getSpecifiedBy().getName().toString());
            }
        });
        ProductDetails productDetails = new ProductDetails(instance.getDisplayName(), attributes, instance.getPriceLines());
        ProductDetails relatedProductDetails;
        try {

            /**
             * Set the price lines.
             */
            productDetails.setPriceAndCostLineDetails();

            /**
             * Populate the related products for each front catalogue product.
             */
            relatedProductInstances = instance.getRelatedToInstances();
            relatedProductInstances.addAll(instance.getChildren());
            if (relatedProductInstances != null && !relatedProductInstances.isEmpty()) {
                for(ProductInstance relatedProductInstance : relatedProductInstances){
                    if(!pmrClient.productOffering(ProductSCode.newInstance(relatedProductInstance.getProductIdentifier().getProductId())).get().isInFrontCatalogue()) {
                        relatedProductDetails = new ProductDetails(relatedProductInstance.getDisplayName(), relatedProductInstance.getInstanceCharacteristics(), relatedProductInstance.getPriceLines());
                        relatedProductDetails.setPriceAndCostLineDetails();
                        relatedProducts.add(relatedProductDetails);
                    }
                }
                productDetails.setRelatedProducts(relatedProducts);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("Error while getting ProductDetails.", e);
        }
        return productDetails;
    }

    private List<QuoteOptionItemDTO> getFrontCatalogueProducts(List<QuoteOptionItemDTO> quoteOptionItemDTOs) {
        LOG.info("Fetching frontCatalogueProducts..");
        List<QuoteOptionItemDTO> frontCatalogueItems = newArrayList();
        for(QuoteOptionItemDTO quoteOptionDTO : quoteOptionItemDTOs) {
            LOG.info("Adding the QuoteOptionItemDTO for the product --> " + quoteOptionDTO.sCode);
            if (pmrClient.productOffering(ProductSCode.newInstance(quoteOptionDTO.sCode)).get().isInFrontCatalogue()) {
                frontCatalogueItems.add(quoteOptionDTO);
            }
        }
        return frontCatalogueItems;
    }

    private JsonElement getSitesAsJson(List<SiteDetails> sites) {
        JsonArray groupedJsonObject;
        JsonArray jsonObjectArray = new JsonArray();
        JsonArray jsonElements = GsonUtil.toJsonArray(sites, new Function<SiteDetails, JsonElement>() {
            @Nullable
            @Override
            public JsonElement apply(SiteDetails siteDetail) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("name", siteDetail.name);
                jsonObject.addProperty("siteAddress", siteDetail.address);
                jsonObject.add("Products", getProductsAsJson(siteDetail.products));
                return jsonObject;
            }
        });

        for(JsonElement jsonObject : jsonElements){
            groupedJsonObject = new JsonArray();
            groupedJsonObject.add(jsonObject);
            jsonObjectArray.add(groupedJsonObject);
        }

        return jsonObjectArray;
    }

    private JsonElement getProductsAsJson(List<ProductDetails> products) {
        JsonArray groupedJsonObject;
        JsonArray jsonObjectArray = new JsonArray();
        JsonArray jsonElements = GsonUtil.toJsonArray(products, new Function<ProductDetails, JsonElement>() {
            @Nullable
            @Override
            public JsonElement apply(ProductDetails productDetails) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("name", productDetails.name);
                jsonObject.add("Attributes", getAttributesAsJson(productDetails.attributes));
                jsonObject.add("PriceLines", getPriceLinesAsJson(productDetails.priceLineDetails));
                jsonObject.add("CostLines", getPriceLinesAsJson(productDetails.costLineDetails));
                jsonObject.add("RelatedProducts", getRelatedProductsAsJson(productDetails.getRelatedProducts()));
                return jsonObject;
            }
        });

        for(JsonElement jsonObject : jsonElements){
            groupedJsonObject = new JsonArray();
            groupedJsonObject.add(jsonObject);
            jsonObjectArray.add(groupedJsonObject);
        }

        return jsonObjectArray;
    }

    private JsonElement getRelatedProductsAsJson(List<ProductDetails> products) {
        JsonArray groupedJsonObject;
        JsonArray jsonObjectArray = new JsonArray();
        JsonArray jsonElements = GsonUtil.toJsonArray(products, new Function<ProductDetails, JsonElement>() {
            @Nullable
            @Override
            public JsonElement apply(ProductDetails productDetails) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("name", productDetails.name);
                jsonObject.add("Attributes", getAttributesAsJson(productDetails.attributes));
                jsonObject.add("PriceLines", getPriceLinesAsJson(productDetails.priceLineDetails));
                jsonObject.add("CostLines", getPriceLinesAsJson(productDetails.costLineDetails));
                return jsonObject;
            }
        });

        for(JsonElement jsonObject : jsonElements){
            groupedJsonObject = new JsonArray();
            groupedJsonObject.add(jsonObject);
            jsonObjectArray.add(groupedJsonObject);
        }

        return jsonObjectArray;
    }

    private JsonElement getPriceLinesAsJson(List<PriceLineDetails> priceLineDetails) {
        JsonArray groupedJsonObject;
        JsonArray jsonObjectArray = new JsonArray();

        //Generate the Headings
        JsonObject headerJson = new JsonObject();
        headerJson.addProperty("name", "Charging Scheme Name");
        headerJson.addProperty("otcGross", "OTC Gross");
        headerJson.addProperty("otcDisc", "OTC Discount(%)");
        headerJson.addProperty("otcNet", "OTC Net");
        headerJson.addProperty("rcGross", "RC Gross");
        headerJson.addProperty("rcDisc", "RC Discount(%)");
        headerJson.addProperty("rcNet", "RC Net");

        JsonArray jsonElements = new JsonArray();
        jsonElements.add(headerJson);

        JsonArray priceElements = GsonUtil.toJsonArray(priceLineDetails, new Function<PriceLineDetails, JsonElement>() {
            @Nullable
            @Override
            public JsonElement apply(PriceLineDetails priceLineDetails) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("name", priceLineDetails.name);
                jsonObject.addProperty("otcGross", priceLineDetails.getNonRecurringGross());
                jsonObject.addProperty("otcDisc", priceLineDetails.getNonRecurringDiscount());
                jsonObject.addProperty("otcNet", priceLineDetails.getNonRecurringNet());
                jsonObject.addProperty("rcGross", priceLineDetails.getRecurringGross());
                jsonObject.addProperty("rcDisc", priceLineDetails.getRecurringDiscount());
                jsonObject.addProperty("rcNet", priceLineDetails.getRecurringNet());
                return jsonObject;
            }
        });

        jsonElements.addAll(priceElements);

        for(JsonElement jsonObject : jsonElements){
            groupedJsonObject = new JsonArray();
            groupedJsonObject.add(jsonObject);
            jsonObjectArray.add(groupedJsonObject);
        }

        return jsonObjectArray;
    }

    private JsonElement getAttributesAsJson(List<InstanceCharacteristic> attributes) {
        JsonArray groupedJsonObject = new JsonArray();
        JsonArray jsonObjectArray = new JsonArray();
        int i = 1;
        JsonArray jsonElements = GsonUtil.toJsonArray(attributes, new Function<InstanceCharacteristic, JsonElement>() {
            @Nullable
            @Override
            public JsonElement apply(InstanceCharacteristic input) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("name", input.getDisplayName());
                jsonObject.addProperty("value", ""+(input.getValue() == null ? "": input.getValue()));
                return jsonObject;
            }
        });

        for(JsonElement jsonObject : jsonElements){
            groupedJsonObject.add(jsonObject);

            if(i++%3 == 0){
                jsonObjectArray.add(groupedJsonObject);
                groupedJsonObject = new JsonArray();
            }
        }

        return jsonObjectArray;
    }

    public JsonObject buildSitesByProductTree() {
        LOG.info("Inside buildSitesByProductTree()");

        List<QuoteOptionItemDTO> quoteOptionItemDTOs = quoteOptionFacade.getAllQuoteOptionItem(params.get(PROJECT_ID), params.get(QUOTE_OPTION_ID));

        if(!Strings.isNullOrEmpty(params.get(ORDER_ID))){
            quoteOptionItemDTOs = filterLineItemsByOrder(quoteOptionItemDTOs, params.get(ORDER_ID));
        }
        else if(!Strings.isNullOrEmpty(params.get(OFFER_ID))){
            quoteOptionItemDTOs = filterLineItemsByOffer(quoteOptionItemDTOs, params.get(OFFER_ID));
        }

        Set<SiteDTO> allSites = new HashSet<>();
        ProductInstance instance;
        JsonObject jsonObject = null;

        try {
            //Get all the front catalogue products
            List<QuoteOptionItemDTO> frontCatalogueProducts = getFrontCatalogueProducts(quoteOptionItemDTOs);
            Map<String, ProductSiteDetails> sitesByProductMap = new HashMap();
            List<ProductSiteDetails> catalogProducts = new ArrayList<>(frontCatalogueProducts.size());
            ProductSiteDetails productSiteDetails;
            //Get all the site using front catalogue products
            List<SiteDetails> siteList;

            for(QuoteOptionItemDTO item : frontCatalogueProducts){
                instance = productInstanceClient.get(new LineItemId(item.getId()));

                //Add product to catalogProductMap
                if(sitesByProductMap.containsKey(item.getProductCode())) {
                    sitesByProductMap.get(item.getProductCode()).getSiteDetailsList().add((getSiteDetails(instance)));
                }
                else{
                    siteList = new ArrayList<>();

                    siteList.add(getSiteDetails(instance));

                    productSiteDetails = new ProductSiteDetails(item.getProductCode(), instance.getProductName(), siteList);
                    sitesByProductMap.put(item.getProductCode(), productSiteDetails);

                    catalogProducts.add(productSiteDetails);
                }
            }

            jsonObject = new JsonObject();
            jsonObject.add("products", getSitesByProductAsJson(catalogProducts));
        }
        catch(Exception e){
            e.printStackTrace();
            LOG.error("Error while building SitesByProductTree.", e);
        }

        LOG.info("In buildSitesByProductTree(). JSON created is --->" + jsonObject.toString());
        return jsonObject;
    }

    private JsonElement getSitesByProductAsJson(List<ProductSiteDetails> catalogProducts) {
        JsonArray groupedJsonObject;
        JsonArray jsonObjectArray = new JsonArray();
        JsonArray jsonElements = GsonUtil.toJsonArray(catalogProducts, new Function<ProductSiteDetails, JsonElement>() {
            @Nullable
            @Override
            public JsonElement apply(ProductSiteDetails productSiteDetail) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("name", productSiteDetail.productName);
                jsonObject.add("Sites", getSiteDetailAsJson(productSiteDetail.siteDetailsList));
                return jsonObject;
            }
        });

        for(JsonElement jsonObject : jsonElements){
            groupedJsonObject = new JsonArray();
            groupedJsonObject.add(jsonObject);
            jsonObjectArray.add(groupedJsonObject);
        }

        return jsonObjectArray;
    }

    private JsonElement getSiteDetailAsJson(List<SiteDetails> sites) {
        JsonArray groupedJsonObject;
        JsonArray jsonObjectArray = new JsonArray();
        JsonArray jsonElements = GsonUtil.toJsonArray(sites, new Function<SiteDetails, JsonElement>() {
            @Nullable
            @Override
            public JsonElement apply(SiteDetails site) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("name", site.name);
                jsonObject.addProperty("address", site.address);
                jsonObject.add("Products", getProductDetailsAsJson(site.products));
                return jsonObject;
            }
        });

        for(JsonElement jsonObject : jsonElements){
            groupedJsonObject = new JsonArray();
            groupedJsonObject.add(jsonObject);
            jsonObjectArray.add(groupedJsonObject);
        }

        return jsonObjectArray;
    }


    private JsonElement getProductDetailsAsJson(final List<ProductDetails> products) {
        JsonArray groupedJsonObject;
        JsonArray jsonObjectArray = new JsonArray();
        JsonArray jsonElements = GsonUtil.toJsonArray(products, new Function<ProductDetails, JsonElement>() {
            @Nullable
            @Override
            public JsonElement apply(ProductDetails site) {
                ProductDetails frontCatalogProduct = products.get(0);
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("name", frontCatalogProduct.name);
                jsonObject.add("Attributes", getAttributesAsJson(frontCatalogProduct.attributes));
                jsonObject.add("PriceLines", getPriceLinesAsJson(frontCatalogProduct.priceLineDetails));
                jsonObject.add("CostLines", getPriceLinesAsJson(frontCatalogProduct.costLineDetails));
                jsonObject.add("RelatedProducts", getRelatedProductsAsJson(frontCatalogProduct.getRelatedProducts()));
                return jsonObject;
            }
        });

        for(JsonElement jsonObject : jsonElements){
            groupedJsonObject = new JsonArray();
            groupedJsonObject.add(jsonObject);
            jsonObjectArray.add(groupedJsonObject);
        }

        return jsonObjectArray;
    }

    private SiteDetails getSiteDetails(ProductInstance instance) {
        SiteResource siteResource = customerResource.siteResource(params.get(CUSTOMER_ID));

        /**
         * If the site Id is null then get the central site info.
         */
        SiteDTO siteDTO = Strings.isNullOrEmpty(instance.getSiteId()) ? siteResource.getCentralSite(params.get(PROJECT_ID)) : siteResource.getSiteDetails(instance.getSiteId());

        List<ProductDetails> products = new ArrayList();
        products.add(getProductDetails(instance));

        SiteDetails siteDetails = new  SiteDetails(siteDTO.getSiteName(), siteDTO.getCountry(), products);
        return siteDetails;
    }

    class SiteDetails{
        String name;
        String address;

        List<ProductDetails> products;

        public SiteDetails(String name, String address, List<ProductDetails> products) {
            this.name = name;
            this.address = address;
            this.products = products;
        }

        public List<ProductDetails> getProducts() {
            return products;
        }
    }

    class ProductDetails{
        String name;
        List<InstanceCharacteristic> attributes;
        List<PriceLine> priceLines;
        List<PriceLineDetails> priceLineDetails = new ArrayList<>();
        List<PriceLineDetails> costLineDetails = new ArrayList<>();
        List<ProductDetails> relatedProducts = new ArrayList<>();
        Map<String, PriceLineDetails> priceLineByChargingSchemeMap = new HashMap<>();
        Map<String, PriceLineDetails> costLineByChargingSchemeMap = new HashMap<>();

        public ProductDetails(String name, List<InstanceCharacteristic> attributes, List<PriceLine> priceLines){
            this.name = name;
            this.attributes = attributes;
            this.priceLines = priceLines;
        }

        public void setPriceAndCostLineDetails() throws Exception{
            List<PriceLineDetails>  priceLineDetails = new ArrayList<>();
            List<PriceLineDetails>  costLineDetails = new ArrayList<>();

            if(priceLines != null && !priceLines.isEmpty()){
                populatePriceLineAndCostLineMaps();

                //Populate the Price Lines
                for(String chargingScheme : priceLineByChargingSchemeMap.keySet()){
                    priceLineDetails.add(priceLineByChargingSchemeMap.get(chargingScheme));
                }
                //Populate the Cost Lines
                for(String chargingScheme : costLineByChargingSchemeMap.keySet()){
                    costLineDetails.add(costLineByChargingSchemeMap.get(chargingScheme));
                }
            }
            this.priceLineDetails = priceLineDetails;
            this.costLineDetails = costLineDetails;
        }


        private void populatePriceLineAndCostLineMaps() throws Exception {
            for(PriceLine priceLine : priceLines){
                PriceLineDetails priceLineDetails = priceLine.tariffType().equals(TariffType.COST) ? costLineByChargingSchemeMap.get(priceLine.getChargingSchemeName()) :
                        priceLineByChargingSchemeMap.get(priceLine.getChargingSchemeName());

                if(priceLineDetails == null){
                    priceLineDetails = new PriceLineDetails(priceLine.getChargingSchemeName());
                    if(priceLine.tariffType().equals(TariffType.COST)) {
                        costLineByChargingSchemeMap.put(priceLine.getChargingSchemeName(), priceLineDetails);
                    }
                    else{
                        priceLineByChargingSchemeMap.put(priceLine.getChargingSchemeName(), priceLineDetails);
                    }
                }

                // Get the One-Time charges
                if(priceLine.getPriceType().equals(PriceType.ONE_TIME)){
                    priceLineDetails.setNonRecurringGross(priceLine.getChargePrice().getPrice());
                    priceLineDetails.setNonRecurringDiscount(priceLine.getChargePrice().getDiscountPercentage());
                    priceLineDetails.setNonRecurringNet(priceLine.getChargePrice().getDiscountedPrice());
                }
                // Get the recurring charges
                else if(priceLine.getPriceType().equals(PriceType.RECURRING)){
                    priceLineDetails.setRecurringGross(priceLine.getChargePrice().getPrice());
                    priceLineDetails.setRecurringDiscount(priceLine.getChargePrice().getDiscountPercentage());
                    priceLineDetails.setRecurringNet(priceLine.getChargePrice().getDiscountedPrice());
                }
            }
        }

        public List<ProductDetails> getRelatedProducts() {
            return relatedProducts;
        }

        public void setRelatedProducts(List<ProductDetails> relatedProducts) {
            this.relatedProducts = relatedProducts;
        }
    }

    class PriceLineDetails{
        private String name;
        private BigDecimal recurringGross;
        private BigDecimal recurringDiscount;
        private BigDecimal recurringNet;
        private BigDecimal nonRecurringGross;
        private BigDecimal nonRecurringDiscount;
        private BigDecimal nonRecurringNet;

        public PriceLineDetails(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setRecurringGross(BigDecimal recurringGross) {
            this.recurringGross = recurringGross;
        }

        public void setRecurringDiscount(BigDecimal recurringDiscount) {
            this.recurringDiscount = recurringDiscount;
        }

        public void setRecurringNet(BigDecimal recurringNet) {
            this.recurringNet = recurringNet;
        }

        public void setNonRecurringGross(BigDecimal nonRecurringGross) {
            this.nonRecurringGross = nonRecurringGross;
        }

        public void setNonRecurringNet(BigDecimal nonRecurringNet) {
            this.nonRecurringNet = nonRecurringNet;
        }

        public void setNonRecurringDiscount(BigDecimal nonRecurringDiscount) {
            this.nonRecurringDiscount = nonRecurringDiscount;
        }

        public BigDecimal getRecurringGross() {
            return recurringGross;
        }

        public BigDecimal getRecurringDiscount() {
            return recurringDiscount;
        }

        public BigDecimal getRecurringNet() {
            return recurringNet;
        }

        public BigDecimal getNonRecurringGross() {
            return nonRecurringGross;
        }

        public BigDecimal getNonRecurringDiscount() {
            return nonRecurringDiscount;
        }

        public BigDecimal getNonRecurringNet() {
            return nonRecurringNet;
        }
    }

    class ProductSiteDetails {
        String sCode;
        String productName;

        public List<SiteDetails> getSiteDetailsList() {
            return siteDetailsList;
        }

        List<SiteDetails> siteDetailsList;

        public ProductSiteDetails(String sCode, String productName, List<SiteDetails> siteDetailsList) {
            this.sCode = sCode;
            this.productName = productName;
            this.siteDetailsList = siteDetailsList;
        }
    }
}
