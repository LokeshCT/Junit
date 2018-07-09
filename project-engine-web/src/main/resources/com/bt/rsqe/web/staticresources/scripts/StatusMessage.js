var rsqe = rsqe || {};

rsqe.StatusMessage = function(cssSelector) {
    var element = $(cssSelector);

    function hide() {
        element.text("");
        element.addClass("hidden");
    }

    function show(message, timeout) {
        element.html(message).removeClass("hidden");

        if (timeout !== undefined) {
            setTimeout(function() {
                element.addClass("hidden");
            }, timeout);
        }
    }

    return {
        hide: hide,
        show: show
    }
};
