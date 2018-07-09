package com.bt.rsqe.projectengine.web;

import com.bt.rsqe.configuration.FileExtensionsConfig;
import com.bt.rsqe.configuration.SharePointUrlConfig;
import com.bt.rsqe.customerrecord.ExpedioClientResources;
import com.bt.rsqe.expedio.project.ExpedioProjectResource;
import com.bt.rsqe.projectengine.web.quoteoptiondetails.QuoteOptionDetailsOrchestrator;
import com.bt.rsqe.projectengine.web.view.filtering.PaginatedFilter;
import com.bt.rsqe.web.Presenter;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class AttachmentDialogResourceHandlerTest {

    private static final String CUSTOMER_ID = "customerId";
    private static final String CONTRACT_ID = "contractId";
    private static final String PROJECT_ID = "projectId";
    private static final String QUOTE_OPTION_ID = "quoteOptionId";
    private static final String FILE_NAME = "fileName.txt";
    private static final String CATOGORY_ID = "categoryId";
    private static final String DOCUMENT_ID = "documentId";
    private static final String SAMPLE_TEXT = "Sample Text";
    private static final InputStream INPUT_STREAM = new ByteArrayInputStream(SAMPLE_TEXT.getBytes());
    private static final byte[] FILE_CONTENT = {83, 97, 109, 112, 108, 101, 32, 84, 101, 120, 116};

    AttachmentDialogResourceHandler handler;
    private Presenter presenter;
    private QuoteOptionDetailsOrchestrator orchestrator;
    private JUnit4Mockery context;

    @Before
    public void setUp(){
        //initMocks(this);
        context = new JUnit4Mockery() {{
            setImposteriser(ClassImposteriser.INSTANCE);
        }};
        presenter = context.mock(Presenter.class);
        orchestrator = context.mock(QuoteOptionDetailsOrchestrator.class);
       handler = new AttachmentDialogResourceHandler(presenter,
                                                     orchestrator
       );
    }

    @Test
    public void shouldDeleteAnAttachment() {
        context.checking(new Expectations() {{

            oneOf(orchestrator).deleteAttachment(with(DOCUMENT_ID), with(CATOGORY_ID), with(CUSTOMER_ID), with(PROJECT_ID)
            );
        }});

        handler.deleteAttachment(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, CATOGORY_ID, DOCUMENT_ID);
        context.assertIsSatisfied();

    }

    @Test
    public void shouldDownloadAnAttachment() {
        context.checking(new Expectations() {{

            oneOf(orchestrator).downloadAttachment(with(DOCUMENT_ID), with(CATOGORY_ID), with(CUSTOMER_ID), with(PROJECT_ID)
            );
        }});

        handler.downloadAttachment(CUSTOMER_ID, PROJECT_ID, CATOGORY_ID, FILE_NAME, DOCUMENT_ID);
        context.assertIsSatisfied();
    }

    @Test
    public void shouldUploadAnAttachment() throws IOException, InvalidFormatException {
        context.checking(new Expectations() {{

            oneOf(orchestrator).uploadAttachment(with(CUSTOMER_ID), with(PROJECT_ID), with(CATOGORY_ID), with(FILE_NAME), with(FILE_CONTENT)
            );
        }});

        handler.uploadAttachment(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, CATOGORY_ID, FILE_NAME, INPUT_STREAM);
        context.assertIsSatisfied();
    }

    @Ignore
    public void shouldLoadAnAttachmentTableTest(){

        context.checking(new Expectations() {{

            oneOf(orchestrator).loadAttachmentTable(with(CUSTOMER_ID), with(PROJECT_ID), with(CATOGORY_ID), with(any(PaginatedFilter.class)));
            will(returnValue(null));
        }});

        handler.loadAttachmentTable(CUSTOMER_ID, PROJECT_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, 0, 1, 1);

        context.assertIsSatisfied();
    }
}
