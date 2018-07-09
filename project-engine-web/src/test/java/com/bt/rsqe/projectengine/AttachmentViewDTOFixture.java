package com.bt.rsqe.projectengine;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class AttachmentViewDTOFixture {
    private List<AttachmentViewDTO.ItemRowDTO> itemRowDTOs = newArrayList();
    private int pageNumber;
    private int totalDisplayRecords;
    private int totalRecords;

    public AttachmentViewDTOFixture withItemRowDTOs(AttachmentViewDTO.ItemRowDTO... itemRowDTOs) {
        for (AttachmentViewDTO.ItemRowDTO itemRowDTO : itemRowDTOs) {
            this.itemRowDTOs.add(itemRowDTO);
        }
        return this;
    }

    public AttachmentViewDTOFixture withPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
        return this;
    }

    public AttachmentViewDTOFixture withTotalDisplayRecords(int totalDisplayRecords) {
        this.totalDisplayRecords = totalDisplayRecords;
        return this;
    }

    public AttachmentViewDTOFixture withTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
        return this;
    }

    public AttachmentViewDTO build() {
        return new AttachmentViewDTO(itemRowDTOs, pageNumber, totalDisplayRecords, totalRecords);
    }

    public static AttachmentViewDTOFixture attachmentViewDTO() {
        return new AttachmentViewDTOFixture();
    }
}
