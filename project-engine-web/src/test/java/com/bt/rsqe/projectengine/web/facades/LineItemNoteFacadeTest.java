package com.bt.rsqe.projectengine.web.facades;

import com.bt.rsqe.projectengine.LineItemNoteResource;
import com.bt.rsqe.dto.NoteDto;
import com.bt.rsqe.security.UserDTO;
import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class LineItemNoteFacadeTest {

    public static final String PROJECT_ID = "aProjectId";
    public static final String OPTION_ID = "anOptionId";
    public static final String ITEM_ID = "itemId";
    private LineItemNoteResource notesResource;
    private LineItemNotesFacade notesFacade;
    private UserFacade userFacade;


    @Before
    public void setup() {
        notesResource = mock(LineItemNoteResource.class);
        userFacade = mock(UserFacade.class);
        notesFacade = new LineItemNotesFacade(notesResource, userFacade);
    }

    @Test
    public void shouldRetrieveNotes() {
        List<NoteDto> expectedNotes = Lists.newArrayList();
        when(notesResource.getNotes(PROJECT_ID, OPTION_ID, ITEM_ID)).thenReturn(expectedNotes);

        List<NoteDto> notes = notesFacade.getNotes(PROJECT_ID, OPTION_ID, ITEM_ID);

        verify(notesResource).getNotes("aProjectId", "anOptionId", "itemId");
        assertEquals(expectedNotes, notes);
    }

    @Test
    public void shouldRetrieveNotesWithResolvedUsernames() {
        NoteDto note = new NoteDto("some note text", new DateTime(), "me");
        List<NoteDto> expectedNotes = Lists.newArrayList(note);
        when(notesResource.getNotes(PROJECT_ID, OPTION_ID, ITEM_ID)).thenReturn(expectedNotes);

        UserDTO me = new UserDTO();
        me.forename = "My";
        me.surname = "Fullname";
        when(userFacade.findUser("me")).thenReturn(me);

        List<NoteDto> notes = notesFacade.getNotes(PROJECT_ID, OPTION_ID, ITEM_ID);

        verify(userFacade).findUser("me");
        NoteDto resolvedNote = notes.iterator().next();
        assertEquals("My Fullname", resolvedNote.createdBy);
    }

    @Test
    public void shouldCreateNote() {
        NoteDto newNote = new NoteDto("note text", new DateTime(), "me");
        notesFacade.createNote(PROJECT_ID, OPTION_ID, ITEM_ID, newNote);
        verify(notesResource).post(PROJECT_ID, OPTION_ID, ITEM_ID, newNote);
    }
}
