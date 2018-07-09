package com.bt.rsqe.projectengine.web;

import com.bt.rsqe.domain.AssetKey;
import org.junit.Test;

import static junit.framework.Assert.*;

public class AssetKeyContainerTest {
    @Test
    public void shouldReturnValueWhenKeyPresentInContainer(){
        AssetKeyContainer container = new AssetKeyContainer();
        container.addKey("rowID", AssetKey.newInstance("assetId", 1L));
        assertTrue(container.getAssetKey("rowID").isPresent());
    }

    @Test
    public void shouldReturnAbsentWhenKeyNotPresentInContainer(){
        AssetKeyContainer container = new AssetKeyContainer();
        container.addKey("rowID", AssetKey.newInstance("assetId", 1L));
        assertFalse(container.getAssetKey("someOtherRowId").isPresent());
    }
}
