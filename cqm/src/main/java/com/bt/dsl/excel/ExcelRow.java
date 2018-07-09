package com.bt.dsl.excel;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: 607937181
 * Date: 25/08/15
 * Time: 14:44
 * To change this template use File | Settings | File Templates.
 */
public abstract class ExcelRow {

    public List<KeyValueMap> getFields() {

        Class cls = this.getClass();
        Field[] fields = cls.getFields();
        List<KeyValueMap> list = new ArrayList<KeyValueMap>();
        String fieldName = null;
        String methodName = null;


        for (Field field : fields) {

            ExcelColumnAnnotation exc = field.getAnnotation(ExcelColumnAnnotation.class);
            fieldName = field.getName();
            methodName = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
            KeyValueMap keyValueMap = new KeyValueMap(exc.columnPosition(), methodName);

            list.add(keyValueMap);

        }

        return list;
    }

    public List<KeyValueMap> getFieldsWitGetMethod() {

        Class cls = this.getClass();
        Field[] fields = cls.getFields();
        List<KeyValueMap> list = new ArrayList<KeyValueMap>();
        String fieldName = null;
        String methodName = null;


        for (Field field : fields) {

            ExcelColumnAnnotation exc = field.getAnnotation(ExcelColumnAnnotation.class);
            fieldName = field.getName();
            methodName = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
            KeyValueMap keyValueMap = new KeyValueMap(exc.columnPosition(), methodName);

            list.add(keyValueMap);

        }

        return list;
    }

    public abstract XLCellStyle getCellFormat(int colIndx, String value);

    public abstract boolean isRowEmpty();
}
