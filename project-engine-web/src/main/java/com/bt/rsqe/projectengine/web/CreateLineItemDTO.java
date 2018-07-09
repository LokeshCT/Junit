package com.bt.rsqe.projectengine.web;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class CreateLineItemDTO {
    @XmlElement
    private String rsqeQuoteOptionId;
    @XmlElement
    private String expedioQuoteId;
    @XmlElement
    private String expedioCustomerId;
    @XmlElement
    private String authenticationToken;
    @XmlElement
    private List<ActionDTO> lineItems = new ArrayList<ActionDTO>();

    public CreateLineItemDTO(String rsqeQuoteOptionId, String expedioQuoteId, String expedioCustomerId, String authenticationToken, List<ActionDTO> lineItems) {
        this.rsqeQuoteOptionId = rsqeQuoteOptionId;
        this.expedioQuoteId = expedioQuoteId;
        this.expedioCustomerId = expedioCustomerId;
        this.authenticationToken = authenticationToken;
        this.lineItems = lineItems;
    }

    public CreateLineItemDTO() {/*jaxb*/
    }

    public String getRsqeQuoteOptionId() {
        return rsqeQuoteOptionId;
    }

    public String getExpedioQuoteId() {
        return expedioQuoteId;
    }

    public String getExpedioCustomerId() {
        return expedioCustomerId;
    }

    public String getAuthenticationToken() {
        return authenticationToken;
    }

    public List<ActionDTO> getLineItems() {
        return lineItems;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ActionDTO {
        @XmlElement
        public String expedioSiteId;
        @XmlElement
        public String action;

        public ActionDTO(String expedioSiteId, String action) {
            this.expedioSiteId = expedioSiteId;
            this.action = action;
        }

        public ActionDTO() { /*jaxb*/
        }
    }
}
