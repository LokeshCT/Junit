package com.bt.cqm.utils;

import com.bt.rsqe.web.rest.dto.ErrorDTO;

import javax.ws.rs.core.GenericEntity;

/**
 * Created with IntelliJ IDEA.
 * User: 608026723
 * Date: 3/23/15
 * Time: 7:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class Utility {

    public static GenericEntity<ErrorDTO> buildGenericError(String msg){

        return new GenericEntity<ErrorDTO>(new ErrorDTO(msg)){};
    }

    public static String buildSharePointFolderStructure(String salesChannelName, String customerId) {
        Long custID = Long.parseLong(customerId);
        long lower = (custID / 5000) * 5000;
        long higher = lower + 5000;
        String range = lower + 1 + "-" + higher;
        String finalPath;

        finalPath = salesChannelName + "/" + range + "/" + custID;

        return finalPath;

    }
}
