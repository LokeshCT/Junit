package com.bt.nrm.kernel;


import com.bt.nrm.config.ConfigurationProvider;
import com.bt.nrm.logging.ApplicationLog;
import com.bt.rsqe.NrmEnvironmentConfig;
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

public class NRMMain {
    private static final ApplicationLog LOGGER = LogFactory.createDefaultLogger(ApplicationLog.class);
    private static final String DEFAULT_ENVIRONMENT = "dev";
    private static final String OPTION_ENVIRONMENT = "e";
    private static final String OPTION_SKIP_ALREADY_RUNNING_CHECK = "skip";
    private CountDownLatch applicationStop = new CountDownLatch(1);

    private boolean skipAlreadyRunningCheck;
    private boolean gracefulExit;
    private NrmEnvironmentConfig nrmConfigNrm;
    private NRMApplication nrmApp = null;

    public NRMMain(boolean skipAlreadyRunningCheck, String environment) {
        this.skipAlreadyRunningCheck = skipAlreadyRunningCheck;
        gracefulExit = false;
        nrmConfigNrm = ConfigurationProvider.provide(NrmEnvironmentConfig.class, environment);
        nrmApp = new NRMApplication(nrmConfigNrm.getNrmConfig());   //for NRM, stats collection is ON.
    }

    public static void main(String[] args) {
        System.out.println("Inside Main");
        CommandLine cl = processCommandLineArgs(args);
        if (cl != null) {
            process(cl);
        }
    }

    static void process(CommandLine commandLine) {
        NRMMain nrm = null;
        try {
            String environment = commandLine.getOptionValue(OPTION_ENVIRONMENT, DEFAULT_ENVIRONMENT);
            LOGGER.environment(environment);

            nrm = new NRMMain(commandLine.hasOption(OPTION_SKIP_ALREADY_RUNNING_CHECK), environment);
            addShutdownHook(nrm);
            nrm.start();
            nrm.awaitApplicationStop();
            System.exit(0);
        } catch (Throwable t) {
            LOGGER.serverCouldNotStart(t);
            if (nrm != null) {
                nrm.stop();
            }
            System.exit(1);
        }
    }

    public void start() throws Exception {
        if (!skipAlreadyRunningCheck) {
            LOGGER.checkingIfServerAlreadyRunning("NRM");
            if (isAlreadyRunning(nrmConfigNrm)) {
                gracefulExit = true;
                throw new RuntimeException("RSQE is already running");
            }
        } else {
            LOGGER.skippingAlreadyRunningCheck();
        }
        startApplication();
    }

    private void startApplication() throws IOException {
        LOGGER.serverStarting("NRM");
        nrmApp.start();
        LOGGER.serverStarted("NRM", System.currentTimeMillis());
        LOGGER.serverRunning("NRM", nrmApp.getBaseUri());
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
                " whether to check if Non-standard request management is already running on startup");
        skipOption.setRequired(false);
        options.addOption(skipOption);
        return options;
    }

    private static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java com.bt.cqm.core.NonStandardReqestManagement", options);
    }

    private static void addShutdownHook(final NRMMain nrm) {
        Runtime.getRuntime().addShutdownHook(
                new Thread() {
                    @Override
                    public void run() {
                        nrm.stop();
                    }
                });
    }

    // We expect this method to be called from the shutdown hook, when break is sent.
    public void stop() {
        if (!gracefulExit) {
            try {
                nrmApp.stop();
                LOGGER.serverStopped("NRM");
            } catch (Exception t) {
                LOGGER.serverStopFailure("NRM", t);
            }
            gracefulExit = true;
            applicationStopped(); // this will cause the process method to drop through and exit.
        }
    }

    public static boolean isAlreadyRunning(com.bt.rsqe.NrmEnvironmentConfig nrmConfigNrm) throws Exception {
        HttpURLConnection conn = null;
        ApplicationConfig config = nrmConfigNrm.getNrmConfig().getApplicationConfig();
        String nrmAppWebURI = new UriBuilder().scheme(config.getScheme())
                .host(config.getHost()).port(config.getPort()).build().toString();
        String uri = String.format("%s/monitoring/information", nrmAppWebURI);
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
