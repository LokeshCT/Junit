package com.bt.rsqe.projectengine.web.facades;

import com.bt.rsqe.domain.product.DefaultProductInstanceFixture;
import com.bt.rsqe.domain.product.InstanceCharacteristic;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.project.PriceLine;
import com.bt.rsqe.integration.PriceLineFixture;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Sets.*;
import static java.util.Arrays.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class FlattenedProductStructureTest {
    @Test
    public void shouldRetriveAllValuesForAGivenAttribute() {
        final FlattenedProductStructure flattenedProductStructure = new FlattenedProductStructure(null, new HashMap<ProductIdentifier, Set<MarkedProductInstance>>() {{
            final Set<MarkedProductInstance> productInstances = newLinkedHashSet(asList(productInstance((String) null), productInstance("value1"), productInstance((String) null), productInstance("value2")));
            put(new ProductIdentifier("prodId", "1.0"), productInstances);
        }});
        assertThat(flattenedProductStructure.nonNullAttributeValueFor("prodId", "foo"), is("value1"));
        assertThat(flattenedProductStructure.nonNullAttributeValueFor("prodId", "bar"), is(""));
        assertThat(flattenedProductStructure.nonNullAttributeValueFor("doesnt-exist", "bar"), is(""));
    }

    @Test
    public void shouldRetriveFirstValueForAGivenAttribute() {
        final FlattenedProductStructure flattenedProductStructure = new FlattenedProductStructure(null, new HashMap<ProductIdentifier, Set<MarkedProductInstance>>() {{
            final Set<MarkedProductInstance> productInstances = newLinkedHashSet(asList(productInstance((String) null), productInstance("value1"), productInstance((String) null), productInstance("value2")));
            put(new ProductIdentifier("prodId", "1.0"), productInstances);
        }});
        assertThat(flattenedProductStructure.firstAttributeValueFor("prodId", "bar"), is(""));
    }

    @Test
    public void shouldGetRFOAttributes() {
        final FlattenedProductStructure flattenedProductStructure = new FlattenedProductStructure(null, new HashMap<ProductIdentifier, Set<MarkedProductInstance>>() {{
            final Set<MarkedProductInstance> productInstances = newLinkedHashSet(asList(productInstance("value1"), rfoProductInstance("value2")));
            put(new ProductIdentifier("prodId", "1.0"), productInstances);
        }});
        assertThat(flattenedProductStructure.rfoAttributes().size(), is(1));
    }

    @Test
    public void shouldReturnSortedSetOfRFOAttributes() {
        final FlattenedProductStructure flattenedProductStructure = new FlattenedProductStructure(null, new HashMap<ProductIdentifier, Set<MarkedProductInstance>>() {{

            final Set<MarkedProductInstance> productInstances = newLinkedHashSet(asList(rfoProductInstance("foo", "", true), rfoProductInstance("bar", "", false), rfoProductInstance("car", "", false)));
            put(new ProductIdentifier("prodId", "1.0"), productInstances);
        }});
        Set<FlattenedProductStructure.AttributeId> attributeIds = flattenedProductStructure.rfoAttributeNames();
        assertThat(attributeIds.size(), is(3));
        FlattenedProductStructure.AttributeId[] attributeIdsArr = attributeIds.toArray(new FlattenedProductStructure.AttributeId[attributeIds.size()]);
        assertThat(attributeIdsArr[0].name, is("bar"));
        assertThat(attributeIdsArr[1].name, is("car"));
        assertThat(attributeIdsArr[2].name, is("foo"));
    }

    @Test
    public void shouldSetAttributeValueForAllMatchingInstances() {
        final FlattenedProductStructure flattenedProductStructure = new FlattenedProductStructure(null, new HashMap<ProductIdentifier, Set<MarkedProductInstance>>() {{
            final Set<MarkedProductInstance> productInstances = newLinkedHashSet(asList(productInstance("value0"), productInstance("value1")));
            put(new ProductIdentifier("prodId", "1.0"), productInstances);
        }});
        FlattenedProductStructure.AttributeId id = new FlattenedProductStructure.AttributeId("foo", "prodId");
        flattenedProductStructure.setAttributeValueFor(new FlattenedProductStructure.Attribute(id, "bar"));
        List<InstanceCharacteristic> instanceCharacteristics = flattenedProductStructure.instanceCharacteristicsFor(id);
        for (InstanceCharacteristic instanceCharacteristic : instanceCharacteristics) {
            assertThat(instanceCharacteristic.getStringValue(), is("bar"));
        }
    }

    @Test
    public void shouldReturnNonNullChargedPriceBookVersionAndEUPPriceBookVersion() {
        //Given
        List<PriceLine> priceLines = asList(new PriceLineFixture().withChargedBookVersion("Charge-1.0").withEupPriceBookVersion("EUP-1.0").build(),
                                            new PriceLineFixture().withChargedBookVersion("Charge-2.0").withEupPriceBookVersion("EUP-2.0").build());
        final DefaultProductInstanceFixture defaultProductInstanceFixture = new DefaultProductInstanceFixture().withPriceLines(priceLines);

        final FlattenedProductStructure flattenedProductStructure = new FlattenedProductStructure(null, new HashMap<ProductIdentifier, Set<MarkedProductInstance>>() {{
            final Set<MarkedProductInstance> productInstances = newLinkedHashSet(asList(productInstance(defaultProductInstanceFixture)));
            put(new ProductIdentifier("prodId", "1.0"), productInstances);
        }});
        //When
        String chargedPriceBookVersion = flattenedProductStructure.nonNullChargedPriceBookVersion();
        String eupPriceBookVersion = flattenedProductStructure.nonNullEupPriceBookVersion();
        //Then
        assertThat(chargedPriceBookVersion, is("Charge-1.0"));
        assertThat(eupPriceBookVersion, is("EUP-1.0"));
    }

    @Test
    public void shouldReturnEmptyStringIfChargedPriceBookVersionNotPresent() {
        //Given
        List<PriceLine> priceLines = asList(new PriceLineFixture().withChargedBookVersion(null).withEupPriceBookVersion(null).build(),
                                            new PriceLineFixture().withChargedBookVersion(null).withEupPriceBookVersion(null).build());
        final DefaultProductInstanceFixture defaultProductInstanceFixture = new DefaultProductInstanceFixture().withPriceLines(priceLines);

        final FlattenedProductStructure flattenedProductStructure = new FlattenedProductStructure(null, new HashMap<ProductIdentifier, Set<MarkedProductInstance>>() {{
            final Set<MarkedProductInstance> productInstances = newLinkedHashSet(asList(productInstance(defaultProductInstanceFixture)));
            put(new ProductIdentifier("prodId", "1.0"), productInstances);
        }});
        //When
        String chargedPriceBookVersion = flattenedProductStructure.nonNullChargedPriceBookVersion();
        String eupPriceBookVersion = flattenedProductStructure.nonNullEupPriceBookVersion();

        //Then
        assertThat(chargedPriceBookVersion, is(""));
        assertThat(eupPriceBookVersion, is(""));
    }

    private MarkedProductInstance productInstance(String value) {
        return new MarkedProductInstance(new DefaultProductInstanceFixture().withAttributeValue("foo", value).build());
    }

    private MarkedProductInstance productInstance(DefaultProductInstanceFixture defaultProductInstanceFixture) {
        return new MarkedProductInstance(defaultProductInstanceFixture.build());
    }

    private MarkedProductInstance rfoProductInstance(String value) {
        return rfoProductInstance("bar", value, false);
    }

    private MarkedProductInstance rfoProductInstance(String name, String value, boolean optional) {
        return new MarkedProductInstance(new DefaultProductInstanceFixture().withRFOAttributeValue(name, value, optional, false).build());
    }
}
