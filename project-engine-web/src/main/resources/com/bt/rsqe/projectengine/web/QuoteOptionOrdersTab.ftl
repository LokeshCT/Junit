<div class="">
    <table id="orders">
        <thead>
        <tr>
            <th>Name</th>
            <th>Creation Date</th>
            <th>Status</th>
            <th>Offer Name</th>
            <th>Actions</th>
        </tr>
        </thead>
        <tbody>
        <div id="commonError" class="error hidden"></div>
        <div id="successMessage" class="successmessage hidden"></div>
        <#list view.orders as order>
        <tr class="order">
            <td class="name">
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
            <td class="actions">
                <span id="orderId" class="hidden">${order.id}</span>
                <span id="submitUrl" class="hidden">${order.submitLink}</span>
                <span id="projectId" class="hidden">${view.projectId}</span>
                <span id="quoteOptionId" class="hidden">${view.quoteOptionId}</span>
                <a id="submitOrder" class="submitOrder main-action" href="#">Submit Order</a>
                <a href="${order.exportRFOLink}" rel="rfoImport" class="exportRFO">
                    <img src="/rsqe/project-engine/static/images/page_white_put.png" title="Export RFO" alt="Export RFO"/>
                </a>
                <#if order.isRFOExportable >
                    <a href="#" rel="rfoImport" class="importRFO">
                        <img src="/rsqe/project-engine/static/images/page_white_get.png" title="Import RFO" alt="Import RFO"/>
                    </a>
                </#if>
            </td>
        </tr>
        </#list>
        </tbody>
    </table>
</div>
