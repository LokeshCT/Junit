package com.bt.rsqe.projectengine.web.error;

import com.bt.rsqe.ProjectEngineWebEnvironmentTestConfig;
import com.bt.rsqe.config.CookieConfig;
import com.bt.rsqe.config.CookieDomainConfig;
import com.bt.rsqe.configuration.ConfigurationProvider;
import com.bt.rsqe.container.ioc.DefaultResourceHandlerFactory;
import com.bt.rsqe.container.ioc.ResourceHandlerFactory;
import com.bt.rsqe.error.RsqeApplicationException;
import com.bt.rsqe.error.web.WebExceptionMapper;
import com.bt.rsqe.projectengine.server.ProjectEngineApplication;
import com.bt.rsqe.projectengine.server.ProjectEngineWebConfig;
import com.bt.rsqe.utils.Environment;
import com.bt.rsqe.web.Presenter;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import java.io.IOException;

import static com.bt.rsqe.projectengine.web.fixtures.SessionServiceClientResourcesFixture.*;

@SuppressWarnings("PMD.TooManyMethods")
public class ErrorHandlingFrameworkTest {

    private static final String RSQE_ERROR_PAGE_TITLE = "RSQE Error";
    private static final String ERROR_PAGE_HEADING = "Whoops, it seems something went wrong!";
    private static final String DATABASE_EXCEPTION_HAPPENED = "DATABASE_EXCEPTION_HAPPENED";
    private static final String FREEMARKER_EXCEPTION_MESSAGE = "RSQE Internal Server Error";
    private static final String TEST_EXCEPTION_MESSAGE = "TEST_MESSAGE";
    private static final String TEST_UNCAUGHT_EXCEPTION_MESSAGE = "RunTime Error";
    private static final String TEST_CHECKED_EXCEPTION_MESSAGE = "IO Exception";



    private static final String FREEMARKER_EXCEPTION_PATH = "http://%s:%s/rsqe/static/exception/freemarker";
    private static final String RUNTIME_EXCEPTION_PATH = "http://%s:%s/rsqe/static/exception/runtime";
    private static final String UNCAUGHT_RUNTIME_EXCEPTION_PATH = "http://%s:%s/rsqe/static/exception/runtimeuncaught";
    private static final String CHECKED_EXCEPTION_PATH = "http://%s:%s/rsqe/static/exception/checkedexception";
    private static final String CORRECTPAGE_PATH = "http://%s:%s/rsqe/static/exception/correct";

    private ErrorHandlingPage errorHandlingPage;
    private static boolean filterShouldThrowException;

    private static WebDriver driver = new HtmlUnitDriver();
    private static ProjectEngineApplication projectEngineApplication;

    private static String host = "";
    private static String port = "";


    @Before
    public void before(){

        ProjectEngineWebConfig config = ConfigurationProvider.provide(ProjectEngineWebEnvironmentTestConfig.class, Environment.env()).getProjectEngineWebConfig();
        host = config.getApplicationConfig().getHost();
        port = String.valueOf(config.getApplicationConfig().getPort());


      filterShouldThrowException = false;
    }
    @BeforeClass
    public static void setUp() throws IOException {

        projectEngineApplication = new ProjectEngineApplication(ConfigurationProvider.provide(ProjectEngineWebEnvironmentTestConfig.class, Environment.env()).getProjectEngineWebConfig(), aFakeSessionService().build(), new CookieConfig() {
            @Override
            public CookieDomainConfig getCookieDomainConfig() {
                return new CookieDomainConfig() {
                    @Override
                    public String getValue() {
                        return "someDomainName";
                    }

                    @Override
                    public String getOn() {
                        return "false";
                    }
                };
            }
        }) {
            {
                ContainerResponseFilter responseFilter = new ContainerResponseFilter() {
                    @Override
                    public void filter(ContainerRequestContext request, ContainerResponseContext response) {
                        try {
                            if (filterShouldThrowException) {
                                throw new RuntimeException();
                            }
                        } catch (Exception exception) {
                            throw new RsqeApplicationException(exception, "DATABASE_EXCEPTION_HAPPENED");
                        }
                    }
                };

                applicationContainerInstance().addContainerResponseFilter(responseFilter);
                applicationContainerInstance().register(WebExceptionMapper.class);
            }

            @Override
            protected ResourceHandlerFactory createResourceHandlerFactory() {

                final ResourceHandlerFactory resourceHandlerFactory = super.createResourceHandlerFactory();
                return new DefaultResourceHandlerFactory() {
                    {
                        for (Class<?> aClass : resourceHandlerFactory.getResourceClasses()) {
                            withSingleton(resourceHandlerFactory.getResourceHandler(aClass));
                        }
                        withSingleton(new EndToEndErrorSimulationResourceHandler(new Presenter()));
                    }
                };

            }
        };
        projectEngineApplication.start();
    }

    @AfterClass
    public static void tearDown() throws Exception {
       projectEngineApplication.stop();
    }

    @Test
    public void shouldDisplayErrorPageForApplicationExceptionThrown() throws Exception {
        driver.get(String.format(RUNTIME_EXCEPTION_PATH,host,port));
        errorHandlingPage = new ErrorHandlingPage(driver);
        checkCommonAssertions();
        errorHandlingPage.assertErrorMessage(TEST_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldDisplayErrorPageForUnCaughtRuntimeException() throws Exception {
        driver.get(String.format(UNCAUGHT_RUNTIME_EXCEPTION_PATH,host,port));
        errorHandlingPage = new ErrorHandlingPage(driver);
        checkCommonAssertions();
        errorHandlingPage.assertErrorMessage(TEST_UNCAUGHT_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldDisplayErrorPageForCheckedException() throws Exception {
        driver.get(String.format(CHECKED_EXCEPTION_PATH,host,port));
        errorHandlingPage = new ErrorHandlingPage(driver);
        checkCommonAssertions();
        errorHandlingPage.assertErrorMessage(TEST_CHECKED_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldDisplayFreemarkerErrorPageForFreemarkerTemplateError() throws Exception {
        driver.get(String.format(FREEMARKER_EXCEPTION_PATH,host,port));
        errorHandlingPage = new ErrorHandlingPage(driver);
        checkCommonAssertions();
        errorHandlingPage.assertErrorMessage(FREEMARKER_EXCEPTION_MESSAGE);
    }

    @Test
    @Ignore("Leela to fix this")
    public void shouldDisplayErrorPageForDatabaseExceptionThrown() throws Exception {
        filterShouldThrowException = true;
        driver.get(String.format(CORRECTPAGE_PATH,host,port));
        errorHandlingPage = new ErrorHandlingPage(driver);
        checkCommonAssertions();
        errorHandlingPage.assertErrorMessage(DATABASE_EXCEPTION_HAPPENED);
    }

    private void checkCommonAssertions() {
        errorHandlingPage.assertTitleIs(RSQE_ERROR_PAGE_TITLE);
        errorHandlingPage.assertHeaderIs(ERROR_PAGE_HEADING);
    }


}
