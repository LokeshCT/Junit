package com.bt.rsqe.projectengine.web.userImport;

import com.bt.rsqe.projectengine.web.quoteoptionorders.ecrfsheet.ECRFImportException;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.bt.rsqe.projectengine.web.userImport.UserImportUtil.*;

public class HeaderSheetExtractor extends AbstractProductSheetDataExtractor {

    @ProductModelTemplate(name = "Spreadsheet Template Version", allowNull = false, priority = 1)
    protected String templateVersion;
    @ProductModelTemplate(name = "Customer Name", allowNull = false, priority = 2)
    protected String customerName;
    @ProductModelTemplate(name = "Quote Name", allowNull = false, priority = 3)
    protected String quoteName;
    @ProductModelTemplate(name = "Quote ID", priority = 4)
    protected String quoteId;
    @ProductModelTemplate(name = "Quote Status", priority = 5)
    protected String quoteStatus;
    @ProductModelTemplate(name = "Currency", priority = 6)
    protected String currency;
    @ProductModelTemplate(name = "Contract Term", priority = 7)
    protected String contractTerm;
    @ProductModelTemplate(name = "Contract ID", priority = 8)
    protected String contractId;

    private int index = -1;

    private static final int DATA_ROW_COUNT = 8;
    private static final String PROPERTY_COLUMN = "Property";
    private static final String VALUE_COLUMN = "Value";
    private static final String HEADER_SHEET = "Header";

    public HeaderSheetExtractor(XSSFWorkbook workbook, ListValidationBuilder listValidationBuilder, UserImportExcelStyler styler) {
        super(workbook, listValidationBuilder, styler);
    }

    @Override
    public String getSheetName() {
        return HEADER_SHEET;
    }

    private int nextIndex() {
        return ++index;
    }

    public void setTemplateVersion(String templateVersion) {
        this.templateVersion = templateVersion;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public void setQuoteName(String quoteName) {
        this.quoteName = quoteName;
    }

    public void setQuoteId(String quoteId) {
        this.quoteId = quoteId;
    }

    public void setQuoteStatus(String quoteStatus) {
        this.quoteStatus = quoteStatus;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setContractTerm(String contractTerm) {
        this.contractTerm = contractTerm;
    }

    public void setContractId(String contractId) {
        this.contractId = contractId;
    }

    @Override
    protected void createHeaderRows(Sheet sheet) {

        CellStyle style = styler.buildStyle(StyleConfiguration.PALE_BLUE_HEADER);
        Row headerRow = sheet.createRow(nextIndex());
        createCell(headerRow, 0, PROPERTY_COLUMN, style);
        createCell(headerRow, 1, VALUE_COLUMN, style);
        setCellWidths(headerRow);
    }

    @Override
    protected void createDataRows(Sheet sheet) {

        if (DATA_ROW_COUNT == sheet.getLastRowNum()) {
            // Avoid re-creating header data rows when already available
            return;
        }

        Field[] declaredFields = this.getClass().getDeclaredFields();
        List<Field> fields = Arrays.asList(declaredFields);
        Collections.sort(fields, new PriorityComparator());
        for (Field field : fields) {
            if (field.isAnnotationPresent(ProductModelTemplate.class)) {
                try {
                    final ProductModelTemplate annotation = field.getAnnotation(ProductModelTemplate.class);
                    final Row dataRow = sheet.createRow(nextIndex());
                    setFieldValues(field, dataRow, annotation, styler.buildStyle(StyleConfiguration.NORMAL_STYLE));
                    setCellWidths(dataRow);
                } catch (Exception e) {
                    throw new ECRFImportException(e.getMessage());
                }
            }
        }
    }

    private void setFieldValues(Field field, Row row, ProductModelTemplate annotation, CellStyle style) throws Exception {

        style.setLocked(annotation.locked());

        final Class<?> fieldType = field.getType();
        if (Long.class == fieldType) {
            createCell(row, 0, annotation.name(), style);
            createCell(row, 1, String.valueOf(field.getLong(this)), style);
        } else if (String.class == fieldType) {
            createCell(row, 0, annotation.name(), style);
            createCell(row, 1, (String) field.get(this), style);
        } else if (Date.class == fieldType) {
            createCell(row, 0, annotation.name(), style);
            createCell(row, 1, (String) field.get(this), style);
        } else if (Double.class == fieldType) {
            createCell(row, 0, annotation.name(), style);
            createCell(row, 1, String.valueOf(field.getDouble(this)), style);
        } else {
            throw new Exception("Unsupported Field Type");
        }
    }
}
