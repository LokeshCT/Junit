package com.bt.cqm.ldap;

import com.bt.cqm.ldap.model.LdapSearchModel;
import com.bt.rsqe.utils.AssertObject;

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

import static com.bt.cqm.ldap.LDAPConstants.*;

public class LdapRepository {

    public static final String[] SEARCH_ATTR = new String[]{BT_ORIGINAL_MAIL_ID, MAIL, PHONE_NUMBER, GIVENNAME, CN, TITLE, FULLNAME, SN,THIRD_PARTY_EMAIL};

    private DirContext getContext() throws LdapSearchException {
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, CONTEXT_FACTORY_CLASS);
        env.put(Context.PROVIDER_URL, REPOSITORY_URL);
        env.put(Context.SECURITY_PRINCIPAL, BT_SECURITY_PRINCIPAL);

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
                if (!AssertObject.anyEmpty(searchDTO.getBoatId())) {
                    list.add(searchDTO);
                }
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
        if (args.get(EIN) != null && !args.get(EIN).isEmpty()) {
            filters.append("(").append(CN).append("=").append(args.get(EIN)).append(")");
        }
        if (args.get(FIRST_NAME) != null && !args.get(FIRST_NAME).isEmpty()) {
            filters.append("(").append(GIVENNAME).append("=*").append(args.get(FIRST_NAME)).append("*)");
            //filters.append("(givenname=").append(args.get(FIRST_NAME)).append("*)");
        }
        if (args.get(LAST_NAME) != null && !args.get(LAST_NAME).isEmpty()) {
            filters.append("(").append(SN).append("=*").append(args.get(LAST_NAME)).append("*)");
            //filters.append("(sn=").append(args.get("lastName")).append("*)");
        }
        if (args.get(EMAIL) != null && !args.get(EMAIL).isEmpty()) {
            filters.append("(").append(MAIL).append("=").append(args.get(EMAIL)).append(")");
            //filters.append("(mail=").append(args.get(EMAIL)).append(")");
        }
        if (args.get(BOAT_ID) != null && !args.get(BOAT_ID).isEmpty()) {
            filters.append("(").append(BT_ORIGINAL_MAIL_ID).append("=").append(args.get(BOAT_ID)).append(")");
        }


        filters.append(")");
        return filters.toString();
    }

}
