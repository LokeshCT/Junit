package com.bt.rsqe.inlife.web;

import com.bt.rsqe.inlife.entities.ErrorFactEntity;
import com.bt.rsqe.inlife.entities.ExceptionPointEntity;
import com.bt.rsqe.inlife.entities.UserEntity;
import com.bt.rsqe.inlife.repository.ErrorFactRepository;
import com.bt.rsqe.inlife.repository.ExceptionPointRepository;
import com.bt.rsqe.inlife.repository.UserRepository;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.bt.rsqe.monitoring.ErrorFactDTO;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/rsqe/inlife/error-metrics")
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class ErrorMetricsResourceHandler
{
    private static final Logger LOG = LogFactory.createDefaultLogger(Logger.class);

    private ErrorFactRepository errorFactRepository;
    private UserRepository userRepository;
    private ExceptionPointRepository exceptionPointRepository;

    public ErrorMetricsResourceHandler (ErrorFactRepository errorFactRepository, UserRepository userRepository, ExceptionPointRepository exceptionPointRepository)
    {
        this.errorFactRepository = errorFactRepository;
        this.userRepository = userRepository;
        this.exceptionPointRepository = exceptionPointRepository;
    }

    @POST
    public Response create (ErrorFactDTO errorFactDTO)
    {
        UserEntity userEntityByName = userRepository.getUserEntityByName(errorFactDTO.getUserId().getUserIdentifier());
        if (userEntityByName != null)
        {
            errorFactDTO.setUserId(userEntityByName.toDto());
        }
        ExceptionPointEntity exceptionPointEntityByName = exceptionPointRepository.getExceptionPointEntityByName(errorFactDTO.getExceptionPointId().getExceptionPoint());
        if (exceptionPointEntityByName != null)
        {
            errorFactDTO.setExceptionPointId(exceptionPointEntityByName.toDto());
        }
        LOG.errorFactReceived(errorFactDTO);
        int recordId = errorFactRepository.save(ErrorFactEntity.fromDto(errorFactDTO));
        return Response.status(Response.Status.CREATED).entity("" + recordId).build();
    }

    @Path("startTime/{startTime}/endTime/{endTime}")
    @GET
    public Response getErrorFactsWithinTimeFrame (@PathParam("startTime") String startTime, @PathParam("endTime") String endTime)
    {
        List<ErrorFactEntity> errorFactsWithinTimeFrame = errorFactRepository.getErrorFactsWithinTimeFrame(startTime, endTime);
        List<ErrorFactDTO> errorFactDTOList = new ArrayList<ErrorFactDTO>();

        for (ErrorFactEntity errorFactEntity : errorFactsWithinTimeFrame)
        {
            errorFactDTOList.add(errorFactEntity.toDto());
        }

        return Response.ok(new GenericEntity<List<ErrorFactDTO>>(errorFactDTOList) {
        }).build();
    }

    @Path("optionLineItem/{optionLineItem}")
    @GET
    public Response getErrorCount (@PathParam("optionLineItem") String optionLineItem)
    {
        long errorFactEntityByOptionLineItemId = errorFactRepository.getErrorFactEntityByOptionLineItemId(optionLineItem);
        return Response.ok(new GenericEntity<Long>(errorFactEntityByOptionLineItemId) {} ).build();
    }

    private interface Logger
    {
        @Log(level = LogLevel.DEBUG, format = "Error fact to persist, %s")
        void errorFactReceived(ErrorFactDTO errorFactDTO);
    }
}
