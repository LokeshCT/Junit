package com.bt.rsqe.inlife.entities;

import com.bt.rsqe.monitoring.UserDTO;
import org.hamcrest.core.Is;
import org.junit.Test;

import static org.junit.Assert.*;

public class UserEntityTest
{
    @Test
    public void testToAndFromDto()
    {
        UserEntity userEntity = new UserEntity("user-1", "type-1", "salesChannel-1");
        assertNotNull(userEntity);
        UserDTO userDTO = userEntity.toDto();
        assertNotNull(userDTO);
        assertThat(userDTO, Is.is(UserDTO.class));
        assertEquals("user-1", userDTO.getUserIdentifier());
        assertEquals("type-1", userDTO.getType());
        assertEquals("salesChannel-1", userDTO.getSalesChanel());

        UserEntity newEntity = UserEntity.fromDto(userDTO);
        assertThat(newEntity, Is.is(UserEntity.class));
        assertEquals(userEntity, newEntity);
    }
}