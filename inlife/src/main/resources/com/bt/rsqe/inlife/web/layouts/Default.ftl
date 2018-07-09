<#macro default>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
    <title><#nested "title"></title>
    <meta http-equiv="X-UA-Compatible" content="IE=Edge" />
    <meta http-equiv="Content-type" content="text/html;charset=UTF-8"/>
    <#include "../ScriptsAndCss.ftl">
    <#nested "head">
</head>
<body>
<div id="container">
    <#nested "body">
</div>
</body>
</html>
</#macro>
