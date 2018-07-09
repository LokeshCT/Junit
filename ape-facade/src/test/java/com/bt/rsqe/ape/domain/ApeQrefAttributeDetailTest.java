package com.bt.rsqe.ape.domain;

import com.bt.rsqe.ape.dto.ApeQrefAttributeDetail;
import org.junit.Test;

import static com.bt.rsqe.util.Assertions.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class ApeQrefAttributeDetailTest {
    @Test
    public void testEquals() throws Exception {
        final ApeQrefAttributeDetail expected = new ApeQrefAttributeDetail("n1", "v1");
        final ApeQrefAttributeDetail sameAsExpected = new ApeQrefAttributeDetail("n1", "v1");
        final ApeQrefAttributeDetail different = new ApeQrefAttributeDetail("n1", "v2");
        final ApeQrefAttributeDetail different2 = new ApeQrefAttributeDetail("n2", "v1");
        assertThatEqualsAndHashcodeWork(expected, sameAsExpected, different, different2);
        assertThat(expected.hashCode(), is(not(different.hashCode())));
    }
}
