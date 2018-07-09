package com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet;


import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.domain.bom.parameters.ProductInstanceId;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;


public class RFORowModelTest {
    final String childScode = "childScode1";
    final String grandChild1Scode = "grandChildScode";
    final String grandChild2Scode = "grandChildScode";


    @Test
    public void shouldMarshallSheet() {
        final RFOSheetModel.RFORowModel root = createRecursiveRFORowModelWithThreeLevels();
        assertThat(root.getLeafNodeCount(), is(4));
    }



    private RFOSheetModel.RFORowModel createRecursiveRFORowModelWithThreeLevels() {
            RFOSheetModel.RFORowModel child1 = new RFOSheetModel.RFORowModel(new ProductInstanceId("productInstanceId1"), "", childScode);
            child1.addAttribute("Contact Name", "default Contact1");

            RFOSheetModel.RFORowModel grandChild11 = new RFOSheetModel.RFORowModel(new ProductInstanceId("productInstanceIdGrandChild11"), "", grandChild1Scode);
            grandChild11.addAttribute("grand child name", "value11");

            RFOSheetModel.RFORowModel grandChild12 = new RFOSheetModel.RFORowModel(new ProductInstanceId("productInstanceIdGrandChild12"), "", grandChild2Scode);
            grandChild12.addAttribute("grand child name", "value12");

            RFOSheetModel.RFORowModel grandChild21 = new RFOSheetModel.RFORowModel(new ProductInstanceId("productInstanceIdGrandChild21"), "", grandChild1Scode);
            grandChild21.addAttribute("grand child name", "value21");

            RFOSheetModel.RFORowModel grandChild22 = new RFOSheetModel.RFORowModel(new ProductInstanceId("productInstanceIdGrandChild22"), "", grandChild2Scode);
            grandChild22.addAttribute("grand child name", "value22");

            RFOSheetModel.RFORowModel child2 = new RFOSheetModel.RFORowModel(new ProductInstanceId("productInstanceIdTwo"), "", childScode);
            child2.addAttribute("Contact Name", "default Contact2");

            child1.addChild(grandChild1Scode, grandChild11);
            child1.addChild(grandChild2Scode, grandChild12);

            child2.addChild(grandChild1Scode, grandChild21);
            child2.addChild(grandChild1Scode, grandChild22);

            RFOSheetModel.RFORowModel root = new RFOSheetModel.RFORowModel(new LineItemId("lineItemId"), "siteId", "siteName", "rootScode", "summary");
            root.addAttribute("Product Type", "default product type");

            root.addChild(childScode, child1);
            root.addChild(childScode, child2);
            return root;
        }




}
