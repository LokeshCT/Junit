package com.bt.rsqe.projectengine.web.resourcestubs;

import com.bt.rsqe.domain.project.LineItemAction;
import com.bt.rsqe.projectengine.ProjectDTO;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTOFixture;
import com.bt.rsqe.domain.QuoteOptionItemStatus;

import java.net.URI;
import java.util.Map;

import static com.google.common.collect.Maps.*;

public class ProjectResourceStub extends ProjectResource {

    private Map<String, ProjectDTO> projects = newHashMap();
    private Map<String, QuoteOptionResourceStub> quoteOptionResourceStubs = newHashMap();

    public ProjectResourceStub() {
        super(URI.create(""),"");
    }

    public ProjectResourceStub with(ProjectDTO project) {
        projects.put(project.id, project);
        return this;
    }

    @Override
    public QuoteOptionResourceStub quoteOptionResource(String projectId) {
        if (!quoteOptionResourceStubs.containsKey(projectId)) {
            QuoteOptionResourceStub quoteOptionResourceStub = new QuoteOptionResourceStub().with(QuoteOptionDTO.newInstance("QUOTE_OPTION_ID", "blahName", "USD", "12", "createdBy"));
            QuoteOptionItemDTO quoteOptionItemDTO = QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().withSCode("sCode").withAction(LineItemAction.PROVIDE.name())
                                                               .withStatus(QuoteOptionItemStatus.DRAFT).build();

            quoteOptionResourceStub.quoteOptionItemResource("12").with(quoteOptionItemDTO);
            quoteOptionResourceStubs.put(projectId, quoteOptionResourceStub);
        }
        return quoteOptionResourceStubs.get(projectId);
    }


    @Override
    public ProjectDTO get(String id) {
        final ProjectDTO project = projects.get(id);
        if (project == null) {
            throw new com.bt.rsqe.web.rest.exception.ResourceNotFoundException();
        } else {
            return project;
        }
    }

    @Override
    public ProjectDTO put(String id, String name, String customerId, String contractId) {
        if (projects.containsKey(id)) {
            return projects.put(id, new ProjectDTO(id, name, customerId, contractId));
        }
        return null;
    }

}
