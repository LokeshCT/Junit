<!--form snippet.  not intended to be a full html page-->
<@cc.dialog ; for >
<@content for=="main">
<div id="newSiteForm" name="move.product.form" style="top:50px">
    <div id="selectNewSiteDialog"></div>
    <div>
    <h3>Move In-Country</h3>
    <div class="data-Container border">
        <table id="newSiteTable">
            <thead>
            <tr>
                <th></th>
                <th>Site Name</th>
                <th>Address Line 1</th>
                <th>Address Line 2</th>
                <th>Address Line 3</th>
                <th>Town/City</th>
                <th>Country</th>
                <th>Post Code</th>
            </tr>
            </thead>
            <tbody>
            </tbody>
        </table>
    </div>

    <input type="hidden" class="productAction" name="productAction" id="productAction" value="${view.productAction}"/>
    <input type="hidden" class="getSitesUri" name="getSitesUri" value="${view.getSitesUri}"/>
    <input type="hidden" class="productSCode" name="productSCode" value=""/>
    </div>
    <h3>Move In-Campus</h3>
    <label>
        <input type="radio" id="showHiddenSites" name="siteIds" class="choice" value="sameSite">Please select this box for same site moves(Internal Shift)</label>
    <br>
</div>
</@content>
<@content for=="buttons">
    <input type="button" id="selectNewSiteOkButton" class="button" value="OK">
</@content>
</@cc.dialog>
