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
    <table>
    <#list boms?keys as bom>
        <tr>
            <td>${boms[bom]?datetime}</td><td><a href="/rsqe/inlife/boms/${bom}">${bom}</a></td>
            <td>
                <button onClick="window.open('/rsqe/inlife/diagnostics/line-item/${bom?substring(bom?index_of("_")+1,(bom?index_of("_")+37))}');">Look-up CIF Detail for this Quote</button>
            </td>
        </tr>
    </#list>
    </table>
</div>
</@content>
</@layout.default>
