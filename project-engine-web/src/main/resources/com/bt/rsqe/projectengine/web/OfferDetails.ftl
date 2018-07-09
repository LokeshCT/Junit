<div class="panes-container offers_tab">
    <span class="hidden" id="customerId">${view.customerId}</span>
    <span class="hidden" id="contractId">${view.contractId}</span>
    <span class="hidden" id="projectId">${view.projectId}</span>
    <span class="hidden" id="quoteOptionId">${view.quoteOptionId}</span>
    <span class="hidden" id="offerId">${view.offerId}</span>
    <span class="hidden" id="showApproveOffer">${view.showApproveOffer}</span>
    <span class="hidden" id="showRejectOffer">${view.showRejectOffer}</span>
    <span class="hidden" id="showCreateOrder">${view.showCreateOrder}</span>
    <span class="hidden" id="validate">${view.validateAction}</span>
    <span class="hidden" id="showCancelOfferApproval">${view.cancelOfferApproval}</span>

    <div style="display:none; line-height:17px;"  title="Pricing Sheet Download" id="export-pricing-sheet-msg">
        <p>Your pricing sheet is being generated and will be available to view shortly.</p>
    </div>

    <div class="leftPaneContainer">

        <div class="rsqe-nav">
            <#if view.isApprovable>
                <a id="customerApproved" class="main-action" action="${view.approveAction}" href="#">Customer Approve</a>
            </#if>
            <#if view.isRejectable>
                <a id="customerRejected" class="main-action" href="#">Customer Reject</a>
            </#if>
            <#if view.canCreateOrder>
                <a href="#" id="createOrder" class="main-action">Create Order</a>
            </#if>
            <#if view.showCancelOfferApproval>
                <a href="#" id="cancelOfferApproval" class="main-action">Cancel Approval</a>
                <span class="hidden" id="cancelOfferApprovalUri">${view.cancelApprovalAction}</span>
            </#if>
            <a href="${view.exportPricingSheetLink}" id="exportPricingSheet"
                               class="exportPricingSheet main-action">Export pricing sheet</a>
        </div>

        <div class="data-Container border">

            <div class="infoContainer">
                <span id="customerName" style="font-weight: bold;">Creation Date:</span>
                <span id="created">[${view.created}]</span>
            </div>

            <div id="commonError" class="error hidden"></div>
            <table id="offerDetails">
                <thead>
                <tr>
                    <th class="noBorder checkbox"><input id="selectAll" type="checkbox" title="Select All"/></th>
                    <th>Site</th>
                   <th>Site Address</th>
                    <th>Product</th>
                    <th>Summary</th>
                    <th>Status</th>
                    <th>Discount Status</th>
                    <th>Pricing Status</th>
                    <th>Valid</th>
                </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
        </div>
    </div>
</div>

<div>
<#if view.isRejectable>
    <form id="rejectOfferForm" action="${view.rejectAction}" method="post">
        <input type="hidden" class="projectId" name="projectId" value="${view.projectId}"/>
        <input type="hidden" class="quoteOptionId" name="quoteOptionId" value="${view.quoteOptionId}"/>
        <input type="hidden" class="offerId" name="offerId" value="${view.offerId}"/>
    </form>
</#if>
</div>

<#if view.canCreateOrder>
<@cc.dialog id="createOrderDialog" ; for>
<@content for=="main">
<form id="createOrderForm" action="${view.createOrderAction}" method="post">
    <div id="orderLoadingMessage" class="dialog-loading-msg">Creating Order...</div>
    <input type="hidden" class="offerItemIds" id="offerItemIds" name="offerItemIds"/>

        <ul class="form dialogbox">
        <li><label>Order name:</label>
            <div class="fields">
        <input type="text" id="orderName" class="orderName" name="orderName" value="" maxlength="50"/>
                </div>
            </li>
            </ul>

</form>
<p id="dialogError" class="error"></p>
</@content>
<@content for=="buttons">
<input type="button" id="submitCreateOrder" class="submit" value="Save"/>
</@content>
</@cc.dialog>
</#if>
<#if view.showCancelOfferApproval>
<@cc.dialog id="cancelOfferApprovalDialog"; for>
    <@content for=="main">
    <div>
        <span>Are you sure you want cancel the approved offer?</span>
        <br/>
    </div>
    </@content>
    <@content for=="buttons">
    <input type="button" class="button" id="cancelDialogOkButton" value="Ok"/>
    </@content>
</@cc.dialog>
</#if>