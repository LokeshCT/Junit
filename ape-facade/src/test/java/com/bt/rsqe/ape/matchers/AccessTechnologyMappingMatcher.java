package com.bt.rsqe.ape.matchers;

import com.bt.rsqe.ape.config.AccessTechnologyMapping;
import org.hamcrest.Description;
import org.junit.matchers.TypeSafeMatcher;

public class AccessTechnologyMappingMatcher extends TypeSafeMatcher<AccessTechnologyMapping> {
    private String baseAccessTechnology, id;

    public static AccessTechnologyMappingMatcher anAccessTechnologyMapping() {
        return new AccessTechnologyMappingMatcher();
    }

    public AccessTechnologyMappingMatcher withId(String id) {
        this.id = id;
        return this;
    }

    public AccessTechnologyMappingMatcher withBaseAccessTechnology(String baseAccessTechnology) {
        this.baseAccessTechnology = baseAccessTechnology;
        return this;
    }

    @Override
    public boolean matchesSafely(AccessTechnologyMapping accessTechnologyMapping) {
        return accessTechnologyMapping.getId().equals(id) && accessTechnologyMapping.getBaseAccessTechnology().equals(baseAccessTechnology);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("Access Type Mapping with baseAccessTechnology ").appendValue(baseAccessTechnology).appendText(" and ID ").appendValue(id);
    }
}
