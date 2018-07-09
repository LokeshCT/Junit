package com.bt.rsqe.customerinventory.service;

import com.bt.rsqe.domain.product.Attribute;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.*;

public class AttributeSorter {

    public static final int ATTRIBUTE_DEPENDENCY_COUNT = 1;

    public List<Attribute> sort(List<Attribute> attributes) {
        try {
            List<Attribute> sortedAttributes = newArrayList();
            List<Attribute> dependableAttributes = newArrayList();

            for (Attribute attribute : attributes) {
                if (attribute.getDependsOnAttributesRecursively(ATTRIBUTE_DEPENDENCY_COUNT).isEmpty()) {
                    sortedAttributes.add(attribute);
                } else {
                    dependableAttributes.add(attribute);
                }
            }

            sortedAttributes.addAll(sortDependableCharacteristics(dependableAttributes));
            return Collections.unmodifiableList(sortedAttributes);
        } catch (Exception e) {
            return attributes;
        }

    }

    private List<Attribute> sortDependableCharacteristics(List<Attribute> dependableAttributes) {
        for (int characteristicPos = 0; characteristicPos < dependableAttributes.size(); characteristicPos++) {
            if (!dealingLastElement(characteristicPos, dependableAttributes.size())) {
                Attribute currentAttribute = dependableAttributes.get(characteristicPos);
                Attribute nextAttribute = dependableAttributes.get(characteristicPos + 1);

                Set<Attribute> currentAttributeDependencies = currentAttribute.getDependsOnAttributesRecursively(ATTRIBUTE_DEPENDENCY_COUNT);

                if (currentAttributeDependencies.contains(nextAttribute)) {
                    Collections.swap(dependableAttributes, characteristicPos, characteristicPos + 1);
                    sortDependableCharacteristics(dependableAttributes);
                }
            }
        }
        return dependableAttributes;
    }

    private boolean dealingLastElement(int charPos, int charsSize) {
        return charsSize == charPos + 1;
    }

}
