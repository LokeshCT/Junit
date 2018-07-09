package com.bt.rsqe.projectengine.web.userImport;

import com.bt.rsqe.domain.product.Attribute;
import com.bt.rsqe.domain.product.InstanceCharacteristic;
import com.bt.rsqe.domain.product.constraints.AllowedValuesProvider;
import com.bt.rsqe.domain.product.constraints.AttributeValue;
import com.bt.rsqe.domain.project.ProductInstance;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;

import java.util.List;

import static com.bt.rsqe.utils.AssertObject.*;
import static com.google.common.collect.Lists.*;

public class UserImportUtil {

    public static String getDefaultValue(InstanceCharacteristic instanceCharacteristic) {
        if (instanceCharacteristic.getSpecifiedBy().hasDefaultValue()) {
            return instanceCharacteristic.getSpecifiedBy().getDefaultValue().getValue().toString();
        }
        return null;
    }

    public static String getDefaultValue(Attribute attribute) {
        if (isNotNull(attribute.getDefaultValue().getValue())) {
            return attribute.getDefaultValue().getValue().toString();
        }
        return "";
    }

    public static boolean isDropDownRequired(InstanceCharacteristic instanceCharacteristic) {
        Optional<AllowedValuesProvider> allowedValuesProvider = instanceCharacteristic.getAllowedValuesProvider();
        if (allowedValuesProvider.isPresent()) {
            if (!allowedValuesProvider.get().getAllowedValues().isEmpty() && allowedValuesProvider.get().getAllowedValues().size() > 1) {
                return true;
            }
        }
        return false;
    }

    public static List<String> transformAttributeValues(List<AttributeValue> allowedValues) {
        return newArrayList(Iterables.transform(allowedValues, new Function<AttributeValue, String>() {
            @Override
            public String apply(AttributeValue input) {
                return input.getCaption();
            }
        }));
    }

    public static void setDefaultAllowedValueFromInstance(boolean fromOffering, boolean isStencilRelation, ProductInstance productInstance, String productName, List<String> allowedValues, Cell cell) {
        if (!fromOffering) {
            if (isStencilRelation) {
                for (String allowedStencil : allowedValues) {
                    if (productInstance.isRelationshipWithStencilExists(allowedStencil) && productInstance.isRelationshipByNameExists(productName)) {
                        cell.setCellValue(allowedStencil);
                    }
                }
            } else {
                for (String allowedValue : allowedValues) {
                    if (productInstance.isRelationshipByNameExists(allowedValue)) {
                        cell.setCellValue(allowedValue);
                    }
                }
            }
        }
    }

    public static void setCellWidths(Row row) {
        for (int i = 0; i < row.getLastCellNum(); i++) {
            row.getSheet().autoSizeColumn(i);
        }
    }

    public static void setStyleForAllCells(Row row, CellStyle style) {
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (null == cell) {
                row.createCell(i).setCellStyle(style);
            } else {
                cell.setCellStyle(style);
            }
        }
    }
}
