package com.bt.rsqe.projectengine.web;

import java.util.ArrayList;
import java.util.List;

public class ImportResults extends ArrayList<ImportLineItemError> {

    public List<ImportLineItemError> importErrors() {
        return this;
    }

    public void addError(String sCode, String message) {
        add(new ImportLineItemError(sCode, message));
    }

    public boolean hasErrors(){
        return !isEmpty();
    }
}
