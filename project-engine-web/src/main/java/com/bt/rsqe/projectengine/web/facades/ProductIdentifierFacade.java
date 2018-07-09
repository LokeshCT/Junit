package com.bt.rsqe.projectengine.web.facades;

import com.bt.rsqe.client.Pmr;
import com.bt.rsqe.customerinventory.client.resource.ProductAgreementResourceClient;
import com.bt.rsqe.domain.StencilId;
import com.bt.rsqe.domain.bom.parameters.ProductSCode;
import com.bt.rsqe.domain.product.Attribute;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.SellableProduct;
import com.bt.rsqe.domain.product.chargingscheme.Discount;
import com.bt.rsqe.domain.product.chargingscheme.ProductChargingScheme;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.dto.LookUpConstants;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.bt.rsqe.pmr.api.ProductNotFoundException;
import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.pmr.client.PmrLookupClient;
import com.bt.rsqe.pmr.dto.SalesChannelDTO;
import com.bt.rsqe.projectengine.web.view.Products;
import com.google.common.base.Optional;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ProductIdentifierFacade {
    private Logger logger = LogFactory.createDefaultLogger(Logger.class);
    private PmrClient pmrClient;
    private ProductAgreementResourceClient productAgreementResourceClient;
    private PmrLookupClient pmrLookupClient;

    private static final String YES = "Y";
    private static final String INPUT_COLUMN_NAME = "Customer ID";
    private static final String OUTPUT_COLUMN_NAME = "Category ID";
    private static final String CUSTOMER_BASED_PRODUCT_CATEGORY_LOOKUP_MATRIX_RULE_ID = "R0302681";


    public ProductIdentifierFacade(PmrClient pmrClient, ProductAgreementResourceClient productAgreementResourceClient, PmrLookupClient pmrLookupClient) {
        this.pmrClient = pmrClient;
        this.productAgreementResourceClient = productAgreementResourceClient;
        this.pmrLookupClient = pmrLookupClient;
    }

    public Products getSellableProductsForSalesChannel(String salesChannel) {
        Products products;

        SalesChannelDTO salesChannelDTO = getSalesChannelDto(salesChannel);
        String productCategoryListString = "(" + StringUtils.join(salesChannelDTO.getProductCategories(), ", ") + ")";
        logger.salesChannelDtoDetails(salesChannelDTO.getSalesChannelName(), salesChannelDTO.getId(), salesChannelDTO.getIsExcluded().toString(), productCategoryListString);

        if (salesChannelDTO.isExcluded) {
            products = excludeProducts(salesChannelDTO.getProductCategories());
        } else {
            products = includeProducts(salesChannelDTO.getProductCategories());
        }

            return products;
    }

    public Products getSellableProductsForCustomerBased(Products products,String customerId) {

            //check Customer ID exists in ProductCategoryLookupMatrix
            List<String> customerBasedProductCategoryAvailableList = getCustomerBasedProductCategoryListFromLookup(customerId);

            if (customerBasedProductCategoryAvailableList.size() > 0) {
                //filter product categories from customer based product matrix
                Products filterProductList = filterCustomerBasedProductCategorybasedProducts(products, customerBasedProductCategoryAvailableList);
                if(!filterProductList.sellableProducts().isEmpty()){
                    products = filterProductList;
                }
            }else{
                logger.customerBasedLookupNotAvailable(customerId);
            }

        return products;
    }

    private Products filterCustomerBasedProductCategorybasedProducts(Products sellableProductsForSalesChannelList, List<String> customerBasedProductCategoryAvailableList){
        List<SellableProduct> productList = new ArrayList<SellableProduct>();
        if (customerBasedProductCategoryAvailableList == null) {
            productList = sellableProductsForSalesChannelList.sellableProducts();
        } else {
            for (SellableProduct product : sellableProductsForSalesChannelList.sellableProducts()) {
                String productId = product.getProductId();
                ProductIdentifier hCode = product.getProductCategory();
                if (null == hCode) {
                    logger.productCategoryNotFound(productId);
                } else {
                    String productHCode = hCode.getProductId();
                    for (String productCategoryCode : customerBasedProductCategoryAvailableList) {
                        if (productCategoryCode.equals(productHCode)) {
                            productList.add(product);
                            logger.productSuccessfullyLoaded(productId, productHCode);
                        }
                    }
                }
            }
        }

        Products products = new Products(productList);
        return products;
    }

    public boolean isComplexContractCustomer(Long contractId) {
        return YES.equalsIgnoreCase(productAgreementResourceClient.getContractManagedSolutionFlag(contractId.toString()));
    }

    public List<String> getCustomerBasedProductCategoryListFromLookup(final String customerId) {
        HashMap lookUpInput = new HashMap<String, String>() {{
            put(LookUpConstants.RULESET_ID, CUSTOMER_BASED_PRODUCT_CATEGORY_LOOKUP_MATRIX_RULE_ID);
            put(INPUT_COLUMN_NAME, customerId);
        }};

        try {
            //Customer based Product Category Lookup
            List<String> lookupResult = pmrLookupClient.lookupRuleSet(OUTPUT_COLUMN_NAME, lookUpInput).getFirstColumnValues();

            return lookupResult;

        }catch (Exception e){
            throw new RuntimeException("Rule Set " + CUSTOMER_BASED_PRODUCT_CATEGORY_LOOKUP_MATRIX_RULE_ID +" not exits");
        }
    }

    public Products getAllSellableProducts() {
        return new Products(pmrClient.getSalesCatalogue().getAllSellableProducts());
    }

    public SalesChannelDTO getSalesChannelDto(String salesChannelName) {
        SalesChannelDTO salesChannelDTO = pmrClient.getSalesChannelDto(salesChannelName);
        return salesChannelDTO;
    }

    public String getProductName(ProductOffering productOffering) {
        return productOffering.getProductIdentifier().getProductName();
    }

    public String getProductName(String productCode) {
        try {
            return getProductName(pmrClient.productOffering(ProductSCode.newInstance(productCode)).get());
        } catch (ProductNotFoundException e) {
            return "Unknown Product";
        }
    }

    public String getDisplayName(ProductOffering productOffering) {
        String displayName = null != productOffering.getDisplayName() ? productOffering.getDisplayName() : productOffering.getProductIdentifier().getDisplayName();

        if(displayName != null) {
            return displayName;
        } else {
            return productOffering.getProductIdentifier().getProductName();
        }
    }

    public String getDisplayName(String productCode) {
        try {
            return getDisplayName(pmrClient.productOffering(ProductSCode.newInstance(productCode)).get());
        } catch (ProductNotFoundException e) {
            return "Unknown Product";
        }
    }

    public Optional<ProductIdentifier> getProductHCode(String sCode) {
        return pmrClient.getProductHCode(sCode);
    }

    public List<ProductChargingScheme> getChargingSchemes(String productCode, String stencilId) {
        try {
            final Pmr.ProductOfferings productOfferings = pmrClient.productOffering(ProductSCode.newInstance(productCode));
            if(stencilId != null) {
                productOfferings.withStencil(StencilId.latestVersionFor(stencilId));
            }
            ProductOffering productOffering = productOfferings.get();
            return productOffering.getProductChargingSchemes();
        } catch (ProductNotFoundException e) {
            return Collections.emptyList();
        }
    }

    private Products includeProducts(List<String> productCategoryCodeList) {
        List<SellableProduct> productList = new ArrayList<SellableProduct>();
        if (productCategoryCodeList == null) {
            productList = getAllSellableProducts().sellableProducts();
        } else {
            for (SellableProduct product : getAllSellableProducts().sellableProducts()) {
                String productId = product.getProductId();
                ProductIdentifier hCode = product.getProductCategory();
                if (null == hCode) {
                    logger.productCategoryNotFound(productId);
                } else {
                    String productHCode = hCode.getProductId();
                    for (String productCategoryCode : productCategoryCodeList) {
                        if (productCategoryCode.equals(productHCode)) {
                            productList.add(product);
                            logger.productSuccessfullyLoaded(productId, productHCode);
                        }
                    }
                }
            }
        }
        Products products = new Products(productList);
        return products;
    }

    private Products excludeProducts(List<String> productCategoryCodeList) {
        List<SellableProduct> productList = getAllSellableProducts().sellableProducts();
        Iterator<SellableProduct> iterator = productList.iterator();
        Products products = null;
        if (productCategoryCodeList == null) {
            products = new Products(productList);
        } else {
            while (iterator.hasNext()) {
                SellableProduct product = iterator.next();
                String productHCode = product.getProductCategory().getProductId();
                for (String productCategoryCode : productCategoryCodeList) {
                    if (productCategoryCode.equals(productHCode)) {
                        if (productList.contains(product)) {
                            iterator.remove();
                        }
                    }
                }
                products = new Products(productList);
            }
        }
        return products;
    }

    public boolean isProductSpecialBid(String sCode){
        try {
            ProductOffering productOffering = pmrClient.productOffering(ProductSCode.newInstance(sCode)).get();
            for (Attribute attribute : productOffering.getAttributes()) {
                if(attribute.getName().toString().equalsIgnoreCase(ProductOffering.SPECIAL_BID_ATTRIBUTE_INDICATOR)) {
                    return attribute.getDefaultValue().getValue().toString().equalsIgnoreCase("Yes");
                }
            }
            return false;
        } catch (Exception e) {
            // This is fine as sometimes no product will be selected
            return false;
        }
    }

    public Set<Discount> getChargingSchemesForDiscount(String chargingSchemeName) {
        Set<Discount> prodChargingSet = null;
        try {
            prodChargingSet = pmrClient.getChargingSchemesForDiscount(chargingSchemeName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return prodChargingSet;
    }
    public static interface Logger {
        @Log(level = LogLevel.INFO, format = "No Product Category found for sCode: %s")
        void productCategoryNotFound(String productId);

        @Log(level = LogLevel.INFO, format = "Product with sCode: %s and category code: %s successfully added to drop down list")
        void productSuccessfullyLoaded(String productId, String hCode);

        @Log(level = LogLevel.INFO, format = "Returned SalesChannelDTO with name: %s\nid: %s\nisExluded: %s\nProduct Category List: %s")
        void salesChannelDtoDetails(String salesChannelName, String id, String isExcluded, String productCategoryList);

        @Log(level = LogLevel.INFO, format = "Product Category List not available for Customer : %s")
        void customerBasedLookupNotAvailable(String customerId);

    }
}
