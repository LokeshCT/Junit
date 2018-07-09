package com.bt.rsqe.ape.repository.entities;

import com.bt.rsqe.ape.dto.ApeQrefError;
import com.bt.rsqe.util.Assertions;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class ApeQrefErrorEntityTest {
    @Test
    public void shouldHaveAWorkingEqualsAndHashCode() throws Exception {
        ApeQrefErrorEntity errorEntity1 = new ApeQrefErrorEntity("qref", "code", "error");
        ApeQrefErrorEntity errorEntity2 = new ApeQrefErrorEntity("qref", "code", "error");
        ApeQrefErrorEntity errorEntity3 = new ApeQrefErrorEntity("diffqref", "diffcode", "differror");

        Assertions.assertThatEqualsAndHashcodeWork(errorEntity1, errorEntity2, errorEntity3);
    }

    @Test
    public void shouldConvertEntityToDTO() throws Exception {
        ApeQrefError apeQrefError = new ApeQrefErrorEntity("qref", "code", "error").toDto();
        assertThat(apeQrefError.getErrorCode(), is("code"));
        assertThat(apeQrefError.getErrorMessage(), is("error"));
    }

    @Test
    public void shouldHaveAccessorMethods() throws Exception {
        ApeQrefErrorEntity errorEntity = new ApeQrefErrorEntity("qref", "code", "error");
        assertThat(errorEntity.getErrorCode(), is("code"));
        assertThat(errorEntity.getErrorMsg(), is("error"));
        assertThat(errorEntity.getQrefId(), is("qref"));
        assertThat(errorEntity.getErrorId(), is(not(nullValue())));
    }
}
