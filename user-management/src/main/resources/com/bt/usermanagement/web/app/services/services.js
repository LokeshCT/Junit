'use strict';
angular.module('app.services', [])

.factory('httpService', ['$http', '$q', function ($http, $q) {
    return {
        httpGet:function (urlString) {
            var deferred = $q.defer();
            $http({
                      method:'GET',
                      url:urlString

                  }).success(
                    function (responseData, status) {
                        deferred.resolve(responseData);
                    }).error(function (responseData, status) {
                                 deferred.resolve(responseData);
                             });
            return deferred.promise;
        },
        httpQParamGet:function (urlString, qParams) {
            var deferred = $q.defer();
            $http({
                      method:'GET',
                      url:urlString,
                      params:qParams
                  }).success(
                    function (responseData, status) {
                        deferred.resolve(responseData);
                    }).error(function (responseData, status) {
                                 deferred.resolve(responseData);
                             });
            return deferred.promise;
        },
        httpGetFun:function (urlString, callbackFunction) {
            $http({
                      method:'GET',
                      url:urlString

                  }).success(
                    function (responseData, status) {
                        callbackFunction(responseData, status);
                    }).error(function (responseData, status) {
                                 callbackFunction(responseData, status);
                             });
        },
        httpQParamGetFun:function (urlString, qParams, callbackFunction) {
            $http({
                      method:'GET',
                      url:urlString,
                      params:qParams
                  }).success(
                    function (responseData, status) {
                        callbackFunction(responseData, status);
                    }).error(function (responseData, status) {
                                 callbackFunction(responseData, status);
                             });
        }
    };
}])

.factory('UrlConfiguration', ['$document', function ($document) {
    return JSON.parse($document.find("#urlConfig").text());
}])

.factory('userService', ['httpService', '$rootScope', '$document', 'UrlConfiguration', function (httpService, $rootScope, $document, UrlConfiguration) {
    return {

        getUserId: function(){
            return JSON.parse($document.find("#userId").text());
        },

        getRSQEUserRelatedRoles: function (callback) {
            var userId = this.getUserId();
            var qParams = {
                userID: userId
            };
            httpService.httpQParamGetFun(UrlConfiguration.getAllRolesByUserIdUrl, qParams, callback);
        },

        getCQMUrl: function(){
            return $document.find("#cqmUrl").text();
        },

        getNRMUrl: function(){
            return $document.find("#nrmUrl").text();
        },

        getRoleGroupConstants: function(){
            return JSON.parse($document.find("#roleGroupConstants").text());
        },
    };
}])





