package com.bt.rsqe.projectengine.web.userImport;

import com.bt.rsqe.customerrecord.CustomerDTO;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.customerrecord.SiteResource;
import com.bt.rsqe.domain.product.Attribute;
import com.bt.rsqe.domain.product.AttributeGroup;
import com.bt.rsqe.domain.product.InstanceCharacteristic;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.product.parameters.ResolvesTo;
import com.bt.rsqe.domain.product.parameters.SalesRelationship;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Maps;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.bt.rsqe.utils.AssertObject.*;
import static com.google.common.collect.Lists.*;
import static org.jadira.usertype.spi.utils.lang.StringUtils.*;

//todo : tests to be covered
public class InstanceToModelParser implements ProductModelAdapter {

    private ProductInstance productInstance;
    private XSSFWorkbook workbook;
    private QuoteOptionDTO quoteOptionDTO;
    private CustomerDTO customerDTO;
    private ListValidationBuilder listValidationBuilder;
    private SiteResource siteResource;
    private UserImportExcelStyler styler;
    private Map<ProductIdentifier, ProductOffering> exportableProducts;
    private List<ProductSheetDataExtractor> abstractProductSheetDataExtractor;

    private static AttributeGroup DEFAULT_ATTR_GROUP = new AttributeGroup("Base Configuration", 1);
    private static final Predicate<List<SalesRelationship>> RELATIONSHIP_PREDICATE = new Predicate<List<SalesRelationship>>() {
        @Override
        public boolean apply(List<SalesRelationship> input) {
            return !input.get(0).isTargetAFeatureSpecification() || (input.get(0).isTargetAFeatureSpecification() && input.get(0).getResolvesToValue() == ResolvesTo.NewOnly);
        }
    };

    private static final Predicate<List<SalesRelationship>> BEARER_PREDICATE = new Predicate<List<SalesRelationship>>() {
        @Override
        public boolean apply(List<SalesRelationship> input) {
            return !input.get(0).getRelationshipName().value().equals("Access");
        }
    };

    public InstanceToModelParser(
        ProductInstance productInstance,
        XSSFWorkbook workbook,
        QuoteOptionDTO quoteOptionDTO,
        CustomerDTO customerDTO,
        ListValidationBuilder listValidationBuilder,
        SiteResource siteResource,
        UserImportExcelStyler styler, Map<ProductIdentifier, ProductOffering> exportableProducts) {
        this.productInstance = productInstance;
        this.workbook = workbook;
        this.quoteOptionDTO = quoteOptionDTO;
        this.customerDTO = customerDTO;
        this.listValidationBuilder = listValidationBuilder;
        this.siteResource = siteResource;
        this.styler = styler;
        this.exportableProducts = exportableProducts;
        abstractProductSheetDataExtractor = newArrayList();
    }

    public List<? extends ProductSheetDataExtractor> build() {

        createHeaderSheet();
        createRootProductSheet();
        createNonRootExportableProductSheets();
        return abstractProductSheetDataExtractor;
    }

    private void createRootProductSheet() {
        createProductSheet(this.productInstance);
    }

    private void createHeaderSheet() {

        HeaderSheetExtractor quoteDetails = new HeaderSheetExtractor(workbook, listValidationBuilder, styler);
        quoteDetails.setContractTerm(productInstance.getContractTerm());
        quoteDetails.setCurrency(quoteOptionDTO.getCurrency());
        quoteDetails.setCustomerName(customerDTO.getName());
        quoteDetails.setTemplateVersion(productInstance.getProductIdentifier().getVersionNumber());
        quoteDetails.setQuoteId(productInstance.getQuoteOptionId());
        quoteDetails.setQuoteName(quoteOptionDTO.getName());
        quoteDetails.setQuoteStatus(productInstance.getAssetVersionStatus().name());
        quoteDetails.setContractId(productInstance.getContractId());
        abstractProductSheetDataExtractor.add(quoteDetails);
    }

    private Map<AttributeGroup, List<InstanceCharacteristic>> getProductCharacteristics(ProductInstance productInstance) {

        Map<AttributeGroup, List<InstanceCharacteristic>> charMap = new TreeMap<AttributeGroup, List<InstanceCharacteristic>>();
        AttributeGroup key = null;

        List<InstanceCharacteristic> instanceCharacteristics = productInstance.getInstanceCharacteristics();
        for (InstanceCharacteristic instanceCharacteristic : instanceCharacteristics) {
            if (instanceCharacteristic.isVisible() || instanceCharacteristic.getName().equals("STENCIL")) {
                if (productInstance.isStencilable() && instanceCharacteristic.getName().equals("STENCIL")) {
                    key = new AttributeGroup(instanceCharacteristic.getDisplayName(), 0);
                } else if (instanceCharacteristic.getSpecifiedBy().isAttributeGroupingRequired()) {
                    key = instanceCharacteristic.getSpecifiedBy().getAttributeGroupInfo();
                } else {
                    key = DEFAULT_ATTR_GROUP;
                }

                if (charMap.containsKey(key)) {
                    charMap.get(key).add(instanceCharacteristic);
                } else {
                    charMap.put(key, newArrayList(instanceCharacteristic));
                }
            }

        }
        return charMap;
    }

    private Map<AttributeGroup, List<Attribute>> getAttributesMap(ProductOffering productOffering) {

        Map<AttributeGroup, List<Attribute>> charMap = new TreeMap<AttributeGroup, List<Attribute>>();
        AttributeGroup key = null;

        List<Attribute> attributes = productOffering.getAttributes();
        for (Attribute attribute : attributes) {
            if (productOffering.isStencilable() && attribute.getName().getName().equals("STENCIL")) {
                key = new AttributeGroup(attribute.getDisplayName(), 0);
            } else if (isNotEmpty(attribute.getAttributeGroupInfo().getGroupName())) {
                key = attribute.getAttributeGroupInfo();
            } else {
                key = DEFAULT_ATTR_GROUP;
            }

            if (charMap.containsKey(key)) {
                charMap.get(key).add(attribute);
            } else {
                charMap.put(key, newArrayList(attribute));
            }

        }
        return charMap;
    }

    private void createProductSheet(ProductInstance productInstance) {

        ProductSheetExtractor productSheet = new ProductSheetExtractor(workbook, listValidationBuilder, styler);
        productSheet.fromOffering(false);
        productSheet.setProductInstance(productInstance);
        productSheet.setSheetName(productInstance.getProductIdentifier().getProductName());

        populateSiteDetails(productSheet);
        productSheet.setCharacteristicMap(getProductCharacteristics(productInstance));
        productSheet.setGroupRelationships(productInstance.getProductOffering().getSalesGroupRelationships());
        populateRelationshipDetails(productSheet, productInstance.getProductOffering());

        abstractProductSheetDataExtractor.add(productSheet);
    }

    private void createProductSheet(ProductOffering productOffering) {

        ProductSheetExtractor productSheet = new ProductSheetExtractor(workbook, listValidationBuilder, styler);
        productSheet.fromOffering(true);
        productSheet.setSheetName(productOffering.getProductIdentifier().getProductName());

        populateSiteDetails(productSheet);
        productSheet.setAttributesMap(getAttributesMap(productOffering));
        populateRelationshipDetails(productSheet, productOffering);

        abstractProductSheetDataExtractor.add(productSheet);
    }

    private void populateSiteDetails(ProductSheetExtractor productSheet) {

        SiteDTO siteDTO = getSiteDetails();
        productSheet.setCity(siteDTO.getCity());
        productSheet.setCountry(siteDTO.getCountry());
        productSheet.setSiteId(siteDTO.getSiteId().toString());
        productSheet.setSiteName(siteDTO.getSiteName());
        productSheet.setServiceInstance(productInstance.getAssetUniqueId().getValue());
    }

    private void createNonRootExportableProductSheets() {

        for (Map.Entry<ProductIdentifier, ProductOffering> entry : exportableProducts.entrySet()) {
            if (checkExportableProductApplicableForInstance()) {
                ProductOffering productOffering = entry.getValue();
                Optional<ProductInstance> exportableInstance = productInstance.getRelatedProductInstanceByScode(entry.getKey().getProductId());

                if (exportableInstance.isPresent()) {
                    createProductSheet(exportableInstance.get());
                } else {
                    createProductSheet(productOffering);
                }

            }
        }
    }

    private void populateRelationshipDetails(ProductSheetExtractor productSheet, ProductOffering productOffering) {

        productSheet.setGroupRelationships(productOffering.getSalesGroupRelationships());
        Map<ProductIdentifier, List<SalesRelationship>> nonGroupedSalesRelationships = productOffering.getNonGroupedSalesRelationships();
        nonGroupedSalesRelationships = Maps.filterValues(nonGroupedSalesRelationships, Predicates.and(RELATIONSHIP_PREDICATE, BEARER_PREDICATE));
        nonGroupedSalesRelationships = filterRelationshipsWithExportableProducts(nonGroupedSalesRelationships);

        Map<ProductIdentifier, List<SalesRelationship>> nonStenciledRelationships = extractNonStenciledRelationships(nonGroupedSalesRelationships);
        productSheet.setNonGroupRelationships(nonStenciledRelationships);

        // find stenciled relationship exists
        Map<ProductIdentifier, List<SalesRelationship>> stenciledRelationshipMap = extractStenciledRelationships(nonGroupedSalesRelationships);
        productSheet.setStenciledRelationshipMap(stenciledRelationshipMap);
    }

    private boolean checkExportableProductApplicableForInstance() {
        for (SalesRelationship salesRelationship : productInstance.getProductOffering().getSalesRelationships()) {
            if (exportableProducts.containsKey(salesRelationship.getProductIdentifier())) {
                return true;
            }
        }

        return false;
    }

    private Map<ProductIdentifier, List<SalesRelationship>> extractStenciledRelationships(final Map<ProductIdentifier, List<SalesRelationship>> nonGroupedSalesRelationships) {

        final Map<ProductIdentifier, List<SalesRelationship>> salesRelationshipMap = Maps.filterKeys(nonGroupedSalesRelationships, new Predicate<ProductIdentifier>() {
            @Override
            public boolean apply(ProductIdentifier input) {
                List<SalesRelationship> salesRelationships = nonGroupedSalesRelationships.get(input);
                if (isNotNull(salesRelationships.get(0).getRelatedProductIdentifier().getStencilId().getCCode().getValue())) {
                    return true;
                }
                return false;
            }
        });

        return salesRelationshipMap;
    }

    private Map<ProductIdentifier, List<SalesRelationship>> extractNonStenciledRelationships(final Map<ProductIdentifier, List<SalesRelationship>> nonGroupedSalesRelationships) {

        Map<ProductIdentifier, List<SalesRelationship>> salesRelationshipMap = Maps.filterKeys(nonGroupedSalesRelationships, new Predicate<ProductIdentifier>() {
            @Override
            public boolean apply(ProductIdentifier input) {
                List<SalesRelationship> salesRelationships = nonGroupedSalesRelationships.get(input);
                if (isNotNull(salesRelationships.get(0).getRelatedProductIdentifier().getStencilId().getCCode().getValue())) {
                    return false;
                }
                return true;
            }
        });

        return salesRelationshipMap;
    }

    private Map<ProductIdentifier, List<SalesRelationship>> filterRelationshipsWithExportableProducts(Map<ProductIdentifier, List<SalesRelationship>> salesRelationships) {

        Map<ProductIdentifier, List<SalesRelationship>> productIdentifierListMap = Maps.filterKeys(salesRelationships, new Predicate<ProductIdentifier>() {
            @Override
            public boolean apply(ProductIdentifier input) {
                return !exportableProducts.containsKey(input);
            }
        });

        return productIdentifierListMap;
    }


    private SiteDTO getSiteDetails() {

        if (productInstance.isSiteInstallable()) {
            return siteResource.get(productInstance.getSiteId(), productInstance.getProjectId());
        }

        return siteResource.getCentralSite(productInstance.getProjectId());
    }
}
