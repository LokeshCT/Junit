package com.bt.dsl.util;

import com.bt.rsqe.domain.AttachmentDTO;

/**
 * Created with IntelliJ IDEA.
 * User: 608026723
 * Date: 05/10/15
 * Time: 18:48
 * To change this template use File | Settings | File Templates.
 */
public final class Utility {
    private static final String FOLDER_TYPE_IMPORT = "IMPORT";
    private static final String FOLDER_TYPE_FAILURE = "FAILURE";
    private static final String FOLDER_TYPE_TEMPLATE = "TEMPLAT";
    private static final String FOLDER_TYPE_RESULT = "RESULT";

    public static String getSharePointFileName(String docType) {
        String suffix = "";
        if (FOLDER_TYPE_RESULT.equalsIgnoreCase(docType)) {
            suffix = "_Output.xlsx";
        } else if (FOLDER_TYPE_FAILURE.equalsIgnoreCase(docType)) {
            suffix = "_Failure Result.xlsx";
        } else if (FOLDER_TYPE_IMPORT.equalsIgnoreCase(docType)) {
            suffix = ".xlsx";
        }
        return suffix;
    }

    public static AttachmentDTO createAttachmentDTO(byte[] byteArray, String fileName,
                                              String parentPath) throws Exception {
        AttachmentDTO doc = new AttachmentDTO(parentPath);
        doc.setFileName(fileName);
        doc.setFileContent(byteArray);
        return doc;
    }
}
