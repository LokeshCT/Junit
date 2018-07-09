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
    <#if (enabled)>
    <table>
    <tr>
    <th>Capability</th>
    <th>Description</th>
    <th>Value</th>
    <th>Action</th>
    </tr>
    <#list applicationCapabilityInfoList as prop>
        <tr class="alternate">
            <td>${prop.name}</td>
            <td>${prop.description}</td>
            <td><#if (prop.value)??>${prop.value?string("yes","no")}<#else>undefined</#if></td>
            <td>
            <a href="${basePath}/${prop.name}/true">Set</a>
            <a href="${basePath}/${prop.name}/false">Clear</a>
            </td>
        </tr>
    </#list>
    </table>
    <#else>
    <p>Capability app not enabled</p>
    </#if>
</div>
</@content>
</@layout.default>
