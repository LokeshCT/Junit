package com.bt.rsqe.projectengine.web.quoteoption;

import com.bt.rsqe.projectengine.web.facades.BulkUploadFacade;
import com.bt.rsqe.projectengine.web.uri.UriFactory;
import com.bt.rsqe.utils.RSQEMockery;
import com.bt.rsqe.web.AjaxResponseDTO;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMock.class)
public class QuoteOptionBulkUploadOrchestratorTest {
    private static final String PRODUCT_CODE = "PRODUCT_CODE";

    private QuoteOptionBulkUploadOrchestrator orchestrator;
    private BulkUploadFacade mockBulkUploadFacade;
    private UriFactory mockUriFactory;

    private final Mockery context = new RSQEMockery();

    @Before
    public void before() {
        mockUriFactory = context.mock(UriFactory.class);
        mockBulkUploadFacade = context.mock(BulkUploadFacade.class);
        orchestrator = new QuoteOptionBulkUploadOrchestrator(mockUriFactory, mockBulkUploadFacade);
    }

    @Test
    public void shouldResolveBulkUploadUriGivenProductCode() throws Exception {
        final FormDataMultiPart multiPartFormData = context.mock(FormDataMultiPart.class);

        context.checking(new Expectations() {{
            oneOf(mockUriFactory).getBulkUploadUri(PRODUCT_CODE);
            will(returnValue("RESOLVED_BULK_UPLOAD_URI"));

            oneOf(mockBulkUploadFacade).upload("RESOLVED_BULK_UPLOAD_URI", multiPartFormData);
        }});

        orchestrator.upload(PRODUCT_CODE, multiPartFormData);
    }

    @Test
    public void shouldUpload() throws Exception {
        final FormDataMultiPart multiPartFormData = context.mock(FormDataMultiPart.class);
        final AjaxResponseDTO bulkUploadDto = context.mock(AjaxResponseDTO.class);

        context.checking(new Expectations() {{
            ignoring(mockUriFactory);

            oneOf(mockBulkUploadFacade).upload(with(any(String.class)), with(multiPartFormData));
            will(returnValue(bulkUploadDto));
        }});

        orchestrator.upload(PRODUCT_CODE, multiPartFormData);
    }
}
