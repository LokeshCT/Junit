package com.bt.cqm.ldap;

/**
 * Created with IntelliJ IDEA.
 * User: 607520161
 * Date: 25/02/14
 * Time: 14:33
 * To change this template use File | Settings | File Templates.
 */
public interface LDAPConstants {
    /* LDAP Related constants */
    final String REPOSITORY_URL = "ldap://de-ldap.nat.bt.com";
    final String CONTEXT_FACTORY_CLASS = "com.sun.jndi.ldap.LdapCtxFactory";
    final String BT_SECURITY_PRINCIPAL = "ou=people,ou=btplc,o=bt??one";

    /* Searchable attributes */
    final String FIRST_NAME = "firstName";
    final String LAST_NAME = "lastName";
    final String EMAIL = "email";
    final String EIN = "ein";
    final String BOAT_ID="boatId";

    /* Search arguments */
    final String BT_ORIGINAL_MAIL_ID = "btoriginalmailid";
    final String MAIL = "mail";
    final String PHONE_NUMBER = "telephonenumber";
    final String GIVENNAME = "givenname";
    final String CN = "cn";
    final String TITLE = "title";
    final String FULLNAME = "fullname";
    final String SN = "sn";
    final String THIRD_PARTY_EMAIL="btrfc822mailbox";
}
