package com.bt.cqm.handler;

import com.bt.cqm.repository.user.UserManagementRepository;
import com.bt.rsqe.customerinventory.dto.le.CusLeDTO;
import com.bt.rsqe.customerinventory.dto.le.LegalEntityDTO;
import com.bt.rsqe.customerinventory.resources.ILegalEntityResource;
import javax.ws.rs.core.Form;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Created with IntelliJ IDEA.
 * User: 608026723
 * Date: 10/17/14
 * Time: 4:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class LegalEntityHandlerTest {
    private LegalEntityHandler legalEntityHandler;
    @Mock
    private ILegalEntityResource iLegalEntityResourceMock;
    @Mock
    private UserManagementRepository cqmRepository;



    @Before
    public void setup(){
        MockitoAnnotations.initMocks(this);
        legalEntityHandler = new LegalEntityHandler(iLegalEntityResourceMock, cqmRepository);
    }

    @Test
    public void shouldGetLegalEntities(){
        List<CusLeDTO> cusLeDTOs = new ArrayList<CusLeDTO>();
        CusLeDTO cusLeDTO = new CusLeDTO();
        cusLeDTOs.add(cusLeDTO);
         Long cusId = 10001L;
         when(iLegalEntityResourceMock.getLegalEntities(any(Long.class))).thenReturn(cusLeDTOs);

        Response resp=legalEntityHandler.getLegalEntities(cusId);

       assert(resp.getStatus() == Response.Status.OK.getStatusCode());
    }

    @Test
    public void shouldGetLegalEntitiesReturnNFOnNoResult(){

        Long cusId = 10001L;
        when(iLegalEntityResourceMock.getLegalEntities(any(Long.class))).thenReturn(null);

        Response resp=legalEntityHandler.getLegalEntities(cusId);

        assert(resp.getStatus() == Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void shouldGetLegalEntitiesHandleException(){

        Long cusId = 10001L;
        doThrow(new RuntimeException()).when(iLegalEntityResourceMock).getLegalEntities(any(Long.class));

        Response resp=legalEntityHandler.getLegalEntities(cusId);

        assert(resp.getStatus() == Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    public void shouldCreateLegalEntityRetInvalidInputOnNullInput(){
        String generatedLeId = "1001";
        Form form = new Form();
         when(iLegalEntityResourceMock.createLegalEntity(anyString(),any(LegalEntityDTO.class))).thenReturn(generatedLeId);
          Response resp = legalEntityHandler.createLegalEntity("","1234","AMERICAS",form);

        assert (resp.getStatus() == Response.Status.BAD_REQUEST.getStatusCode());

    }

    @Test
    public void shouldCreateLegalEntity(){
        String generatedLeId = "1001";

        LegalEntityDTO legalEntityDTO = new LegalEntityDTO();
        when(iLegalEntityResourceMock.createLegalEntity(anyString(),any(LegalEntityDTO.class))).thenReturn(generatedLeId);
        Form form = new Form();
        Response resp = legalEntityHandler.createLegalEntity("123","1234","AMERICAS",form);

      assert (resp.getStatus() == Response.Status.OK.getStatusCode());

    }

    @Test
    public void shouldCreateLegalEntityHandleException(){

        LegalEntityDTO legalEntityDTO = new LegalEntityDTO();
        doThrow(new RuntimeException()).when(iLegalEntityResourceMock).createLegalEntity(anyString(),any(LegalEntityDTO.class));
        Form form = new Form();
        Response resp = legalEntityHandler.createLegalEntity("123","1234","AMERICAS",form);

        assert (resp.getStatus() == Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());

    }

    @Test
    public void shouldUpdateLegalEntity(){
        when(iLegalEntityResourceMock.updateLegalEntity(anyString(),any(LegalEntityDTO.class))).thenReturn(true);
        Form form = new Form();
        Response resp = legalEntityHandler.updateLegalEntity("123","1234","AMERICAS",form);
        Boolean status=(Boolean)resp.getEntity();
        assert (resp.getStatus() == Response.Status.OK.getStatusCode());
       assert (status);
    }

    @Test
    public void shouldUpdateLegalEntityHandleException(){
        LegalEntityDTO legalEntityDTO = new LegalEntityDTO();
        doThrow(new RuntimeException()).when(iLegalEntityResourceMock).updateLegalEntity(anyString(),any(LegalEntityDTO.class));
        Form form = new Form();
        Response resp = legalEntityHandler.updateLegalEntity("123","1234","AMERICAS",form);

         assert (resp.getStatus() == Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    public void shouldUpdateLegalEntityHandleNullInput(){
        LegalEntityDTO legalEntityDTO = null;
        when(iLegalEntityResourceMock.updateLegalEntity(anyString(),any(LegalEntityDTO.class))).thenReturn(true);
        Form form = new Form();
        Response resp = legalEntityHandler.updateLegalEntity("","1234","AMERICAS",form);

        assert (resp.getStatus() == Response.Status.BAD_REQUEST.getStatusCode());

    }

    @Test
    public void shouldCreateLegalEntityToCustomer(){
        CusLeDTO cusLeDTO = new CusLeDTO();
        String customerId="1234";
        String leId = "102";
        String leAssociationType = "Internal";
        String oldLeId = "104";
        when(iLegalEntityResourceMock.createLegalEntityLinkToCustomer(anyString(),any(CusLeDTO.class))).thenReturn(true);
        Response resp = legalEntityHandler.createLegalEntityToCustomer("USER_ID",customerId,leId,leAssociationType);
        assert (resp.getStatus() == Response.Status.OK.getStatusCode());
    }
    @Test
    public void shouldCreateLegalEntityToCustomerHandleException(){
        CusLeDTO cusLeDTO = new CusLeDTO();

        String customerId="1234";
        String leId = "102";
        String leAssociationType = "Internal";
        String oldLeId = "104";
        doThrow(new RuntimeException()).when(iLegalEntityResourceMock).createLegalEntityLinkToCustomer(anyString(),any(CusLeDTO.class));

        Response resp = legalEntityHandler.createLegalEntityToCustomer("USER",customerId,leId,leAssociationType);
        assert (resp.getStatus() == Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    public void shouldCreateLegalEntityToCustomerHandleInvalidInput(){
        CusLeDTO cusLeDTO = null;
        String customerId=null;
        String leId = "102";
        String leAssociationType = "Internal";
        String oldLeId = "104";
        when(iLegalEntityResourceMock.createLegalEntityLinkToCustomer(anyString(),any(CusLeDTO.class))).thenReturn(true);

        Response resp = legalEntityHandler.createLegalEntityToCustomer("USER_ID",customerId,leId,leAssociationType);
        assert (resp.getStatus() == Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void shouldUpdateLegalEntityToCustomer(){
        CusLeDTO cusLeDTO = new CusLeDTO();
        String customerId="1234";
        String leId = "102";
        String leAssociationType = "Internal";
        String oldLeId = "104";

        doReturn(true).when(iLegalEntityResourceMock).updateLegalEntityLinkToCustomer(anyString(),any(CusLeDTO.class),any(Long.class),any(Long.class));

        Response resp = legalEntityHandler.updateLegalEntityToCustomer("USER",customerId,leId,leAssociationType,oldLeId);
        assert (resp.getStatus() == Response.Status.OK.getStatusCode());
    }

    @Test
    public void shouldUpdateLegalEntityToCustomerHandleException(){
        CusLeDTO cusLeDTO = new CusLeDTO();
        String customerId="1234";
        String leId = "102";
        String leAssociationType = "Internal";
        String oldLeId = "104";

        doThrow(new RuntimeException()).when(iLegalEntityResourceMock).updateLegalEntityLinkToCustomer(anyString(),any(CusLeDTO.class), any(Long.class), any(Long.class));

        Response resp = legalEntityHandler.updateLegalEntityToCustomer("USER",customerId,leId,leAssociationType,oldLeId);
        assert (resp.getStatus() == Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    public void shouldUpdateLegalEntityToCustomerHandleInvalidInput(){

        String customerId=null;
        String leId = "102";
        String leAssociationType = "Internal";
        String oldLeId = "104";
        when(iLegalEntityResourceMock.updateLegalEntityLinkToCustomer(anyString(),any(CusLeDTO.class),any(Long.class),any(Long.class))).thenReturn(true);

        Response resp = legalEntityHandler.updateLegalEntityToCustomer("USER",customerId,leId,leAssociationType,oldLeId);
        assert (resp.getStatus() == Response.Status.BAD_REQUEST.getStatusCode());
    }
}
