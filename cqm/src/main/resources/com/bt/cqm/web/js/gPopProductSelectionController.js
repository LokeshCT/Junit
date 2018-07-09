var module = angular.module('cqm.controllers');

module.controller('GPopProductController',  function ($scope, UIService, customerService, $location, $rootScope,filterFilter,$timeout) {
    console.log('Inside GPopProductController ..');

    $scope.products =[{label:'IP Connect Global', value:'IP Connect Global', selected:false},
        {label:'Etherflow', value:'Etherflow', selected:false},
        {label:'Internet Connect Global', value:'Internet Connect Global', selected:false},
        {label:'One Voice', value:'One Voice', selected:false},
        {label:'MSE', value:'MSE', selected:false}]

    $scope.formdata = {};
    $scope.onPageLoad = function () {
        console.log('On GPopProductController Page Load ...');
        $scope.title = 'Select a Product';
        $scope.formdata = {};
        $timeout(function(){ $scope.reset()});


        if(!$rootScope.$$phase){
            $rootScope.$digest();
        }

    };

    $scope.reset = function(){
        $scope.formdata.includeDomestic = false;
        _.forEach($scope.products,function(product){
            product.selected =false;
        });

    }

    $scope.selection = [];

    /*$scope.selectedProducts = function() {
        return filterFilter($scope.products, { selected: true });
    };*/

    $scope.$watch('products|filter:{selected:true}', function (nv) {
        $scope.selection = nv.map(function (product) {
            return product.value;
        });
    }, true);

    $scope.onSubmit = function () {

        //$scope.selection = $scope.selectedProducts();
        var gpopFilterDto = {productNames:$scope.selection,includeDomesticGPOPs:$scope.formdata.includeDomestic};

        $scope.$emit('FILTER_GPOPS',gpopFilterDto);

    };



})
    