package com.bt.rsqe.projectengine.web.userImport;

import java.lang.reflect.Field;
import java.util.Comparator;

public class PriorityComparator implements Comparator<Field> {
    @Override
    public int compare(Field field1, Field field2) {
        int fieldPriority1 = 0;
        int fieldPriority2 = 0;
        if (field1.isAnnotationPresent(ProductModelTemplate.class)) {
            fieldPriority1 = field1.getAnnotation(ProductModelTemplate.class).priority();
        }
        if (field2.isAnnotationPresent(ProductModelTemplate.class)) {
            fieldPriority2 = field2.getAnnotation(ProductModelTemplate.class).priority();
        }
        if (fieldPriority1 < fieldPriority2) {
            return -1;
        } else if (fieldPriority1 > fieldPriority2) {
            return 1;
        } else {
            return 0;
        }
    }
}
