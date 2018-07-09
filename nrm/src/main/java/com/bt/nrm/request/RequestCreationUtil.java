package com.bt.nrm.request;

import com.bt.nrm.dto.request.NonStandardRequestAttributeDTO;
import com.bt.nrm.dto.request.NonStandardRequestDTO;
import com.bt.nrm.dto.request.NonStandardRequestSiteDTO;
import com.bt.nrm.dto.request.NonStandardRequestSiteDetailsDTO;
import com.bt.nrm.dto.request.ProductDTO;
import com.bt.nrm.dto.request.QuoteDTO;
import com.bt.nrm.dto.request.UserDTO;
import com.bt.nrm.repository.QuoteOptionRequestRepository;
import com.bt.nrm.repository.entity.QuoteEntity;
import com.bt.nrm.repository.entity.RequestAttributeEntity;
import com.bt.nrm.repository.entity.RequestEntity;
import com.bt.nrm.repository.entity.RequestEvaluatorEntity;
import com.bt.nrm.repository.entity.RequestEvaluatorPriceGroupEntity;
import com.bt.nrm.repository.entity.RequestEvaluatorSiteEntity;
import com.bt.nrm.repository.entity.RequestHistoryEntity;
import com.bt.nrm.repository.entity.RequestPriceGroupEntity;
import com.bt.nrm.repository.entity.RequestSiteEntity;
import com.bt.nrm.util.Constants;
import com.bt.nrm.util.GeneralUtil;
import com.bt.pms.dto.AttributeDTO;
import com.bt.pms.dto.EvaluatorGroupDTO;
import com.bt.pms.dto.PriceGroupDTO;
import com.bt.pms.dto.TemplateDTO;
import com.bt.pms.resources.PMSResource;
import com.bt.rsqe.web.ClasspathConfiguration;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bt.rsqe.utils.AssertObject.*;
import static java.lang.String.*;

public class RequestCreationUtil {
    private QuoteOptionRequestRepository quoteOptionRequestRepository;
    public PMSResource pmsResource;
    private RequestEntity requestEntity;

    public RequestCreationUtil(QuoteOptionRequestRepository quoteOptionRequestRepository, PMSResource pmsResource){
        this.quoteOptionRequestRepository = quoteOptionRequestRepository;
        this.pmsResource = pmsResource;
        this.requestEntity = new RequestEntity();
    }

    public RequestEntity constructNRMRequestEntity(NonStandardRequestDTO requestDTO, TemplateDTO templateDTO) {
        //Call sequence to get latest request Id and append it with source name(appending SQE as of now. Can be changed further to identify source(SQE/rSQE))
        requestEntity.setRequestId("SQE-" + quoteOptionRequestRepository.getRequestIdFromSequence());
        requestEntity.setRequestName(requestDTO.getRequestName());
        // Check if the Quote is already present. In case of sql, QuoteId and QuoteOptionId both stores quoteId. In case of rSQE, both QuoteId and QuoteOptionId both stores separate values.
        // Query will always use QuoteOptionId as a generic approach.
        QuoteEntity quoteEntity = quoteOptionRequestRepository.getQuoteByQuoteOptionId(requestDTO.getQuote().getQuoteOptionId());
        if (isNotNull(quoteEntity)) {
            requestEntity.setQuote(quoteEntity);
        } else {
            requestEntity.setQuote(new QuoteEntity(null, requestDTO.getQuote().getQuoteId(), requestDTO.getQuote().getQuoteName(), requestDTO.getQuote().getQuoteOptionId(),
                    requestDTO.getQuote().getQuoteOptionName(), requestDTO.getQuote().getCustomerId(), requestDTO.getQuote().getCustomerName(), requestDTO.getQuote().getCustomerMoreInfo(),
                    requestDTO.getQuote().getSourceSystem(), requestDTO.getQuote().getSalesChannelId(), requestDTO.getQuote().getSalesChannelName(), requestDTO.getQuote().getSalesChannelType(),
                    requestDTO.getQuote().getQuoteCurrency(), requestDTO.getQuote().getTier(), requestDTO.getQuote().getContractValue(), requestDTO.getQuote().getContractLength(),
                    requestDTO.getQuote().getAlternateContact(), requestDTO.getQuote().getWinChance(), requestDTO.getQuote().getCreatedByEIN(), requestDTO.getQuote().getCreatedByEmailId(),
                    requestDTO.getQuote().getCreatedByUserName(), GeneralUtil.getCurrentTimeStamp(), GeneralUtil.getCurrentTimeStamp(),
                    requestDTO.getUser().getUserFirstName() + " " + requestDTO.getUser().getUserLastName()));
        }

        requestEntity.setProductCategoryCode(requestDTO.getProduct().getProductCategoryCode());
        requestEntity.setProductCategoryName(requestDTO.getProduct().getProductCategoryName());
        requestEntity.setTemplateCode(requestDTO.getTemplateCode());
        requestEntity.setTemplateName(templateDTO.getTemplateName());
        requestEntity.setTemplateVersion(requestDTO.getTemplateVersion());
        if(isNotNull(templateDTO.getWorkflowType()) && isNotNull(templateDTO.getWorkflowType().getWorkflowName())) {
            requestEntity.setWorkFlowType(templateDTO.getWorkflowType().getWorkflowName());
            String outputDescription = null;
            if (templateDTO.getWorkflowType().getWorkflowName().equalsIgnoreCase(Constants.templateWorkFlows.get("evaluated")) && isNotNull(templateDTO.getWorkflowType().getEvaluated())) {
                if(isNotNull(templateDTO.getWorkflowType().getEvaluated().getMode())) {
                    requestEntity.setEvaluatedMode(templateDTO.getWorkflowType().getEvaluated().getMode().getModeName());
                }
                requestEntity.setResponseType(Constants.requestResponseType.get("DEFAULT"));
            }else if (templateDTO.getWorkflowType().getWorkflowName().equalsIgnoreCase(Constants.templateWorkFlows.get("fastTrack")) && isNotNull(templateDTO.getWorkflowType().getFastTrack())) {
                outputDescription = templateDTO.getWorkflowType().getFastTrack().getDefaultOutputDescription();
                requestEntity.setResponseType(Constants.requestResponseType.get("DEFAULT"));
            } else if (templateDTO.getWorkflowType().getWorkflowName().equalsIgnoreCase(Constants.templateWorkFlows.get("zeroTouch")) && isNotNull(templateDTO.getWorkflowType().getZeroTouch())) {
                outputDescription = templateDTO.getWorkflowType().getZeroTouch().getPredefinedOutputDescription();
                requestEntity.setResponseType(templateDTO.getWorkflowType().getZeroTouch().getResponseStatus());
            }
            requestEntity.setOutputDescription(outputDescription);
        }
        requestEntity.setConfigurationType(templateDTO.getConfigurationType());
        requestEntity.setIsReusable(templateDTO.getReusable().equalsIgnoreCase("true") ? Constants.YES : Constants.NO);
        requestEntity.setSpecialBidCategory(templateDTO.getSpecialBidCategory());
        requestEntity.setUSTaxCategory(templateDTO.getUsTaxCategory());
        requestEntity.setDataBuildRequired(templateDTO.getDataBuildRequired() ? Constants.YES : Constants.NO);
        requestEntity.setDataBuildCompleted(Constants.NO);
        //TODO Only add working days. Ignore weekends.
        requestEntity.setExpectedResponseTime(GeneralUtil.formatDate(GeneralUtil.addDaysToDate(new Date(), templateDTO.getTurnAroundTime())));
        requestEntity.setState(Constants.requestStateConstants.get("issued"));
        //TODO Yet to analyze how to create public URLs
        //requestEntity.setPublicURL();
        //TODO Find out how to create attachment URL for request
        //requestEntity.setAttachments();
        requestEntity.setBillDescription(templateDTO.getBillDescription());
        requestEntity.setDetailedResponse((isNotNull(templateDTO.getNegativeResponseDefaultText()) ? templateDTO.getNegativeResponseDefaultText() : "") + "" + (isNotNull(templateDTO.getPositiveResponseDefaultText()) ? templateDTO.getPositiveResponseDefaultText() : ""));
        requestEntity.setIsAlertOnApprovalOnly(templateDTO.getIsAlertOnApprovalOnly().equalsIgnoreCase("true") ? Constants.YES : Constants.NO);
        requestEntity.setAlertEmailAddress(templateDTO.getAlertEmailAddress());
        requestEntity.setAlertMessage(templateDTO.getAlertMessage());
        requestEntity.setIsVPNDataRequired(templateDTO.getVPNDataRequired().equalsIgnoreCase("true") ? Constants.YES : Constants.NO);
        requestEntity.setIsCOTCVisible(templateDTO.getCOTCVisible().equalsIgnoreCase("true") ? Constants.YES : Constants.NO);

        populateRequestSitesAndEvaluators(requestDTO, templateDTO);
        populateCommonNResponseAttributes(requestDTO, templateDTO);
        populateRequestHistoryValues(requestDTO, templateDTO);

        requestEntity.setCreatedDate(GeneralUtil.getCurrentTimeStamp());
        requestEntity.setCreatedUser(requestDTO.getUser().getUserFirstName() + " " + requestDTO.getUser().getUserLastName());
        requestEntity.setModifiedDate(GeneralUtil.getCurrentTimeStamp());
        requestEntity.setModifiedUser(requestDTO.getUser().getUserFirstName() + " " + requestDTO.getUser().getUserLastName());

        return requestEntity;
    }

    private void populateRequestSitesAndEvaluators(NonStandardRequestDTO requestDTO, TemplateDTO templateDTO) {

        String masterEvaluatorGroupIds = "";
        List<EvaluatorGroupDTO> evaluatorDTOs = new ArrayList<>();

        if (isNotNull(templateDTO.getWorkflowType()) && isNotNull(templateDTO.getWorkflowType().getEvaluated()) && isNotNull(templateDTO.getWorkflowType().getEvaluated().getMode())) {

            for (EvaluatorGroupDTO evaluatorGroup : templateDTO.getWorkflowType().getEvaluated().getMode().getEvaluatorGroups()) {
                if (isNotNull(evaluatorGroup.getEvaluatorGroupId())) {
                    if(evaluatorGroup.getIsEvaluatorGroupMaster().equalsIgnoreCase(Constants.YES.toString())) {
                        if(isNull(masterEvaluatorGroupIds) || masterEvaluatorGroupIds.length() == 0){
                            masterEvaluatorGroupIds = evaluatorGroup.getEvaluatorGroupId();
                        }else {
                            masterEvaluatorGroupIds = masterEvaluatorGroupIds + "," + evaluatorGroup.getEvaluatorGroupId();
                        }
                    }else{
                        //For all Child Evaluator Groups in template, retain them as it is
                        evaluatorDTOs.add(evaluatorGroup);
                    }
                }
            }
            if(masterEvaluatorGroupIds.length() > 0) {
                //For all Master Evaluator Groups in template, fetch relevant children from PMS - use country as filter
                evaluatorDTOs = pmsResource.getEvaluatorMappings(masterEvaluatorGroupIds, requestDTO.getSites().get(0).getCountryISOCode());
            }
        }

        List<RequestSiteEntity> sites = new ArrayList<>();

        for(NonStandardRequestSiteDTO site : requestDTO.getSites()) {

            RequestSiteEntity siteEntity = new RequestSiteEntity();
            siteEntity.setRequestEntity(requestEntity);
            siteEntity.setSiteId(site.getSiteId());
            siteEntity.setSiteName(site.getSiteName());
            siteEntity.setCountryName(site.getCountryName());
            siteEntity.setCountryISOAlpha2Code(site.getCountryISOCode());
            List<RequestAttributeEntity> siteAttributes = new ArrayList<>();

            // For current site fill primary attributes
            for (NonStandardRequestAttributeDTO requestPrimaryAttribute : site.getPrimaryDetails().getAttributes()) {
                for (AttributeDTO templatePrimaryAttribute : templateDTO.getPrimaryDetails().getAttributes()) {
                    if (templatePrimaryAttribute.getAttributeCode().equalsIgnoreCase(requestPrimaryAttribute.getAttributeCode()) ||
                            templatePrimaryAttribute.getAttributeId().equalsIgnoreCase(requestPrimaryAttribute.getAttributeId())) {

                        siteAttributes.add(new RequestAttributeEntity(null, requestEntity, siteEntity, site.getPrimaryDetails().getUniqueIdentifier(), templatePrimaryAttribute.getAttributeName(), templatePrimaryAttribute.getDefaultValue(),
                                templatePrimaryAttribute.getAttributeValueHeader(), requestPrimaryAttribute.getAttributeValue(), templatePrimaryAttribute.getAttributeValueDisplayName()
                                , templatePrimaryAttribute.getAttributeValueDisplayIndex(), templatePrimaryAttribute.getControllerType(), templatePrimaryAttribute.getIsRequired().equalsIgnoreCase(Constants.YES.toString()) ? Constants.YES : Constants.NO,
                                templatePrimaryAttribute.getDataType(), templatePrimaryAttribute.getDisplayName(), templatePrimaryAttribute.getDisplayIndex(), templatePrimaryAttribute.getTooltip(),
                                templatePrimaryAttribute.getMinimumLength(), templatePrimaryAttribute.getMaximumLength(), templatePrimaryAttribute.getMinimumDataValue(), templatePrimaryAttribute.getMaximumDataValue(),
                                templatePrimaryAttribute.getFreedomTextColumns(), templatePrimaryAttribute.getFreedomTextRows(), Constants.TEMPLATE_PLACEHOLDER_PRIMARY,
                                GeneralUtil.getCurrentTimeStamp(), requestDTO.getUser().getUserFirstName() + " " + requestDTO.getUser().getUserLastName(), GeneralUtil.getCurrentTimeStamp()
                                , requestDTO.getUser().getUserFirstName() + " " + requestDTO.getUser().getUserLastName()));
                    }
                }
            }

            // For current site fill secondary attributes
            for (NonStandardRequestAttributeDTO requestSecondaryAttribute : site.getSecondaryDetails().getAttributes()) {
                for (AttributeDTO templateSecondaryAttribute : templateDTO.getSecondaryDetails().getAttributes()) {
                    if (templateSecondaryAttribute.getAttributeCode().equalsIgnoreCase(requestSecondaryAttribute.getAttributeCode()) ||
                            templateSecondaryAttribute.getAttributeId().equalsIgnoreCase(requestSecondaryAttribute.getAttributeId())) {

                        siteAttributes.add(new RequestAttributeEntity(null, requestEntity, siteEntity, site.getSecondaryDetails().getUniqueIdentifier(), templateSecondaryAttribute.getAttributeName(), templateSecondaryAttribute.getDefaultValue(),
                                templateSecondaryAttribute.getAttributeValueHeader(), requestSecondaryAttribute.getAttributeValue(), templateSecondaryAttribute.getAttributeValueDisplayName()
                                , templateSecondaryAttribute.getAttributeValueDisplayIndex(), templateSecondaryAttribute.getControllerType(), templateSecondaryAttribute.getIsRequired().equalsIgnoreCase(Constants.YES.toString()) ? Constants.YES : Constants.NO,
                                templateSecondaryAttribute.getDataType(), templateSecondaryAttribute.getDisplayName(), templateSecondaryAttribute.getDisplayIndex(), templateSecondaryAttribute.getTooltip(),
                                templateSecondaryAttribute.getMinimumLength(), templateSecondaryAttribute.getMaximumLength(), templateSecondaryAttribute.getMinimumDataValue(), templateSecondaryAttribute.getMaximumDataValue(),
                                templateSecondaryAttribute.getFreedomTextColumns(), templateSecondaryAttribute.getFreedomTextRows(), Constants.TEMPLATE_PLACEHOLDER_SECONDARY,
                                GeneralUtil.getCurrentTimeStamp(), requestDTO.getUser().getUserFirstName() + " " + requestDTO.getUser().getUserLastName(), GeneralUtil.getCurrentTimeStamp()
                                , requestDTO.getUser().getUserFirstName() + " " + requestDTO.getUser().getUserLastName()));
                    }
                }
            }

            //For current site fill primary price groups for request and evaluator
            List<RequestPriceGroupEntity> sitePriceGroups = new ArrayList<>();
            for (PriceGroupDTO templatePrimaryPriceGroup : templateDTO.getPrimaryDetails().getPriceGroups()) {
                sitePriceGroups.add(new RequestPriceGroupEntity(null, siteEntity, site.getPrimaryDetails().getUniqueIdentifier(), Constants.TEMPLATE_PLACEHOLDER_PRIMARY, templatePrimaryPriceGroup.getPriceGroupDescription(),
                        templatePrimaryPriceGroup.getOneOffRecommendedRetail(), templatePrimaryPriceGroup.getRecurringRecommendedRetail(), templatePrimaryPriceGroup.getNrcPriceToPartner(),
                        templatePrimaryPriceGroup.getRcPriceToPartner(), templatePrimaryPriceGroup.getOneOffCost(), templatePrimaryPriceGroup.getRecurringCost(), GeneralUtil.getCurrentTimeStamp(),
                        requestDTO.getUser().getUserFirstName() + " " + requestDTO.getUser().getUserLastName(), GeneralUtil.getCurrentTimeStamp()
                        , requestDTO.getUser().getUserFirstName() + " " + requestDTO.getUser().getUserLastName()));
            }

            //For current site fill secondary price groups for request and evaluator
            for (PriceGroupDTO templateSecondaryPriceGroup : templateDTO.getSecondaryDetails().getPriceGroups()) {
                sitePriceGroups.add(new RequestPriceGroupEntity(null, siteEntity, site.getSecondaryDetails().getUniqueIdentifier(), Constants.TEMPLATE_PLACEHOLDER_SECONDARY, templateSecondaryPriceGroup.getPriceGroupDescription(),
                        templateSecondaryPriceGroup.getOneOffRecommendedRetail(), templateSecondaryPriceGroup.getRecurringRecommendedRetail(), templateSecondaryPriceGroup.getNrcPriceToPartner(),
                        templateSecondaryPriceGroup.getRcPriceToPartner(), templateSecondaryPriceGroup.getOneOffCost(), templateSecondaryPriceGroup.getRecurringCost(), GeneralUtil.getCurrentTimeStamp(),
                        requestDTO.getUser().getUserFirstName() + " " + requestDTO.getUser().getUserLastName(), GeneralUtil.getCurrentTimeStamp()
                        , requestDTO.getUser().getUserFirstName() + " " + requestDTO.getUser().getUserLastName()));
            }

            siteEntity.setSiteAttributes(siteAttributes);
            siteEntity.setPriceGroups(sitePriceGroups);
            siteEntity.setCreatedUser(requestDTO.getUser().getUserFirstName() + " " + requestDTO.getUser().getUserLastName());
            siteEntity.setCreatedDate(GeneralUtil.getCurrentTimeStamp());
            siteEntity.setModifiedDate(GeneralUtil.getCurrentTimeStamp());
            siteEntity.setModifiedUser(requestDTO.getUser().getUserFirstName() + " " + requestDTO.getUser().getUserLastName());
            sites.add(siteEntity);
        }

        requestEntity.setRequestSites(sites);

        List<RequestEvaluatorEntity> evaluatorEntities = new ArrayList<>();

        //Construct RequestEvaluatorEntities
        for(EvaluatorGroupDTO evaluatorDTO : evaluatorDTOs){

            RequestEvaluatorEntity evaluatorEntity = new RequestEvaluatorEntity(null, requestEntity, evaluatorDTO.getEvaluatorGroupId(), evaluatorDTO.getName(), null,
                                                                                Constants.requestEvaluatorStateConstants.get("requestEvaluatorState_created"), Constants.requestEvaluatorResponseConstants.get("requestEvaluatorResponse_none"), null, null, null, null, null, null
                , GeneralUtil.getCurrentTimeStamp(), requestDTO.getUser().getUserEIN(), requestDTO.getUser().getUserFirstName() + " " + requestDTO.getUser().getUserLastName(), GeneralUtil.getCurrentTimeStamp()
                , requestDTO.getUser().getUserEIN(), requestDTO.getUser().getUserFirstName() + " " + requestDTO.getUser().getUserLastName(), "[]", null, requestDTO.getQuote().getQuoteCurrency());


            //For every site, fill price groups for current Evaluator
            List<RequestEvaluatorSiteEntity> requestEvaluatorSiteEntities = new ArrayList<>();
            for(RequestSiteEntity siteEntity : requestEntity.getRequestSites()) {
                RequestEvaluatorSiteEntity requestEvaluatorSiteEntity = new RequestEvaluatorSiteEntity();
                requestEvaluatorSiteEntity.setRequestEvaluatorEntity(evaluatorEntity);
                requestEvaluatorSiteEntity.setSiteId(siteEntity.getSiteId());
                requestEvaluatorSiteEntity.setSiteName(siteEntity.getSiteName());
                requestEvaluatorSiteEntity.setCountryName(siteEntity.getCountryName());
                requestEvaluatorSiteEntity.setCountryISOAlpha2Code(siteEntity.getCountryISOAlpha2Code());

                List<RequestEvaluatorPriceGroupEntity> evaluatorPriceGroups = new ArrayList<>();

                for (RequestPriceGroupEntity requestPriceGroupEntity : siteEntity.getPriceGroups()) {

                    evaluatorPriceGroups.add(new RequestEvaluatorPriceGroupEntity(null, requestEvaluatorSiteEntity, requestPriceGroupEntity.getPriceGroupType(),
                            requestPriceGroupEntity.getPriceGroupDescription(), requestPriceGroupEntity.getOneOffRecommendedRetail(), requestPriceGroupEntity.getRecurringRecommendedRetail(),
                            requestPriceGroupEntity.getNrcPriceToPartner(), requestPriceGroupEntity.getRcPriceToPartner(), requestPriceGroupEntity.getOneOffCost(),
                            requestPriceGroupEntity.getRecurringCost(), GeneralUtil.getCurrentTimeStamp(), requestDTO.getUser().getUserFirstName() + " " + requestDTO.getUser().getUserLastName(),
                            GeneralUtil.getCurrentTimeStamp() , requestDTO.getUser().getUserFirstName() + " " + requestDTO.getUser().getUserLastName()));
                }
                requestEvaluatorSiteEntity.setRequestEvaluatorPriceGroups(evaluatorPriceGroups);
                requestEvaluatorSiteEntity.setCreatedUser(requestDTO.getUser().getUserFirstName() + " " + requestDTO.getUser().getUserLastName());
                requestEvaluatorSiteEntity.setCreatedDate(GeneralUtil.getCurrentTimeStamp());
                requestEvaluatorSiteEntity.setModifiedDate(GeneralUtil.getCurrentTimeStamp());
                requestEvaluatorSiteEntity.setModifiedUser(requestDTO.getUser().getUserFirstName() + " " + requestDTO.getUser().getUserLastName());
                requestEvaluatorSiteEntities.add(requestEvaluatorSiteEntity);
            }

            evaluatorEntity.setRequestEvaluatorSites(requestEvaluatorSiteEntities);
            evaluatorEntities.add(evaluatorEntity);
        }
        requestEntity.setRequestEvaluators(evaluatorEntities);
    }

    private void populateCommonNResponseAttributes(NonStandardRequestDTO requestDTO, TemplateDTO templateDTO) {

        List<RequestAttributeEntity> commonNResponseAttributes = new ArrayList<>();

        //Fill common attributes
        for(NonStandardRequestAttributeDTO requestCommonAttribute : requestDTO.getCommonDetails()){
            for(AttributeDTO templateCommonAttribute : templateDTO.getCommonDetails()) {
                if (templateCommonAttribute.getAttributeCode().equalsIgnoreCase(requestCommonAttribute.getAttributeCode()) ||
                        templateCommonAttribute.getAttributeId().equalsIgnoreCase(requestCommonAttribute.getAttributeId())) {
                    commonNResponseAttributes.add(new RequestAttributeEntity(null, requestEntity, null, null, templateCommonAttribute.getAttributeName(), templateCommonAttribute.getDefaultValue(),
                            templateCommonAttribute.getAttributeValueHeader(), templateCommonAttribute.getAttributeValue(), templateCommonAttribute.getAttributeValueDisplayName()
                            , templateCommonAttribute.getAttributeValueDisplayIndex(), templateCommonAttribute.getControllerType(), templateCommonAttribute.getIsRequired().equalsIgnoreCase(Constants.YES.toString()) ? Constants.YES : Constants.NO,
                            templateCommonAttribute.getDataType(), templateCommonAttribute.getDisplayName(), templateCommonAttribute.getDisplayIndex(), templateCommonAttribute.getTooltip(),
                            templateCommonAttribute.getMinimumLength(), templateCommonAttribute.getMaximumLength(), templateCommonAttribute.getMinimumDataValue(), templateCommonAttribute.getMaximumDataValue(),
                            templateCommonAttribute.getFreedomTextColumns(), templateCommonAttribute.getFreedomTextRows(), Constants.TEMPLATE_PLACEHOLDER_COMMON,
                            GeneralUtil.getCurrentTimeStamp(), requestDTO.getUser().getUserFirstName() + " " + requestDTO.getUser().getUserLastName(), GeneralUtil.getCurrentTimeStamp()
                            , requestDTO.getUser().getUserFirstName() + " " + requestDTO.getUser().getUserLastName()));
                }
            }
        }

        //Fill Response attributes
        for(AttributeDTO templateCommonAttribute : templateDTO.getResponseDetails()) {
            commonNResponseAttributes.add(new RequestAttributeEntity(null, requestEntity, null, null, templateCommonAttribute.getAttributeName(), templateCommonAttribute.getDefaultValue(),
                    templateCommonAttribute.getAttributeValueHeader(), templateCommonAttribute.getAttributeValue(), templateCommonAttribute.getAttributeValueDisplayName()
                    , templateCommonAttribute.getAttributeValueDisplayIndex(), templateCommonAttribute.getControllerType(), templateCommonAttribute.getIsRequired().equalsIgnoreCase(Constants.YES.toString()) ? Constants.YES : Constants.NO,
                    templateCommonAttribute.getDataType(), templateCommonAttribute.getDisplayName(), templateCommonAttribute.getDisplayIndex(), templateCommonAttribute.getTooltip(),
                    templateCommonAttribute.getMinimumLength(), templateCommonAttribute.getMaximumLength(), templateCommonAttribute.getMinimumDataValue(), templateCommonAttribute.getMaximumDataValue(),
                    templateCommonAttribute.getFreedomTextColumns(), templateCommonAttribute.getFreedomTextRows(), Constants.TEMPLATE_PLACEHOLDER_RESPONSE,
                    GeneralUtil.getCurrentTimeStamp(), requestDTO.getUser().getUserFirstName() + " " + requestDTO.getUser().getUserLastName(), GeneralUtil.getCurrentTimeStamp()
                    , requestDTO.getUser().getUserFirstName() + " " + requestDTO.getUser().getUserLastName()));
        }

        requestEntity.setCommonNResponseAttributes(commonNResponseAttributes);
    }

    private void populateRequestHistoryValues(NonStandardRequestDTO requestDTO, TemplateDTO templateDTO) {

        List<RequestHistoryEntity> requestHistoryEntities = new ArrayList<>();

        if (templateDTO.getWorkflowType().getWorkflowName().equalsIgnoreCase(Constants.templateWorkFlows.get("evaluated")) || templateDTO.getWorkflowType().getWorkflowName().equalsIgnoreCase(Constants.templateWorkFlows.get("fastTrack"))) {

            requestHistoryEntities.add(new RequestHistoryEntity(null, requestEntity, Constants.requestStateConstants.get("issued"), requestDTO.getUser().getUserEIN()
                    ,requestDTO.getUser().getUserFirstName() + " " + requestDTO.getUser().getUserLastName(), GeneralUtil.getCurrentTimeStamp()));

        }else if (templateDTO.getWorkflowType().getWorkflowName().equalsIgnoreCase(Constants.templateWorkFlows.get("zeroTouch"))) {

            requestHistoryEntities.add(new RequestHistoryEntity(null, requestEntity, Constants.requestStateConstants.get("signedOff"), requestDTO.getUser().getUserEIN()
                    ,requestDTO.getUser().getUserFirstName() + " " + requestDTO.getUser().getUserLastName(), GeneralUtil.getCurrentTimeStamp()));

        }
        requestEntity.setRequestHistoryLogs(requestHistoryEntities);
    }

    public NonStandardRequestDTO getTestRequestData(){
        //Fetch template to set test data
        TemplateDTO templateDetails = pmsResource.getCompleteTemplateDetails("SB00121", "V1.1");

        //Common Attr
        List<NonStandardRequestAttributeDTO> commonAttrs = new ArrayList<>();
        for(AttributeDTO commonAttribute : templateDetails.getCommonDetails()){
            String attrVal = isNotNull(commonAttribute.getAttributeValueLOV()) || commonAttribute.getAttributeValueLOV().size() <= 0 ? "Test Attr Val" : commonAttribute.getAttributeValueLOV().get(0);
            commonAttrs.add(new NonStandardRequestAttributeDTO(commonAttribute.getAttributeId(), commonAttribute.getAttributeCode(), commonAttribute.getAttributeName(),
                    attrVal));
        }

        //Primary Attr
        NonStandardRequestSiteDetailsDTO primaryDetails = null;
        if(isNotNull(templateDetails.getPrimaryDetails())){
            primaryDetails = new NonStandardRequestSiteDetailsDTO();
            primaryDetails.setUniqueIdentifier("sqeUniqueIdentifier");
            primaryDetails.setAttributes(new ArrayList<NonStandardRequestAttributeDTO>());
        }

        for(AttributeDTO primaryAttribute : templateDetails.getPrimaryDetails().getAttributes()){
            String attrVal = (isNull(primaryAttribute.getAttributeValueLOV()) || primaryAttribute.getAttributeValueLOV().size() <= 0 )? "Test Attr Val" : primaryAttribute.getAttributeValueLOV().get(0);
            primaryDetails.getAttributes().add(new NonStandardRequestAttributeDTO(primaryAttribute.getAttributeId(), primaryAttribute.getAttributeCode(), primaryAttribute.getAttributeName(),
                    attrVal));
        }

        //Secondary Attr
        NonStandardRequestSiteDetailsDTO secondaryDetails = null;
        if(isNotNull(templateDetails.getSecondaryDetails())){
            secondaryDetails = new NonStandardRequestSiteDetailsDTO();
            secondaryDetails.setUniqueIdentifier("sqeUniqueIdentifier");
            secondaryDetails.setAttributes(new ArrayList<NonStandardRequestAttributeDTO>());
        }
        for(AttributeDTO secondaryAttribute : templateDetails.getSecondaryDetails().getAttributes()){
            String attrVal = (isNull(secondaryAttribute.getAttributeValueLOV()) || secondaryAttribute.getAttributeValueLOV().size() <= 0) ? "Test Attr Val" : secondaryAttribute.getAttributeValueLOV().get(0);
            secondaryDetails.getAttributes().add(new NonStandardRequestAttributeDTO(secondaryAttribute.getAttributeId(), secondaryAttribute.getAttributeCode(), secondaryAttribute.getAttributeName(),
                    attrVal));
        }

        List<NonStandardRequestSiteDTO> sites = new ArrayList<>();
        sites.add(new NonStandardRequestSiteDTO("SiteId1", "SiteName1", "Hungary", "HU",
                primaryDetails, secondaryDetails));

        return new NonStandardRequestDTO(
                null, "Test Request", "30/01/2016", "SB00121", "V1.1", "False", "15", "Test BidManager Name",
                new QuoteDTO("1", "1", "QuoteName", "1", "QuoteOptionName", "GBP", "12 Months", "", "90%", "rSQE", "CustomerId1", "CustomerName", "CustomerMoreInfo", "OppRefNo", "AlternateContact",
                        "SalesChannelId1", "SalesChannelName", "Direct", "100000", "USD","608143048", "mittal.2.patel@bt.com", "Mittal Patel",new Date()),
                new ProductDTO("H0301101", "Internet Connect Global", "S0319798", "CPE"),
                new UserDTO("608143048", "mittal.2.patel@bt.com", "Mittal", "Patel"),
                commonAttrs, sites);
    }

    public String constructMessageSubjectForRequestCreation(String requestId) {
        return format(Constants.REQUEST_CREATION_EMAIL_SUBJECT, requestId);
    }

    public String constructMessageBodyForRequestCreation(String requestId) throws IOException, TemplateException {
        Configuration config = new ClasspathConfiguration();

        Map<String, Object> rootMap = new HashMap<>();
        rootMap.put("messageNote", Constants.REQUEST_CREATION_SUCCESS_NOTE);
        rootMap.put("requestId", requestId);

        Template emailTemplate = config.getTemplate(Constants.REQUEST_CREATION_TEMPLATE);

        Writer out = new StringWriter();
        emailTemplate.process(rootMap, out);

        return out.toString();
    }


}



