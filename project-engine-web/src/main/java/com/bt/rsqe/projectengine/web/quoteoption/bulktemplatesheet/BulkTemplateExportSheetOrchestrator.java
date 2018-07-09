package com.bt.rsqe.projectengine.web.quoteoption.bulktemplatesheet;

import com.bt.rsqe.client.Pmr;
import com.bt.rsqe.domain.bom.parameters.ProductSCode;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.parameters.RelationshipType;
import com.bt.rsqe.domain.product.parameters.SalesRelationship;
import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet.ExcelWorkbook;
import com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet.ExportExcelMarshaller;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.List;
import java.util.Map;

import static com.bt.rsqe.utils.AssertObject.*;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Maps.*;


public class BulkTemplateExportSheetOrchestrator {

    private PmrClient pmrClient;
    private final BulkTemplateDetailSheetModelBuilder bulkTemplateDetailSheetModelBuilder;
    private Map<String,BulkTemplateProductModel> productModelMap;


    public BulkTemplateExportSheetOrchestrator(PmrClient pmrClient) {
        this.pmrClient = pmrClient;
        this.bulkTemplateDetailSheetModelBuilder = new BulkTemplateDetailSheetModelBuilder(pmrClient);
    }

    public ExcelWorkbook buildBulkTemplateExportSheet(String sCode, String quoteOptionId) {
        productModelMap = newLinkedHashMap();
        String productName = getRootProductName(sCode);
        List<BulkTemplateProductModel> productModels = getProductCodeList(sCode);
        final BulkTemplateControlSheetModel bulkTemplateControlSheetModel = new BulkTemplateControlSheetModel(productModels);

        final Map<String, BulkTemplateDetailSheetModel> bulkTemplateDetailSheetModelMap = bulkTemplateDetailSheetModelBuilder.build(productModels);
        final XSSFWorkbook xssfWorkbook = new ExportExcelMarshaller(bulkTemplateControlSheetModel,bulkTemplateDetailSheetModelMap.values()).marshall();

        return new ExcelWorkbook(xssfWorkbook, productName+ ".xlsx");
    }

    private String getRootProductName(String sCode) {
        Pmr.ProductOfferings offerings = pmrClient.productOffering(ProductSCode.newInstance(sCode));
        ProductOffering productOffering = offerings.get();
        return productOffering.getProductIdentifier().getProductName();
    }

    private List<BulkTemplateProductModel> getProductCodeList(String sCode) {
        String productId;
        String productName;
        Pmr.ProductOfferings offerings = pmrClient.productOffering(ProductSCode.newInstance(sCode));
        if (isNotNull(offerings)){
            ProductOffering productOffering = offerings.get();
            productId = productOffering.getProductIdentifier().getProductId();
            productName = productOffering.getProductIdentifier().getProductName();
            fetchSalesRelationshipsBySCode(productId, productName,RelationshipType.NONE.value());
        }

        return newLinkedList(productModelMap.values());
    }

    private void fetchSalesRelationshipsBySCode(String sCode, String productName,String relationshipType) {
        Pmr.ProductOfferings offerings = pmrClient.productOffering(ProductSCode.newInstance(sCode));
        if(isNotNull(offerings)){
            List<SalesRelationship> relatedSalesRelationships = fetchSalesRelationShips(offerings.get().getSalesRelationships(), RelationshipType.RelatedTo);
            addRelatedProductCodes(relatedSalesRelationships);

            productModelMap.put(sCode,new BulkTemplateProductModel(sCode,productName,relationshipType));

            List<SalesRelationship> childSalesRelationships = fetchSalesRelationShips(offerings.get().getSalesRelationships(), RelationshipType.Child);
            addChildProductCodes(childSalesRelationships);
        }
    }

    private List<SalesRelationship> fetchSalesRelationShips(List<SalesRelationship> salesRelationships,final RelationshipType relationshipType) {
        return newLinkedList(Iterables.filter(salesRelationships, new Predicate<SalesRelationship>() {
            @Override
            public boolean apply(SalesRelationship input) {
                return relationshipType.equals(input.getType());
            }
        }));

    }

    private void addRelatedProductCodes(List<SalesRelationship> salesRelationships) {
         for(SalesRelationship salesRelationship : salesRelationships){
             String productId = salesRelationship.getProductIdentifier().getProductId();
             String productName = salesRelationship.getProductIdentifier().getDisplayName();
             Integer minCardinality = salesRelationship.getMinimum();
             Integer maxCardinality = salesRelationship.getMaximum();
             boolean isOneToOne = (maxCardinality == 1 && minCardinality.equals(maxCardinality));
             if (!isABearerProduct(productId) && isNonOrderableRelatedProduct(productId) && isOneToOne){
                 fetchSalesRelationshipsBySCode(productId, productName,RelationshipType.RelatedTo.value());
             }
         }
    }

    private void addChildProductCodes(List<SalesRelationship> salesRelationships) {
        for(SalesRelationship salesRelationship : salesRelationships){
            if(!isABearerProduct(salesRelationship.getProductIdentifier().getProductId())){
                fetchSalesRelationshipsBySCode(salesRelationship.getProductIdentifier().getProductId(),
                                               salesRelationship.getProductIdentifier().getDisplayName(),RelationshipType.Child.value());
            }
        }
    }

    private boolean isABearerProduct(String productId){
        Pmr.ProductOfferings offerings = pmrClient.productOffering(ProductSCode.newInstance(productId));
        return isNotNull(offerings) && offerings.get().isBearer();
    }

    private boolean isNonOrderableRelatedProduct(String productId) {
        Pmr.ProductOfferings offerings = pmrClient.productOffering(ProductSCode.newInstance(productId));
        return isNotNull(offerings) && !offerings.get().isInFrontCatalogue();
    }

}


