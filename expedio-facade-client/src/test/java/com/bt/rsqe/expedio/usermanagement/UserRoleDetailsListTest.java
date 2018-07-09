package com.bt.rsqe.expedio.usermanagement;

import com.bt.rsqe.util.AbstractPOJOTest;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: 607982105
 * Date: 01/04/15
 * Time: 16:56
 * To change this template use File | Settings | File Templates.
 */
public class UserRoleDetailsListTest extends AbstractPOJOTest {

    @Override
    public void addCustomTestValues() {
        addTestValue(List.class,new ArrayList<UserRoleDetails>());
    }
    @Test
    public void   shouldTestGetterSetter() throws Exception {
        testPOJO(UserRoleDetailsList.class);

    }
}
