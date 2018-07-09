package com.bt.rsqe.ape.repository.entities;

import com.bt.rsqe.ape.dto.AccessStaffComment;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.UUID;

@Table(name = "ACCESS_STAFF_COMMENT")
@Entity
public class AccessStaffCommentEntity {
    @Id
    @Column(name = "STAFF_COMMENT_ID")
    private String commentId;

    @Column(name = "QREF_ID")
    private String qrefId;

    @Column(name = "STAFF_COMMENT")
    private String comment;

    @Column(name = "STAFF_EMAIL")
    private String staffEmail;

    @Column(name = "STAFF_NAME")
    private String staffName;

    @Column(name = "CREATE_DATE")
    private Date createDate;

    public AccessStaffCommentEntity () {}

    public AccessStaffCommentEntity (String qrefId,
                                     String comment,
                                     String staffEmail,
                                     String staffName,
                                     Date createDate) {
        this.commentId = UUID.randomUUID().toString();
        this.qrefId = qrefId;
        this.comment = comment;
        this.staffEmail = staffEmail;
        this.staffName = staffName;
        this.createDate = createDate;
    }

    public String getCommentId() {
        return commentId;
    }

    public String getQrefId() {
        return qrefId;
    }

    public String getComment() {
        return comment;
    }

    public String getStaffEmail() {
        return staffEmail;
    }

    public String getStaffName() {
        return staffName;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public AccessStaffComment toDto() {
        return new AccessStaffComment(comment,
                                      staffName,
                                      staffEmail,
                                      toDayMonthYear(createDate));
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(qrefId)
            .append(comment)
            .append(staffEmail)
            .append(staffName)
            .append(toDayMonthYear(createDate))
            .toHashCode();
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AccessStaffCommentEntity that = (AccessStaffCommentEntity) o;

        return new EqualsBuilder()
            .append(qrefId, that.qrefId)
            .append(comment, that.comment)
            .append(staffEmail, that.staffEmail)
            .append(staffName, that.staffName)
            .append(toDayMonthYear(createDate), toDayMonthYear(that.createDate))
            .isEquals();
    }

    private String toDayMonthYear(Date date) {
        return new SimpleDateFormat("dd/MM/yyyy").format(date);
    }
}
