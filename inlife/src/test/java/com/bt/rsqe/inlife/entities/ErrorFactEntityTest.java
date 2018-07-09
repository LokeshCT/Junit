package com.bt.rsqe.inlife.entities;

import com.bt.rsqe.monitoring.ErrorFactDTO;
import org.hamcrest.core.Is;
import org.junit.Test;

import static org.junit.Assert.*;

public class ErrorFactEntityTest
{
    @Test
    public void testToDto()
    {
        UserEntity userEntity = new UserEntity("user-1", "type-1", "salesChannel-1");
        assertNotNull(userEntity);
        ExceptionPointEntity exceptionPointEntity = new ExceptionPointEntity("Configure Asset");
        assertNotNull(exceptionPointEntity);
        ErrorFactEntity errorFactEntity = new ErrorFactEntity("quoteOptionId", "quoteLineItemId", "This is an error message!", "test/url");
        errorFactEntity.setUserId(userEntity);
        errorFactEntity.setExceptionPointId(exceptionPointEntity);
        assertNotNull(errorFactEntity);
        ErrorFactDTO errorFactDTO = errorFactEntity.toDto();
        assertNotNull(errorFactDTO);
        assertThat(errorFactDTO, Is.is(ErrorFactDTO.class));
        assertEquals("quoteOptionId", errorFactDTO.getQuoteOptionId());
        assertEquals("quoteLineItemId", errorFactDTO.getQuoteLineItemId());
        assertEquals("This is an error message!", errorFactDTO.getErrorMessage());
        assertEquals("test/url", errorFactDTO.getUrl());

        ErrorFactEntity newEntity = ErrorFactEntity.fromDto(errorFactDTO);
        assertThat(newEntity, Is.is(ErrorFactEntity.class));
        assertEquals(errorFactEntity, newEntity);
    }
}