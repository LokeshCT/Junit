package com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet;


import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.*;

public class WorksheetHeaderNodeTree {

    List<HeaderNode> myHeaderNodes = new ArrayList<HeaderNode>();
    Map<String, WorksheetHeaderNodeTree> childHeadersMap = new LinkedHashMap<String, WorksheetHeaderNodeTree>();

    public void addHeader(HeaderNode headerNode){
        addHeader(headerNode, false);
    }

    public void addHeader(HeaderNode headerNode, boolean ignoreDuplicates) {
        if(ignoreDuplicates && myHeaderNodes.contains(headerNode)) {
            return;
        }

        myHeaderNodes.add(headerNode);
    }

    public void addChildHeaders(String sCode, WorksheetHeaderNodeTree worksheetHeaderNodeTree){
         childHeadersMap.put(sCode, worksheetHeaderNodeTree);
    }

    public WorksheetHeaderNodeTree getChildHeader(String sCode){
        return childHeadersMap.get(sCode);
    }

    public List<HeaderNode> traverseHeader(){
        List<WorksheetHeaderNodeTree> worksheetHeaderNodeTreeList = flattenWorksheetHeaders();
        return getAllHeaders(worksheetHeaderNodeTreeList);
    }

    private List<HeaderNode> getAllHeaders(List<WorksheetHeaderNodeTree> worksheetHeaderNodeTreeList) {
        List<HeaderNode> headerNodeList = newArrayList();
        for (WorksheetHeaderNodeTree worksheetHeaderNodeTree : worksheetHeaderNodeTreeList) {
            headerNodeList.addAll(worksheetHeaderNodeTree.myHeaderNodes);
        }
        return headerNodeList;
    }

    private List<WorksheetHeaderNodeTree> flattenWorksheetHeaders() {
        List<WorksheetHeaderNodeTree> worksheetHeaderNodeTreeList = newArrayList(this);
        //Updatation and iteration both happening in following loop.
        //Flattening the tree hence size of list needs to be calculated inside the loop
        for(int index = 0; index < worksheetHeaderNodeTreeList.size(); index++){
            WorksheetHeaderNodeTree worksheetHeaderNodeTree = worksheetHeaderNodeTreeList.get(index);
            worksheetHeaderNodeTreeList.addAll(worksheetHeaderNodeTree.childHeadersMap.values());
        }
        return worksheetHeaderNodeTreeList;
    }

    public static class HeaderNode {
        private String sCode;
        private String column;


        HeaderNode(String sCode, String column) {
            this.column = column;
            this.sCode = sCode;
        }

        public String getScode(){
            return sCode;
        }

        public String getColumn(){
            return column;
        }

        @Override
        public int hashCode(){
            return HashCodeBuilder.reflectionHashCode(this);
        }

        @Override
        public boolean equals(Object that){
            return EqualsBuilder.reflectionEquals(this, that);
        }

    }
}
