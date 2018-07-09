<!--form snippet.  not intended to be a full html page-->
<@cc.dialog ; for >
    <@content for=="main">
    <div id="serviceLevelAgreementForm" name="serviceLevel.Agreements.Form" style="top:20px">
        <div id="commonError" class="hidden"></div>
        <div id="successMessage" class="hidden"></div>
        <div id="serviceLevelAgreementDialog"></div>
        <span id="complexContract" class="hidden">${view.complexContract}</span>

        <div class="data-AgreementContainer" style="height: auto !important;">
            <table id="agreementsFilterTable">
                <tr>
                    <td>
                        <label>Type:</label>
                    </td>
                    <td>
                        <@cc.select id="typeFilter">
                            <#list view.agreementTypeList as agreementType>
                                <option value="${agreementType.value}" <#if agreementType.value = 'All'!>selected</#if>>${agreementType.value}</option>
                            </#list>
                        </@cc.select>
                    </td>
                </tr>
                <tr class="blank_row_new">
                    <td colspan="3">&nbsp;</td>
                </tr>
                <tr>
                    <td>
                        <label>Country :</label>
                    </td>
                    <td>
                        <@cc.select id="countryFilter">
                            <#list view.countries as portCountryDTO>
                                <option value="${portCountryDTO.cityId}">${portCountryDTO.cityName}</option>
                            </#list>
                        </@cc.select>
                    </td>
                </tr>
            </table>

           <#-- <div class="data-Container border">-->
            <#--                <div class="search-container">
                <input type="text" id="globalSearch" placeholder="Search SLA Ids..." title="Search by SLA ID"/>
                <input type="button" id="globalSearchBtn" class="submit button" value="Search"/>
            </div>-->
                <table id="serviceLevelAgreementTable">
                    <thead>
                    <tr>
                        <th></th>
                        <th>ID</th>
                        <th>Label</th>
                        <th>Service Level Type</th>
                        <th>Hours of Business</th>
                        <th>Resiliency Repair Target</th>
                        <th>Resiliency Response Target</th>
                        <th>Severity Repair Target</th>
                        <th>Severity Response Target</th>
                        <th>Site Availability Target</th>
                        <th>SLA Reference</th>
                    </tr>
                    </thead>
                    <tbody>
                    </tbody>
                </table>
         <#--   </div>-->

            <span id="productAgreementsUri" class="hidden">${view.productAgreementsUri}</span>

        </div>
    </@content>
    <@content for=="buttons">
        <input type="button" id="associateSLAButton" class="button" value="Associate SLA">
    </@content>
</@cc.dialog>
