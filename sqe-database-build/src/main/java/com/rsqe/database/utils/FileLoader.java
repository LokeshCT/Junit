package com.rsqe.database.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileLoader {

    public static List<String> asList(String file) throws IOException {
        List<String> reply = new ArrayList<String>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(FileLoader.class.getClassLoader().getResourceAsStream(file)));
        String line;
        while((line = reader.readLine()) != null) {
            reply.add(line.trim());
        }
        reader.close();
        return reply;
    }

    public static Map<String, List<String>> asMultipleList(String file) throws IOException {
        Map<String, List<String>> result = new HashMap<String, List<String>>();
        List<String> reply = new ArrayList<String>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(FileLoader.class.getClassLoader().getResourceAsStream(file)));
        String line="";
        while((line = reader.readLine()) != null) {
            if("LINE_ITEM_DETAIL".equals(line.trim())) {
                break;
            }
            reply.add(line.trim());
        }
        result.put("1ST", reply);
        reply = new ArrayList<String>();
        reply.add(line.trim());
        result.put("2ND", reply);
        reply = new ArrayList<String>();
        while((line = reader.readLine()) != null) {
            reply.add(line.trim());
        }
        result.put("3RD", reply);
        reader.close();
        return result;
    }

}