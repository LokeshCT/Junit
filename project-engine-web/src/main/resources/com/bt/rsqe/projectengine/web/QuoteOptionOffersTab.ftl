<div class="" id="offersContainer">

    <div id="filterPane" class="filterPaneContainer"></div>

    <div class="data-Container border">
    <div id="commonError" class="error hidden"></div>
    <table id="offers">
        <thead>
        <tr>
            <th>Name</th>
            <!-- New Filed Addition Customer Order Reference, GSCE-126943 -->
            <th>Customer Order Reference</th>
            <th>Creation Date</th>
            <th class="ColumnWidth">Status</th>
            <th>Actions</th>
            <th class="uri"></th>
        </tr>
        </thead>
        <tbody>
        <#list view.offers as offer>
        <#assign trCss = (offer_index % 2 == 0)?string("odd","even")>
        <tr class="offer ${trCss}">
            <td class="name">
            ${offer.name}
            </td>
            <td class="name">
            ${offer.customerOrderReference}
            </td>
            <td class="created">
            ${offer.created}
            </td>
            <td class="status">
            ${offer.status}
            </td>
            <td class="actions">
            <#if offer.canApprove>
                    <a action="${offer.approveOfferLink}" href="#" class="approve action"><img src="/rsqe/project-engine/static/images/accept.png" title="Customer Approve" alt="Customer Approve" /></a>
                    <a href="${offer.rejectOfferLink}" class="reject action"><img src="/rsqe/project-engine/static/images/cancel.png" title="Customer Reject"  alt="Customer Reject" /></a>




            <#elseif offer.canCancel>
            <input type="button" id="cancelApproval" class="submit button" value="Cancel Approval"/>
                        <input type="hidden" id="cancelOfferApprovalUri" value="${offer.cancelOfferApprovalUri}"/>
            </#if>

            </td>
            <td class="uri">${offer.offerDetailsLink}</td>
        </tr>
        </#list>
        </tbody>
    </table>
    </div>
</div>
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
