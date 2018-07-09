<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:tem="http://tempuri.org/" xmlns:dsl="http://schemas.datacontract.org/2004/07/DSLService1">
  <soapenv:Header/>
  <soapenv:Body>
    <tem:GetDSLApplicability>
      <tem:input>
        <dsl:requestId>${request.getRequestId()}</dsl:requestId>
        <dsl:Requests>
          <#list request.getSupplierSites() as site>
            <dsl:RequestDetails>
              <dsl:countryISOCode>${site.getCountryISOCode()}</dsl:countryISOCode>
              <dsl:countryName/>
              <dsl:TechnologyList>
                <dsl:Technology>
                  <dsl:parentAccessType/>
                  <dsl:deliveryMode/>
                </dsl:Technology>
              </dsl:TechnologyList>
            </dsl:RequestDetails>
          </#list>
        </dsl:Requests>
      </tem:input>
    </tem:GetDSLApplicability>
  </soapenv:Body>
</soapenv:Envelope>
