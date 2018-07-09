<@cc.dialog id="theDialog" ; for>
<@content for=="main">
some dialog content
</@content>
<@content for=="buttons">
<input type="button" id="button" value="button"/>
</@content>
</@cc.dialog>

<@cc.dialog id="anotherDialog" ; for>
<@content for=="main">
some other content
<button class="another-close">Close</button>
</@content>
</@cc.dialog>

<@cc.dialog id="validatedDialog" ; for>
<@content for=="main">
validated dialog content
<button class="another-close">Close</button>
</@content>
</@cc.dialog>


<button id="openTheDialog1">open the dialog</button>
<button id="openTheDialog2">open the dialog</button>
<button id="openAnotherDialog">open another dialog</button>
<button id="closeAnotherDialog1">close another dialog</button>
