package com.bt.rsqe.ape.repository.entities;

import com.bt.rsqe.util.AbstractPOJOTest;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class DslEfmSupportedCountriesEntityTest extends AbstractPOJOTest {
    @Override
    protected void addCustomTestValues() {
        addTestValue(DslEfmSupportedCountriesEntity.class, new DslEfmSupportedCountriesEntity());
    }

    @Test
    public void shouldCheckGettersAndSetters() throws Exception {
        testPOJO(DslEfmSupportedCountriesEntity.class);
    }
}