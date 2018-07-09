var rsqe = rsqe || {};
rsqe.init = function() {
    $.ajaxSetup({cache:false});
};

rsqe.jsonSafeBooleanCheck = function(value) {
    switch(typeof value) {
        case 'string':
            return value == 'true';
            break;
        case 'boolean':
            return value;
            break;
    }
};
$(rsqe.init);

// not sure this is the best place for this function, if there are more string util methods we should split this out
// Formats a string as a decimal currency value
function convertFloatToCurrency(i) {
    if (isNaN(i)) { i = 0.00; }
    if (i < 0) {
        i = parseInt((i - .004) * 100);
    }
    else
    {
        i = parseInt((i + .005) * 100);
    }
    i = i / 100;
    var s = new String(i);
    if (s.indexOf('.') < 0) { s += '.00'; }
    if (s.indexOf('.') == (s.length - 2)) { s += '0'; }
    return s.toString();
}

// Formats a string as a decimal currency value with 5 digit round up.
function convertFloatToPercent(i) {
    if (isNaN(i)) { i = 0.00; }
    if (i < 0) {
        i = parseInt((i - .000004) * 100000);
    }
    else
    {
        i = parseInt((i + .000005) * 100000);
    }
    i = i / 100000;
    var s = new String(i);
    if (s.indexOf('.') < 0) { s += '.'; }
    while (s.indexOf('.') >= (s.length - 5)) { s += '0'; }
    return s.toString();
}

function disableLink(that) {
    return !$(that).hasClass('disabled');
}

Number.prototype.asCurrency = function() {
    return convertFloatToCurrency(this);
};

String.prototype.asCurrency = function() {
    var amount = this;
    var i = parseFloat(amount);
    return convertFloatToCurrency(i);
};

Number.prototype.asPercent = function() {
    return convertFloatToPercent(this);
};

String.prototype.asPercent = function() {
    var amount = this;
    var i = parseFloat(amount);
    return convertFloatToPercent(i);
};