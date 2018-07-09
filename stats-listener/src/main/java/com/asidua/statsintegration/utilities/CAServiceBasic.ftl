<#-- @ftlvariable name="params" type="com.asidua.statsintegration.services.ParameterMap" -->
<?xml version="1.0"?>
<StatsIntegration name="CA Service Basic" version="1.0" uri="${params.URI}">
   <ProductInformation projectId="${params.projectId}" customerId="${params.customerId}" quoteName="${params.quoteName}">
        <Cmc numberOfUsers="10" einNumber="${params.triggerEin}"/>
   </ProductInformation>
</StatsIntegration>