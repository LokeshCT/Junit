package com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet;

import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.domain.product.ContributesToCharacteristicUpdater;
import com.bt.rsqe.projectengine.migration.QuoteMigrationDetailsProvider;
import com.bt.rsqe.projectengine.web.model.LineItemModel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Maps.newHashMap;

public class RFOSheetModelBuilder {
    private final ProductInstanceClient futureProductInstanceClient;
    private final QuoteMigrationDetailsProvider migrationDetailsProvider;
    private ContributesToCharacteristicUpdater characteristicUpdater;
    private Map<String, String> sCodeToProductNames = newHashMap();

    public RFOSheetModelBuilder(ProductInstanceClient futureProductInstanceClient, QuoteMigrationDetailsProvider migrationDetailsProvider, ContributesToCharacteristicUpdater characteristicUpdater) {
        this.futureProductInstanceClient = futureProductInstanceClient;
        this.migrationDetailsProvider = migrationDetailsProvider;
        this.characteristicUpdater = characteristicUpdater;
    }

    private String getProductName(LineItemModel lineItem) {
        String productName = sCodeToProductNames.get(lineItem.getProductSCode());

        if(null == productName) {
            productName = lineItem.getProductName();
            sCodeToProductNames.put(lineItem.getProductSCode(), productName);
        }

        return productName;
    }

    public Map<String, RFOSheetModel> build(List<LineItemModel> lineItems) {
        HashMap<String, RFOSheetModel> rfoSheetModelsForSCodes = new HashMap<String, RFOSheetModel>();

        for (LineItemModel lineItem : lineItems) {
            if (!rfoSheetModelsForSCodes.containsKey(lineItem.getProductSCode())) {
                rfoSheetModelsForSCodes.put(lineItem.getProductSCode(), new RFOSheetModel(futureProductInstanceClient, getProductName(lineItem), lineItem.getProductSCode(), migrationDetailsProvider, characteristicUpdater));
            }
            rfoSheetModelsForSCodes.get(lineItem.getProductSCode()).add(lineItem, lineItem.getProductSCode());
        }

        // We now re-parse the model merging in missing attributes with default values. (i.e. disabled/greyed)
        // This is to cover the scenario where an attribute can be mandatory on one instance and optional on another -
        // each ends up with only "AttrName (O)" or "AttrName (M)" but every row needs both, the other being disabled.
        for (RFOSheetModel rfoSheetModel : rfoSheetModelsForSCodes.values()) {
            HashMap<String, Set<String>> combinedAttributes = new HashMap<String, Set<String>>();
            addAttributesRecursively(combinedAttributes, rfoSheetModel.getRFOExportModel());
            mergeAttributesRecursively(combinedAttributes, rfoSheetModel.getRFOExportModel());
        }

        return rfoSheetModelsForSCodes;
    }

    private void mergeAttributesRecursively(Map<String, Set<String>> combinedAttributes, List<RFOSheetModel.RFORowModel> rfoRowModels) {
        for (RFOSheetModel.RFORowModel rfoRowModel : rfoRowModels) {
            for (String combinedAttribute : combinedAttributes.get(rfoRowModel.getsCode())) {
                if(!rfoRowModel.hasAttribute(combinedAttribute)){
                    rfoRowModel.addAttribute(combinedAttribute, "");
                    rfoRowModel.addGrayOutColumns(combinedAttribute);
                    rfoRowModel.addLockedColumns(combinedAttribute);
                }
            }

            for (List<RFOSheetModel.RFORowModel> childRowModels : rfoRowModel.getRFOChildrenMap().values()) {
                mergeAttributesRecursively(combinedAttributes, childRowModels);
            }
        }
    }

    private void addAttributesRecursively(Map<String, Set<String>> combinedAttributes, List<RFOSheetModel.RFORowModel> rfoRowModels) {
        for (RFOSheetModel.RFORowModel rfoRowModel : rfoRowModels) {
            final String sCode = rfoRowModel.getsCode();
            if(combinedAttributes.containsKey(sCode)) {
                combinedAttributes.get(sCode).addAll(rfoRowModel.getAttributes().keySet());
            }else{
                combinedAttributes.put(sCode, new HashSet(rfoRowModel.getAttributes().keySet()));
            }

            for (List<RFOSheetModel.RFORowModel> childRowModels : rfoRowModel.getRFOChildrenMap().values()) {
                addAttributesRecursively(combinedAttributes, childRowModels);
            }
        }
    }
}
