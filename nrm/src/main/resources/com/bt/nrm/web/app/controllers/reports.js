angular.module('app')
        .controller('ReportsController', ['$scope' , function ($scope) {


    $scope.barChartData = {
        labels: ["January", "February", "March", "April", "May", "June", "July"],
        datasets: [
            {
                fillColor: '#FF0099',
                strokeColor: '#00FF00',
                data: [65, 59, 90, 81, 56, 55, 40]
            },
            {
                fillColor: '#FF0099',
                strokeColor: '#00FF00',
                data: [28, 48, 40, 19, 96, 27, 100]
            }
        ]

    };

    new Chart(document.getElementById("bar").getContext("2d")).Bar($scope.barChartData);


}]);