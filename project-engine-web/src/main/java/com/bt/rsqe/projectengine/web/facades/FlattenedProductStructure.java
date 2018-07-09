package com.bt.rsqe.projectengine.web.facades;

import com.bt.rsqe.domain.product.InstanceCharacteristic;
import com.bt.rsqe.domain.product.InstanceTreeScenario;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.project.InstanceCharacteristicNotFound;
import com.bt.rsqe.domain.project.PriceLine;
import com.google.common.base.Strings;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class FlattenedProductStructure {
    private final MarkedProductInstance rootProductInstance;
    private Map<ProductIdentifier, Set<MarkedProductInstance>> instances;

    protected FlattenedProductStructure(MarkedProductInstance rootProductInstance,
                                        Map<ProductIdentifier, Set<MarkedProductInstance>> instances) {
        this.rootProductInstance = rootProductInstance;
        this.instances = instances;
    }

    public static FlattenedProductStructure flattenProductStructure(MarkedProductInstance productInstance) {
        Map<ProductIdentifier, Set<MarkedProductInstance>> allProducts = new LinkedHashMap<ProductIdentifier, Set<MarkedProductInstance>>();
        populate(productInstance, allProducts);
        return new FlattenedProductStructure(productInstance, allProducts);
    }

    private static void populate(MarkedProductInstance product, Map<ProductIdentifier, Set<MarkedProductInstance>> allProducts) {
        Set<MarkedProductInstance> productInstances = allProducts.get(product.getSourceInstance().getProductIdentifier());
        if (productInstances == null) {
            productInstances = new LinkedHashSet<MarkedProductInstance>();
            allProducts.put(product.getSourceInstance().getProductIdentifier(), productInstances);
        }
        productInstances.add(product);
        for (MarkedProductInstance child : product.getChildren()) {
            populate(child, allProducts);
        }
    }

    public String firstAttributeValueFor(AttributeId attributeId) {
        return firstAttributeValueFor(attributeId.sCode, attributeId.name);
    }

    public String firstAttributeValueFor(String productId, String attributeName) {
        Set<MarkedProductInstance> forCode = instances.get(new ProductIdentifier(productId, "1.0"));
        if (forCode != null) {
            for (MarkedProductInstance instance : forCode) {
                try {
                    InstanceCharacteristic characteristic = instance.getSourceInstance().getInstanceCharacteristic(attributeName);
                    return characteristic.getStringValue();
                } catch (InstanceCharacteristicNotFound icnf) {
                    /*ignore*/
                }
            }
        }
        return "";
    }

    public String nonNullAttributeValueFor(String productId, String attributeName) {
        for (InstanceCharacteristic instanceCharacteristic : instanceCharacteristicsFor(new AttributeId(attributeName, productId))) {
            Object value = instanceCharacteristic.getValue();
            if (value == null) {
                continue;
            }
            return value.toString();
        }
        return "";
    }

    public List<InstanceCharacteristic> instanceCharacteristicsFor(AttributeId attributeId) {
        List<InstanceCharacteristic> instanceCharacteristics = new ArrayList<InstanceCharacteristic>();
        Set<MarkedProductInstance> forCode = instances.get(new ProductIdentifier(attributeId.sCode, "1.0"));
        if (forCode != null) {
            for (MarkedProductInstance instance : forCode) {
                try {
                    instanceCharacteristics.add(instance.getSourceInstance().getInstanceCharacteristic(attributeId.name));
                } catch (InstanceCharacteristicNotFound icnf) {
                    /*ignore*/
                }
            }
        }
        return instanceCharacteristics;
    }


    public void setAttributeValueFor(Attribute attribute) {
        setAttributeValueFor(attribute.sCode(), attribute.name(), attribute.value());
    }

    public void setAttributeValueFor(String productId, String attributeName, String value) {
        if (value != null) {
            Set<MarkedProductInstance> forCode = instances.get(new ProductIdentifier(productId, "1.0"));
            if (forCode != null) {
                for (MarkedProductInstance instance : forCode) {
                    try {
                        InstanceCharacteristic characteristic = instance.getSourceInstance().getInstanceCharacteristic(attributeName);
                        characteristic.setValue(value);
                        instance.mark();
                    } catch (InstanceCharacteristicNotFound icnf) {
                        /*ignore*/
                    }
                }
            }
        }
    }

    public void markAll() {
        for (Set<MarkedProductInstance> set : instances.values()) {
            for (MarkedProductInstance instance : set) {
                instance.mark();
            }
        }
    }

    public MarkedProductInstance getRootProductInstance() {
        return rootProductInstance;
    }

    public String getRootProductCode() {
        return rootProductInstance.getSourceInstance().getProductIdentifier().getProductId();
    }

    public int size() {
        return instances.size();
    }

    public Set<Attribute> rfoAttributes() {
        Set<Attribute> attributes = new HashSet<Attribute>();
        for (Set<MarkedProductInstance> markedProductInstances : instances.values()) {
            for (MarkedProductInstance markedProductInstance : markedProductInstances) {
                addRfoAttributesFor(markedProductInstance, attributes);
            }
        }
        return attributes;
    }

    public String nonNullChargedPriceBookVersion() {
        for (PriceLine priceLine : priceLines()) {
            String chargedBookVersion = priceLine.getChargePrice().getBookVersion();
            if (!Strings.isNullOrEmpty(chargedBookVersion)) {
                return chargedBookVersion;
            }
        }
        return "";
    }

    private List<PriceLine> priceLines() {
        List<PriceLine> allPriceLines = new ArrayList<PriceLine>();
        for (Set<MarkedProductInstance> markedProductInstances : instances.values()) {
            for (MarkedProductInstance markedProductInstance : markedProductInstances) {
                List<PriceLine> priceLines = markedProductInstance.getSourceInstance().getPriceLines();
                for (PriceLine priceLine : priceLines) {
                    allPriceLines.add(priceLine);
                }
            }
        }
        return allPriceLines;
    }

    public String nonNullEupPriceBookVersion() {
        for (PriceLine priceLine : priceLines()) {
            String eupBookVersion = priceLine.getEupPrice().getBookVersion();
            if (!Strings.isNullOrEmpty(eupBookVersion)) {
                return eupBookVersion;
            }
        }
        return "";
    }

    public static class AttributeId implements Comparable<AttributeId> {
        public final String name;
        public final String sCode;

        public AttributeId(String name, String sCode) {
            this.name = name;
            this.sCode = sCode;
        }

        @Override//to disallow duplicates in hashset
        public boolean equals(Object o) {
            return EqualsBuilder.reflectionEquals(this, o);
        }

        @Override//to disallow duplicates in hashset
        public int hashCode() {
            return HashCodeBuilder.reflectionHashCode(this);
        }

        @Override
        public int compareTo(AttributeId o) {
            return this.name.compareTo(o.name);
        }
    }

    public static class Attribute {


        private final AttributeId id;
        private final String value;

        public Attribute(AttributeId id, String value) {
            this.id = id;
            this.value = value;
        }

        public Attribute(String attributeName, String value, String productCode) {
            this.id = new AttributeId(attributeName, productCode);
            this.value = value;
        }

        public String name() {
            return id.name;
        }

        public String value() {
            return value;
        }

        public String sCode() {
            return id.sCode;
        }

        @Override//to disallow duplicates in hashset
        public boolean equals(Object o) {
            return EqualsBuilder.reflectionEquals(this, o, new String[]{"value"});
        }

        @Override//to disallow duplicates in hashset
        public int hashCode() {
            return HashCodeBuilder.reflectionHashCode(this.id);
        }
    }

    private void addRfoAttributesFor(MarkedProductInstance markedProductInstance, Set<Attribute> attributes) {
        List<InstanceCharacteristic> instanceCharacteristics = markedProductInstance.getSourceInstance().getInstanceCharacteristics();
        for (InstanceCharacteristic instanceCharacteristic : instanceCharacteristics) {
            if (instanceCharacteristic.getSpecifiedBy().isRequiredForOrderPhase(InstanceTreeScenario.PROVIDE)) {
                attributes.add(new Attribute(instanceCharacteristic.getName(), instanceCharacteristic.getStringValue(), markedProductInstance.getSourceInstance().getProductIdentifier().getProductId()));
            }
        }
    }

    public Set<AttributeId> rfoAttributeNames() {
        Set<AttributeId> attributeIds = new TreeSet<AttributeId>();
        for (Set<MarkedProductInstance> markedProductInstances : instances.values()) {
            for (MarkedProductInstance markedProductInstance : markedProductInstances) {
                //Getting attributes from product Offering
                List<com.bt.rsqe.domain.product.Attribute> attributes = markedProductInstance.getSourceInstance().whatAttributesShouldIConfigure();
                for (com.bt.rsqe.domain.product.Attribute attribute : attributes) {
                    if (attribute.isForReadyForOrderPhase(InstanceTreeScenario.PROVIDE)) {
                        attributeIds.add(new AttributeId(attribute.getName().getName(), markedProductInstance.getSourceInstance().getProductIdentifier().getProductId()));
                    }
                }
            }
        }
        return attributeIds;
    }
}
