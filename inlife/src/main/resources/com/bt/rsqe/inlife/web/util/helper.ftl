<#macro attr attrs>
    <#list attrs?keys as key>
        <#if attrs[key]!="">
        ${key}="${attrs[key]}"
        </#if>
    </#list>
</#macro>
