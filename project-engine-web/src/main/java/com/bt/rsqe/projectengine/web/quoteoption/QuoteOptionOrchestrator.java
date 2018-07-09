package com.bt.rsqe.projectengine.web.quoteoption;

import com.bt.rsqe.customerrecord.CustomerDTO;
import com.bt.rsqe.projectengine.ProjectIdDTO;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.bt.rsqe.projectengine.web.facades.CustomerFacade;
import com.bt.rsqe.projectengine.web.facades.QuoteOptionFacade;
import com.bt.rsqe.projectengine.web.quoteoption.validation.QuoteOptionDependencyValidator;
import com.bt.rsqe.projectengine.web.quoteoption.validation.QuoteOptionsBillAccountCurrencyValidator;
import com.bt.rsqe.projectengine.web.uri.UriFactory;
import com.bt.rsqe.projectengine.web.uri.UriFactoryImpl;
import com.bt.rsqe.projectengine.web.view.CustomerProjectQuoteOptionsTab;
import com.bt.rsqe.security.UserContextManager;
import com.bt.rsqe.web.LocalDateTimeFormatter;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.bt.rsqe.projectengine.QuoteOptionDTOComparator.*;
import static com.google.common.collect.Sets.*;

public class QuoteOptionOrchestrator {

    private final QuoteOptionFacade quoteOptionFacade;
    private final CustomerFacade customerFacade;
    private final QuoteOptionDependencyValidator validator;
    private final UriFactory productConfiguratorUriFactory;
    private ProjectResource projectResource;

    public QuoteOptionOrchestrator(QuoteOptionFacade quoteOptionFacade, CustomerFacade customerFacade,
                                   QuoteOptionDependencyValidator validator, UriFactory productConfiguratorUriFactory, ProjectResource projectResource) {
        this.quoteOptionFacade = quoteOptionFacade;
        this.customerFacade = customerFacade;
        this.validator = validator;
        this.productConfiguratorUriFactory = productConfiguratorUriFactory;
        this.projectResource = projectResource;
    }

    public CustomerProjectQuoteOptionsTab buildResponse(String customerId, String contractId, String projectId, ProjectIdDTO projectIdDTO) {
        final CustomerDTO customerDto = customerFacade.getByToken(customerId, UserContextManager.getCurrent().getRsqeToken());

        CustomerProjectQuoteOptionsTab view = createView(customerId, contractId, projectId, customerDto, projectIdDTO);
        validateAndAddMessagesToView(customerId, projectId, view);
        addQuoteOptionRowsToView(customerId, contractId, projectId, view);

        return view;
    }

    private void validateAndAddMessagesToView(String customerId, String projectId, CustomerProjectQuoteOptionsTab view) {
        List<QuoteOptionDTO> quoteOptionDTOs = projectResource.quoteOptionResource(projectId).get();
        Set<String> messages = validator.validate(customerId, new QuoteOptionsBillAccountCurrencyValidator(getQuoteOptionCurrencies(quoteOptionDTOs)));
        view.addValidationMessages(messages);
    }

    private Set<String> getQuoteOptionCurrencies(List<QuoteOptionDTO> quoteOptionDTOs) {
        return newHashSet(Iterables.transform(quoteOptionDTOs, new Function<QuoteOptionDTO, String>() {
            @Override
            public String apply(QuoteOptionDTO input) {
                return input.getCurrency();
            }
        }));
    }



    private CustomerProjectQuoteOptionsTab createView(String customerId, String contractId, String projectId, CustomerDTO customerDto, ProjectIdDTO projectIdDTO) {
        return new CustomerProjectQuoteOptionsTab(customerDto.name, UriFactoryImpl.quoteOptionDialog(customerId, contractId, projectId).toString(), UriFactoryImpl.viewConfigurationDialog(customerId, contractId, projectId).toString()
                , productConfiguratorUriFactory, UriFactoryImpl.quoteOptionNotesDialog(customerId, contractId, projectId).toString(), UriFactoryImpl.deleteQuoteOptionUri(customerId, contractId, projectId).toString(), projectIdDTO.getProjectIdsForCustomer(),projectIdDTO.getProjectNamesForCustomer(), customerId, contractId, projectId);
    }

    private void addQuoteOptionRowsToView(String customerId, String contractId, String projectId, CustomerProjectQuoteOptionsTab view) {
        List<QuoteOptionDTO> options = quoteOptionFacade.getAll(projectId);
        Collections.sort(options, DATE_CREATED_ORDER);

        for (QuoteOptionDTO option : options) {
            view.addQuoteOption(view.new QuoteOptionRow(option.id,
                                                        option.friendlyQuoteId,
                                                        option.name,
                                                        option.currency,
                                                        new LocalDateTimeFormatter(option.creationDate).format(),
                                                        option.createdBy,
                                                        UriFactoryImpl.quoteOption(customerId, contractId, projectId, option.getId()).toString(),
                                                        UriFactoryImpl.quoteOptionBcm(customerId, contractId, projectId, option.getId()).toString(),
                                                        option.isEditAllowed,
                                                        option.hasQuoteOptionNotes,
                                                        option.ifcPending,
                                                        option.discountApprovalRequested,
                                                        option.getStatus().getDescription(),
                                                        option.migrationQuote != null ? option.migrationQuote : false, option.getDiscountStatus()));
        }
    }
}
