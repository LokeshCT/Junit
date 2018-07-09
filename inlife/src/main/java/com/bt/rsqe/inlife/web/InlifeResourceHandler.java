package com.bt.rsqe.inlife.web;

import com.bt.rsqe.ComponentNames;
import com.bt.rsqe.configuration.RestClientConfig;
import com.bt.rsqe.container.ApplicationConfig;
import com.bt.rsqe.inlife.config.InlifeConfig;
import com.bt.rsqe.inlife.monitoring.client.CustomerInventoryDiagnosticsResource;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.bt.rsqe.monitoring.C3P0ConnectionPoolInformationProvider;
import com.bt.rsqe.monitoring.ConnectionPoolInformationProvider;
import com.bt.rsqe.monitoring.DataSource;
import com.bt.rsqe.monitoring.HealthStatus;
import com.bt.rsqe.monitoring.MonitoringHealthDTO;
import com.bt.rsqe.monitoring.client.MonitoringHealthResource;
import com.bt.rsqe.monitoring.client.MonitoringInformationResource;
import com.bt.rsqe.monitoring.client.MonitoringStatsResource;
import com.bt.rsqe.monitoring.client.Uri;
import com.bt.rsqe.projectengine.web.view.PageView;
import com.bt.rsqe.taskscheduler.TaskSchedulerOrchestrator;
import com.bt.rsqe.utils.Clock;
import com.bt.rsqe.utils.JSONSerializer;
import com.bt.rsqe.utils.VersionClassFactory;
import com.bt.rsqe.web.Presenter;
import com.bt.rsqe.web.ViewFocusedResourceHandler;
import com.bt.rsqe.web.staticresources.FileSystemStaticResourceLoader;
import com.bt.rsqe.web.staticresources.UnableToLoadResourceException;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Maps.*;

@Path("/rsqe/inlife")
public class InlifeResourceHandler extends ViewFocusedResourceHandler {

    private static final String INLIFE_MONITORING = "Inlife Monitoring";
    private static final String VIEW = "view";
    MonitoringLog LOG = LogFactory.createDefaultLogger(MonitoringLog.class);

    private final InlifeConfig configuration;
    private TaskSchedulerOrchestrator dataArchivingSchedulerOrchestrator;
    private final FileSystemStaticResourceLoader bomResourceLoader;
    private final FileSystemStaticResourceLoader grabstateResourceLoader;
    private FileSystemStaticResourceLoader rsqeLogResourceLoader;
    private FileSystemStaticResourceLoader apacheLogResourceLoader;
    private final ApplicationConfig[] monitoredApplications;
    private List<ConnectionPoolInformationProvider> connectionPoolProviders;



    public InlifeResourceHandler(Presenter presenter, InlifeConfig configuration, TaskSchedulerOrchestrator dataArchivingSchedulerOrchestrator, String logsBasePath) {
        this(
            presenter,
            configuration,
            dataArchivingSchedulerOrchestrator,
            new FileSystemStaticResourceLoader(configuration.getStoredBomsPath()),
            new FileSystemStaticResourceLoader(configuration.getGrabstateArchivesPath()),
            new FileSystemStaticResourceLoader(logsBasePath),
            new FileSystemStaticResourceLoader(configuration.getApacheLogsBasePath()),
            configuration.getMonitoredApplications().getApplications(),
            Lists.newArrayList(C3P0ConnectionPoolInformationProvider.getInstance())
        );
    }

    public InlifeResourceHandler(Presenter presenter,
                                 InlifeConfig configuration,
                                 TaskSchedulerOrchestrator dataArchivingSchedulerOrchestrator,
                                 FileSystemStaticResourceLoader bomResourceLoader,
                                 FileSystemStaticResourceLoader grabstateResourceLoader,
                                 FileSystemStaticResourceLoader rsqeLogResourceLoader,
                                 FileSystemStaticResourceLoader apacheLogResourceLoader,
                                 ApplicationConfig[] monitoredApplications,
                                 List<ConnectionPoolInformationProvider> connectionPoolProviders) {
        super(presenter);
        this.configuration = configuration;
        this.dataArchivingSchedulerOrchestrator = dataArchivingSchedulerOrchestrator;
        this.bomResourceLoader = bomResourceLoader;
        this.grabstateResourceLoader = grabstateResourceLoader;
        this.rsqeLogResourceLoader = rsqeLogResourceLoader;
        this.apacheLogResourceLoader = apacheLogResourceLoader;
        this.monitoredApplications = monitoredApplications;
        this.connectionPoolProviders = connectionPoolProviders;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response index() {
        final PageView view = new PageView(INLIFE_MONITORING, "Index");
        return responseOk(presenter.render(view("Index.ftl")
                                               .withContext(VIEW, view)
                                               .withContext("rsqeVersion", VersionClassFactory.versionForModule("RsqeApp").all())
                                               .withContext("serverStartTime", ServerStartTime.get().toString())));
    }

    @GET
    @Path(Uri.BASE)
    @Produces(MediaType.TEXT_HTML)
    public Response getMonitoringInformation() {

        final PageView view = new PageView(INLIFE_MONITORING, "Monitoring Overview");

        final RestClientConfig.RestAuthenticationClientConfig authConfig = this.configuration.getRestAuthenticationClientConfig();

        Map<String, String> infoMap = new HashMap<String, String>();
        Map<String, String> statsMap = new HashMap<String, String>();
        Map<String, String> healthMap = new HashMap<String, String>();


        MonitoringInformationResource informationResource;
        MonitoringHealthResource healthResource;
        MonitoringStatsResource monitoringStatsResource;

        for (ApplicationConfig monitoredAppConfig : monitoredApplications) {
            String monitoredAppUrl = buildURL(monitoredAppConfig);
            try {
                LOG.collectingInformationFor(monitoredAppUrl);


                informationResource = new MonitoringInformationResource(new URI(monitoredAppUrl), authConfig.getSecret());
                infoMap.put(monitoredAppConfig.getId(), JSONSerializer.getInstance().serialize(informationResource.get()));

                healthResource = new MonitoringHealthResource(new URI(monitoredAppUrl), authConfig.getSecret());
                healthMap.put(monitoredAppConfig.getId(), JSONSerializer.getInstance().serialize(healthResource.get()));

                monitoringStatsResource = new MonitoringStatsResource(new URI(monitoredAppUrl), authConfig.getSecret());
                statsMap.put(monitoredAppConfig.getId(), JSONSerializer.getInstance().serialize(monitoringStatsResource.get()));

                LOG.informationCollectedFor(monitoredAppConfig.getId());
            } catch (Throwable e) {
                healthMap.put(monitoredAppConfig.getId(), "{'status':{'level':'RED','reason':'Component cannot be reached'},'timestamp':'" + Clock.timeNowFormatted() + "'}");
                LOG.failedMonitoringCall(monitoredAppConfig.getId(), monitoredAppUrl, e.getMessage());
            }
        }

        String page = presenter.render(view("Monitoring.ftl")
                                           .withContext(VIEW, view)
                                           .withContext("info", infoMap)
                                           .withContext("health", healthMap)
                                           .withContext("stats", statsMap));

        return responseOk(page);

    }

    @GET
    @Path(Uri.HEALTH)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHealthStatuses() {
        return getMonitoringInfoFor(new MonitoringDataHandler() {
            @Override
            public void doRetrieval(Map<String, Object> monitoringData, String monitoredAppUrl, ApplicationConfig monitoredAppConfig, RestClientConfig.RestAuthenticationClientConfig authConfig) throws URISyntaxException, JSONException {
                LOG.collectingHealthInformationFor(monitoredAppUrl);
                monitoringData.put(monitoredAppConfig.getId(), new MonitoringHealthResource(new URI(monitoredAppUrl), authConfig.getSecret()).get().asJSON());
                LOG.healthInformationCollectedFor(monitoredAppConfig.getId(), monitoredAppUrl);
            }

            @Override
            public void error(Map<String, Object> monitoringData, String monitoredAppUrl, ApplicationConfig monitoredAppConfig, Throwable e) {
                monitoringData.put(monitoredAppConfig.getId(), new MonitoringHealthDTO(HealthStatus.red("Component cannot be reached")));
                LOG.failedMonitoringCall(monitoredAppConfig.getId(), monitoredAppUrl, e.getMessage());
            }
        });
    }

    @GET
    @Path(Uri.INFO)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInfoStatuses() {
        return getMonitoringInfoFor(new MonitoringDataHandler() {
            @Override
            public void doRetrieval(Map<String, Object> monitoringData, String monitoredAppUrl, ApplicationConfig monitoredAppConfig, RestClientConfig.RestAuthenticationClientConfig authConfig) throws URISyntaxException, JSONException {
                LOG.collectingApplicationInfoFor(monitoredAppUrl);
                monitoringData.put(monitoredAppConfig.getId(), new MonitoringInformationResource(new URI(monitoredAppUrl), authConfig.getSecret()).get().asJSON());
                LOG.applicationInformationCollectedFor(monitoredAppConfig.getId(), monitoredAppUrl);
            }

            @Override
            public void error(Map<String, Object> monitoringData, String monitoredAppUrl, ApplicationConfig monitoredAppConfig, Throwable e) {
                LOG.failedMonitoringCall(monitoredAppConfig.getId(), monitoredAppUrl, e.getMessage());
            }
        });
    }

    @GET
    @Path(Uri.STATS)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStatsInformation() {
        return getMonitoringInfoFor(new MonitoringDataHandler() {
            @Override
            public void doRetrieval(Map<String, Object> monitoringData, String monitoredAppUrl, ApplicationConfig monitoredAppConfig, RestClientConfig.RestAuthenticationClientConfig authConfig) throws URISyntaxException, JSONException {
                LOG.collectingStatsFor(monitoredAppUrl);
                monitoringData.put(monitoredAppConfig.getId(), new MonitoringStatsResource(new URI(monitoredAppUrl), authConfig.getSecret()).get().asJSON());
                LOG.statsCollectedFor(monitoredAppConfig.getId(), monitoredAppUrl);
            }

            @Override
            public void error(Map<String, Object> monitoringData, String monitoredAppUrl, ApplicationConfig monitoredAppConfig, Throwable e) {
                LOG.failedMonitoringCall(monitoredAppConfig.getId(), monitoredAppUrl, e.getMessage());
            }
        });
    }

    @GET
    @Path("/boms")
    @Produces(MediaType.TEXT_HTML)
    public Response getBomList() {
        final PageView view = new PageView(INLIFE_MONITORING, "Generated BOMs");

        Map<String, Date> boms = new LinkedHashMap<String, Date>();
        final List<File> fileList = bomResourceLoader.list();
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File file, File file1) {
                // Sort by most recent first
                if (file1.lastModified() < file.lastModified()) {
                    return -1;
                }
                if (file1.lastModified() > file.lastModified()) {
                    return 1;
                }
                return 0;
            }
        });

        for (File bomFile : fileList) {
            if (bomFile.getName().matches("quoteOptionItemId_.*\\.log")) {
                boms.put(bomFile.getName(), new Date(bomFile.lastModified()));
            }
        }

        return responseOk(presenter.render(view("BomList.ftl")
                .withContext(VIEW, view)
                .withContext("boms", boms)));
    }

    @GET
    @Path("/boms/{bom}")
    @Produces(MediaType.TEXT_XML)
    public Response getBom(@PathParam("bom") String bom) {
        return tryToLoadResource(bom, bomResourceLoader);
    }


    @GET
    @Path("/grabstate-archives")
    @Produces(MediaType.TEXT_HTML)
    public Response getGrabstateFilesList() {
        final PageView view = new PageView(INLIFE_MONITORING, "Grabstate Archives");

        Collection<String> grabstateArchives = resourcesMatching("rsqe-state-.*\\.tgz", grabstateResourceLoader);

        return responseOk(presenter.render(view("GrabstateList.ftl")
                .withContext(VIEW, view)
                .withContext("grabstateArchives", grabstateArchives)));
    }

    @GET
    @Path("/grabstate-archives/{archive}")
    @Produces("application/x-compress")
    public Response getGrabstateArchive(@PathParam("archive") String archive) {
        return tryToLoadResource(archive, grabstateResourceLoader);
    }


    @GET
    @Path("/rsqe-logs")
    @Produces(MediaType.TEXT_HTML)
    public Response getLogsFilesList() {
        final PageView view = new PageView(INLIFE_MONITORING, "RSQE Logs");

        List<FileInfo> rsqeLogs = matchingResources("(.*)\\.(log|gc)", rsqeLogResourceLoader);

        return responseOk(presenter.render(view("RsqeLogsList.ftl")
                .withContext(VIEW, view)
                .withContext("rsqeLogs", rsqeLogs)));
    }

    @GET
    @Path("/rsqe-logs/{log}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getLog(@PathParam("log") String log) {
        return tryToLoadResource(log, rsqeLogResourceLoader);
    }


    @GET
    @Path("/apache-logs")
    @Produces(MediaType.TEXT_HTML)
    public Response getApacheLogsFilesList() {
        final PageView view = new PageView(INLIFE_MONITORING, "Apache Logs");

        List<String> apacheLogs = resourcesMatching(".*\\.log", apacheLogResourceLoader);

        return responseOk(presenter.render(view("ApacheLogsList.ftl")
                                               .withContext(VIEW, view)
                                               .withContext("apacheLogs", apacheLogs)));
    }

    @GET
    @Path("/apache-logs/{log}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getApacheLog(@PathParam("log") String log) {
        return tryToLoadResource(log, apacheLogResourceLoader);
    }

    @GET
    @Path("/diagnostics")
    @Produces(MediaType.TEXT_HTML)
    public Response diagnostics() {
        final PageView view = new PageView(INLIFE_MONITORING, "Diagnostics");
        return responseOk(presenter.render(view("Diagnostics.ftl").withContext(VIEW, view)));
    }

    @GET
    @Path("/diagnostics/line-item/{lineItemId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLineItem(@PathParam("lineItemId") String lineItemId) throws Throwable {
        try {
            final String secret = this.configuration.getRestAuthenticationClientConfig().getSecret();
            final CustomerInventoryDiagnosticsResource client =
                new CustomerInventoryDiagnosticsResource(new URI(appUrlFor(ComponentNames.CIF)), secret);
            return Response.ok().entity(client.getLineItemDetail(lineItemId)).build();
        } catch (Throwable e) {
            LOG.failedToGetLineItem(lineItemId, e.getMessage());
            throw e;
        }
    }


    @GET
    @Path(Uri.CONNECTIONS)
    @Produces(MediaType.TEXT_HTML)
    public Response getConnectionPoolInfo() {
        final PageView view = new PageView(INLIFE_MONITORING, "Connection Pool Overview");
        Set<DataSource> dataSourcesSortedBySourceName = Sets.newTreeSet(new Comparator<DataSource>() {
            @Override
            public int compare(DataSource o1, DataSource o2) {
                try {
                    return o1.getDataSourceName().compareTo(o2.getDataSourceName());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        for (ConnectionPoolInformationProvider connectionPoolInfoProvider : connectionPoolProviders) {
            final Set<DataSource> dataSourcesForProvider = connectionPoolInfoProvider.getDataSourceInfo();
            for (DataSource dataSource : dataSourcesForProvider) {
                dataSourcesSortedBySourceName.add(dataSource);
            }
        }

        return responseOk(presenter.render(view("Connections.ftl")
                                               .withContext(VIEW, view)
                                               .withContext("dataSources", dataSourcesSortedBySourceName)));
    }

    @GET
    @Path("/trigger-data-archiving/")
    @Produces(MediaType.TEXT_PLAIN)
    public Response triggerDataArchivingTask() {
        try {
            //TODO: Is this service required? Chris B to check.
            dataArchivingSchedulerOrchestrator.runScheduledTask("", "", new Date());
        } catch (Exception e) {
            LOG.dataArchivingTaskError(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return Response.status(Response.Status.OK).build();
    }

    private String appUrlFor(ComponentNames cif) {
        for (ApplicationConfig appConfig : this.monitoredApplications) {
            if (appConfig.getId().startsWith(cif.toString())) {
                return buildURL(appConfig);
            }
        }
        throw new IllegalArgumentException("No configuration found for " + cif);
    }


    private String buildURL(ApplicationConfig config) {
        return config.getScheme() + "://" + config.getHost() + ":" + config.getPort();
    }

    private Response getMonitoringInfoFor(MonitoringDataHandler handler) {
        final RestClientConfig.RestAuthenticationClientConfig authConfig = this.configuration.getRestAuthenticationClientConfig();
        Map<String, Object> monitoringData = newHashMap();
        for (ApplicationConfig monitoredAppConfig : monitoredApplications) {
            String monitoredAppUrl = buildURL(monitoredAppConfig);
            try {
                handler.doRetrieval(monitoringData, monitoredAppUrl, monitoredAppConfig, authConfig);
            } catch (Throwable e) {
                handler.error(monitoringData, monitoredAppUrl, monitoredAppConfig, e);
            }
        }

        return Response.ok().entity(new JSONObject(monitoringData)).build();
    }

    private interface MonitoringDataHandler {
        void doRetrieval(Map<String, Object> monitoringData, String monitoredAppUrl, ApplicationConfig monitoredAppConfig, RestClientConfig.RestAuthenticationClientConfig authConfig) throws URISyntaxException, JSONException;

        void error(Map<String, Object> monitoringData, String monitoredAppUrl, ApplicationConfig monitoredAppConfig, Throwable e);
    }


    private List<String> resourcesMatching(final String regex, FileSystemStaticResourceLoader resourceLoader) {

        List<String> files = newArrayList(Iterables.filter(Lists.transform(resourceLoader.list(), new Function<File, String>() {
            @Override
            public String apply(File file) {
                if (file.getName().matches(regex)) {
                    return file.getName();
                } else {
                    return "";
                }
            }
        }), new Predicate<String>() {
            @Override
            public boolean apply(final String input) {
                return !Strings.isNullOrEmpty(input);
            }

        }));
        Collections.sort(files, String.CASE_INSENSITIVE_ORDER);
        return files;
    }

    List<FileInfo> matchingResources(final String regex, FileSystemStaticResourceLoader resourceLoader) {

        List<File> interestedFiles = newArrayList(Iterables.filter(resourceLoader.list(), new Predicate<File>() {
            @Override
            public boolean apply(File input) {
                return input.getName().matches(regex);
            }
        }));

        Collections.sort(interestedFiles, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2)
            {
                return o1.getName().compareToIgnoreCase(o2.getName()) ;
            }
        });

        return newArrayList(Iterables.transform(interestedFiles, new Function<File, FileInfo>() {
            @Override
            public FileInfo apply(File input) {
                long length = input.length();;
                return new FileInfo(input.getName(), new Date(input.lastModified()), length);
            }
        }));
    }

    private Response tryToLoadResource(String name, FileSystemStaticResourceLoader resourceLoader) {
        try {
            return Response.ok().entity(resourceLoader.loadResourceAsStream(name)).build();
        } catch (UnableToLoadResourceException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    private interface MonitoringLog {

        @Log(level = LogLevel.INFO, format = "Contacting %s for monitoring information")
        void collectingInformationFor(String monitoredAppUrl);

        @Log(level = LogLevel.INFO, format = "Collected monitoring information for %s")
        void informationCollectedFor(String componentName);

        @Log(level = LogLevel.WARN, format = "Failed to retrieve monitoring data for %s: %s")
        void failedMonitoringCall(String message, String monitoredAppUrl, String eMessage);

        @Log(level = LogLevel.WARN, format = "Failed to get details for %s: %s")
        void failedToGetLineItem(String lineItemId, String eMessage);

        @Log(level = LogLevel.DEBUG, format = "Contacting %s for health-monitoring information")
        void collectingHealthInformationFor(String monitoredAppUrl);

        @Log(level = LogLevel.DEBUG, format = "Collected health-monitoring information for %s")
        void healthInformationCollectedFor(String id, String monitoredAppUrl);

        @Log(level = LogLevel.DEBUG, format = "Contacting %s for application information")
        void collectingApplicationInfoFor(String monitoredAppUrl);

        @Log(level = LogLevel.DEBUG, format = "Collected application information for %s")
        void applicationInformationCollectedFor(String id, String monitoredAppUrl);

        @Log(level = LogLevel.DEBUG, format = "Contacting %s for stats information")
        void collectingStatsFor(String monitoredAppUrl);

        @Log(level = LogLevel.DEBUG, format = "Collected stats information for %s")
        void statsCollectedFor(String id, String monitoredAppUrl);

        @Log(level = LogLevel.DEBUG, format = "Error while triggering data archiving task: %s")
        void dataArchivingTaskError(String error);
    }
}
