package com.bt.rsqe.ape.matchers;

import com.bt.rsqe.ape.config.AttributeMapping;
import org.hamcrest.Description;
import org.junit.matchers.TypeSafeMatcher;

public class AttributeMappingMatcher extends TypeSafeMatcher<AttributeMapping> {
    private String userVisible, name, mapping, mapsToOffering;

    public static AttributeMappingMatcher anAttributeMapping() {
        return new AttributeMappingMatcher();
    }

    public AttributeMappingMatcher withUserVisible(String userVisible) {
        this.userVisible = userVisible;
        return this;
    }

    public AttributeMappingMatcher withName(String name) {
        this.name = name;
        return this;
    }

    public AttributeMappingMatcher withMapsToOffering(String mapsToOffering) {
        this.mapsToOffering = mapsToOffering;
        return this;
    }

    public AttributeMappingMatcher withMapping(String mapping) {
        this.mapping = mapping;
        return this;
    }

    @Override
    public boolean matchesSafely(AttributeMapping attributeMapping) {
        return match(userVisible, attributeMapping, "getUserVisible")
                    && match(name, attributeMapping, "getName")
                    && match(mapsToOffering, attributeMapping, "getMapsToOffering")
                    && match(mapping, attributeMapping, "getMapping");
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("Attribute Mapping with with userVisible ")
                   .appendValue(userVisible)
                   .appendText(", name ")
                   .appendValue(name)
                   .appendText(", mapsToOffering ")
                   .appendValue(mapsToOffering)
                   .appendText(" and mapping ")
                   .appendValue(mapping);
    }

    private boolean match(Object expected, AttributeMapping mapping, String method) {
        if(null == expected) {
            return makeOptional(mapping, method) == null;
        } else if(null == makeOptional(mapping, method)) {
            return expected == null;
        } else {
            return expected.equals(makeOptional(mapping, method));
        }
    }

    private String makeOptional(AttributeMapping mapping, String method) {
        try {
            return (String)AttributeMapping.class.getMethod(method).invoke(mapping);
        } catch (Exception e) {
            // This is ok.  The parameter might not be in the XML file (as its optional)
        }

        return null;
    }
}
