<div id="costPricingView">

<div class="leftPaneContainer">
    .
    <span id="costAttachmentUrl" class="hidden">${view.costAttachmentUrl}</span>

    <@cc.filterpane>
        <h2>Filter</h2>

        <div class="filterContent">

            <div class="content">
                <label>Product:</label> <@cc.select id="costProductFilter">
                <#list view.productNames as productName>
                    <option value="${productName}">${productName}</option>
                </#list>
            </@cc.select>
                <label>Country:</label> <@cc.select id="costCountryFilter">
                <#list view.countries as country>
                    <option value="${country}">${country}</option>
                </#list>
            </@cc.select>
            <label>Vendor Discount Ref:</label>
            <select id="vendorDiscountRefFilter">
                <option value="">--Please Select--</option>
            </select>

                <input type="button" class="button" id="applyCostFilterButton" value="Apply"/>
                <input type="button" class="button" id="clearCostFilterButton" value="Clear All"/>
            </div>

        </div>
    </@cc.filterpane>

    <div class="data-Container border">
        <div id="costWarn">
            <ul class="warning-message">
                <li>
                    <span>You have unsaved changes</span>
                    <input type="button" value="Save" class="button" id="costSave"/>
                    <input type="button" value="Discard" class="button" id="costDiscard"/>
                </li>
            </ul>
        </div>
        <div id="costSuccess"><ul class="success-message"><li>Your changes have been saved</li></ul></div>
        <div class="ui-state-highlight savediscount" style="width: 400px;" id="costDiscountWarnMessage">
            <p>Enter Gross/discount percentage/Ref or untick check box.</p>
        </div>
        <div id="costError" class="commonError hidden"></div>
        <br/>
        <table id="costPriceLines" style="font-size: smaller;">
            <thead>
                <tr>
                    <th rowspan="2" class="noBorder">Product</th>
                    <th rowspan="2">Summary</th>
                    <th rowspan="2">Site</th>
                    <th rowspan="2">Site Address</th>
                    <th rowspan="2">Description</th>
                    <th rowspan="2">Status</th>
                    <th colspan="3" class="th_bg">One Time Cost - RRP (${view.currency})</th>
                    <th colspan="3" class="th_bg">Recurring Cost - RRP (${view.currency})</th>
                    <th rowspan="2">Vendor Discount Ref</th>
                </tr>
                <tr>
                    <th class="subtypes">Gross</th>
                    <th class="subtypes">Discount (%)</th>
                    <th class="subtypes">Net</th>
                    <th class="subtypes">Gross</th>
                    <th class="subtypes">Discount (%)</th>
                    <th class="subtypes">Net</th>
                </tr>
            </thead>
            <tbody>
            </tbody>
        </table>
        <div id="costAttachmentDialog"></div>
    </div>
</div>

<@cc.rightpane>

    <h3>Cost Summary</h3>

    <h4 class="totalPrice">One Time Total</h4>

    <div class="content discounts pricingSummaryContainer" id="oneTimeCostSummary">
        <ul>
            <li>
                <span id="totalOneTimeCost">0</span>
                Before discount:
            </li>
            <li>
                <span id="totalOneTimeNet">0</span>
                After discount:
            </li>
            <li>
                <span id="totalOneTimeDiscount">0%</span>
                Discount average:
            </li>
        </ul>
    </div>

    <h4 class="totalPrice recurringHeading">Recurring Total</h4>

    <div class="content discounts pricingSummaryContainer" id="recurringCostSummary">
        <ul>
            <li>
                <span id="totalRecurringCost">0</span>
                Before discount:
            </li>
            <li>
                <span id="totalRecurringNet">0</span>
                After discount:
            </li>
            <li>
                <span id="totalRecurringDiscount">0%</span>
                Discount average:
            </li>
        </ul>
    </div>

    <h4 class="totalPrice usageHeading">Usage Total</h4>

    <div class="content discounts pricingSummaryContainer" id="usageCostSummary">
        <ul>
            <li>
                <span id="totalOffNetUsageCost">0</span>
                Off net total:
            </li>
            <li>
                <span id="totalOnNetUsageCost">0</span>
                On net total:
            </li>
            <li>
                <span id="totalUsageCost">0</span>
                Total:
            </li>
        </ul>
    </div>

    <h3 class="discount_actions">
        Discount Actions
    </h3>

    <div class="content bulkDiscount">
        <h4>One Time</h4>
        <ul id="oneTimeCostBulkPricingActions">
            <li class="percent">
                <input type="radio" name="bulkOneTimeCostRadio" checked="checked" value="percent"/>
                <label for="bulkOneTimeCostPercent">Percent</label>
                <input class="amount" type="text" id="bulkOneTimeCostPercent"/>
                <span class="fieldSuffix">%</span>
            </li>
            <li class="net">
                <input type="radio" name="bulkOneTimeCostRadio" value="net"/>
                <label for="bulkOneTimeCostNet">Net</label>
                <input class="amount" type="text" id="bulkOneTimeCostNet"/>
            </li>
        </ul>

        <h4>Recurring</h4>
        <ul id="recurringCostBulkPricingActions">
            <li class="percent">
                <input type="radio" name="bulkRecurringCostRadio" checked="checked" value="percent"/>
                <label for="bulkRecurringCostPercent">Percent</label>
                <input class="amount" type="text" id="bulkRecurringCostPercent"/>
                <span class="fieldSuffix">%</span>
            </li>
            <li class="net">
                <input type="radio" name="bulkRecurringCostRadio" value="net"/>
                <label for="bulkRecurringCostNet">Net</label>
                <input class="amount" type="text" id="bulkRecurringCostNet"/>
            </li>
        </ul>
        <button id="applyBulkDiscountCost" class="button">Apply discount</button>
    </div>

    <div class="content">
        <h4>Vendor Discount Reference</h4>
        <ul>
            <li>
                <input type="text" id="vendorDiscountReference">
            </li>
        </ul>
        <button id="updateVendorDiscountReference" class="button">Update Reference</button>
    </div>

    <div class="content" id="actions">
        <ul>
            <li id="uploadAttachments">
                <button id="uploadCostAttachmentsButton" class="main-action">Upload Attachments</button>
            </li>

            <li id="exportBCM">
                <a href="${view.bcmUri}?newBcmExportVersion=no">
                    <button id="exportBCMButton" class="main-action">Export BCM</button>
                </a>
            </li>

            <li id="importBCM">
                <span class="hidden" id="importBCMActionUrl">${view.bcmUri}</span>
                <button data-id="${view.quoteOptionId}" id="importBCMButton" class="main-action">Import BCM</button>
            </li>
        </ul>
    </div>

</@cc.rightpane>

<span class="hidden" id="costDiscountApplicable">${view.costDiscountApplicable?string("true","false")}</span>

<#-- To show Warning dialog before importing BCM -->
<@cc.dialog id="bcmImportWarningDialog"; for>
    <@content for=="main">
    <div>
        <span>Have you attached Cost Approval documents?</span><br/><br/>
        <span>Click Yes to continue or Cancel for necessary Actions</span><br/><br/>
    </div>
    </@content>

    <@content for=="buttons">
    <input type="button" id="warningDialogYesButton" class="submit" value="Yes"/>
    </@content>
</@cc.dialog>

<@cc.dialog id="bcmImportDialog" ; for>
<@content for=="main">
<form id="bcmImportForm" action="#" enctype="multipart/form-data" method="post" target="bcmTarget">
    <ul class="form dialogbox bcmImport">
        <li class="filebox">
            <label for="bcmSheet">File : </label>

            <div class="fields">
                <input type="file" name="bcmSheet" class="bcmSheet"/>
            </div>
        </li>
    </ul>

    <iframe id="bcmTarget" name="bcmTarget" src="about:blank"></iframe>
</form>
</@content>

<@content for=="buttons">
<input type="button" id="uploadButton" class="submit" value="Upload"/>
</@content>

</@cc.dialog>

<@cc.progressDialog id="progressDialog"; for>
<@content for=="main">
<div>
    <div class="commonError">
        <span id="progressText"></span>

        <p id="errorMessages" class="hidden"></p>
    </div>
</div>
<div id="spinning">
    <img src="/rsqe/project-engine/static/images/spinning.gif"/>
</div>
<input type="button" id="progressButton" class="ok" value="Ok"/>
<input type="button" id="yes" class="yes button hidden" value="Yes"/>
<input type="button" id="no" class="yes button hidden" value="No"/>
</@content>
</@cc.progressDialog>

</div>