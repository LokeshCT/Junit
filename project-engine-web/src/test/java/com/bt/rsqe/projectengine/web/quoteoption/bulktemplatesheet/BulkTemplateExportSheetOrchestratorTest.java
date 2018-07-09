package com.bt.rsqe.projectengine.web.quoteoption.bulktemplatesheet;


import com.bt.rsqe.client.Pmr;
import com.bt.rsqe.domain.bom.fixtures.ProductOfferingFixture;
import com.bt.rsqe.domain.bom.fixtures.SalesRelationshipFixture;
import com.bt.rsqe.domain.bom.parameters.ProductSCode;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.SimpleProductOfferingType;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.product.parameters.RelationshipType;
import com.bt.rsqe.enums.ProductCodes;
import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet.ExcelWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Before;
import org.junit.Test;

import java.io.FileOutputStream;

import static com.bt.rsqe.domain.bom.fixtures.AttributeFixture.*;
import static com.bt.rsqe.domain.product.constraints.AttributeValue.*;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class BulkTemplateExportSheetOrchestratorTest {

    private PmrClient pmrClient;
    private ProductSCode rootSCode;
    private final String ROOT_SCODE = "ROOT_PROD";
    private final String CHILD_SCODE_1 = "CHILD1";
    private final String CHILD_SCODE_2 = "CHILD2";
    private final String GRAND_CHILD_SCODE = "GRANDCHILD";
    private final String RELATED_SCODE_1 = "REL-CODE-1";
    private final String RELATED_RELATED_SCODE_1 = "REL-RELATED-CODE-1";
    private final String RELATED_CHILD_SCODE_1 = "REL-CHILD-CODE-1";
    private final String DEFAULT_VALUE = "DEFAULT";
    private final String ACCESS_RELATION = "Access";
    private final String SITE_ID = "SITE ID";

    @Before
    public void setUp() {
        initProductOfferings();
    }

    private void initProductOfferings() {

        pmrClient=mock(PmrClient.class);

        ProductCodes.StandaloneAccessCircuit.productCode();
        String accessSCodeStr = ProductCodes.StandaloneAccessCircuit.productCode();
        String accessSCodeName = ProductCodes.StandaloneAccessCircuit.productName();

        //ROOT
        rootSCode = ProductSCode.newInstance(ROOT_SCODE);
        Pmr.ProductOfferings rootOfferings=mock(Pmr.ProductOfferings.class);
        String RELATED_SCODE_2 = "REL-CODE-2";
        ProductOffering rootOffering = ProductOfferingFixture.aProductOffering()
                                                             .withProductIdentifier(new ProductIdentifier(ROOT_SCODE, ROOT_SCODE, "1"))
                                                             .withAttributes(
                                                                 anRfqAttribute().called("ATT1").build(),
                                                                 anRfqAttribute().called("ATT2").withDefaultValue(DEFAULT_VALUE).build(),
                                                                 anInvisibleRfqAttribute().called("ATT3").build(),
                                                                 anRfqAttribute().called("ATT4").
                                                                     withAllowedValues(newInstance("Connect Acceleration Site"),
                                                                                       newInstance("Connect Acceleration Service"),
                                                                                       newInstance("Connect Acceleration Site Management"),
                                                                                       newInstance("Connect Intelligence Site"),
                                                                                       newInstance("Connect Intelligence Service"),
                                                                                       newInstance("Connect Intelligence Site Management"),
                                                                                       newInstance("Internet Connect Global"),
                                                                                       newInstance("Internet Connect GLobal Reach"),
                                                                                       newInstance("Telecom Expense Site"),
                                                                                       newInstance("Telecom Expense Site Management"),
                                                                                       newInstance("Access Scenario"),
                                                                                       newInstance("Access Circuit"),
                                                                                       newInstance("Customer Premise Equipment")).build(),
                                                                 anRfoAttribute().called("ATT5").build(),
                                                                 anRfqAttribute().called("STENCIL").build())
                                                             .withSalesRelationship(SalesRelationshipFixture.aSalesRelationship()
                                                                                                            .withRelationName("child1")
                                                                                                            .withRelationType(RelationshipType.Child)
                                                                                                            .withProductIdentifier(CHILD_SCODE_1, CHILD_SCODE_1))
                                                             .withSalesRelationship(SalesRelationshipFixture.aSalesRelationship()
                                                                                                            .withRelationName("child2")
                                                                                                            .withRelationType(RelationshipType.Child)
                                                                                                            .withProductIdentifier(CHILD_SCODE_2, CHILD_SCODE_2))
                                                             .withSalesRelationship(SalesRelationshipFixture.aSalesRelationship()
                                                                                                            .withRelationName("related")
                                                                                                            .withRelationType(RelationshipType.RelatedTo)
                                                                                                            .withProductIdentifier(RELATED_SCODE_1, RELATED_SCODE_1)
                                                                                                            .withCardinality(1,1,1))
                                                             .withSalesRelationship(SalesRelationshipFixture.aSalesRelationship()
                                                                                                            .withRelationName(ACCESS_RELATION)
                                                                                                            .withRelationType(RelationshipType.Child)
                                                                                                            .withProductIdentifier(accessSCodeStr, accessSCodeName))
                                                             .withSalesRelationship(SalesRelationshipFixture.aSalesRelationship()
                                                                                                            .withRelationName("related2")
                                                                                                            .withRelationType(RelationshipType.RelatedTo)
                                                                                                            .withProductIdentifier(RELATED_SCODE_2, RELATED_SCODE_2)
                                                                                                            .withCardinality(0,1,0))
                                                             .withSimpleProductOfferingType(SimpleProductOfferingType.Package)
                                                             .build();
        when(pmrClient.productOffering(rootSCode)).thenReturn(rootOfferings);
        when(rootOfferings.get()).thenReturn(rootOffering);

        //Child 1
        Pmr.ProductOfferings child1Offerings=mock(Pmr.ProductOfferings.class);
        ProductSCode child1SCode = ProductSCode.newInstance(CHILD_SCODE_1);
        ProductOffering child1Offering = ProductOfferingFixture.aProductOffering()
                                                               .withProductIdentifier(new ProductIdentifier(CHILD_SCODE_1, CHILD_SCODE_1, "1"))
                                                               .withAttributes(
                                                                   anRfqAttribute().called("C1ATT1").withAllowedValues(newInstance("MANCHESTER UNITED FC"),
                                                                                                                       newInstance("CHELSEA FC"),
                                                                                                                       newInstance("MANCHESTER CITY FC"),
                                                                                                                       newInstance("ARSENAL FC")).build(),
                                                                   anRfqAttribute().called("C1ATT2").withDefaultValue(DEFAULT_VALUE).build(),
                                                                   anInvisibleRfqAttribute().called("C1ATT3").build())
                                                               .withSalesRelationship(SalesRelationshipFixture.aSalesRelationship()
                                                                                                              .withRelationName("grandchild")
                                                                                                              .withRelationType(RelationshipType.Child)
                                                                                                              .withProductIdentifier(GRAND_CHILD_SCODE, GRAND_CHILD_SCODE))
                                                               .withSalesRelationship(SalesRelationshipFixture.aSalesRelationship()
                                                                                                              .withRelationName(ACCESS_RELATION)
                                                                                                              .withRelationType(RelationshipType.RelatedTo)
                                                                                                              .withProductIdentifier(accessSCodeStr, accessSCodeName)
                                                                                                              .withCardinality(1, 1, 1))
                                                               .build();
        when(pmrClient.productOffering(child1SCode)).thenReturn(child1Offerings);
        when(child1Offerings.get()).thenReturn(child1Offering);

        //Child2
        Pmr.ProductOfferings child2Offerings=mock(Pmr.ProductOfferings.class);
        ProductSCode child2SCode = ProductSCode.newInstance(CHILD_SCODE_2);
        ProductOffering child2Offering = ProductOfferingFixture.aProductOffering()
                                                               .withProductIdentifier(new ProductIdentifier(CHILD_SCODE_2, CHILD_SCODE_2, "1"))
                                                               .withAttributes(
                                                                   anRfqAttribute().called("C2ATT1").build(),
                                                                   anRfqAttribute().called("C2ATT2").withDefaultValue(DEFAULT_VALUE).build(),
                                                                   anInvisibleRfqAttribute().called("C2ATT3").build())
                                                               .build();
        when(pmrClient.productOffering(child2SCode)).thenReturn(child2Offerings);
        when(child2Offerings.get()).thenReturn(child2Offering);

        //Grand Child
        Pmr.ProductOfferings grandChildOfferings=mock(Pmr.ProductOfferings.class);
        ProductSCode grandchild1SCode = ProductSCode.newInstance(GRAND_CHILD_SCODE);
        ProductOffering grandChild1ffering = ProductOfferingFixture.aProductOffering()
                                                               .withProductIdentifier(new ProductIdentifier(GRAND_CHILD_SCODE, GRAND_CHILD_SCODE,"1"))
                                                               .withAttributes(
                                                                   anRfqAttribute().called("GCATT1").build(),
                                                                   anRfqAttribute().called("GCATT2").withDefaultValue(DEFAULT_VALUE).build(),
                                                                   anInvisibleRfqAttribute().called("GCATT3").build())
                                                               .build();
        when(pmrClient.productOffering(grandchild1SCode)).thenReturn(grandChildOfferings);
        when(grandChildOfferings.get()).thenReturn(grandChild1ffering);

        //Related Product with Front Catalogue true
        Pmr.ProductOfferings relatedOfferings=mock(Pmr.ProductOfferings.class);
        ProductSCode relatedSCode = ProductSCode.newInstance(RELATED_SCODE_1);

        ProductOffering relatedOffering = ProductOfferingFixture.aProductOffering()
                                                                   .withProductIdentifier(new ProductIdentifier(RELATED_SCODE_1, RELATED_SCODE_1,"1"))
                                                                   .withSalesRelationship(SalesRelationshipFixture.aSalesRelationship()
                                                                                                                  .withRelationName("relatedchild")
                                                                                                                  .withRelationType(RelationshipType.Child)
                                                                                                                  .withProductIdentifier(RELATED_CHILD_SCODE_1, RELATED_CHILD_SCODE_1))
                                                                   .withSalesRelationship(SalesRelationshipFixture.aSalesRelationship()
                                                                                                                  .withRelationName("related")
                                                                                                                  .withRelationType(RelationshipType.RelatedTo)
                                                                                                                  .withProductIdentifier(RELATED_RELATED_SCODE_1, RELATED_RELATED_SCODE_1)
                                                                                                                  .withCardinality(1,1,1))
                                                                   .withAttributes(
                                                                       anRfqAttribute().called("R-ATT1").build(),
                                                                       anRfqAttribute().called("R-ATT2").withDefaultValue(DEFAULT_VALUE).build(),
                                                                       anInvisibleRfqAttribute().called("R-ATT3").build())
                                                                   .withIsInFrontCatalogue(false)
                                                                   .build();
        when(pmrClient.productOffering(relatedSCode)).thenReturn(relatedOfferings);
        when(relatedOfferings.get()).thenReturn(relatedOffering);

        //Related Product with Front Catalogue false
        Pmr.ProductOfferings relatedOfferings2=mock(Pmr.ProductOfferings.class);
        ProductSCode relatedSCode2 = ProductSCode.newInstance(RELATED_SCODE_2);
        String RELATED_CHILD_SCODE_2 = "REL-CHILD-CODE-2";
        String RELATED_RELATED_SCODE_2 = "REL-RELATED-CODE-2";
        ProductOffering relatedOffering2 = ProductOfferingFixture.aProductOffering()
                                                                .withProductIdentifier(new ProductIdentifier(RELATED_SCODE_2, RELATED_SCODE_2,"1"))
                                                                .withSalesRelationship(SalesRelationshipFixture.aSalesRelationship()
                                                                                                               .withRelationName("relatedchild")
                                                                                                               .withRelationType(RelationshipType.Child)
                                                                                                               .withProductIdentifier(RELATED_CHILD_SCODE_2, RELATED_CHILD_SCODE_2))
                                                                .withSalesRelationship(SalesRelationshipFixture.aSalesRelationship()
                                                                                                               .withRelationName("related")
                                                                                                               .withRelationType(RelationshipType.RelatedTo)
                                                                                                               .withProductIdentifier(RELATED_RELATED_SCODE_2, RELATED_RELATED_SCODE_2)
                                                                                                               .withCardinality(1,1,1))
                                                                .withAttributes(
                                                                    anRfqAttribute().called("R-ATT1").build(),
                                                                    anRfqAttribute().called("R-ATT2").withDefaultValue(DEFAULT_VALUE).build(),
                                                                    anInvisibleRfqAttribute().called("R-ATT3").build())
                                                                .withIsInFrontCatalogue(true)
                                                                .build();
        when(pmrClient.productOffering(relatedSCode2)).thenReturn(relatedOfferings2);
        when(relatedOfferings2.get()).thenReturn(relatedOffering2);

        //Related Related Product
        Pmr.ProductOfferings relatedRelatedOfferings=mock(Pmr.ProductOfferings.class);
        ProductSCode relatedRelatedSCode = ProductSCode.newInstance(RELATED_RELATED_SCODE_1);
        ProductOffering relatedRelatedOffering = ProductOfferingFixture.aProductOffering()
                                                                .withProductIdentifier(new ProductIdentifier(RELATED_RELATED_SCODE_1, RELATED_RELATED_SCODE_1,"1"))
                                                                .withAttributes(
                                                                    anRfqAttribute().called("R-R-ATT1").build(),
                                                                    anRfqAttribute().called("R-R-ATT2").withDefaultValue(DEFAULT_VALUE).build(),
                                                                    anInvisibleRfqAttribute().called("R-R-ATT3").build())
                                                                .withIsInFrontCatalogue(false)
                                                                .build();
        when(pmrClient.productOffering(relatedRelatedSCode)).thenReturn(relatedRelatedOfferings);
        when(relatedRelatedOfferings.get()).thenReturn(relatedRelatedOffering);

        //RELATED CHILD
        Pmr.ProductOfferings relatedChildOfferings=mock(Pmr.ProductOfferings.class);
        ProductSCode relatedChildSCode = ProductSCode.newInstance(RELATED_CHILD_SCODE_1);
        ProductOffering relatedChildOffering = ProductOfferingFixture.aProductOffering()
                                                                       .withProductIdentifier(new ProductIdentifier(RELATED_CHILD_SCODE_1, RELATED_CHILD_SCODE_1,"1"))
                                                                       .withAttributes(
                                                                           anRfqAttribute().called("R-C-ATT1").build(),
                                                                           anRfqAttribute().called("R-C-ATT2").withDefaultValue(DEFAULT_VALUE).build(),
                                                                           anInvisibleRfqAttribute().called("R-C-ATT3").build())
                                                                       .build();
        when(pmrClient.productOffering(relatedChildSCode)).thenReturn(relatedChildOfferings);
        when(relatedChildOfferings.get()).thenReturn(relatedChildOffering);

        //Access Offering
        Pmr.ProductOfferings accessOfferings=mock(Pmr.ProductOfferings.class);
        ProductSCode accessSCode = ProductSCode.newInstance(accessSCodeStr);
        ProductOffering accessOffering = ProductOfferingFixture.aProductOffering()
                                                                     .withProductIdentifier(new ProductIdentifier(accessSCodeStr, accessSCodeName,"1"))
                                                                     .withAttributes(
                                                                         anRfqAttribute().called("AC-ATT1").build(),
                                                                         anRfqAttribute().called("AC-ATT2").withDefaultValue(DEFAULT_VALUE).build(),
                                                                         anInvisibleRfqAttribute().called("AC-ATT3").build())
                                                                     .withSimpleProductOfferingType(SimpleProductOfferingType.Bearer)
                                                                     .build();
        when(pmrClient.productOffering(accessSCode)).thenReturn(accessOfferings);
        when(accessOfferings.get()).thenReturn(accessOffering);

    }

    @Test
    public void shouldGenerateExportSheet(){
        BulkTemplateExportSheetOrchestrator exportConfigSheetOrchestrator = new BulkTemplateExportSheetOrchestrator(pmrClient);
        ExcelWorkbook excelWorkbook = exportConfigSheetOrchestrator.buildBulkTemplateExportSheet(rootSCode.getValue(),"QUOTE_OPTION_ID");

        assertNotNull(excelWorkbook);
        assertThat(excelWorkbook.getName(), is(rootSCode.getValue()+".xlsx"));
        validateControlSheet(excelWorkbook);
        validateRelatedProductsSheet(excelWorkbook);
        validateRootProductSheet(excelWorkbook);
        validateChildProductsSheet(excelWorkbook);

        //For generating the Excelsheet uncomment the method and change the Directory Path in Write Method
        /*final XSSFWorkbook xssfWorkbook = excelWorkbook.getFile();
        write(xssfWorkbook, excelWorkbook.getName());*/
    }

    public void validateControlSheet(ExcelWorkbook excelWorkbook){
        String CONTROL_SHEET = "Control Sheet";
        assertThat(excelWorkbook.getFile().getSheetAt(0).getSheetName(),is(CONTROL_SHEET));

        assertThat(excelWorkbook.getFile().getSheetAt(0).getRow(0).getCell(0).getStringCellValue(),is("SCode"));
        assertThat(excelWorkbook.getFile().getSheetAt(0).getRow(0).getCell(1).getStringCellValue(),is("Sheet Name"));

        assertThat(excelWorkbook.getFile().getSheetAt(0).getRow(1).getCell(0).getStringCellValue(),is(RELATED_RELATED_SCODE_1));
        assertThat(excelWorkbook.getFile().getSheetAt(0).getRow(2).getCell(0).getStringCellValue(),is(RELATED_SCODE_1));
        assertThat(excelWorkbook.getFile().getSheetAt(0).getRow(3).getCell(0).getStringCellValue(),is(RELATED_CHILD_SCODE_1));
        assertThat(excelWorkbook.getFile().getSheetAt(0).getRow(4).getCell(0).getStringCellValue(),is(ROOT_SCODE));
        assertThat(excelWorkbook.getFile().getSheetAt(0).getRow(5).getCell(0).getStringCellValue(),is(CHILD_SCODE_1));
        assertThat(excelWorkbook.getFile().getSheetAt(0).getRow(6).getCell(0).getStringCellValue(),is(GRAND_CHILD_SCODE));

    }

    public void validateRelatedProductsSheet(ExcelWorkbook excelWorkbook){
        assertThat(excelWorkbook.getFile().getSheetAt(1).getSheetName(),is(RELATED_RELATED_SCODE_1));
        assertThat(excelWorkbook.getFile().getSheetAt(2).getSheetName(),is(RELATED_SCODE_1));
        assertThat(excelWorkbook.getFile().getSheetAt(3).getSheetName(),is(RELATED_CHILD_SCODE_1));

        assertThat(excelWorkbook.getFile().getSheetAt(1).getRow(0).getCell(0).getStringCellValue(),is("ID"));
        assertThat(excelWorkbook.getFile().getSheetAt(1).getRow(0).getCell(1).getStringCellValue(),is("RELATED TO ID"));
        assertThat(excelWorkbook.getFile().getSheetAt(1).getRow(0).getCell(2).getStringCellValue(),is(SITE_ID));
        assertThat(excelWorkbook.getFile().getSheetAt(1).getRow(0).getCell(3).getStringCellValue(),is("R-R-ATT1"));
        assertThat(excelWorkbook.getFile().getSheetAt(1).getRow(0).getCell(4).getStringCellValue(),is("R-R-ATT2"));

        assertThat(excelWorkbook.getFile().getSheetAt(1).getRow(2).getCell(4).getStringCellValue(),is(DEFAULT_VALUE));

        assertThat(excelWorkbook.getFile().getSheetAt(3).getRow(0).getCell(1).getStringCellValue(),is("PARENT PRODUCT ID"));
    }

    public void validateRootProductSheet(ExcelWorkbook excelWorkbook){
        assertThat(excelWorkbook.getFile().getSheetAt(4).getSheetName(),is(ROOT_SCODE));
        excelWorkbook.getFile().getSheetAt(4).getRow(2).getCell(3);
    }

    public void validateChildProductsSheet(ExcelWorkbook excelWorkbook){
        assertThat(excelWorkbook.getFile().getSheetAt(5).getSheetName(),is(CHILD_SCODE_1));
        assertThat(excelWorkbook.getFile().getSheetAt(6).getSheetName(),is(GRAND_CHILD_SCODE));
        assertThat(excelWorkbook.getFile().getSheetAt(7).getSheetName(),is(CHILD_SCODE_2));
    }


    private void write(XSSFWorkbook xssfWorkbook, String fileName) {
        FileOutputStream out;
        try {
            out = new FileOutputStream("C:\\applications\\RPMDSQE\\" + fileName);
            xssfWorkbook.write(out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
