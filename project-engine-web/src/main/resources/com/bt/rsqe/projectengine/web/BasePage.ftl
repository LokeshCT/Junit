<@layout.default ; for>
<@content for=="title">${view.title}</@content>
<@content for=="head">

    ${additionalImport!}
        <script type="text/javascript">
            var global = {basePage : null};
            $(function() {
                global.basePage = new rsqe.BasePage();
                global.basePage.initialise();
            });
        </script>

</@content>

<@content for=="body">
<div id="hideScreen">
<div id="loadingMessage" class="dataTables_processing" style="display:none;">Loading...</div>

    <div id="header">
        <img src="/rsqe/project-engine/static/images/mx_banner.jpg" alt="${view.header}" />
    </div>

    <div id="breadCrumb">
        <span>
            <#list view.breadCrumbs as breadCrumb>
                <a href="${breadCrumb.uri}">${breadCrumb.displayText}</a> >
            </#list>
            ${view.header}
            <#if quoteDetails??>
                <span class="additionalInfo infoContainer"><b>Customer: </b><span id="customerDetails">${customerDetails.name}</span> &nbsp;&nbsp;<b>Quote Option ID: </b><span id="quoteDetailsId"><#if quoteDetails.friendlyQuoteId??>${quoteDetails.friendlyQuoteId}<#else>${quoteDetails.id}</#if></span> &nbsp;&nbsp;<b>Quote Option Name: </b><span id="quoteDetailsName">${quoteDetails.name}</span> &nbsp;&nbsp;<b>Currency: </b><span id="quoteDetailsCurrency">${quoteDetails.currency}</span>
                &nbsp;&nbsp;&nbsp;<span><a target="_blank" href="${helpLinkUri}"><b>Help?</b></a></span></span>
            <#elseif customerDetails??>
                <span class="additionalInfo infoContainer"><b>Customer: </b>${customerDetails.name}
                    &nbsp;&nbsp;&nbsp;<span><a target="_blank" href="${helpLinkUri}"><b>Help?</b></a></span></span></span>
            </#if>
        </span>
    </div>

    <div id="content">

        <div id="overallError" class="hidden commonError"></div>
        <div id="overallMessage" class="hidden successmessage"></div>

        <ul id="tabs">
            <#list view.tabs as tab>
                <li>
                    <a href="${tab.uri}"
                       id="${tab.tabClass}"
                       class="${tab.tabClass}">${tab.label}
                    </a>
                </li>
            </#list>
        </ul>

    </div>

    <input id="submitWebMetricsUri" type="hidden" value="${submitWebMetricsUri}">
    <input id="viewConfigurationDialogUri" type="hidden" value="${viewConfigurationDialogUri}">

</div>
</@content>
</@layout.default>

