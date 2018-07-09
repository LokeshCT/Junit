<@cc.dialog ; for>
    <@content for=="main">
    <form id="commentsAndCaveatsForm">
        <div id="bidComments">
            <h1>Comments</h1>
            <#if view.commentsAndCaveats?size != 0>
                <div id="commentsDiv" class="commentsAndCaveatsCapture">
                    <ul>
                        <#list view.commentsAndCaveats as comment>
                            <li class="comment">
                                <pre>${comment.created}</pre>
                                <pre>${comment.createdBy}</pre>
                                <pre>${(comment.comments)!}</pre>
                            </li>
                        </#list>
                    </ul>
                </div>
            </#if>
        </div>
        <div id="addNewComments">
            <h1>Add Comments</h1>
            <textarea rows="10" cols="90" id="newComment" name="newComment"></textarea>
            <br/>
        </div>

        <div id="bidCaveats">
            <h1>Bid Manager Terms & Conditions for approval of discounts</h1>
            <textarea rows="10" cols="90" id="newCaveat" name="newCaveat">${(view.lastCreatedCaveat)!}</textarea>
            <br/>
        </div>
        <input type="hidden" id="approveDiscountUri" value="${view.bcmApproveUri}"/>
        <input type="hidden" id="rejectDiscountUri" value="${view.bcmRejectUri}"/>
    </form>
    </@content>
    <@content for=="buttons">
    <input type="button" class="submit button" value="Save"/>
    </@content>
</@cc.dialog>
