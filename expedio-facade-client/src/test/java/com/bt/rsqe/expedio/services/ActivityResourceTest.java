package com.bt.rsqe.expedio.services;

import com.bt.rsqe.ContainerUtils;
import com.bt.rsqe.container.Application;
import com.bt.rsqe.container.ApplicationConfig;
import com.bt.rsqe.container.StubApplicationConfig;
import com.bt.rsqe.expedio.activity.AcceptActivityTaskDTO;
import com.bt.rsqe.expedio.activity.ReassignActivityTaskDTO;
import com.bt.rsqe.expedio.activity.RejectActivityTaskDTO;
import com.bt.rsqe.expedio.activity.WithdrawApprovalActivityTaskDTO;
import com.bt.rsqe.utils.AssertObject;
import com.bt.rsqe.utils.UriBuilder;
import org.hamcrest.core.Is;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static com.google.common.collect.Lists.*;
import static org.junit.Assert.*;

public class ActivityResourceTest {

    private static ApplicationConfig applicationConfig = StubApplicationConfig.defaultTestConfig();
    private static ActivityResource activityResource;
    private static Application application;

    @BeforeClass
    public static void beforeClass() throws IOException {
        application = ContainerUtils.startContainer(applicationConfig, new Handler());
        activityResource = new ActivityResource(UriBuilder.buildUri(applicationConfig), null);
    }

    @Test
    public void shouldGetActivitiesFromServer() {
        List<ActivityDTO> activityList = activityResource.getActivityList(new GetActivityRequestDTO("BT INDIA", null, null, null, null, null, null, null));
        assertThat(activityList.size(), Is.is(1));
        assertThat(activityList.get(0).getActivityID(), Is.is("1"));
    }

    @Test
    public void shouldInvokeServerToCreateActivity() {
        ActivityDTO activityDTO = ActivityDTO.Builder.get().withActivityDescription("do something").build();
        String activityId = activityResource.create(activityDTO);
        assertThat(activityId, Is.is("1"));
    }

    @Test
    public void shouldInvokeServerToChangeOwnership() {
        ChangeOwnershipDTO changeOwnershipDTO = new ChangeOwnershipDTO();
        changeOwnershipDTO.setActivityID("2");
        UpdateActivityResponseDTO updateActivityResponseDTO = activityResource.changeOwnership(changeOwnershipDTO);
        assertThat(updateActivityResponseDTO.getActivityID(), Is.is("2"));
    }

    @Test
    public void shouldInvokeServerToChangeStatus() {
        UpdateStatusDTO updateStatusDTO = new UpdateStatusDTO();
        updateStatusDTO.setActivityID("2");
        UpdateActivityResponseDTO updateActivityResponseDTO = activityResource.updateStatus(updateStatusDTO);
        assertThat(updateActivityResponseDTO.getActivityID(), Is.is("2"));
    }

    @Test
    public void shouldReassignActivityHandleException() {
        ReassignActivityTaskDTO reassignActivityTaskDTO = new ReassignActivityTaskDTO();

        boolean isSuccess = activityResource.reassignActivityDelegation(reassignActivityTaskDTO);
        assert (!isSuccess);
    }

    @Test
    public void shouldReassignActivity() {
        ReassignActivityTaskDTO reassignActivityTaskDTO = new ReassignActivityTaskDTO();
        reassignActivityTaskDTO.setActivityID("10001");
        boolean isSuccess = activityResource.reassignActivityDelegation(reassignActivityTaskDTO);
        assert (isSuccess);
    }


    @Test
    public void shouldAcceptActivityHandleException() {
        AcceptActivityTaskDTO acceptActivityTaskDTO = new AcceptActivityTaskDTO();

        boolean isSuccess = activityResource.acceptActivityDelegation(acceptActivityTaskDTO);
        assert (!isSuccess);
    }

    @Test
    public void shouldAcceptActivity() {
        AcceptActivityTaskDTO acceptActivityTaskDTO = new AcceptActivityTaskDTO();
        acceptActivityTaskDTO.setActivityID("10001");
        boolean isSuccess = activityResource.acceptActivityDelegation(acceptActivityTaskDTO);
        assert (isSuccess);
    }


    @Test
    public void shouldRejectActivityHandleException() {
        RejectActivityTaskDTO rejectActivityTaskDTO = new RejectActivityTaskDTO();

        boolean isSuccess = activityResource.rejectActivityDelegation(rejectActivityTaskDTO);
        assert (!isSuccess);
    }

    @Test
    public void shouldRejectActivity() {
        RejectActivityTaskDTO rejectActivityTaskDTO = new RejectActivityTaskDTO();
        rejectActivityTaskDTO.setActivityID("10001");
        boolean isSuccess = activityResource.rejectActivityDelegation(rejectActivityTaskDTO);
        assert (isSuccess);
    }


    @Test
    public void shouldWithdrawApprovalActivityHandleException() {
        WithdrawApprovalActivityTaskDTO withdrawApprovalActivityTaskDTO = new WithdrawApprovalActivityTaskDTO();

        boolean isSuccess = activityResource.withdrawApproval(withdrawApprovalActivityTaskDTO);
        assert (!isSuccess);
    }

    @Test
    public void shouldWithdrawApprovalActivity() {
        WithdrawApprovalActivityTaskDTO withdrawApprovalActivityTaskDTO = new WithdrawApprovalActivityTaskDTO();
        withdrawApprovalActivityTaskDTO.setActivityID("10001");
        boolean isSuccess = activityResource.withdrawApproval(withdrawApprovalActivityTaskDTO);
        assert (isSuccess);
    }

    @AfterClass
    public static void afterClass() throws IOException {
        ContainerUtils.stop(application);
    }


    @Path("/rsqe/expedio-services")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public static class Handler {
        @PUT
        @Path("activities")
        public Response getActivities(GetActivityRequestDTO getActivityRequestDTOt) {
            assertThat(getActivityRequestDTOt.getSalesChannel(), Is.is("BT INDIA"));

            GenericEntity<List<ActivityDTO>> genericEntity = new GenericEntity<List<ActivityDTO>>(newArrayList(ActivityDTO.Builder.get().withActivityID("1").build())) {
            };
            return Response.ok(genericEntity).build();
        }

        @POST
        @Path("activity/create")
        public Response create(ActivityDTO activityDTO) {
            assertThat(activityDTO.getActivityDescription(), Is.is("do something"));
            return Response.ok("1").build();
        }

        @POST
        @Path("activity/ownership")
        public Response changeOwnership(ChangeOwnershipDTO changeOwnershipDTO) {
            assertThat(changeOwnershipDTO.getActivityID(), Is.is("2"));
            UpdateActivityResponseDTO updateActivityResponseDTO = new UpdateActivityResponseDTO();
            updateActivityResponseDTO.setActivityID(changeOwnershipDTO.getActivityID());
            GenericEntity<UpdateActivityResponseDTO> genericEntity = new GenericEntity<UpdateActivityResponseDTO>(updateActivityResponseDTO) {
            };
            return Response.ok(genericEntity).build();
        }

        @POST
        @Path("activity/status")
        public Response updateStatus(UpdateStatusDTO updateStatusDTO) {
            assertThat(updateStatusDTO.getActivityID(), Is.is("2"));
            UpdateActivityResponseDTO updateActivityResponseDTO = new UpdateActivityResponseDTO();
            updateActivityResponseDTO.setActivityID(updateStatusDTO.getActivityID());
            GenericEntity<UpdateActivityResponseDTO> genericEntity = new GenericEntity<UpdateActivityResponseDTO>(updateActivityResponseDTO) {
            };
            return Response.ok(genericEntity).build();
        }

        @POST
        @Path("activity/reassign")
        public Response activityReassign(ReassignActivityTaskDTO reassignActivityTaskDTO) throws URISyntaxException {
            if (reassignActivityTaskDTO == null || AssertObject.isEmpty(reassignActivityTaskDTO.getActivityID())) {
                throw new RuntimeException("Service Unavailable !!");
            }
            return Response.ok().build();
        }

        @POST
        @Path("activity/accept")
        public Response activityAccept(AcceptActivityTaskDTO acceptActivityTaskDTO) throws Exception {
            if (acceptActivityTaskDTO == null || AssertObject.isEmpty(acceptActivityTaskDTO.getActivityID())) {
                throw new RuntimeException("Service Unavailable !!");
            }
            return Response.ok().build();
        }

        @POST
        @Path("activity/reject")
        public Response activityReject(RejectActivityTaskDTO rejectActivityTaskDTO) throws Exception {
            if (rejectActivityTaskDTO == null || AssertObject.isEmpty(rejectActivityTaskDTO.getActivityID())) {
                throw new RuntimeException("Service Unavailable !!");
            }
            return Response.ok().build();
        }

        @POST
        @Path("activity/withdraw")
        public Response activityWithdraw(WithdrawApprovalActivityTaskDTO withdrawApprovalActivityTaskDTO) throws Exception {
            if (withdrawApprovalActivityTaskDTO == null || AssertObject.isEmpty(withdrawApprovalActivityTaskDTO.getActivityID())) {
                throw new RuntimeException("Service Unavailable !!");
            }
            return Response.ok().build();
        }
    }
}
