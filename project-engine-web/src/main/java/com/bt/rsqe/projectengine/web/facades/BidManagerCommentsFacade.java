package com.bt.rsqe.projectengine.web.facades;


import com.bt.rsqe.dto.BidManagerCommentsDTO;
import com.bt.rsqe.projectengine.BidManagerCommentsResource;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.security.UserContextManager;
import com.bt.rsqe.security.UserDTO;

import java.util.List;

public class BidManagerCommentsFacade {

    private ProjectResource projectResource;
    private UserFacade userFacade;

    public BidManagerCommentsFacade(ProjectResource projectResource, UserFacade userFacade) {
        this.projectResource = projectResource;
        this.userFacade = userFacade;
    }

    public List<BidManagerCommentsDTO> getBidManagerComments(String projectId, String quoteOptionId) {
        final BidManagerCommentsResource bidManagerCommentsResource = projectResource.quoteOptionResource(projectId).bidManagerCommentsResource(quoteOptionId);
        return bidManagerCommentsResource.getAll();
    }

    public void saveCommentsAndCaveats(String projectId, String quoteOptionId, String comments, String caveats) {
        final BidManagerCommentsResource bidManagerCommentsResource = projectResource.quoteOptionResource(projectId).bidManagerCommentsResource(quoteOptionId);
        final String loginName = UserContextManager.getCurrent().getLoginName();
        UserDTO userDTO = userFacade.findUser(loginName);
        final String createdBy = String.format("%s %s", userDTO.forename, userDTO.surname);
        bidManagerCommentsResource.post(new BidManagerCommentsDTO(comments, caveats, null, createdBy, userDTO.getEmail()));

    }

    public UserFacade getUserFacade() {
        return userFacade;
    }
}
