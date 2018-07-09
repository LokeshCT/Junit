<!--form snippet.  not intended to be a full html page-->
<@cc.dialog ; for >
<@content for=="main">
<form id="contractForm" name="create.contract.form" method="POST" action="${view.formAction}">
    <div class="contract-form">
        <ul class="form dialogbox">
        <li>
            <label for="contractTerm">Contract Term (in months) : </label>
            <div class="fields">
                <input type="text" maxlength="50" id="contractTerm" readonly="true" name="contractTerm" value="<#if view.contractTerm?has_content>${view.contractTerm}</#if>"/>
            </div>
        </li>
        <li> <label for="eupPriceBook">RRP Price Book: </label>
               <div class="fields">
             <@cc.select attrs={"disabled":"${view.priceBookEditable}"} id="eupPriceBook" name="eupPriceBook">
                <#list view.eupPriceBooks as eupPriceBook>
                    <option value="${eupPriceBook}" <#if eupPriceBook = view.eupPriceBook!>selected</#if>>${eupPriceBook}</option>
                </#list>
            </@cc.select>
               </div>
        </li>
    <#if permissions.indirectUser == true>
        <li> <label for="ptpPriceBook">PTP Price Book: </label>
               <div class="fields">
             <@cc.select attrs={"disabled":"${view.priceBookEditable}"} id="ptpPriceBook" name="ptpPriceBook">
                <#list view.ptpPriceBooks as ptpPriceBook>
                    <option value="${ptpPriceBook}" <#if ptpPriceBook = view.ptpPriceBook!>selected</#if>>${ptpPriceBook}</option>
                </#list>
            </@cc.select>
               </div>
        </li>
    </#if>
    </div>
</form>
</@content>

<@content for=="buttons">
<input type="button" id="submitContractButton" class="button" value="Save">
</@content>
</@cc.dialog>
