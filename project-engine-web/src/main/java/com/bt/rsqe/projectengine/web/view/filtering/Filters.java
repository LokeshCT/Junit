package com.bt.rsqe.projectengine.web.view.filtering;

import java.util.ArrayList;
import java.util.List;

public class Filters<T> {
    private List<Filter<T>> filters = new ArrayList<Filter<T>>();

    public Filters<T> add(Filter<T> filter) {
        filters.add(filter);
        return this;
    }

    public List<T> apply(List<T> itemsToFilter) {
        List<T> filteredList = itemsToFilter;

        if (!filters.isEmpty()) {
            filteredList = new ArrayList<T>();
            for (T item : itemsToFilter) {
                boolean ok = true;
                for (Filter filter : filters) {
                    ok &= filter.apply(item);
                    if (!ok) {
                        break;
                    }
                }
                if (ok) {
                    filteredList.add(item);
                }
            }
        }
        return filteredList;
    }

    public interface Filter<T> {
        boolean apply(T t);
    }

}
