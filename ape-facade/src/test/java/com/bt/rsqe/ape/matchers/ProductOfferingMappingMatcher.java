package com.bt.rsqe.ape.matchers;

import com.bt.rsqe.ape.config.ProductOfferingMapping;
import org.hamcrest.Description;
import org.junit.matchers.TypeSafeMatcher;

public class ProductOfferingMappingMatcher extends TypeSafeMatcher<ProductOfferingMapping> {

    private String relationName, id;

    public static ProductOfferingMappingMatcher aProductOfferingMapping() {
        return new ProductOfferingMappingMatcher();
    }

    public ProductOfferingMappingMatcher withId(String id) {
        this.id = id;
        return this;
    }

    public ProductOfferingMappingMatcher withRelationName(String relationName) {
        this.relationName = relationName;
        return this;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("product Offering Mapping with Id ").appendValue(id)
                   .appendText(" and relationName").appendValue(relationName);
    }

    @Override
    public boolean matchesSafely(ProductOfferingMapping productOfferingMapping) {
        return productOfferingMapping.getId().equals(id)
               && productOfferingMapping.getRelationshipName().equals(relationName);
    }
}
