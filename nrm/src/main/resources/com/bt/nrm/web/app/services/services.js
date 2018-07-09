'use strict';
angular.module('app.services', [])

.factory('UrlConfiguration', ['$document', function ($document) {
    return JSON.parse($document.find("#urlConfig").text());
}])

.factory('RequestStateConstantsService', ['$document', function ($document) {
    return JSON.parse($document.find("#requestStateConstants").text());
}])

.factory('RequestEvaluatorStateConstantsService', ['$document', function ($document) {
    return JSON.parse($document.find("#requestEvaluatorStateConstants").text());
}])

.factory('RequestEvaluatorResponseConstantsService', ['$document', function ($document) {
    return JSON.parse($document.find("#requestEvaluatorResponseConstants").text());
}])

.factory('RequestResponseTypeService', ['$document', function ($document) {
    return JSON.parse($document.find("#requestResponseType").text());
}])

.factory('NRMUserRolesService', ['$document', function ($document) {
    return JSON.parse($document.find("#nrmUserRoles").text());
}])

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
                        callbackFunction(responseData, status);

                    }).error(function (responseData, status) {
                                 callbackFunction(responseData, status);

                             });
        }
    };
}])

.factory('nrmUserService', ['httpService', '$rootScope', 'UrlConfiguration', function (httpService, $rootScope, UrlConfiguration) {
    return {

        getNrmUserByUserId: function (callbackFunction) {
            httpService.httpGetFun(UrlConfiguration.getNrmUserByUserIdUri, callbackFunction);
        },

        getUserByEINOrName: function (EINOrName, callbackFunction) {
            var qParams = {
                EINOrName: EINOrName
            };
            httpService.httpQParamGetFun(UrlConfiguration.getUserByEINOrNameUri, qParams, callbackFunction);
        },

        getUserManagementData: function (userId, callbackFunction) {
            var qParams = {
                userId: userId
            };
            httpService.httpQParamGetFun(UrlConfiguration.getUserManagementDataUri, qParams, callbackFunction);
        },

        getAllGroups: function (callbackFunction) {
            httpService.httpGetFun(UrlConfiguration.getAllGroupsUri, callbackFunction);
        },

        addProductsToUser:function(userDTO,userId, callbackFunction){
            var qParams={
                userId: userId
            }
            httpService.httpPostDTO(UrlConfiguration.addProductsToUserUri, qParams, userDTO, callbackFunction);
        },

        addGroupToUser:function (UserGroupDTO, callbackFunction) {
            httpService.httpPostDTO(UrlConfiguration.addGroupToUserUri, '', UserGroupDTO, callbackFunction);
        },

        deleteGroupFromUser:function (UserGroupDTO, callbackFunction) {
            httpService.httpPostDTO(UrlConfiguration.deleteGroupFromUserUri, '', UserGroupDTO, callbackFunction);
        },

        addRoleToUser:function(userRoleConfigDTO, callbackFunction){
            httpService.httpPostDTO(UrlConfiguration.addRoleToUserUri, '', userRoleConfigDTO, callbackFunction);
        },

        deleteRoleFromUser:function(userRoleConfigDTO, callbackFunction){
            httpService.httpPostDTO(UrlConfiguration.deleteRoleFromUserUri, '', userRoleConfigDTO, callbackFunction);
        },

        getUserStats: function (userId, callbackFunction) {
            var qParams = {
                userId: userId
            };
            httpService.httpQParamGetFun(UrlConfiguration.getUserStatsUri, qParams, callbackFunction);
        }
    };
}])

.factory('ProductTemplateService', ['httpService', '$rootScope', 'UrlConfiguration', function (httpService, $rootScope, UrlConfiguration) {
    return {

        getProductsByUserId: function (userId, callback) {
            var qParams = {
                userId:userId
            };
            httpService.httpQParamGetFun(UrlConfiguration.getProductsByUserIdUri, qParams, callback);
        },

        getAllProducts: function (callback) {
            httpService.httpGetFun(UrlConfiguration.getAllProductsUri, callback);
        },

        getTemplatesByProductId:function (productId, callback) {
            var qParams = {
                productId: productId
            };
            httpService.httpQParamGetFun(UrlConfiguration.getTemplatesByProductIdUri, qParams, callback);
        },

        getTemplateByTemplateCode:function (templateCode, templateVersion, callback) {
            var qParams = {
                templateCode: templateCode,
                templateVersion: templateVersion
            };
            httpService.httpQParamGetFun(UrlConfiguration.getTemplateByTemplateCode, qParams, callback);
        }
    };
}])

 .factory('EvaluatorService', ['httpService', '$rootScope', 'UrlConfiguration', function (httpService, $rootScope, UrlConfiguration) {
    return {

        getListOfEvaluatorActionsUri: function (userId, userGroups, callback) {
            var qParams = {
                userId:userId
            };
            httpService.httpPostDTO(UrlConfiguration.getListOfEvaluatorActionsUri, qParams, userGroups, callback);
        },
        updateEvaluatorPriceGroupUri: function (requestId, evaluatorPriceGroupDTO, modifiedBy, callback) {
            var qParams = {
                requestId:requestId,
                modifiedBy: modifiedBy
            };
            httpService.httpPostDTO(UrlConfiguration.updateEvaluatorPriceGroupUri, qParams, evaluatorPriceGroupDTO, callback);
        },
        startWorkingOnAction: function (requestEvaluatorDTO, callback) {
            httpService.httpPostDTO(UrlConfiguration.startWorkingOnAction, '', requestEvaluatorDTO, callback);
        }

    };
}])

.factory('RequestsService', ['httpService', '$rootScope', 'UrlConfiguration', function (httpService, $rootScope, UrlConfiguration) {
    return {

        getRequestsByUserId:function (userId, callback) {
            var qParams = {
                userId: userId
            };
            httpService.httpQParamGetFun(UrlConfiguration.getRequestsByUserIdUri, qParams, callback);
        },

        getRequestsByUserIdAndStates: function (userId, requestStates, callback) {
            var qParams = {
                userId: userId,
                requestStates: requestStates.join(",")
            };
            httpService.httpQParamGetFun(UrlConfiguration.getRequestsByUserIdAndStatesUri, qParams, callback);
        },

        getRequestByRequestId:function (requestId, callback) {
            var qParams = {
                requestId: requestId
            };
            httpService.httpQParamGetFun(UrlConfiguration.getRequestByRequestIdUri, qParams, callback);
        },

        getDataBuildRequests : function (userId, callback) {
            var qParams = {
                userId: userId
            };
            httpService.httpQParamGetFun(UrlConfiguration.getDataBuildRequestsUri, qParams, callback);
        },

        saveRequestComments:function (requestId, comments, modifiedBy, formData, callback) {
            var qParams = {
                modifiedBy: modifiedBy,
                requestId: requestId,
                comments: comments
            };
            httpService.httpPostQParamFun(UrlConfiguration.saveRequestCommentsUri, qParams, formData, callback);
        },

        saveRequestGroupComments:function (requestGroupId, comments, modifiedBy, formData, callback) {
            var qParams = {
                modifiedBy: modifiedBy,
                requestGroupId: requestGroupId,
                comments: comments
            };
            httpService.httpPostQParamFun(UrlConfiguration.saveRequestGroupCommentsUri, qParams, formData, callback);
        },

        createRequest:function (requestId, formData, callbackFunction) {
            var qParams = {
            };
            return httpService.httpPostDTO(UrlConfiguration.createRequestUri, qParams, requestDTO, callbackFunction);
        },

        updateRequestDetail : function (requestDetailDTO, callbackFunction) {
            return httpService.httpPostDTO(UrlConfiguration.updateRequestDetailUri, null, requestDetailDTO, callbackFunction);
        },

        updateDataBuildStatus : function (requestId,dataBuildCompletedStatus,modifiedBy, callbackFunction) {
            var qParams = {
                requestId : requestId,
                dataBuildCompletedStatus: dataBuildCompletedStatus,
                modifiedBy : modifiedBy
            };
            return httpService.httpPostDTO(UrlConfiguration.updateDataBuildStatusUri, qParams, null, callbackFunction);
        },

        getAllQuoteOptions : function(callbackFunction){
            return httpService.httpGetFun(UrlConfiguration.getAllQuoteOptionsUri,callbackFunction);
        },

        getAllRequestsByQuoteId : function(quoteOptionId,callbackFunction){
            var qParams = {
                quoteOptionId : quoteOptionId
            };
            return httpService.httpQParamGetFun(UrlConfiguration.getAllRequestsByQuoteIdUri,qParams,callbackFunction) ;
        }
    };
}])

.factory('AttachmentService',['httpService', '$http', 'UrlConfiguration',function(httpService, $http, UrlConfiguration){
    return{
        uploadFile:function (file, requestId,userId, $upload, callback) {
            var queryParams = {
                requestId:requestId,
                modifiedBy:userId
            };
            $upload.upload({
                               url:UrlConfiguration.uploadRequestAttachement,
                               method:'POST',
                               params:queryParams,
                               headers:{'file':'file'},
                               file:file,
                               fileFormDataName:'file',
                               progress:true
                           }).progress(function (evt) {
                                           //console.log('percent: ' + parseInt(100.0 * evt.loaded / evt.total));
                                       }).then(function (evt) {
                                                   callback(evt);
                                                   //console.log('Progress :' + parseInt(100.0 * evt.loaded / evt.total));
                                               });
        }
    }
}])




