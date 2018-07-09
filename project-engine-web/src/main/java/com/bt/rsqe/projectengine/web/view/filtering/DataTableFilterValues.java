package com.bt.rsqe.projectengine.web.view.filtering;

import com.google.common.base.Strings;

import javax.ws.rs.core.MultivaluedMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.*;

public class DataTableFilterValues implements FilterValues {
    private static final List<String> singleValueFilters = newArrayList(LineItemGlobalSearchFilter.GLOBAL_SEARCH_FILTER);
    private Map<String, String> filterValues = new HashMap<String, String>();

    private DataTableFilterValues() {
    }

    @Override
    public boolean isThereAnyFilterValues() {
        return !filterValues.isEmpty();
    }

    @Override
    public String getValue(String fieldName) {
        String filterValue = filterValues.get(fieldName);
        if (filterValue == null) {
            return "";
        }
        return filterValue;
    }

    // Splits sSearch values sent by calls from the front end on the '|' character.
    private void parseFilterValuesFrom(MultivaluedMap<String, String> queryParams) {
        String searchParameter = queryParams.getFirst("sSearch");
        if (searchParameter != null) {
            String[] params = searchParameter.split("\\|");

            for (String param : params) {
                String[] nameValue = param.split("=");
                if (nameValue.length > 1 && !nameValue.equals("")) {
                    filterValues.put(nameValue[0], nameValue[1]);
                }
            }
        }

        for(String singleValueFilter : singleValueFilters) {
            String filterValue = queryParams.getFirst(singleValueFilter);

            if(!Strings.isNullOrEmpty(filterValue)) {
                filterValues.put(singleValueFilter, filterValue);
            }
        }
    }

    public static FilterValues parse(MultivaluedMap<String, String> queryParams) {
        final DataTableFilterValues dataTableFilterValues = new DataTableFilterValues();
        dataTableFilterValues.parseFilterValuesFrom(queryParams);
        return dataTableFilterValues;
    }
}
