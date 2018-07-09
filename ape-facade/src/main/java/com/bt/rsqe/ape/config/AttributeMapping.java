package com.bt.rsqe.ape.config;

public interface AttributeMapping {
    public String getName();
    public String getMapsToOffering();
    public String getMapping();
    public String getUserVisible();
    public String getTransformer();
    public String getPriority();
    public String getDefaultValue();
    public boolean isApeMapping();
}
