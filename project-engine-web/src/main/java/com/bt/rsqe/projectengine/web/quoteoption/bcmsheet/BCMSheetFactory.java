package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;


import com.bt.rsqe.domain.bom.parameters.ProductSCode;
import com.bt.rsqe.domain.product.InstanceCharacteristic;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.project.InstanceCharacteristicNotFound;
import com.bt.rsqe.domain.project.PricingStatus;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.pricing.PricingClient;
import com.bt.rsqe.pricing.config.dto.BillingTariffRulesetConfig;
import com.bt.rsqe.pricing.config.dto.CPERuleConfig;
import com.bt.rsqe.pricing.config.dto.ChargingSchemeConfig;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model.AbstractPricingSheetProductModel;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model.PricingSheetDataModel;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bt.rsqe.projectengine.web.quoteoption.bcmsheet.BCMProductSheetProperty.*;
import static com.bt.rsqe.projectengine.web.quoteoption.bcmsheet.PriceColumns.*;
import static com.bt.rsqe.utils.AssertObject.*;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Maps.*;

public class BCMSheetFactory {

    private static final String LICENCE = "Licence ";
    private HeaderRowModelFactory headerRowModelFactory;
    private BCMDataRowModelFactory bcmDataRowModelFactory;
    private PmrClient pmrClient;
    private BCMSheetGenerator bcmSheetGenerator;
    private PricingClient pricingClient;

    public BCMSheetFactory(HeaderRowModelFactory headerRowModelFactory, BCMDataRowModelFactory bcmDataRowModelFactory,
                           PmrClient pmrClient, BCMSheetGenerator bcmSheetGenerator, PricingClient pricingClient) {
        this.headerRowModelFactory = headerRowModelFactory;
        this.bcmDataRowModelFactory = bcmDataRowModelFactory;
        this.pmrClient = pmrClient;
        this.bcmSheetGenerator = bcmSheetGenerator;
        this.pricingClient = pricingClient;
    }

    public void createBidInfoSheet(HSSFWorkbook workbook, List<Map<String, String>> dataRowModel, String sheetName) {
        HeaderRowModel headerRowModel = headerRowModelFactory.createBidInfoHeader();
        bcmSheetGenerator.createBCMSheet(workbook, headerRowModel,dataRowModel, sheetName);
    }

    public void createProductPerSiteSheet(HSSFWorkbook workbook, List<Map<String, String>> dataRowModel, String sheetName) {
        HeaderRowModel headerRowModel = headerRowModelFactory.createProductPerSiteHeader();
        bcmSheetGenerator.createBCMSheet(workbook, headerRowModel,dataRowModel, sheetName);
    }

    public void createSpecialBidSheet(HSSFWorkbook workbook, List<Map<String, String>> dataRowModel, String sheetName) {
        HeaderRowModel headerRowModel = headerRowModelFactory.createSpecialBidInfoSheetHeader();
        bcmSheetGenerator.createBCMSheet(workbook, headerRowModel,dataRowModel, sheetName);
    }

    public void createSiteServiceRootProductSheet(HSSFWorkbook workbook, PricingSheetDataModel dataModel){
        List<? extends AbstractPricingSheetProductModel> productModels = dataModel.getProducts();
        Map<String, List<ProductIdentifier>> productsForSheet = getProductsForSheet();

        //Site Based Data
        HashMultimap<ProductIdentifier, AbstractPricingSheetProductModel> pricingSheetProductModelMapForSiteInstallable
            = groupPricingSheetProductModelByHcode(productModels, productsForSheet.get(SiteInstallable.sheetName));

        //Service Based Data
        HashMultimap<String, AbstractPricingSheetProductModel> pricingSheetProductModelMapForSiteAgnostic
            = groupPricingSheetProductModelByScode(productModels, productsForSheet.get(SiteAgnostic.sheetName));

        //Create The Site Based Sheets
        if(!pricingSheetProductModelMapForSiteInstallable.isEmpty()){
            createSitePerRootProductSheet(pricingSheetProductModelMapForSiteInstallable, workbook);

        }

        //Create the Service Based Sheets
        if(!pricingSheetProductModelMapForSiteAgnostic.isEmpty()){
            createServicePerRootProductSheet(pricingSheetProductModelMapForSiteAgnostic, workbook);
        }
    }

    private void createServicePerRootProductSheet(HashMultimap<String, AbstractPricingSheetProductModel> productModelMap,
                                                  HSSFWorkbook workbook) {
        for(String productModelScode : productModelMap.keySet()){
            String shortServiceName = ServiceProductScode.getShortServiceNameByScode(productModelScode);
            if(isNotNull(shortServiceName)&& !shortServiceName.isEmpty()){
                createServiceBasedSheet(workbook, Lists.newArrayList(productModelMap.get(productModelScode)),
                                        shortServiceName);
            }
        }
    }

    private void createSitePerRootProductSheet(HashMultimap<ProductIdentifier, AbstractPricingSheetProductModel> productModelMap,
                                               HSSFWorkbook workbook) {
        for(ProductIdentifier productIdentifier : productModelMap.keySet()){
            String siteManagementSheetName=Strings.nullToEmpty(productIdentifier.getDisplayName())+" "+BCMConstants.BCM_SITE_MANAGEMENT_SHEET;
            String siteSheetName=Strings.nullToEmpty(productIdentifier.getDisplayName())+" "+BCMConstants.BCM_SITE_SHEET;
            createSiteBasedSheet(workbook, Lists.newArrayList(productModelMap.get(productIdentifier)),siteSheetName);
            createSiteManagementSheet(workbook,Lists.newArrayList(productModelMap.get(productIdentifier)),siteManagementSheetName);
        }
    }

    private void createSiteBasedSheet(HSSFWorkbook workbook, List<AbstractPricingSheetProductModel> productModels, String sheetName) {
        HeaderRowModel headerRowModel = headerRowModelFactory.createSiteBasedRootProductSheetHeader(sheetName);
        List<ProductDataRowModel> dataRowModel = bcmDataRowModelFactory.createProductRowModel(productModels);
        List<Map<String,String>> dataRows = getSiteDataRows(dataRowModel,sheetName);
        bcmSheetGenerator.createSiteBasedBCMSheet(workbook, headerRowModel, dataRows, sheetName);
    }

    private void createServiceBasedSheet(HSSFWorkbook workbook,List<AbstractPricingSheetProductModel> productModels, String sheetName) {
        HeaderRowModel headerRowModel = headerRowModelFactory.createServiceBasedRootProductSheetHeader(sheetName);
        List<ProductDataRowModel> dataRowModel = bcmDataRowModelFactory.createServiceRowModel(productModels, pricingClient.getPricingConfig());
        List<Map<String,String>> dataRows = getServiceDataRows(dataRowModel, sheetName);
        bcmSheetGenerator.createServiceBasedBCMSheet(workbook, headerRowModel, dataRows, sheetName);
    }

    private List<Map<String, String>> getServiceDataRows(List<ProductDataRowModel> dataRowModel, String sheetName) {
        List<Map<String,String>> dataRows = newArrayList();
        for(ProductDataRowModel productDataRowModel: dataRowModel){
            Map<String,String> columnMap = newLinkedHashMap();
            if(isNotNull(productDataRowModel.getRootProductInstance())){
                columnMap.put(ServiceProductSheetStaticColumn.PRODUCT_INSTANCE.retrieveValueFrom,
                              productDataRowModel.getRootProductInstance().getProductInstance().getProductInstanceId().getValue());
                columnMap.put(ServiceProductSheetStaticColumn.PRODUCT_INSTANCE_VERSION.retrieveValueFrom,
                              String.valueOf(productDataRowModel.getRootProductInstance().getProductInstance().getProductInstanceVersion()));
                 if(isNotNull(productDataRowModel.getRootProductInstance().getBcmPriceModel())){
                     if(isNotNull(productDataRowModel.getRootProductInstance().getBcmPriceModel().getOneTimePriceLine())){
                         columnMap.put(ServiceProductSheetStaticColumn.ONE_TIME_PRICE_LINE.retrieveValueFrom,
                                       productDataRowModel.getRootProductInstance().getBcmPriceModel().getOneTimePriceLine().getId());
                     }
                     if(isNotNull(productDataRowModel.getRootProductInstance().getBcmPriceModel().getMonthlyPriceLine())){
                         columnMap.put(ServiceProductSheetStaticColumn.MONTHLY_PRICE_LINE.retrieveValueFrom,
                                       productDataRowModel.getRootProductInstance().getBcmPriceModel().getMonthlyPriceLine().getId());
                     }
                     columnMap.put(ServiceProductSheetStaticColumn.ORDER_TYPE.retrieveValueFrom,
                                   productDataRowModel.getRootProductInstance().getBcmPriceModel().lineItemAction);
                     columnMap.put(ServiceProductSheetStaticColumn.SERVICE_TYPE.retrieveValueFrom,
                                   productDataRowModel.getRootProductInstance().getBcmPriceModel().scheme.getName());

                     //ATTRIBUTES
                     Map<String,String> attributesMap = isNotNull(AttributesMapper.getAttributeMapper(sheetName)) ? AttributesMapper.getAttributeMapper(sheetName).attributesMap(): Collections.EMPTY_MAP;
                     for(String attributeKey: attributesMap.keySet()){
                         String characteristicKey= attributesMap.get(attributeKey);
                         String attributeValue = getInstanceCharacteristicValue(productDataRowModel.getRootProductInstance().getProductInstance(),characteristicKey);
                         columnMap.put(attributeKey,attributeValue);
                     }

                     //PRICE SECTION
                     columnMap.put(ServiceProductSheetStaticColumn.ONE_TIME_EUP.retrieveValueFrom,
                                   productDataRowModel.getRootProductInstance().getBcmPriceModel().getOnetimeEUPPrice());
                     columnMap.put(ServiceProductSheetStaticColumn.ONE_TIME_PTP.retrieveValueFrom,
                                   productDataRowModel.getRootProductInstance().getBcmPriceModel().getOneTimePTPPrice());
                     columnMap.put(ServiceProductSheetStaticColumn.ONE_TIME_DISCOUNT.retrieveValueFrom,
                                   String.valueOf(productDataRowModel.getRootProductInstance().getBcmPriceModel().getOneTimeDiscount()));

                     columnMap.put(ServiceProductSheetStaticColumn.MONTHLY_EUP.retrieveValueFrom,
                                   productDataRowModel.getRootProductInstance().getBcmPriceModel().getRecurringEUPPrice());
                     columnMap.put(ServiceProductSheetStaticColumn.MONTHLY_PTP.retrieveValueFrom,
                                   productDataRowModel.getRootProductInstance().getBcmPriceModel().getRecurringPTPPrice());
                     columnMap.put(ServiceProductSheetStaticColumn.MONTHLY_DISCOUNT.retrieveValueFrom,
                                   String.valueOf(productDataRowModel.getRootProductInstance().getBcmPriceModel().getMonthlyDiscount()));
                 }

                if(isNotNull(productDataRowModel.getRootProductInstance().getBcmCostModel()) && productDataRowModel.getRootProductInstance().getBcmCostModel().size() > 0) {
                    columnMap.put(ServiceProductSheetStaticColumn.NRC.retrieveValueFrom,
                                  productDataRowModel.getRootProductInstance().getBcmCostModel().get(0).getOneTimeEUPValue());
                    columnMap.put(ServiceProductSheetStaticColumn.MRC.retrieveValueFrom,
                                  productDataRowModel.getRootProductInstance().getBcmCostModel().get(0).getRecurringEUPValue());
                }
            }
            dataRows.add(columnMap);
        }
        return dataRows;
    }

    private List<Map<String, String>> getSiteManagementDataRows(List<ProductDataRowModel> dataRowModel, String sheetName) {
        List<Map<String,String>> dataRows = newArrayList();
        for(ProductDataRowModel productDataRowModel: dataRowModel){
            Map<String,String> columnMap = newLinkedHashMap();
            if(isNotNull(productDataRowModel.getRootProductInstance())){
                columnMap.put(SiteManagementStaticColumns.SITE_STATUS.retrieveValueFrom,
                              productDataRowModel.getRootProductInstance().getProductInstance().getBcmPricingStatus());
                columnMap.put(SiteManagementStaticColumns.PRODUCT_INSTANCE.retrieveValueFrom,
                              productDataRowModel.getRootProductInstance().getProductInstance().getProductInstanceId().getValue());
                columnMap.put(SiteManagementStaticColumns.PRODUCT_INSTANCE_VERSION.retrieveValueFrom,
                              String.valueOf(productDataRowModel.getRootProductInstance().getProductInstance().getProductInstanceVersion()));
                columnMap.put(SiteManagementStaticColumns.SITE_ID.retrieveValueFrom,
                              productDataRowModel.getSite().bfgSiteID);
                columnMap.put(SiteManagementStaticColumns.SITE_NAME.retrieveValueFrom,
                              productDataRowModel.getSite().getSiteName());
                columnMap.put(SiteManagementStaticColumns.COUNTRY.retrieveValueFrom,
                              productDataRowModel.getSite().getCountryName());
                columnMap.put(SiteManagementStaticColumns.CITY.retrieveValueFrom,
                              productDataRowModel.getSite().getCity());
                if(isNotNull(productDataRowModel.getRootProductInstance().getBcmPriceModel())){
                    columnMap.put(SiteManagementStaticColumns.ORDER_TYPE.retrieveValueFrom,
                                  productDataRowModel.getRootProductInstance().getBcmPriceModel().lineItemAction);
                    columnMap.put(SiteManagementStaticColumns.SERVICE_TYPE.retrieveValueFrom,
                                  productDataRowModel.getRootProductInstance().getBcmPriceModel().scheme.getName());

                    //ATTRIBUTES
                    Map<String,String> attributesMap = isNotNull(AttributesMapper.getAttributeMapper(sheetName)) ? AttributesMapper.getAttributeMapper(sheetName).attributesMap(): Collections.EMPTY_MAP;
                    for(String attributeKey: attributesMap.keySet()){
                        String characteristicKey= attributesMap.get(attributeKey);
                        String attributeValue = getInstanceCharacteristicValue(productDataRowModel.getRootProductInstance().getProductInstance(),characteristicKey);
                        columnMap.put(attributeKey,attributeValue);
                    }

                    //PRICE SECTION
                    if(isNotNull(productDataRowModel.getRootProductInstance().getBcmPriceModel().getOneTimePriceLine())){
                        columnMap.put(SiteManagementStaticColumns.ONE_TIME_PRICE_LINE.retrieveValueFrom,
                                      productDataRowModel.getRootProductInstance().getBcmPriceModel().getOneTimePriceLine().getId());
                    }
                    if(isNotNull(productDataRowModel.getRootProductInstance().getBcmPriceModel().getMonthlyPriceLine())){
                        columnMap.put(SiteManagementStaticColumns.MONTHLY_PRICE_LINE.retrieveValueFrom,
                                      productDataRowModel.getRootProductInstance().getBcmPriceModel().getMonthlyPriceLine().getId());
                    }
                    columnMap.put(SiteManagementStaticColumns.ONE_TIME_EUP.retrieveValueFrom,
                                  productDataRowModel.getRootProductInstance().getBcmPriceModel().getOnetimeEUPPrice());
                    columnMap.put(SiteManagementStaticColumns.ONE_TIME_PTP.retrieveValueFrom,
                                  productDataRowModel.getRootProductInstance().getBcmPriceModel().getOneTimePTPPrice());
                    columnMap.put(SiteManagementStaticColumns.ONE_TIME_DISCOUNT.retrieveValueFrom,
                                  String.valueOf(productDataRowModel.getRootProductInstance().getBcmPriceModel().getOneTimeDiscount()));

                    columnMap.put(SiteManagementStaticColumns.MONTHLY_EUP.retrieveValueFrom,
                                  productDataRowModel.getRootProductInstance().getBcmPriceModel().getRecurringEUPPrice());
                    columnMap.put(SiteManagementStaticColumns.MONTHLY_PTP.retrieveValueFrom,
                                  productDataRowModel.getRootProductInstance().getBcmPriceModel().getRecurringPTPPrice());
                    columnMap.put(SiteManagementStaticColumns.MONTHLY_DISCOUNT.retrieveValueFrom,
                                  String.valueOf(productDataRowModel.getRootProductInstance().getBcmPriceModel().getMonthlyDiscount()));
                }
                if(isNotNull(productDataRowModel.getRootProductInstance().getBcmCostModel()) && productDataRowModel.getRootProductInstance().getBcmCostModel().size() > 0) {
                    columnMap.put(SiteManagementStaticColumns.NRC.retrieveValueFrom,
                                  productDataRowModel.getRootProductInstance().getBcmCostModel().get(0).getOneTimeEUPValue());
                    columnMap.put(SiteManagementStaticColumns.MRC.retrieveValueFrom,
                                  productDataRowModel.getRootProductInstance().getBcmCostModel().get(0).getRecurringEUPValue());
                }
            }
            dataRows.add(columnMap);
        }
        return dataRows;
    }

    private void createSiteManagementSheet(HSSFWorkbook workbook, List<AbstractPricingSheetProductModel> productModels, String sheetName) {
        HeaderRowModel headerRowModel = headerRowModelFactory.createSiteManagementSheetHeader(sheetName);
        List<ProductDataRowModel> dataRowModel = bcmDataRowModelFactory.createSiteManagementRowModel(productModels,pricingClient.getPricingConfig());
        List<Map<String,String>> dataRows = getSiteManagementDataRows(dataRowModel,sheetName);
        bcmSheetGenerator.createSiteManagementBCMSheet(workbook, headerRowModel, dataRows, sheetName);
    }

    private List<Map<String, String>> getSiteDataRows(List<ProductDataRowModel> dataRowModel, String sheetName) {
         List<Map<String,String>> dataRows = newArrayList();
        for(ProductDataRowModel productDataRowModel : dataRowModel){
            Map<String,String> columnMap = newLinkedHashMap();
            columnMap.put(SiteProductSheetStaticColumn.SITE_ID.retrieveValueFrom,
                          productDataRowModel.getSite().bfgSiteID);
            columnMap.put(SiteProductSheetStaticColumn.SITE_NAME.retrieveValueFrom,
                          productDataRowModel.getSite().getSiteName());
            columnMap.put(SiteProductSheetStaticColumn.COUNTRY.retrieveValueFrom,
                          productDataRowModel.getSite().getCountryName());
            columnMap.put(SiteProductSheetStaticColumn.CITY.retrieveValueFrom,
                          productDataRowModel.getSite().getCity());
            columnMap.put(SiteProductSheetStaticColumn.ORDER_TYPE.retrieveValueFrom,
                          productDataRowModel.getQuoteOptionItem().action);
            if(isNotNull(productDataRowModel.getCpeProductInstance())){
                columnMap.put(SiteProductSheetStaticColumn.SITE_STATUS.retrieveValueFrom,getSiteStatus(productDataRowModel.getCpeProductInstance()));
                columnMap.put(SiteProductSheetStaticColumn.CPE_PRICE_STATUS.retrieveValueFrom,
                              productDataRowModel.getCpeProductInstance().getProductInstance().getBcmPricingStatus());
                //ATTRIBUTES SECTION
                Map<String,String> attributesMap = isNotNull(AttributesMapper.getAttributeMapper(sheetName)) ? AttributesMapper.getAttributeMapper(sheetName).attributesMap(): Collections.EMPTY_MAP;
                for(String attributeKey: attributesMap.keySet()){
                    String characteristicKey= attributesMap.get(attributeKey);
                    String attributeValue;
                    if(ProductOffering.LAN_TYPE.equals(characteristicKey)){
                        attributeValue=getInstanceCharacteristicValue(productDataRowModel.getCpeProductInstance().getProductInstance().getParentOptional().get(),characteristicKey);
                    }else {
                        attributeValue=getInstanceCharacteristicValue(productDataRowModel.getCpeProductInstance().getProductInstance(),characteristicKey);
                    }
                    columnMap.put(attributeKey,attributeValue);
                }
                //CPE PRICE SECTION
                BCMPriceModel cpePriceModel = productDataRowModel.getCpeProductInstance().getBcmPriceModel();
                if(isNotNull(cpePriceModel)){
                    columnMap.put(SiteProductSheetStaticColumn.CPE_PRODUCT_INSTANCE.retrieveValueFrom,
                                  productDataRowModel.getCpeProductInstance().getProductInstance().getProductInstanceId().getValue());
                    columnMap.put(SiteProductSheetStaticColumn.CPE_PRODUCT_INSTANCE_VERSION.retrieveValueFrom,
                                  String.valueOf(productDataRowModel.getCpeProductInstance().getProductInstance().getProductInstanceVersion()));
                    if(isNotNull(cpePriceModel.getOneTimePriceLine())){
                        columnMap.put(SiteProductSheetStaticColumn.CPE_INSTALL_PRICE_LINE.retrieveValueFrom,
                                  cpePriceModel.getOneTimePriceLine().getId());
                    }
                    if(isNotNull(cpePriceModel.getMonthlyPriceLine())){
                        columnMap.put(SiteProductSheetStaticColumn.CPE_MONTHLY_PRICE_LINE.retrieveValueFrom,
                                      cpePriceModel.getMonthlyPriceLine().getId());
                    }
                    columnMap.put(SiteProductSheetStaticColumn.CPE_INSTALL_EUP.retrieveValueFrom,
                                  cpePriceModel.getOnetimeEUPPrice());
                    columnMap.put(SiteProductSheetStaticColumn.CPE_INSTALL_PTP.retrieveValueFrom,
                                  cpePriceModel.getOneTimePTPPrice());   //4 decimal Places
                    columnMap.put(SiteProductSheetStaticColumn.CPE_INSTALL_DISCOUNT.retrieveValueFrom,
                                  String.valueOf(cpePriceModel.getOneTimeDiscount())); //4 decimal Places

                    columnMap.put(SiteProductSheetStaticColumn.CPE_MONTHLY_EUP.retrieveValueFrom,
                                  cpePriceModel.getRecurringEUPPrice());
                    columnMap.put(SiteProductSheetStaticColumn.CPE_MONTHLY_PTP.retrieveValueFrom,
                                  cpePriceModel.getRecurringPTPPrice());
                    columnMap.put(SiteProductSheetStaticColumn.CPE_MONTHLY_DISCOUNT.retrieveValueFrom,
                                  String.valueOf(cpePriceModel.getMonthlyDiscount()));
                }

                List<BCMPriceModel> cpeCostModel = productDataRowModel.getCpeProductInstance().getBcmCostModel();
                if(isNotNull(cpeCostModel)) {

                    Map<String,BCMPriceModel> bcmCostModelMap = getCostModelFromCpeRules(cpeCostModel);

                    BCMPriceModel costModel = bcmCostModelMap.get("originalCost");
                    if(isNotNull(costModel)){
                        columnMap.put(SiteProductSheetStaticColumn.CPE_NRC.retrieveValueFrom, costModel.getOnetimeEUPPrice());
                        columnMap.put(SiteProductSheetStaticColumn.CPE_MRC.retrieveValueFrom, costModel.getRecurringEUPPrice());
                        columnMap.put(SiteProductSheetStaticColumn.CPE_DISCOUNTED_MRC.retrieveValueFrom, costModel.getMonthlyDiscountedValue());
                    }

                    costModel = bcmCostModelMap.get("capexCost");
                    if(isNotNull(costModel)){
                        columnMap.put(SiteProductSheetStaticColumn.CAPEX_NRC.retrieveValueFrom, costModel.getOnetimeEUPPrice());
                        columnMap.put(SiteProductSheetStaticColumn.CAPEX_DISCOUNTED_NRC.retrieveValueFrom, costModel.getOneTimeDiscountedValue());
                    }

                    costModel = bcmCostModelMap.get("kitCost");
                    if(isNotNull(costModel)){
                        columnMap.put(SiteProductSheetStaticColumn.KIT_MRC.retrieveValueFrom, costModel.getRecurringEUPPrice());
                        columnMap.put(SiteProductSheetStaticColumn.KIT_DISCOUNTED_MRC.retrieveValueFrom, costModel.getMonthlyDiscountedValue());
                        columnMap.put(SiteProductSheetStaticColumn.CPE_VENDOR_DISCOUNT_REF.retrieveValueFrom, costModel.getVendorDiscountRef());
                    }
                    costModel = bcmCostModelMap.get("maintenanceCost");
                    if(isNotNull(costModel)){
                        columnMap.put(SiteProductSheetStaticColumn.CPE_SUPPLIER_MTCE_MRC.retrieveValueFrom, costModel.getRecurringEUPPrice());
                    }
                }
            }

            //VENDOR MAINTENANCE  SECTION
            if(isNotNull(productDataRowModel.getVendorMaintenanceInstance()) && isNotNull(productDataRowModel.getVendorMaintenanceInstance().getBcmCostModel()) && productDataRowModel.getVendorMaintenanceInstance().getBcmCostModel().size() >0) {
                BCMPriceModel vendorMaintenanceCostModel = productDataRowModel.getVendorMaintenanceInstance().getBcmCostModel().get(0);
                if(isNotNull(vendorMaintenanceCostModel)){
                    columnMap.put(SiteProductSheetStaticColumn.HARDWARE_VENDOR_MTCE_NRC.retrieveValueFrom, vendorMaintenanceCostModel.getOnetimeEUPPrice());
                    columnMap.put(SiteProductSheetStaticColumn.HARDWARE_VENDOR_MTCE_MRC.retrieveValueFrom, vendorMaintenanceCostModel.getRecurringEUPPrice());
                    columnMap.put(SiteProductSheetStaticColumn.VENDOR_MTCE_DISCOUNTED_MRC.retrieveValueFrom, vendorMaintenanceCostModel.getMonthlyDiscountedValue());
                    columnMap.put(SiteProductSheetStaticColumn.VENDOR_MTCE_DISCOUNT_REF.retrieveValueFrom, vendorMaintenanceCostModel.getVendorDiscountRef());
                }
            }

            //LICENCE SECTION
            int i=1;
            List<ProductDataInfo> licences = productDataRowModel.getLicences();
            for(ProductDataInfo licence : licences){
                columnMap.put(LICENCE + i + " " + DESCRIPTION.retrieveValueFrom
                        , getInstanceCharacteristicValue(licence.getProductInstance(),"PART DESCRIPTION"));
                columnMap.put(LICENCE + i + " " + PRODUCT_INSTANCE.retrieveValueFrom
                    , licence.getProductInstance().getProductInstanceId().getValue());
                columnMap.put(LICENCE + i + " " + PRODUCT_INSTANCE_VERSION.retrieveValueFrom
                    , String.valueOf(licence.getProductInstance().getProductInstanceVersion()));
                if(isNotNull(licence.getBcmPriceModel())){
                    if(isNotNull(licence.getBcmPriceModel().getOneTimePriceLine())){
                        columnMap.put(LICENCE +i+" "+ONE_TIME_PRICE_LINE.retrieveValueFrom
                            ,licence.getBcmPriceModel().getOneTimePriceLine().getId());
                    }
                    if(isNotNull(licence.getBcmPriceModel().getMonthlyPriceLine())){
                        columnMap.put(LICENCE +i+" "+MONTHLY_PRICE_LINE.retrieveValueFrom
                            ,licence.getBcmPriceModel().getMonthlyPriceLine().getId());
                    }
                    columnMap.put(LICENCE +i+" "+TOTAL_ONE_TIME_EUP.retrieveValueFrom
                        ,licence.getBcmPriceModel().getOnetimeEUPPrice());
                    columnMap.put(LICENCE +i+" "+TOTAL_ONE_TIME_PTP.retrieveValueFrom
                        ,licence.getBcmPriceModel().getOneTimePTPPrice());
                    columnMap.put(LICENCE +i+" "+ONE_TIME_DISCOUNT.retrieveValueFrom
                        ,String.valueOf(licence.getBcmPriceModel().getOneTimeDiscount()));
                    columnMap.put(LICENCE + i + " " + TOTAL_MONTHLY_EUP.retrieveValueFrom
                        , licence.getBcmPriceModel().getRecurringEUPPrice());
                    columnMap.put(LICENCE +i+" "+TOTAL_MONTHLY_PTP.retrieveValueFrom
                        ,licence.getBcmPriceModel().getRecurringPTPPrice());
                    columnMap.put(LICENCE +i+" "+MONTHLY_DISCOUNT.retrieveValueFrom
                        ,String.valueOf(licence.getBcmPriceModel().getMonthlyDiscount()));
                }

                if(isNotNull(licence.getBcmCostModel()) && (licence.getBcmCostModel().size() >0) ){
                    BCMPriceModel licenceCostModel = licence.getBcmCostModel().get(0);
                    columnMap.put(LICENCE +i+" "+NRC.retrieveValueFrom ,licenceCostModel.getOnetimeEUPPrice());
                    columnMap.put(LICENCE +i+" "+MRC.retrieveValueFrom ,licenceCostModel.getRecurringPrice());
                    columnMap.put(LICENCE +i+" "+DISCOUNTED_MRC.retrieveValueFrom ,licenceCostModel.getMonthlyDiscountedValue());
                    columnMap.put(LICENCE +i+" "+VENDOR_DISCOUNT_REF.retrieveValueFrom ,licenceCostModel.getVendorDiscountRef());
                }

                i++;
            }
            dataRows.add(columnMap);
        }
        return dataRows;
    }

    private Map<String,BCMPriceModel> getCostModelFromCpeRules(List<BCMPriceModel> cpeCostModels) {
        String chargingSchemeName = getChargingScheme(cpeCostModels);
        Map<String,BCMPriceModel> bcmCostModelMap = newHashMap();

        final List<ChargingSchemeConfig> chargingSchemeConfigList = pricingClient.getPricingConfig().chargingSchemes()
                                                                                 .forName(chargingSchemeName)
                                                                                 .search();

        if(chargingSchemeConfigList.size()==0){
            return bcmCostModelMap;
        }

        List<BillingTariffRulesetConfig> billingTariffRulesetConfigs = chargingSchemeConfigList.get(0).getBillingTariffRuleSets();

        for(BillingTariffRulesetConfig billingTariffRulesetConfig : billingTariffRulesetConfigs){
            for(CPERuleConfig cpeRuleConfig : billingTariffRulesetConfig.getCPERules()){
                for(BCMPriceModel cpeCostModel : cpeCostModels){
                    if(billingTariffRulesetConfig.getId().equals(cpeCostModel.getPmfId())){
                        bcmCostModelMap.put(cpeRuleConfig.getId(),cpeCostModel);
                    }
                }
            }
        }

        return bcmCostModelMap;
    }
    private String getChargingScheme(List<BCMPriceModel> cpeCostModels) {
        if(isNotNull(cpeCostModels) && cpeCostModels.size() >0){
            String chargingSchemeName = cpeCostModels.get(0).getScheme().getName();
            for(BCMPriceModel currentPriceModel: cpeCostModels){
                if(!(currentPriceModel.getScheme().getName().equals(chargingSchemeName))){
                    return "";
                }
            }
            return chargingSchemeName;
        }
        return "";
    }


    private String getSiteStatus(ProductDataInfo cpeProductInstance) {
        String  siteStatus =PricingStatus.NOT_PRICED.getDescription();
        if (cpeProductInstance.getProductInstance().getParentOptional().isPresent()) {
            siteStatus = cpeProductInstance.getProductInstance().getParentOptional().get().getBcmPricingStatus();
        }
        return  siteStatus;
    }

    private Map<String, List<ProductIdentifier>> getProductsForSheet() {
        List<ProductIdentifier> allSellableProductIdentifiers = pmrClient.getSalesCatalogue().getAllSellableProductIdentifiers();
        List<ProductIdentifier> siteAgnosticProducts = new ArrayList<ProductIdentifier>();
        List<ProductIdentifier> siteInstallableProducts = new ArrayList<ProductIdentifier>();
        Map<String, List<ProductIdentifier>> productMap = new HashMap<String, List<ProductIdentifier>>();
        for (ProductIdentifier productIdentifier : allSellableProductIdentifiers) {
            ProductOffering productOffering = pmrClient.productOffering(ProductSCode.newInstance(productIdentifier.getProductId())).get();
            if (productOffering.isSiteInstallable()) {
                siteInstallableProducts.add(productIdentifier);
            } else if (!productOffering.isSiteInstallable()) {
                siteAgnosticProducts.add(productIdentifier);
            }
        }
        productMap.put(SiteAgnostic.sheetName, siteAgnosticProducts);
        productMap.put(SiteInstallable.sheetName, siteInstallableProducts);
        return productMap;
    }

    private HashMultimap<String, AbstractPricingSheetProductModel> groupPricingSheetProductModelByScode(List<? extends AbstractPricingSheetProductModel> productModels, List<ProductIdentifier> productIdentifiers) {
        HashMultimap<String, AbstractPricingSheetProductModel> pricingSheetProductModelMap = HashMultimap.create();
        for (AbstractPricingSheetProductModel pricingSheetProductModel : productModels){
            for(ProductIdentifier productIdentifier : productIdentifiers) {
                if(pricingSheetProductModel.getSCode().equals(productIdentifier.getProductId())) {
                    pricingSheetProductModelMap.put(pricingSheetProductModel.getSCode(), pricingSheetProductModel);
                }
            }
        }
        return pricingSheetProductModelMap;
    }

    private HashMultimap<ProductIdentifier, AbstractPricingSheetProductModel> groupPricingSheetProductModelByHcode(List<? extends AbstractPricingSheetProductModel> productModels, List<ProductIdentifier> productIdentifiers) {
        HashMultimap<ProductIdentifier, AbstractPricingSheetProductModel> pricingSheetProductModelMap = HashMultimap.create();
        for (AbstractPricingSheetProductModel pricingSheetProductModel : productModels){
            for(ProductIdentifier productIdentifier : productIdentifiers) {
                if(pricingSheetProductModel.getSCode().equals(productIdentifier.getProductId())) {
                    ProductIdentifier identifier = pmrClient.getProductHCode(productIdentifier.getProductId()).get();
                    pricingSheetProductModelMap.put(identifier, pricingSheetProductModel);
                }
            }
        }
        return pricingSheetProductModelMap;
    }

    private String getInstanceCharacteristicValue(ProductInstance productInstance, String characteristicKey) {
        if(isNotNull(productInstance)){
            try{
                InstanceCharacteristic instanceCharacteristic = productInstance.getInstanceCharacteristic(characteristicKey);
                if (isNotNull(instanceCharacteristic)) {
                    return instanceCharacteristic.getStringValue();
                }
            } catch (InstanceCharacteristicNotFound instanceCharacteristicNotFound) {
                return StringUtils.EMPTY;
            }
        }
        return StringUtils.EMPTY;
    }

}
