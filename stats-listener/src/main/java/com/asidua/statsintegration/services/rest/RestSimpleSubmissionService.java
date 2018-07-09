package com.asidua.statsintegration.services.rest;

import com.asidua.statsintegration.services.Errors;
import com.asidua.statsintegration.services.ParameterMap;
import com.asidua.statsintegration.services.TestInvocationException;
import com.asidua.statsintegration.services.rest.dto.HeartbeatResponse;
import com.asidua.statsintegration.services.rest.dto.PurgeResponse;
import com.asidua.statsintegration.services.rest.dto.TestResponse;
import com.asidua.statsintegration.utilities.Templater;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import org.apache.commons.beanutils.BeanUtils;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Path("/stats")
public class RestSimpleSubmissionService implements SimpleSubmissionService {

    private Logger logger = LogFactory.createDefaultLogger(Logger.class);


    public RestSimpleSubmissionService() {
        super();
        logger.message("Initialised");
    }


    @Path("heartbeat")
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response heartbeat(@QueryParam("options") String options) {
        logger.message("Heartbeat");
        return okResponse(new HeartbeatResponse(options));
    }



    @Path("purge/{id}")
    @DELETE
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response purgeTest(@PathParam("id") String testId) {
        System.out.println("###### Purge Request for ID " + testId);
        return okResponse(new PurgeResponse(TestManager.getInstance().purgeTest(testId)));
        //return Response.ok(new PurgeResponse(TestManager.getInstance().purgeTest(testId))).build();
    }


    @Path("trigger/{name}/{id}")
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response triggerTestNamed(@PathParam("name") String testname,
                                         @PathParam("id") String testid,
                                         @DefaultValue("false") @QueryParam("wait") boolean synchronous, MultivaluedMap<String, String> body) {

        ParameterMap map = new ParameterMap();
        logger.message("trigger " + testname + "/" + testid);

        try {
            BeanUtils.copyProperties(map, body);
        } catch (IllegalAccessException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, new TestResponse(testname, testid, e));

        } catch (InvocationTargetException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, new TestResponse(testname, testid, e));
        }


        map.setTestId(testid);
        map.setTestName(testname);

        Errors err = map.validate();
        if (err.hasErrors()) {
            TestResponse fail = new TestResponse(testid, testname, err.getMessagesAsString());
            fail.setResponseSummary(TestResponse.ResponseStatus.FAILED);

            return errorResponse(Response.Status.BAD_REQUEST, fail);
            //return fail;
        }

        Templater theTemplate = new Templater();
        String paramFile = "";

        try {
            paramFile = theTemplate.toTempFile("CAServiceBasic.ftl", map);
        } catch (TestInvocationException e) {
            System.out.println("Problem processing template " + e);
            System.out.println(e.getCause());
            e.getCause().printStackTrace(System.out);
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, new TestResponse(testname, testid, e));
        }
        System.out.println("###### Triggered " + (synchronous ? "SYNCHRONOUSLY " : "ASYNCHRONOUSLY ") + testname + " ID " + testid);

        return triggerTest(testname, testid, synchronous, paramFile);
    }

    private Response triggerTest(String testname, String testid, boolean synchronous, String paramFile) {

        TestResponse response = new TestResponse();

        response.setTestName(testname);
        response.setTestId(testid);

        //Simple test for unique id
        if (TestManager.getInstance().isNonUniqueKey(testid)) {
            System.out.println("###### Duplicate Text id "+testid);
            response.addResponseMessage("Duplicate Test Ids are not allowed. TestId: " + testid);
            response.setResponseSummary(TestResponse.ResponseStatus.FAILED);
            return errorResponse(Response.Status.BAD_REQUEST, response);

        } else {
            Future task = TestManager.getInstance().launchTest(testname, testid, paramFile);

            if (synchronous) {
                //Simulate a synchronous response by watching the progress
                try {
                    System.out.println("###### Waiting for Completion");
                    task.get();//block until completion
                } catch (InterruptedException e) {
                    response.addResponseMessage("Task Interrupted");
                    response.setResponseSummary(TestResponse.ResponseStatus.FAILED);
                    return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, response);
                } catch (ExecutionException e) {
                    response.setResponseSummary(TestResponse.ResponseStatus.FAILED);
                    response.addResponseMessage("Execution Failed");
                    return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, response);
                }
                TestProgressListener listener = TestManager.getInstance().getProgressForRunningTest(testid);
                response.setResponseSummary(TestResponse.ResponseStatus.PASSED);
                response.setResponseMessages(listener.getEvents());
            } else {
                response.setSingleResponseMessage("Test Launched - Check for progress status/" + testid);
                response.setResponseSummary(TestResponse.ResponseStatus.INPROGRESS);
            }
        }
        return okResponse(response);
    }


    @Path("status/{id}")
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response statusOfTest(@PathParam("id") String testid) {
        logger.message("Status/" + testid);

        TestResponse response = new TestResponse();
        response.setTestId(testid);
        TestProgressListener listener = TestManager.getInstance().getProgressForRunningTest(testid);

        if (listener == TestProgressListener.UNKNOWNTEST) {//not currently running - is it finished?
            System.out.println("No Running Test - Is it a finished one ?");
            TestResponse.ResponseStatus status = TestManager.getInstance().getStatusForCompletedTest(testid);
            if (status == TestResponse.ResponseStatus.UNKNOWN) {//No message
                response.setResponseSummary(status);
                response.setTestName("UNKNOWN");
                response.addResponseMessage("Status request failed: ");
                response.addResponseMessage("Supplied testId was unknown");
                return errorResponse(Response.Status.NOT_FOUND, response);
            } else {
                System.out.println("Known test finished with status " + status);
                response.setResponseSummary(status);
                response.setResponseMessages(TestManager.getInstance().getMessageForCompletedTest(testid));
            }
        } else {
            System.out.println("Known test in progress");
            response.setTestName(listener.getTestName());
            response.addResponseMessage(listener.getLatestMessage());
            response.addResponseMessage("Test Still Running - Check later");
            response.setResponseSummary(TestResponse.ResponseStatus.INPROGRESS);
            return okResponse(response);
        }
        return okResponse(response);
       // return response;
    }


    private Response okResponse(Serializable responseObject){
          return Response.ok(responseObject).build();
      }


    private Response errorResponse(Response.Status statusCode, Serializable responseEntity){
        return Response.status(statusCode).entity(responseEntity).build();
    }

    interface Logger {
        @Log(level = LogLevel.DEBUG, format = "Service Message - %s")
        void message(String message);
    }

}