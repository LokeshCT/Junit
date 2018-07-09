package com.bt.rsqe.inlife.web;

import com.bt.rsqe.inlife.client.dto.RequestResponseLog;
import com.bt.rsqe.persistence.store.RequestResponseStore;
import org.hamcrest.core.Is;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class RequestResponseResourceHandlerTest {
    @Test
    public void shouldStoreRequestResponseLog() throws Exception {
        RequestResponseStore store = mock(RequestResponseStore.class);
        RequestResponseResourceHandler handler = new RequestResponseResourceHandler(store);

        Response response = handler.log(new RequestResponseLog(RequestResponseStore.Type.REQUEST, "origin", "operation", "id", "<xml/>"));

        assertThat(response.getStatus(), Is.is(200));

        verify(store).save(RequestResponseStore.Type.REQUEST, "origin", "operation", "id", "<xml/>");
    }
}
