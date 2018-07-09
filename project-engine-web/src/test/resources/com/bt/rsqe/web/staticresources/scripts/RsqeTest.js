describe('RSQE', function() {
    describe('When converting as Percent', function() {
        it('Should round to 5 decimal places', function() {
            expect("0.555555".asPercent()).toEqual('0.55556');
            expect("-0.555555".asPercent()).toEqual('-0.55555');
            expect("0.555554".asPercent()).toEqual('0.55555');
            expect("-0.555556".asPercent()).toEqual('-0.55556');
            expect(0.5555545.asPercent()).toEqual('0.55555');
            expect(0.5555544.asPercent()).toEqual('0.55555');
        });
        it('Should add zeros to the end', function () {
            expect('1'.asPercent()).toEqual('1.00000');
            expect(0.1.asPercent()).toEqual('0.10000');
            expect('0.01'.asPercent()).toEqual('0.01000');
            expect(0.001.asPercent()).toEqual('0.00100');
            expect('0.0001'.asPercent()).toEqual('0.00010');
        });
    });

    describe('When converting as Currency', function() {
        it ('should handle empty',function (){
            expect("0".asCurrency()).toEqual("0.00");
            expect("".asCurrency()).toEqual("0.00");
        });

        it('Should round positive numbers to 2 decimal places', function() {
            expect("0.555".asCurrency()).toEqual('0.56');
            expect("0.554".asCurrency()).toEqual('0.55');
            expect("0.556".asCurrency()).toEqual('0.56');
            expect(0.5545.asCurrency()).toEqual('0.55');
            expect(0.5544.asCurrency()).toEqual('0.55');
        });

        it('Should round negative numbers to 2 decimal places', function() {
            expect("-0.555".asCurrency()).toEqual('-0.55');
            expect("-0.554".asCurrency()).toEqual('-0.55');
            expect("-0.556".asCurrency()).toEqual('-0.56');
            expect("-0.5545".asCurrency()).toEqual('-0.55');
            expect("-0.5544".asCurrency()).toEqual('-0.55');

        });

        it('Should add zeros to the end', function () {
            expect('1'.asCurrency()).toEqual('1.00');
            expect(0.1.asCurrency()).toEqual('0.10');
            expect('0.01'.asCurrency()).toEqual('0.01');
        });
    });

    describe('When checking boolean from JSON responses', function() {
        it('should handle string values', function() {
            var value = "true";
            expect(rsqe.jsonSafeBooleanCheck(value)).toBeTruthy();
            value = "false";
            expect(rsqe.jsonSafeBooleanCheck(value)).toBeFalsy();
            value = true;
            expect(rsqe.jsonSafeBooleanCheck(value)).toBeTruthy();
            value = false;
            expect(rsqe.jsonSafeBooleanCheck(value)).toBeFalsy();
        })
    })
});
