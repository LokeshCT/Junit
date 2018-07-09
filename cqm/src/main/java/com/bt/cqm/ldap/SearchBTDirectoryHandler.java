package com.bt.cqm.ldap;

import com.bt.cqm.ldap.model.LdapSearchListModel;
import com.bt.cqm.ldap.model.LdapSearchModel;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.bt.rsqe.rest.ResponseBuilder;
import com.bt.rsqe.utils.AssertObject;

import javax.ws.rs.*;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bt.cqm.ldap.LDAPConstants.*;

@Path("/cqm/searchBTDirectory")
@Produces(MediaType.APPLICATION_JSON)
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class SearchBTDirectoryHandler {

    private static final SearchBTDirectoryLogger LOGGER = LogFactory.createDefaultLogger(SearchBTDirectoryLogger.class);
    private LdapRepository repository = new LdapRepository();

    @GET
    @Path("/ein/{ein}")
    public Response search(@PathParam("ein") String ein) {

        Map<String, String> args = new HashMap<String, String>();
        args.put("ein", ein);

        long startTime = System.nanoTime();

        List<LdapSearchModel> resultList = searchBTDirectory(args);
        /*if (resultList == null || resultList.isEmpty()) {
            model = new LdapSearchListModel(resultList, "No entry is matching the search criteria", true);
        } else {
            model = new LdapSearchListModel(resultList, "Found: " + resultList.size(), false);
        }*/

        long endTime = System.nanoTime();

        LOGGER.receivedResponse((endTime - startTime) + " nanoseconds.");

        GenericEntity<List<LdapSearchModel>> genericModel = new GenericEntity<List<LdapSearchModel>>(resultList) {
        };

        return ResponseBuilder.anOKResponse().withEntity(genericModel).build();
    }

    public List<LdapSearchModel> searchBTDirectory(Map<String, String> args) {
        List<LdapSearchModel> resultList = new ArrayList<LdapSearchModel>();
        try {

            resultList = repository.search(args);
        } catch (Exception e) {
            LOGGER.logError(e);
        }
        return resultList;
    }


    @GET
   // @Path("/ein/{ein}/firstName/{firstName}/lastName/{lastName}/email/{email}/search")
    //@Consumes(MediaType.APPLICATION_JSON)
    //@Produces(MediaType.TEXT_HTML)
    public Response searchDirectory(@QueryParam(EIN) String ein, @QueryParam(FIRST_NAME) String firstName, @QueryParam(LAST_NAME) String lastName, @QueryParam(EMAIL) String email) {

        List<LdapSearchModel> searchList = new ArrayList<LdapSearchModel>();
        Date startDate = new Date();
        boolean isError = false;
        try {
            Map<String, String> args = processForm(ein, firstName, lastName, email);
            LdapRepository repository = new LdapRepository();
            searchList = repository.search(args);
            if (searchList == null || searchList.isEmpty()) {
                isError = true;
            }
        } catch (LdapSearchException e) {
            LOGGER.logError(e);
            isError = true;
        }
        Date endDate = new Date();
        LdapSearchListModel model = null;
        if (isError) {
            model = new LdapSearchListModel(searchList, "No entry is matching the search criteria", true);
        } else {
            model = new LdapSearchListModel(searchList, "", false);
        }
        /*String page = presenter.render(view("searchBtDirectory.html")
                .withContext("view", model));*/
        GenericEntity<LdapSearchListModel> genericModel = new GenericEntity<LdapSearchListModel>(model) {
        };
        LOGGER.receivedResponse(((endDate.getTime() - startDate.getTime()) / 1000) + "Seconds");
        return ResponseBuilder.anOKResponse().withEntity(genericModel).build();
        //return responseOk(page);
    }

    private Map<String, String> processForm(String ein, String firstName, String lastName, String email) {
        Map<String, String> args = new HashMap<String, String>();
        if (!AssertObject.isEmpty(firstName)) {
            args.put(LDAPConstants.FIRST_NAME, firstName);
        }
        if (!AssertObject.isEmpty(lastName)) {
            args.put(LDAPConstants.LAST_NAME, lastName);
        }
        if (!AssertObject.isEmpty(email)) {
            args.put(LDAPConstants.EMAIL, email);
        }
        if (!AssertObject.isEmpty(ein)) {
            args.put(LDAPConstants.EIN, ein);
        }
        return args;
    }

    private interface SearchBTDirectoryLogger {
        @Log(level = LogLevel.INFO, format = "Received LDAP search response in %s seconds.")
        void receivedResponse(String timeTaken);

        @Log(level = LogLevel.ERROR, format = "Error occured while fetching user. Details:[%s]")
        void logError(Exception ex);
    }

}
