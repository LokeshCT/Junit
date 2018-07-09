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
    <#list grabstateArchives as archive>
        <tr>
            <td>${archive}</td><td><a href="/rsqe/inlife/grabstate-archives/${archive}">${archive}</a></td>
        </tr>
    </#list>
    </table>
</div>
</@content>
</@layout.default>
