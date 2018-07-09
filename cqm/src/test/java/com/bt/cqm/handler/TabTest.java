package com.bt.cqm.handler;

import com.bt.cqm.dto.TreeNode;
import com.bt.cqm.dto.UserType;
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
 * Time: 3:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class TabTest {
    Tab tab;

    @Before
    public void setUp(){

    }

    @Test
    public void shouldTransformToJson() throws Exception {
        Set<Long> actionRoles =new HashSet<Long>();
        String tabId ="customer";
        actionRoles.add(1L);
        Set<Long> tabRoles =new HashSet<Long>();
        tabRoles.add(1L);
        tab = new Tab(tabId,"Customer Screen","", new TreeNode("viewCustomer","View Customer","/customer",actionRoles,"direct"),tabRoles,"direct");


        JsonObject jObj=tab.asJson(1L, UserType.DIRECT,true);

        JsonElement retObj=jObj.get("id");
        assert(retObj.getAsString().equals(tabId));

    }

    @Test
    public void shouldTransformToJsonHandleInvalidInput() throws Exception {
        Set<Long> actionRoles =new HashSet<Long>();
        String tabId ="customer";
        actionRoles.add(1L);
        Set<Long> tabRoles =new HashSet<Long>();
        tabRoles.add(1L);
        tab = new Tab(tabId,"Customer Screen","", new TreeNode("viewCustomer","View Customer","/customer",actionRoles,"direct"),tabRoles,"direct");


        JsonObject jObj=tab.asJson(null,null,true);

        assert(jObj == null);

    }

    @Test
    public void shouldTransformReturnNullForExemptedRoleId() throws Exception {
        Set<Long> actionRoles =new HashSet<Long>();
        Long availableRole =1L;
        Long unAvailableRole =2L;
        String tabId ="customer";
        actionRoles.add(availableRole);
        Set<Long> tabRoles =new HashSet<Long>();
        tabRoles.add(availableRole);
        tab = new Tab(tabId,"Customer Screen","", new TreeNode("viewCustomer","View Customer","/customer",actionRoles,"indirect"),tabRoles,"indirect");


        JsonObject jObj=tab.asJson(unAvailableRole,UserType.INDIRECT,true);
        assert(jObj==null);

    }
}
