package com.bt.rsqe.projectengine.web.facades;

import com.bt.rsqe.domain.bom.fixtures.StubProductInstanceFixture;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.project.InstanceCharacteristicNotFound;
import com.bt.rsqe.domain.project.ProductInstance;
import com.google.common.collect.Sets;
import org.junit.Test;

import java.util.HashMap;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class ProductInstancesTest {

    @Test
    public void shouldSetAttributeValue() throws InstanceCharacteristicNotFound {

        final ProductInstance rootProductInstance = new StubProductInstanceFixture().build();
        final ProductInstance child1 = new StubProductInstanceFixture().build();
        final ProductInstance child2 = new StubProductInstanceFixture().withProductIdentifier("blah-id","blah-name")
                                                                   .withAttributeValue("blah-attr", "replace-me")
                                                                   .build();
        final FlattenedProductStructure productInstances = new FlattenedProductStructure(new MarkedProductInstance(rootProductInstance),
                                                                                         new HashMap<ProductIdentifier, Set<MarkedProductInstance>>(){{
                                                                                            put(child1.getProductIdentifier(),
                                                                                                Sets.newHashSet(new MarkedProductInstance(child1)));
                                                                                            put(child2.getProductIdentifier(),
                                                                                                Sets.newHashSet(new MarkedProductInstance(child2)));
                                                                                         }});
        productInstances.setAttributeValueFor("blah-id","blah-attr","blah-value");
        assertThat(String.valueOf(child2.getInstanceCharacteristic("blah-attr").getValue()), is("blah-value"));
    }

    @Test
    public void shouldNotSetAttributeValue() throws InstanceCharacteristicNotFound {

        final ProductInstance rootProductInstance = new StubProductInstanceFixture().build();
        final ProductInstance child1 = new StubProductInstanceFixture().build();
        final ProductInstance child2 = new StubProductInstanceFixture().withProductIdentifier("blah-id","blah-name")
                                                                   .withAttributeValue("blah-attr", "replace-me")
                                                                   .build();
        final FlattenedProductStructure productInstances = new FlattenedProductStructure(new MarkedProductInstance(rootProductInstance),
                                                                                         new HashMap<ProductIdentifier, Set<MarkedProductInstance>>(){{
                                                                                            put(child1.getProductIdentifier(),
                                                                                                Sets.newHashSet(new MarkedProductInstance(child1)));
                                                                                            put(child2.getProductIdentifier(),
                                                                                                Sets.newHashSet(new MarkedProductInstance(child2)));
                                                                                         }});
        productInstances.setAttributeValueFor("blah-id","blah-attr",null);
        assertThat(String.valueOf(child2.getInstanceCharacteristic("blah-attr").getValue()), is("replace-me"));
    }
}
