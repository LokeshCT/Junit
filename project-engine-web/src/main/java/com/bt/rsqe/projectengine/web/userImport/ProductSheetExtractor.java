package com.bt.rsqe.projectengine.web.userImport;

import com.bt.rsqe.domain.product.Attribute;
import com.bt.rsqe.domain.product.AttributeGroup;
import com.bt.rsqe.domain.product.InstanceCharacteristic;
import com.bt.rsqe.domain.product.constraints.AllowedValuesProvider;
import com.bt.rsqe.domain.product.constraints.AttributeValue;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.product.parameters.RelationshipGroupName;
import com.bt.rsqe.domain.product.parameters.SalesRelationship;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.projectengine.web.productconfigurator.model.BulkConfigAttributeGroup;
import com.bt.rsqe.projectengine.web.quoteoptionorders.ecrfsheet.ECRFImportException;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.bt.rsqe.projectengine.web.userImport.UserImportUtil.*;
import static com.bt.rsqe.utils.AssertObject.*;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Maps.*;
import static com.google.common.collect.Sets.*;

public class ProductSheetExtractor extends AbstractProductSheetDataExtractor {

    @ProductModelTemplate(name = "Site Name", allowNull = false, priority = 1, locked = true)
    protected String siteName;
    @ProductModelTemplate(name = "Site / Service Instance", allowNull = false, priority = 3, locked = true)
    protected String serviceInstance;
    @ProductModelTemplate(name = "Site Id", allowNull = false, priority = 2, locked = true)
    protected String siteId;
    @ProductModelTemplate(name = "City", allowNull = false, priority = 4, locked = true)
    protected String city;
    @ProductModelTemplate(name = "Country", allowNull = false, priority = 5, locked = true)
    protected String country;

    private static final String CONTROL_SHEET = "Control Sheet";
    private static final String STENCIL = "STENCIL";
    private static final String RFQ = "RFQ";

    private boolean fromOffering;
    private ProductInstance productInstance;
    private Map<AttributeGroup, List<InstanceCharacteristic>> characteristicMap;
    private Map<AttributeGroup, List<Attribute>> offeringAttributesMap;
    private Multimap<RelationshipGroupName, SalesRelationship> groupRelationshipMap;
    private Map<ProductIdentifier, List<SalesRelationship>> nonGroupRelationshipMap;
    private Map<ProductIdentifier, List<SalesRelationship>> stenciledRelationshipMap;

    private int columnIndex = -1;
    private int columnHeaderIndex = -1;

    public ProductSheetExtractor(XSSFWorkbook workbook, ListValidationBuilder validationBuilder, UserImportExcelStyler styler) {
        super(workbook, validationBuilder, styler);
    }

    @Override
    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public void setServiceInstance(String serviceInstance) {
        this.serviceInstance = serviceInstance;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void fromOffering(boolean flag) {
        this.fromOffering = flag;
    }

    public void setProductInstance(ProductInstance productInstance) {
        this.productInstance = productInstance;
    }

    public Map<AttributeGroup, List<InstanceCharacteristic>> getCharacteristicMap() {
        return characteristicMap;
    }

    public void setCharacteristicMap(Map<AttributeGroup, List<InstanceCharacteristic>> characteristicMap) {
        this.characteristicMap = characteristicMap;
    }

    public Map<AttributeGroup, List<Attribute>> getAttributesMap() {
        return offeringAttributesMap;
    }

    public void setAttributesMap(Map<AttributeGroup, List<Attribute>> attributesMap) {
        this.offeringAttributesMap = attributesMap;
    }

    public Multimap<RelationshipGroupName, SalesRelationship> getGroupRelationships() {
        return groupRelationshipMap;
    }

    public void setGroupRelationships(Multimap<RelationshipGroupName, SalesRelationship> groupRelationshipMap) {
        this.groupRelationshipMap = groupRelationshipMap;
    }

    public Map<ProductIdentifier, List<SalesRelationship>> getNonGroupedRelationships() {
        return nonGroupRelationshipMap;
    }

    public void setNonGroupRelationships(Map<ProductIdentifier, List<SalesRelationship>> nonGroupedSalesRelationships) {
        this.nonGroupRelationshipMap = nonGroupedSalesRelationships;
    }

    public Map<ProductIdentifier, List<SalesRelationship>> getStenciledRelationshipMap() {
        return stenciledRelationshipMap;
    }

    public void setStenciledRelationshipMap(Map<ProductIdentifier, List<SalesRelationship>> stenciledRelationships) {
        this.stenciledRelationshipMap = stenciledRelationships;
    }

    private int currentIndex() {
        return columnIndex;
    }

    private int nextIndex() {
        return ++columnIndex;
    }

    private int currentHeaderIndex() {
        return columnHeaderIndex;
    }

    private int nextHeaderIndex() {
        return ++columnHeaderIndex;
    }

    @Override
    protected void createHeaderRows(Sheet sheet) {

        HeaderRows headerRows = new HeaderRows(sheet);

        // Static Headers
        Field[] declaredFields = this.getClass().getDeclaredFields();
        List<Field> fields = Arrays.asList(declaredFields);
        Collections.sort(fields, new PriorityComparator());
        for (Field field : fields) {
            if (field.isAnnotationPresent(ProductModelTemplate.class)) {
                try {
                    final ProductModelTemplate annotation = field.getAnnotation(ProductModelTemplate.class);
                    createCell(headerRows.getHeaderRow(), nextHeaderIndex(), annotation.name());
                } catch (Exception e) {
                    throw new ECRFImportException(e.getMessage());
                }
            }
        }

        // Dynamic Headers
        if (fromOffering) {
            buildAttributeHeader(headerRows);
        } else {
            buildCharacteristicHeader(headerRows);
        }
        buildRelationshipsHeader(headerRows, getNonGroupedRelationships());
        buildRelationshipsHeader(headerRows, getStenciledRelationshipMap());
        buildGroupedRelationshipHeader(headerRows);
        headerRows.applyHeaderStyle(styler);
    }

    @Override
    protected void createDataRows(Sheet sheet) {

        Row row = sheet.createRow(sheet.getLastRowNum() + 1);
        populateTemplateData(row);

        if (fromOffering) {
            populateAttributeData(row, styler.buildStyle(StyleConfiguration.NORMAL_STYLE));
        } else {
            populateCharacteristicData(row, styler.buildStyle(StyleConfiguration.NORMAL_STYLE));
        }
        populateRelationshipData(row);
        populateGroupRelationshipData(row);
    }

    private void populateTemplateData(Row row) {

        Field[] declaredFields = this.getClass().getDeclaredFields();
        List<Field> fields = Arrays.asList(declaredFields);
        Collections.sort(fields, new PriorityComparator());

        for (Field field : fields) {
            if (field.isAnnotationPresent(ProductModelTemplate.class)) {
                try {
                    final ProductModelTemplate annotation = field.getAnnotation(ProductModelTemplate.class);
                    final CellStyle cellStyle = true == annotation.locked() ? styler.buildStyle(StyleConfiguration.GREY_STYLE) : styler.buildStyle(StyleConfiguration.NORMAL_STYLE);
                    cellStyle.setLocked(annotation.locked());
                    setFieldValues(field, row, nextIndex(), cellStyle);
                    setCellWidths(row);
                } catch (Exception e) {
                    throw new ECRFImportException(e.getMessage());
                }
            }
        }
    }

    private void setFieldValues(Field field, Row row, int index, CellStyle style) throws Exception {

        final Class<?> fieldType = field.getType();
        if (Long.class == fieldType) {
            createCell(row, index, String.valueOf(field.getLong(this)), style);
        } else if (String.class == fieldType) {
            createCell(row, index, (String) field.get(this), style);
        } else if (Date.class == fieldType) {
            createCell(row, index, (String) field.get(this), style);
        } else if (Double.class == fieldType) {
            createCell(row, index, String.valueOf(field.getDouble(this)), style);
        } else {
            throw new Exception("Unsupported Field Type");
        }
    }

    private void buildCharacteristicHeader(HeaderRows headerRow) {

        for (AttributeGroup attributeGroup : getCharacteristicMap().keySet()) {
            int firstColumn = currentHeaderIndex() + 1;
            for (InstanceCharacteristic instanceCharacteristic : getCharacteristicMap().get(attributeGroup)) {
                if (!instanceCharacteristic.isRfo()) {
                    constructCharacteristicHeader(headerRow, nextHeaderIndex(), instanceCharacteristic.getDisplayName(), instanceCharacteristic.getName(), instanceCharacteristic.isRfq(), STENCIL.equals(instanceCharacteristic.getName()));
                }
            }
            String attributeGroupName = attributeGroup.getGroupName();
            createMergeCell(headerRow.getGroupNameRow(), attributeGroupName, firstColumn, currentHeaderIndex() + 1, 0, 0);
        }
    }

    private void buildAttributeHeader(HeaderRows headerRow) {

        for (AttributeGroup attributeGroup : getAttributesMap().keySet()) {
            int firstColumn = currentHeaderIndex() + 1;
            for (Attribute attribute : getAttributesMap().get(attributeGroup)) {
                final boolean isStencil = STENCIL.equals(attribute.getName().getName());
                if (attribute.isRfq() && (!attribute.isHidden() || isStencil)) {
                    constructCharacteristicHeader(headerRow, nextHeaderIndex(), attribute.getDisplayName(), attribute.getName().getName(), attribute.isRfq(), isStencil);
                }
            }
            String attributeGroupName = attributeGroup.getGroupName();
            createMergeCell(headerRow.getGroupNameRow(), attributeGroupName, firstColumn, currentHeaderIndex() + 1, 0, 0);
        }
    }

    private void constructCharacteristicHeader(HeaderRows row, int index, String headerName, String name, boolean isRfq, boolean isStencil) {

        createCell(row.getHeaderRow(), index, headerName);
        createCell(row.getAttributeNameRow(), index, name);

        final String groupType = isStencil ? BulkConfigAttributeGroup.GroupType.STENCIL.name() : BulkConfigAttributeGroup.GroupType.BASE_CONFIG.name();
        createCell(row.getHeaderMetaRow(), index, groupType);

        if (isRfq) {
            createCell(row.getRfqRow(), index, RFQ);
        }
    }

    private void buildRelationshipsHeader(HeaderRows headerRow, Map<ProductIdentifier, List<SalesRelationship>> relationshipMap) {

        if (isNotNull(relationshipMap)) {
            for (ProductIdentifier productIdentifier : relationshipMap.keySet()) {
                List<String> tempNameList = newArrayList();
                int firstColumn = currentHeaderIndex() + 1;
                for (SalesRelationship salesRelationship : relationshipMap.get(productIdentifier)) {
                    String groupType;
                    switch (salesRelationship.getType()) {
                        case Child:
                            groupType = BulkConfigAttributeGroup.GroupType.PARENT_CHILD.name();
                            break;
                        case RelatedTo:
                            groupType = BulkConfigAttributeGroup.GroupType.RELATED_TO.name();
                            break;
                        default:
                            groupType = salesRelationship.getType().value();
                            break;
                    }
                    String displayName = salesRelationship.getRelationshipName().getDisplayName();
                    if (!tempNameList.contains(displayName)) {
                        tempNameList.add(displayName);
                        createCell(headerRow.getHeaderRow(), nextHeaderIndex(), displayName);
                        createCell(headerRow.getHeaderMetaRow(), currentHeaderIndex(), groupType);
                        createCell(headerRow.getAttributeNameRow(), currentHeaderIndex(), salesRelationship.getRelationshipName().value());
                    }
                }
                createMergeCell(headerRow.getGroupNameRow(), productIdentifier.getDisplayName(), firstColumn, currentHeaderIndex() + 1, 0, 0);
            }
        }
    }

    private void buildGroupedRelationshipHeader(HeaderRows headerRow) {

        for (RelationshipGroupName attributeGroup : getGroupRelationships().keySet()) {
            createCell(headerRow.getGroupNameRow(), nextHeaderIndex(), attributeGroup.getValue());
            createCell(headerRow.getHeaderRow(), currentHeaderIndex(), attributeGroup.getValue());
            createCell(headerRow.getHeaderMetaRow(), currentHeaderIndex(), BulkConfigAttributeGroup.GroupType.RELATIONSHIP_GROUP.name());
            createCell(headerRow.getAttributeNameRow(), currentHeaderIndex(), attributeGroup.getValue());
        }
    }

    private void populateAttributeData(Row row, CellStyle style) {

        for (AttributeGroup attributeGroup : getAttributesMap().keySet()) {
            for (Attribute attribute : getAttributesMap().get(attributeGroup)) {
                if (attribute.isRfq() && (!attribute.isHidden() || STENCIL.equals(attribute.getName().getName()))) {
                    if (attribute.getAllowedValuesWithCaptions().isPresent()) {
                        createDropDownCell(row, nextIndex(), attribute, style);
                    } else {
                        createCell(row, nextIndex(), getDefaultValue(attribute), style);
                    }

                    if (!isEmpty(attribute.getPromptValue())) {
                        createCellComment(row, currentIndex(), attribute.getPromptValue());
                    }
                }
            }
        }
    }

    private void createDropDownCell(Row row, int index, Attribute attribute, CellStyle style) {

        List<AttributeValue> attributeValues = attribute.getAllowedValuesWithCaptions().get();
        List<String> allowedValues = UserImportUtil.transformAttributeValues(attributeValues);
        DataValidation dataValidation = listValidationBuilder.buildHiddenCell(allowedValues, index, row);
        chooseDefaultForAttribute(row, index, attribute, style);
        row.getSheet().addValidationData(dataValidation);
    }

    private void chooseDefaultForAttribute(Row row, int cellIndex, Attribute attribute, CellStyle style) {
        //Create a cell for ListValidations to apply Style
        Cell cell = row.createCell(cellIndex);
        cell.setCellStyle(style);

        String selectedValue = getDefaultValue(attribute);
        if (isNotNull(selectedValue)) {
            cell.setCellValue(selectedValue);
        }
    }

    private void chooseDefaultValueForInstanceCharacteristic(Row row, int cellIndex, InstanceCharacteristic instanceCharacteristic, CellStyle style) {
        //Create a cell for ListValidations to apply Style
        Cell cell = row.createCell(cellIndex);
        cell.setCellStyle(style);
        String selectedValue = !isEmpty(instanceCharacteristic.getStringValue()) ? getCaptionValueFromCharacteristic(instanceCharacteristic) : getDefaultValue(instanceCharacteristic);
        if (isNotNull(selectedValue)) {
            cell.setCellValue(selectedValue);
        }
    }

    private String getCaptionValueFromCharacteristic(final InstanceCharacteristic instanceCharacteristic) {
        Optional<AllowedValuesProvider> allowedValuesProvider = instanceCharacteristic.getAllowedValuesProvider();
        if (allowedValuesProvider.isPresent()) {
            Optional<AttributeValue> attributeValueOptional = Iterables.tryFind(allowedValuesProvider.get().getAllowedValues(), new Predicate<AttributeValue>() {
                @Override
                public boolean apply(AttributeValue input) {
                    return input.getAsStringValue().equals(instanceCharacteristic.getStringValue());
                }
            });

            if (attributeValueOptional.isPresent()) {
                return attributeValueOptional.get().getCaption();
            }
        }

        return instanceCharacteristic.getStringValue();
    }

    private void populateCharacteristicData(Row row, CellStyle style) {

        for (AttributeGroup attributeGroup : getCharacteristicMap().keySet()) {
            for (InstanceCharacteristic instanceCharacteristic : getCharacteristicMap().get(attributeGroup)) {
                if (!instanceCharacteristic.isRfo()) {
                    if (isDropDownRequired(instanceCharacteristic)) {
                        createDropDownCell(row, nextIndex(), instanceCharacteristic, style);
                    } else {
                        createCell(row, nextIndex(), instanceCharacteristic.getStringValue(), style);
                    }

                    if (!isEmpty(instanceCharacteristic.getPromptValue())) {
                        createCellComment(row, currentIndex(), instanceCharacteristic.getPromptValue());
                    }
                }
            }
        }
    }

    private void createDropDownCell(Row row, int cellIndex, InstanceCharacteristic characteristic, CellStyle style) {

        List<AttributeValue> allowedValues = characteristic.getAllowedValuesProvider().get().getAllowedValues();
        List<String> allowedValueList = UserImportUtil.transformAttributeValues(allowedValues);
        DataValidation dataValidation = listValidationBuilder.buildHiddenCell(allowedValueList, cellIndex, row);
        chooseDefaultValueForInstanceCharacteristic(row, cellIndex, characteristic, style);
        row.getSheet().addValidationData(dataValidation);
    }

    private void populateRelationshipData(Row row) {
        // Normal relationships
        for (ProductIdentifier productIdentifier : getNonGroupedRelationships().keySet()) {
            Set<SalesRelationship> salesRelationships = newLinkedHashSet(getNonGroupedRelationships().get(productIdentifier));
            for (SalesRelationship salesRelationship : salesRelationships) {
                createDropDownCell(row, nextIndex(), newArrayList(salesRelationship.getRelationshipName().getDisplayName()), null, null, false);
            }
        }

        // Stencil Relationships
        final Map<String, List<String>> stenciledRelationships = getStenciledRelationships();
        for (String productName : stenciledRelationships.keySet()) {
            List<String> allowedStencils = stenciledRelationships.get(productName);
            createDropDownCell(row, nextIndex(), allowedStencils, productName, null, true);
        }
    }

    private void createDropDownCell(Row row, int cellIndex, List<String> allowedValues, String productName, CellStyle style, boolean isStencilRelation) {
        DataValidation dataValidation = listValidationBuilder.buildHiddenCell(allowedValues, cellIndex, row);
        Cell cell = row.createCell(cellIndex);
        cell.setCellStyle(style);
        row.getSheet().addValidationData(dataValidation);
        setDefaultAllowedValueFromInstance(fromOffering, isStencilRelation, productInstance, productName, allowedValues, cell);
    }

    private Map<String, List<String>> getStenciledRelationships() {
        Map<String, List<String>> stencilMap = newLinkedHashMap();

        if (stencilMap.isEmpty() && isNotNull(stenciledRelationshipMap)) {
            for (ProductIdentifier productIdentifier : getStenciledRelationshipMap().keySet()) {
                List<SalesRelationship> salesRelationships = getStenciledRelationshipMap().get(productIdentifier);
                for (SalesRelationship salesRelationship : salesRelationships) {
                    if (stencilMap.containsKey(salesRelationship.getRelationshipName().value())) {
                        stencilMap.get(salesRelationship.getRelationshipName().value()).add(salesRelationship.getRelatedProductIdentifier().getStencilId().getProductName().toString());
                    } else {
                        stencilMap.put(salesRelationship.getRelationshipName().value(), newArrayList(salesRelationship.getRelatedProductIdentifier().getStencilId().getProductName().toString()));
                    }
                }
            }
        }

        return stencilMap;
    }

    private void populateGroupRelationshipData(Row row) {
        for (RelationshipGroupName relationshipGroupName : getGroupRelationships().keySet()) {
            Set<SalesRelationship> salesRelationships = newHashSet(getGroupRelationships().get(relationshipGroupName));

            ArrayList<String> values = newArrayList(Iterables.transform(salesRelationships, new Function<SalesRelationship, String>() {
                @Override
                public String apply(SalesRelationship input) {
                    return input.getRelationshipName().getDisplayName();
                }
            }));

            createDropDownCell(row, nextIndex(), values, null, null, false);
        }
    }
}
