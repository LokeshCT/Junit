package com.bt.rsqe.expedio.site;

/**
 * Created with IntelliJ IDEA.
 * User: 607882627
 * Date: 31/12/15
 * Time: 17:57
 * To change this template use File | Settings | File Templates.
 */
public class ContactNotFoundException extends Exception{

    private String msg;

    public ContactNotFoundException(String msg)
           {
               super(msg);
               this.msg = msg;
           }


}
