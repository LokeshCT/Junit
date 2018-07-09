package com.bt.rsqe.projectengine.web.userImport;

import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.web.quoteoptionorders.ecrfsheet.ECRFSheetOrchestratorTest;
import com.bt.rsqe.utils.RSQEMockery;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.io.IOException;

import static com.bt.rsqe.matchers.ResponseMatcher.*;
import static org.junit.Assert.*;

public class UserImportResourceHandlerTest {

    private Mockery mockery;
    private UserImportResourceHandler userImportResourceHandler;
    private ProjectResource project;
    private ProductInstanceClient productInstanceClient;
    private static final String PROJECT_ID = "projectId";
    private static final String QUOTE_OPTION_ID = "quoteOptionId";
    private static final String PRODUCT_CODE = "sCode";
    private static final String CUSTOMER_ID = "customerId";
    private static final String CONTRACT_ID = "contractId";

    @Before
    public void setUp() throws Exception {
        mockery = new RSQEMockery();
        project = mockery.mock(ProjectResource.class);
        productInstanceClient = mockery.mock(ProductInstanceClient.class);
        userImportResourceHandler = new UserImportResourceHandler(project, productInstanceClient, null, null, null, null);
    }

    @Test
    public void shouldInvokeUserImportDuringImportRequest() throws IOException {
        //mockery.checking(new Expectations() {{
        //
        //}});
        //final Response response = userImportResourceHandler.userImport(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, PRODUCT_CODE,
        //                                                               ECRFSheetOrchestratorTest.class.getResourceAsStream("occ_test_upload.xls"),
        //                                                               FormDataContentDisposition
        //                                                                   .name("occ_test_upload.xls")
        //                                                                   .fileName("occ_test_upload.xls").build());
        //assertThat(response, aResponse().withStatusOK());
        //mockery.assertIsSatisfied();
    }

    @Test
    public void shouldInvokeUserExportDuringExportRequest() {
        //mockery.checking(new Expectations() {{
        //
        //}});
        //final Response response = userImportResourceHandler.userExport(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, PRODUCT_CODE);
        //assertThat(response, aResponse().withStatusOK());
        //mockery.assertIsSatisfied();
    }
}
