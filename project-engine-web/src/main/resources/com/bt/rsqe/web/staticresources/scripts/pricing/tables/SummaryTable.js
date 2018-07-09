/**
 * This namespace defines all functionality required to build the Summary DataTable displayed on the {@link standardTab}.
 *
 * @namespace summaryTable
 * @summaryTable The object to append all logic onto.
 * @undefined This parameter prevents undefined from being redefined by other JavaScript code.
 */
(function(summaryTable, undefined) {

    // Short names for the namespaces used here.
    var pricing     = rsqe.pricingTab;
    var url         = rsqe.urlBuilder;
    var columnNames = rsqe.pricingTab.PRICE_LINE_TABLE_COLUMN_NAME;
    var standardTab = rsqe.pricingTab.standardTab;

    // The indices of the RRP columns. Required so that we can hide them if the user does not have EUP Access.
    var ONE_TIME_RRP_COLUMN_INDEX  = 7;
    var RECURRING_RRP_COLUMN_INDEX = 11;

    /**
     * Initialize the Summary DataTable and it's supporting elements.
     * @public
     */
    summaryTable.initialize = function() {
        buildSummaryTable();
    };

    /**
     * Initialise and configure the Summary DataTable.
     * This table is displayed on the Summary View of the {@link standardTab}.
     * This table shows a summary view of the Price Line data I.e The summary rows from the PRICING_DETAILS_TABLE.
     * No discounting or editing of any kind is possible on the Summary view.
     *
     * Configuration:
     *  bProcessing     :: Display a loading screen during initial page load and whilst performing searches/filters.
     *  bServerSide     :: The databases contain large data-sets that would be inefficient to process on the client side so enable server mode
     *                     and have the database return paginated results.
     *  sAjaxSource     :: Retrieve product prices from the /productPrices end point. As the JSON object returned by the end point does not
     *                     contain either the 'data' or 'aadata' fields required by the DataTable library the default value has been over-
     *                     written to itemDTOs.
     *  sPaginationType :: Set paging to be displayed as: 'First Previous <Current Page> Next Last'.
     *  bSort           :: Table sorting/ordering is disabled.
     *  bAutoWidth      :: Prevent DataTables from restricting the size of the table. When true the table drawn is about half the size of
     *                     it's containing div.
     *  aoColumns       :: Column configuration options.
     *                     - sWidth      - Specify the size of the column. Required to prevent the jeditable library from resizing table cells.
     *                     - sClass      - Specify classes to apply to the cells of this table.
     *                      - numeric    - Display value as a number.
     *                      - nowrap     - Do not wrap the numeric columns.
     *  aoColumnDefs    :: Hide the RRP columns if the current user does not have EUP access.
     *  oLanguage       :: Adds two information displays to the table.
     *                     - sInfo       - Shows the pagination for the data currently on screen: E.g. 'Showing 1 to 5 of 5 Root Products'
     *                     - aLengthMenu - Shows a menu that allows the user to select how many results should be shown on the page.
     *
     * @private
     * @returns {DataTable} The Summary DataTable after it has been initialized.
     */
    function buildSummaryTable() {

        $(pricing.PRICING_SUMMARY_TABLE).dataTable(
            {
                bProcessing: true,
                bServerSide: true,
                sAjaxSource: url.PRODUCT_PRICES,
                sPaginationType: "full_numbers",
                bSort: false,
                bAutoWidth: false,
                aoColumns: [
                    { mData: columnNames.SITE,                sWidth: "3%" },
                    { mData: columnNames.MINI_ADDRESS,        sWidth: "5%" },
                    { mData: columnNames.PRODUCT,             sWidth: "10%" },
                    { mData: columnNames.SUMMARY,             sWidth: "10%" },
                    { mData: columnNames.DESCRIPTION,         sWidth: "15%" },
                    { mData: columnNames.OFFER_NAME,          sWidth: "5%" },
                    { mData: columnNames.DISCOUNT_STATUS,     sWidth: "5%" },
                    { mData: columnNames.ONE_TIME_RRP,        sWidth: "4%", sClass: "numeric nowrap readOnly" },
                    { mData: columnNames.ONE_TIME_GROSS,      sWidth: "4%", sClass: "numeric nowrap readOnly" },
                    { mData: columnNames.ONE_TIME_DISCOUNT,   sWidth: "4%", sClass: "numeric nowrap readOnly" },
                    { mData: columnNames.ONE_TIME_NET_TOTAL,  sWidth: "4%", sClass: "numeric nowrap readOnly" },
                    { mData: columnNames.RECURRING_RRP,       sWidth: "4%", sClass: "numeric nowrap readOnly" },
                    { mData: columnNames.RECURRING_GROSS,     sWidth: "4%", sClass: "numeric nowrap readOnly" },
                    { mData: columnNames.RECURRING_DISCOUNT,  sWidth: "4%", sClass: "numeric nowrap readOnly" },
                    { mData: columnNames.RECURRING_NET_TOTAL, sWidth: "4%", sClass: "numeric nowrap readOnly" }

                ],
                aoColumnDefs: [
                    {
                        aTargets: [
                            ONE_TIME_RRP_COLUMN_INDEX,
                            RECURRING_RRP_COLUMN_INDEX
                        ],
                        bVisible: pricing.hasEupAccess()
                    }
                ],
                oLanguage: {
                    sInfo: "Showing _START_ to _END_ of _TOTAL_ Root Products",
                    aLengthMenu: "Display _MENU_ Root Products"
                },

                /**
                 * By default DataTables expects the JSON object returned by sAjaxSource to contain either a 'aadata' or 'data' field.
                 * The /product-prices end point contains neither. This configuration overwrites the default to point at the itemDTOs field.
                 * @param data The JSON object returned by the URL set by sAjaxSource.
                 * @returns {Array} The itemDTOs array returned as part of the JSON response returned by a call to the /product-prices end point.
                 */
                sAjaxDataProp: function (data) {
                    return data.itemDTOs;
                },

                /**
                 * Called every time the table is drawn.
                 * The functions here are executed AFTER the table has been drawn.
                 * - Prepend the Summary row for each price line. I.e adds the content for this table.
                 * - Removes any rows that are not summary rows.
                 * @param settings The settings object for this table.
                 */
                fnDrawCallback: function (settings) {
                    standardTab.renderGroups( $(pricing.PRICING_SUMMARY_TABLE), settings );
                    removeNonSummaryRows( $(pricing.PRICING_SUMMARY_TABLE) );
                    standardTab.configureTableForNoEupAccess();
                }
            }
        );
    }

    /**
     * Remove any row from the given tables body that is NOT a summary row.
     * @private
     * @param table The table to remove the non-summary rows from.
     *              The table should be formatted as a JQuery selector I.e. $(table_name);
     */
    function removeNonSummaryRows(table) {
        table.find('tbody tr').not('.product_group.group_0').remove();
    }

// Immediately invoke this namespace.
}( rsqe.pricingTab.standardTab.summaryTable = rsqe.pricingTab.standardTab.summaryTable || {} ));