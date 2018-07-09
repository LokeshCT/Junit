<li><label>Quote Option </label>
<@cc.select id="targetQuoteOption" class="targetQuoteOption" >

    <#list view.quoteOptionDTOs as quoteOptionDTO>
        <option value="${quoteOptionDTO.id}">${quoteOptionDTO.name}</option>
    </#list>

</@cc.select>
</li>