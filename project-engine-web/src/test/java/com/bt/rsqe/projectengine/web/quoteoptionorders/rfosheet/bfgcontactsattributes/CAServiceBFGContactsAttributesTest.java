package com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet.bfgcontactsattributes;


import com.bt.rsqe.domain.bom.parameters.BfgContact;
import org.junit.Test;

import java.util.List;

import static junit.framework.Assert.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.Is.*;

public class CAServiceBFGContactsAttributesTest {

    @Test
    public void returns_formatted_column_name(){
        assertThat(CAServiceBFGContactsAttributes.CONTACT_FIRST_NAME.columnName("firstName"),is("firstName First Name"));
        assertThat(CAServiceBFGContactsAttributes.CONTACT_LAST_NAME.columnName("lastName"),is("lastName Last Name"));
        assertThat(CAServiceBFGContactsAttributes.CONTACT_JOB_TITLE.columnName("jobTitle"),is("jobTitle Job Title"));
        assertThat(CAServiceBFGContactsAttributes.CONTACT_PHONE_NUMBER.columnName("1234"),is("1234 Phone Number"));
        assertThat(CAServiceBFGContactsAttributes.CONTACT_USER_NAME_EIN.columnName("name_ein"),is("name_ein User Name EIN"));
        assertThat(CAServiceBFGContactsAttributes.CONTACT_EMAIL_ADDRESS.columnName("email@bt.com"),is("email@bt.com Email Address"));
    }

    @Test
    public void bfgAttributes_should_have_null_value_when_contact_is_null(){
        List<BFGContactAttribute> bfgContactAttributes = CAServiceBFGContactsAttributes.getBFGContactsAttributes("Contact", null);

        assertThat(bfgContactAttributes.size(),is(6));
        assertTrue(bfgContactAttributes.contains(new BFGContactAttribute("Contact First Name")));
        assertTrue(bfgContactAttributes.contains(new BFGContactAttribute("Contact Last Name")));
        assertTrue(bfgContactAttributes.contains(new BFGContactAttribute("Contact Job Title")));
        assertTrue(bfgContactAttributes.contains(new BFGContactAttribute("Contact Phone Number")));
        assertTrue(bfgContactAttributes.contains(new BFGContactAttribute("Contact User Name EIN")));
        assertTrue(bfgContactAttributes.contains(new BFGContactAttribute("Contact Email Address")));
    }

    @Test
    public void bfgAttributes_should_have_values_when_contact_is_not_null(){
        BfgContact contact = new BfgContact(1L, "firstName", "lastName", 1, "", "", "IPSWICH", "SUFFOLK", "12781", "UNITED KINGDOM", "avbc@mac.cm", "12823322", "", 7789L, 1L, "Site Primary Contact", "Some Title", 1L, "12345", "67890", "3456");
        List<BFGContactAttribute> bfgContactAttributes = CAServiceBFGContactsAttributes.getBFGContactsAttributes("Contact", contact);

        assertThat(bfgContactAttributes.size(),is(6));
        assertTrue(bfgContactAttributes.contains(new BFGContactAttribute("Contact First Name", contact.getFirstName())));
        assertTrue(bfgContactAttributes.contains(new BFGContactAttribute("Contact Last Name", contact.getLastName())));
        assertTrue(bfgContactAttributes.contains(new BFGContactAttribute("Contact Job Title", contact.getJobTitle())));
        assertTrue(bfgContactAttributes.contains(new BFGContactAttribute("Contact Phone Number", contact.getPhoneNumber())));
        assertTrue(bfgContactAttributes.contains(new BFGContactAttribute("Contact User Name EIN", contact.getUin())));
        assertTrue(bfgContactAttributes.contains(new BFGContactAttribute("Contact Email Address", contact.getEmailAddress())));
    }
}
