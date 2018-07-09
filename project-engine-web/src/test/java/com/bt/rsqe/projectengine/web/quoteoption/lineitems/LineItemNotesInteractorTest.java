package com.bt.rsqe.projectengine.web.quoteoption.lineitems;

import com.bt.rsqe.dto.NoteDto;
import com.bt.rsqe.projectengine.web.facades.LineItemNotesFacade;
import com.bt.rsqe.projectengine.web.uri.UriFactoryImpl;
import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.core.Response;

import java.net.URI;
import java.util.List;

import static org.mockito.Matchers.anyString;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class LineItemNotesInteractorTest {

    private LineItemNoteInteractor interactor;
    private LineItemNotesFacade lineItemNotesFacade;
    private LineItemNoteViewRenderer viewRenderer;

    private List<NoteDto> notes;
    public static final NoteDto NEW_NOTE = new NoteDto("some note text", new DateTime(), "me");

    @Before
    public void setup() {
        notes = Lists.newArrayList();

        lineItemNotesFacade = mock(LineItemNotesFacade.class);
        when(lineItemNotesFacade.getNotes(anyString(), anyString(), anyString())).thenReturn(notes);

        viewRenderer = mock(LineItemNoteViewRenderer.class);

        interactor = new LineItemNoteInteractor(lineItemNotesFacade, viewRenderer);

        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldGetNotesSuccessfully() {
        Response response = getNotesResponse();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void shouldInvokeNotesFacade() {
        getNotesResponse();

        verify(lineItemNotesFacade).getNotes("projectId", "optionId", "itemId");
    }

    @Test
    public void shouldInvokeRenderer() {
        getNotesResponse();
        URI expectedUri = UriFactoryImpl.lineItemNotes("customerId", "contractId", "projectId", "optionId", "itemId");
        verify(viewRenderer).render(notes, expectedUri);
    }

    @Test
    public void shouldReturnPageInResponse() {
        String page = "a page";

        when(viewRenderer.render(anyListOf(NoteDto.class), any(URI.class))).thenReturn(page);

        Response response = getNotesResponse();

        assertEquals(page, response.getEntity());
    }

    @Test
    public void shouldPostNoteSuccessfully() {
        Response response = interactor.createNote("projectId", "optionId", "itemId", NEW_NOTE);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void shouldInvokeFacadeWhenPosting() {
        interactor.createNote("projectId", "optionId", "itemId", NEW_NOTE);
        verify(lineItemNotesFacade).createNote("projectId", "optionId", "itemId", NEW_NOTE);
    }

    private Response getNotesResponse() {
        return interactor.getNotes("customerId", "contractId", "projectId", "optionId", "itemId");
    }
}
