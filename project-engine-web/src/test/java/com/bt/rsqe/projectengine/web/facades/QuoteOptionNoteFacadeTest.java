package com.bt.rsqe.projectengine.web.facades;

import com.bt.rsqe.dto.NoteDto;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionNoteResource;
import com.bt.rsqe.projectengine.QuoteOptionResource;
import com.bt.rsqe.security.UserContext;
import com.bt.rsqe.security.UserContextManager;
import com.bt.rsqe.security.UserPrincipal;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.bt.rsqe.matchers.ReflectionEqualsMatcher.*;
import static java.util.UUID.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.Is.*;

public class QuoteOptionNoteFacadeTest {

    private Mockery context;
    private QuoteOptionNoteFacade quoteOptionNoteFacade;

    private static final String PROJECT_ID = "PROJECT_ID";
    private static final String QUOTE_OPTION_ID = "QUOTE_OPTION_ID";
    private QuoteOptionResource quoteOptionResource;
    private QuoteOptionNoteResource quoteOptionNoteResource;
    private ProjectResource projectResource;

    @Before
    public void before() {
        context = new JUnit4Mockery() {{
            setImposteriser(ClassImposteriser.INSTANCE);
        }};
        quoteOptionNoteResource = context.mock(QuoteOptionNoteResource.class);
        quoteOptionResource = context.mock(QuoteOptionResource.class);
        projectResource = context.mock(ProjectResource.class);

        quoteOptionNoteFacade = new QuoteOptionNoteFacade(projectResource);
    }

    @Test
    public void shouldSaveNote() throws Exception {

        UserContextManager.setCurrent(new UserContext(new UserPrincipal("user"), randomUUID().toString()));

        context.checking(new Expectations() {{
            allowing(projectResource).quoteOptionResource(PROJECT_ID);
            will(returnValue(quoteOptionResource));
            allowing(quoteOptionResource).quoteOptionNoteResource(QUOTE_OPTION_ID);
            will(returnValue(quoteOptionNoteResource));
            oneOf(quoteOptionNoteResource).post(with(reflectionEquals(new NoteDto("Notes",null,"user"))));
        }});

        quoteOptionNoteFacade.saveNote(PROJECT_ID, QUOTE_OPTION_ID, "Notes");

        context.assertIsSatisfied();
    }

    @Test
    public void shouldLoadNotes() throws Exception {
        final List<NoteDto> noteDtos = new ArrayList<NoteDto>();

        context.checking(new Expectations() {{
            allowing(projectResource).quoteOptionResource(PROJECT_ID);
            will(returnValue(quoteOptionResource));
            allowing(quoteOptionResource).quoteOptionNoteResource(QUOTE_OPTION_ID);
            will(returnValue(quoteOptionNoteResource));
            oneOf(quoteOptionNoteResource).getAll();
            will(returnValue(noteDtos));
        }});

        final List<NoteDto> notes = quoteOptionNoteFacade.getNotes(PROJECT_ID, QUOTE_OPTION_ID);

        assertThat(notes, is(noteDtos));
    }
}
