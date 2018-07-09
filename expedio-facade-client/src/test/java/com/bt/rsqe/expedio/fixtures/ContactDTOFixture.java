package com.bt.rsqe.expedio.fixtures;

import com.bt.rsqe.expedio.contact.ContactDTO;
import com.bt.rsqe.expedio.contact.ContactDetailDTO;

import static com.google.common.collect.Lists.newArrayList;

public class ContactDTOFixture {
    private ContactDTO contactDTO;

    public ContactDTOFixture() {
        contactDTO = new ContactDTO();
        contactDTO.contacts = newArrayList();
    }

    public static ContactDTOFixture aContactDTO() {
        return new ContactDTOFixture();
    }

    public ContactDTO build() {
        return contactDTO;
    }

    public ContactDTOFixture withContactDetail(ContactDetailDTO contactDetailDTO) {
        contactDTO.contacts.add(contactDetailDTO);
        return this;
    }

    public ContactDTOFixture withQuoteId(String quoteId) {
        contactDTO.quoteId = quoteId;
        return this;
    }

    public ContactDTOFixture withExpedioReferenceId(String expedioReferenceId) {
        contactDTO.expedioReference = expedioReferenceId;
        return this;
    }
}
