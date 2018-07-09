package com.bt.rsqe.projectengine.web.quoteoption.lineitems;


import com.bt.rsqe.dto.NoteDto;
import com.bt.rsqe.projectengine.web.view.NotesView;
import com.bt.rsqe.web.Presenter;
import com.bt.rsqe.web.View;

import java.net.URI;
import java.util.List;

public class LineItemNoteViewRenderer {

    public static final String NOTES_FORM_TEMPLATE = "com/bt/rsqe/web/custom_controls/NotesForm.ftl";

    private Presenter presenter;

    public LineItemNoteViewRenderer(Presenter presenter) {
        this.presenter = presenter;
    }

    public String render(List<NoteDto> notes, URI newNoteUri) {
        View view = buildView(notes, newNoteUri);
        return presenter.render(view);
    }

    private View buildView(List<NoteDto> notes, URI newNoteUri) {
        View view = View.viewUsingTemplate(NOTES_FORM_TEMPLATE);

        view.withContext("newNoteURI", newNoteUri);

        NotesView notesView = buildNotesView(notes);
        view.withContext("view", notesView);

        return view;
    }

    private NotesView buildNotesView(List<NoteDto> notes) {
        return new NotesView(notes);
    }
}
