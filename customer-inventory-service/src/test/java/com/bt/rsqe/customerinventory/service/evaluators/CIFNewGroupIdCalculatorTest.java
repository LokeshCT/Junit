package com.bt.rsqe.customerinventory.service.evaluators;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.externals.PmrHelper;
import com.bt.rsqe.domain.bom.parameters.AttributeAction;
import com.bt.rsqe.domain.product.Association;
import com.bt.rsqe.domain.product.AttributeAssociations;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.expressionevaluator.NewGroupId;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.mockito.Mockito.*;


public class CIFNewGroupIdCalculatorTest {
    private static final String attrToUpdate = "SPLIT ORDER QUANTITY";

    private static String SPLIT_ORDER_QUANTITY = "4";
    CIFAsset productInstance;
    CIFAsset productInstance1;
    CIFAsset productInstance2;
    CIFAsset productInstance3;
    CIFAsset productInstance4;
    PmrHelper pmrHelper;
    ProductOffering productOffering;

    @Before
    public void setUp() {
        productInstance = mock(CIFAsset.class);
        productInstance1 = mock(CIFAsset.class);
        productInstance2 = mock(CIFAsset.class);
        productInstance3 = mock(CIFAsset.class);
        productInstance4 = mock(CIFAsset.class);
        pmrHelper = mock(PmrHelper.class);
        productOffering = mock(ProductOffering.class);

        when(pmrHelper.getProductOffering(any(CIFAsset.class))).thenReturn(productOffering);
        Set<Association> attributeAssociationList = newHashSet();
        when(productOffering.getAttributeAssociations()).thenReturn(attributeAssociationList);
    }

    @Test
    public void shouldUpdateNewGroupIdAsQuadIfSplitOrderQuantityIs4() {
        new CIFNewGroupIdCalculator(pmrHelper).calculate(newArrayList(productInstance, productInstance1, productInstance2, productInstance3), SPLIT_ORDER_QUANTITY, attrToUpdate);

        verify(productInstance, times(1)).updateCharacteristicValue(attrToUpdate, NewGroupId.Quad.name());
        verify(productInstance1, times(1)).updateCharacteristicValue(attrToUpdate, NewGroupId.Quad.name());
        verify(productInstance2, times(1)).updateCharacteristicValue(attrToUpdate, NewGroupId.Quad.name());
        verify(productInstance3, times(1)).updateCharacteristicValue(attrToUpdate, NewGroupId.Quad.name());
    }

    @Test
    public void shouldUpdateNewGroupIdAsQuadIfSplitOrderQuantityIs2() {
        SPLIT_ORDER_QUANTITY = "2";
        new CIFNewGroupIdCalculator(pmrHelper).calculate(newArrayList(productInstance, productInstance1), SPLIT_ORDER_QUANTITY, attrToUpdate);
        verify(productInstance, times(1)).updateCharacteristicValue(attrToUpdate, NewGroupId.Twin.name());
        verify(productInstance1, times(1)).updateCharacteristicValue(attrToUpdate, NewGroupId.Twin.name());
    }

    @Test
    public void shouldUpdateNewGroupIdAsQuadIfSplitOrderQuantityIs1() {
        SPLIT_ORDER_QUANTITY = "1";
        new CIFNewGroupIdCalculator(pmrHelper).calculate(newArrayList(productInstance), SPLIT_ORDER_QUANTITY, attrToUpdate);
        verify(productInstance, times(1)).updateCharacteristicValue(attrToUpdate, NewGroupId.Single.name());
    }

    @Test
    public void shouldUpdateNewGroupIdAsQuadForFourAssetsAndSingleForOneAsssetIfSplitOrderQuantityIs5() {
        SPLIT_ORDER_QUANTITY = "5";
        new CIFNewGroupIdCalculator(pmrHelper).calculate(newArrayList(productInstance, productInstance1, productInstance2, productInstance3, productInstance4), SPLIT_ORDER_QUANTITY, attrToUpdate);

        verify(productInstance, times(1)).updateCharacteristicValue(attrToUpdate, NewGroupId.Quad.name());
        verify(productInstance1, times(1)).updateCharacteristicValue(attrToUpdate, NewGroupId.Quad.name());
        verify(productInstance2, times(1)).updateCharacteristicValue(attrToUpdate, NewGroupId.Quad.name());
        verify(productInstance3, times(1)).updateCharacteristicValue(attrToUpdate, NewGroupId.Quad.name());
        verify(productInstance4, times(1)).updateCharacteristicValue(attrToUpdate, NewGroupId.Single.name());
    }

    // If Split Order qunatity is 7 then
    // four assets should be quad and two assets should be Twin and one asset should be single
    @Test
    public void shouldUpdateNewGroupIdIfSplitOrderQuantityIs7() {
        CIFAsset productInstance5 = mock(CIFAsset.class);
        CIFAsset productInstance6 = mock(CIFAsset.class);
        SPLIT_ORDER_QUANTITY = "7";
        new CIFNewGroupIdCalculator(pmrHelper).calculate(newArrayList(productInstance, productInstance1, productInstance2, productInstance3, productInstance4, productInstance5, productInstance6), SPLIT_ORDER_QUANTITY, attrToUpdate);

        verify(productInstance, times(1)).updateCharacteristicValue(attrToUpdate, NewGroupId.Quad.name());
        verify(productInstance1, times(1)).updateCharacteristicValue(attrToUpdate, NewGroupId.Quad.name());
        verify(productInstance2, times(1)).updateCharacteristicValue(attrToUpdate, NewGroupId.Quad.name());
        verify(productInstance3, times(1)).updateCharacteristicValue(attrToUpdate, NewGroupId.Quad.name());
        verify(productInstance4, times(1)).updateCharacteristicValue(attrToUpdate, NewGroupId.Twin.name());
        verify(productInstance5, times(1)).updateCharacteristicValue(attrToUpdate, NewGroupId.Twin.name());
        verify(productInstance6, times(1)).updateCharacteristicValue(attrToUpdate, NewGroupId.Single.name());
    }
}
