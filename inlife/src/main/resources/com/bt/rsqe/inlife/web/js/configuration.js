services.factory('Configuration', ['$document', function ($document) {
    return JSON.parse($document.find("#pageContext").text());
}]);
