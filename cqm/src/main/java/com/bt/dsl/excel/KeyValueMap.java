package com.bt.dsl.excel;

/**
 * Created with IntelliJ IDEA.
 * User: 607937181
 * Date: 26/08/15
 * Time: 14:47
 * To change this template use File | Settings | File Templates.
 */
public class KeyValueMap {
    private String methodName;
    private int columnIndex;

    public KeyValueMap(int columnIndex, String methodName) {
        this.columnIndex = columnIndex;
        this.methodName = methodName;
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    public void setColumnIndex(int columnIndex) {
        this.columnIndex = columnIndex;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }



}
