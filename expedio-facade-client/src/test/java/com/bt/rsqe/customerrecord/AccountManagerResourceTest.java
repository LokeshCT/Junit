package com.bt.rsqe.customerrecord;

import com.bt.rsqe.web.rest.RestRequestBuilder;
import com.bt.rsqe.web.rest.RestResource;
import com.bt.rsqe.web.rest.RestResponse;
import com.bt.rsqe.web.rest.exception.ResourceNotFoundException;
import com.google.common.base.Optional;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class AccountManagerResourceTest {
    @Test
    public void shouldGetAccountManagerByRole() {
        //Given
        RestRequestBuilder requestBuilder = mock(RestRequestBuilder.class);
        RestResponse restResponse = mock(RestResponse.class);
        RestResource restResource = mock(RestResource.class);

        AccountManagerDTO accountManagerDTO = new AccountManagerDTO();
        String[] segments = {"role", "anRole"};
        when(requestBuilder.withSecret("aSecret")).thenReturn(requestBuilder);
        when(requestBuilder.build(segments)).thenReturn(restResource);
        when(restResource.get()).thenReturn(restResponse);
        when(restResponse.getEntity(AccountManagerDTO.class)).thenReturn(accountManagerDTO);
        //When
        AccountManagerResource accountManagerResource = new AccountManagerResource(requestBuilder, "aSecret");
        Optional<AccountManagerDTO> accountManagerDTOOptional = accountManagerResource.getByRole("anRole");

        //Then
        verify(requestBuilder, times(1)).build(segments);
        verify(restResource, times(1)).get();
        assertThat(accountManagerDTOOptional.get(), is(accountManagerDTO));
    }

    @Test
    public void shouldGetOptionalAbsentAccountManagerWhenNotFound() {
        //Given
        RestRequestBuilder requestBuilder = mock(RestRequestBuilder.class);
        RestResponse restResponse = mock(RestResponse.class);
        RestResource restResource = mock(RestResource.class);

        String[] segments = {"role", "anRole"};
        when(requestBuilder.withSecret("aSecret")).thenReturn(requestBuilder);
        when(requestBuilder.build(segments)).thenReturn(restResource);
        when(restResource.get()).thenReturn(restResponse);
        doThrow(ResourceNotFoundException.class).when(restResponse).getEntity(AccountManagerDTO.class);
        //When
        AccountManagerResource accountManagerResource = new AccountManagerResource(requestBuilder, "aSecret");
        Optional<AccountManagerDTO> accountManagerDTOOptional = accountManagerResource.getByRole("anRole");

        //Then
        verify(requestBuilder, times(1)).build(segments);
        verify(restResource, times(1)).get();
        assertThat(accountManagerDTOOptional.isPresent(), is(false));
    }
}
