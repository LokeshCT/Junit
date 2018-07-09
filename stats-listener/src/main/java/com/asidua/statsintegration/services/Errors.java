package com.asidua.statsintegration.services;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Errors {
    private Map<String,String> errorFields = new HashMap<String,String>();
    private static final List<String> EMPTYLIST = new ArrayList<String>(0);
    private String relatingTo = "";
    public Errors(String use){
        relatingTo = use;
    }


    public void add(String field,String message){
        errorFields.put(field,message);
    }

    public boolean hasErrors(){
        return errorFields.isEmpty()?false:true;
    }

    public Map<String, String> getErrorFields() {
        return errorFields;
    }

    public List<String> getMessages(){
        if (!hasErrors()){
            return EMPTYLIST;
        }
        List<String> messages = new ArrayList<String>(errorFields.size());

        for (String fieldKey : errorFields.keySet()) {
            messages.add(fieldKey+" - "+errorFields.get(fieldKey));
        }
        return messages;
    }

    public String getMessagesAsString(){

        String separator = "";
        StringBuilder buff = new StringBuilder("");
        for (String s : getMessages()) {
            buff.append(separator);
            buff.append(s);
            separator = ",";
        }
        return relatingTo+":"+buff.toString();
    }
}
