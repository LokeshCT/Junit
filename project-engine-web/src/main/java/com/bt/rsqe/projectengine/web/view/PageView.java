package com.bt.rsqe.projectengine.web.view;

import com.bt.rsqe.projectengine.web.BreadCrumb;
import com.google.common.collect.Lists;

import java.util.List;

import static com.google.common.collect.Lists.*;

public class PageView {
    private final String title;
    private final String header;
    private final List<Tab> tabs;
    private final List<BreadCrumb> breadCrumbs;

    public PageView(String title, String header, List breadCrumbs) {
        this.title = title;
        this.header = header;
        tabs = newArrayList();
        this.breadCrumbs = breadCrumbs;
    }

    public PageView(String title, String header){
        this(title, header, Lists.newArrayList());
    }

    public PageView addTab(String tabClass, String label, String uri) {
        tabs.add(new Tab(tabClass, label, uri));
        return this;
    }

    public String getTitle() {
        return title;
    }

    public String getHeader() {
        return header;
    }

    public List<Tab> getTabs() {
        return tabs;
    }

    public List<BreadCrumb> getBreadCrumbs() {
        return breadCrumbs;
    }

    public static class Tab {
        private final String tabClass;
        private final String label;
        private final String uri;

        Tab(String tabClass, String label, String uri) {
            this.tabClass = tabClass;
            this.label = label;
            this.uri = uri;
        }

        public String getTabClass() {
            return tabClass;
        }

        public String getLabel() {
            return label;
        }

        public String getUri() {
            return uri;
        }
    }
}
