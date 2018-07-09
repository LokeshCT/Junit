<div class="panes-container" id="pricingDiv">

    <form>
      <fieldset>
        <div id="pricingTypeRadio">
          <input type="radio" id="standardChargesRadio" name="currentPricingType" checked="checked">
          <label for="standardChargesRadio">Standard</label>

          <input type="radio" id="usageChargesRadio" name="currentPricingType">
          <label for="usageChargesRadio">Usage</label>

          <#if permissions.costPricingTabAccess>
              <input type="radio" id="costPricingRadio" name="currentPricingType">
              <label for="costPricingRadio">Cost</label>
          </#if>
        </div>
      </fieldset>
    </form>

    <div id="usageChargesView">
        <#include "QuoteOptionPricingTabUsageCharges.ftl">
    </div>

    <#if permissions.costPricingTabAccess>
        <div id="costPricingView">
            <#include "QuoteOptionPricingTabCostPricing.ftl">
        </div>
    </#if>

    <div id="standardChargesView">

    <div class="leftPaneContainer">

        <@cc.filterpane>
            <h2>Filter</h2>

            <div id="filterPanel" class="filterContent">

                <div class="content">
                    <label>Product:</label> <@cc.select id="productFilter">
                    <#list view.productNames as productName>
                        <option value="${productName}">${productName}</option>
                    </#list>
                </@cc.select>
                    <label>Country:</label> <@cc.select id="countryFilter">
                    <#list view.countries as country>
                        <option value="${country}">${country}</option>
                    </#list>
                </@cc.select>

                    <input type="button" class="button" id="applyFilterButton" value="Apply"/>
                    <input type="button" class="button" id="clearFilterButton" value="Clear All"/>
                </div>

            </div>
        </@cc.filterpane>

        <div class="data-Container border">
            <span class="hidden" id="customerId">${customerId}</span>
            <span class="hidden" id="contractId">${contractId}</span>
            <span class="hidden" id="projectId">${projectId}</span>
            <span class="hidden" id="quoteOptionId">${quoteOptionId}</span>
            <span class="hidden" id="usageDiscountPostUri">/rsqe/customers/${customerId}/projects/${projectId}/quote-options/${quoteOptionId}/discounts/usage</span>
            <span class="hidden" id="discountPostUri">/rsqe/customers/${customerId}/projects/${projectId}/quote-options/${quoteOptionId}/discounts</span>
            <span class="hidden" id="costDiscountPostUri">/rsqe/customers/${customerId}/projects/${projectId}/quote-options/${quoteOptionId}/discounts/cost</span>
            <div id="commonError" class="error hidden"></div>
            <div id="successMessage" class="successmessage hidden"></div>
            <div class="hidden ui-state-highlight savediscount" id="unsavedDiscounts">
                <span class="hidden save-button-overlay" id="pricing-change-save">Saving...</span>
                <p>You have unsaved changes</p>

                <input type="button" id="persistDiscounts" value="Save" class="button"/>
                <input type="button" id="discardDiscounts" value="Discard" class="button"/>
            </div>
            <div class="hidden ui-state-highlight savediscount" style="width: 400px;" id="manualPriceWarnMessage">
                <p>Enter valid price or untick checkbox.</p>
            </div>
            <div class="hidden successmessage" id="saveMessage">
                Your changes have been saved.
            </div>
            <div style="display:none; line-height:17px;"  title="Pricing Sheet Download" id="export-pricing-sheet-msg">
              <p>Your pricing sheet is being generated and will be available to view shortly.</p>
            </div>
            <div style="display:none; line-height:17px;"  title="BCM Sheet Download" id="export-bcm-sheet-msg">
                <p>Your BCM sheet is being generated and will be available to view shortly.</p>
            </div>
            <span class="hidden" id="hasEupAccess">${permissions.eupAccess?string("true","false")}</span>
            <span class="hidden" id="hasIndirectAccess">${permissions.indirectUser?string("true","false")}</span>
            <span class="hidden" id="hasCostPricingTabAccess">${permissions.costPricingTabAccess?string("true","false")}</span>
            <span class="hidden" id="hasBcmAccess">${permissions.bcmAccess?string("true","false")}</span>
             <#--<div>OUR NEW MAP : ${view.isManualModify}</div>-->

             <div style="margin-top:15px">
            <table id="priceLines" style="font-size: smaller">
                <thead>
                <tr>
                    <th rowspan="2">Site</th>
                    <th rowspan="2">Site Address</th>
                    <th rowspan="2">Product</th>
                    <th rowspan="2">Summary</th>
                    <th rowspan="2">Discount Status</th>
                    <th rowspan="2">Offer</th>
                    <th rowspan="2" class="checkbox"><input id="selectAll" type="checkbox" title="Select All"/> Description</th>
                    <th rowspan="2">Status</th>
                <#if permissions.eupAccess>
                    <th colspan="4" class="th_bg">One Time Price - PTP (${view.currency})</th>
                    <#else>
                        <th colspan="3" class="th_bg">One Time Price - RRP (${view.currency})</th>
                </#if>
                <#if permissions.eupAccess>
                    <th colspan="4" class="th_bg tipDetails" title="Recurring Price (Per Month)">Recurring Price - PTP (${view.currency})</th>
                    <#else>
                        <th colspan="3" class="th_bg tipDetails" title="Recurring Price (Per Month)">Recurring Price - RRP (${view.currency})</th>
                </#if>
                </tr>
                <tr>
                <#if permissions.eupAccess>
                    <th class="subtypes">RRP</th>
                </#if>
                    <th class="subtypes">Gross</th>
                    <th class="subtypes">Discount (%)</th>
                    <th class="subtypes">Net</th>
                <#if permissions.eupAccess>
                    <th class="subtypes">RRP</th>
                </#if>
                    <th class="subtypes">Gross</th>
                    <th class="subtypes">Discount (%)</th>
                    <th class="subtypes">Net</th>
                </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
            </div>
        </div>
    </div>
<@cc.rightpane>
    <#if permissions.bcmAccess>
    <h4>BCM Actions</h4>
    <a href="${view.bcmUri}?newBcmExportVersion=no" data-id="${view.quoteOptionId}" rel="bcmExport" class="action bcm-export-button"><img
        src="/rsqe/project-engine/static/images/page_white_put.png" title="Connect Applications BCM Export"
        alt="Connect Applications BCM Export"/></a>

          <a href="${view.bcmUri}?newBcmExportVersion=yes" data-id="${view.quoteOptionId}" rel="bcmExport" class="action bcm-export-button"><img
            src="/rsqe/project-engine/static/images/page_white_put.png" title="BCM Export (excl. Connect Applications)" alt="BCM Export (excl. Connect Applications)"/></a>

        <#if view.discountApprovalRequested>
        <a href="${view.bcmUri}" data-id="${view.quoteOptionId}" id = "bcmImport" rel="bcmImport" class="action bcmImport"><img
            src="/rsqe/project-engine/static/images/page_white_get.png" title="BCM Import" alt="BCM Import"/></a>

        <a id="discountApprove" action="${view.commentsUri}"
           href="#"
           data-id="${view.quoteOptionId}"
           rel="bcmApproveDiscounts"
           class="action bcmApprove">
            <img src="/rsqe/project-engine/static/images/accept.png" title="BCM Approve Discounts"
                 alt="BCM Approve Discounts"/></a>

        <a id="discountReject" href="#" action="${view.commentsUri}" data-id="${view.quoteOptionId}" rel="bcmRejectDiscounts"
           class="action bcmReject"><img src="/rsqe/project-engine/static/images/cancel.png"
                                         title="BCM Reject Discounts"
                                         alt="BCM Reject Discounts"/></a>
        </#if>
    <@cc.select id="bcmExportTypeFilter">
    <option value="">All</option>
        <#list view.offerNames as name>
        <option value="${name}">${name}</option>
        </#list>
    </@cc.select>
    </#if>
    <h3>Pricing Summary</h3>
    <div id="commentsDialog"></div>
    <input type="hidden" id="quoteName" value="${view.quoteOptionName}"/>
    <h4 class="totalPrice">One Time Total</h4>

    <div class="content discounts" id="oneTimePricingSummary">
        <ul>
            <li>
                <span id="oneTimeGrossTotal"></span>
                Before discount:
            </li>
            <li>
                <span id="oneTimeNetTotal"></span>
                After discount:
            </li>
            <li>
                <span id="oneTimeDiscountTotal"></span>
                Discount average:
            </li>
        </ul>
    </div>

    <h4 class="totalPrice recurringHeading">Recurring Total</h4>

    <div class="content discounts" id="recurringPricingSummary">
        <ul>
            <li>
                <span id="recurringGrossTotal"></span>
                Before discount:
            </li>
            <li>
                <span id="recurringNetTotal"></span>
                After discount:
            </li>
            <li>
                <span id="recurringDiscountTotal"></span>
                Discount average:
            </li>
        </ul>
    </div>

    <h4 class="totalPrice usageHeading">Usage Total</h4>

    <div class="content discounts" id="usagePricingSummary">
        <ul>
            <li>
                <span id="usageOffNetTotal"></span>
                Off net total:
            </li>
            <li>
                <span id="usageOnNetTotal"></span>
                On net total:
            </li>
            <li>
                <span id="usageTotal"></span>
                Total:
            </li>
        </ul>
    </div>


    <h3 class="discount_actions">
        Discount Actions
    </h3>


    <div class="content bulkDiscount" id="bulkDiscount">
        <h4>One Time</h4>
        <ul>

            <li>
                <input type="radio" name="oneTime" checked="checked" value="percent"/>
                <label for="bulkDiscountOneTimePercent">Percent</label>
                <input class="amount" type="text" id="bulkDiscountOneTimePercent"/>
                <span class="fieldSuffix">%</span>
            </li>
            <li>
                <input type="radio" name="oneTime" value="nett"/>
                <label for="bulkDiscountOneTimeNett">Net</label>
                <input class="amount" type="text" id="bulkDiscountOneTimeNett"/>
            </li>
        </ul>
        <h4>Recurring</h4>
        <ul>

            <li>
                <input type="radio" name="recurring" checked="checked" value="percent"/>
                <label for="bulkDiscountRecurringPercent">Percent</label>
                <input class="amount" type="text" id="bulkDiscountRecurringPercent"/>
                <span class="fieldSuffix">%</span>
            </li>
            <li>
                <input type="radio" name="recurring" value="nett"/>
                <label for="bulkDiscountRecurringNett">Net</label>
                <input class="amount" type="text" id="bulkDiscountRecurringNett"/>
            </li>
        </ul>
        <button id="applyBulkDiscount" class="button">Apply discount</button>
    </div>

    <div class="content" id="actions">
        <ul>
            <li id="requestDiscount">
                <button id="requestDiscountPopupButton" class="main-action <#if !view.allowRequestDiscount>disabled</#if>" <#if !view.allowRequestDiscount>disabled="true"</#if>>Request Discount</button>
            </li>

            <li id="unlockPriceLines">
                <button id="unlockPriceLinesButton" class="main-action <#if !view.priceLinesLocked>disabled</#if>" <#if !view.priceLinesLocked>disabled="true"</#if>>Unlock PriceLines</button>
            </li>

            <li>
                <a href="${view.exportPricingSheetLink}" id="exportPricingSheet"
                   class="exportPricingSheet main-action">Export pricing sheet</a>
            </li>
        </ul>
    </div>

</@cc.rightpane>

<@cc.dialog id="requestDiscountDialog"  ; for>
<@content for=="main">
<div id="requestDiscountLoadingMessage" class="dialog-loading-msg">Sending request...</div>
    <form id="requestDiscountDialogForm" action="#" method="post" class="requestdiscount">
        <ul class="form dialog">

            <li>
            <label>Select Bid Manager:</label>
            <@cc.select class="bidManagerList"  id="bidManagerList" name="bidManagerList">
            </@cc.select>
            </li>

            <li>
                <div id="copyToEmail">
                    <label>Copy Email to:</label>
                    <input type="text" id="customerGroupEmailId" name="customerGroupEmailId"/>
                </div>
            </li>

            <li>
                <div id="salesUserComments">
                    <h1>Comments :</h1>
                    <#if view.comments?size != 0>
                        <div id="commentsDiv" class="commentsAndCaveatsCapture">
                            <ul>
                                <#list view.comments as comment>
                                    <li class="comment">
                                        <pre>${comment.created}</pre>
                                        <pre>${comment.createdBy}</pre>
                                        <pre>${(comment.comments)!}</pre>
                                    </li>
                                </#list>
                            </ul>
                        </div>
                    </#if>
                </div>
            </li>

            <li>
                <h1>Add Comments :</h1>
                <textarea rows="10" cols="90" id="salesUserNewComment" name="salesUserNewComment" class = "salesUserComments"></textarea>
            </li>

             <li>
                 <input type="checkbox" class="commercialNonStandardRequest" id="commercialNonStandardRequest" name="commercialNonStandardRequest" >Commercial Non Standard Request</input>
            </li>
            <li>
                <div id="commercialNonStandardRequestPanel">
                    <table id="revenueTable" style="font-size: smaller">
                        <thead>
                        <tr>
                            <th rowspan="2" class="checkbox"><input id="revenueSelectAll" name="revenueSelectAll" type="checkbox" title="Select All"/> Product
                                Category
                            </th>
                            <th rowspan="2" class="th_bg">Existing Revenue(${view.currency})</th>
                            <th class="th_bg">Proposed Revenue(${view.currency})</th>
                            <th class="th_bg">Trigger Months</th>
                        </tr>
                        <tr>
                            <th class="checkbox"><input id="proposedText" type="text" title="proposed"/></th>
                            <th class="checkbox"><input id="triggerMonths" type="text" title="triggerMonths"/></th>
                        </tr>
                        </thead>
                        <tbody>
                        </tbody>
                    </table>
                </div>
            </li>
        </ul>
        <span class="hidden" id="requestDiscountActionUrl">${view.requestDiscountActionUrl}</span>
    </form>
    <div id="sendDiscountApprovalMessage">
    </div>

</@content>
<@content for=="buttons">
    <input type="button" id="sendDiscountApprovalButton" value="Request Discount" class="submit"/>
    <input type="button" id="okDiscountApprovalSuccess" value="Ok"/>
</@content>
</@cc.dialog>




<div id="isManualModifyMap" style="display: none;">${view.isManualModify}</div>

    </div>

</div>

