package com.bt.rsqe.sqefacade.domain;

import com.google.common.base.Predicate;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Arrays;

import static com.google.common.collect.Iterables.*;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public enum  RAGStatus {

    Complete("Green"), InProgress("Amber"), NotStarted("Red");

    private final String status;

    RAGStatus(String status) {
        this.status = status;
    }

    public static RAGStatus from(final String str) {
        return find(Arrays.asList(RAGStatus.values()), new Predicate<RAGStatus>() {
            @Override
            public boolean apply(RAGStatus input) {
                return input.status.equalsIgnoreCase(str);
            }
        });
    }

    @Override
    public String toString() {
        return status;
    }
}
