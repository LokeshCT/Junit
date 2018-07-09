package com.bt.rsqe.inlife.repository;

import com.bt.rsqe.inlife.client.dto.ApplicationProperty;
import com.bt.rsqe.persistence.JPAPersistenceManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class ApplicationPropertyStoreTest {
    private JPAPersistenceManager persistenceManager;
    private ApplicationPropertyStore store;

    @Before
    public void setup() {
        persistenceManager = mock(JPAPersistenceManager.class);
        store = new ApplicationPropertyStore(persistenceManager);
    }

    @Test
    public void shouldReturnNullWhenPropertyDoesNotExist() throws Exception {
        when(persistenceManager.get(ApplicationPropertyEntity.class, "someProperty")).thenReturn(null);
        ApplicationProperty property = store.getProperty("someProperty");
        assertThat(property, is(nullValue()));
    }

    @Test
    public void shouldGetProperty() throws Exception {
        when(persistenceManager.get(ApplicationPropertyEntity.class, "someProperty")).thenReturn(new ApplicationPropertyEntity("someProperty", "someValue"));
        ApplicationProperty property = store.getProperty("someProperty");
        assertThat(property.getName(), is("someProperty"));
        assertThat(property.getValue(), is("someValue"));
    }

    @Test
    public void shouldCreateProperty() throws Exception {
        ApplicationProperty property  = new ApplicationProperty("someProperty", "someValue");
        store.createProperty(property);

        ArgumentCaptor<ApplicationPropertyEntity> captor = ArgumentCaptor.forClass(ApplicationPropertyEntity.class);
        verify(persistenceManager).save(captor.capture());

        final ApplicationProperty dto = captor.getValue().dto();
        assertThat(dto.getName(), is("someProperty"));
        assertThat(dto.getValue(), is("someValue"));
    }

    @Test
    public void shouldGetQuoteOptionProperty() throws Exception {
        when(persistenceManager.get(QuoteOptionApplicationPropertyEntity.class,
                                    new QuoteOptionApplicationPropertyEntity.QuoteOptionApplicationPropertyKey("aQuoteOptionId", "someProperty")))
            .thenReturn(new QuoteOptionApplicationPropertyEntity("aQuoteOptionId", "someProperty", "someValue"));
        ApplicationProperty property = store.getQuoteOptionProperty("aQuoteOptionId", "someProperty");
        assertThat(property.getName(), is("someProperty"));
        assertThat(property.getValue(), is("someValue"));
    }

    @Test
    public void shouldReturnNullWhenQuoteOptionIdIsNull() throws Exception {
        assertThat(store.getQuoteOptionProperty(null, "someProperty"), is(nullValue()));
    }

    @Test
    public void shouldReturnNullWhenQuoteOptionPropertyDoesNotExist() throws Exception {
        assertThat(store.getQuoteOptionProperty("aQuoteOptionId", "someProperty"), is(nullValue()));
    }
}
