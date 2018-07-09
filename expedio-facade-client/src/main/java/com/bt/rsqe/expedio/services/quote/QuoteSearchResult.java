package com.bt.rsqe.expedio.services.quote;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class QuoteSearchResult {

    @XmlElement(name = "quoteSearchResult")
    List<QuoteDetailsDTO> quoteList;

    public QuoteSearchResult() {

    }

    public List<QuoteDetailsDTO> getQuoteList() {
        return quoteList;
    }

    public void setQuoteList(List<QuoteDetailsDTO> quoteList) {
        this.quoteList = quoteList;
    }
}
