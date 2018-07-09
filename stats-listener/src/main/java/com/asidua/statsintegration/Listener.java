package com.asidua.statsintegration;

import com.asidua.statsintegration.services.ListenerServer;
import com.google.common.base.Strings;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.File;


public class Listener {

    private static final int DEFAULT_PORT = 9889;
    private static final Options theOptions = buildOptions();

    public static void main(String[] args) {
        String home = null;
        int serverPort = DEFAULT_PORT;


        CommandLineParser parser = new GnuParser();

        try {
            CommandLine cmd = parser.parse(theOptions, args, true);

            if (cmd.hasOption("help")){
                generateUsage();
                System.exit(0);

            }


            if (cmd.hasOption("r")) {
                home = cmd.getOptionValue("r");
                //does this look like rsqe
                if (!cmd.hasOption("nocheck")&&!isRsqeEnvironmentRootedAt(home)) {
                    die("Start Failed; Supplied HOME Path '" + home + "' does not look like rSQE");
                }
            } else {
                die("Start Failed; rSQE project home location must be specified");
            }


            if (cmd.hasOption("p")) {//server port will be second argument
                serverPort = Integer.parseInt(cmd.getOptionValue("p"));
            }

        } catch (ParseException e) {
            parseFailed(e.getMessage());
        }


        ListenerServer server = new ListenerServer(home, serverPort);

        try {
            server.start();
        } catch (Exception e) {
            die("Start Server failed", e);
        }

    }


    private static void commandFailed() {
        parseFailed(null);
    }

    private static void parseFailed(String message) {
        if (!Strings.isNullOrEmpty(message)) {
            System.err.println("Command Parsing Failed: " + message);
        }

        generateUsage();
        System.exit(1);

    }

    private static void generateUsage() {
        HelpFormatter formatter = new HelpFormatter();
        String footer = String.format("Tool version: %s",Constants.version) ;
        formatter.printHelp("stats-listener", "",theOptions,footer, true);
    }


    private static void die(String message) {
        System.err.println(message);
        System.exit(1);
    }

    private static void die(String operation, Exception e) {
        System.err.println(operation + " with exception " + e.getMessage());
        e.printStackTrace(System.err);
        System.exit(1);
    }


    private static boolean isRsqeEnvironmentRootedAt(String projectHome) {
        boolean matched = true;
        String[] filesToCheck = {"build.gradle", "stats-listener/build/install/stats-listener/README.md"};
        for (String file : filesToCheck) {
            File test = new File(projectHome + File.separator + file);
            matched = matched && (test.exists() && test.isFile());
        }
        return matched;
    }


    private static Options buildOptions() {
        Options opts = new Options();

        Option help = new Option("help", "print this message");
        Option noStructureCheck = new Option("nocheck", "do not perform project home structure check");

        Option port = OptionBuilder.withDescription("port number for listener - Default is 9889")
                                   .hasArg()
                                   .withArgName("portnumber")
                                   .create("p");


        Option projectHome = OptionBuilder.withDescription("rSQE project home directory to locate supported tests and build files")
                                          .hasArg()
                                          .withArgName("rSqe Project Home")
                                          .isRequired()
                                          .withLongOpt("rsqehome")
                                          .create("r");


        opts.addOption(projectHome);
        opts.addOption(port);
        opts.addOption(noStructureCheck);
        opts.addOption(help);


        return opts;
    }

}
