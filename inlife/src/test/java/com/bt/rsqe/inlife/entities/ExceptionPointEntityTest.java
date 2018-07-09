package com.bt.rsqe.inlife.entities;

import com.bt.rsqe.monitoring.ExceptionPointDTO;
import org.hamcrest.core.Is;
import org.junit.Test;

import static org.junit.Assert.*;

public class ExceptionPointEntityTest
{
    @Test
    public void testToAndFromDto()
    {
        ExceptionPointEntity exceptionPointEntity = new ExceptionPointEntity("Configure Asset");
        assertNotNull(exceptionPointEntity);
        ExceptionPointDTO exceptionPointDTO = exceptionPointEntity.toDto();
        assertNotNull(exceptionPointDTO);
        assertThat(exceptionPointDTO, Is.is(ExceptionPointDTO.class));
        assertEquals("Configure Asset", exceptionPointDTO.getExceptionPoint());

        ExceptionPointEntity newEntity = ExceptionPointEntity.fromDto(exceptionPointDTO);
        assertThat(newEntity, Is.is(ExceptionPointEntity.class));
        assertEquals(exceptionPointEntity, newEntity);
    }
}