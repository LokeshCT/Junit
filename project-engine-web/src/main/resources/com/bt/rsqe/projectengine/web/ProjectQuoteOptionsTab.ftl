<div class="panes-container quote_options_tab" xmlns="http://www.w3.org/1999/html">
    <div class="leftPaneContainer">
        <div class="expedioQuote">
            <label>Expedio Quote IDs: </label>
            <@cc.select id="expedioQuoteId">
            <#list view.expedioQuoteId as QuoteId>
                 <option value="${QuoteId}"<#if QuoteId = view.projectId!>selected</#if>>${QuoteId}-${view.expedioQuoteName[QuoteId_index]}</option>
            </#list>
        </@cc.select>
<input type="button"
       launchQuoteUri="${view.productConfiguratorUriFactory.getQuoteLaunchUri(view.customerId,view.contractId,view.projectId)}"
       rel="launchQuote" class="launchQuote" value="OK"/>
<input type="image" id="viewConfigurationLink" src="/rsqe/project-engine/static/images/view_config.png" href="#" title="Detailed Quote View"/>
        </div>

<div class="rsqe-nav">


<#function ifOrderSubmitted options>
<#list options as option>
<#if option.status=="Complete" || (option.status=="Order Submitted" || option.status="Order Created")  >
<#return true>
</#if>
</#list>
<#return false>
</#function>

<!--
<#if  ifOrderSubmitted(view.quoteOptions) && '${allowQuoteCreation}'=="false">
<a  class="main-action disabled" title="You cannot create a new quote option now as an order is already in Progress/Complete. Please create a new Quote in Expedio/CQM�" href="#">Create Quote Option</a>
<#else>
<a id="newQuoteOptionLink" class="main-action"  href="#">Create Quote Option</a>
</#if>
-->

<a id="newQuoteOptionLink" class="main-action"  href="#">Create Quote Option</a>


</div>
<div id="commentsDialog"></div>
<div class="data-Container border">
<#if (view.validationMessages?size > 0)>
<div id="validationMessages">
    <p>One or more errors may prevent this order from being submitted:</p>
    <ul><#list view.validationMessages as message>
        <li>${message}</li>
    </#list></ul>
</div>
        </#if>
<div id="commonError" class="error hidden"></div>
<div id="successMessage" class="successmessage hidden"></div>
<table cellpadding="0" cellspacing="0" id="quoteOptionTable">
<thead>
<tr>
    <th>Quote Option ID</th>
    <th>Quote Option Name</th>
    <th>Date Created</th>
    <th>Created By</th>
    <th>Currency</th>
    <th>Discount Status</th>
    <th>Status</th>
    <th>IFC Pending</th>
    <th>Migration</th>
    <th class="uri"></th>
    <th>Action</th>
</tr>
</thead>
<tbody>
<#list view.quoteOptions as quoteOption>
<#assign trCss = (quoteOption_index % 2 == 0)?string("odd","even")>
<#assign deleteCss = (quoteOption.isDeletabled())?string("delete","delete-disabled")>
<#assign editCss = (quoteOption.isEditAllowed())?string("edit","edit-disabled")>
<#assign editButtonTitle = (quoteOption.isEditAllowed())?string("Edit","Contract Term update is allowed only for Draft Quotes and Provide Q2O journey")>
<tr class="quoteOption ${trCss}" id="id_${quoteOption.id}">
    <td class="id"><#if quoteOption.friendlyId??>${quoteOption.friendlyId}<#else>${quoteOption.id}</#if></td>
<td class="name">${quoteOption.name}</td>
<td class="creationDate">${quoteOption.creationDate}</td>
<td class="createdBy"><#if quoteOption.createdBy??>${quoteOption.createdBy}</#if></td>
<td class="currency">${quoteOption.currency}</td>
<td class="status">
    ${quoteOption.discountStatus}
    <br>
        <#if permissions.bcmAccess && quoteOption.discountApprovalRequested >

        <a href="${quoteOption.bcmUri}?newBcmExportVersion=no" data-id="${quoteOption.id}" rel="bcmExport" class="action"><img
                src="/rsqe/project-engine/static/images/page_white_put.png" title="Connect Applications BCM Export"
                alt="Connect Applications BCM Export"/></a>

        <a href="${quoteOption.bcmUri}?newBcmExportVersion=yes" data-id="${quoteOption.id}" rel="bcmExport" class="action"><img
                src="/rsqe/project-engine/static/images/page_white_put.png" title="BCM Export (excl. Connect Applications)" alt="BCM Export (excl. Connect Applications)"/></a>


        <a href="${quoteOption.bcmUri}" data-id="${quoteOption.id}" action="BcmImportDialog" id = "bcmImportForm" rel="bcmImport" class="action bcmImport"> <img
                src="/rsqe/project-engine/static/images/page_white_get.png" title="BCM Import" alt="BCM Import"/></a>



        <a id="discountApprove" href="#" action="${quoteOption.bcmUri}/commentsandcaveats" data-id="${quoteOption.id}" rel="bcmApproveDiscounts" class="action bcmApprove">
            <img src="/rsqe/project-engine/static/images/accept.png" title="BCM Approve Discounts"
                 alt="BCM Approve Discounts"/></a>


        <a id="discountReject" href="#" action="${quoteOption.bcmUri}/commentsandcaveats" data-id="${quoteOption.id}" rel="bcmRejectDiscounts"
           class="action bcmReject"><img src="/rsqe/project-engine/static/images/cancel.png"
                                         title="BCM Reject Discounts"
                                         alt="BCM Reject Discounts"/></a>
    </#if>



</td>
<td class="status">${quoteOption.status}</td>
<td class="ifcPending"><#if quoteOption.ifcPending>Yes<#else>No</#if></td>
<td class="migrationQuote"><#if quoteOption.migrationQuote>Yes<#else>No</#if></td>
<td class="uri">${quoteOption.uri}</td>
<td class="actions ${permissions.bcmAccess?string("bid_mgr","")}">
<#if quoteOption.hasQuoteOptionNotes>
<a href="#" data-id="${quoteOption.id}" rel="note" class="action">View</a>
<#else>
<a href="#" data-id="${quoteOption.id}" rel="note" class="action"><img
        src="/rsqe/project-engine/static/images/note_add.png"
        title="Note" alt="Note"/></a>
        </#if>

<#assign editQuoteId = (quoteOption.isEditAllowed())?string(quoteOption.id,"")>
<a href="#" rel="edit" data-id="${editQuoteId}" class="action">
<img class="${editCss}" src="/rsqe/project-engine/static/images/page_edit.png" title="${editButtonTitle}" alt="Edit"/>
</a>
<a href="#" rel="delete">
<img class="${deleteCss}" src="/rsqe/project-engine/static/images/delete.png" title="Delete Quote Option"
     alt="Delete Quote Option" data-id="${quoteOption.id}"/>
</a>
        </td>
        </tr>
        </#list>
        </tbody>
        </table>
        </div>
        </div>
        </div>
<div id="newQuoteOptionDialog"></div>
<div id="viewConfigurationDialog"></div>
<div id="editQuoteOptionDialog"></div>
<div id="notesDialog"></div>
<div class="hidden" id="quoteOptionDialogUri">${view.quoteOptionDialogUri}</div>
<div class="hidden" id="notesDialogUri">${view.notesDialogUri}</div>
<div class="hidden" id="deleteQuoteOptionUri">${view.deleteQuoteUri}</div>
<@cc.progressDialog id="progressDialog"; for>
<@content for=="main">
<div>
<div id="progressText">
</div>
</div>
<div id="spinning">
<img src="/rsqe/project-engine/static/images/spinning.gif"/>
</div>
<input type="button" id="progressButton" class="ok" value="Ok"/>
        </@content>
        </@cc.progressDialog>


<@cc.dialog id="confirmationDialog"; for>
<@content for=="main">
<div>
<span>Are you sure you want to proceed?</span>
<br/>
<input type="radio" id="confirmationDialogYesOption" name="confirmationDialogOption" value="Yes" /> Yes
<br/>
<input type="radio" id="confirmationDialogNoOption" name="confirmationDialogOption" checked="checked" value="No" /> No
<br/>
</div>

        </@content>
<@content for=="buttons">
<input type="button" id="dialogOkButton" value="Ok"/>
        </@content>
        </@cc.dialog>

<@cc.dialog id="deleteQuoteOptionDialog"; for>
<@content for=="main">
<div>
<span>Are you sure you want delete the quote?</span>
<br/>
</div>

        </@content>
<@content for=="buttons">
<input type="button" class="button" id="deleteDialogOkButton" value="Ok"/>
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