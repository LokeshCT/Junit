<div class="data-Container border">
    <div id="usageWarn">
        <ul class="warning-message">
            <li>
                <span>You have unsaved changes</span>
                <input type="button" value="Save" class="button" id="usageSave"/>
                <input type="button" value="Discard" class="button" id="usageDiscard"/>
            </li>
        </ul>
    </div>
    <div id="usageSuccess"><ul class="success-message"><li>Your changes have been saved</li></ul></div>
    <br/>
    <table id="usagePriceLines" style="font-size: smaller;">
        <thead>
            <tr>
                <th rowspan="2" class="noBorder">Product</th>
                <th rowspan="2">Summary</th>
                <th rowspan="2">Priceline Description</th>
                <th rowspan="2">Tier</th>
                <th rowspan="2">Pricing Model</th>
                <th colspan="3" class="th_bg">Min Charge (${view.currency})</th>
                <th colspan="3" class="th_bg">Fixed Charge (${view.currency})</th>
                <th colspan="3" class="th_bg">Charge Rate (%)</th>
            </tr>
            <tr>
                <th class="subtypes">Gross</th>
                <th class="subtypes">Discount (%)</th>
                <th class="subtypes">Net</th>
                <th class="subtypes">Gross</th>
                <th class="subtypes">Discount (%)</th>
                <th class="subtypes">Net</th>
                <th class="subtypes">Gross</th>
                <th class="subtypes">Discount (%)</th>
                <th class="subtypes">Net</th>
            </tr>
        </thead>
        <tbody>
        </tbody>
    </table>
</div>