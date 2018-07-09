/**
 * This namespace defines the BCM functionality used by the {@link standardTab} and {@link costTab}.
 *
 * @namespace BCM
 * @BCM The object to append all logic onto.
 * @undefined This parameter prevents undefined from being redefined by other JavaScript code.
 */
(function(BCM, undefined) {

    // Short names for the namespaces used here.
    var pricing     = rsqe.pricingTab;
    var url         = rsqe.urlBuilder;

    var BCM_IMPORT_WARNING_DIALOG              = '#bcmImportWarningDialog';
    var WARNING_DIALOG_YES_BUTTON              = '#warningDialogYesButton';
    var BCM_EXPORT_TYPE_FILTER                 = '#bcmExportTypeFilter';
    var BCM_IMPORT_LINKS                       = "a[rel='bcmImport']";
    var BCM_EXPORT_LINKS                       = "a[rel='bcmExport']";
    var BCM_REJECT_DISCOUNTS_LINK              = "a[rel='bcmRejectDiscounts']";
    var BCM_APPROVE_DISCOUNTS_LINKS            = "a[rel='bcmApproveDiscounts']";

    var PROGRESS_DIALOG                        = '#progressDialog';
    var BID_COMMENTS_DIALOG                    = '#commentsDialog';
    var PROGRESS_TEXT                          = '#progressText';
    var PROGRESS_BUTTON                        = '#progressButton';

    // Button to Export BCM and message displayed on clicking the button.
    BCM.BCM_EXPORT_BUTTON                      = '.bcm-export-button';
    BCM.EXPORT_BCM_SHEET_MESSAGE               = '#export-bcm-sheet-msg';

    // Import BCM Button. Located on the Cost Tab.
    BCM.IMPORT_BCM_BUTTON                      = '#importBCMButton';

    /**
     * Initialize the BCM functionality.
     * @public
     */
    BCM.initialize = function() {

        // Only initialize if the current user has BCM access.
        if (pricing.hasBcmAccess()) {

            // Initialize Dialogs.
            setupCommentsDialog();
            setupProgressDialog();
            setupBcmImportWarningDialog();

            // Initialize BCM
            initBCM();

        }

    };

    // TODO: JSDOC
    function initBCM() {

        var quoteOptionName = pricing.getQuoteOptionName();

        // Setup BCM Functionality.
        BCM.BCM_IMPORT_DIALOG = new rsqe.BcmImportDialog(BCM.progressDialog, BCM.progressDialogInstance);
        BCM.BCM_IMPORT_DIALOG.setupDialog();
        $(BCM_IMPORT_LINKS).each(function() {
            $(this).click(function() {
                BCM.bcmImportWarningDialogInstance.open();
                return false;
            });
        });

        $(BCM_EXPORT_LINKS).each(function() {
            var uri = $(this).attr("href");
            $(this).click(function() {
                return $(this).attr("href", uri + '&offerName=' + $(BCM_EXPORT_TYPE_FILTER).val());
            });
        });


        // TODO: Should have a function to do this.
        $(BCM_REJECT_DISCOUNTS_LINK).each(function() {
            var commentsUri = this.getAttribute('action');
            $(this).click(function () {
                BCM.COMMENTS_DIALOG.setOptions( {"title":"Rejection Confirmation"} );
                loadCommentsAndCaveatsDialog(commentsUri, "Reject", quoteOptionName, "Pricing");
                BCM.COMMENTS_DIALOG.open();
                return false;
            });
        });

        $(BCM_APPROVE_DISCOUNTS_LINKS).click(function() {
            var commentsUri = this.getAttribute('action');
            BCM.COMMENTS_DIALOG.setOptions({"title":"Approval Confirmation"});
            loadCommentsAndCaveatsDialog(commentsUri, "Approve", quoteOptionName, "Pricing");
            BCM.COMMENTS_DIALOG.open();
            return false;
        });

    }

    function loadCommentsAndCaveatsDialog(commentsCaveatsUri, discountRequestStatus, quoteOptionName, calledFromPage) {

        BCM.COMMENTS_DIALOG.setOptions({close:function () {
            BCM.COMMENTS_DIALOG_FORM.resetForm();
        }});
        $(BID_COMMENTS_DIALOG).load(commentsCaveatsUri, function () {
            BCM.COMMENTS_DIALOG_FORM.load(discountRequestStatus, quoteOptionName, calledFromPage);
        });
    }

    function setupBcmImportWarningDialog() {
        BCM.bcmImportWarningDialogInstance = new rsqe.Dialog($(BCM_IMPORT_WARNING_DIALOG), {
            title: "Warning!",
            width: 350
        });

        $(WARNING_DIALOG_YES_BUTTON).click(function() {
            BCM.bcmImportWarningDialogInstance.close();
            openBcmImportDialog();
            return false;
        });

    }

    function setupCommentsDialog() {

        BCM.COMMENTS_DIALOG = new rsqe.Dialog(
            $(BID_COMMENTS_DIALOG),
            {
                width:500, position:'top'
            }
        );

        BCM.COMMENTS_DIALOG_FORM = new rsqe.BidCommentsAndCaveatsDialog(
            {
                cancelHandler:function () {
                    BCM.COMMENTS_DIALOG.close();
                }
            }
        );

    }

    function setupProgressDialog() {
        BCM.progressDialog = new rsqe.ProgressDialog($(PROGRESS_DIALOG), {
            progressText:   PROGRESS_TEXT,
            progressButton: PROGRESS_BUTTON,
            errorClass:     "error",
            successClass:   "success"
        });

        BCM.progressDialogInstance = new rsqe.Dialog($(PROGRESS_DIALOG), {
            title   : "Progress",
            width   : "350px",
            closers : $(PROGRESS_DIALOG).find(".ok")
        });
    }

    function openBcmImportDialog() {
        BCM.BCM_IMPORT_DIALOG.openDialog(url.BCM_URL, $(BCM.IMPORT_BCM_BUTTON));
    }

// Immediately invoke this namespace.
}( rsqe.pricingTab.BCM = rsqe.pricingTab.BCM || {} ));