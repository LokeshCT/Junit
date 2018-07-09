package com.bt.rsqe.projectengine.web.quoteoptionorders.ecrfsheet;

import com.bt.rsqe.client.Pmr;
import com.bt.rsqe.client.QuoteOptionClient;
import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerrecord.CustomerResource;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.ContractDTO;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.parameters.ProductCategoryCode;
import com.bt.rsqe.pc.client.ConfiguratorContractClient;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.web.AssetKeyContainer;
import com.bt.rsqe.projectengine.web.ImportResults;
import com.bt.rsqe.security.UserContext;
import com.google.common.base.Optional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.bt.rsqe.utils.AssertObject.isNotNull;
import static com.google.common.collect.Sets.newHashSet;

public class ProductBasedImporter extends BulkImporter {

    public ProductBasedImporter(ProductInstanceClient productInstanceClient, QuoteOptionClient quoteOptionClient, Pmr pmr, CardinalityValidator cardinalityValidator,
                                CustomerResource customerResource, ConfiguratorContractClient configuratorClient, ProjectResource projectResource, ProductRelationshipService productRelationshipService) {
        super(productInstanceClient, quoteOptionClient, pmr, cardinalityValidator, customerResource, configuratorClient, projectResource, productRelationshipService);
    }

    @Override
    protected Set<LineItemId> importRootProduct(ECRFWorkBook workBook, String customerId, String projectId, String contractId, String contractTerm, String quoteOptionId,
                                                Map<String, SiteDTO> customerSites, ImportResults importResults, ProductOffering productOffering, AssetKeyContainer assetKeyContainer,
                                                ECRFSheet ecrfSheet, BulkProduct bulkProduct, Optional<LineItemId> lineItemIdOptional, String importedProductCode, boolean isMigration, ProductCategoryCode productCategoryCode) {

        Set<LineItemId> impactedLineItems = newHashSet();
        List<ECRFSheetModelRow> rows = ecrfSheet.getRows();
        String lineItemId = null;
        //Mandatory Validation Block
        try {
            validateCardinality(customerId, contractId, quoteOptionId, customerSites.keySet(), importResults, productOffering, rows, null);
        } catch (Exception e) {
            importResults.addError(productOffering.getProductIdentifier().getProductId(), e.getMessage());
            return impactedLineItems;
        }

        for (ECRFSheetModelRow ecrfSheetModelRow : rows) {
            try {
                String siteId = null;
                if (productOffering.isSiteInstallable()) {
                    checkSiteIdColumnPresentWithValues(ecrfSheetModelRow);
                    siteId = getSiteIdFromSheet(ecrfSheetModelRow, customerSites);
                }
                lineItemId = UUID.randomUUID().toString();
                impactedLineItems.add(createAndImport(projectId, customerId, contractId, contractTerm, productOffering.getProductIdentifier().getProductId(), siteId, quoteOptionId, workBook, ecrfSheetModelRow, productOffering, assetKeyContainer, bulkProduct, lineItemIdOptional, lineItemId, isMigration, productCategoryCode));
                if(isAdditionalDetailMapped(workBook, ecrfSheetModelRow, DELIVERY_ADDRESS, ADDRESS_ID)){
                    extractDeliveryAddress(workBook, ecrfSheetModelRow.getRowId(), projectId, quoteOptionId, lineItemId, ADDRESS_ID);
                }

            } catch (Exception e) {
                importResults.addError(productOffering.getProductIdentifier().getProductId(), e.getMessage());
                if (isNotNull(lineItemId)) {
                    deleteOrphanInstances(projectId, quoteOptionId, lineItemId);
                    impactedLineItems.remove(new LineItemId(lineItemId));
                }
            }
        }
        return impactedLineItems;
    }

    @Override
    protected void createQuoteOptionItem(String customerId, String contractId, String projectId, String quoteOptionId, String siteId, String productCode, Optional<LineItemId> lineItemIdOptional, String lineItemId, String contractTerm, UserContext userContext, String holderLineItemId, ProductCategoryCode productCategoryCode) {
        ContractDTO contractDto = getContractDto(contractTerm, customerId, productCode, contractId, userContext.getRsqeToken());
        quoteOptionClient.createQuoteOptionItem(projectId, quoteOptionId, lineItemId, siteId, productCode, contractDto, holderLineItemId, productCategoryCode);
    }

}

