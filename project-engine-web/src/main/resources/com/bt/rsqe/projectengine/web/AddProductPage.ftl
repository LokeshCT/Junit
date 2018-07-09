<div id="${view.productAction}-product" class="addProduct">

    <div style="display:none; line-height:17px;"  title="Bulk Template Download" id="export-bulk-template-msg">
        <p>Your bulk template is being generated and will be available to view shortly.</p>
    </div>

    <div id="commonError" class="error hidden"></div>
    <ul class="select-product form">
        <div>
            <table id="addProductTable" class="floatLeft">
                <tr>
                    <td>
                        <label>Product Family:</label>
                    </td>
                    <td class="tdDisplay">
                        <@cc.select id="categoryGroupFilter">
                            <#list view.categoryGroups as category>
                                <option value="${category.id}">${category.name}</option>
                            </#list>
                        </@cc.select>
                    </td>
                    <td>
                    <input type="text" id="categoryGroupSearch" placeholder="Please type here" class="textStyle">
                    <input type="hidden" id="categoryGroup-id">
                    <input type="hidden" id="categoryGroupUrl">
                    </td>
                </tr>
                <tr id="categoryFilterPanel">
                    <td>
                        <label>Product Variant:</label>
                    </td>
                    <td class="tdDisplay">
                        <@cc.select id="categoryFilter">
                            <#list view.categories as category>
                                <option value="${category.id}" orderPreRequisiteUrl="${category.orderPreRequisiteUrl}">${category.name}</option>
                            </#list>
                        </@cc.select>
                            <a id="helpLink" href="" target="_blank">
                                <img src="/rsqe/project-engine/static/images/helpLink.png" alt="help link"/>
                            </a>
                    </td>
                    <td>
                    <input type="text" id="categorySearch" placeholder="Please type here" class="textStyle">
                    <input type="hidden" id="category-id">
                    <input type="hidden" id="categoryUrl">
                    </td>
                </tr>
                <tr id="productFilterPanel">
                    <td>
                        <label>Product Offering:</label>
                    </td>
                    <td class="tdDisplay">
                        <@cc.product_list view.products;  productId, productCategoryCode>
                            ${view.productJson(productId, productCategoryCode)}
                        </@cc.product_list>
                    </td>
                     <td>
                     <input type="text" id="productSearch" placeholder="Please type here" class="textStyle">
                     <input type="hidden" id="productSearch-id">
                     <input type="hidden" id="productSearchUrl">
                     </td>
                </tr>
                <tr id="filterPanel">
                    <td>
                        <label>Country:</label>
                    </td>
                    <td>
                        <@cc.countrySelect id="countryFilter">
                            <option value="all">All Countries</option>
                            <#list view.countries as country>
                                <option value="${country}">${country}</option>
                            </#list>
                        </@cc.countrySelect>

                    </td>
                </tr>
                <tr id="numberOfProductsPanel" style="display: none;">
                    <td>
                        <label>Number Of Products:</label>
                    </td>
                    <td>
                        <input id="numberOfProducts" value="1" onkeypress='return event.charCode >= 48 && event.charCode <= 57'>
                    </td>
                </tr>
            <#if view.productAction == "Modify">
                <tr id="resignPanel">
                    <td>
                        <label>Contract Resign</label>
                    </td>
                    <td class="noBorder checkbox">
                        <input type="checkbox" id="selectResignCheckBox" title="Select Resign"/>
                    </td>
                </tr>
            </#if>
            </table>
            <div class="productCounterWrapper floatLeft"><p>Number of quote items</p><p id="productCounter">${view.quoteOptionItemsSize}</p></div>
        </div>
        <div id="complianceCheckPanel" style="margin: 0px 0 0 !important; clear: both">
            <label>${view.complianceCheckMessage}</label>
            <input id="complianceCheckBox" type="checkbox" />
            <a id="prerequisiteUrl" href="#" target="blank">Prerequisites URL</a>
        </div>
        <div class="clearBoth">
            <li class="addproductactions">
                <#switch view.productAction>
                    <#case "Add">
                        <input type="button" id="submit" class="submit disabled" value="Add Product"
                                title="Add Product" disabled="disabled"/>
                    <#break>
                    <#case "Modify">
                        <input type="button" id="submit" class="submit disabled" value="Remove/Modify Product"
                                title="Remove/Modify selected product" disabled="disabled"/>
                        <#break>
                    <#case "Move">
                        <input type="button" id="submit" class="submit disabled" value="Move Product"
                                title="Move selected product" disabled="disabled"/>
                        <#break>
                    <#case "Migrate">
                        <input type="button" id="submit" class="submit disabled" value="Migrate Product"
                                title="Migrate selected product" disabled="disabled"/>
                        <#break>
                </#switch>
            </li>

            <li class="addproductactions"><a href="${view.productConfiguratorUriFactory.getBulkViewUri(view.customerId,view.contractId,view.projectId,view.quoteOptionId)?html}&noFilter=1" id="bulkConfigurationButton" class="main-action" onclick="return disableLink(this)">Configure Product</a></li>
            <li class="addproductactions"><a href="${view.redirectUri}" class="main-action" id="continue-to-quote-details">Continue to Quote Option Details</a></li>


            <div class="clearBoth" id="addImportContainer">
                <div class="importBtnTitle" style="padding: 5px;">This product supports bulk import</div>

                <#if view.productAction=="Add">
                    <input type="button" id="bulkTemplateExport" class="main-action" value="Export Bulk Template">
                </#if>

                <a href="#" class="main-action" id="addImportButton">Import Product</a>
            </div>

               <div class="clearBoth" id="addUserImportContainer">
               <div> <span><a id="hideButtons" class="hideButtons" style="background-position:0px -195px;"></a>This product supports bulk import</span></div>

            <#if (view.productAction=="Add" || view.productAction=="Modify")>
                <input type="button" id="bulkTemplateUserExport" class="main-action hidden" value="Export Bulk Template">
            </#if>

                <a href="#" class="main-action hidden" id="addUserImportButton">Import Product</a>
            </div>

        </div>


        <li>
            <input class="orderType" type="radio" name="orderType" value="Provide"  checked="checked" style="display: none" />
           <#-- <label>Order Type:</label>
            <input class="orderType" type="radio" name="orderType" value="Provide" checked="checked">Provide</input>-->
        <#-- Toy 06/02/12 Hide these options for now -->
        <#--<input class="orderType" type="radio" name="orderType" value="Modify">Modify</input>-->
        <#--<input class="orderType" type="radio" name="orderType" value="Cease">Cease</input>-->
        </li>
    </ul>

    <form id="itemCreationForm" method="post" action="#">
        <input type="hidden" class="quoteOptionContext" name="quoteOptionContext" value=""/>

    </form>

    <input type="hidden" class="rsqeQuoteOptionId" name="rsqeQuoteOptionId" id="rsqeQuoteOptionId" value="${view.quoteOptionId}"/>
    <input type="hidden" class="rsqeQuoteOptionName" name="rsqeQuoteOptionName" id="rsqeQuoteOptionName" value="${view.name}"/>
    <input type="hidden" class="rsqeQuoteOptionCurrency" name="rsqeQuoteOptionCurrency" id="rsqeQuoteOptionCurrency"
           value="${view.currency}"/>
    <input type="hidden" class="expedioQuoteId" name="expedioQuoteId" id="expedioQuoteId" value="${view.projectId}"/>
    <input type="hidden" class="expedioCustomerId" name="expedioCustomerId" id="expedioCustomerId" value="${view.customerId}"/>
    <input type="hidden" class="expedioContractId" name="expedioContractId" id="expedioContractId" value="${view.contractId}"/>
    <input type="hidden" class="orderType" name="orderType" id="orderType" value="${view.orderType}"/>
    <input type="hidden" class="subOrderType" name="subOrderType" id="subOrderType" value="${view.subOrderType}"/>
    <input type="hidden" class="authenticationToken" name="authenticationToken" value="auth token"/>
    <input type="hidden" class="revenueOwner" name="revenueOwner" value="${view.revenueOwner}"/>
    <input type="hidden" class="productSCode" name="productSCode" id="productSCode" value=""/>
    <input type="hidden" class="productVersion" name="productVersion" value=""/>
    <input type="hidden" class="redirectUri" name="redirectUri" value="${view.redirectUri}"/>
    <input type="hidden" class="productAction" name="productAction" id="productAction" value="${view.productAction}"/>
    <input type="hidden" class="getSitesUri" name="getSitesUri" value="${view.getSitesUri}"/>
    <input type="hidden" class="getServicesUri" name="getServicesUri" value="${view.getServicesUri}"/>
    <input type="hidden" class="getLaunchStatusUri" name="getLaunchStatusUri" value="${view.getLaunchStatusUri}"/>
    <input type="hidden" class="getCreateProductUri" name="getCreateProductUri" value="${view.createProductUri}"/>
    <input type="hidden" class="cardinalityCheckUri" name="cardinalityCheckUri" value="${view.cardinalityCheckUri}"/>
    <input type="hidden" class="siteSelectedForProductCheckUri" name="siteSelectedForProductCheckkUri" value="${view.siteSelectedForProductCheckUri}"/>
    <input type="hidden" class="selectNewSiteDialogUri" name="selectNewSiteDialogUri" id="selectNewSiteDialogUri" value="${view.selectNewSiteDialogUri}"/>
    <input type="hidden" class="isProductImportable" name="isProductImportable" value=""/>
    <input type="hidden" class="getProductAttributesUri" name="getProductAttributesUri" value="${view.getProductAttributesUri}"/>
    <input type="hidden" class="bulkTemplateExportUri" name="bulkTemplateExportUri" value="${view.getBulkTemplateExportUri}"/>
    <input type="hidden" class="validateImportProductURL" name="validateImportProductURL" value="${view.validateImportProductURL}"/>
    <input type="hidden" class="validateUserImportProductURL" name="validateUserImportProductURL" value="${view.userImportValidateUri}"/>
    <input type="hidden" class="userImportStatusURL" name="userImportStatusURL" value="${view.userImportStatusUri}"/>
    <input type="hidden" class="productMoveConfigurationType" name="productMoveConfigurationType" value=""/>
    <input type="hidden" class="contractTermForMove" name="contractTermForMove" value=""/>
    <input type="hidden" class="endOfLifeCheckUri" name="endOfLifeCheckUri" value="${view.endOfLifeCheckUri}"/>
    <input type="hidden" class="userExportUri" name="userExportUri" value="${view.userExportUri}"/>
    <input type="hidden" class="userImportUri" name="userExportUri" value="${view.userImportUri}"/>
    <input type="hidden" class="productAvailabilityUri" name="productAvailabilityUri" value="${view.productAvailabilityUri}"/>

    <div class="data-Container border" id="containerBorder">
        <div id="creating-product-spinner" class="dataTables_processing" style="display: none;">Creating product...</div>
        <div class="search-container">
            <input type="text" id="globalSearch" placeholder="Search Sites ..."/>
            <input type="button" id="siteGlobalSearchBtn" class="button" value="Search"/>
            <br>
            <span class="search-input-description" id="quote-option-details-input-search-example-text" style="margin-left:42px; font-weight:normal; display:none;">Search by Site Name or Address</i></span>
            <!-- <span style="font-weight:normal">Example: &lt;Site&gt; && &lt;Address&gt;</span> -->
        </div>
        <table id="siteTable">
            <thead>
            <tr>
                <th class="noBorder checkbox"><input id="selectAll" type="checkbox" title="Select All"/></th>
                <th class="siteHeader">Site Name</th>
                <th class="siteHeader">Site Address</th>
                <#if view.productAction == "Modify">
                    <th class="siteHeader">Summary</th>
                </#if>
            <#if view.productAction == "Move">
                <th class="siteHeader">New Site Name</th>
                <th class="siteHeader">New Site Address</th>
            </#if>
            </tr>
            </thead>
            <tbody>
            </tbody>
        </table>
        </div>
    </div>
    <span class="hidden" id="isIndirectUser">${permissions.indirectUser?string("true","false")}</span>
    <div id="selectNewSiteDialog"></div>
    <@cc.progressDialog id="warningDialog"; for>
    <@content for=="main">
    <div class="commonWarning">
        <span id="warningText"></span>
        <p id="warningMessages" class="hidden"></p>
    </div>
    <br>
    <input type="button" id="continueMove" class="yes button" value="Continue Move"/>
    <input type="button" id="cancelMove" class="no button" value="Cancel"/>
    </@content>
</@cc.progressDialog>

    <@cc.progressDialog id="importProgressDialog"; for>
        <@content for=="main">
        <div class="commonError">
            <span id="progressText"></span>
            <p id="errorMessages" class="hidden"></p>
        </div>
        <div id="spinning">
            <img src="/rsqe/project-engine/static/images/spinning.gif"/>
        </div>
        <input type="button" id="close" style="margin-top: 10px"  class="close button hidden" value="Close"/>
        <input type="button" id="yes" class="yes button hidden" value="Yes"/>
        <input type="button" id="no" class="no button hidden" value="No"/>
        </@content>
    </@cc.progressDialog>

    <@cc.dialog id="addImportProductDialog" ; for>
        <@content for=="main">
        <p>Upload work book with configured products: </p>
        <div>
            <form id="addImportProductForm" action="${view.importProductURL}" enctype="multipart/form-data" method="post" target="bulkTarget">
                <input type="hidden" name="customerId" id="customerId" value="${view.customerId}"/>
                <input type="hidden" name="contractId" id="contractId" value="${view.contractId}"/>
                <input type="hidden" name="projectId" id="projectId" value="${view.projectId}"/>
                <input type="hidden" name="quoteOptionId" id="quoteOptionId" value="${view.quoteOptionId}"/>
                <input type="hidden" name="revenueOwner" id="revenueOwner" value="${view.revenueOwner}"/>
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
        <input id="addImportProductUpload" type="button" class="button" value="Upload"/>
        </@content>
    </@cc.dialog>
    <@cc.dialog id="bulkImportProductDialog" ; for>
        <@content for=="main">
        <p>Upload work book with configured products: </p>
        <div>
            <form id="bulkImportProductForm" action="#" enctype="multipart/form-data" method="post" target="bulkTarget">
                <input type="hidden" name="customerId" id="bulkCustomerId" value="${view.customerId}"/>
                <input type="hidden" name="contractId" id="bulkContractId" value="${view.contractId}"/>
                <input type="hidden" name="projectId" id="bulkProjectId" value="${view.projectId}"/>
                <input type="hidden" name="quoteOptionId" id="bulkQuoteOptionId" value="${view.quoteOptionId}"/>
                <input type="hidden" name="revenueOwner" id="bulkRevenueOwner" value="${view.revenueOwner}"/>
                <input type="hidden" name="quoteOptionContext" id="bulkQuoteOptionContext" value=""/>
                <input type="hidden" name="bulkWorkBookName" id="bulkWorkBookName" value=""/>
                <ul class="form dialogbox importProduct">
                    <li class="filebox">
                        <input id="bulkSheet" type="file" name="bulkSheet" class="file"/>
                    </li>
                </ul>
            </form>
        </div>
        </@content>
        <@content for=="buttons">
        <input id="bulkImportProductUpload" type="button" class="button" value="Upload"/>
        </@content>
    </@cc.dialog>
</div>
