package com.bt.rsqe.customerinventory.service.updates;

import org.junit.Test;

import static com.bt.rsqe.domain.product.AttributeDataType.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class CIFCharacteristicValueTest {

    @Test
    public void shouldHandleNullValue () throws Exception
    {
        CIFCharacteristicValue cifCharacteristicValue = new CIFCharacteristicValue();
        assertThat(cifCharacteristicValue.valueOf(DATE, null), nullValue());
    }

    @Test
    public void shouldChangeDateToCIFFormat() {
        CIFCharacteristicValue cifCharacteristicValue = new CIFCharacteristicValue();
        assertThat(cifCharacteristicValue.valueOf(DATE, "2015-Jun-10"), is("2015/06/10 00:00"));
    }

    @Test
    public void shouldNotChangeGivenDateToCIFFormatWhenItsAlreadyACIFFormatDate() {
        CIFCharacteristicValue cifCharacteristicValue = new CIFCharacteristicValue();
        assertThat(cifCharacteristicValue.valueOf(DATE, "2015/06/10 12:00"), is("2015/06/10 12:00"));
        assertThat(cifCharacteristicValue.valueOf(DATE, "2015/06/10 00:00"), is("2015/06/10 00:00"));
        assertThat(cifCharacteristicValue.valueOf(DATE, "2015/06/10 23:59"), is("2015/06/10 23:59"));
    }

    @Test
    public void shouldJustReturnSameCharacteristicValueWhenDataTypeIsNotDate() {
        CIFCharacteristicValue cifCharacteristicValue = new CIFCharacteristicValue();
        assertThat(cifCharacteristicValue.valueOf(STRING, "someStringValue"), is("someStringValue"));
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionWhenGivenCharacteristicValueIsNotInExpectedDateFormat() {
        new CIFCharacteristicValue().valueOf(DATE, "yyyy-mm-dd");
    }
}