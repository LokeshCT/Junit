<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <title>Jasmine Test Runner</title>
    <link href="styles/jquery-ui-1.8.17.custom.css" type="text/css" rel="stylesheet"/>
    <link rel="stylesheet" type="text/css" href="lib/jasmine/jasmine.css">
    <script type="text/javascript" src="lib/jasmine/jasmine.js"></script>
    <script type="text/javascript" src="lib/jasmine/jasmine-html.js"></script>
    <script type="text/javascript" src="lib/jasmine/jasmine.teamcity_reporter.js"></script>
    <script type="text/javascript" src="lib/jquery-1.7.1.js"></script>
    <script type="text/javascript" src="lib/jquery-ui-1.8.16.custom.js"></script>
    <script type="text/javascript" src="lib/select2.js"></script>
    <script type="text/javascript" src="lib/underscore-1.4.1.js"></script>
    <script type="text/javascript" src="lib/jquery.json-2.3.js"></script>
    <script type="text/javascript" src="lib/jquery.form.js"></script>
    <script type="text/javascript" src="lib/jquery.validate.js"></script>
    <script type="text/javascript" src="lib/jquery.jeditable.js"></script>
    <script type="text/javascript" src="lib/jquery.dataTables.js"></script>
    <script type="text/javascript" src="scripts/BasePage.js"></script>
    <script type="text/javascript" src="scripts/rsqe.js"></script>
    <script type="text/javascript" src="scripts/Dialog.js"></script>
    <script type="text/javascript" src="scripts/Disableable.js"></script>
<#list model.jsIncludes as path_to_js>
    <script type="text/javascript" src="${path_to_js}"></script>
</#list>
</head>
<body>
<div id="testContainer">
${model.htmlUnderTest}
</div>
<script type="text/javascript">
    window.console = window.console || {log:function() {}};
    window.alert = function(args) {

        console.log(args);
    };
    var staticConfig = staticConfig || {};
    staticConfig.dateFormat = {};
    jasmine.getEnv().addReporter(new jasmine.TrivialReporter());
    jasmine.getEnv().addReporter(new jasmine.TeamcityReporter());
    $(function() {
        jasmine.getEnv().execute();
    });
</script>
</body>
</html>
