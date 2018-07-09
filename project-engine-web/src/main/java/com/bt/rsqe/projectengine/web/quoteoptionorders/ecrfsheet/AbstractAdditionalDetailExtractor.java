package com.bt.rsqe.projectengine.web.quoteoptionorders.ecrfsheet;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class AbstractAdditionalDetailExtractor<T> implements AdditionalDetailExtractor {
    public String input;
    public ECRFWorkBook workBook;
    private static SimpleDateFormat dateFormatter = new SimpleDateFormat("mm-dd-yyyy");

    @Override
    public boolean validate(Field field, ECRFSheetModelAttribute attribute) {
        ECRFSheetMapping annotation;
        if (field.isAnnotationPresent(ECRFSheetMapping.class)) {
            try {
                annotation = field.getAnnotation(ECRFSheetMapping.class);
                if (!annotation.allowNull() && attribute.getValue().isEmpty()) {
                    return false;
                }
                return true;
            } catch (Exception e) {
                throw new ECRFImportException(String.format(ECRFImportException.additionalSheetValidationError, attribute.getName()));
            }
        }
        return true;
    }

    @Override
    public DeliveryAddressExtractor extractData(ECRFSheetModelRow row, String sheetName) {
        ECRFSheetMapping annotation;
        DeliveryAddressExtractor deliveryAddressExtractor = new DeliveryAddressExtractor();
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(ECRFSheetMapping.class)) {
                try {
                    annotation = field.getAnnotation(ECRFSheetMapping.class);
                    ECRFSheetModelAttribute attribute = row.getAttributeByName(annotation.name());
                    boolean isAttributeValid = validate(field, attribute);
                    if (null != attribute) {
                        if (!isAttributeValid) {
                            throw new ECRFImportException(String.format(ECRFImportException.additionalDetailAttributeIsNull, attribute.getName(), sheetName));
                        }
                        setValue(field, attribute, deliveryAddressExtractor);
                    }
                } catch (Exception e) {
                    throw new ECRFImportException(e.getMessage());
                }
            }
        }
        return deliveryAddressExtractor;
    }

    private void setValue(Field field, ECRFSheetModelAttribute attribute, DeliveryAddressExtractor deliveryAddressExtractor) throws IllegalAccessException, ECRFImportException {
        try {
            if (field.getType().equals(Long.class)) {
                field.set(deliveryAddressExtractor, Long.valueOf(attribute.getValue()));
            }
            if (field.getType().equals(String.class)) {
                field.set(deliveryAddressExtractor, attribute.getValue());
            }
            if (field.getType().equals(Date.class)) {
                field.set(deliveryAddressExtractor, dateFormatter.parse(attribute.getValue()));
            }
            if (field.getType().equals(Double.class)) {
                field.set(deliveryAddressExtractor, Double.valueOf(attribute.getValue()));
            }
        } catch (NumberFormatException e) {
            throw new ECRFImportException(String.format(ECRFImportException.notAValidNumber, attribute.getValue(), attribute.getName()));
        } catch (ParseException parseException) {
            throw new ECRFImportException(String.format(ECRFImportException.notAValidDate, attribute.getValue(), attribute.getName()));
        }

    }

    public abstract T execute();

    public abstract ECRFSheetModelRow pickRowToEvaluate(ECRFSheet sheet, String mappingAttribute);
}
