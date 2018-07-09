package com.bt.rsqe.projectengine.web.js;

import com.bt.rsqe.web.Presenter;
import com.bt.rsqe.web.View;
import com.bt.rsqe.web.staticresources.ClasspathStaticResourceLoader;
import com.bt.rsqe.web.staticresources.StaticResourceLoader;
import com.bt.rsqe.web.staticresources.UnableToLoadResourceException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import java.util.Random;

import static javax.ws.rs.core.Response.*;

public class JavaScriptTestFilter implements ContainerResponseFilter {

    private StaticResourceLoader resourceLoader;
    private Presenter presenter;
    private JSTestViewModel model;

    public JavaScriptTestFilter() {
        resourceLoader = new ClasspathStaticResourceLoader("");
        presenter = new Presenter();
    }

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) {

        try {
            String rawPath = request.getUriInfo().getAbsolutePath().getRawPath();
            if (rawPath.contains(".js") || rawPath.endsWith("css") || rawPath.endsWith("jpg")|| rawPath.endsWith("png")|| rawPath.endsWith("gif")) {
                byte[] entity = resourceLoader.loadBinaryResource(rawPath);
                response.setEntity(entity);
            } else {
                String responseEntity = presenter.render(View.viewUsingTemplate(rawPath).withContext("model", model));
                response.setEntity(responseEntity);
            }
            response.setStatus(200);
        } catch (UnableToLoadResourceException e) {
            throw new RuntimeException(e);
        }
    }

    public void setupContextForResponse(final String htmlUnderTest, final String[] jsPaths) {
        disableCaching(jsPaths);
        model = new JSTestViewModel(htmlUnderTest, jsPaths);
    }

    private void disableCaching(String[] jsPaths) {
        Random random = new Random();
        for (int i = 0; i < jsPaths.length; i++) {
            jsPaths[i] += "?" + random.nextInt();
        }
    }
}

