package com.bt.rsqe.ape.matchers;

import com.bt.rsqe.ape.config.LocalIdentifierMapping;
import com.bt.rsqe.ape.config.OfferingAttribute;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.hamcrest.Description;
import org.junit.matchers.TypeSafeMatcher;

import javax.annotation.Nullable;
import java.util.List;

import static com.google.common.collect.Lists.*;

public class LocalIdentifierMappingMatcher extends TypeSafeMatcher<LocalIdentifierMapping> {

    private String id;
    private List<String> offeringAttributeNames = newArrayList();

    public static LocalIdentifierMappingMatcher aLocalIdentifierMapping() {
        return new LocalIdentifierMappingMatcher();
    }

    public LocalIdentifierMappingMatcher withId(String id) {
        this.id = id;
        return this;
    }

    public LocalIdentifierMappingMatcher withOfferingAttribute(String name) {
        offeringAttributeNames.add(name);
        return this;
    }

    @Override
    public boolean matchesSafely(LocalIdentifierMapping localIdentifierMapping) {
        return localIdentifierMapping.getId().equals(id)
                    && offeringAttributeNames.size() == localIdentifierMapping.getOfferingAttributeConfig().length
                    && allAttributesFound(localIdentifierMapping.getOfferingAttributeConfig());
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("local identifier mapping that has an ID ").appendText(id);
    }

    private boolean allAttributesFound(OfferingAttribute[] offeringAttributes) {
        return newArrayList(Iterables.transform(newArrayList(offeringAttributes), new Function<OfferingAttribute, String>() {
            @Override
            public String apply(@Nullable OfferingAttribute offeringAttribute) {
                return offeringAttribute.getName();
            }
        })).containsAll(offeringAttributeNames);
    }
}
