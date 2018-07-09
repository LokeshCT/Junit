<@layout.default ; for>
<@content for=="title">${view.title}</@content>
<@content for=="head">
</@content>
<@content for=="body">
<div id="header">
    <h1>
    ${view.header}
    </h1>
</div>
<div id="content">
    <hr/>
    <form>
        <table id='connections' class="data-uplift-input-table">
            <tbody>
            <tr>
                <td>Uplift By</td>
                <td style="width=200px;"><select id="upliftBy">
                    <option value="-1"></option>
                    <option value="assetId">Asset Id</option>
                    <option value="lineItemId">LineItem Id</option>
                    <option value="quoteOptionId">Quote Option Id</option>
                    <option value="productAttributeName">Attribute Name</option>
                </select></td>
            </tr>
            <tr class="assetIdRow">
                <td>Asset Id :</td>
                <td style="width=200px;"><input class="" id="assetId"/></td>
                <td>Version :</td>
                <td><input style="width: 40px;" id="assetVersion" value="1"/></td>
            </tr>
            <tr class="lineItemIdRow">
                <td>Line Item Id :</td>
                <td><input class="" id="lineItemId"/></td>
            </tr>
            <tr class="quoteOptionIdRow">
                <td>Project Id :</td>
                <td><input class="" id="projectId"/></td>
                <td>Quote Option Id :</td>
                <td><input class="" id="quoteOptionId"/></td>
            </tr>
            <tr class="productAttributeRow">
                <td>Product Code :</td>
                <td><input class="" id="sCode"/></td>
                <td>Attribute Name :</td>
                <td><input class="" id="attributeName"/></td>
            </tr>
            <tr class="upliftBtnRow">
                <td></td>
                <td><input type="button" id="uplift" value="Uplift"/></td>
            </tr>
            </tbody>
        </table>
    </form>
    <div class="uplift-response">
        <hr/>
        <h2>Uplift Result :</h2>
        <div class="uplift-response-table"></div>
    </div>

    <input type="hidden" id="assetUpliftUrl" value="${assetUpliftUrl}"/>
    <input type="hidden" id="lineItemUpliftUrl" value="${lineItemUpliftUrl}"/>
    <input type="hidden" id="quoteOptionUpliftUrl" value="${quoteOptionUpliftUrl}"/>
    <input type="hidden" id="productAttributeUpliftUrl" value="${productAttributeUpliftUrl}"/>
</div>
</@content>
</@layout.default>
