package com.bt.rsqe.ape;

import com.bt.rsqe.EmailService;
import com.bt.rsqe.ape.config.LocalIdentifier;
import com.bt.rsqe.ape.dto.ApeQref;
import com.bt.rsqe.ape.dto.ApeQrefPrices;
import com.bt.rsqe.ape.dto.ApeQrefProductConfiguration;
import com.bt.rsqe.ape.dto.ApeQrefProjectDetail;
import com.bt.rsqe.ape.dto.ApeQrefSiteDetails;
import com.bt.rsqe.ape.dto.ApeQrefUpdate;
import com.bt.rsqe.ape.repository.APEQrefRepository;
import com.bt.rsqe.ape.repository.entities.AccessStaffCommentEntity;
import com.bt.rsqe.ape.repository.entities.ApeRequestEntity;
import com.bt.rsqe.ape.workflow.AccessWorkflowStatus;
import com.bt.rsqe.ape.workflow.TransitionName;
import com.bt.rsqe.customerrecord.UserResource;
import com.bt.rsqe.rest.ResponseBuilder;
import com.bt.rsqe.security.UserDTO;
import com.bt.rsqe.web.ClasspathConfiguration;
import com.bt.rsqe.web.rest.exception.ResourceNotFoundException;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import javax.mail.MessagingException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bt.rsqe.ape.workflow.AccessWorkflowStatus.*;
import static com.bt.rsqe.utils.AssertObject.*;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.*;
import static org.apache.commons.lang.StringUtils.*;

@Path("/rsqe/ape-facade/email")
public class ApeEmailHandler extends ApeHandler {
    private static final String UPDATE_TEMPLATE = "com/bt/rsqe/ape/qref-update-email-temp.ftl";
    private static final String REJECT_TEMPLATE = "com/bt/rsqe/ape/qref-reject-email-temp.ftl";
    private static final String INITIAL_SYNC_UP_NOTIFICATION_TEMPLATE = "com/bt/rsqe/ape/qref-intial-syncup-response-email.ftl";
    private static final String QREF_UPDATE_SUBJECT = "%s - APE Manual Workflow - %s - %s - %s - %s %s";

    private EmailService emailService;
    private APEQrefRepository apeQrefRepository;
    private UserResource userResource;

    public ApeEmailHandler(EmailService emailService,
                           APEQrefRepository apeQrefRepository,
                           UserResource userResource) {
        this.emailService = emailService;
        this.apeQrefRepository = apeQrefRepository;
        this.userResource = userResource;
    }

    @POST
    @Path("qref-update")
    public Response sendQrefUpdateEmail(ApeQrefUpdate apeQrefUpdate) {
        ApeQref apeQref = apeQrefRepository.getApeQref(apeQrefUpdate.getQrefStencilId().getValue());
        QrefAttributeExtractor attributeExtractor = new QrefAttributeExtractor(apeQref);

        if (!transitionAllowed(TransitionName.SEND_QREF_UPDATE_EMAIL, attributeExtractor)) {
            return invalidWorkflowResponse();
        }

        String qrefId = attributeExtractor.getAttributeValue(LocalIdentifier.QREF);
        AccessWorkflowStatus workflowStatus = getWorkflowStatus(attributeExtractor);
        ApeRequestEntity apeRequestEntity = apeQrefRepository.getAPERequestByRequestId(apeQref.getRequestId());

        try {
                String bodyMessage = constructMessageBody(apeQrefUpdate, REJECTED.equals(workflowStatus));
                UserDTO userDTO = userResource.findUser(apeRequestEntity.getUserLogin());
                String subject = constructMessageSubject(apeQrefUpdate, qrefId, workflowStatus);
                emailService.sendEmail(EmailService.DEFAULT_FROM_MAIL_ACCOUNT,
                        subject,
                        bodyMessage,
                        userDTO.getEmail());
        } catch (MessagingException e) {
            return ResponseBuilder.internalServerError().withEntity(e.getMessage()).build();
        } catch (ResourceNotFoundException e) {
            return ResponseBuilder.notFound().withEntity(e.getMessage()).build();
        } catch (IOException e) {
            return ResponseBuilder.internalServerError().withEntity(e.getMessage()).build();
        } catch (TemplateException e) {
            return ResponseBuilder.internalServerError().withEntity(e.getMessage()).build();
        }

        return ResponseBuilder.anOKResponse().build();
    }

    @POST
    @Path("qref-initial-syncup-response")
    public Response sendQrefInitialSyncUpResponse(ApeQrefProjectDetail apeQrefProjectDetail) {
        try {
            ApeRequestEntity apeRequestEntity = apeQrefRepository.getAPERequestByUniqueId(apeQrefProjectDetail.uniqueId);

            List<String> apeQrefId = apeQrefRepository.getApeQrefId(apeRequestEntity.getRequestId());
            StringBuffer buffer = new StringBuffer("");
            if (apeQrefId != null && !apeQrefId.isEmpty()) {
                List<AccessStaffCommentEntity> staffComments = apeQrefRepository.getStaffComments(apeQrefId.get(0));
                if (staffComments != null && !staffComments.isEmpty()) {
                    List<String> staffCommentList = newArrayList(Collections2.transform(staffComments, new Function<AccessStaffCommentEntity, String>() {
                        @Nullable
                        @Override
                        public String apply(AccessStaffCommentEntity input) {
                            return input.getComment();
                        }
                    }));
                    for (String comment : staffCommentList) {
                        buffer.append(comment).append(";");
                    }
                }

            }
            if (!apeRequestEntity.isSimulatedRequest()) {
                UserDTO userDTO = userResource.findUser(apeRequestEntity.getUserLogin());

                Template emailTemplate = new ClasspathConfiguration().getTemplate(INITIAL_SYNC_UP_NOTIFICATION_TEMPLATE);

                Map<String, String> rootMap = new HashMap<String, String>();
                rootMap.put("salesUser", String.format("%s %s", userDTO.getForename(), userDTO.getSurname()));
                rootMap.put("customerName", apeQrefProjectDetail.customerName);
                rootMap.put("salesChannel", apeQrefProjectDetail.salesChannel);
                rootMap.put("quoteName", apeQrefProjectDetail.quoteName);
                rootMap.put("quoteRefId", apeQrefProjectDetail.projectId);
                rootMap.put("quoteVersion", apeQrefProjectDetail.quoteVersion);
                rootMap.put("quoteType", apeQrefProjectDetail.orderType);
                rootMap.put("quoteContractTerm", apeQrefProjectDetail.contractTerm);
                rootMap.put("quoteCurrency", apeQrefProjectDetail.currency);
                rootMap.put("siteId", apeQrefProjectDetail.siteId);
                rootMap.put("siteName", apeQrefProjectDetail.siteName);
                rootMap.put("requestId", apeRequestEntity.getRequestId());
                rootMap.put("staffCommentsFromApe", buffer.toString());

                String errorMessage = apeRequestEntity.getErrorMessage();
                rootMap.put("syncUpStatus", "Completed");
                rootMap.put("errorDesc", isNotEmpty(errorMessage) ? errorMessage : "");

                Writer out = new StringWriter();
                emailTemplate.process(rootMap, out);
                String bodyMessage = out.toString();

                String subject = String.format("Access pricing request for [Quote Name: %s ] under [Customer Name: %s ]", apeQrefProjectDetail.quoteName,
                        apeQrefProjectDetail.customerName);

                emailService.sendEmail(EmailService.DEFAULT_FROM_MAIL_ACCOUNT, subject, bodyMessage, userDTO.getEmail());
            }

        } catch (MessagingException e) {
            return ResponseBuilder.internalServerError().withEntity(e.getMessage()).build();
        } catch (ResourceNotFoundException e) {
            return ResponseBuilder.notFound().withEntity(e.getMessage()).build();
        } catch (IOException e) {
            return ResponseBuilder.internalServerError().withEntity(e.getMessage()).build();
        } catch (TemplateException e) {
            return ResponseBuilder.internalServerError().withEntity(e.getMessage()).build();
        }

        return ResponseBuilder.anOKResponse().build();
    }


    private String constructMessageSubject(ApeQrefUpdate qrefUpdate, String qrefId, AccessWorkflowStatus workflowStatus) {
        List<AccessStaffCommentEntity> staffComments = apeQrefRepository.getStaffComments(qrefUpdate.getQrefStencilId().getValue());
        String country = qrefUpdate.getApeQrefSiteDetails().getCountry();
        String city = qrefUpdate.getApeQrefSiteDetails().getCity();
        String speed = qrefUpdate.getApeQrefProductConfiguration().getSpeed();
        String staffName = staffComments.isEmpty() ? StringUtils.EMPTY : staffComments.get(0).getStaffName();

        return format(QREF_UPDATE_SUBJECT, qrefId, country, city, speed, getSubjectStatus(workflowStatus), staffName);
    }

    private String constructMessageBody(ApeQrefUpdate qrefUpdate, boolean isReject) throws IOException, TemplateException {
        ApeQrefSiteDetails siteDetails = qrefUpdate.getApeQrefSiteDetails();
        ApeQrefProductConfiguration productConfiguration = qrefUpdate.getApeQrefProductConfiguration();
        ApeQrefPrices pricingDetails = qrefUpdate.getApeQrefPrices();

        Configuration config = new ClasspathConfiguration();
        Template emailTemplate;

        if (isReject) {
            emailTemplate = config.getTemplate(REJECT_TEMPLATE);
        } else {
            emailTemplate = config.getTemplate(UPDATE_TEMPLATE);
        }

        Map<String, String> rootMap = new HashMap<String, String>();
        rootMap.put("siteName", replaceNullValues(siteDetails.getSiteName()));
        rootMap.put("streetAddress", replaceNullValues(siteDetails.getStreetAddress()));
        rootMap.put("city", replaceNullValues(siteDetails.getCity()));
        rootMap.put("state", siteDetails.getState() == null ? "" : replaceNullValues(siteDetails.getState().toString()));
        rootMap.put("postCode", replaceNullValues(siteDetails.getPostCode()));
        rootMap.put("country", replaceNullValues(siteDetails.getCountry()));
        rootMap.put("product", replaceNullValues(productConfiguration.getProduct()));
        rootMap.put("accessTechnology", replaceNullValues(productConfiguration.getAccessTechnology()));
        rootMap.put("speed", replaceNullValues(productConfiguration.getSpeed()));
        rootMap.put("supplier", replaceNullValues(productConfiguration.getSupplier()));
        rootMap.put("supplierProduct", replaceNullValues(productConfiguration.getSupplierProduct()));
        rootMap.put("workflowStatus", replaceNullValues(productConfiguration.getAccessWorkflowStatus().getDescription()));
        rootMap.put("currency", replaceNullValues(pricingDetails.getCurrency()));
        rootMap.put("installPrice", replaceNullValues(pricingDetails.getOneTimePrice()));
        rootMap.put("monthlyPrice", replaceNullValues(pricingDetails.getRecurringPrice()));
        rootMap.put("priceStatus", replaceNullValues(pricingDetails.getStatus()));

        Writer out = new StringWriter();
        emailTemplate.process(rootMap, out);

        return out.toString();
    }

    private String replaceNullValues(String apeValue) {
        return isNull(apeValue) ? "" : apeValue;
    }

    private String getSubjectStatus(AccessWorkflowStatus accessWorkflowStatus) {
        switch (accessWorkflowStatus) {
            case REJECTED:
                return "Rejected by";
            case COMPLETED:
                return "is completed by";
            default:
                return "has been assigned to";
        }
    }
}
