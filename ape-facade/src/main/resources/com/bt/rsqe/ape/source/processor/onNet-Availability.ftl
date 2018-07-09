<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:tem="http://tempuri.org/">
    <soapenv:Header/>
    <soapenv:Body>
        <tem:getOnnetBuildingsStatusPerSite>
            <tem:arSiteDetails>
            <#list request.siteDTOList as siteDTO>
                <tem:SiteDetails>
                    <tem:siteID>siteDTO.</tem:siteID>
                    <tem:sqeBFGsiteID>siteDTO.bfgSiteID</tem:sqeBFGsiteID>
                    <tem:countryID>int</tem:countryID>
                    <tem:accuracyLevel>int</tem:accuracyLevel>
                    <tem:latitude>double</tem:latitude>
                    <tem:longitude>double</tem:longitude>
                    <tem:countryName>string</tem:countryName>
                    <tem:postCode>string</tem:postCode>
                    <tem:city>string</tem:city>
                    <tem:streetName>string</tem:streetName>
                    <tem:telephoneNumber>string</tem:telephoneNumber>
                    <tem:COUNTY_STATE_PROVINCE>string</tem:COUNTY_STATE_PROVINCE>
                    <tem:STATE_CODE>string</tem:STATE_CODE>
                </tem:SiteDetails>
            </#list>
            </tem:arSiteDetails>
        </tem:getOnnetBuildingsStatusPerSite>
    </soapenv:Body>
</soapenv:Envelope>
