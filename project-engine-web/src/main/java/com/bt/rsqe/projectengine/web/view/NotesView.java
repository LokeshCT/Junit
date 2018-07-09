package com.bt.rsqe.projectengine.web.view;

import com.bt.rsqe.dto.NoteDto;
import com.bt.rsqe.web.LocalDateTimeFormatter;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NotesView {
    private List<NoteView> notes = new ArrayList<NoteView>();
    public NotesView(List<NoteDto> noteDTOs) {
        Collections.sort(noteDTOs, DATE_CREATED_ORDER);
        for (NoteDto note : noteDTOs) {
            this.notes.add(new NoteView(note.noteText, new LocalDateTimeFormatter(note.dateCreated).format(), note.createdBy));
        }
    }

    public List<NoteView> getNotes() {
        return notes;
    }

    public String getLastCreatedNoteText() {
        return notes.isEmpty() ? StringUtils.EMPTY : Iterables.get(notes, 0).getNoteText();
    }

    public static class NoteView {

        private String noteText;
        private String dateCreated;
        private String createdBy;

        public NoteView(String noteText, String dateCreated, String createdBy) {

            this.noteText = noteText;
            this.dateCreated = dateCreated;
            this.createdBy = createdBy;
        }

        public String getNoteText() {
            return noteText;
        }

        public String getDateCreated() {
            return dateCreated;
        }

        public String getCreatedBy() {
            return createdBy;
        }
    }

    private static final Comparator<NoteDto> DATE_CREATED_ORDER = new Comparator<NoteDto>() {
        @Override
        public int compare(NoteDto n1, NoteDto n2) {
            return new DateTime(n2.dateCreated).compareTo(new DateTime(n1.dateCreated));
        }
    };
}
