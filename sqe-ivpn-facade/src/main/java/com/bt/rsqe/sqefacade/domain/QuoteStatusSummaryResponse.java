package com.bt.rsqe.sqefacade.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class QuoteStatusSummaryResponse extends SqeResponse<List<Ipcg2QuoteStatusSummary>> {

}
