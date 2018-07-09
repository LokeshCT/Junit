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
    <#list apacheLogs as log>
        <tr>
            <td>${log}</td><td><a href="/rsqe/inlife/apache-logs/${log}">${log}</a></td>
        </tr>
    </#list>
    </table>
</div>
</@content>
</@layout.default>
