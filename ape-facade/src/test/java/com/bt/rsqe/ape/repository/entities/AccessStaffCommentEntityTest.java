package com.bt.rsqe.ape.repository.entities;

import com.bt.rsqe.ape.dto.AccessStaffComment;
import com.bt.rsqe.util.Assertions;
import org.junit.Test;

import java.sql.Date;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class AccessStaffCommentEntityTest {
    @Test
    public void shouldHaveAWorkingEqualsAndHashCode() throws Exception {
        AccessStaffCommentEntity entity1 = new AccessStaffCommentEntity("qrefId", "comment", "staffEmail", "staffName", Date.valueOf("2000-01-01"));
        AccessStaffCommentEntity entity2 = new AccessStaffCommentEntity("qrefId", "comment", "staffEmail", "staffName", Date.valueOf("2000-01-01"));
        AccessStaffCommentEntity entity3 = new AccessStaffCommentEntity("diffqrefId", "diffcomment", "diffstaffEmail", "diffstaffName", Date.valueOf("2001-01-01"));

        Assertions.assertThatEqualsAndHashcodeWork(entity1, entity2, entity3);
    }

    @Test
    public void shouldConvertToDTO() throws Exception {
        AccessStaffComment staffComment = new AccessStaffCommentEntity("qrefId", "comment", "staffEmail", "staffName", Date.valueOf("2000-01-01")).toDto();

        assertThat(staffComment.getComment(), is("comment"));
        assertThat(staffComment.getStaffEmail(), is("staffEmail"));
        assertThat(staffComment.getStaffName(), is("staffName"));
        assertThat(staffComment.getCreateDate(), is("01/01/2000"));
    }

    @Test
    public void shouldHaveAccessorMethods() throws Exception {
        Date date = Date.valueOf("2000-01-01");

        AccessStaffCommentEntity entity = new AccessStaffCommentEntity("qrefId", "comment", "staffEmail", "staffName", date);
        assertThat(entity.getQrefId(), is("qrefId"));
        assertThat(entity.getComment(), is("comment"));
        assertThat(entity.getStaffEmail(), is("staffEmail"));
        assertThat(entity.getStaffName(), is("staffName"));
        assertThat(entity.getCreateDate(), is(date));
        assertThat(entity.getCommentId(), is(not(nullValue())));
    }
}
