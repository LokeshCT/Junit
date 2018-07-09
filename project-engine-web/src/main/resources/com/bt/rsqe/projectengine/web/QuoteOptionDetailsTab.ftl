<div class="panes-container details_tab">

    <span id="bulkTemplateUri" class="hidden">${view.bulkTemplateUri}</span>
    <span id="raiseIfcUrl" class="hidden">${view.raiseIfcUrl}</span>
    <span id="currency" class="hidden">${view.currency}</span>
    <span id="pricingUrl" class="hidden">${view.pricingUrl}</span>
    <span id="lineItemsUrl" class="hidden">${view.lineItemsUrl}</span>
    <span id="attachmentUrl" class="hidden">${view.attachmentUrl}</span>
    <span id="locateOnGoogleMapsUrl" class="hidden">${view.uriFactory.getLocateOnGoogleMapsViewUri(view.customerId,view.contractId,view.projectId,view.quoteOptionId)?html}</span>
    <span id="validate" class="hidden">${view.uriFactory.lineItemValidationUri(view.customerId,view.contractId,view.projectId,view.quoteOptionId,"(id)")}</span>

    <div class="leftPaneContainer">

    <@cc.filterpane>
        <h2>Filter</h2>
        <div id="filterPanel" class="filterContent">
            <div class="content">
                <label>Hide failed line items:</label>
                <input type="checkbox" class="checkbox" id="hideFailedLineItemsCheckbox" name="hideFailedLineItemsCheckbox" value="true" />
                <input type="button" class="button" id="applyFilterButton" value="Apply" />
            </div>
        </div>
    </@cc.filterpane>

        <div class="rsqe-nav">
                    <a href="${view.quoteOptionId}/add-product/" id="newLineItem" class="main-action btnDisable">Add Product</a>
                    <a href="${view.uriFactory.getBulkViewUri(view.customerId,view.contractId,view.projectId,view.quoteOptionId)?html}" id="bulkConfigurationButton" class="main-action btnDisable">Configure Product</a>
                    <a href="#" id="importProduct" class="main-action hidden">Import Product</a>
                    <a href="#" id="fetchPrices" class="main-action disabled actionBtnDisable" >Calculate Price</a>
                    <a href="#" id="createOffer" class="main-action disabled actionBtnDisable" >Create Offer</a>
                    <#if view.allowCopyOptions ><a href="#" id="copyOptions" class="main-action disabled">Copy to Quote Option</a></#if>
                    <a href="#" id="raiseIfcs"  class="main-action disabled actionBtnDisable">Raise IFC</a>
                    <a href="#" id="attachments" class="main-action enabled btnDisable">Add/Edit Attachments</a>
                    <a href="#" id="locateOnGoogleMaps" class="main-action disabled btnDisable">Locate On Google Map</a>
                </div>

        <div class="data-Container border" style="height: auto !important;">
            <div id="commonError" class="hidden"></div>
            <div id="userInfo"></div>
            <div id="successMessage" class="hidden"></div>
            <div id="viewConfigurationDialog"></div>
            <div class="hidden" id="viewConfigurationDialogUri">${view.viewConfigurationDialogUri}</div>
            <div id="notesDialog"></div>
            <div id="contractDialog"></div>
            <div id="serviceLevelAgreementDialog"></div>
            <div id="maintainerAgreementDialog"></div>
            <div id="attachmentDialog"></div>
            <div class="search-container">
                <input type="text" id="globalSearch" placeholder="Search Line Items..." title="Search by Site, Product, Summary or Pricing Status"/>
                <input type="button" id="globalSearchBtn" class="submit button" value="Search"/>
                <span class="search-input-description" id="globalSearch-example-text" style="display:none;">You can search using a combination of site, product, summary or pricing status.  For example: <i style="font-style:italic !important;">&lt;Site&gt; && &lt;Product&gt;</i></span>
                <input type="image" id="viewConfigurationLink" src="/rsqe/project-engine/static/images/view_config.png" href="#" title="Detailed Quote View"/>
            </div>

            <div>
          <label>Show All Columns</label>
          <input type="checkbox" name="hideColumns" />
            </div>

            <table id="lineItems">
                <thead>
                <tr>
                    <th class="noBorder checkbox"><input id="selectAll" type="checkbox" title="Select All"/></th>
                    <th><div><span>Site</span></div></th>
                    <th><div><span>Site Address</span></div></th>
                    <th class="hideColumns"><div><span>Sublocation Name</span></div></th>
                    <th class="hideColumns"><div><span>Room</span></div></th>
                    <th class="hideColumns"><div><span>Floor</span></div></th>
                    <th><div><span>Product</span></div></th>
                    <th><div><span>Summary</span></div></th>
                    <th><div><span>Order Type</span></div></th>
                    <th class="tipDetails" title="Contract Term (in months)"><div><span>Term</span></div></th>
                    <th><div><span>Offer</span></div></th>
                    <th><div><span>Status</span></div></th>
                    <th><div><span>Discount Status</span></div></th>
                    <th><div><span>Pricing Status</span></div></th>
                    <th><div><span>Order Status</span></div></th>
                    <th><div><span>Configured</span></div></th>
                    <th><div><span>Service Level Agreements</span></div></th>
                    <th><div><span>Maintainer Agreements</span></div></th>
                    <th><div><span>Remaining Contract Term</span></div></th>
                    <th><div><span>Actions</span></div></th>
                </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
        </div>
    </div>
</div>

<div id="bulkTemplateDialog"></div>

<@cc.dialog id="importProductDialog" ; for>
    <@content for=="main">
    <p>Upload work book with configured products: </p>
    <div>
    <form id="importProductForm" action="${view.importProductURL}" enctype="multipart/form-data" method="post" target="bulkTarget">
        <input type="hidden" name="maxLineItems" id="maxLineItems" value="${view.maxConfigurableLineItems}"/>
        <input type="hidden" name="removeLineItemEnabled" id="removeLineItemEnabled" value="${view.removeLineItemAllowed}"/>
        <input type="hidden" name="customerId" id="customerId" value="${view.customerId}"/>
        <input type="hidden" name="contractId" id="contractId" value="${view.contractId}"/>
        <input type="hidden" name="projectId" id="projectId" value="${view.projectId}"/>
        <input type="hidden" name="quoteOptionId" id="quoteOptionId" value="${view.quoteOptionId}"/>
        <input type="hidden" name="revenueOwner" id="revenueOwner" value="${view.revenueOwner}"/>
        <input type="hidden" name="validateImportProductUrl" id="validateImportProductUrl" value="${view.validateImportProductUrl}"/>
        <input type="hidden" name="quoteOptionContext" id="quoteOptionContext" value=""/>
        <input type="hidden" name="eCRFSheetWorkBookName" id="eCRFSheetWorkBookName" value=""/>
        <ul class="form dialogbox importProduct">
            <li class="filebox">
                <input id="eCRFSheet" type="file" name="eCRFSheet" class="file"/>
            </li>
        </ul>
    </form>
    </div>
    </@content>
    <@content for=="buttons">
        <input id="importProductUpload" type="button" class="submit button" value="Upload"/>
    </@content>
</@cc.dialog>

<@cc.dialog id="updateProductConfigDialog" ; for>
    <@content for=="main">
    <form id="productConfigForm" method="post" action="#">
        <input type="hidden" class="quoteOptionContext" name="quoteOptionContext" value=""/>

        <ul class="form dialogbox productConfig">
            <li>
                <label>Product:</label>
                <@cc.product_list view.products;  productId>
                ${view.getCreateProductUrl(productId)}
                </@cc.product_list>
            </li>
        </ul>

    </form>
    </@content>
    <@content for=="buttons">
    <input type="button" class="prod-config button" value="Next" id="prodConfig"/>
    </@content>
</@cc.dialog>

<@cc.dialog id="createOfferDialog" ; for>
    <@content for=="main">
    <form id="createOfferForm" action="${view.offersFormAction}" method="post">
        <div id="offerLoadingMessage" class="dialog-loading-msg">Creating Offer...</div>
        <div id="createOfferError" class="error hidden commonError" style="margin-bottom: 10px;"></div>
        <input type="hidden" class="projectId" name="projectId" value="${view.projectId}"/>
        <input type="hidden" class="quoteOptionId" name="quoteOptionId" value="${view.quoteOptionId}"/>
        <input type="hidden" class="quoteOptionItemIds" id="quoteOptionItemIds" name="quoteOptionItemIds"/>

        <div>
            <ul class="form dialogbox">
                <li><label>Offer name:</label>

                    <div class="fields"><input type="text" id="offerNameText" name="offerName" value=""/></div>
                </li>
                <!-- New Filed Addition Customer Order Reference, GSCE-126943 -->
                <li><label>Customer Order Reference:</label>

                    <div class="fields"><input type="text" id="customerOrderRefText" name="customerOrderReference" maxlength="20" value=""/></div>
                </li>
            </ul>
            <p id="customerOrderRefTextError" class="error commonError hidden">The maximum length for this field is 20.</p>
        </div>
    </form>
    </@content>
    <@content for=="buttons">
    <input type="button" id="submitOffer" class="submit" value="Save"/>
    </@content>
</@cc.dialog>


<@cc.dialog id="copyOptionsDialog" ; for>
    <@content for=="main">
    <form id="copyOptionsForm" action="" method="post">
        <ul class="form dialog">
            <li><span>Please select a Quote Option to copy line items to:</span></li>
            <div id="copyTargetOptions"></div>
        </ul>
        <input type="hidden" class="baseQuoteOptionsUri" name="baseQuoteOptionsUri" value="${view.baseQuoteOptionsUri}"/>
        <input type="hidden" class="targetQuoteOptionsUri" name="targetQuoteOptionsUri" value="${view.quoteOptionTargetForCopyUri}"/>
        <input type="hidden" class="projectId" name="projectId" value="${view.projectId}"/>
        <input type="hidden" class="quoteOptionItemIds" id="quoteOptionItemIdsToCopy" name="quoteOptionItemIdsToCopy"/>
        <input type="hidden" class="quoteOptionId" name="quoteOptionId" value="${view.quoteOptionId}"/>

        <div id="copyOptionsMessage" class="error hidden">Please select a quote option</div>
    </form>
    </@content>
    <@content for=="buttons">
    <input type="button" id="submitQuoteOptionsCopy" class="submit" value="Copy"/>
    </@content>
</@cc.dialog>

<@cc.progressDialog id="progressDialog"; for>
    <@content for=="main">
    <div class="commonError">
        <span id="progressText"></span>
        <p id="errorMessages" class="hidden"></p>
    </div>
    <div id="spinning">
        <img src="/rsqe/project-engine/static/images/spinning.gif"/>
    </div>
    <input type="button" id="close" class="close button hidden" value="Close"/>
    <input type="button" id="yes" class="yes button hidden" value="Yes"/>
    <input type="button" id="no" class="no button hidden" value="No"/>
    </@content>
</@cc.progressDialog>

<@cc.dialog id="confirmationDialog"; for>
    <@content for=="main">
    <div class="confirmation_question">
        <span>Are you sure you want to proceed?</span>

        <input type="radio" id="confirmationDialogYesOption" value="Yes" name="confirmationDialogOption" />
        <label for="confirmationDialogYesOption">Yes</label>

        <input type="radio" id="confirmationDialogNoOption" value="No" name="confirmationDialogOption" checked="checked" />
        <label for="confirmationDialogNoOption">No</label>

    </div>

    </@content>
    <@content for=="buttons">
    <input type="button" id="dialogOkButton" class="submit" value="Ok"/>
    </@content>
</@cc.dialog>

