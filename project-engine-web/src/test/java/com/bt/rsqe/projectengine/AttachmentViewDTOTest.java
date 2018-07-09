package com.bt.rsqe.projectengine;

import com.bt.rsqe.utils.JSONSerializer;
import org.junit.Test;

import static com.bt.rsqe.projectengine.ItemRowDTOFixture.anItemRow;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class AttachmentViewDTOTest {

    @Test
    public void shouldTransferBetweenClientAndServer() {

        AttachmentViewDTO clientCopy = AttachmentViewDTOFixture.attachmentViewDTO().withItemRowDTOs(
            anItemRow().withId("id1").withUploadAppliesTo("ServiceDelivery").withUploadDate("18/03/1982").withUploadFileName("DeliveryMultiplyOne.xls").build(),
            anItemRow().withId("id2").withUploadAppliesTo("ServiceDelivery").withUploadDate("18/03/1982").withUploadFileName("DeliveryMultiplyTwo.xls").build()
        ).withPageNumber(1).withTotalDisplayRecords(2).build();

        String jsonString = JSONSerializer.getInstance().serialize(clientCopy);
        AttachmentViewDTO serverCopy = JSONSerializer.getInstance().deSerialize(jsonString, AttachmentViewDTO.class);


        assertThat(clientCopy, is(serverCopy));
    }
}
