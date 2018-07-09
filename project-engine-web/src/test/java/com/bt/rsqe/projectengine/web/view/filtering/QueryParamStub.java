package com.bt.rsqe.projectengine.web.view.filtering;

import javax.ws.rs.core.MultivaluedMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class QueryParamStub implements MultivaluedMap<String, String> {
    private Map<String, List<String>> queryParams = new HashMap<String, List<String>>();

    public void putSingle(String key, String value) {
        queryParams.put(key, Arrays.asList(value));
    }

    public void add(String key, String value) {
        List<String> values = queryParams.get(key);
        values.add(value);
    }

    public String getFirst(String key) {
        List<String> strings = queryParams.get(key);
        if(strings == null){
            return null;
        }
        return strings.get(0);
    }

    @Override
    public void addAll(String key, String... newValues) {
    }

    @Override
    public void addAll(String key, List<String> valueList) {
    }

    @Override
    public void addFirst(String key, String value) {
    }

    @Override
    public boolean equalsIgnoreValueOrder(MultivaluedMap<String, String> otherMap) {
        return false;
    }

    public int size() {
        return queryParams.size();
    }

    public boolean isEmpty() {
        return queryParams.isEmpty();
    }

    public boolean containsKey(Object key) {
        return queryParams.containsKey(key);
    }

    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException();
    }

    public List<String> get(Object key) {
        return queryParams.get(key);
    }

    public List<String> put(String key, List<String> value) {
        return queryParams.put(key, value);
    }

    public List<String> remove(Object key) {
        return queryParams.remove(key);
    }

    public void putAll(Map<? extends String, ? extends List<String>> m) {
        throw new UnsupportedOperationException();
    }

    public void clear() {
        queryParams.clear();
    }

    public Set<String> keySet() {
        return queryParams.keySet();
    }

    public Collection<List<String>> values() {
        return queryParams.values();
    }

    public Set<Entry<String, List<String>>> entrySet() {
        return queryParams.entrySet();
    }
}
