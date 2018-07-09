package com.bt.cqm.repository.channelhierarchy;

import com.bt.cqm.exception.PriceBookException;
import com.bt.rsqe.persistence.PersistenceManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created with IntelliJ IDEA.
 * User: 607982105
 * Date: 27/03/14
 * Time: 12:38
 * To change this template use File | Settings | File Templates.
 */
public class PriceBookRepositoryJPATest {

    @Mock
    private PersistenceManager persistenceManager;
    @Mock
    private PriceBookRepositoryJPA repository;
    @Mock
    private EntityManager entityManager;
    @Mock
    private Query query;
    @Mock
    private Query querySecond;
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        repository = new PriceBookRepositoryJPA(persistenceManager);
    }

    @After
    public void tearDown() throws Exception {

    }



    @Test
    public void testGetPTPPriceBookVersion() throws Exception {

    }

    @Test
    public void test() throws Exception {

    }

    @Test
    //Method: GetPriceBookDetails
    //Description: Checking for the successful getting of Product Details.
    public void testGetPriceBookDetailsSuccess() throws Exception {

        long customerId = 123456L;
        // expectations
        // expectations
        List<PriceBookEntity> priceBookEntities = new ArrayList<PriceBookEntity>();
        PriceBookEntity priceBookEntity = new PriceBookEntity(1234L, 21265L, "1243", "IVPN", "V89", "V86", "1", "20140808","1234");
        priceBookEntities.add(priceBookEntity);
        when(persistenceManager.query(PriceBookEntity.class,"select  c from PriceBookEntity c where " +
                " c.pbCustomerId = ?0 ",customerId)).thenReturn(priceBookEntities);
        //Execute
        List<PriceBookEntity> responsePriceBookEntities = repository.getPriceBookDetails(customerId);
        //Check
        assert ((priceBookEntities).equals(responsePriceBookEntities));

    }

    @Test
    //Method: GetPriceBookDetails
    //Description: Checking No Record Found Exception.
    public void testGetPriceBookDetailsExceptions() throws Exception {

        long customerId = 123456L;
        // expectations
        // expectations
        List<PriceBookEntity> priceBookEntities = new ArrayList<PriceBookEntity>();
        // PriceBookEntity priceBookEntity = new PriceBookEntity(1234L, 21265L, "1243", "IVPN", "V89", "V86", "1", "20140808","1234");
        //priceBookEntities.add(priceBookEntity);
        when(persistenceManager.query(PriceBookEntity.class,"select  c from PriceBookEntity c where " +
                " c.pbCustomerId = ?0 ",customerId)).thenReturn(priceBookEntities);
        //Execute
        try
        {
            List<PriceBookEntity> responsePriceBookEntities = repository.getPriceBookDetails(customerId);
        }
        catch(PriceBookException e)
        {
            //Check
            assertEquals("Price Book Details not found for Customer: 123456", e.getMessage());
        }
    }

    @Test
    //Method: GetPriceBookDetails
    //Description: Checking Successful Creation of PriceBook..
    public void testCreatePriceBookSuccess() throws Exception {
        String  salesChannelId = "1243";
        String customerId ="21453";
        String customerName ="SUB-CP-TEST";
        String productName ="VPN";
        String rrpVersion ="V86";
        String ptpVersion ="V87";
        Object[] priceBookDetails = {"1234","12","1","IPVN"};
        String requestSequenceId = "0000000000000123";
        // expectations
        List<String> productNameList = new ArrayList<String>();
        productNameList.add("TestProduct");

        when(persistenceManager.entityManager()).thenReturn(entityManager);
        when(entityManager.createNativeQuery(anyString())).thenReturn(querySecond);
        when(querySecond.getSingleResult()).thenReturn(priceBookDetails);

        when(persistenceManager.entityManager()).thenReturn(entityManager);
        when(entityManager.createNativeQuery("select to_char(max(c1) + 1, '000000000000000') from T5730")).thenReturn(query);
        when(query.getSingleResult()).thenReturn(requestSequenceId);
        when(query.executeUpdate()).thenReturn(0);
        String result= repository.createPriceBook(salesChannelId, customerId, customerName,productName, rrpVersion, ptpVersion);
        assertEquals("0",result);
    }

    @Test
    //Method: GetPriceBookDetails
    //Description: Checking Failure Creation of PriceBook..
    public void testCreatePriceBookFailure() throws Exception {
        String  salesChannelId = "1243";
        String customerId ="21453";
        String customerName ="SUB-CP-TEST";
        String productName ="VPN";
        String rrpVersion ="V86";
        String ptpVersion ="V87";
        Object[] priceBookDetails = {"1234","12","1","IPVN"};
        String requestSequenceId = "0000000000000123";
        // expectations
        List<String> productNameList = new ArrayList<String>();
        productNameList.add("TestProduct");

        when(persistenceManager.entityManager()).thenReturn(entityManager);
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(querySecond.getSingleResult()).thenReturn(priceBookDetails);

        when(persistenceManager.entityManager()).thenReturn(entityManager);
        when(entityManager.createNativeQuery("select to_char(max(c1) + 1, '000000000000000') from T5730")).thenReturn(query);
        when(query.getSingleResult()).thenReturn(requestSequenceId);
        when(query.executeUpdate()).thenReturn(0);
        String result= repository.createPriceBook(salesChannelId, customerId, customerName,productName, rrpVersion, ptpVersion);
        assertEquals("-1",result);
    }
}
