package com.bt.rsqe.customerinventory.service.rootAssetUpdater;

import com.bt.rsqe.bfgfacade.repository.BfgRepository;
import com.bt.rsqe.bfgfacade.write.sp.IStoredProcedureFacade;
import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.service.repository.CIFAssetJPARepository;
import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.quoteengine.repository.ProjectJPARepository;
import com.bt.rsqe.rest.ResponseBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.bt.rsqe.utils.AssertObject.isNotNull;


@Path("/rsqe/customer-inventory-service/rootAssetService")
@Produces({MediaType.APPLICATION_JSON})
public class RootAssetOrchestrator {

    protected static ProductInstanceClient productInstanceClient;
    protected static CIFAssetJPARepository cifAssetJPARepository;
    protected static BfgRepository bfgRepository;
    protected static IStoredProcedureFacade iStoredProcedureFacade;
    protected static PmrClient pmr;

    static GetRootAssetDetail getRootAssetDetail = new GetRootAssetDetail();

    private static final Logger LOG = LoggerFactory.getLogger(RootAssetOrchestrator.class);

    public RootAssetOrchestrator(ProductInstanceClient productInstanceClient, CIFAssetJPARepository cifAssetJPARepository, BfgRepository bfgRepository, IStoredProcedureFacade iStoredProcedureFacade,PmrClient pmr) {
        this.productInstanceClient = productInstanceClient;
        this.cifAssetJPARepository = cifAssetJPARepository;
        this.bfgRepository = bfgRepository;
        this.iStoredProcedureFacade=iStoredProcedureFacade;
        this.pmr = pmr;

    }




    @Path("/rootAssetUpdater/{CustomerID}")
    @GET
    public Response rootAssetUpdater(@PathParam("CustomerID") String CustomerID) {
        String flag = getRootAssetDetail.updateCustomerDetails(productInstanceClient, cifAssetJPARepository, bfgRepository, CustomerID,iStoredProcedureFacade,true,pmr);

        if(flag.equals("true")){
            return Response.status(Response.Status.ACCEPTED).build();
        }else if (flag.equals("Empty")){
            LOG.info("No relevant Customer data available for Customer-ID"+CustomerID);
            return Response.status(Response.Status.NOT_FOUND).build();
        } else{
            LOG.info("Error" +flag);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }


    @Path("/rootAssetDetailUpdater")
    @POST
    public boolean rootAssetDetailUpdater(String expRef) {
        boolean flag = false;
        String response = null;
        String CustomerID = getRootAssetDetail.getCustomerId(expRef,bfgRepository,cifAssetJPARepository);
       if(isNotNull(CustomerID)){
           response = getRootAssetDetail.updateCustomerDetails(productInstanceClient, cifAssetJPARepository, bfgRepository, CustomerID,iStoredProcedureFacade,false,pmr);
           if(response.equals("true")){
               flag = true;
           } else{
               LOG.info("Error" +response);
           }
           LOG.info("Customer delta updated"+flag);
       }   else{
           LOG.info("No Customer Present");
       }

        return flag;
    }

    public static  void main(String[] args) {
        new RootAssetOrchestrator(productInstanceClient, cifAssetJPARepository,bfgRepository,iStoredProcedureFacade,pmr).rootAssetUpdater("7789");
    }

}
