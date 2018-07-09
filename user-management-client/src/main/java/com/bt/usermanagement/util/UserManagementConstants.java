package com.bt.usermanagement.util;

import com.google.gson.GsonBuilder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by 608143048 on 20/01/2016.
 */
public class UserManagementConstants {
    public static Map<String, String> roleGroupConstants = new HashMap<String, String>();
    static{
        roleGroupConstants.put("NRM_ROLE_GROUP_ID", "1");
        roleGroupConstants.put("CQM_ROLE_GROUP_ID", "2");
        roleGroupConstants.put("EPM_ROLE_GROUP_ID", "3");
        roleGroupConstants.put("CSPMF_ROLE_GROUP_ID", "4");
        roleGroupConstants.put("NRM_ROLE_GROUP_NAME", "NRM");
        roleGroupConstants.put("CQM_ROLE_GROUP_NAME", "CQM");
        roleGroupConstants.put("EPM_ROLE_GROUP_NAME", "EPM");
        roleGroupConstants.put("CSPPMF_ROLE_GROUP_NAME", "CSPPMF");
    }

    public static String roleGroupConstantsBuild() {
        return new GsonBuilder().disableHtmlEscaping().create().toJson(roleGroupConstants);
    }

}
