<@cc.a id="1" disabled=true />
<@cc.a id="2" disabled=false href="link" />
<@cc.a id="3" href="link" class="blah-class" >
    some text
</@cc.a>
<@cc.a id="4" href="link" class="blah-class" target="foo">
    some text
</@cc.a>
<@cc.a id="5" disabled=true>
    some text
</@cc.a>

<@cc.a {"id":"6", "href":"link", "class":"blah-class", "target":"foo"}>
    some text
</@cc.a>

<@cc.a/>
<@cc.a id="7" switchDisabled=true>
    some text
</@cc.a>
<@cc.a id="8" switchDisabled=true disabled=true>
    some text
</@cc.a>