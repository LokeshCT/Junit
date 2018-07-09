package com.bt.cqm.repository.vpn;


import com.bt.cqm.exception.VPNNotFoundException;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: 607982105
 * Date: 19/02/14
 * Time: 17:35
 * To change this template use File | Settings | File Templates.
 */
public interface VPNRepository {

    List<VPNEntity> findVPNByCustomerId(Long customerID) throws VPNNotFoundException;

    List<VPNEntity> findSharedVPNByCustomerId(Long customerID) throws VPNNotFoundException;
}
