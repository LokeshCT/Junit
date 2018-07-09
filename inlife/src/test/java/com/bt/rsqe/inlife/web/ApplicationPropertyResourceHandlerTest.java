package com.bt.rsqe.inlife.web;

import com.bt.rsqe.inlife.client.dto.ApplicationProperty;
import com.bt.rsqe.inlife.repository.ApplicationPropertyStore;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.ws.rs.core.Response;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ApplicationPropertyResourceHandlerTest {
    private ApplicationPropertyResourceHandler handler;
    private ApplicationPropertyStore propertyStore;

    @Before
    public void setup() {
        propertyStore = mock(ApplicationPropertyStore.class);
        handler = new ApplicationPropertyResourceHandler(propertyStore);
    }

    @Test
    public void shouldGetPropertyForName() throws Exception {
        when(propertyStore.getProperty("someProperty")).thenReturn(new ApplicationProperty("someProperty", "someValue"));

        Response response = handler.getApplicationProperty("someProperty", null, null);

        assertThat(response.getStatus(), is(200));

        ApplicationProperty property = (ApplicationProperty)response.getEntity();

        assertThat(property.getName(), is("someProperty"));
        assertThat(property.getValue(), is("someValue"));
    }

    @Test
    public void shouldCreateAndReturnPropertyWithDefaultValueIfPropertyDoesNotExist() throws Exception {
        when(propertyStore.getProperty("someProperty")).thenReturn(null);

        Response response = handler.getApplicationProperty("someProperty", "defaultValue", null);
        assertThat(response.getStatus(), is(200));

        ApplicationProperty property = (ApplicationProperty)response.getEntity();

        assertThat(property.getName(), is("someProperty"));
        assertThat(property.getValue(), is("defaultValue"));

        ArgumentCaptor<ApplicationProperty> captor = ArgumentCaptor.forClass(ApplicationProperty.class);
        verify(propertyStore).createProperty(captor.capture());

        assertThat(captor.getValue().getName(), is("someProperty"));
        assertThat(captor.getValue().getValue(), is("defaultValue"));
    }

    @Test
    public void shouldUseQuoteOptionPropertyOverOverallPropertyIfOneExists() throws Exception {
        when(propertyStore.getQuoteOptionProperty("aQuoteOptionId", "someProperty")).thenReturn(new ApplicationProperty("someQuoteOptionProperty", "someValue"));

        Response response = handler.getApplicationProperty("someProperty", null, "aQuoteOptionId");

        assertThat(response.getStatus(), is(200));

        ApplicationProperty property = (ApplicationProperty)response.getEntity();

        assertThat(property.getName(), is("someQuoteOptionProperty"));
        assertThat(property.getValue(), is("someValue"));
    }
}
