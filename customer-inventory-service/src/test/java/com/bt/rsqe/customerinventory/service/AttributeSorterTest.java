package com.bt.rsqe.customerinventory.service;

import com.bt.rsqe.domain.product.Attribute;
import org.junit.Test;

import java.util.List;

import static com.bt.rsqe.domain.bom.fixtures.AttributeFixture.*;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Sets.*;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;

public class AttributeSorterTest {

    @Test
    public void shouldSortAttributeBasedOnItsDependencyOverAnotherAttribute() {
        //Given

        //Attribute B depends on C
        //Attribute A depends on B,E
        //Attribute D depends on A,E

        Attribute attr_E = anAttribute().called("E").build();
        Attribute attr_C = anAttribute().called("C").build();
        Attribute attr_B = anAttribute().called("B").dependsOn(newHashSet(attr_C)).build();
        Attribute attr_A = anAttribute().called("A").dependsOn(newHashSet(attr_B, attr_E)).build();
        Attribute attr_D = anAttribute().called("D").dependsOn(newHashSet(attr_A, attr_E)).build();

        //When
        List<Attribute> sortedAttributes = new AttributeSorter().sort(newArrayList(attr_C, attr_D, attr_A, attr_B, attr_E));

        //Then
        assertThat(sortedAttributes.get(0).getName().getName(), is("C"));
        assertThat(sortedAttributes.get(1).getName().getName(), is("E"));
        assertThat(sortedAttributes.get(2).getName().getName(), is("B"));
        assertThat(sortedAttributes.get(3).getName().getName(), is("A"));
        assertThat(sortedAttributes.get(4).getName().getName(), is("D"));
    }

    @Test
    public void shouldNotRecursivelyAddDependenciesWhenAnAttributeIsDependentonItself() {
        //Given

        Attribute attr_D = anAttribute().called("D").build();
        attr_D.dependsOn(newHashSet(attr_D));

        //When
        List<Attribute> sortedAttributes = new AttributeSorter().sort(newArrayList(attr_D));

        //Then
        assertThat(sortedAttributes.get(0).getName().getName(), is("D"));
    }
}