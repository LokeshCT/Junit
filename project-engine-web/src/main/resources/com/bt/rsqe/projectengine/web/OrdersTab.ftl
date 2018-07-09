<div class="orders_tab">

    <div id="filterPane" class="filterPaneContainer" style="min-height:23px;"></div>
    <div style="display:none; line-height:17px;"  title="RFO Sheet Download" id="export-rfo-sheet-msg">
        <p>Your RFO sheet is being generated and will be available to view shortly.</p>
    </div>
    <div id="sitesDetailList" class="data-Container border">
         <table id="orders">
            <thead>
            <tr>
                <th>Name</th>
                <th>Creation Date</th>
            <th>Status</th>
                <th>Offer Name</th>
                <#if (view.enableBomDownload && (!permissions.indirectUser || view.userToken=='BID_MGR'))>
                    <th>Download Bill of Materials</th>
                </#if>
                <th>Configured</th>
                <th>Actions</th>
            </tr>
            </thead>
            <tbody>
            <div id="commonError" class="error hidden"></div>
            <div id="successMessage" class="successmessage hidden"></div>
            <#if view.siteErrorNotification != ''>
            <div id="siteOrderError" class="commonError">${view.siteErrorNotification}</div>
            <div id="siteErrorButton">
                <ul class="ul_button_site">
                    <li id="orderDetail" class="siteError_inactive_btn">Order Details</li>
                    <li id="sitesErrorDetail" class="siteError_active_btn">Site(s) in Error</li>
                </ul>
            </div>

            </#if>
            <#list view.orders as order>
            <tr class="order">

               <td class="name" style="<#list order.orderItemSites as orderSite><#if orderSite.siteId?has_content><#list view.siteDetail as siteForOrder><#if  orderSite.siteId == siteForOrder.getSiteId() &&  ! siteForOrder.asciiCharFlag>background-color: #ffff00;<#assign nonIso=true></#if></#list></#if></#list>">
                ${order.name}
                </td>
                <td class="created">
                ${order.created}
                </td>
                <td class="status">
                ${order.status}
                </td>
                <td class="offerName">
                ${order.offerName}
                </td>
                <#if (order.isAllowBomDownload() && (!permissions.indirectUser || order.userToken=='BID_MGR'))>
                    <td class="downloadBomXmls">
                        <#if (order.isRfoValid())>
                            <a href="${order.bomXml}" id="downloadBomXml"  class="downloadBomXml">
                                <img src="/rsqe/project-engine/static/images/page_white_put.png" title="Download Bill of Materials" alt="Download Bill of Materials"/>
                            </a>
                        </#if>
                    </td>
                </#if>
            <#assign isRfoValidCheck=order.isRfoValid()>
                <td class="validity ${isRfoValidCheck?string("valid","invalid")}">
                ${isRfoValidCheck?string("RFO valid","RFO invalid")}
                </td>
                <td class="actions">
                    <#assign isSubmittedDisabled=order.isSubmitButtonDisabled()>
                    <#assign allowCancelOrder=!(order.allowCancelOrder())>
                    <a class="submitOrder main-action ${isSubmittedDisabled?string("disabled","")}"  ${isSubmittedDisabled?string("disabled='disabled'","")} href="#">Submit Order</a>
                    <a class="cancelOrder main-action ${allowCancelOrder?string("disabled","")}"  ${allowCancelOrder?string("disabled='disabled'","")}
                       href="#" >Delete Order</a>
                    <span class="orderId hidden">${order.id}</span>
                    <span class="submitUrl hidden">${order.submitLink}</span>
                    <span class="statusUrl hidden">${order.orderStatusLink}</span>
                    <span class="cancelUrl hidden">${order.cancelOrderLink}</span>
                    <span class="projectId hidden">${view.projectId}</span>
                    <span class="migrationQuote hidden"><#if order.migrationQuote>Yes<#else>No</#if></span>
                    <span class="quoteOptionId hidden">${view.quoteOptionId}</span>
                    <a href="${order.linkRFO}" id="rfoExport" rel="rfoImport" class="exportRFOSheet"
                       validateRFOExportUri="${order.linkRFO}/validate">
                        <img src="/rsqe/project-engine/static/images/page_white_put.png" title="Export RFO" alt="Export RFO"/>
                    </a>
                    <#if order.isRFOExportable >
                        <a href="${order.linkRFO}" id="rfoImport" rel="rfoImport" class="importRFO">
                            <img src="/rsqe/project-engine/static/images/page_white_get.png" title="Import RFO" alt="Import RFO"/>
                        </a>
                    </#if>
                </td>
            </tr>
            </#list>
            </tbody>
        </table>
    </div>
    <div id="sitesWithError" class="data-Container border hidden">
        <table id="sites">
            <thead>
            <tr>
                <th>Site Name</th>
                <th>Site Address</th>
                <th>Order</th>
                <th>Edit Site Details</th>
            </tr>
            </thead>
            <tbody>
            <div id="commonSiteError" class="error hidden"></div>
            <div id="siteErrorButton">
                <ul class="ul_button_site">
                    <li id="orderDetailTab" class="siteError_active_btn">Order Details</li>
                    <li id="sitesErrorDetailTab" class="siteError_inactive_btn">Site(s) in Error</li>
                </ul>
            </div>
            <#list view.siteDetail as site >
            <tr class="site">
                <td class="siteName">
                ${site.siteName}
                </td>
                <td class="siteAddress">
                ${site.locationId}, ${site.floor}, ${site.building}, ${site.streetName}, ${site.city}, ${site.country}
                </td>
                <td class="orderList">
                    order details
                </td>
                <td class="editSiteDetail">
                    <div  class="link-block" id="siteForEdit">
                        Click here to Edit Site</div>
                   <span class="siteIdVal hidden">${site.siteId}</span>
                   <span class="siteType hidden">${site.siteType}</span>
                    <input type="hidden" id="asciiCharFlag" value="${site.asciiCharFlag?string('true','false')}"/>
                </td>

            </tr>
            </#list>
            <input type="hidden" id="orderUrl" value="<#if view.ordersLink?has_content>${view.ordersLink}</#if>"/>
            <input type="hidden" id="siteCustomerId" value="<#if view.customerId?has_content>${view.customerId}</#if>"/>
            </tbody>

        </table>

    </div>
</div>

<@cc.dialog id="rfoImportDialog" ; for>
    <@content for=="main">
        <form id="rfoImportForm" action="#" enctype="multipart/form-data" method="post" target="rfoTarget">
            <ul class="form dialogbox rfoImport">
                <li class="filebox">
                    <label for="rfoSheet">File : </label>

                    <div class="fields">
                        <input type="file" name="rfoSheet" id="rfoSheet" class="rfoSheet"/>
                    </div>
                </li>
            </ul>
            <iframe id="rfoTarget" name="rfoTarget" src="about:blank">To force template engine to render non-empty iframe tag and generate valid
                XHTML
            </iframe>
        </form>
    </@content>
    <@content for=="buttons">
        <input type="button" id="uploadButton" class="submit" value="Upload"/>
    </@content>
</@cc.dialog>

<@cc.progressDialog id="migrationConfirmationDialog"; for>
    <@content for=="main">
    <img src="/rsqe/project-engine/static/images/warning.png" title="Ceased Existing Inventory?" alt="Ceased Existing Inventory?"/>
    <div class="migration_confirmation_question_container">
        <span>Have you ceased existing inventory for the customer?</span>
        <span>Progressing without ceasing existing inventory will result in failure of this migration order.</span>
    </div>
    <div class="migration_confirmation_buttons_container">
    <input type="button" id="migrationYesButton" class="submit" value="Yes"/>
    <input type="button" id="migrationNoButton" class="submit" value="No"/>
    </div>
    </@content>
</@cc.progressDialog>

<@cc.progressDialog id="migrationCancelledInfoDialog"; for>
    <@content for=="main">
        <div class="migration_cancelled_info_container">
            <span>This order has not been submitted.</span>
            <span>Please return to this order and re-submit after existing inventory has been ceased.</span>
        </div>
        <div class="migration_cancelled_close_button_container">
        <input type="button" id="migrationCancellationInfoDialogCloseButton" class="close" value="Close"/>
        </div>
    </@content>
</@cc.progressDialog>

<@cc.progressDialog id="progressDialog"; for>
    <@content for=="main">
        <div>
            <label id="progressText"></label>
        </div>
        <div id="spinning">
            <img src="/rsqe/project-engine/static/images/spinning.gif"/>
        </div>
    </@content>
</@cc.progressDialog>

<@cc.progressDialog id="submitOrderProgressDialog"; for>
    <@content for=="main">
        <div>
            <label id="submitOrderProgressText"></label>
        </div>
        <div align="center" id="spinning">
            <img src="/rsqe/project-engine/static/images/spinning.gif"/>
        </div>
    </@content>
</@cc.progressDialog>

<@cc.dialog id="confirmationDialog"; for>
<@content for=="main">
<div>
    <span>Are you sure you want to Delete the Order?</span>
    <br/>
<div>
</@content>
    <@content for=="buttons">
    <input type="button" id="confirmationDialogYesButton" class="submit" value="Ok"/>
</@content>
</@cc.dialog>

<@cc.dialog id="updateSiteDialog" ; for>
    <@content for=="main">
   <div id="siteData"></div>
    </@content>
<@content for=="buttons">
    <input type="button" id="siteUpdateButton" class="submit" value="Update Site"/>
</@content>
</@cc.dialog>
