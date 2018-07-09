package com.bt.rsqe.customerinventory.service.handlers;

import com.bt.rsqe.customerinventory.service.client.domain.ValidationNotification;
import com.bt.rsqe.customerinventory.service.orchestrators.ValidationOrchestrator;
import com.bt.rsqe.domain.AssetKey;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;

import static com.bt.rsqe.domain.product.extensions.ValidationErrorType.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class ValidationHandlerTest {
    private final ValidationOrchestrator orchestrator = mock(ValidationOrchestrator.class);
    private final List<ValidationNotification> notifications = new ArrayList<ValidationNotification>();
    private final AssetKey assetKey = new AssetKey("ASSET_ID", 1l);
    private final ValidationHandler handler = new ValidationHandler(orchestrator);
    public static final String ERROR_1 = "Error1";
    public static final String WARNING_1 = "Warning1";

    @Before
    public void setUp() throws Exception {
        notifications.add(new ValidationNotification(Error, ERROR_1));
        notifications.add(new ValidationNotification(Warning, WARNING_1));
    }

    @Test
    public void shouldGetValidationNotificationsFromOrchestrator() {
        when(orchestrator.validate(assetKey)).thenReturn(notifications);

        Response response = handler.validate(assetKey);

        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
        @SuppressWarnings("unchecked")
        List<ValidationNotification> responseEntity = (List<ValidationNotification>) response.getEntity();
        assertThat(responseEntity, is(notifications));
    }
}