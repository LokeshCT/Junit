package com.bt.cqm.ldap;

public class LdapSearchException extends Exception {

    public LdapSearchException(final String message) {
        super(message);

    }

    public LdapSearchException(final Throwable cause) {
        super(cause);
    }

    public LdapSearchException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
