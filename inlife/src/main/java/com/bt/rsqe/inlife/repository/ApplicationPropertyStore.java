package com.bt.rsqe.inlife.repository;

import com.bt.rsqe.inlife.client.dto.ApplicationProperty;
import com.bt.rsqe.persistence.JPAPersistenceManager;

public class ApplicationPropertyStore {
    private JPAPersistenceManager persistence;

    public ApplicationPropertyStore(JPAPersistenceManager persistence) {
        this.persistence = persistence;
    }

    public ApplicationProperty getProperty(String name) {
        final ApplicationPropertyEntity entity = persistence.get(ApplicationPropertyEntity.class, name);
        if(null != entity) {
            return entity.dto();
        }
        return null;
    }

    public ApplicationProperty getQuoteOptionProperty(String quoteOptionId, String name) {
        if(null != quoteOptionId) {
            final QuoteOptionApplicationPropertyEntity entity = persistence.get(QuoteOptionApplicationPropertyEntity.class,
                                                                                new QuoteOptionApplicationPropertyEntity.QuoteOptionApplicationPropertyKey(quoteOptionId, name));

            if(null != entity) {
                return entity.dto();
            }
        }
        return null;
    }

    public void createProperty(ApplicationProperty property) {
        persistence.save(ApplicationPropertyEntity.fromDto(property));
    }
}
