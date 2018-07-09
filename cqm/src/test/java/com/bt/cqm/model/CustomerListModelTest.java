package com.bt.cqm.model;

import com.bt.rsqe.customerinventory.pagination.QueryResult;
import com.bt.rsqe.util.AbstractPOJOTest;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;

/**
 * Created with IntelliJ IDEA.
 * User: 608026723
 * Date: 7/9/14
 * Time: 5:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class CustomerListModelTest extends AbstractPOJOTest {

    @Test
    public void shouldTestCustomerListModel() throws Exception{
        testPOJO(CustomerListModel.class);
    }

    @Override
    protected void addCustomTestValues() {
        addTestValue(QueryResult.class,new QueryResult());
    }

    @Test
    public void shouldCreateObjectUsingConstructor(){
        CustomerListModel obj = new CustomerListModel(true,"CUSTNAME","SALESCHANNEL","ERROR MSG", new QueryResult()) ;
        assertNotNull(obj);
    }
}
