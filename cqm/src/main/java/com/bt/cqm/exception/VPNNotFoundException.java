package com.bt.cqm.exception;

/**
 * Created with IntelliJ IDEA.
 * User: 607982105
 * Date: 19/02/14
 * Time: 17:52
 * To change this template use File | Settings | File Templates.
 */
public class VPNNotFoundException extends Exception{

    public VPNNotFoundException(final String message) {
        super(message);
    }

  /*  public VPNNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public VPNNotFoundException(final Throwable cause) {
        super(cause);
    }*/
}
