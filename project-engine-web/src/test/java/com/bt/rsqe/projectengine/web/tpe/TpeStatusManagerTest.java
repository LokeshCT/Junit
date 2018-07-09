package com.bt.rsqe.projectengine.web.tpe;

import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.dto.AssetDTO;
import com.bt.rsqe.customerinventory.fixtures.AssetDTOFixture;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.domain.product.DefaultProductInstanceFixture;
import com.bt.rsqe.domain.product.parameters.RelationshipName;
import com.bt.rsqe.domain.project.DefaultProductInstance;
import com.bt.rsqe.domain.project.PricingStatus;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.tpe.TpeException;
import com.bt.rsqe.tpe.client.PricingTpeClient;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TpeStatusManagerTest {
    private TpeStatusManager tpeStatusManager;
    private PricingTpeClient pricingTpeClient;
    private ProductInstanceClient productInstanceClient;

    @Before
    public void setup() {
        pricingTpeClient = mock(PricingTpeClient.class);
        productInstanceClient = mock(ProductInstanceClient.class);
        tpeStatusManager = new TpeStatusManager(pricingTpeClient, productInstanceClient, null);
    }

    @Test
    public void shouldReturnTrueForAssetThatIsNotNonStandard() throws Exception {
        returnAsset("aLineItemId", anAsset("A1", false, PricingStatus.FIRM));
        assertTrue(tpeStatusManager.cancelSiteForLineItemIfRequired("aLineItemId"));
    }

    @Test
    public void shouldReturnTrueForNonStandardAssetThatHasANotApplicableForSiteRemovalPricingStatus() throws Exception {
        returnAsset("aLineItemId", anAsset("A1",false, PricingStatus.REJECTED));
        assertTrue(tpeStatusManager.cancelSiteForLineItemIfRequired("aLineItemId"));

        returnAsset("aLineItemId", anAsset("A1",false, PricingStatus.PARTIALLY_APPROVED));
        assertTrue(tpeStatusManager.cancelSiteForLineItemIfRequired("aLineItemId"));

        returnAsset("aLineItemId", anAsset("A1",false, PricingStatus.REFUSED));
        assertTrue(tpeStatusManager.cancelSiteForLineItemIfRequired("aLineItemId"));
    }

    @Test
    public void shouldReturnTrueForNonStandardAssetButNotNonStandardInstance() throws Exception {
        final AssetDTO asset = anAsset("A1",true, PricingStatus.FIRM);
        returnAsset("aLineItemId", asset);
        returnInstance(asset, false, "");
        assertTrue(tpeStatusManager.cancelSiteForLineItemIfRequired("aLineItemId"));
    }

    @Test
    public void shouldCancelSiteForNonStandardRootAssetAndReturnTrue() throws Exception {
        final AssetDTO asset = anAsset("A1",true, PricingStatus.FIRM);
        returnAsset("aLineItemId", asset);
        ProductInstance instance = returnInstance(asset, true, "aSpecialBidId");
        returnRemoveSite(instance, true);

        assertTrue(tpeStatusManager.cancelSiteForLineItemIfRequired("aLineItemId"));

        verifyRemoveSite(instance);
    }

    @Test
    public void shouldCancelSiteForNonStandardChildAssetAndReturnTrue() throws Exception {
        final AssetDTO child = anAsset("A2", true, PricingStatus.FIRM);
        ProductInstance childInstance = returnInstance(child, true, "aChildSpecialBidId");
        returnRemoveSite(childInstance, true);

        final AssetDTO root = anAsset("A1", true, PricingStatus.FIRM, child);
        returnAsset("aLineItemId", root);
        ProductInstance rootInstance = returnInstance(root, true, "aSpecialBidId");
        returnRemoveSite(rootInstance, true);

        assertTrue(tpeStatusManager.cancelSiteForLineItemIfRequired("aLineItemId"));

        verifyRemoveSite(rootInstance);
        verifyRemoveSite(childInstance);
    }

    @Test
    public void shouldUseSourceVersionOverInstanceVersionIfItExists() throws Exception {
        final AssetDTO asset = anAsset("A1",true, PricingStatus.FIRM);
        returnAsset("aLineItemId", asset);
        ProductInstance instance = returnInstance(asset, true, "aSpecialBidId");
        doReturn(5L).when(instance).getProductInstanceVersion();
        returnRemoveSite(instance, true);

        assertTrue(tpeStatusManager.cancelSiteForLineItemIfRequired("aLineItemId"));

        verifyRemoveSite(instance);
    }

    @Test
    public void shouldReturnFalseWhenSiteCancelFails() throws Exception {
        final AssetDTO asset = anAsset("A1",true, PricingStatus.FIRM);
        returnAsset("aLineItemId", asset);
        ProductInstance instance = returnInstance(asset, true, "aSpecialBidId");
        returnRemoveSite(instance, false);

        assertFalse(tpeStatusManager.cancelSiteForLineItemIfRequired("aLineItemId"));

        verifyRemoveSite(instance);
    }

    @Test
    public void shouldReturnFalseWhenSiteCancelThrowsAnException() throws Exception {
        final AssetDTO asset = anAsset("A1",true, PricingStatus.FIRM);
        returnAsset("aLineItemId", asset);
        ProductInstance instance = returnInstance(asset, true, "aSpecialBidId");

        when(pricingTpeClient.RSQE_TPE_Remove_Site(getUniqueId(instance),
                                                   instance.getSpecialBidId()))
            .thenThrow(TpeException.class);

        assertFalse(tpeStatusManager.cancelSiteForLineItemIfRequired("aLineItemId"));

        verifyRemoveSite(instance);
    }

    private void verifyRemoveSite(ProductInstance instance) {
        verify(pricingTpeClient).RSQE_TPE_Remove_Site(getUniqueId(instance), instance.getSpecialBidId());
    }

    private String getUniqueId(ProductInstance instance) {
        return instance.getProductInstanceId().getValue()
                   + "_"
                   + (instance.getAssetSourceVersion() != null ? instance.getAssetSourceVersion() : instance.getProductInstanceVersion());
    }

    private void returnRemoveSite(ProductInstance instance, boolean success) {
        when(pricingTpeClient.RSQE_TPE_Remove_Site(getUniqueId(instance),
                                                   instance.getSpecialBidId()))
            .thenReturn(success);
    }

    private AssetDTO anAsset(String id, boolean specialBid, PricingStatus pricingStatus, AssetDTO... children) {
        final AssetDTOFixture assetDTOFixture = AssetDTOFixture.anAsset().withPricingStatus(pricingStatus).withId(id);
        for(int i = 0; i < children.length; i++) {
            assetDTOFixture.withChild(children[i], RelationshipName.newInstance("relationship" + i));
        }
        final AssetDTO root = spy(assetDTOFixture.build());
        doReturn(specialBid).when(root).isSpecialBid();
        return root;
    }

    private void returnAsset(String lineItemId, AssetDTO asset) {
        when(productInstanceClient.getAssetDTO(new LineItemId(lineItemId))).thenReturn(asset);
    }

    private ProductInstance returnInstance(AssetDTO asset, boolean specialBid, String specialBidId) {
        final DefaultProductInstance instance = spy(DefaultProductInstanceFixture.aProductInstance()
                                                                                 .withProductInstanceId(asset.getId())
                                                                                 .withProductInstanceVersion(asset.getVersion())
                                                                                 .build());
        doReturn(specialBid).when(instance).isSpecialBid();
        doReturn(specialBidId).when(instance).getSpecialBidId();
        when(productInstanceClient.convertAssetToLightweightInstance(asset)).thenReturn(instance);
        return instance;
    }
}
