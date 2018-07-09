<#macro layout>
<head>
    <#nested "head">
</head>
<body>
    <#nested "body">
</body>
</#macro>

<@layout ; for>
    <@content for=="head">
    head content
    </@content>
    <@content for=="body">
    body content
    </@content>
</@layout>