<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:tem="http://tempuri.org/" xmlns:dsl="http://schemas.datacontract.org/2004/07/DSLService1">
  <soapenv:Header/>
  <soapenv:Body>
    <tem:GetAvailabilty>
      <tem:input>
        <dsl:requestId>${request.getRequestId()}</dsl:requestId>
        <dsl:SiteDetails>
          <#list request.getSupplierSites() as site>
            <dsl:SiteDetailsAvailabilty>
              <dsl:siteId>${site.getSiteId()}</dsl:siteId>
              <dsl:siteName>${site.getSiteName()?html}</dsl:siteName>
              <dsl:countryName>${site.getCountryName()}</dsl:countryName>
              <dsl:countryISOCode>${site.getCountryISOCode()}</dsl:countryISOCode>
              <dsl:SupplierList>
                <#list site.getSupplierList() as supplier>
                  <dsl:Supplier>
                    <dsl:supplierId>${supplier.getSupplierId()}</dsl:supplierId>
                    <dsl:supplierName>${supplier.getSupplierName()}</dsl:supplierName>
                    <dsl:SupplierProducts>
                      <#list supplier.getSupplierProductList() as supplierProduct>
                        <dsl:SupplierProdlist>
                          <dsl:SPACID>${supplierProduct.getSpacId()}</dsl:SPACID>
                          <dsl:displaySupplierProductName><#if supplierProduct.getDisplaySupplierProductName()?has_content>${supplierProduct.getDisplaySupplierProductName()}</#if></dsl:displaySupplierProductName>
                          <dsl:supplierProductName>${supplierProduct.getSupplierProductName()}</dsl:supplierProductName>
                          <dsl:availabilityCheckType>${supplierProduct.getAvailabilityCheckType()}</dsl:availabilityCheckType>
                          <dsl:AvailabilityCheckParameters>
                            <#list supplierProduct.getAvailabilitySets() as set>
                              <dsl:SetRequest>
                                <dsl:name>${set.getSetName()}</dsl:name>
                                <dsl:Parameters>
                                  <#list set.getParameterList() as param>
                                    <dsl:Param>
                                      <dsl:name>${param.getName()}</dsl:name>
                                      <dsl:value>${param.getValue()}</dsl:value>
                                    </dsl:Param>
                                  </#list>
                                </dsl:Parameters>
                              </dsl:SetRequest>
                            </#list>
                          </dsl:AvailabilityCheckParameters>
                        </dsl:SupplierProdlist>
                      </#list>
                    </dsl:SupplierProducts>
                  </dsl:Supplier>
                </#list>
              </dsl:SupplierList>
            </dsl:SiteDetailsAvailabilty>
          </#list>
        </dsl:SiteDetails>
        <dsl:syncUri>${request.getSyncUri()}</dsl:syncUri>
      </tem:input>
    </tem:GetAvailabilty>
  </soapenv:Body>
</soapenv:Envelope>

