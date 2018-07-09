<html>
<head>
    <title>View Configuration</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <#include "./ScriptsAndCss.ftl">
</head>
<body onload="loadViewConfiguration();">
    <@cc.dialog ; for >
    <@content for=="main">
    <form id="viewConfigurationForm" name="create.quote.option.form" method="POST" action="${view.formAction}">
        <input type="hidden" name="viewConfigurationURI" id="viewConfigurationURI" value="${view.viewConfigurationURI}"/>
        <div>
            <table class="treeTable">
                <tbody>
                <tr>
                    <td>
                        <label>Quote Name :</label>&nbsp;&nbsp;
                    </td>
                    <td>
                        <input type="text" name="quoteName" id="quoteName" value="${view.quoteName}" readonly="true" class="readonly"/>
                    </td>
                </tr>
                <tr>
                    <td>
                        <label>Expedio Reference :</label>&nbsp;&nbsp;
                    </td>
                    <td>
                        <input type="text" name="expRef" id="expRef" value="${view.expRef}" readonly="true" class="readonly"/>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
        <div>
            <div>
                <table class="treeTable">
                    <tr>
                        <td>
                            <label>Quote Option :</label>
                            <@cc.select id="quoteOptionFilter">
                                <#list view.quoteOptions as quoteOption>
                                    <option value="${quoteOption.id}" <#if (view.quoteOptions?size==1)>selected="selected" readOnly=true </#if>>${quoteOption.name}</option>
                                </#list>
                            </@cc.select>
                        </td>
                        <td id="offerFilterPanel">
                            <label>Offer :</label>
                            <@cc.select id="offerFilter">
                                <#list view.offers as offer>
                                    <option value="${offer.id}" parentId="${offer.parentId}">${offer.name}</option>
                                </#list>
                            </@cc.select>
                        </td>
                        <td id="orderFilterPanel">
                            <label>Order :</label>
                            <@cc.select id="orderFilter">
                                <#list view.orders as order>
                                    <option value="${order.id}" parentId="${order.parentId}">${order.name}</option>
                            </#list>
                            </@cc.select>
                        </td>
                        <td>
                            <div>
                                <input type="button" id="goButton" class="button" value="Go"/>
                            </div>
                        </td>
                    </tr>
                </table>
            </div>
            <div>
                <input type="radio" id="productsBySiteRadio" checked="true"><label class="radioLabel">Products By Site View</label></input>
                &nbsp &nbsp &nbsp
                <input type="radio" id="sitesByProductRadio"><label class="radioLabel">Sites By Product View</label></input>
            </div>
            <div id="loading-data-spinner" class="dataTables_processing">Loading...</div>
            <div id="configurationTree" class="treeContainer"></div>
            <div class="hidden" id="errorMessage">
                <ul class="warning-message">
                    <li>
                        <span>Error while loading the products tree!!! Please check logs for more info.</span>
                    </li>
                </ul>
            </div>
        </div>
    </form>
    </@content>
    </@cc.dialog>
</body>
</html>


