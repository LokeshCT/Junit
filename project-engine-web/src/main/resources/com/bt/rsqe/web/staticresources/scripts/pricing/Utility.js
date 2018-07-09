/**
 * This namespace defines general purpose utility functions.
 *
 * @namespace utility
 * @utility The object to append all functionality to.
 * @undefined This parameter prevents undefined from being redefined by other JavaScript code.
 */
(function(utility, undefined) {

    /**
     * Checks that the given value is a number.
     * @public
     * @param value The value to check.
     * @returns {boolean} True if the value is a number. False otherwise.
     */
    utility.isNumeric = function(value) {
        return !isNaN(value) && !utility.isNull(value) && !utility.isEmptyString(value);
    };

    /**
     * Determines if a String is zero or Blank.
     * @public
     * @param value The value to test.
     * @returns {boolean} True if the given value is zero or blank.
     */
    utility.isValueZeroOrBlank = function(value) {
        return value === "0.00" || utility.isBlank(value);
    };

    /**
     * Checks if the given String value is an empty String, null, or undefined.
     * @public
     * @param value {String} The value to check.
     * @returns {Boolean} True if the given value is an empty String, null, or undefined. False otherwise.
     */
    utility.isBlank = function(value) {
        return utility.isEmptyString(value) || utility.isNullOrUndefined(value);
    };

    /**
     * Checks if the given value is null or undefined.
     * @param value The value to check.
     * @returns {boolean} True if the given value is null or undefined. False otherwise.
     */
    utility.isNullOrUndefined = function(value) {
        return utility.isNull(value) || utility.isUndefined(value);
    };

    /**
     * Checks if the given value is null.
     * @public
     * @param value The value to check.
     * @returns {boolean} True if the value is null. False otherwise.
     */
    utility.isNull = function(value) {
        return value === null;
    };

    /**
     * Checks if the given value is undefined.
     * @public
     * @param value The value to check.
     * @returns {boolean} True if the given value is undefined. False otherwise.
     */
    utility.isUndefined = function(value) {
        return value === undefined;
    };

    /**
     * Determines if a String is empty.
     * @public
     * @param value {String} The String to test.
     * @returns {boolean} True if the String is empty. False otherwise.
     */
    utility.isEmptyString = function(value) {
        return value === "";
    };

    /**
     * Search the given string for the given substring.
     * The indexOf method always returns a number. -1 if the substring was not found. A positive value otherwise.
     * @param string    {String} The string to search within.
     * @param substring {String} The substring to find in string.
     * @returns {Boolean} True if the string contains the substring. False otherwise or if either input was null or undefined.
     */
    utility.contains = function(string, substring) {

        // If either of the values given are null or undefiled then we cannot perform the contains check.
        if(utility.isNullOrUndefined(string) || utility.isNullOrUndefined(substring)) {
            return false;
        }

        return string.indexOf(substring) > -1;
    };

    /**
     * After the given elements on click event has triggered, prevents the event from triggering again until the timeout has elapsed.
     * @param element {String} The page element to prevent users from clicking.
     *                         Should be usable as a Jquery Selector. I.e. $('element') should work.
     * @param timeout {Number} The amount of time (in milliseconds) to delay before re-enabling the event.
     * @public
     */
    utility.timeoutOnClickEvent = function(element, timeout) {
        utility.disableEvent(element);
        setTimeout(
            function () {
                utility.enableEvent(element);
            },
            timeout
        );
    };

    /**
     * Disable the given page element by adding the 'disable' class, and removing the pointer events used to click the element.
     * @public
     * @param element {String} The page element to disable. Should be usable as a Jquery Selector. I.e. $('element') should work.
     */
    utility.disableEvent = function(element) {
        $(element)
            .addClass('disabled')
            .css({'cursor': 'pointer', 'pointer-events' : 'none'});
    };

    /**
     * Enable the given page element by removing the 'disable' class, and resetting the css pointer events used to click the element.
     * @public
     * @param element {String} The page element to enable. Should be usable as a Jquery Selector. I.e. $('element') should work.
     */
    utility.enableEvent = function(element) {
        $(element)
            .removeClass('disabled')
            .css({'cursor': 'pointer', 'pointer-events' : 'auto'});
    };

    /**
     * Checks if the given DataTable is empty.
     * @param table The DataTable to check.
     * @returns {boolean} True if the table is empty. False otherwise.
     */
    utility.isDataTableEmpty = function(table) {
        return $(table).find('tbody tr td').hasClass('dataTables_empty');
    };

    /**
     * Test if the given DataTable has been Initialized.
     * @param table The table to check. Should be in the form $('table_id').
     * @returns {boolean} True if the the DataTable has been initialized. False otherwise.
     */
    utility.isDataTableInitialized = function(table) {
        var settings = $.fn.dataTableSettings;
        for ( var i = 0, iLen = settings.length ; i < iLen ; i++ ) {
            if ( settings[i].nTable == table[0] ) {
                return true;
            }
        }
        return false;
    };

    /**
     * Returns the cookie.
     * @returns {String} The cookie.
     */
    utility.getCookie = function() {
        return document.cookie;
    };

}(
    // Immediately invoke this namespace.
    rsqe.utility = rsqe.utility || {}
));