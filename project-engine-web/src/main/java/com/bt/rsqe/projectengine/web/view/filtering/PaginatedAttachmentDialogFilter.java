package com.bt.rsqe.projectengine.web.view.filtering;

import com.bt.rsqe.projectengine.AttachmentViewDTO;
import com.bt.rsqe.projectengine.web.view.pagination.Pagination;

import java.util.List;

public class PaginatedAttachmentDialogFilter implements PaginatedFilter<AttachmentViewDTO> {
    private Pagination pagination;

    public PaginatedAttachmentDialogFilter(Pagination pagination) {
        this.pagination = pagination;
    }

    @Override
    public PaginatedFilterResult applyTo(List<AttachmentViewDTO> itemRowDTOs) {
        return new PaginatedFilterResult<AttachmentViewDTO>(itemRowDTOs.size(),
                                                  pagination.paginate(itemRowDTOs),
                                                  itemRowDTOs.size(),
                                                  pagination.getPageNumber());
    }
}
