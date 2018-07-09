package com.bt.rsqe.projectengine;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;


@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class AttachmentViewDTO {
    @XmlElement
    public List<ItemRowDTO> itemDTOs;
    @XmlElement(name = "sEcho")
    public int pageNumber;
    @XmlElement(name = "iTotalDisplayRecords")
    public int totalDisplayRecords;
    @XmlElement(name = "iTotalRecords")
    public int totalRecords;

    public AttachmentViewDTO() {
        //for jaxb
    }

    public AttachmentViewDTO(List<ItemRowDTO> itemRowDTOs, int pageNumber, int totalDisplayRecords, int totalRecords) {
        this.itemDTOs = itemRowDTOs;
        this.pageNumber = pageNumber;
        this.totalDisplayRecords = totalDisplayRecords;
        this.totalRecords = totalRecords;
    }

    public List<ItemRowDTO> getItemDTOs() {
        return itemDTOs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AttachmentViewDTO that = (AttachmentViewDTO) o;

        return new EqualsBuilder()
                .append(pageNumber, that.pageNumber)
                .append(totalDisplayRecords, that.totalDisplayRecords)
                .append(totalRecords, that.totalRecords)
                .append(itemDTOs, that.itemDTOs)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(itemDTOs)
                .append(pageNumber)
                .append(totalDisplayRecords)
                .append(totalRecords)
                .toHashCode();
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ItemRowDTO {

        public String uploadAppliesTo;
        public String uploadFileName;
        public String uploadDate;
        public String id;

        public ItemRowDTO() {
        }

        public ItemRowDTO(String uploadAppliesTo, String uploadFileName, String uploadDate, String id) {
            this.uploadAppliesTo = uploadAppliesTo;
            this.uploadFileName = uploadFileName;
            this.uploadDate = uploadDate;
            this.id = id;
        }

        public String getUploadAppliesTo() {
            return uploadAppliesTo;
        }

        public String getUploadFileName() {
            return uploadFileName;
        }

        public String getUploadDate() {
            return uploadDate;
        }

        public String getId() {
            return id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            ItemRowDTO that = (ItemRowDTO) o;

            return new EqualsBuilder()
                    .append(uploadAppliesTo, that.uploadAppliesTo)
                    .append(uploadFileName, that.uploadFileName)
                    .append(uploadDate, that.uploadDate)
                    .append(id, that.id)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                    .append(uploadAppliesTo)
                    .append(uploadFileName)
                    .append(uploadDate)
                    .append(id)
                    .toHashCode();
        }
    }
}
