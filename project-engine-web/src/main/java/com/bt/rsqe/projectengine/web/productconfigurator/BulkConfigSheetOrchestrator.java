package com.bt.rsqe.projectengine.web.productconfigurator;


import com.bt.rsqe.client.QuoteOptionClient;
import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerrecord.SiteResource;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.projectengine.QuoteOptionItemResource;
import com.bt.rsqe.projectengine.web.ImportResults;
import com.bt.rsqe.projectengine.web.productconfigurator.model.BulkConfigDataModel;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

public class BulkConfigSheetOrchestrator {

    private BulkConfigDetailModelBuilder bulkConfigDetailModelBuilder;
    private ProductInstanceClient productInstanceClient;
    private PmrClient pmrClient;
    private QuoteOptionClient quoteOptionClient;

    public BulkConfigSheetOrchestrator(BulkConfigDetailModelBuilder bulkConfigDetailModelBuilder, ProductInstanceClient productInstanceClient, PmrClient pmrClient, QuoteOptionClient quoteOptionClient) {
        this.bulkConfigDetailModelBuilder = bulkConfigDetailModelBuilder;
        this.productInstanceClient = productInstanceClient;
        this.pmrClient = pmrClient;
        this.quoteOptionClient = quoteOptionClient;
    }

    public void importProductByBulkSheet(Workbook bulkSheet, List<ProductInstance> rootInstances, ImportResults importResults, SiteResource siteResource, Map<ProductIdentifier, ProductOffering> offerings, QuoteOptionItemResource quoteOptionItemResource) {
        final BulkConfigDataModel bulkConfigDataModel = createModels(bulkSheet);
        final BulkSheetImporter bulkSheetImporter = new BulkSheetImporter(bulkConfigDataModel, rootInstances, importResults, siteResource, offerings, productInstanceClient, quoteOptionClient, quoteOptionItemResource);
        bulkSheetImporter.importSheet();
    }

    private BulkConfigDataModel createModels(Workbook bulkSheet) {
        return bulkConfigDetailModelBuilder.buildBulkSheetDataModel(bulkSheet);
    }

}
