<!--form snippet.  not intended to be a full html page-->
<@cc.dialog ; for >
<@content for=="main">
<form id="quoteOptionForm" name="create.quote.option.form" method="POST" action="${view.formAction}">
    <div id="createQuoteOptionError" class="error hidden commonError" style="margin-bottom: 10px;"></div>
    <div class="hidden contractTermWarning" id="saveContractTermWarning">
        <ul class="warning-message">
            <li>
                <span>One or more line item(s) are already Firm, updating contract term will reset prices, click Save to continue, else Cancel.</span>
            </li>
        </ul>
    </div>
    <p id="manditoryWarning" class="success">All fields are required.</p>
    <p id="commonError" class="error hidden">There was a problem with the information you entered.</p>

    <div class="quoteoption-form ">
        <ul class="form dialogbox">
           <li> <label for="quoteOptionName">Name : </label>
               <div class="fields">
            <input type="text" maxlength="50" id="quoteOptionName" name="quoteOptionName" value="<#if view.name?has_content>${view.name}</#if>"/>
               </div>
        </li>
        <li><label for="contractTerm">Contract Term (in months) : </label>
               <div class="fields">
                <@cc.select id="contractTerm" name="contractTerm" >
                <#list view.contractTerms as term>
                    <option value="${term.value}" <#if term.value = view.contractTerm!>selected</#if>>${term.description}</option>
                </#list>
            </@cc.select>
               </div>
        </li>
        <li> <label for="currency">Currency: </label>
               <div class="fields">
             <@cc.select id="currency" name="currency" disabled="${view.updateCurrency}">
                <#list view.currencies as currency>
                    <option value="${currency.value}" <#if currency.value = view.currency!>selected</#if>>${currency.description}</option>
                </#list>
            </@cc.select>
               </div>
        </li>

    </div>

    <input type="hidden" name="customerId" value="${view.customerId}"/>
    <input type="hidden" name="projectId" value="${view.projectId}"/>
    <input type="hidden" name="quoteOptionId" <#if view.quoteOptionId?has_content>value="${view.quoteOptionId}"</#if>/>
    <#if view.updateCurrency = "disabled"><input type="hidden" name="currency" value="${view.currency}"/></#if>
    <input type="hidden" name="expedioQuoteOptionId" <#if view.expedioQuoteOptionId?has_content>value="${view.expedioQuoteOptionId}"</#if>/>
    <input type="hidden" id="contractTermChangeValidationUri" value="${contractTermChangeValidationUri}"/>


</form>
</@content>

<div id="blockUI" class="dialog-loading-msg2"></div>

<@content for=="buttons">
<input type="button" id="submitOptionButton" class="button" value="Save">
</@content>
</@cc.dialog>
