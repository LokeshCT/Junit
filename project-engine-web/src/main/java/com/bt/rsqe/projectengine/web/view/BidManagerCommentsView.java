package com.bt.rsqe.projectengine.web.view;

import com.bt.rsqe.dto.BidManagerCommentsDTO;
import com.bt.rsqe.web.LocalDateTimeFormatter;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BidManagerCommentsView {

    private String created;
    private String createdBy;
    private String comments;
    private String caveats;
    private String userEmail;
    private List<BidManagerCommentsView> commentsAndCaveats = new ArrayList<BidManagerCommentsView>();
    private String bcmUri;

    public BidManagerCommentsView(List<BidManagerCommentsDTO> bidManagerCommentsDTOs, String bcmUri) {
        this.bcmUri = bcmUri;
        for (BidManagerCommentsDTO bidManagerCommentsDTO : bidManagerCommentsDTOs) {
            this.commentsAndCaveats.add(new BidManagerCommentsView(new LocalDateTimeFormatter(bidManagerCommentsDTO.created).format(), bidManagerCommentsDTO.createdBy, bidManagerCommentsDTO.caveats, bidManagerCommentsDTO.comments, bidManagerCommentsDTO.userEmail));
        }
    }

    public BidManagerCommentsView(String created, String createdBy, String caveats, String comments, String userEmail) {
        this.created = created;
        this.createdBy = createdBy;
        this.caveats = caveats;
        this.comments = comments;
        this.userEmail = userEmail;
    }

    public String getLastCreatedComment() {
        return commentsAndCaveats.isEmpty() ? StringUtils.EMPTY : Iterables.getLast(commentsAndCaveats).getComments();
    }

    public String getLastCreatedCaveat() {
        return commentsAndCaveats.isEmpty() ? StringUtils.EMPTY : Iterables.getLast(commentsAndCaveats).getCaveats();
    }

    public BidManagerCommentsView getLastCommentAndCaveatDetails() {
        return commentsAndCaveats.isEmpty() ? null : Iterables.getLast(commentsAndCaveats);
    }

    public String getCreated() {
        return created;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getComments() {
        return comments;
    }

    public String getCaveats() {
        return caveats;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public List<BidManagerCommentsView> getCommentsAndCaveats() {
        return commentsAndCaveats;
    }

    public String getBcmApproveUri() {
        return bcmUri + "/approve-discounts";
    }

    public String getBcmRejectUri() {
        return bcmUri + "/reject-discounts";
    }
}
