package com.bt.cqm.utils;

import com.bt.rsqe.web.rest.dto.ErrorDTO;
import org.junit.Test;

import javax.ws.rs.core.GenericEntity;

/**
 * Created with IntelliJ IDEA.
 * User: 608026723
 * Date: 3/24/15
 * Time: 2:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class UtilityTest {

    @Test
    public void shouldBuildGenericError(){
        String errorMsg = " Test error msg";
        GenericEntity<ErrorDTO> genericError = Utility.buildGenericError(errorMsg);

        assert (genericError !=null);
        assert (((ErrorDTO)genericError.getEntity()).description.equals(errorMsg)) ;
    }

    @Test
    public void shouldBuildGenericErrorForNullInput(){
        String errorMsg =null;
        GenericEntity<ErrorDTO> genericError = Utility.buildGenericError(errorMsg);

        assert (genericError !=null);
        assert (((ErrorDTO)genericError.getEntity()).description == null) ;
    }
}
