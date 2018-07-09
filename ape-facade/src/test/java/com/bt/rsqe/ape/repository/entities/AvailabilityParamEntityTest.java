package com.bt.rsqe.ape.repository.entities;

import com.bt.rsqe.util.AbstractPOJOTest;
import org.junit.Test;

/**
 * Created by 605783162 on 10/08/2015.
 */
public class AvailabilityParamEntityTest extends AbstractPOJOTest {

    @Override
    protected void addCustomTestValues() {
        addTestValue(AvailabilityParamEntity.class,new AvailabilityParamEntity());
        addTestValue(AvailabilitySetEntity.class, new AvailabilitySetEntity());
    }

    @Test
    public void shouldCheckGettersAndSetters() throws Exception {
        testPOJO(AvailabilityParamEntity.class);
    }

}