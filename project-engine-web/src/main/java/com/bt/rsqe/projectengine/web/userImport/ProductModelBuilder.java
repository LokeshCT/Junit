package com.bt.rsqe.projectengine.web.userImport;

import com.bt.rsqe.customerrecord.CustomerDTO;
import com.bt.rsqe.customerrecord.SiteResource;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.projectengine.QuoteOptionDTO;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.List;
import java.util.Map;

public class ProductModelBuilder {

    private final List<ProductInstance> productInstances;
    private final QuoteOptionDTO quoteOptionDTO;
    private final SiteResource siteResource;
    private final CustomerDTO customerDTO;
    private final Map<ProductIdentifier, ProductOffering> exportableProducts;

    public ProductModelBuilder(List<ProductInstance> productInstances, QuoteOptionDTO quoteOptionDTO, SiteResource siteResource, CustomerDTO customerDTO, Map<ProductIdentifier, ProductOffering> exportableProducts) {

        this.productInstances = productInstances;
        this.quoteOptionDTO = quoteOptionDTO;
        this.siteResource = siteResource;
        this.customerDTO = customerDTO;
        this.exportableProducts = exportableProducts;
    }

    public XSSFWorkbook constructWorkbook() {

        XSSFWorkbook workbook = new XSSFWorkbook();
        ListValidationBuilder listValidationBuilder = new ListValidationBuilder(workbook);
        UserImportExcelStyler styler = new UserImportExcelStyler(workbook);
        for (ProductInstance productInstance : productInstances) {
            InstanceToModelParser instanceToModelParser = new InstanceToModelParser(productInstance, workbook, quoteOptionDTO, customerDTO, listValidationBuilder, siteResource, styler, exportableProducts);
            List<? extends ProductSheetDataExtractor> dataExtractors = instanceToModelParser.build();
            for (ProductSheetDataExtractor productSheetDataExtractor : dataExtractors) {
                productSheetDataExtractor.constructSheet();
            }
        }
        return workbook;
    }
}
