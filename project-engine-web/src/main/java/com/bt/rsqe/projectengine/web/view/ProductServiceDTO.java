package com.bt.rsqe.projectengine.web.view;

import com.bt.rsqe.customerrecord.ServiceDTO;
import com.bt.rsqe.projectengine.web.view.filtering.PaginatedFilterResult;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: 605908589
 * Date: 21/10/14
 * Time: 15:33
 * To change this template use File | Settings | File Templates.
 */
public class ProductServiceDTO {

    @XmlElement
    public List<ServiceDTO> services = new ArrayList<ServiceDTO>();
    @XmlElement(name = "sEcho")
    public int pageNumber;
    @XmlElement(name = "iTotalDisplayRecords")
    public int totalDisplayRecords = 0;
    @XmlElement(name = "iTotalRecords")
    public int totalRecords = 0;

    public ProductServiceDTO() { /*JAXB*/
    }

    public ProductServiceDTO(PaginatedFilterResult<ServiceDTO> paginatedFilterResult) {
        this.pageNumber = paginatedFilterResult.getPageNumber();
        this.totalDisplayRecords = paginatedFilterResult.getFilteredSize();
        this.totalRecords = paginatedFilterResult.getTotalRecords();
        this.services = paginatedFilterResult.getItems();
    }
}
