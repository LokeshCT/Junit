package com.bt.rsqe.projectengine.web.view.sorting;


import javax.ws.rs.core.MultivaluedMap;
import java.util.HashMap;
import java.util.Map;

public class DataTableSortValues implements SortValues {
    private Map<String, String> sortValues = new HashMap<String, String>();

    private DataTableSortValues() {
    }

    @Override
    public String getSortColumnIndexString() {
        return "iSortCol_0";
    }

    @Override
    public String getSortDirectionString() {
        return "sSortDir_0";
    }

    @Override
    public String getValue(String key) {
        return sortValues.get(key);
    }

    private void parseSortValuesFrom(MultivaluedMap<String, String> queryParams) {
        String sortColumnIndex = queryParams.getFirst(getSortColumnIndexString());
        String sortDirection = queryParams.getFirst(getSortDirectionString());

        sortValues.put(getSortColumnIndexString(), sortColumnIndex);
        sortValues.put(getSortDirectionString(), sortDirection);
    }

    public static SortValues parse(MultivaluedMap<String, String> queryParams) {
        final DataTableSortValues dataTableSortValues = new DataTableSortValues();
        dataTableSortValues.parseSortValuesFrom(queryParams);
        return dataTableSortValues;
    }
}
