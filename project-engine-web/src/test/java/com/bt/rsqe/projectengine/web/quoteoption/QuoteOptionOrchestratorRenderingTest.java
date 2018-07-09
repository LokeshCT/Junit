package com.bt.rsqe.projectengine.web.quoteoption;


import com.bt.rsqe.projectengine.ProjectIdDTO;
import com.bt.rsqe.projectengine.web.quoteoption.validation.QuoteOptionsBillAccountCurrencyValidator;
import com.bt.rsqe.projectengine.web.view.CustomerProjectQuoteOptionsTab;
import com.bt.rsqe.web.Presenter;
import com.bt.rsqe.web.View;

import com.google.common.collect.Sets;
import junit.framework.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

public class QuoteOptionOrchestratorRenderingTest extends QuoteOptionOrchestratorBaseTest {

    public static final String VALIDATION_MESSAGES_ID = "validationMessages";

    @Test
    public void shouldNotShowValidationMessagesBoxIfNoMessages() {
        assertResponseDoesNotContainText(VALIDATION_MESSAGES_ID);
    }

    @Test
    public void shouldShowValidationMessageBoxIfMessages() {
        when(validator.validate(eq(CUSTOMER_ID), any(QuoteOptionsBillAccountCurrencyValidator.class))).thenReturn(Sets.newHashSet("an error message"));

        assertResponseContainsText(VALIDATION_MESSAGES_ID);
    }

    @Test
    public void shouldShowValidationMessages() {
        when(validator.validate(eq(CUSTOMER_ID), any(QuoteOptionsBillAccountCurrencyValidator.class))).thenReturn(Sets.newHashSet("an error message"));

        assertResponseContainsText("<li>an error message</li>");
    }

    @Test
    public void shouldShowMultipleValidationMessages() {
        HashSet<String> messages = Sets.newHashSet("an error message", "another error message");
        when(validator.validate(eq(CUSTOMER_ID), any(QuoteOptionsBillAccountCurrencyValidator.class))).thenReturn(messages);

        assertResponseContainsText("<li>an error message</li>");
        assertResponseContainsText("<li>another error message</li>");
    }

    private void assertResponseContainsText(String text) {
        Assert.assertTrue("expected source to contain text '" + text + "'", responseContainsText(text));
    }

    private void assertResponseDoesNotContainText(String text) {
        Assert.assertFalse("expected source not to contain text '" + text + "'", responseContainsText(text));
    }

    private boolean responseContainsText(String text) {
        String source = getResponseSource();
        return source.contains(text);
    }

    private String getResponseSource() {
        List<String> projectIdList = new ArrayList<String>();
        projectIdList.add("someProjectId");
        ProjectIdDTO projectIdDTO = new ProjectIdDTO(projectIdList);
        CustomerProjectQuoteOptionsTab result = quoteOptionOrchestrator.buildResponse(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, projectIdDTO);
        String viewPath = "com/bt/rsqe/projectengine/web/ProjectQuoteOptionsTab.ftl";
        return new Presenter().render(View.viewUsingTemplate(viewPath).withContext("view", result).withContext("projectHelpLink", "projectHelpLink"));
    }
}
