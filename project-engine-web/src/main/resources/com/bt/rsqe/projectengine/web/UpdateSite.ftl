<div class="updateSiteForm">
    <ul class="form dialogbox">
        <form id="siteUpdateForm" accept-charset="utf-8">
            <table id="updateSiteTableItems" border="0">
                <tr>
                    <div id="siteDetailUpdateError" class="commonError hidden"></div>
                    <div id="siteDetailUpdateSuccess" class="successmessage hidden"></div>
                </tr>
                <tr><input type="hidden" id="bfgSiteID" name="bfgSiteID" value="${view.bfgSiteID}"/></tr>
                <tr class="siteDataPop">
                    <td class="td_style">
                        <div class="fields">
                            <label class="fieldName">SiteName* </label>
                        </div>
                    </td>
                    <td class="td_style">
                        <div class="fields">
                            <input type="text" maxlength="100" id="name" name="name" <#if (view.siteCharDetail.nameValue != true)> style="color:#ff0000;" </#if>
                                   value="${view.name}"/>
                        </div>

                    </td>
                    <td class="td_style">
                        <div class="fields">
                            <label class="fieldName">local Company Name </label>
                        </div>
                    </td>
                    <td class="td_style">
                        <div class="fields">
                            <input type="text" maxlength="50" id="localCompanyName" name="localCompanyName" <#if (view.siteCharDetail.localCompanyNameValue != true)> style="color:#ff0000;" </#if>
                                   value="${view.localCompanyName}"/>

                        </div>
                    </td>

                    <td class="td_style">
                        <div class="fields">
                            <label class="fieldName">Building Name*</label>
                        </div>
                    </td>
                    <td class="td_style">
                        <div class="fields">
                            <input type="text" maxlength="240" id="building" name="building" <#if (view.siteCharDetail.buildingValue != true)> style="color:#ff0000;" </#if> value="${view.building}"/>
                        </div>
                    </td>
                </tr>

                <tr>
                    <td class="td_style">
                        <div class="fields">
                            <label class="fieldName">Street </label>
                        </div>
                    </td>
                    <td class="td_style">
                        <div class="fields">
                            <input type="text" maxlength="240" id="streetName" name="streetName" <#if (view.siteCharDetail.streetNameValue != true)> style="color:#ff0000;" </#if> value="${view.streetName}"/>
                        </div>

                    </td>
                    <td class="td_style">
                        <div class="fields">
                            <label class="fieldName">Locality </label>
                        </div>
                    </td>
                    <td class="td_style">
                        <div class="fields">
                            <input type="text" maxlength="100" id="locality" name="locality" <#if (view.siteCharDetail.localityValue != true)> style="color:#ff0000;" </#if> value="${view.locality}"/>
                        </div>
                    </td>

                    <td class="td_style">
                        <div class="fields">
                            <label class="fieldName">City </label>
                        </div>
                    </td>
                    <td class="td_style">
                        <div class="fields">
                            <input type="text" maxlength="50" id="city" name="city" <#if (view.siteCharDetail.cityValue != true)> style="color:#ff0000;" </#if> value="${view.city}"/>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td class="td_style">
                        <div class="fields">
                            <label class="fieldName">Country</label>
                        </div>
                    </td>
                    <td class="td_style">

                        <div class="fields">
                            <input type="text" maxlength="50" id="country" name="country" <#if (view.siteCharDetail.countryValue != true)> style="color:#ff0000;" </#if> value="${view.country}"/>
                        </div>

                    </td>
                    <td class="td_style">
                        <div class="fields">
                            <label class="fieldName">PO Box</label>
                        </div>
                    </td>
                    <td class="td_style">
                        <div class="fields">
                            <input type="text" maxlength="30" id="postBox" name="postBox" <#if (view.siteCharDetail.postBoxValue != true)> style="color:#ff0000;" </#if> value="${view.postBox}"/>
                        </div>
                    </td>

                    <td class="td_style">
                        <div class="fields">
                            <label class="fieldName">Post/Zip Code</label>
                        </div>
                    </td>
                    <td class="td_style">
                        <div class="fields">
                            <input type="text" maxlength="20" id="postCode" name="postCode" <#if (view.siteCharDetail.postCodeValue != true)> style="color:#ff0000;" </#if>
                                   value="${view.postCode}"/>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td class="td_style">
                        <div class="fields">
                            <label class="fieldName">Sub Street </label>
                        </div>
                    </td>
                    <td class="td_style">
                        <div class="fields">
                            <input type="text" maxlength="80" id="subStreet" name="subStreet" <#if (view.siteCharDetail.subStreetValue != true)> style="color:#ff0000;" </#if>
                                   value="${view.subStreet}"/>
                        </div>

                    </td>
                    <td class="td_style">
                        <div class="fields">
                            <label class="fieldName">Sub locality</label>
                        </div>
                    </td>
                    <td class="td_style">
                        <div class="fields">
                            <input type="text" maxlength="80" id="subLocality" name="subLocality" <#if (view.siteCharDetail.subLocalityValue != true)> style="color:#ff0000;" </#if>
                                   value="${view.subLocality}"/>
                        </div>
                    </td>

                    <td class="td_style">
                        <div class="fields">
                            <label class="fieldName">State Province</label>
                        </div>
                    </td>
                    <td class="td_style">
                        <div class="fields">
                            <input type="text" maxlength="80" id="stateCountySProvince" name="stateCountySProvince" <#if (view.siteCharDetail.stateCountySProvinceValue != true)> style="color:#ff0000;" </#if>
                                   value="${view.stateCountySProvince}"/>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td class="td_style">
                        <div class="fields">
                            <label class="fieldName">Sub Building </label>
                        </div>
                    </td>
                    <td class="td_style">
                        <div class="fields">
                            <input type="text" maxlength="100" id="subBuilding" name="subBuilding" <#if (view.siteCharDetail.subBuildingValue != true)> style="color:#ff0000;" </#if>
                                   value="${view.subBuilding}"/>
                        </div>

                    </td>
                    <td class="td_style">
                        <div class="fields">
                            <label class="fieldName">Sub Premise Name</label>
                        </div>
                    </td>
                    <td class="td_style">
                        <div class="fields">
                            <input type="text" maxlength="50" id="subPremise" name="subPremise" <#if (view.siteCharDetail.subPremiseValue != true)> style="color:#ff0000;" </#if>
                                   value="${view.subPremise}"/>
                        </div>
                    </td>

                    <td class="td_style">
                        <div class="fields">
                            <label class="fieldName">Sub State Province</label>
                        </div>
                    </td>
                    <td class="td_style">
                        <div class="fields">
                            <input type="text" maxlength="100" id="subStateCountyProvince" name="subStateCountyProvince" <#if (view.siteCharDetail.subStateCountyProvinceValue != true)> style="color:#ff0000;" </#if>
                                   value="${view.subStateCountyProvince}"/>
                        </div>
                    </td>
                </tr>
            </table>
        </form>
    </ul>
</div>
