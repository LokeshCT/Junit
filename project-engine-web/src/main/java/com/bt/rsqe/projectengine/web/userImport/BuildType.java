package com.bt.rsqe.projectengine.web.userImport;

public enum BuildType {
    ROW("Property", "Value", 0),
    COLUMN(null, null, 2);
    private String header1;
    private String header2;
    private int headerRowId;

    BuildType(String header1, String header2, int headerRowId) {
        this.header1 = header1;
        this.header2 = header2;
        this.headerRowId = headerRowId;
    }

    public String getHeader1() {
        return header1;
    }

    public void setHeader1(String header1) {
        this.header1 = header1;
    }

    public String getHeader2() {
        return header2;
    }

    public void setHeader2(String header2) {
        this.header2 = header2;
    }


    public int getHeaderRowId() {
        return headerRowId;
    }
}
