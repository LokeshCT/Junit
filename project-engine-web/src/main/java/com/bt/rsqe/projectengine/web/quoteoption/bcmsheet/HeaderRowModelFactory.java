package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;

import com.bt.rsqe.domain.bom.parameters.ProductSCode;
import com.bt.rsqe.domain.product.Attribute;
import com.bt.rsqe.domain.product.InstanceCharacteristic;
import com.bt.rsqe.domain.product.InstanceTreeScenario;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.product.parameters.SalesRelationship;
import com.bt.rsqe.domain.project.InstanceCharacteristicNotFound;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.pmr.client.PmrClient;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.poi.hssf.usermodel.HSSFCell;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.bt.rsqe.projectengine.web.quoteoption.bcmsheet.CostColumn.*;
import static com.bt.rsqe.projectengine.web.quoteoption.bcmsheet.ProductDetailsColumn.*;
import static com.bt.rsqe.utils.AssertObject.isNotNull;
import static com.google.common.collect.Lists.*;

public class HeaderRowModelFactory {

    private static final int TOTAL_COLUMN_IN_SHEET = 255;
    private static final int FOUR = 4;
    private static final String CHILD = "Child";
    private static final String LICENCE = "Licence ";
    private int defaultSheetIndex = 0;
    private int ZERO = 0;
    private PmrClient pmrClient;

    public HeaderRowModelFactory(PmrClient pmrClient) {
        this.pmrClient = pmrClient;
    }

    public HeaderRowModel createHeader(String sheetName, int costSetCount, List<ProductIdentifier> products){
        List<HeaderCell> headerCells = new ArrayList<HeaderCell>();
        Set<BCMHeaderProductModel> allBCMHeaderProductModel = getChildrenBasedOnProductOffering(products);
        createStaticHeader(headerCells, ProductSheetStaticColumn.values());
        createCostAndProductsHeader(headerCells, allBCMHeaderProductModel, sheetName, costSetCount, products);
        return new HeaderRowModel(headerCells);
    }

    private Collection<? extends HeaderCell> createCostAndProductsHeader(List<HeaderCell> headerCells, Set<BCMHeaderProductModel> allBCMHeaderProductModel, String sheetName, int costSetCount, List<ProductIdentifier> products) {
        int startIndexForProductsColumn = createCostHeader(headerCells, sheetName, costSetCount);
        createProductsHeader(headerCells, allBCMHeaderProductModel, startIndexForProductsColumn, products);
        return headerCells;
    }

    public int createProductsHeader(List<HeaderCell> headerCells, Set<BCMHeaderProductModel> allBCMHeaderProductModel, int startIndex, List<ProductIdentifier> products) {
        int sheetIndex = ZERO;
        for(BCMHeaderProductModel model : allBCMHeaderProductModel){
            boolean childRelCellVisible = true;
            if(!checkRemainingCellsInSheetAreEnoughToCreateHeaderCellsFor(model, startIndex)){
                sheetIndex++;
               startIndex = ZERO;
            }
            if(products.contains(model.productIdentifier)){
                childRelCellVisible = false;
            }
            headerCells.add(new HeaderCell(startIndex++, model.getProductName() + " "+ PRIMARY_IDENTIFIER.columnName + " "+ model.getSCode(), PRIMARY_IDENTIFIER.visible, PRIMARY_IDENTIFIER.type, PRIMARY_IDENTIFIER.retrieveValueFrom,sheetIndex, HSSFCell.CELL_TYPE_STRING));
            headerCells.add(new HeaderCell(startIndex++, model.getProductName() + " "+ VERSION_NUMBER.columnName +" "+model.getProductVersion(), VERSION_NUMBER.visible, VERSION_NUMBER.type, VERSION_NUMBER.retrieveValueFrom,sheetIndex, HSSFCell.CELL_TYPE_STRING));
            headerCells.add(new HeaderCell(startIndex++, model.getProductName() + " "+ PRODUCT_INSTANCE_ID.columnName, PRODUCT_INSTANCE_ID.visible, PRODUCT_INSTANCE_ID.type, PRODUCT_INSTANCE_ID.retrieveValueFrom,sheetIndex, HSSFCell.CELL_TYPE_STRING));
            headerCells.add(new HeaderCell(startIndex++, model.getProductName() + " "+ CHILD_RELATIONSHIP_NAME.columnName,childRelCellVisible,CHILD_RELATIONSHIP_NAME.type, CHILD_RELATIONSHIP_NAME.retrieveValueFrom,sheetIndex, HSSFCell.CELL_TYPE_STRING));
            for(Attribute attribute : model.attributes){
                headerCells.add(new HeaderCell(startIndex++, model.getProductName() + " "+ attribute.getName(), true, PRIMARY_IDENTIFIER.type, "",sheetIndex, 1));
            }
        }
        return startIndex;
    }

    public static List<InstanceCharacteristic> getRFQInstanceCharacteristics(ProductInstance productInstance) {
        List<InstanceCharacteristic> instanceCharacteristics = new ArrayList<InstanceCharacteristic>();
        for(Attribute attribute : productInstance.whatReadyForQuoteAttributesShouldIConfigureForScenario(InstanceTreeScenario.PROVIDE)){
            try {
                instanceCharacteristics.add(productInstance.getInstanceCharacteristic(attribute.getName()));
            } catch (InstanceCharacteristicNotFound instanceCharacteristicNotFound) {
                //skip the attribute...
            }
        }
        return instanceCharacteristics;
    }

    public int createCostHeader(List<HeaderCell> headerCells, String sheetName, int costSetCount) {
        int startIndex = COST_DESCRIPTION.columnIndex;
        for(int count = 0; count < costSetCount ; count++){
            headerCells.add(new HeaderCell(startIndex++, COST_DESCRIPTION.columnName, COST_DESCRIPTION.visible, COST_DESCRIPTION.type, COST_DESCRIPTION.retrieveValueFrom, defaultSheetIndex, HSSFCell.CELL_TYPE_STRING));
            headerCells.add(new HeaderCell(startIndex++, ONE_TIME_COST.columnName, ONE_TIME_COST.visible, ONE_TIME_COST.type, ONE_TIME_COST.retrieveValueFrom, defaultSheetIndex, HSSFCell.CELL_TYPE_STRING));
            headerCells.add(new HeaderCell(startIndex++, RECURRING_COST.columnName, RECURRING_COST.visible, RECURRING_COST.type, RECURRING_COST.retrieveValueFrom, defaultSheetIndex, HSSFCell.CELL_TYPE_STRING));
        }
        return startIndex;
    }

    public List<HeaderCell> createStaticHeader(List<HeaderCell> headerCells, ProductSheetStaticColumn[] staticColumns) {
        for(ProductSheetStaticColumn staticColumn : staticColumns){
            headerCells.add(new HeaderCell(staticColumn.columnIndex, staticColumn.columnName, staticColumn.visible, staticColumn.type, staticColumn.retrieveValueFrom, defaultSheetIndex, staticColumn.dataType));
        }
        return headerCells;
    }

    public Set<BCMHeaderProductModel> getChildrenBasedOnProductOffering(List<ProductIdentifier> products) {
        Set<BCMHeaderProductModel> headerProductModels = new LinkedHashSet<BCMHeaderProductModel>();
        for(ProductIdentifier productIdentifier : products){
            headerProductModels = getProductsAndItsChildrenInSequentialOrder(productIdentifier.getProductId(), headerProductModels);
        }
        return headerProductModels;
    }

    public Set<BCMHeaderProductModel> getProductsAndItsChildrenInSequentialOrder(String sCode, Set<BCMHeaderProductModel> headerProductModels) {
        ProductOffering productOffering = null;
        try {
            productOffering = pmrClient.productOffering(ProductSCode.newInstance(sCode)).get();
            if(isExist(headerProductModels, productOffering.getProductIdentifier())){
                return headerProductModels;
            }
            headerProductModels.add(new BCMHeaderProductModel(productOffering.getProductIdentifier(), getRFQAttributes(productOffering)));
            for(SalesRelationship relationship : getChildRelations(productOffering)){
                headerProductModels.addAll(getProductsAndItsChildrenInSequentialOrder(relationship.getProductIdentifier().getProductId(), headerProductModels));
            }
        } catch (Exception e) {
            //skip and go ahead...
        }
        return headerProductModels;
    }

    public List<Attribute> getRFQAttributes(ProductOffering productOffering) {
        List<Attribute> attributes = productOffering.getAttributes();
        return Lists.newArrayList(Iterables.filter(attributes, new Predicate<Attribute>() {
            @Override
            public boolean apply(@Nullable Attribute attribute) {
                return attribute.isForReadyForQuotationPhase(InstanceTreeScenario.PROVIDE);
            }
        }));
    }

    public List<SalesRelationship> getChildRelations(ProductOffering productOffering) {
        return Lists.newArrayList(Iterables.filter(productOffering.getSalesRelationships(), new Predicate<SalesRelationship>() {
            @Override
            public boolean apply(@Nullable SalesRelationship input) {
                return input.getType().name().equals(CHILD);
            }
        }));
    }

    private boolean checkRemainingCellsInSheetAreEnoughToCreateHeaderCellsFor(BCMHeaderProductModel model, int index) {
        int requiredCells = requiredCellsForModel(model);
        if(index+requiredCells < TOTAL_COLUMN_IN_SHEET){
            return true;
        }
        return false;
    }

    private int requiredCellsForModel(BCMHeaderProductModel model) {
        return model.attributes.size() + FOUR;
    }

    private boolean isExist(Set<BCMHeaderProductModel> headerProductModels, ProductIdentifier productIdentifier){
        Iterator<BCMHeaderProductModel> modelIterator = headerProductModels.iterator();
        while (modelIterator.hasNext()){
            if(modelIterator.next().productIdentifier.getProductId().equals(productIdentifier.getProductId())){
                return true;
            }
        }
        return false;
    }

    public HeaderRowModel createBidInfoHeader(){
        List<HeaderCell> headerCells = newArrayList();
        for(BidInfoStaticColumn staticColumn : BidInfoStaticColumn.values()){
            headerCells.add(new HeaderCell(staticColumn.columnIndex, staticColumn.columnName,staticColumn.visibility,
                                           staticColumn.retrieveValueFrom,
                                           staticColumn.dataType,staticColumn.isReadOnly));
        }
        return new HeaderRowModel(headerCells);
    }

    public HeaderRowModel createProductPerSiteHeader(){
        List<HeaderCell> headerCells = newArrayList();
        int columnIndex=0;
        //This is used to Fetch the SITE Headers
        for(ProductPerSiteStaticColumn staticColumn : ProductPerSiteStaticColumn.values()){
            headerCells.add(new HeaderCell(staticColumn.columnIndex, staticColumn.columnName,staticColumn.visibility,
                                           staticColumn.retrieveValueFrom,
                                           staticColumn.dataType,staticColumn.isReadOnly));

            columnIndex++;
        }

        //This is used to Fetch the ROOT Products Categories
        for(RootProductsCategory dynaColumns : RootProductsCategory.values()){
            headerCells.add(new HeaderCell(columnIndex++, dynaColumns.columnName,dynaColumns.visibility,
                                           dynaColumns.retrieveValueFrom,
                                           dynaColumns.dataType,dynaColumns.isReadOnly));
        }
        return new HeaderRowModel(headerCells);
    }

    public HeaderRowModel createSpecialBidInfoSheetHeader() {
        List<HeaderCell> headerCells = newArrayList();
        for(SpecialBidInfoStaticColumn column : SpecialBidInfoStaticColumn.values()){
            headerCells.add(new HeaderCell(column.columnIndex, column.columnName,column.visibility,
                                           column.retrieveValueFrom,
                                           column.dataType,true));
        }
        return new HeaderRowModel(headerCells);
    }

    public HeaderRowModel createSiteBasedRootProductSheetHeader(String sheetName) {
        List<HeaderCell> headerCells = newArrayList();
        int columnIndex =0;
        //Arrange columns using column group names
        for(SiteProductSheetStaticColumn column : SiteProductSheetStaticColumn.values()){
            if(column.columnGroup.equalsIgnoreCase("PriceColumn")) {
                headerCells.add(new HeaderCell(columnIndex++, column.columnName,column.visibility,
                                               column.retrieveValueFrom,
                                               column.dataType,true));
                //Attributes Section
                if(column.name().equalsIgnoreCase(SiteProductSheetStaticColumn.ORDER_TYPE.name()) &&
                   isNotNull(AttributesMapper.getAttributeMapper(sheetName))){
                    for(String mapKey: AttributesMapper.getAttributeMapper(sheetName).attributesMap().keySet()){
                        headerCells.add(new HeaderCell(columnIndex++,mapKey,true,mapKey,1,true));
                    }
                }
            }
        }
        //License columns
        for(int i=1;i<=5;i++){
            for(PriceColumns priceColumn : PriceColumns.values()){
                if(priceColumn.columnGroup.equalsIgnoreCase("LicensePriceColumn")){
                    String columnName=  LICENCE +i+" "+priceColumn.columnName;
                    String retrieveValue = LICENCE +i+" "+priceColumn.retrieveValueFrom;
                        headerCells.add(new HeaderCell(columnIndex++, columnName,true,
                                                       retrieveValue,
                                                       priceColumn.dataType,true));
                }
            }
        }
        //Cost columns
        for(SiteProductSheetStaticColumn licenseColumn : SiteProductSheetStaticColumn.values()){
            if(licenseColumn.columnGroup.equalsIgnoreCase("CostColumn")) {
                headerCells.add(new HeaderCell(columnIndex++, licenseColumn.columnName,licenseColumn.visibility,
                                               licenseColumn.retrieveValueFrom,
                                               licenseColumn.dataType,true));
            }
        }
        //License Cost Columns
        for(int i=1;i<=5;i++){
            for(PriceColumns priceColumn : PriceColumns.values()){
                if(priceColumn.columnGroup.equalsIgnoreCase("LicenseCostColumn")){
                    String columnName=  LICENCE +i+" "+priceColumn.columnName;
                    String retrieveValue = LICENCE +i+" "+priceColumn.retrieveValueFrom;
                        headerCells.add(new HeaderCell(columnIndex++, columnName,true,
                                                       retrieveValue,
                                                       priceColumn.dataType,true));
                    }
            }
        }

        return new HeaderRowModel(headerCells);
    }


    public HeaderRowModel createServiceBasedRootProductSheetHeader(String sheetName) {
        List<HeaderCell> headerCells = newArrayList();
        int columnIndex =0;
        for(ServiceProductSheetStaticColumn column : ServiceProductSheetStaticColumn.values()) {
            headerCells.add(new HeaderCell(columnIndex++, column.columnName,column.visibility,
                                           column.retrieveValueFrom,
                                           column.dataType,true));
            //Attributes Section
            if(column.name().equalsIgnoreCase(ServiceProductSheetStaticColumn.SERVICE_TYPE.name()) &&
               isNotNull(AttributesMapper.getAttributeMapper(sheetName))){
                for(String mapKey: AttributesMapper.getAttributeMapper(sheetName).attributesMap().keySet()){
                    headerCells.add(new HeaderCell(columnIndex++,mapKey,true,mapKey,1,true));
                }
            }
        }
        return new HeaderRowModel(headerCells);
    }

    public HeaderRowModel createSiteManagementSheetHeader(String sheetName) {
        List<HeaderCell> headerCells = newArrayList();
        int columnIndex =0;
        for(SiteManagementStaticColumns column : SiteManagementStaticColumns.values()){
            headerCells.add(new HeaderCell(columnIndex++, column.columnName,column.visibility,
                                           column.retrieveValueFrom,
                                           column.dataType,true));
            //Attributes Section
            if(column.name().equalsIgnoreCase(SiteManagementStaticColumns.PRODUCT_INSTANCE_VERSION.name()) &&
               isNotNull(AttributesMapper.getAttributeMapper(sheetName))){
                for(String mapKey: AttributesMapper.getAttributeMapper(sheetName).attributesMap().keySet()){
                    headerCells.add(new HeaderCell(columnIndex++,mapKey,true,mapKey,1,true));
                }
            }
        }
        return new HeaderRowModel(headerCells);
    }

}

