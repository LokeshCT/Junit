package com.bt.rsqe.projectengine.web.pageframework;

import com.bt.rsqe.ProjectEngineWebEnvironmentTestConfig;
import com.bt.rsqe.configuration.ConfigurationProvider;
import com.bt.rsqe.container.Application;
import com.bt.rsqe.container.ioc.DefaultResourceHandlerFactory;
import com.bt.rsqe.container.ioc.ResourceHandlerFactory;
import com.bt.rsqe.projectengine.server.ProjectEngineStaticResourceHandler;
import com.bt.rsqe.utils.Environment;
import com.bt.rsqe.web.Presenter;
import com.bt.rsqe.web.View;
import com.bt.rsqe.web.staticresources.StaticResourceLoaderFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Path("/page-framework")
@Produces(MediaType.TEXT_HTML)
public class PageFrameworkApplication extends Application {

    private Presenter presenter;

    public PageFrameworkApplication() {
        super(ConfigurationProvider.provide(ProjectEngineWebEnvironmentTestConfig.class, Environment.env())
                                   .getProjectEngineWebConfig()
                                   .getApplicationConfig());
        presenter = new Presenter();
    }

    @Override
    protected ResourceHandlerFactory createResourceHandlerFactory() {
        return new DefaultResourceHandlerFactory() {{
            withSingleton(PageFrameworkApplication.this);
            withSingleton(new ProjectEngineStaticResourceHandler(new StaticResourceLoaderFactory().create()));
        }};
    }

    @GET
    public Response getPage() {
        final PageFrameworkTestView view = new PageFrameworkTestView();
        String html = presenter.render(basePage()
                                           .withContext("view", view
                                               .addTab("PageFrameworkTestTabA", "Tab A", "/page-framework/tab-a")
                                               .addTab("PageFrameworkTestTabB", "Tab B", "/page-framework/tab-b")
                                           ).withContext("additionalImport", "<script type=\"text/javascript\" src=\"/rsqe/project-engine/static/scripts/PageFrameworkTest.js\"></script>")
                                           .withContext("submitWebMetricsUri", "/web-metrics"));
        return Response.ok().entity(html).build();
    }

    @GET
    @Path("/tab-a")
    public Response getTabA() {
        String html = presenter.render(view("TabA.ftl"));
        return Response.ok().entity(html).build();
    }

    @GET
    @Path("/tab-b")
    public Response getTabB() {
        String html = presenter.render(view("TabB.ftl"));
        return Response.ok().entity(html).build();
    }

    private View basePage() {
        return View.viewUsingTemplate("/com/bt/rsqe/projectengine/web/BasePage.ftl");
    }

    private final View view(String templateName) {
        String templatePath = pathToTemplate(templateName);
        return View.viewUsingTemplate(templatePath);
    }

    private String pathToTemplate(final String template) {
        return this.getClass().getPackage().getName().replaceAll("\\.", "/") + "/" + template;
    }


    public static void main(String...args) throws IOException {
        new PageFrameworkApplication().start();
    }
}
