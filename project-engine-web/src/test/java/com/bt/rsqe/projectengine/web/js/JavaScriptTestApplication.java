package com.bt.rsqe.projectengine.web.js;

import com.bt.rsqe.container.Application;
import com.bt.rsqe.container.ApplicationConfig;
import com.bt.rsqe.container.ioc.DefaultResourceHandlerFactory;
import com.bt.rsqe.container.ioc.ResourceHandlerFactory;
import com.bt.rsqe.error.web.ErrorResourceHandler;
import com.bt.rsqe.web.Presenter;

public class JavaScriptTestApplication extends Application {

    private JavaScriptTestFilter filter;

    public JavaScriptTestApplication(ApplicationConfig applicationConfig) {
        super(applicationConfig);
        filter = new JavaScriptTestFilter();
        this.applicationContainerInstance().addContainerResponseFilter(filter);
    }

    @Override
    protected ResourceHandlerFactory createResourceHandlerFactory() {
        return new DefaultResourceHandlerFactory() {
            {
                withSingleton(new ErrorResourceHandler(new Presenter()));
            }
        };
    }

    public void setupContextForResponse(String htmlUnderTest, String[] jsPaths) {
        filter.setupContextForResponse(htmlUnderTest, jsPaths);
    }
}
