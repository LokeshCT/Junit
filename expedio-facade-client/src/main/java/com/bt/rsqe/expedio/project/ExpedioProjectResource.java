package com.bt.rsqe.expedio.project;

import com.bt.rsqe.domain.product.extensions.RuleInterimSiteAttributeSource;
import com.bt.rsqe.web.ProjectResource;
import com.bt.rsqe.web.rest.ProxyAwareRestRequestBuilder;
import com.bt.rsqe.web.rest.RestRequestBuilder;
import com.bt.rsqe.web.rest.RestResponse;

import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class ExpedioProjectResource implements ProjectResource<ProjectDTO> {

    private RestRequestBuilder restRequestBuilder;
    private RestRequestBuilder clarityProjectrestRequestBuilder;

    public ExpedioProjectResource(URI baseUri, String secret) {
        URI uri = UriBuilder.fromUri(baseUri).path("rsqe").path("expedio").path("projects").build();
        restRequestBuilder = new ProxyAwareRestRequestBuilder(uri).withSecret(secret);
        URI clarityProjectUri = UriBuilder.fromUri(baseUri).path("rsqe").path("expedio").path("clarity").path("projects").build();
        clarityProjectrestRequestBuilder = new ProxyAwareRestRequestBuilder(clarityProjectUri).withSecret(secret);
    }

    public ProjectDTO getProject(final String projectId) {
        return restRequestBuilder.build(projectId).get().getEntity(ProjectDTO.class);
    }

    public void put(final String projectId, ProjectDTO projectDTO) {
        restRequestBuilder.build(projectId).put(projectDTO);
    }

    public void post(final String projectId, ProjectDTO projectDTO) {
        restRequestBuilder.build(projectId).post(projectDTO);
    }

    public ProjectDTO getProjectWithQuoteOptionName(final String projectId, final String quoteOptionName) {
        return restRequestBuilder.build(projectId, "quoteOptionName", quoteOptionName).get().getEntity(ProjectDTO.class);
    }

    public List<ClarityProjectDto> getClarityProject(ClarityProjectRequestDto requestDto) {

        RestResponse response= clarityProjectrestRequestBuilder.build().post(requestDto);
        List<ClarityProjectDto> list = response.getEntity(new GenericType<List<ClarityProjectDto>>() {
        });
        return list;
    }
}
