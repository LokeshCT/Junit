package com.bt.rsqe.projectengine.web.view.pagination;

import java.util.Collections;
import java.util.List;

public class DefaultPagination implements Pagination {

    private int pageNumber;
    private int startIndex;
    private int pageLength;

    public DefaultPagination(int pageNumber, int startIndex, int pageLength) {
        this.pageNumber = pageNumber;
        this.startIndex = startIndex;
        this.pageLength = pageLength;
    }

    @Override
    public int getPageNumber() {
        return pageNumber;
    }

    @Override
    public <T> List<T> paginate(List<T> itemRowDTOs) {
        int totalSize = itemRowDTOs.size();
        int endIndex = startIndex + pageLength;
        List<T> returnList;
        if(startIndex >= totalSize){//reached end of pages, no items to return
            returnList = Collections.emptyList();
        } else if(endIndex == -1) {
            returnList = itemRowDTOs; // -1 means show all items
        } else if(endIndex >= totalSize){//reached last page, return remaining items
            returnList = itemRowDTOs.subList(startIndex, totalSize);
        } else{// return full page
            returnList = itemRowDTOs.subList(startIndex, endIndex);
        }

        return  returnList;
    }
}

