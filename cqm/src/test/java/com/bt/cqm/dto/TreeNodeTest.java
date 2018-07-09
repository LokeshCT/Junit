package com.bt.cqm.dto;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: 608026723
 * Date: 7/28/14
 * Time: 3:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class TreeNodeTest {
    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void shouldCreateNewInstance() throws Exception {
        TreeNode treeNodeObj=new TreeNode("customer","CustomerScreen");
        assert (treeNodeObj!=null);
    }

    @Test
    public void shouldReturnPermittedTreeNoded() throws Exception {
        String treeNodeId="customer";

        TreeNode treeNodeObj=new TreeNode(treeNodeId,"CustomerScreen");
        Set<Long> permittedRoles = new HashSet<Long>();
        permittedRoles.add(1L);
        permittedRoles.add(2L);

        treeNodeObj.addChildNodes(new TreeNode("viewCustomer","View Customer","/customer",permittedRoles,""));

        JsonObject jObj=treeNodeObj.asJson(1L,UserType.DIRECT);
        JsonElement jElem =jObj.get("id");
        assert(jElem.getAsString().equals(treeNodeId));


    }

    @Test
    public void shouldNotReturnUnAuthTreeNoded() throws Exception {
        String treeNodeId="viewCustomer";

        TreeNode treeNodeObj=new TreeNode("customer","CustomerScreen");
        Set<Long> permittedRoles = new HashSet<Long>();
        permittedRoles.add(1L);
        permittedRoles.add(2L);

        treeNodeObj.addChildNodes(new TreeNode(treeNodeId,"View Customer","/customer",permittedRoles,""));

        JsonObject jObj=treeNodeObj.asJson(3L,UserType.DIRECT);
        JsonElement jElem =jObj.get("children");
        JsonArray childElem = jElem.getAsJsonArray();
        assert(childElem.size()==0);


    }




}
