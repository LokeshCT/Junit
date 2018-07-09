package com.bt.rsqe.projectengine.web.quoteoption;

import com.bt.rsqe.seleniumsupport.liftstyle.SeleniumWebDriverTestContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.Arrays;

import static com.bt.rsqe.projectengine.web.quoteoption.PaginatedTab.PaginationButtonElement.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public abstract class PaginatedTab extends SeleniumWebDriverTestContext {

    private WebDriver driver;
    private String representedEntity = "Root Products";

    @FindBy(css = ".dataTables_info")
    private WebElement messageElement;
    @FindBy(id = "lineItems_first")
    private WebElement firstButtonElement;
    @FindBy(id = "lineItems_previous")
    private WebElement previousButtonElement;
    @FindBy(id = "lineItems_next")
    private WebElement nextButtonElement;
    @FindBy(id = "lineItems_last")
    private WebElement lastButtonElement;


    public PaginatedTab(WebDriver driver) {
        super(driver);
        this.driver = driver;
    }

    protected PaginatedTab(WebDriver driver, String representedEntity) {
        this(driver);
        this.representedEntity = representedEntity;
    }

    public void assertShowingCorrectPagination(int numberShowingStart, int numberShowingEnd, int totalNumber) {
        String message = String.format("Showing %d to %d of %d %s", numberShowingStart, numberShowingEnd, totalNumber, representedEntity);
        assertThat(messageElement.getText(), containsString(message));
    }

    public void assertShowingPaginationButtonWithStatuses(PaginationButtonState firstButtonState, PaginationButtonState previousButtonState, PaginationButtonState nextButtonState, PaginationButtonState lastButtonState) {
        assertButtonState(PaginationButtons.FIRST, firstButtonState);
        assertButtonState(PaginationButtons.PREVIOUS, previousButtonState);
        assertButtonState(PaginationButtons.NEXT, nextButtonState);
        assertButtonState(PaginationButtons.LAST, lastButtonState);
    }

    public void assertButtonState(PaginationButtons button, PaginationButtonState state) {
        switch (button) {
            case FIRST:
                paginationButtonFor(button, firstButtonElement).assertState(state);
                break;
            case PREVIOUS:
                paginationButtonFor(button, previousButtonElement).assertState(state);
                break;
            case NEXT:
                paginationButtonFor(button, nextButtonElement).assertState(state);
                break;
            case LAST:
                paginationButtonFor(button, lastButtonElement).assertState(state);
                break;
        }
    }

    static class PaginationButtonElement {
        private WebElement element;
        private PaginationButtons button;
        public static final String PAGINATE_BUTTON_DISABLED_CSS_CLASS = "paginate_button_disabled";

        private PaginationButtonElement(PaginationButtons button, WebElement element) {
            this.element = element;
            this.button = button;
        }

        private void assertState(PaginationButtonState state) {

            String reason = String.format("Pagination Button: %s current state does not match expected state: %s", button, state);
            switch (state) {
                case ENABLED:
                    assertThat(reason, isEnabled(), is(true));
                    break;
                case DISABLED:
                    assertThat(reason, isDisabled(), is(true));
                    break;
            }
        }

        public boolean isDisabled() {
            String[] classes = element.getAttribute("class").split("\\s");
            return Arrays.asList(classes).contains(PAGINATE_BUTTON_DISABLED_CSS_CLASS);
        }

        public boolean isEnabled() {
            return !isDisabled();
        }

        public static PaginationButtonElement paginationButtonFor(PaginationButtons button, WebElement element) {
            return new PaginationButtonElement(button, element);
        }
    }


    public enum PaginationButtons {
        FIRST, PREVIOUS, NEXT, LAST
    }

    public enum PaginationButtonState {
        ENABLED, DISABLED;
    }
}
