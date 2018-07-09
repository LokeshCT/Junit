package com.bt.dsl.excel;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created with IntelliJ IDEA.
 * User: 607937181
 * Date: 25/08/15
 * Time: 17:13
 * To change this template use File | Settings | File Templates.
 */


@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelColumnAnnotation {
    String columnName();

    int columnSize() default 20;

    int columnPosition() default 0;
}
