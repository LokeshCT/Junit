'use strict';

var services = angular.module('rsqe.inlife.services', []);


services.factory('UIService', ['$q', '$http', function ($q, $http) {
    return {
        block:function () {
            $.blockUI({
                          message:'<div style="height: 60px;text-align: center;"><b style="font: 13px">Please wait..</b></div>',
                          css:{
                              background:'url("/rsqe/inlife/static/img/ajax-loader.gif") no-repeat scroll 50% 20px #595E62'
                          }
                      });
        },
        unblock:function () {
            $.unblockUI();
        }
    };
}]);

services.factory('Colors', [function () {
    function randomColor() {
        var color = '#' + (Math.random() * 0xFFFFFF << 0).toString(16);
        return  color == '#ffffff' || color == '#3adaa' ? randomColor() : color;
    }

    return {
        randomColor:randomColor
    };
}]);


services.factory('NoCacheHttpUrlConvertor', function () {
    return  {
        convert:function (uri) {
            var dateStamp = new Date().getTime();
            var delimiter = uriAppearsToAlreadyHaveQueryStringsInIt(uri) ? '&' : '?';

            var noCacheFlag = "noCacheFlag=" + dateStamp;
            return uri + delimiter + noCacheFlag;

            function uriAppearsToAlreadyHaveQueryStringsInIt(uri) {
                return uri.indexOf('?') != -1;
            }
        }
    }
})

        .factory('httpService', ['$http', '$q', 'NoCacheHttpUrlConvertor', 'UIService', function ($http, $q, NoCacheHttpUrlConvertor,UIService) {
    return {
        httpGet:function (urlString) {
            UIService.block();
            var deferred = $q.defer();
            $http({
                      method:'GET',
                      url: NoCacheHttpUrlConvertor.convert(urlString)

                  }).success(
                    function (responseData, status) {
                        deferred.resolve(responseData);
                         UIService.unblock();
                    }).error(function (responseData, status) {
                                 deferred.resolve(responseData);
                                 UIService.unblock();
                             });
            return deferred.promise;
        },
        httpQParamGet:function (urlString, qParams) {
            UIService.block();
            var deferred = $q.defer();
            $http({
                      method:'GET',
                      url:urlString,
                      params:qParams
                  }).success(
                    function (responseData, status) {
                        deferred.resolve(responseData);
                         UIService.unblock();
                    }).error(function (responseData, status) {
                                 deferred.resolve(responseData);
                                  UIService.unblock();
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
        },
        httpPost:function (urlString, payload) {
            var deferred = $q.defer();
            $http({
                      method:'POST',
                      url:urlString,
                      data:payload,
                      headers:{ 'Content-Type':'application/x-www-form-urlencoded; charset=UTF-8'},
                      transformRequest:function (data) {
                          return jQuery.param(data);
                      }
                  }).success(
                    function (responseData, status) {
                        deferred.resolve(responseData);
                    }).error(function (responseData, status) {
                                 deferred.resolve(responseData);
                             });
            return deferred.promise;
        },
        httpPostQParam:function (urlString, payload, qParams) {
            var deferred = $q.defer();
            $http({
                      method:'POST',
                      url:urlString,
                      data:payload,
                      params:qParams,
                      headers:{ 'Content-Type':'application/x-www-form-urlencoded; charset=UTF-8'},
                      transformRequest:function (data) {
                          return jQuery.param(data);
                      }
                  }).success(
                    function (responseData, status) {
                        deferred.resolve(responseData);
                    }).error(function (responseData, status) {
                                 deferred.resolve(responseData);
                             });
            return deferred.promise;
        },
        httpPostFun:function (urlString, payload, callbackFunction) {
            $http({
                      method:'POST',
                      url:urlString,
                      data:payload,
                      headers:{ 'Content-Type':'application/x-www-form-urlencoded; charset=UTF-8'},
                      transformRequest:function (data) {
                          return jQuery.param(data);
                      }
                  }).success(
                    function (responseData, status) {
                        callbackFunction(responseData, status);
                    }).error(function (responseData, status) {
                                 callbackFunction(responseData, status);
                             });
        },
        httpPostQParamFun:function (urlString, qParams, payload, callbackFunction) {
            $http({
                      method:'POST',
                      url:urlString,
                      params:qParams,
                      data:payload,
                      headers:{ 'Content-Type':'application/x-www-form-urlencoded; charset=UTF-8'},
                      transformRequest:function (data) {
                          return jQuery.param(data);
                      }
                  }).success(
                    function (responseData, status) {
                        callbackFunction(responseData, status);
                    }).error(function (responseData, status) {
                                 callbackFunction(responseData, status);
                             });
        },
        httpPostDTO:function (urlString, qParams, payload, callbackFunction) {
            $http({
                      method:'POST',
                      url:urlString,
                      params:qParams,
                      data:payload
                  }).success(
                    function (responseData, status) {
                        try {
                            callbackFunction(responseData, status);
                        } catch (ex) {
                            callbackFunction(responseData, status);
                        }
                    }).error(function (responseData, status) {
                                 try {
                                     callbackFunction(responseData, status);
                                 } catch (ex) {
                                     callbackFunction(responseData, status);
                                 }
                             });
        },
        httpPutDTO:function (urlString, qParams, payload, callbackFunction) {
            $http({
                      method:'PUT',
                      url:urlString,
                      params:qParams,
                      data:payload
                  }).success(
                    function (responseData, status) {
                        try {
                            callbackFunction(responseData, status);
                        } catch (ex) {
                            callbackFunction(responseData, status);
                        }
                    }).error(function (responseData, status) {
                                 try {
                                     callbackFunction(responseData, status);
                                 } catch (ex) {
                                     callbackFunction(responseData, status);
                                 }
                             });
        }
    };
}])


services.factory('QuoteStatsSummaryAdapter', ['Colors', function (Colors) {

    function attachBehaviour(data) {
        data.findStats = function (range, entity) {
            var stats = _.find(data[range].stats, function (stat) {
                return stat.groupBy == entity;
            });
            return _.isUndefined(stats) ? { "groupBy":entity, "quoteOptionCount":0, "lineItemCount":0 } : stats;
        };
    }

    function transform(data) {
        return _.map(data.Total.stats, function (stats) {
            var todayStats = data.findStats('Today', stats.groupBy);
            var yesterdayStats = data.findStats('Yesterday', stats.groupBy);
            var last7daysStats = data.findStats('Last7Days', stats.groupBy);
            var last30dayStats = data.findStats('Last30Days', stats.groupBy);
            var last90dayStats = data.findStats('Last90Days', stats.groupBy);
            var priorTo90dayStats = data.findStats('Total', stats.groupBy);
            return {
                entity:stats.groupBy,
                Today:todayStats.quoteOptionCount + "(" + todayStats.lineItemCount + ")",
                Yesterday:yesterdayStats.quoteOptionCount + "(" + yesterdayStats.lineItemCount + ")",
                Last7Days:last7daysStats.quoteOptionCount + "(" + last7daysStats.lineItemCount + ")",
                Last30Days:last30dayStats.quoteOptionCount + "(" + last30dayStats.lineItemCount + ")",
                Last90Days:last90dayStats.quoteOptionCount + "(" + last90dayStats.lineItemCount + ")",
                Total:priorTo90dayStats.quoteOptionCount + "(" + priorTo90dayStats.lineItemCount + ")"
            };
        });
    }

    return {
        adapt:function (data) {
            attachBehaviour(data);
            return transform(data);
        },
        adaptToPieChart:function (data) {
            return _.map(data.Total.stats, function (stats) {
                return {
                    value:stats.lineItemCount,
                    color:Colors.randomColor(),
                    label:stats.groupBy
                };
            });
        }
    };
}]);

services.factory('QuoteItemStatsAdapter', [function () {
    return {
        adapt:function (data) {
            return data;
        }
    };
}]);

services.factory('ColorUtils', [function () {
    return {
        randomColor:function (data) {
            return data;
        }
    };
}]);

services.factory('Configuration', ['$document', function ($document) {
    return JSON.parse($document.find("#pageContext").text());
}]);

