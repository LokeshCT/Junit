package com.asidua.statsintegration.services.rest;

import com.asidua.statsintegration.services.rest.dto.TestResponse;
import org.gradle.tooling.ProgressEvent;
import org.gradle.tooling.ProgressListener;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

public class TestProgressListener implements ProgressListener {

    private static final ProgressEvent CREATED = new ProgressEvent() {
        @Override
        public String getDescription() {
            return "Created at " + new DateTime().toString();
        }
    };
    private static final ProgressEvent UNKNOWN = new ProgressEvent() {
        @Override
        public String getDescription() {
            return "Error: Unknown Test";
        }
    };

    public static final TestProgressListener UNKNOWNTEST = new TestProgressListener("UNKNOWN TEST", "0", UNKNOWN);

    private String testName;
    private String testId;
    private TestResponse.ResponseStatus finalStatus;

    private List<ProgressEvent> events = new ArrayList<ProgressEvent>();

    public TestProgressListener(String testId, String testName) {
        this(testId, testName, CREATED);
    }

    private TestProgressListener(String testId, String testName, ProgressEvent seed) {
        finalStatus = null;
        this.testName = testName;
        this.testId = testId;
        events.add(seed);
    }


    @Override
    public void statusChanged(ProgressEvent progressEvent) {
        events.add(progressEvent);
    }


    public String getLatestMessage() {
        return events.get(events.size() - 1).getDescription();
    }

    public String getFullReportAsString() {
        StringBuffer finalMessage = new StringBuffer();
        int count = 1;
        for (ProgressEvent event : events) {
            finalMessage.append(count + ": ");
            finalMessage.append(event.getDescription());
            finalMessage.append("\n");
            count++;
        }
        return finalMessage.toString();
    }

    public String getFullReportAsXml() {
        StringBuffer finalMessage = new StringBuffer();
        int count = 1;
        for (ProgressEvent event : events) {
            finalMessage.append("<Event seq=" + count + ">");
            finalMessage.append(event.getDescription());
            finalMessage.append("</Event>");
            count++;
        }
        return finalMessage.toString();
    }


    public String getTestName() {
        return testName;
    }

    public String getTestId() {
        return testId;
    }


    public String toString() {
        return "Progress Listener for testId " + getTestId() +
               " (" + getTestName() + ")" +
               " with " + events.size() + " events" +
               (null == finalStatus ? " In progress" : " Final State " + finalStatus.toString());
    }


    public TestResponse.ResponseStatus getFinalStatus() {
        return null == finalStatus ? TestResponse.ResponseStatus.INPROGRESS : finalStatus;
    }

    public void setFinalStatus(TestResponse.ResponseStatus finalStatus) {
        this.finalStatus = finalStatus;
    }


    public List<String> getEvents() {
        List<String> newEvents = new ArrayList<String>(events.size());
        for (ProgressEvent event : events) {
            newEvents.add(event.getDescription());
        }
        return newEvents;
    }

}
