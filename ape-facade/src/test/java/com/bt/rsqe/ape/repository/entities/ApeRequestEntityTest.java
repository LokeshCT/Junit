package com.bt.rsqe.ape.repository.entities;

import com.bt.rsqe.ape.dto.ApeQrefRequestDTO;
import com.bt.rsqe.ape.dto.AsIsAsset;
import com.bt.rsqe.expedio.fixtures.SiteDTOFixture;
import com.bt.rsqe.productinstancemerge.ChangeType;
import org.junit.Test;

import static com.bt.rsqe.fixtures.UserDTOFixture.*;
import static com.bt.rsqe.util.Assertions.*;
import static com.google.common.collect.Lists.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ApeRequestEntityTest {
    @Test
    public void apeRequestEntityShouldHaveEqualsAndHashCode(){
        ApeRequestEntity one = new ApeRequestEntity("reqId", "uniqueId", "userLogin", "aCurrencyCode");
        ApeRequestEntity sameAsOne = new ApeRequestEntity("reqId", "uniqueId", "userLogin", "aCurrencyCode");
        ApeRequestEntity differentKey = new ApeRequestEntity("reqId2", "uniqueId", "diffuserLogin", "aCurrencyCode");
        ApeRequestEntity nullKey = new ApeRequestEntity(null, "uniqueId", "userLogin", "aCurrencyCode");

        assertThatEqualsAndHashcodeWork(one, sameAsOne, differentKey, nullKey);
    }

    @Test
    public void apeQrefDetailEntityShouldHaveEqualsAndHashCode(){
        ApeQrefDetailEntity one = new ApeQrefDetailEntity("reqId", "qr1", "name1", "val1", 0);
        ApeQrefDetailEntity sameAsOne = new ApeQrefDetailEntity("reqId", "qr1", "name1", "val1", 0);
        ApeQrefDetailEntity differentKey = new ApeQrefDetailEntity("reqId2", "qr2", "name2", "val1", 1);
        ApeQrefDetailEntity nullKey = new ApeQrefDetailEntity(null, null, null, "val1", 0);

        assertThatEqualsAndHashcodeWork(one, sameAsOne, differentKey, nullKey);
    }

    @Test
    public void apeQrefDetailEntityPKShouldHaveEqualsAndHashCode(){
        ApeQrefDetailEntityPK one = new ApeQrefDetailEntityPK("reqId", "qr1", "name1");
        ApeQrefDetailEntityPK sameAsOne = new ApeQrefDetailEntityPK("reqId", "qr1", "name1");
        ApeQrefDetailEntityPK differentKey = new ApeQrefDetailEntityPK("reqId2", "qr2", "name2");
        ApeQrefDetailEntityPK nullKey = new ApeQrefDetailEntityPK(null, null, null);

        assertThatEqualsAndHashcodeWork(one, sameAsOne, differentKey, nullKey);
    }

    @Test
    public void shouldConvertDtoToEntity() {
        final String uniqueId = "uniqueId";
        final String requestId = "requestId";
        ApeQrefRequestDTO requestDto = new ApeQrefRequestDTO(uniqueId, null, null,
                                                                    anUser().withLoginName("User1").build(),
                                                                    "GBP",
                                                                    newArrayList(new ApeQrefRequestDTO.AssetAttribute("NON STANDARD", "Yes")), null, null, null, null, null, AsIsAsset.NIL, null, ChangeType.ADD, null,"1234", "5678");

        ApeRequestEntity entity = ApeRequestEntity.toEntity(requestId, uniqueId, requestDto);
        assertThat(entity, is(new ApeRequestEntity(requestId, uniqueId, "User1", "GBP", new ApeRequestDetailEntity(requestId, "NON STANDARD", "Yes"))));
    }

    @Test
    public void shouldSetSiteTelephoneNumberIfSiteIsPresentInRequestDTO() throws Exception {
        final String uniqueId = "uniqueId";
        final String requestId = "requestId";
        ApeQrefRequestDTO requestDto = new ApeQrefRequestDTO(uniqueId, null, SiteDTOFixture.aSiteDTO().withPhoneNumber("1234").build(),
                                                                    anUser().withLoginName("User1").build(),
                                                                    "GBP",
                                                                    newArrayList(new ApeQrefRequestDTO.AssetAttribute("NON STANDARD", "Yes")), null, null, null, null, null, AsIsAsset.NIL, null, ChangeType.ADD, null,"1234", "5678");

        ApeRequestEntity entity = ApeRequestEntity.toEntity(requestId, uniqueId, requestDto);
        assertThat(entity.getSiteTelNumber(), is("1234"));
    }

    @Test
    public void shouldSetAccessMethodTypeInRequestDTO() throws Exception {
        final String uniqueId = "uniqueId";
        final String requestId = "requestId";
        ApeQrefRequestDTO requestDto1 = new ApeQrefRequestDTO(uniqueId, null, SiteDTOFixture.aSiteDTO().withPhoneNumber("1234").build(),
                                        anUser().withLoginName("User1").build(),
                                        "GBP",newArrayList(new ApeQrefRequestDTO.AssetAttribute("NON STANDARD", "Yes")),
                                        null, null, null, null, null, AsIsAsset.NIL, null, ChangeType.ADD, "","1234","5678");

        ApeRequestEntity entity1 = ApeRequestEntity.toEntity(requestId, uniqueId, requestDto1);
        assertThat(entity1.getAccessMethodType(), is(""));

        ApeQrefRequestDTO requestDto2 = new ApeQrefRequestDTO(uniqueId, null, SiteDTOFixture.aSiteDTO().withPhoneNumber("1234").build(),
                                        anUser().withLoginName("User1").build(),
                                        "GBP",newArrayList(new ApeQrefRequestDTO.AssetAttribute("NON STANDARD", "Yes")),
                                        null, null, null, null, null, AsIsAsset.NIL, null, ChangeType.ADD, "MBP Access","1234","5678");

        ApeRequestEntity entity2 = ApeRequestEntity.toEntity(requestId, uniqueId, requestDto2);
        assertThat(entity2.getAccessMethodType(), is("MBP Access"));

        }
}
