package com.bt.rsqe.ape;

import com.bt.rsqe.ape.config.LocalIdentifier;
import com.bt.rsqe.ape.workflow.AccessWorkflowStatus;
import com.bt.rsqe.ape.workflow.TransitionName;
import com.bt.rsqe.rest.ResponseBuilder;
import com.bt.rsqe.statemachine.StateMachine;
import com.google.common.base.Strings;

import javax.ws.rs.core.Response;

public abstract class ApeHandler {
    public static final String INVALID_WORKFLOW_STATE_ERROR = "Workflow is in an invalid state";
    private final StateMachine accessWorkFlowStateMachine = new StateMachine("/com/bt/rsqe/ape/workflow/AccessWorkflowStateTransitionsConfig");


    protected AccessWorkflowStatus getWorkflowStatus(QrefAttributeExtractor attributeExtractor) {
        String workflowStatus = attributeExtractor.getAttributeValue(LocalIdentifier.WORKFLOW_STATUS);
        return Strings.isNullOrEmpty(workflowStatus) ? null : AccessWorkflowStatus.workflowFromStatus(workflowStatus);
    }

    protected boolean transitionAllowed(TransitionName transitionName, QrefAttributeExtractor attributeExtractor) {
        AccessWorkflowStatus workflowStatus = getWorkflowStatus(attributeExtractor);
        return accessWorkFlowStateMachine.transitionAllowed(transitionName.name(), workflowStatus != null ? workflowStatus.name() : null);
    }

    protected Response invalidWorkflowResponse() {
        return invalidWorkflowResponse(INVALID_WORKFLOW_STATE_ERROR);
    }

    protected Response invalidWorkflowResponse(String msg) {
        return ResponseBuilder.aResponse().withStatus(Response.Status.PRECONDITION_FAILED).withEntity(msg).build();
    }
}
