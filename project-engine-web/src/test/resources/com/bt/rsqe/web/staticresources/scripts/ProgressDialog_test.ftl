<@cc.progressDialog id="toBeStartedProgressDialog" class="done"; for>
<@content for=="main">
<div>
    <span id="toBeStartedProgressText" class="error success">Text</span>
</div>
<div id="spinning">
    <img src="/rsqe/project-engine/static/images/spinning.gif"/>
</div>
<input type="button" id="toBeStartedButton" value="Button"/>
</@content>
</@cc.progressDialog>

<@cc.progressDialog id="toBeStartedWithNoProgressText" class="done"; for>
<@content for=="main">
<div id="spinning">
    <img src="/rsqe/project-engine/static/images/spinning.gif"/>
</div>
<input type="button" id="toBeStartedWithNoTextButton" value="Button"/>
</@content>
</@cc.progressDialog>

<@cc.progressDialog id="toBeFinishedProgressDialog"; for>
<@content for=="main">
<div>
    <span id="toBeFinishedProgressText"></span>
</div>
<div id="spinning">
    <img src="/rsqe/project-engine/static/images/spinning.gif"/>
</div>
<input type="button" id="toBeFinishedButton" class="hidden" value="Button"/>

</@content>
</@cc.progressDialog>

<style type="text/css">
    .hidden {
        display: none !important;
    }
</style>