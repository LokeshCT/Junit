package com.bt.rsqe.customerinventory;

import com.bt.rsqe.customerinventory.dto.SpecialPriceBookDTO;
import com.bt.rsqe.customerinventory.parameter.QuoteOptionId;
import com.bt.rsqe.domain.project.SpecialPriceBook;
import com.bt.rsqe.utils.countries.Country;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SpecialPriceBooks {

    private Map<SpecialPriceBooks.Key, SpecialPriceBookDTO> priceBooks;

    public SpecialPriceBooks() {
        priceBooks = Collections.synchronizedMap(new HashMap<Key, SpecialPriceBookDTO>());
    }

    public void add(SpecialPriceBook priceBook) {
        SpecialPriceBookDTO dto = new SpecialPriceBookDTO(priceBook);
        put(new Key(dto), dto);
    }

    public void add(SpecialPriceBookDTO dto) {
        put(new Key(dto), dto);
    }

    public SpecialPriceBookDTO put(Key key, SpecialPriceBookDTO value) {
        return priceBooks.put(key, value);
    }

    public SpecialPriceBookDTO remove(Key key) {
        return priceBooks.remove(key);
    }

    public void putAll(Map<? extends Key, ? extends SpecialPriceBookDTO> m) {
        for (Map.Entry<? extends Key, ? extends SpecialPriceBookDTO> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    public void clear() {
        priceBooks.clear();
    }

    public Set<Key> keySet() {
        return priceBooks.keySet();
    }

    public Collection<SpecialPriceBookDTO> values() {
        return priceBooks.values();
    }

    public Set<Map.Entry<Key, SpecialPriceBookDTO>> entrySet() {
        return priceBooks.entrySet();
    }

    @Override
    public boolean equals(Object o) {
        return priceBooks.equals(o);
    }

    @Override
    public int hashCode() {
        return priceBooks.hashCode();
    }

    public int size() {
        return priceBooks.size();
    }

    public boolean isEmpty() {
        return priceBooks.isEmpty();
    }

    public boolean containsKey(Key key) {
        return priceBooks.containsKey(key);
    }

    public boolean containsValue(SpecialPriceBookDTO value) {
        return priceBooks.containsValue(value);
    }

    public SpecialPriceBookDTO get(Key key) {
        return priceBooks.get(key);
    }

    public static class Key {
        private String quoteOptionId;
        private Country country;

        public Key(QuoteOptionId quoteOptionId, Country country) {
            this.quoteOptionId = quoteOptionId == null ? null : quoteOptionId.toString();
            this.country = country;
        }

        public Key(SpecialPriceBookDTO priceBook) {
            quoteOptionId = priceBook.getQuoteOptionId();
            country = priceBook.getCountry();
        }

        public Key(SpecialPriceBook priceBook) {
            quoteOptionId = priceBook.getQuoteOptionId();
            country = priceBook.getCountry();
        }

        @Override
        public boolean equals(Object o) {
            return EqualsBuilder.reflectionEquals(this, o);
        }

        @Override
        public int hashCode() {
            return HashCodeBuilder.reflectionHashCode(this);
        }
    }
}
