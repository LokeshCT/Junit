package com.bt.rsqe.projectengine;

public class ItemRowDTOFixture {
    private String uploadAppliesTo;
    private String uploadFileName;
    private String uploadDate;
    private String id;

    public ItemRowDTOFixture withUploadAppliesTo(String uploadAppliesTo) {
        this.uploadAppliesTo = uploadAppliesTo;
        return this;
    }

    public ItemRowDTOFixture withUploadFileName(String uploadFileName) {
        this.uploadFileName = uploadFileName;
        return this;
    }

    public ItemRowDTOFixture withUploadDate(String uploadDate) {
        this.uploadDate = uploadDate;
        return this;
    }

    public ItemRowDTOFixture withId(String id) {
        this.id = id;
        return this;
    }

    public AttachmentViewDTO.ItemRowDTO build() {
        return new AttachmentViewDTO.ItemRowDTO(uploadAppliesTo, uploadFileName, uploadDate, id);
    }

    public static ItemRowDTOFixture anItemRow() {
        return new ItemRowDTOFixture();
    }
}
