package com.bt.rsqe.projectengine.web;

import com.bt.rsqe.web.AjaxResponseDTO;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ImportProductResponseDTO extends AjaxResponseDTO {

    @XmlElement
    private List<String> impactedLineItems;     //this is used in Js.

    public ImportProductResponseDTO() {
    }

    public ImportProductResponseDTO(boolean isImportSuccessful, String errors, List<String> impactedLineItems) {
        super(isImportSuccessful, errors);
        this.impactedLineItems = impactedLineItems;
    }
}
