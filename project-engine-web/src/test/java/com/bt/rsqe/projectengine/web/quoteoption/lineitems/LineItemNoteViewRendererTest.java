package com.bt.rsqe.projectengine.web.quoteoption.lineitems;

import com.bt.rsqe.dto.NoteDto;
import com.bt.rsqe.projectengine.web.view.NotesView;
import com.bt.rsqe.web.Presenter;
import com.bt.rsqe.web.View;
import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

public class LineItemNoteViewRendererTest {

    private final static URI A_URI;
    static {
        try {
            A_URI = new URI("foo");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private List<NoteDto> notes;
    private LineItemNoteViewRenderer renderer;
    private Presenter presenter;

    @Before
    public void setup() {
        notes = Lists.newArrayList();
        presenter = mock(Presenter.class);
        renderer = new LineItemNoteViewRenderer(presenter);
    }

    @Test
    public void something() {
        List<NoteDto> notes = Lists.newArrayList();
        renderer.render(notes, A_URI);
    }

    @Test
    public void shouldInvokePresenter() {
        List<NoteDto> notes = Lists.newArrayList();
        renderer.render(notes, A_URI);

        verify(presenter).render(any(View.class));
    }

    @Test
    public void shouldSetViewTemplate() {
        View view = captureView();

        assertEquals(LineItemNoteViewRenderer.NOTES_FORM_TEMPLATE, view.template());
    }

    @Test
    public void shouldSetNewNoteUri() {
        View view = captureView();

        assertEquals(A_URI, view.context().get("newNoteURI"));
    }

    @Test
    public void shouldSetNotesView() {
        NotesView notesView = captureNotesView();
        assertNotNull(notesView);
    }

    @Test
    public void shouldAddNotes() {
        NoteDto note = new NoteDto("some note text", new DateTime(), "me");
        NoteDto anotherNote = new NoteDto("some more note text", new DateTime(), "someone");

        notes.add(note);
        notes.add(anotherNote);

        NotesView notesView = captureNotesView();
        assertEquals(2, notesView.getNotes().size());
    }

    private NotesView captureNotesView() {
        View view = captureView();
        return (NotesView) view.context().get("view");
    }

    private View captureView() {
        renderer.render(notes, A_URI);

        ArgumentCaptor<View> viewCapture = ArgumentCaptor.forClass(View.class);
        verify(presenter).render(viewCapture.capture());
        return viewCapture.getValue();
    }

}
