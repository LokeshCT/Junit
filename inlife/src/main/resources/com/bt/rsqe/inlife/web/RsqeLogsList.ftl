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
    <#list rsqeLogs as log>
        <tr>
            <td>${log.name}</td>
            <td>${log.lastModified}</td>
            <td>${log.humanReadableLength}</td>
            <td><a href="/rsqe/inlife/rsqe-logs/${log.name}">${log.name}</a></td>
        </tr>
    </#list>
    </table>
</div>
</@content>
</@layout.default>
