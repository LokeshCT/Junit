
describe('Unit tests for the Utility file.', function() {

    // Constants for the namespaces used in this file.
    var utility = rsqe.utility;

    // String Test Values:
    var EMPTY_STRING     = "";
    var NON_EMPTY_STRING = "Test";

    // Numeric test Values:
    var POSITIVE_NUMBER  = 5;
    var NEGATIVE_NUMBER  = -5;
    var ZERO             = 0;

    describe("Unit tests for the isNumeric function.", function() {

        // Numeric
        it("should return true for a numeric value", function() {
            expect( utility.isNumeric(POSITIVE_NUMBER) ).toBeTruthy();
            expect( utility.isNumeric(ZERO) ).toBeTruthy();
            expect( utility.isNumeric(NEGATIVE_NUMBER) ).toBeTruthy();
        });

        // String
        it("should return false for a String value", function() {
            expect( utility.isNumeric(NON_EMPTY_STRING)).toBeFalsy();
        });

        // null
        it("should return false for a null value", function(){
            expect( utility.isNumeric(null) ).toBeFalsy();
        });

        // undefined
        it("should return false for an undefined value", function(){
            expect( utility.isNumeric(undefined) ).toBeFalsy();
        });

        // Empty String
        it("should return false for an empty String value", function(){
            expect( utility.isNumeric(EMPTY_STRING) ).toBeFalsy();
        });

    });

    describe('Unit tests for the isValueZeroOrBlank function.', function() {

        // 0.00
        it('0.00 value should return true.', function() {
            expect( utility.isValueZeroOrBlank("0.00") ).toBeTruthy();
        });

        // Null Value.
        it('null value should return true.', function() {
            expect( utility.isValueZeroOrBlank(null) ).toBeTruthy();
        });

        // Undefined.
        it('undefined should return true.', function() {
            expect( utility.isValueZeroOrBlank(undefined) ).toBeTruthy();
        });

        // Strings.
        it('Empty String should return true, non-empty strings should return false.', function() {
            expect( utility.isValueZeroOrBlank(EMPTY_STRING) ).toBeTruthy();
            expect( utility.isValueZeroOrBlank(NON_EMPTY_STRING) ).toBeFalsy();
        });

        // Number
        it('Numbers should not return false.', function() {
            expect( utility.isValueZeroOrBlank(POSITIVE_NUMBER) ).toBeFalsy();
            expect( utility.isValueZeroOrBlank(NEGATIVE_NUMBER) ).toBeFalsy();
            expect( utility.isValueZeroOrBlank(ZERO) ).toBeFalsy();
        });

    });

    describe('Unit tests for the isBlank function.', function() {

        // Null Value.
        it('null value should return true.', function() {
            expect( utility.isBlank(null) ).toBeTruthy();
        });

        // Undefined.
        it('undefined should return true.', function() {
            expect( utility.isBlank(undefined) ).toBeTruthy();
        });

        // Strings.
        it('Empty String should return true, non-empty strings should return false.', function() {
            expect( utility.isBlank(EMPTY_STRING) ).toBeTruthy();
            expect( utility.isBlank(NON_EMPTY_STRING) ).toBeFalsy();
        });

        // Number
        it('Numbers should return false.', function() {
            expect( utility.isBlank(POSITIVE_NUMBER) ).toBeFalsy();
            expect( utility.isBlank(NEGATIVE_NUMBER) ).toBeFalsy();
            expect( utility.isBlank(ZERO) ).toBeFalsy();
        });

    });

    describe('Unit tests for the isNull function.', function() {

        // Null Value.
        it('null value should return true.', function() {
            expect( utility.isNull(null) ).toBeTruthy();
        });

        // Undefined.
        it('undefined should return false.', function() {
            expect( utility.isNull(undefined) ).toBeFalsy();
        });

        // Strings.
        it('Strings should return false.', function() {
            expect( utility.isNull(EMPTY_STRING) ).toBeFalsy();
            expect( utility.isNull(NON_EMPTY_STRING) ).toBeFalsy();
        });

        // Number.
        it('Numbers should return false.', function() {
            expect( utility.isNull(POSITIVE_NUMBER) ).toBeFalsy();
            expect( utility.isNull(NEGATIVE_NUMBER) ).toBeFalsy();
            expect( utility.isNull(ZERO) ).toBeFalsy();
        });

    });

    describe('Unit tests for the isUndefined function.', function() {

        // Null
        it('null value should return false.', function() {
            expect( utility.isUndefined(null) ).toBeFalsy();
        });

        // Undefined
        it('undefined value should return true.', function() {
            expect( utility.isUndefined(undefined) ).toBeTruthy();
        });

        // Numeric
        it('numeric values should return false.', function() {
            expect( utility.isUndefined(POSITIVE_NUMBER) ).toBeFalsy();
            expect( utility.isUndefined(ZERO) ).toBeFalsy();
            expect( utility.isUndefined(NEGATIVE_NUMBER) ).toBeFalsy();
        });

        // Non-empty string
        it("empty String value should return false.", function() {
            expect( utility.isUndefined(NON_EMPTY_STRING) ).toBeFalsy();
        });

        it("non empty String value should return false.", function() {
            expect( utility.isUndefined(EMPTY_STRING) ).toBeFalsy();
        });
    });

    describe('Unit tests for the isEmptyString function.', function() {

        // Null
        it('null value should return false.', function() {
            expect( utility.isEmptyString(null) ).toBeFalsy();
        });

        // Undefined
        it('undefined value should return false.', function() {
            expect( utility.isEmptyString(undefined) ).toBeFalsy();
        });

        // Numeric
        it('numeric values should return false.', function() {
            expect( utility.isEmptyString(POSITIVE_NUMBER) ).toBeFalsy();
            expect( utility.isEmptyString(ZERO) ).toBeFalsy();
            expect( utility.isEmptyString(NEGATIVE_NUMBER) ).toBeFalsy();
        });

        // Non empty String
        it("non empty String value should return false", function() {
            expect( utility.isEmptyString(NON_EMPTY_STRING) ).toBeFalsy();
        });

        // Empty Strings
        it("empty String value should return true.", function() {
            expect( utility.isEmptyString(EMPTY_STRING) ).toBeTruthy();
        });
    });

    describe("Unit tests for the contains function.", function() {

        // Null
        it("null value should return false.", function() {
            expect( utility.contains() ).toBeFalsy();
        });

        // Undefined
        it("undefined value should return false.", function(){
            expect( utility.contains(undefined) ).toBeFalsy();
        });

        // Strings
        it("String values should return true.", function(){
            expect( utility.contains("contains function unit test", "unit test") ).toBeTruthy();
        });

        // Numeric
        it("numeric values should return false", function(){
            expect( utility.contains(POSITIVE_NUMBER) ).toBeFalsy();
            expect( utility.contains(NEGATIVE_NUMBER) ).toBeFalsy();
            expect( utility.contains(ZERO) ).toBeFalsy();
        });
    });

    describe("Unit Tests for timeoutOnClickEvent function.", function(){

        it("should call timeoutOnClickEvent.", function(){

            spyOn(utility, 'timeoutOnClickEvent');
            utility.timeoutOnClickEvent("test", 5000);
            expect( utility.timeoutOnClickEvent ).toHaveBeenCalledWith("test", 5000);
        });
    });
});