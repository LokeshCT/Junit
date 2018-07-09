package com.bt.rsqe.projectengine.web.quoteoptionorders.ecrfsheet;

import com.bt.rsqe.domain.bom.parameters.ProductInstanceId;
import com.bt.rsqe.domain.product.parameters.RelationshipName;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.Is.*;

public class AssetRelationshipMapTest {
    public AssetRelationshipMap firstMap = new AssetRelationshipMap(new ProductInstanceId("Inst1"), RelationshipName.newInstance("RelName1"));
    public AssetRelationshipMap equalsMap = new AssetRelationshipMap(new ProductInstanceId("Inst1"), RelationshipName.newInstance("RelName1"));
    public AssetRelationshipMap unequalMap1 = new AssetRelationshipMap(new ProductInstanceId("Inst1"), RelationshipName.newInstance("RelName2"));
    public AssetRelationshipMap unequalMap2 = new AssetRelationshipMap(new ProductInstanceId("Inst2"), RelationshipName.newInstance("RelName1"));
    public AssetRelationshipMap unequalMap3 = new AssetRelationshipMap(new ProductInstanceId("Inst2"), RelationshipName.newInstance("RelName2"));

    @Test
    public void shouldCalculateEqualsCorrectly() throws Exception {
        assertThat(firstMap, is(equalsMap));
        assertThat(firstMap, not(is(unequalMap1)));
        assertThat(firstMap, not(is(unequalMap2)));
        assertThat(firstMap, not(is(unequalMap3)));
    }
}
