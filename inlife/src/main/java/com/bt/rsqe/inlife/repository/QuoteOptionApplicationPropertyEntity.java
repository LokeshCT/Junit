package com.bt.rsqe.inlife.repository;

import com.bt.rsqe.inlife.client.dto.ApplicationProperty;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "QUOTE_OPTION_APP_PROP_STORE")
public class QuoteOptionApplicationPropertyEntity {
    @EmbeddedId
    protected QuoteOptionApplicationPropertyKey key;

    @Column(name = "PROPERTY_VALUE")
    private String value;

    public QuoteOptionApplicationPropertyEntity() {}

    public QuoteOptionApplicationPropertyEntity(String quoteOptionId, String name, String value) {
        this.key = new QuoteOptionApplicationPropertyKey(quoteOptionId, name);
        this.value = value;
    }

    public ApplicationProperty dto() {
        return new ApplicationProperty(key.getName(), value);
    }

    @Embeddable
    public static class QuoteOptionApplicationPropertyKey implements Serializable {
        @Column(name = "QUOTE_OPTION_ID")
        private String quoteOptionId;
        @Column(name = "PROPERTY_NAME")
        private String name;

        public QuoteOptionApplicationPropertyKey() {}

        public QuoteOptionApplicationPropertyKey(String quoteOptionId, String name) {
            this.quoteOptionId = quoteOptionId;
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public int hashCode() {
            return HashCodeBuilder.reflectionHashCode(this);
        }

        @Override
        public boolean equals(Object obj) {
            return EqualsBuilder.reflectionEquals(this, obj);
        }
    }
}
