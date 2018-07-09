function provide$configuration(provide, config) {
    provide.value('$document', {
        find: function(selector) {
            if (selector != '#urlConfig') throw 'wrong selector';
            return {
                text: function() {
                    return _.isUndefined(config) ? "{}" : JSON.stringify(config);
                }
            };
        }
    });
}