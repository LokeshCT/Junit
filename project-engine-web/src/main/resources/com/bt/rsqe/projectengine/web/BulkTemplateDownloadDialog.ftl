<@cc.dialog ; for>
    <@content for=="main">
        <@cc.product_list products=view.products
        selected=view.getBulkTemplateUri(view.defaultSCode);  productId>
        ${view.getBulkTemplateUri(productId)}
        </@cc.product_list>
    </@content>
    <@content for=="buttons">
    <a class="downloadForProduct actionbutton" href="#" id="downloadForProduct">Download</a>
    </@content>
</@cc.dialog>
