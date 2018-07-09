package com.asidua.statsintegration.services.rest.dto;

import com.asidua.statsintegration.Constants;
import com.asidua.statsintegration.services.rest.TestManager;
import org.apache.commons.lang.StringUtils;
import org.bouncycastle.util.Strings;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.Serializable;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

@XmlRootElement(name = "heartbeat")
public class HeartbeatResponse implements Serializable {

    private static final long serialVersionUID = 6576635150651285913L;

    public enum Option {SHOW_RUNNING, SHOW_FINISHED}

    private Set<String> EMPTYSET = new HashSet<String>(0);

    private XMLGregorianCalendar currentTime;
    @XmlElement
    private int runningTests;
    @XmlElement
    private long totalTests;
    private Set<String> runningTestIds = EMPTYSET;
    private Set<String> finishedTestIds = EMPTYSET;



    public HeartbeatResponse() {
        System.out.println("###### Constructed heartbeat response");

        basicHeartbeatDetails();
    }

    private void basicHeartbeatDetails() {
        currentTime = getXMLGregorianCalendarNow();

        runningTests = TestManager.getInstance().getRunningTestCount();
        totalTests = TestManager.getInstance().getTotalTriggeredCount();
        System.out.println("###### as " + toString());
    }


    public HeartbeatResponse(String optionString) {
        System.out.println("###### Constructed heartbeat response with options " + optionString);
        basicHeartbeatDetails();

        if (StringUtils.isNotEmpty(optionString)) {
            String[] options = Strings.split(optionString, ' ');
            System.out.println("Size " + options.length);
            for (String optionStr : options) {
                Option option = null;
                try {
                    option = Option.valueOf(optionStr);
                    System.out.println("Option is " + option);
                    switch (option) {
                        case SHOW_RUNNING:
                            runningTestIds = TestManager.getInstance().getRunningTestIds();
                            System.out.println("Running");
                            break;
                        case SHOW_FINISHED:
                            finishedTestIds = TestManager.getInstance().getFinishedTestIds();
                            System.out.println("Finished");
                            break;
                    }
                } catch (IllegalArgumentException e) {
                    //Unknown OPTION given...Silently ignore it
                    System.out.println("Ignoring unknown option " + optionStr);
                }

            }
        }

    }


    @XmlElement
    public String getApiVersion(){
        return Constants.version;
    }

    public int getRunningTests() {
        return runningTests;
    }

    @XmlElement
    public String getCurrentTime() {
        return currentTime.toString();
    }

    public long getTotalTests() {
        return totalTests;
    }

    @XmlElement(nillable = true)
    public String[] getRunningTestIds() {
        return runningTestIds.toArray(new String[runningTestIds.size()]);
    }

    @XmlElement(nillable = true)
    public String[] getFinishedTestIds() {
        return finishedTestIds.toArray(new String[finishedTestIds.size()]);
    }

    public String toString() {

        return "Heartbeat - It's " + getCurrentTime() + ". Current Tasks = " + getRunningTests() + " Total= " + getTotalTests();


    }

    private XMLGregorianCalendar getXMLGregorianCalendarNow() {
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        XMLGregorianCalendar now = null;
        DatatypeFactory datatypeFactory = null;
        try {
            datatypeFactory = DatatypeFactory.newInstance();
            now = datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return now;

    }
}
