package com.bt.rsqe.projectengine.web.quoteoption.lineitems;

import com.bt.rsqe.dto.NoteDto;
import com.bt.rsqe.security.UserContextProvider;
import com.bt.rsqe.utils.DateProvider;
import org.joda.time.DateTime;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@Path("/rsqe/customers/{customerId}/contracts/{contractId}/projects/{projectId}/quote-options/{quoteOptionId}/line-items/{lineItemId}/notes")
public class LineItemNoteResourceHandler {

    private LineItemNoteInteractor interactor;
    private UserContextProvider userContextProvider;
    private DateProvider dateProvider;

    public LineItemNoteResourceHandler(LineItemNoteInteractor interactor, UserContextProvider userContextProvider, DateProvider dateProvider) {
        this.interactor = interactor;
        this.userContextProvider = userContextProvider;
        this.dateProvider = dateProvider;
    }
    
    @GET
    public Response getNotes(
        @PathParam("customerId") String customerId,
        @PathParam("contractId") String contractId,
        @PathParam("projectId") String projectId,
        @PathParam("quoteOptionId") String optionId,
        @PathParam("lineItemId") String itemId
    ) {
        return interactor.getNotes(customerId, contractId, projectId, optionId, itemId);
    }

    @POST
    public Response createNote(
        @PathParam("projectId") String projectId,
        @PathParam("quoteOptionId") String optionId,
        @PathParam("lineItemId") String itemId,
        @FormParam("newText") String noteText
    ) {
        NoteDto note = createNoteDto(noteText);
        return interactor.createNote(projectId, optionId, itemId, note);
    }

    private NoteDto createNoteDto(String noteText) {
        DateTime createdOn = dateProvider.now();
        String createdBy = userContextProvider.getCurrentUserLoginName();
        return new NoteDto(noteText, createdOn, createdBy);
    }

}
