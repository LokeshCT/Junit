package com.bt.rsqe.projectengine.web.facades;

import com.bt.rsqe.projectengine.LineItemNoteResource;
import com.bt.rsqe.dto.NoteDto;
import com.bt.rsqe.security.UserDTO;

import java.util.List;

public class LineItemNotesFacade {
    private LineItemNoteResource lineItemResource;
    private UserFacade userFacade;

    public LineItemNotesFacade(LineItemNoteResource resource, UserFacade userFacade) {
        this.lineItemResource = resource;
        this.userFacade = userFacade;
    }

    public List<NoteDto> getNotes(String projectId, String optionId, String itemId) {
        try {
            List<NoteDto> notes = lineItemResource.getNotes(projectId, optionId, itemId);
            resolveCreatedByNames(notes);
            return notes;
        }
        catch(Exception e) {
            throw new RuntimeException("Unable to retrieve notes from quote engine", e);
        }
    }

    private void resolveCreatedByNames(List<NoteDto> notes) {
        for(NoteDto note : notes) {
            UserDTO userDto = userFacade.findUser(note.createdBy);
            note.createdBy = String.format("%s %s", userDto.forename, userDto.surname);
        }
    }

    public void createNote(String projectId, String optionId, String itemId, NoteDto newNote) {
        lineItemResource.post(projectId, optionId, itemId, newNote);
    }
}
