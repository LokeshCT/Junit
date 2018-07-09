if (!window.rsqe) rsqe = {};

rsqe.PageFrameworkTestTabA = function() {

};

rsqe.PageFrameworkTestTabA.prototype.initialise = function() {

};

rsqe.PageFrameworkTestTabB = function() {

};

rsqe.PageFrameworkTestTabB.prototype.initialise = function() {
    $("#tabBCopy").click(function() {
        $("#tabBDestination").text($("#tabBSource").text());
    });
};