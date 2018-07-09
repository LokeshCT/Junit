package com.bt.rsqe.expedio.services;

import com.bt.rsqe.ContainerUtils;
import com.bt.rsqe.container.Application;
import com.bt.rsqe.container.ApplicationConfig;
import com.bt.rsqe.container.StubApplicationConfig;
import com.bt.rsqe.utils.UriBuilder;
import org.hamcrest.core.Is;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;

import java.io.IOException;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertThat;

public class AssignedToContactResourceTest {

    private static ApplicationConfig applicationConfig = StubApplicationConfig.defaultTestConfig();
    private static Application application;
    private static AssignedToContactResource assignedToContactResource;

    @BeforeClass
    public static void beforeClass() throws IOException {
        application = ContainerUtils.startContainer(applicationConfig, new Handler());
        assignedToContactResource = new AssignedToContactResource(UriBuilder.buildUri(applicationConfig), "");
    }

    @Test
    public void shouldGetAssignedToContactsFromServer() {
        List<ActivityAssignedToContactDTO> assignedToContacts = assignedToContactResource.getAssignedToContacts("BT INDIA", "ANY", "1234");

        assertThat(assignedToContacts.size(), Is.is(1));
        assertThat(assignedToContacts.get(0).getRequestID(), Is.is("1234"));
        assertThat(assignedToContacts.get(0).getFullName(), Is.is("Contact Full Name"));
    }


    @AfterClass
    public static void afterClass() throws IOException {
        ContainerUtils.stop(application);
    }


    @Path("/rsqe/expedio-services/get-assigned-to-contacts")
    public static class Handler {

        @GET
        public Response getActivityAssignedToContacts(@QueryParam(AssignedToContactResource.SALES_CHANNEL_QUERY_PARAM) String salesChannel,
                                                      @QueryParam(AssignedToContactResource.USER_ROLE_QUERY_PARAM) String userRole,
                                                      @QueryParam(AssignedToContactResource.CUSTOMER_ID_QUERY_PARAM) String customerId) {
            assertThat(salesChannel, Is.is("BT INDIA"));
            assertThat(userRole, Is.is("ANY"));
            assertThat(customerId, Is.is("1234"));

            GenericEntity<List<ActivityAssignedToContactDTO>> entity =
                new GenericEntity<List<ActivityAssignedToContactDTO>>(newArrayList(new ActivityAssignedToContactDTO("1234", "Contact Full Name"))) {
                };

            return Response.ok(entity).build();
        }

    }
}
