package com.bt.rsqe.ape.matchers;

import com.bt.rsqe.ape.repository.entities.ApeQrefDetailEntity;
import org.hamcrest.Description;
import org.junit.matchers.TypeSafeMatcher;

public class APEQrefDetailEntityMatcher extends TypeSafeMatcher<ApeQrefDetailEntity> {
    private String attributeValue, requestId, qrefId, attributeName;
    private Integer sequence;

    public APEQrefDetailEntityMatcher(){

    }

    public APEQrefDetailEntityMatcher(String qrefId, String attributeName, String attributeValue, Integer sequence){
        this.qrefId = qrefId;
        this.attributeName = attributeName;
        this.attributeValue = attributeValue;
        this.sequence = sequence;
    }

    public static APEQrefDetailEntityMatcher anAPEQrefDetailEntity() {
        return new APEQrefDetailEntityMatcher();
    }

    public static APEQrefDetailEntityMatcher anAPEQrefDetailEntity(String qrefId, String attributeName, String attributeValue, Integer sequence) {
        return new APEQrefDetailEntityMatcher(qrefId, attributeName, attributeValue, sequence);
    }

    public APEQrefDetailEntityMatcher withAttributeValue(String attributeValue) {
        this.attributeValue = attributeValue;
        return this;
    }

    public APEQrefDetailEntityMatcher withAttributeName(String attributeName) {
        this.attributeName = attributeName;
        return this;
    }

    public APEQrefDetailEntityMatcher withRequestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

    public APEQrefDetailEntityMatcher withQrefId(String qrefId) {
        this.qrefId = qrefId;
        return this;
    }

    public APEQrefDetailEntityMatcher withSequence(Integer sequence) {
        this.sequence = sequence;
        return this;
    }

    @Override
    public boolean matchesSafely(ApeQrefDetailEntity expected) {
        ApeQrefDetailEntity expectedCopy = new ApeQrefDetailEntity(expected);
        if(requestId==null){
            expectedCopy.setRequestId(null);
        }
        return expectedCopy.equals(new ApeQrefDetailEntity(requestId, qrefId, attributeName, attributeValue, sequence));
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("Qref detail with with qrefId ")
                   .appendValue(qrefId)
                   .appendText(", requestId ")
                   .appendValue(requestId)
                   .appendText(", attributeName ")
                   .appendValue(attributeName)
                   .appendText(" and attributeValue ")
                   .appendValue(attributeValue);
    }
}
