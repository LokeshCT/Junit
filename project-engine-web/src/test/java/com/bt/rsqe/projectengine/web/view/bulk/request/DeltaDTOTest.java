package com.bt.rsqe.projectengine.web.view.bulk.request;

import com.google.gson.GsonBuilder;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class DeltaDTOTest {

    @Test
    public void shouldUnMarshallJson() {
        String json = "[{\"asset\":{\"id\":\"e96180426089788fcaf68e2ed0\",\"version\":1.0,\"lineItemId\":\"15de69c8-2fbc-4403-9836-8d740dbf3a42\"},\"characteristics\":[{\"name\":\"BONDING MODE\",\"newValue\":\"NotBonded\"}],\"stencil\":{\"id\":\"S0308457\"},\"lockVersion\":0.0}]";

        List<DeltaDTO> list = new GsonBuilder().create().fromJson(json, List.class);
        assertThat(list.size(), is(1));


        String json2 = new GsonBuilder().create().toJson(list);
        assertThat(json2, is(json));
    }

}
