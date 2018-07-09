package com.bt.rsqe.projectengine.web.view;

import com.bt.rsqe.dto.NoteDto;
import com.bt.rsqe.web.LocalDateTimeFormatter;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.junit.Test;

import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.Is.*;

public class NotesViewTest {

    public static final String NOTE = "NOTE";
    public static final String NOTE_2 = "NOTE_2";
    public static final String CREATED_BY = "createdBy";

    @Test
    public void shouldMapSingleNotesDTO() throws Exception {
        final DateTime dateCreated = new DateTime();
        final NoteDto noteDto = new NoteDto(NOTE, dateCreated, CREATED_BY);

        final NotesView notesView = new NotesView(new ArrayList<NoteDto>() {{
            add(noteDto);
        }});

        assertThat(notesView.getNotes().size(), is(1));
        assertThat(notesView.getNotes().get(0).getCreatedBy(), is(CREATED_BY));
        assertThat(notesView.getNotes().get(0).getDateCreated(), is(new LocalDateTimeFormatter(dateCreated.toString()).format()));
        assertThat(notesView.getNotes().get(0).getNoteText(), is(NOTE));
        assertThat(notesView.getLastCreatedNoteText(), is(NOTE));
    }

    @Test
    public void shouldOrderMultipleNotesByDate() throws Exception {
        final DateTime dateCreated = new DateTime();

        final NoteDto noteDTO1 = new NoteDto(NOTE_2, dateCreated, CREATED_BY);
        final NoteDto noteDTO2 = new NoteDto(NOTE, dateCreated.minusHours(1), CREATED_BY);

        final NotesView notesView = new NotesView(new ArrayList<NoteDto>() {{
            add(noteDTO1);
            add(noteDTO2);
        }});

        assertThat(notesView.getNotes().size(), is(2));
        assertThat(notesView.getNotes().get(0).getDateCreated(), is(new LocalDateTimeFormatter(dateCreated.toString()).format()));
        assertThat(notesView.getNotes().get(1).getDateCreated(), is(new LocalDateTimeFormatter(dateCreated.minusHours(1).toString()).format()));
        assertThat(notesView.getLastCreatedNoteText(), is(NOTE_2));
    }

    @Test
    public void shouldReturnEmptyNoteTextWhenNodesDontExist() throws Exception {
        final NotesView notesView = new NotesView(new ArrayList<NoteDto>() {});

        assertThat(notesView.getNotes().size(), is(0));
        assertThat(notesView.getLastCreatedNoteText(), is(StringUtils.EMPTY));
    }

}
