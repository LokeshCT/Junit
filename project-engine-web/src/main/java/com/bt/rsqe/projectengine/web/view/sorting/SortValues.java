package com.bt.rsqe.projectengine.web.view.sorting;

public interface SortValues {
    String getSortColumnIndexString();
    String getSortDirectionString();
    String getValue(String fieldName);
}
