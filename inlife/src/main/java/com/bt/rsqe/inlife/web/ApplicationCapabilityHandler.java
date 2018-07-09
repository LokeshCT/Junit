package com.bt.rsqe.inlife.web;

import com.bt.rsqe.inlife.client.ApplicationCapabilityProvider;
import com.bt.rsqe.inlife.client.dto.ApplicationProperty;
import com.bt.rsqe.inlife.config.InlifeConfig;
import com.bt.rsqe.inlife.repository.ApplicationPropertyStore;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.bt.rsqe.projectengine.web.view.PageView;
import com.bt.rsqe.web.Presenter;
import com.bt.rsqe.web.ViewFocusedResourceHandler;
import com.google.common.base.Optional;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static com.google.common.collect.Lists.newArrayList;

@Path(ApplicationCapabilityHandler.BASE_PATH)
public class ApplicationCapabilityHandler extends ViewFocusedResourceHandler {

    public static final String BASE_PATH = "/rsqe/inlife/capability";
    private static final String INLIFE_MONITORING = "Inlife Monitoring";
    private static final String VIEW = "view";
    Logger LOGGER = LogFactory.createDefaultLogger(Logger.class);

    private final InlifeConfig configuration ;
    private final ApplicationPropertyStore applicationPropertyStore ;


    public ApplicationCapabilityHandler(Presenter presenter,
                                        InlifeConfig configuration,
                                        ApplicationPropertyStore applicationPropertyStore) {
        super(presenter);
        this.configuration = configuration;
        this.applicationPropertyStore = applicationPropertyStore;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response index() {
        LOGGER.index() ;
        ApplicationProperty enabledProp = applicationPropertyStore.getProperty(ApplicationCapabilityProvider.Capability.ENABLE_CAPABILITY_WEBAPP.getName());
        boolean enabled = (enabledProp != null) && Boolean.parseBoolean(enabledProp.getValue()) ;
        final PageView view = new PageView(INLIFE_MONITORING, "Application Capability");
        List<ApplicationCapabilityInfo> applicationCapabilityInfoList = allProperties () ;
        return responseOk(presenter.render(view("ApplicationCapability.ftl")
                .withContext(VIEW, view)
                .withContext("enabled", enabled)
                .withContext("basePath", BASE_PATH)
                .withContext("applicationCapabilityInfoList", applicationCapabilityInfoList)));
    }


    @GET
    @Path ("/{name}/{value}")
    @Produces(MediaType.TEXT_HTML)
    public Response update(@PathParam("name") String name, @PathParam("value") Boolean value)
    {
        LOGGER.update(name, value) ;
        updateProperty(name, value) ;
        return responseRedirect(UriBuilder.fromUri(BASE_PATH).build()) ;
    }

    private void updateProperty(String name, Boolean value) {
        ApplicationProperty property = new ApplicationProperty(name, value.toString());
        applicationPropertyStore.createProperty(property);
    }


    private Boolean booleanValueOf(String value)
    {
        if (value == null)
        {
            return null ;
        }
        return Boolean.valueOf(value) ;
    }

    private List<ApplicationCapabilityInfo> allProperties()
    {
        List<ApplicationCapabilityInfo> result = newArrayList();
        for (ApplicationCapabilityProvider.Capability capability : ApplicationCapabilityProvider.Capability.values())
        {
            ApplicationProperty prop = applicationPropertyStore.getProperty(capability.getName());
            ApplicationCapabilityInfo applicationCapabilityInfo = new ApplicationCapabilityInfo(
                    capability.getName(),
                    capability.getDescription(),
                    (prop == null) ? null : booleanValueOf(prop.getValue()));
            LOGGER.property(applicationCapabilityInfo) ;
            result.add (applicationCapabilityInfo) ;
        }

        Collections.sort(result, new Comparator<ApplicationCapabilityInfo>() {
            @Override
            public int compare(ApplicationCapabilityInfo o1, ApplicationCapabilityInfo o2)
            {
                return Objects.compare(o1.getName(), o2.getName(), String.CASE_INSENSITIVE_ORDER) ;
            }
        });

        return result ;
    }

    private interface Logger {

        @Log(level = LogLevel.DEBUG, format="Index page for Application Capability")
        void index();

        @Log(level = LogLevel.INFO, format="name=%s value=%s")
        void update(String name, Boolean value);

        @Log(level = LogLevel.DEBUG, format="applicationCapabilityInfo=%s")
        void property(ApplicationCapabilityInfo applicationCapabilityInfo);
    }
}
