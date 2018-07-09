package com.asidua.statsintegration.services.rest;

import com.asidua.statsintegration.services.rest.dto.TestResponse;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import org.apache.commons.lang.StringUtils;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProgressEvent;
import org.gradle.tooling.ProgressListener;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.ResultHandler;

import java.io.File;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class TestManager implements ResultHandler<TestTask> {

    private static final String[] UNKNOWN_TEST = new String[]{TestResponse.ResponseStatus.UNKNOWN.toString(), "Unknown Test"};
    private static final String COMPLETE = "COMPLETE";
    private static final String FAILED = "FAILED";
    private static final String TEST_TASK = ":stats-integration:test";
    private static final String TEST_SINGLE_ARGUMENT = "stats-integration.single=%s";
    private static final String TEST_INPUT_ARGUMENT = "test.input=%s";
    private static final String GRADLE_HOME = "GRADLE_HOME";
    private static final String DEBUG_FLAG = "-d";
    private static final String INFO_FLAG = "-i";

    private GradleConnector connector = null;
    private long totalTestsTriggered = 0;
    private ExecutorService pool;
    private Map<String, TaskHolder> listenerMap = null;
    private Map<String, ProgressListener> completionMap = null;

    private Logger logger = LogFactory.createDefaultLogger(Logger.class);

    public ProjectConnection getConnection() {
        return connection;
    }

    ProjectConnection connection;

    static TestManager currentInstance = null;

    private TestManager(String projectHome) {


        String gradleHome = System.getenv(GRADLE_HOME);


        //Double check prerequisites exist
        if (StringUtils.isBlank(gradleHome)) {
            throw new RuntimeException("Mandatory GRADLE_HOME environment variable not configured");
        }

        if (StringUtils.isBlank(projectHome)) {
            throw new RuntimeException("Mandatory Project Home not defined in invoking process");
        }

        dumpEnvironment();

        logger.debugMessage(" Constructed Test Manager");
        logger.debugMessage(" Project Home" + projectHome);
        logger.debugMessage(" Gradle Home" + gradleHome);


        connector = GradleConnector.newConnector();

        connector.forProjectDirectory(new File(projectHome));
        connector.useInstallation(new File(gradleHome));
        listenerMap = new HashMap<String, TaskHolder>();
        completionMap = new HashMap<String, ProgressListener>();
        connection = connector.connect();

        //Spin up the executor pool

        pool = Executors.newCachedThreadPool();


    }

    private void dumpEnvironment() {
        logger.debugMessage(" Connector " + connector);
        Properties sys = System.getProperties();
        logger.debugMessage(" Environment");
        for (Object o : sys.keySet()) {
            logger.debugMessage("     Key " + o.toString() + " = " + sys.get(o));
        }
        Map props = System.getenv();
        logger.debugMessage(" Environment");
        for (Object o : props.keySet()) {
            logger.debugMessage("     Key " + o.toString() + " = " + props.get(o));
        }
    }


    public static TestManager appointForProjectHome(String home) {
        currentInstance = new TestManager(home);
        return currentInstance;
    }


    public static TestManager getInstance() {

        if (null == currentInstance) {
            throw new RuntimeException("Test Manager not appointed for project home");
        }
        return currentInstance;
    }


    public static void recycle() {
        //Completion status
        currentInstance.completionMap.clear();
        currentInstance.completionMap = null;
        //running listeners
        currentInstance.listenerMap.clear();
        currentInstance.listenerMap = null;

        currentInstance.pool.shutdownNow();
        currentInstance.pool = null;
        currentInstance.connection.close();
    }


    public ProgressListener completeTest(String testid, String testname) {
        logger.debugMessage("Completing " + testid);

        TestProgressListener finished =  listenerMap.remove(testid).getListener();
        finished.setFinalStatus(TestResponse.ResponseStatus.PASSED);
        return completionMap.put(testid, finished);
    }


    public ProgressListener failTest(String testid, String testname) {
        logger.debugMessage("Failing " + testid);

        TestProgressListener finished =  listenerMap.remove(testid).getListener();
        finished.setFinalStatus(TestResponse.ResponseStatus.FAILED);
        return completionMap.put(testid, finished);
    }


    public TestProgressListener getProgressForRunningTest(String testId) {
        if (listenerMap.containsKey(testId)) {
            TestProgressListener progress = getListenerForTestid(testId);
            return progress;
        } else {
            return TestProgressListener.UNKNOWNTEST;
        }
    }

    public TestResponse.ResponseStatus getStatusForCompletedTest(String testId) {

        if (completionMap.containsKey(testId)) {
            return ((TestProgressListener) completionMap.get(testId)).getFinalStatus();
        } else {
            return TestResponse.ResponseStatus.UNKNOWN;
        }
    }

    public List<String> getMessageForCompletedTest(String testId) {
        if (completionMap.containsKey(testId)) {
            return ((TestProgressListener) completionMap.get(testId)).getEvents();
        } else {
            return Arrays.asList(TestResponse.ResponseStatus.UNKNOWN.toString());
        }
    }


    public boolean isNonUniqueKey(String key) {
        return completionMap.containsKey(key) || listenerMap.containsKey(key);
    }

    public int getRunningTestCount() {
        return listenerMap.size();
    }

    public Future launchTest(String testName, String testId, String paramFile) {
        logger.debugMessage("Launch Test " + testName + "(" + testId + ")");

        TestTask task = new TestTask(testId, testName, paramFile);

        listenerMap.put(testId, new TaskHolder(new TestProgressListener(testId, testName)));
        return associateFuture(testId, pool.submit(task));
    }

    private Future associateFuture(String testId, Future todo) {

        TaskHolder holder = listenerMap.get(testId);
        if (null == holder) {
            throw new RuntimeException("Tried to associate task to non existent listener for " + testId);
        }

        return todo;
    }


    public BuildLauncher getLauncherFor(final String testid, final String testname, final String paramFile,OutputStream out,OutputStream err) {
        logger.debugMessage("Getting Launcher for " + testname + "(" + testid + ")");
        BuildLauncher launcher = getConnection().newBuild();
        ProgressListener theListener = getListenerForTestid(testid);
        launcher.addProgressListener(theListener);
        theListener.statusChanged(new ProgressEvent() {
            @Override
            public String getDescription() {
                return "Created Task "+TEST_TASK +" with args " + String.format(TEST_SINGLE_ARGUMENT, testname)+ "," +String.format(TEST_INPUT_ARGUMENT, paramFile);
            }
        });
        launcher.forTasks(TEST_TASK);
        launcher.withArguments();
        launcher.setJvmArguments("-D"+String.format(TEST_SINGLE_ARGUMENT, testname), "-D"+String.format(TEST_INPUT_ARGUMENT, paramFile));

        totalTestsTriggered++;

        launcher.setStandardError(err);
        launcher.setStandardOutput(out);
        return launcher;
    }
    public TestProgressListener getListenerForTestid(String testid) {
        return listenerMap.get(testid).getListener();
    }
    public long getTotalTriggeredCount() {
        return totalTestsTriggered;
    }

    public Set<String> getRunningTestIds() {
        return listenerMap.keySet();
    }

    public Set<String> getFinishedTestIds() {
        return completionMap.keySet();
    }

    public String purgeTest(String testId) {
        logger.debugMessage("Purge " + testId);
        //is this a running or completed test

        if (completionMap.containsKey(testId)) {//completed
            logger.debugMessage("Purge completed test");
            completionMap.remove(testId);
            return "Purged";
        }


        //might be running
        if (listenerMap.containsKey(testId)) {//its running
            logger.debugMessage("Purge running test");
            Future theFuture = listenerMap.get(testId).getFuture();
            theFuture.cancel(true);
            return "Stopped - purge again to remove";
        }

        return "Test ID " + testId + " unknown";
    }

    @Override
    public void onComplete(TestTask testTask) {
        completeTest(testTask.testId,testTask.testName);
    }

    @Override
    public void onFailure(GradleConnectionException e) {
        System.out.println("Test Failed because "+e);
    }

    public Map<String, ProgressListener> getCompletionMap() {
        return completionMap;
    }

    public void setCompletionMap(Map<String, ProgressListener> completionMap) {
        this.completionMap = completionMap;
    }


interface Logger {
    @Log(level = LogLevel.DEBUG, format = "###### Test Manager Debug - %s")
    void debugMessage(String message);
}

}
