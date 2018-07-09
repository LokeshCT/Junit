package com.bt.rsqe.projectengine.web.quoteoption.lineitems;

import com.bt.rsqe.dto.NoteDto;
import com.bt.rsqe.projectengine.web.facades.LineItemNotesFacade;
import com.bt.rsqe.projectengine.web.uri.UriFactoryImpl;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

public class LineItemNoteInteractor {
    private LineItemNotesFacade lineItemNotesFacade;
    private LineItemNoteViewRenderer viewRenderer;

    public LineItemNoteInteractor(LineItemNotesFacade lineItemNotesFacade, LineItemNoteViewRenderer viewRenderer) {
        this.lineItemNotesFacade = lineItemNotesFacade;
        this.viewRenderer = viewRenderer;
    }

    public Response getNotes(String customerId, String contractId, String projectId, String optionId, String itemId) {
        List<NoteDto> notes = lineItemNotesFacade.getNotes(projectId, optionId, itemId);
        URI lineItemNotesUri = UriFactoryImpl.lineItemNotes(customerId, contractId, projectId, optionId, itemId);
        String responsePage = viewRenderer.render(notes, lineItemNotesUri);
        return Response.ok().entity(responsePage).build();
    }

    public Response createNote(String projectId, String optionId, String itemId, NoteDto note) {
        lineItemNotesFacade.createNote(projectId, optionId, itemId, note);
        return Response.ok().build();
    }
}
