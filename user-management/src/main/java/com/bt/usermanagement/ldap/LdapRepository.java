package com.bt.usermanagement.ldap;

import com.bt.usermanagement.ldap.model.LdapSearchModel;
import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class LdapRepository {

    public static final String[] SEARCH_ATTR = new String[]{LDAPConstants.BT_ORIGINAL_MAIL_ID, LDAPConstants.MAIL, LDAPConstants.PHONE_NUMBER, LDAPConstants.GIVENNAME, LDAPConstants.CN, LDAPConstants.TITLE, LDAPConstants.FULLNAME, LDAPConstants.SN, LDAPConstants.POSTAL_ADDRESS, LDAPConstants.MOBILE};

    private DirContext getContext() throws LdapSearchException {
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, LDAPConstants.CONTEXT_FACTORY_CLASS);
        env.put(Context.PROVIDER_URL, LDAPConstants.REPOSITORY_URL);
        env.put(Context.SECURITY_PRINCIPAL, LDAPConstants.BT_SECURITY_PRINCIPAL);

        DirContext ctx;

        try {
            ctx = new InitialDirContext(env);
        } catch (NamingException e) {
            throw new LdapSearchException("LDAP Context Not Found", e);
        }

        return ctx;
    }

    private void close(NamingEnumeration results, DirContext ctx) throws LdapSearchException {
        if (results != null) {
            try {
                results.close();
            } catch (Exception e) {
                throw new LdapSearchException("Ldap Attribute Not Found", e);
            }
        }
        if (ctx != null) {
            try {
                ctx.close();
            } catch (NamingException e) {
                throw new LdapSearchException("Ldap Context Not Found", e);
            }
        }
    }

    public List<LdapSearchModel> search(Map<String, String> searchArgs) throws LdapSearchException {
        List<LdapSearchModel> list = new ArrayList<LdapSearchModel>();
        NamingEnumeration results = null;
        DirContext ctx = null;
        try {
            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            controls.setReturningAttributes(SEARCH_ATTR);
            controls.setTimeLimit(5000);

            ctx = getContext();

            String filter = createFilterString(searchArgs);
            results = ctx.search("", filter, controls);

            while (results.hasMore()) {
                SearchResult searchResult = (SearchResult) results.next();
                Attributes attributes = searchResult.getAttributes();
                LdapSearchModel searchDTO = new LdapSearchModel();
                searchDTO.setValues(attributes);
                list.add(searchDTO);
            }

        } catch (NameNotFoundException e) {
            throw new LdapSearchException("Ldap Attribute Not Found", e);
        } catch (NamingException e) {
            throw new LdapSearchException("Ldap Attribute Not Found", e);
        } finally {
            close(results, ctx);
        }
        return list;
    }

    private String createFilterString(Map<String, String> args) {
        StringBuilder filters = new StringBuilder();
        filters.append("(&(objectclass=person)");
        if (args.get(LDAPConstants.EIN) != null && !args.get(LDAPConstants.EIN).isEmpty()) {
            filters.append("(").append(LDAPConstants.CN).append("=").append(args.get(LDAPConstants.EIN)).append(")");
        }
        if (args.get(LDAPConstants.FIRST_NAME) != null && !args.get(LDAPConstants.FIRST_NAME).isEmpty()) {
            filters.append("(").append(LDAPConstants.GIVENNAME).append("=*").append(args.get(LDAPConstants.FIRST_NAME)).append("*)");
            //filters.append("(givenname=").append(args.get(FIRST_NAME)).append("*)");
        }
        if (args.get(LDAPConstants.LAST_NAME) != null && !args.get(LDAPConstants.LAST_NAME).isEmpty()) {
            filters.append("(").append(LDAPConstants.SN).append("=*").append(args.get(LDAPConstants.LAST_NAME)).append("*)");
            //filters.append("(sn=").append(args.get("lastName")).append("*)");
        }
        if (args.get(LDAPConstants.EMAIL) != null && !args.get(LDAPConstants.EMAIL).isEmpty()) {
            filters.append("(").append(LDAPConstants.MAIL).append("=").append(args.get(LDAPConstants.EMAIL)).append(")");
            //filters.append("(mail=").append(args.get(EMAIL)).append(")");
        }if (args.get(LDAPConstants.BOAT_ID) != null && !args.get(LDAPConstants.BOAT_ID).isEmpty()) {
            filters.append("(").append(LDAPConstants.BT_ORIGINAL_MAIL_ID).append("=").append(args.get(LDAPConstants.BOAT_ID)).append(")");
        }

        filters.append(")");
        return filters.toString();
    }


}
