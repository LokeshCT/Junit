package com.bt.rsqe.projectengine.web.quoteoption;

import com.bt.rsqe.projectengine.web.facades.BulkUploadFacade;
import com.bt.rsqe.projectengine.web.uri.UriFactory;
import com.bt.rsqe.web.AjaxResponseDTO;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;

public class QuoteOptionBulkUploadOrchestrator {
    private UriFactory uriFactory;
    private BulkUploadFacade bulkUploadFacade;

    public QuoteOptionBulkUploadOrchestrator(UriFactory uriFactory, BulkUploadFacade bulkUploadFacade) {
        this.uriFactory = uriFactory;
        this.bulkUploadFacade = bulkUploadFacade;
    }

    public AjaxResponseDTO upload(String productCode, FormDataMultiPart multiPartFormData) {
        final String bulkUploadUri = uriFactory.getBulkUploadUri(productCode);
        return bulkUploadFacade.upload(bulkUploadUri, multiPartFormData);
    }
}
