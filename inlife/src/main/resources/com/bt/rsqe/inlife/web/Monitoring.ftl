<@layout.default ; for>
    <@content for=="title">${view.title}</@content>
    <@content for=="head">
    </@content>
    <@content for=="body">
    <div id="header">
        <h1>
        ${view.header}
        </h1>
        <script type="text/javascript">
            (function ($) {
                $(document).ready(function () {
                    <#list info?keys as key>
                        var info = eval(${info[key]});
                        var row = "<tr><td>${key}</td><td>" + info.componentVersion + "</td><td>";
                        $.each($.makeArray(info.componentProperties),
                               function (index, prop) {
                                   row = row + prop.name + ":" + prop.value + "<br/>"
                               });
                        row = row + "</td><td>" + info.timestamp + "</td><tr>\n";
                        $('#infoContent').append(row);
                    </#list>
                    <#list health?keys as key>
                        var health = eval(${health[key]});
                        $('#healthContent').append("<tr><td width='10%'>${key}</td><td width='10%'>" + health.status.level +
                                                   "</td><td>" + health.status.reason +
                                                   "</td><td width='10%'>" + health.timestamp +
                                                   "</td><tr>\n");
                    </#list>
                    <#list stats?keys as key>
                        var stat = eval(${stats[key]});
                        var row = "<tr><td>${key}</td><td>" + stat.totalRequests +
                                  "</td><td>" + stat.totalCompletedResponses;

                        row = row + "</td><td>" + (stat.totalRequests - stat.totalCompletedResponses) + "</td>";
                        row = row + "</td><td>" + stat.timestamp + "</td><tr>\n";
                        $('#statsContent').append(row);

                        row = "<tr><td>&nbsp;</td><td colspan='4'><table id='statstable-${key}'><thead><tr><th>Path</th><th>Hits</th><th>Min (ms)</th><th>Max (ms)</th><th>Total (ms)</th><th>Mean (ms)</th></tr></thead><tbody>";
                        $.each(stat.requestStats, function (index, pathStats) {
                            row = row + "<tr>";
                            row = row + "<td width='40%'>" + pathStats.path + "</td>";
                            row = row + "<td width='10%'>" + pathStats.hitCount + "</td>";
                            row = row + "<td width='10%'>" + pathStats.minTime + "</td>";
                            row = row + "<td width='10%'>" + pathStats.maxTime + "</td>";
                            row = row + "<td width='15%'>" + pathStats.totalTime + "</td>";
                            row = row + "<td width='15%'>" + (pathStats.totalTime / pathStats.hitCount).toFixed(0) + "</td>";
                            row = row + "</tr>";
                        });
                        row = row + "</tbody></table></tr>";
                        $('#statsContent').append(row);
                        $('#statstable-${key}').dataTable({
                          "aaSorting":[
                              [ 5, "desc" ]
                          ],
                          "bPaginate":false,
                          "bFilter":true,
                          "bInfo":false,
                          "bSort":true
                      });
                    </#list>
                });
            })(jQuery);

        </script>
    </div>
    <div id="content">
        <hr/>
        <div id="information">
            <h2>Information</h2>
            <table>
                <thead>
                <tr>
                    <th>Component</th>
                    <th>Version</th>
                    <th>Properties</th>
                    <th>Timestamp</th>
                </tr>
                </thead>
                <tbody id="infoContent"></tbody>
            </table>
        </div>

        <div id="health">
            <h2>Health</h2>
            <table>
                <thead>
                <tr>
                    <th>Component</th>
                    <th>Status</th>
                    <th>Reason</th>
                    <th>Timestamp</th>
                </tr>
                </thead>
                <tbody id="healthContent"></tbody>
            </table>
        </div>


        <div id="stats">
            <h2>Statistics</h2>
            <table>
                <thead>
                <tr>
                    <th>Component</th>
                    <th>Total<br/>Requests</th>
                    <th>Total<br/>Completed<br/>Responses</th>
                    <th>In Progress</th>
                    <th>Timestamp</th>
                </tr>
                </thead>
                <tbody id="statsContent"></tbody>
            </table>
        </div>
    </div>
    </@content>
</@layout.default>
