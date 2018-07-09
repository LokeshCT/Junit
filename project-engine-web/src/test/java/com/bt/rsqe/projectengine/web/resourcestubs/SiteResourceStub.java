package com.bt.rsqe.projectengine.web.resourcestubs;

import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.customerrecord.SiteResource;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Maps.*;

public class SiteResourceStub extends SiteResource {

    private Map<String, SiteDTO> sites = newHashMap();

    public SiteResourceStub() {
        super(URI.create(""));
    }

    public SiteResourceStub with(SiteDTO site) {
        sites.put(site.bfgSiteID, site);
        return this;
    }

    public List<SiteDTO> storedItemList() {
        return newArrayList(sites.values());
    }

    @Override
    public List<SiteDTO> getBranchSites(String expedioQuoteRef) {
        return newArrayList(sites.values());

    }

    public Map<String, SiteDTO> storedItemMap() {
        return sites;
    }
}
