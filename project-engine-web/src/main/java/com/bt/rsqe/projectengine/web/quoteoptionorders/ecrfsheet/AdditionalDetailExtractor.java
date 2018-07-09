package com.bt.rsqe.projectengine.web.quoteoptionorders.ecrfsheet;

import java.lang.reflect.Field;

public interface AdditionalDetailExtractor<T> {
    String getSheetName();

    T execute();

    T mapToEntity();

    ECRFSheetModelRow pickRowToEvaluate(ECRFSheet sheet, String mappingAttribute);

    boolean validate(Field field, ECRFSheetModelAttribute attribute);

    DeliveryAddressExtractor extractData(ECRFSheetModelRow row, String sheetName);
}
