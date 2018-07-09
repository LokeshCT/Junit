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
    <form id="lineItem">
        <hr/>
        <h3>Line Item Details</h3>
        <input type="text" id="lineItemId" size="40"/>&nbsp;
        <button onClick="window.open('/rsqe/inlife/diagnostics/line-item/'+this.form.lineItemId.value);">Look-up</button>
        <hr/>
    </form>
</div>
</@content>
</@layout.default>
