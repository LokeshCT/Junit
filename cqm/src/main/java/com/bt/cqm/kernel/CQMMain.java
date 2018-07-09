package com.bt.cqm.kernel;

import com.bt.cqm.config.ConfigurationProvider;
import com.bt.rsqe.CqmEnvironmentConfig;
import com.bt.cqm.logging.ApplicationLog;
import com.bt.rsqe.container.ApplicationConfig;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.utils.UriBuilder;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CountDownLatch;

import static java.net.HttpURLConnection.*;

public class CQMMain {
    private static final ApplicationLog LOGGER = LogFactory.createDefaultLogger(ApplicationLog.class);
    private static final String DEFAULT_ENVIRONMENT = "dev";
    private static final String OPTION_ENVIRONMENT = "e";
    private static final String OPTION_SKIP_ALREADY_RUNNING_CHECK = "skip";
    private static final String CQM = "CQM";
    private CountDownLatch applicationStop = new CountDownLatch(1);

    private boolean skipAlreadyRunningCheck;
    private boolean gracefulExit;
    private CqmEnvironmentConfig cqmConfigCqm;
    private CQMApplication cqmApp = null;

    public CQMMain(boolean skipAlreadyRunningCheck, String environment) {
        this.skipAlreadyRunningCheck = skipAlreadyRunningCheck;
        gracefulExit = false;
        cqmConfigCqm = ConfigurationProvider.provide(CqmEnvironmentConfig.class, environment);
        cqmApp = new CQMApplication(cqmConfigCqm.getCqmConfig());
    }

    public static void main(String[] args) {
        System.out.println("Inside Main");
        CommandLine cl = processCommandLineArgs(args);
        if (cl != null) {
            process(cl);
        }
    }

    static void process(CommandLine commandLine) {
        CQMMain cqm = null;
        try {
            String environment = commandLine.getOptionValue(OPTION_ENVIRONMENT, DEFAULT_ENVIRONMENT);
            LOGGER.environment(environment);

            cqm = new CQMMain(commandLine.hasOption(OPTION_SKIP_ALREADY_RUNNING_CHECK), environment);
            addShutdownHook(cqm);
            cqm.start();
            cqm.awaitApplicationStop();
            System.exit(0);
        } catch (Throwable t) {
            LOGGER.serverCouldNotStart(t);
            if (cqm != null) {
                cqm.stop();
            }
            System.exit(1);
        }
    }

    public void start() throws Exception {
        if (!skipAlreadyRunningCheck) {
            LOGGER.checkingIfServerAlreadyRunning(CQM);
            if (isAlreadyRunning(cqmConfigCqm)) {
                gracefulExit = true;
                throw new RuntimeException("RSQE is already running");
            }
        } else {
            LOGGER.skippingAlreadyRunningCheck();
        }
        startApplication();
    }

    private void startApplication() throws IOException {
        LOGGER.serverStarting(CQM);
        cqmApp.start();
        LOGGER.serverStarted(CQM, System.currentTimeMillis());
        LOGGER.serverRunning(CQM, cqmApp.getBaseUri());
    }

    static CommandLine processCommandLineArgs(String... args) {
        try {
            Options options = defineOptions();
            boolean requiresHelp = false;
            int i = 0;
            while (i < args.length) {
                if ("/?".equals(args[i])) {
                    requiresHelp = true;
                }
                i++;
            }

            if (requiresHelp) {
                printHelp(options);
                return null;
            }

            CommandLineParser parser = new PosixParser();
            return parser.parse(options, args);
        } catch (ParseException t) {
            LOGGER.commandLineCouldNotBeParsed(t);
            return null;
        }
    }

    private static Options defineOptions() {
        Options options = new Options();
        Option envOption = new Option(OPTION_ENVIRONMENT, "environment", true, "Environment");
        envOption.setRequired(false);
        options.addOption(envOption);
        Option skipOption = new Option(OPTION_SKIP_ALREADY_RUNNING_CHECK, "skipAlreadyRunningCheck", false, "Controls" +
                " whether to check if CustomerQuoteManagement is already running on startup");
        skipOption.setRequired(false);
        options.addOption(skipOption);
        return options;
    }

    private static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java com.bt.cqm.core.CustomerQuoteManagement", options);
    }

    private static void addShutdownHook(final CQMMain cqm) {
        Runtime.getRuntime().addShutdownHook(
                new Thread() {
                    @Override
                    public void run() {
                        cqm.stop();
                    }
                });
    }

    // We expect this method to be called from the shutdown hook, when break is sent.
    public void stop() {
        if (!gracefulExit) {
            try {
                cqmApp.stop();
                LOGGER.serverStopped(CQM);
            } catch (Exception t) {
                LOGGER.serverStopFailure(CQM, t);
            }
            gracefulExit = true;
            applicationStopped(); // this will cause the process method to drop through and exit.
        }
    }

    public static boolean isAlreadyRunning(CqmEnvironmentConfig cqmConfigCqm) throws Exception {
        HttpURLConnection conn = null;
        ApplicationConfig config = cqmConfigCqm.getCqmConfig().getApplicationConfig();
        String cqmAppWebURI = new UriBuilder().scheme(config.getScheme())
                .host(config.getHost()).port(config.getPort()).build().toString();
        String uri = String.format("%s/monitoring/information", cqmAppWebURI);
        try {
            URL url = new URL(uri);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setReadTimeout(5000);
            conn.connect();
            return (conn.getResponseCode() == HTTP_OK);
        } catch (IOException e) {
            LOGGER.pingFailure(uri);
            return false;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private void awaitApplicationStop() throws InterruptedException {
        applicationStop.await();
    }

    private void applicationStopped() {
        applicationStop.countDown();
    }
}
