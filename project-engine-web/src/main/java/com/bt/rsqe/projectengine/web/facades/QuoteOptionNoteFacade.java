package com.bt.rsqe.projectengine.web.facades;

import com.bt.rsqe.dto.NoteDto;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionNoteResource;
import com.bt.rsqe.security.UserContextManager;

import java.util.List;

public class QuoteOptionNoteFacade {

    private ProjectResource projectResource;

    public QuoteOptionNoteFacade(ProjectResource projectResource) {
        this.projectResource = projectResource;
    }

    public void saveNote(String projectId, String quoteOptionId, String note) {
        final QuoteOptionNoteResource quoteOptionNoteResource = projectResource.quoteOptionResource(projectId).quoteOptionNoteResource(quoteOptionId);
        final String createdBy = UserContextManager.getCurrent().getLoginName();
        quoteOptionNoteResource.post(new NoteDto(note, null, createdBy));
    }

    public List<NoteDto> getNotes(String projectId, String quoteOptionId) {
        final QuoteOptionNoteResource quoteOptionNoteResource = projectResource.quoteOptionResource(projectId).quoteOptionNoteResource(quoteOptionId);
        return quoteOptionNoteResource.getAll();
    }
}
