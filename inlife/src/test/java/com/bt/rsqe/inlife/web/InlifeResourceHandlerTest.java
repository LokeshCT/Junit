package com.bt.rsqe.inlife.web;

import com.bt.rsqe.web.Presenter;
import com.bt.rsqe.web.staticresources.FileSystemStaticResourceLoader;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.io.File;
import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class InlifeResourceHandlerTest {

    private FileSystemStaticResourceLoader resourceLoader;
    private InlifeResourceHandler inlifeResourceHandler;

    @Before
    public void setup() {
        resourceLoader = mock(FileSystemStaticResourceLoader.class);
        File file1 = mock(File.class);
        File file2 = mock(File.class);
        when(resourceLoader.list()).thenReturn(newArrayList(file1, file2));

        when(file1.getName()).thenReturn("RSQE1.log");
        when(file2.getName()).thenReturn("cqm.gc");
        when(file1.compareTo(file2)).thenReturn(1);

        inlifeResourceHandler = new InlifeResourceHandler(new Presenter(), null, null, null, null, resourceLoader, null, null, null);
    }

    @Test
    public void shouldListAllRsqeRelatedLogs() {
        Response response = inlifeResourceHandler.getLogsFilesList();
        String responseContent = response.getEntity().toString();
        System.out.println();

        assertTrue(responseContent.contains("cqm.gc"));
        assertTrue(responseContent.contains("RSQE1.log"));
    }


    @Test
    public void shouldReturnMatchingResourcesWithAscOrder() {

        List<FileInfo> fileInfos = inlifeResourceHandler.matchingResources("(.*)\\.(log|gc)", resourceLoader);

        assertThat(fileInfos.size(), is(2));
        assertThat(fileInfos.get(0).getName(), is("cqm.gc"));
        assertThat(fileInfos.get(1).getName(), is("RSQE1.log"));
    }
}
