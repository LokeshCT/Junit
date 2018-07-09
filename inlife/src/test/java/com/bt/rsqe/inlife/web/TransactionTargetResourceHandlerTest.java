package com.bt.rsqe.inlife.web;

import com.bt.rsqe.mis.client.TransactionTargetResource;
import com.bt.rsqe.mis.client.dto.TransactionTargetDTO;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static com.bt.rsqe.mis.client.fixtures.TransactionTargetFixture.*;
import static javax.ws.rs.core.Response.Status.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TransactionTargetResourceHandlerTest {
    private TransactionTargetResource transactionTargetResource;
    private TransactionTargetResourceHandler transactionTargetResourceHandler;

    @Before
    public void setup() {
        transactionTargetResource = mock(TransactionTargetResource.class);
        transactionTargetResourceHandler = new TransactionTargetResourceHandler(transactionTargetResource, null);
    }

    @Test
    public void shouldHandleCreateRequest() {
        final TransactionTargetDTO transactionTargetDTO = aTransactionTarget().build();
        List<TransactionTargetDTO> transactionTargetDTOs = new ArrayList<TransactionTargetDTO>() {{
            add(transactionTargetDTO);
        }};
        when(transactionTargetResource.saveTargetsTransactions(transactionTargetDTO)).thenReturn("123");

        Response response = transactionTargetResourceHandler.create(transactionTargetDTOs);

        verify(transactionTargetResource, only()).saveTargetsTransactions(transactionTargetDTO);
        assertThat(response.getStatus(), Is.is(CREATED.getStatusCode()));
        assertThat(response.getEntity().toString(), Is.is("123"));
    }

    @Test
    public void shouldReturnErrorResponseIfThereIsAnyExceptionWhileCreatingMetrics() {
        final TransactionTargetDTO transactionTargetDTO = aTransactionTarget().build();
        List<TransactionTargetDTO> transactionTargetDTOs = new ArrayList<TransactionTargetDTO>() {{
            add(transactionTargetDTO);
        }};

        doThrow(new RuntimeException("exception msg")).when(transactionTargetResource).saveTargetsTransactions(transactionTargetDTO);

        Response response = transactionTargetResourceHandler.create(transactionTargetDTOs);

        assertThat(response.getStatus(), Is.is(NOT_FOUND.getStatusCode()));
    }
}
