<@layout.default ; for>
<@content for=="title">${view.title}</@content>
<@content for=="head"></@content>
<@content for=="body">
<div id="header">
    <h1>
    ${view.header}
    </h1>
</div>
<div id="content">
    <hr/>
    <b>Version : </b>${rsqeVersion}  </br>
    <b>Server start time : </b>${serverStartTime}
    <ul>
        <li><a href="/rsqe/inlife/monitoring">Monitoring</a></li>
        <li><a href="/rsqe/inlife/monitoring/connections">Connection Pools</a></li>
        <li><a href="/rsqe/inlife/boms">Generated BOMs</a></li>
        <li><a href="/rsqe/inlife/grabstate-archives">Grabstate Archives</a></li>
        <li><a href="/rsqe/inlife/rsqe-logs">RSQE Logs</a></li>
        <li><a href="/rsqe/inlife/apache-logs">Apache Logs</a></li>
        <li><a href="/rsqe/inlife/diagnostics">Diagnostics</a></li>
        <li><a href="/rsqe/inlife/uplift">Data Uplift</a></li>
    </ul>
</div>
</@content>
</@layout.default>
