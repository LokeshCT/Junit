<@layout.default ; for>
    <@content for=="title">${view.title}</@content>
    <@content for=="head">
    </@content>
    <@content for=="body">
    <script type="text/javascript">
        $(document).ready(function () {
            $('#connections').dataTable({
                "aaSorting":[
                    [ 1, "desc" ]
                ],
                "bPaginate":false,
                "bFilter":false,
                "bInfo":false,
                "bSort":true,
                "bJQueryUI":true,
                "bAutoWidth":true
            });
    </script>
    <div id="content">
        <hr/>
        <table id='connections'>
            <caption>${view.header}</caption>
            <thead>
            <tr align='center'>
                <td><strong>DataSource Name</strong></td>
                <td><strong>Thread Pool Size</strong></td>
                <td><strong>User Pool Size</strong></td>
                <td><strong>No. Total Connections</strong></td>
                <td><strong>No. Busy Connections</strong></td>
                <td><strong>No. Idle Connections</strong></td>
                <td><strong>No. Orphaned Connections</strong></td>
            </tr>
            </thead>
            <tbody>
                <#list dataSources as dataSource>
                <tr>
                    <th>${dataSource.dataSourceName}</th>
                    <td class="metric">${dataSource.userPoolCount}</td>
                    <td class="metric">${dataSource.threadPoolSize}</td>
                    <td class="metric">${dataSource.numConnections}</td>
                    <td class="metric">${dataSource.numBusyConnections}</td>
                    <td class="metric">${dataSource.numIdleConnections}</td>
                    <td class="metric">${dataSource.numOrphanedConnections}</td>
                </tr>
                </#list>
            </tbody>
        </table>
    </div>
    </@content>
</@layout.default>
