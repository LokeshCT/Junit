package com.bt.rsqe.projectengine.web.quoteoptiondetails;

import com.bt.rsqe.domain.AttachmentDTO;
import com.bt.rsqe.dto.NoteDto;
import com.bt.rsqe.emppal.attachmentresource.AttachmentCategoryEnum;
import com.bt.rsqe.emppal.attachmentresource.AttachmentManager;
import com.bt.rsqe.expedio.project.ExpedioProjectResource;
import com.bt.rsqe.inlife.client.ApplicationCapabilityProvider;
import com.bt.rsqe.inlife.client.ApplicationPropertyResourceClient;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.bt.rsqe.projectengine.AttachmentViewDTO;
import com.bt.rsqe.projectengine.web.facades.LineItemFacade;
import com.bt.rsqe.projectengine.web.facades.ProductIdentifierFacade;
import com.bt.rsqe.projectengine.web.facades.QuoteOptionFacade;
import com.bt.rsqe.projectengine.web.facades.UserFacade;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.DetailsDTOLineItemVisitor;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.PriceSuppressStrategy;
import com.bt.rsqe.projectengine.web.uri.UriFactory;
import com.bt.rsqe.projectengine.web.uri.UriFactoryImpl;
import com.bt.rsqe.projectengine.web.view.NotesView;
import com.bt.rsqe.projectengine.web.view.QuoteOptionDetailsDTO;
import com.bt.rsqe.projectengine.web.view.QuoteOptionDetailsView;
import com.bt.rsqe.projectengine.web.view.filtering.PaginatedFilter;
import com.bt.rsqe.projectengine.web.view.filtering.PaginatedFilterResult;
import com.bt.rsqe.projectengine.web.view.sorting.PaginatedSort;
import com.bt.rsqe.projectengine.web.view.sorting.PaginatedSortResult;
import com.bt.rsqe.security.UserDTO;
import com.google.common.base.Optional;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.bt.rsqe.inlife.client.ApplicationCapabilityProvider.Capability.*;

public class QuoteOptionDetailsOrchestrator {

    private static final QuoteOptionDetailsOrchestratorLogger LOG = LogFactory.createDefaultLogger(QuoteOptionDetailsOrchestratorLogger.class);
    private LineItemFacade lineItemFacade;
    private UriFactory productConfiguratorUriFactory;
    private ProductIdentifierFacade productIdentifierFacade;
    private UserFacade userFacade;
    private QuoteOptionFacade quoteOptionFacade;
    private ExpedioProjectResource projectResource;
    private AttachmentManager attachmentManager;
    private ApplicationCapabilityProvider capabilityProvider;
    private ApplicationPropertyResourceClient applicationPropertyResourceClient;
    public static final String FILE_SIZE_VALIDATION_MESSAGE = "File size should be less than 10Mb, please reduce this file size or split the file into two parts and upload again.";

    public QuoteOptionDetailsOrchestrator(LineItemFacade lineItemFacade,
                                          UriFactory productConfiguratorUriFactory,
                                          ProductIdentifierFacade productIdentifierFacade,
                                          UserFacade userFacade,
                                          QuoteOptionFacade quoteOptionFacade,
                                          ExpedioProjectResource projectResource,
                                          AttachmentManager attachmentManager,
                                          ApplicationCapabilityProvider capabilityProvider,
                                          ApplicationPropertyResourceClient applicationPropertyResourceClient) {
        this.lineItemFacade = lineItemFacade;
        this.productConfiguratorUriFactory = productConfiguratorUriFactory;
        this.productIdentifierFacade = productIdentifierFacade;
        this.userFacade = userFacade;
        this.quoteOptionFacade = quoteOptionFacade;
        this.projectResource = projectResource;
        this.attachmentManager = attachmentManager;
        this.capabilityProvider = capabilityProvider;
        this.applicationPropertyResourceClient = applicationPropertyResourceClient;
    }

    public QuoteOptionDetailsView buildView(String customerId, String contractId, String projectId, String quoteOptionId) {
        final String currency = quoteOptionFacade.get(projectId, quoteOptionId).currency;
        final String salesOrganization = projectResource.getProject(projectId).organisation.name;
        final boolean allowCopyOptions = capabilityProvider.isFunctionalityEnabled(ALLOW_COPY_OPTION_ITEMS, false, Optional.of(quoteOptionId));
        final int maxConfigurableLineItems = Integer.parseInt(applicationPropertyResourceClient.getApplicationProperty("maxConfigurableLineItems", "30", quoteOptionId).getValue());
        final boolean removeLineItemAllowed = capabilityProvider.isFunctionalityEnabled(REMOVE_LINE_ITEM_ALLOWED, true, Optional.of(quoteOptionId));

        final QuoteOptionDetailsView view = new QuoteOptionDetailsView(customerId,
                                                                       contractId,
                                                                       projectId,
                                                                       quoteOptionId,
                                                                       "",
                                                                       currency,
                                                                       salesOrganization,
                                                                       productConfiguratorUriFactory,
                                                                       allowCopyOptions,
                                                                       maxConfigurableLineItems,
                                                                       removeLineItemAllowed,
                                                                       UriFactoryImpl.viewConfigurationDialog(customerId, contractId, projectId).toString());
        view.setProducts(productIdentifierFacade.getAllSellableProducts());
        return view;
    }

    public String uploadAttachment(String customerId, String projectId, String categoryId, String fileName,
                                   byte[] attachmentContent) throws IOException, InvalidFormatException {
        if(!attachmentManager.acceptedFileSize(attachmentContent.length)){
            return FILE_SIZE_VALIDATION_MESSAGE;
        }
        return attachmentManager.uploadAttachment(customerId,projectId,categoryId,fileName,attachmentContent, StringUtils.EMPTY);
    }

    public AttachmentViewDTO loadAttachmentTable(String customerId, String projectId, String categoryId, PaginatedFilter paginatedFilter) {
        String parentPath = attachmentManager.getAttachmentParentPath(categoryId,customerId,projectId);
        List<AttachmentDTO> availableAttachments = attachmentManager.loadAttachments(categoryId, customerId, projectId, parentPath);
        List<AttachmentViewDTO.ItemRowDTO> attachmentItemRowDtos;

        if (AttachmentCategoryEnum.BidManager.getValue().equals(categoryId)) {
            attachmentItemRowDtos = buildCostAttachmentView(availableAttachments);
        } else {
            attachmentItemRowDtos = buildAttachmentView(availableAttachments, categoryId);
        }
        for(AttachmentViewDTO.ItemRowDTO itemRowDTO :attachmentItemRowDtos ){
            LOG.logAttachmentLoadResponse(itemRowDTO.uploadAppliesTo,itemRowDTO.uploadFileName,itemRowDTO.getUploadDate(),itemRowDTO.id);
        }
        final PaginatedFilterResult<AttachmentViewDTO.ItemRowDTO> filterResult = paginatedFilter.applyTo(attachmentItemRowDtos);
        return new AttachmentViewDTO(filterResult.getItems(),filterResult.getPageNumber(),filterResult.getFilteredSize(),filterResult.getTotalRecords());
    }

    public String deleteAttachment(String documentId, String categoryId, String customerId, String projectId) {
        return attachmentManager.deleteAttachment(documentId,categoryId,customerId,projectId, "");
    }

    public AttachmentDTO downloadAttachment(String documentId, String categoryId, String customerId, String projectId) {
        return attachmentManager.downloadAttachment(documentId,categoryId,customerId,projectId, StringUtils.EMPTY, StringUtils.EMPTY);

    }

    public List<AttachmentViewDTO.ItemRowDTO> buildAttachmentView(List<AttachmentDTO> attachmentDTOs, String categoryId) {
        List<AttachmentViewDTO.ItemRowDTO> attachmentItemRowDtos = new ArrayList<AttachmentViewDTO.ItemRowDTO>();
        for(AttachmentDTO attachmentDTO : attachmentDTOs) {
            attachmentItemRowDtos.add(new AttachmentViewDTO.ItemRowDTO(categoryId, attachmentDTO.getFileName(), attachmentDTO.getCreatedDate().toString(), attachmentDTO.getDocumentId()));
        }
        return attachmentItemRowDtos;
    }

    public List<AttachmentViewDTO.ItemRowDTO> buildCostAttachmentView(List<AttachmentDTO> attachmentDTOs) {
        List<AttachmentViewDTO.ItemRowDTO> attachmentItemRowDtos = new ArrayList<AttachmentViewDTO.ItemRowDTO>();
        Collections.sort(attachmentDTOs, new Comparator<AttachmentDTO>() {
            public int compare(AttachmentDTO attachmentDTO1, AttachmentDTO attachmentDTO2) {
                return attachmentDTO2.getCreatedDate().toDate().compareTo(attachmentDTO1.getCreatedDate().toDate());
            }
        });

        for (AttachmentDTO attachmentDTO : attachmentDTOs) {
            DateTime utcCreatedDate = attachmentDTO.getCreatedDate().withZone(DateTimeZone.UTC);
            attachmentItemRowDtos.add(new AttachmentViewDTO.ItemRowDTO(AttachmentCategoryEnum.CostDocument.getValue(), attachmentDTO.getFileName(), utcCreatedDate.toString(), attachmentDTO.getDocumentId()));
        }
        return attachmentItemRowDtos;
    }

    public QuoteOptionDetailsDTO buildJsonResponse(String customerId, String contractId, String projectId, String quoteOptionId, PaginatedFilter<LineItemModel> paginatedFilter,
                                                   PaginatedSort<LineItemModel> paginatedSort, String userToken, String siteCeaseItemsDescription) {
        final List<LineItemModel> models = lineItemFacade.fetchVisibleLineItems(customerId, contractId, projectId, quoteOptionId, null, true, PriceSuppressStrategy.None);

        boolean isDiscountRequested = false;
        for (LineItemModel lineItem : models) {
            if (lineItem.isDiscountApprovalRequested()) {
                isDiscountRequested = true;
                break;
            }
        }

        final List<QuoteOptionDetailsDTO.LineItem> lineItems = new ArrayList<QuoteOptionDetailsDTO.LineItem>();

        // Filtering data
        final PaginatedFilterResult<LineItemModel> paginatedFilterResult = paginatedFilter.applyTo(models);
        // Sorting data
        final PaginatedSortResult<LineItemModel> paginatedSortResult = paginatedSort.applyTo(paginatedFilterResult);

        DetailsDTOLineItemVisitor lineItemVisitor = new DetailsDTOLineItemVisitor(lineItems);
        for (LineItemModel lineItem : paginatedSortResult.getPaginatedFilterResultItems().getItems()) {
            lineItem.setUserToken(userToken);
            lineItemVisitor.visit(lineItem);
        }
        return new QuoteOptionDetailsDTO(paginatedFilterResult.getPageNumber(),
                                         paginatedFilterResult.getFilteredSize(),
                                         models.size(), lineItems, isDiscountRequested, siteCeaseItemsDescription);
    }

    public NotesView buildNoteView(List<NoteDto> notes) {
        final List<NoteDto> notDtos = constructFullNames(notes);
        return new NotesView(notDtos);
    }

    private List<NoteDto> constructFullNames(List<NoteDto> notes) {
        for (NoteDto note : notes) {
            UserDTO userDto = userFacade.findUser(note.createdBy);
            note.createdBy = String.format("%s %s", userDto.forename, userDto.surname);
        }
        return notes;
    }

    interface  QuoteOptionDetailsOrchestratorLogger {

        @Log(level = LogLevel.INFO, loggerName = "QuoteOptionDetailsOrchestratorLogger", format = "List of the attachment documents categoryId [%s], fileName [%s], uploadedDate [%s]," +
                                                                                                   " and documentId [%s]")
        void logAttachmentLoadResponse(String categoryId, String fileName, String uploadedDate, String documentId);

    }
}
