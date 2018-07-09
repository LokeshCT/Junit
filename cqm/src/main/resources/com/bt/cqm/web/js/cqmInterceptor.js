'use strict';
var module = angular.module('cqm');

module.factory('cqmInterceptor', ['$log', 'UrlConfiguration', 'UIService', '$q', '$location', '$rootScope','$injector', function ($log, UrlConfiguration, UIService, $q, $location, $rootScope,$injector) {
    var statusZeroCount =0;

    var cqmInterceptor = {
        request:function (config) {
            return config
        },
        response:function (response) {
            statusZeroCount =0;
            return response;
        },
        responseError:function (response) {

            if (response.status == 0 ) {
                $log.debug('Oops ... Request didn\'t reach server ! Seems to have caught in proxy. Response STATUS TEXT ::' + response.statusText +', STATUS ::'+response.status);
                statusZeroCount++;
                if(statusZeroCount>1){
                    statusZeroCount =0;
                    window.location.reload();
                }else{
                    var deferred = $q.defer();
                    retryHttpRequest(response.config, deferred);

                    return deferred.promise;
                }
                UIService.unblock();
            }else if (response.status == 502){
                $log.debug('Bad Gateway !!' + response.statusText +', STATUS ::'+response.status);
                var deferred = $q.defer();
                retryHttpRequest(response.config, deferred);
                return deferred.promise;
            }else{
                statusZeroCount =0;
            }
            return response;
        },
        requestError:function (rejectReason) {
            statusZeroCount =0;
            $log.debug('Oops ..client couldn\'t process request !! REQUEST_ERROR ::' + rejectReason);
            UIService.openDialogBox('Client Information', 'Couldn\'t process client request. Click OK and retry', true, false);

        }

    };

    function retryHttpRequest(config, deferred){
        function successCallback(response){
            deferred.resolve(response);
        }
        function errorCallback(response){
            deferred.reject(response);
        }
        var $http = $injector.get('$http');
        $http(config).then(successCallback, errorCallback);
    }

    return cqmInterceptor;
}]);

