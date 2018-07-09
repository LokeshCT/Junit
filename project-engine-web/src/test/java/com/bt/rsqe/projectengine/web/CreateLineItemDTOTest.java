package com.bt.rsqe.projectengine.web;

import com.bt.rsqe.utils.JSONSerializer;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.Is.*;

public class CreateLineItemDTOTest {

    @Test
    public void shouldSerialize() throws Exception {
        JSONSerializer jsonSerializer = JSONSerializer.getInstance();
        List<CreateLineItemDTO.ActionDTO> actionDTOs = new ArrayList<CreateLineItemDTO.ActionDTO>();
        actionDTOs.add(new CreateLineItemDTO.ActionDTO("1", "Provide"));
        actionDTOs.add(new CreateLineItemDTO.ActionDTO("2", "Provide"));
        CreateLineItemDTO createLineItemDTO = new CreateLineItemDTO("1", "0000012121221", "54", "auth token", actionDTOs);

        String serialized = jsonSerializer.serialize(createLineItemDTO);
        CreateLineItemDTO deserializedDTO = jsonSerializer.deSerialize(serialized, CreateLineItemDTO.class);

        assertThat(deserializedDTO.getRsqeQuoteOptionId(), is("1"));
        assertThat(deserializedDTO.getAuthenticationToken(), is("auth token"));
        assertThat(deserializedDTO.getExpedioCustomerId(), is("54"));
        assertThat(deserializedDTO.getExpedioQuoteId(), is("0000012121221"));
        assertThat(deserializedDTO.getLineItems().size(), is(2));
    }

    @Test
    public void shouldDeserialiseJsonString() {
        String json = "{\"rsqeQuoteOptionId\":1,\"expedioQuoteId\":\"0000012121221\",\"expedioCustomerId\":54,\"authenticationToken\":\"auth token\",\"orderType\":\"Provide\",\"lineItems\":[{\"expedioSiteId\":1,\"action\":\"Provide\"},{\"expedioSiteId\":2,\"action\":\"Modify\"}]}";
        CreateLineItemDTO createLineItemDTO = JSONSerializer.getInstance().deSerialize(json, CreateLineItemDTO.class);
        assertThat(createLineItemDTO.getRsqeQuoteOptionId(), is("1"));
        assertThat(createLineItemDTO.getExpedioQuoteId(), is("0000012121221"));
        assertThat(createLineItemDTO.getExpedioCustomerId(), is("54"));
        assertThat(createLineItemDTO.getAuthenticationToken(), is("auth token"));
        List<CreateLineItemDTO.ActionDTO> lineItems = createLineItemDTO.getLineItems();
        assertThat(lineItems.size(), is(2));
        assertThat(lineItems.get(0).expedioSiteId, is("1"));
        assertThat(lineItems.get(0).action, is("Provide"));
        assertThat(lineItems.get(1).expedioSiteId, is("2"));
        assertThat(lineItems.get(1).action, is("Modify"));
    }
}
