package com.asidua.statsintegration.services.rest;

import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.ProgressEvent;

import java.io.ByteArrayOutputStream;
import java.util.Map;

public class TestTask implements Runnable {
    private static final boolean PASSED = true;
    private static final boolean FAILED = false;
    BuildLauncher taskLauncher;
    ByteArrayOutputStream out;
    ByteArrayOutputStream err;
    TestManager myManager = TestManager.getInstance();
    String testId;
    String testName;
    String paramFile;
    private Logger logger = LogFactory.createDefaultLogger(Logger.class);


    public TestTask(String testId, String testName, String paramFile) {
        logger.debugMessage("Constructing TestTask for "+testName+"/"+testId+" params from "+paramFile);
        this.testId = testId;
        this.testName = testName;
        this.paramFile = paramFile;
    }

    @Override
    public void run() {
        logger.debugMessage("Starting Test " + testName + "(" + testId + ")");

        err = new ByteArrayOutputStream();
        out = new ByteArrayOutputStream();

        try {
            taskLauncher = myManager.getLauncherFor(testId, testName, paramFile,out,err);

            taskLauncher.run();
            recordCompletion(PASSED);
        } catch (Exception e) {
            addProgressEvent("Exception Caught: " + e.getMessage());
            recordCompletion(FAILED);
        }
        logger.debugMessage("Finished Test" + testName + "(" + testId + ")");

    }

    private void addProgressEvent(final String messageText) {
        myManager.getListenerForTestid(testId).statusChanged(new ProgressEvent() {
            @Override
            public String getDescription() {
                return "" + messageText;
            }
        });
    }


    private void recordCompletion(boolean passed) {
        final String messageText = passed ? "Completed" : "Failed";
        addProgressEvent("Final Status= " + messageText + ", OUT=" + out.toString() + ", ERR=" + err.toString());
        if (passed) {
            myManager.completeTest(testId, testName);
        } else {
            myManager.failTest(testId, testName);
        }
        logger.debugMessage("!completion");
        Map comple = myManager.getCompletionMap();
        for (Object o : comple.keySet()) {
            logger.debugMessage("Completion map " + o.toString() + " is " + comple.get(o));
        }
    }


    interface Logger {
    @Log(level = LogLevel.DEBUG, format = "###### Test Task Debug - %s")
    void debugMessage(String message);
    }

}

