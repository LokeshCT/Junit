'use strict';

angular.module('cqm.services', [])
        .factory('salesChannelService', ["httpService", 'UrlConfiguration', function (httpService, UrlConfiguration) {
    var cachedCustomerList = [];
    return {
        getCustomers:function (salesChannelName) {
            var qParams = {
                salesChannel:salesChannelName
            };
            return httpService.httpQParamGet(UrlConfiguration.getCustomersByChannelUri, qParams);
        }, cacheCustomers:function (custList) {
            cachedCustomerList = custList;
        }, getCachedCustomerList:function () {
            return cachedCustomerList;
        }
    };
}])

        .factory('salesUserService', ['httpService', '$rootScope', 'UrlConfiguration', function (httpService, $rootScope, UrlConfiguration) {
    return {
        getSalesUser:function (callbackFunction) {
            httpService.httpGetFun(UrlConfiguration.getSalesUserUri, callbackFunction);
        },
        broadcastSalesChannelSelected:function (salesChannel) {
            console.log('Broadcasting salesChannelSelected event : ', salesChannel);
            $rootScope.$broadcast('salesChannelSelectedBc', salesChannel);
        }
    };
}])

        .factory('addressService', ['httpService', '$rootScope', 'UrlConfiguration', function (httpService, $rootScope, UrlConfiguration) {
    return {
        searchAddress:function (nadAddressRequestDTO, callback) {
            return httpService.httpQParamGetFun(UrlConfiguration.searchAddressUri, nadAddressRequestDTO, callback);
        },
        searchAddressWithGeoCode:function (nadAddressRequestDTO, callback) {
            return httpService.httpQParamGetFun(UrlConfiguration.searchAddressByGeoCodeUri, nadAddressRequestDTO, callback);
        },
        broadcastOnLoadAddress:function (addressDto) {
            $rootScope.$broadcast('onLoadAddress', addressDto);
        },
        broadcastResetAddress:function () {
            $rootScope.$broadcast('onResetAddress');
        },
        broadcastSubmitAddress:function () {
            $rootScope.$broadcast('onSubmitAddress');
        }
    };
}])

        .factory('customerContactService', ['httpService', '$http', 'UrlConfiguration', 'PageContext', '$q', function (httpService, $http, UrlConfiguration, PageContext, $q) {
    return {
        getSiteContacts:function (customerId, siteId, callbackFunction) {
            var qParams = {
                customerId:customerId,
                siteId:siteId
            };
            return httpService.httpQParamGetFun(UrlConfiguration.getSiteContactsUri, qParams, callbackFunction);
        },
        isSiteContactValid:function () {
            var qParams = {
                customerId:PageContext.getCustomer().cusId,
                siteId:PageContext.getCentralSiteId()
            };
            var deffered = $q.defer();
            var httpPromise = httpService.httpGet(UrlConfiguration.getSiteContactsUri, qParams);
            var status = false;

            httpPromise.then(function (data) {
                if (!_.isUndefined(data) && !_.isUndefined(data.length) && data.length > 0) {
                    var isPrimaryCustomerContactExist = false;
                    var isMainContactExist = false;
                    var isSitePrimaryExist = false;

                    _.each(data, function (contact) {
                        if (contact.ctpType == "PRIMARY CUSTOMER CONTACT") {
                            isPrimaryCustomerContactExist = true;
                        } else if (contact.ctpType == "Main Contact") {
                            isMainContactExist = true;
                        } else if (contact.ctpType == "Site Primary Contact") {
                            isSitePrimaryExist = true;
                        }
                    })

                    if ((isMainContactExist || isSitePrimaryExist) && isPrimaryCustomerContactExist) {
                        status = true;
                    }
                }
                deffered.resolve(status);
            }, function (error) {
                deffered.resolve(status);
            });

            return deffered.promise;

        },
        createCentralSiteContact:function (userId, customerId, siteId, formData, callbackFunction) {

            var qParams = {
                userId:userId,
                customerId:customerId
            };

            var contactDto = {
                position:formData.jobTitle,
                firstName:formData.firstName,
                lastName:formData.lastName,
                phoneNumber:formData.phoneNumber,
                fax:formData.fax,
                email:formData.email,
                mobileNumber:formData.mobileNumber

            };

            var roleDto = {
                ctpType:formData.role,
                customerId:customerId,
                id:formData.contactRoleId,
                siteId:siteId,
                contact:contactDto
            };


            return httpService.httpPostDTO(UrlConfiguration.createContactUri, qParams, roleDto, callbackFunction);

        },
        updateCentralSiteContact:function (userId, customerId, siteId, formData, callbackFunction) {

            var qParams = {
                userId:userId,
                customerId:customerId
            };

            var contactDto = {
                contactId:formData.contactID,
                position:formData.jobTitle,
                firstName:formData.firstName,
                lastName:formData.lastName,
                phoneNumber:formData.phoneNumber,
                fax:formData.fax,
                email:formData.email,
                mobileNumber:formData.mobileNumber

            };

            var roleDto = {
                ctpType:formData.role,
                customerId:customerId,
                id:formData.contactRoleId,
                siteId:siteId,
                contact:contactDto
            };

            return httpService.httpPostDTO(UrlConfiguration.updateContactUri, qParams, roleDto, callbackFunction);

        }
    };
}])
        .factory('siteService', ['$http', 'UrlConfiguration', function ($http, UrlConfiguration) {
    return {
        createSite:function (userId, customerID, customerName, contractId,siteReference, custDto, addressDto, callbackFunction) {
            var uiData = {
                //userId:userId,
                customerId:customerID,
                customerName:customerName,
                sitReference: siteReference,
                adrSitePremises:addressDto.buildingName,
                subBuilding:addressDto.subBuilding,
                adrStreetNumber:addressDto.buildingNumber,
                adrStreetName:addressDto.street,
                subStreet:addressDto.subStreet,
                adrLocality:addressDto.locality,
                subLocality:addressDto.subLocality,
                adrTown:addressDto.city,
                adrCounty:addressDto.state,
                subCountyStateProvince:addressDto.subState,
                adrCountry:addressDto.country.name,
                adrCountryCode:addressDto.country.codeAlpha2,
                adrPostZipCode:addressDto.postCode,
                subPostCode:addressDto.subPostCode,
                adrPoBoxNumber:addressDto.POBox,
                sitPhoneNumber:addressDto.phoneNumber,
                adrLatitude:addressDto.latitude,
                adrLongitude:addressDto.longitude,
                custValidStatus:custDto.custValidStatus.value
            };

            var qParams = {
                userID:userId,
                contractID:contractId
            };
            $http({
                      method:'POST',
                      url:UrlConfiguration.createSiteUri,
                      data:uiData,
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

        .factory('customerService', ['httpService', '$rootScope', 'UrlConfiguration', 'SessionContext', 'PageContext', '$q', function (httpService, $rootScope, UrlConfiguration, SessionContext, PageContext, $q) {

    var cachedSalesChannelList = undefined;
    var cachedUserSubGroupList = undefined;
    return {
        getCentralSite:function (contractId, customerId, callbackFunction) {
            var qParams = {
                customerID:customerId,
                contractID:contractId
            };
            return httpService.httpQParamGetFun(UrlConfiguration.getCentralSiteUri, qParams, callbackFunction);
        },
        hasValidCentralSite:function () {
            var qParams = {
                userId:SessionContext.getUser().name,
                salesChannel:PageContext.getSalesChannel().name,
                customerName:PageContext.getCustomer().cusName,
                contractID:PageContext.getContract().id
            };

            var deferred = $q.defer();
            var status = false;
            var httpPromise = httpService.httpGet(UrlConfiguration.getCentralSiteUri, qParams);

            httpPromise.then(function (data) {
                if (!_.isUndefined(data) && !_.isEmpty(data.country)) {
                    status = true;
                }
                deferred.resolve(status);
            }, function (error) {
                deferred.resolve(status);
            });

            return deferred.promise;
        },
        getMatchingCustomers:function (salesChannelName, customerName, paginationDetail) {
            var qParams = {
                salesChannel:salesChannelName,
                customerName:customerName,
                startIndex:paginationDetail.pageNo,
                pageSize:paginationDetail.pageSize,
                sortColumn:paginationDetail.sortCol,
                sortOrder:paginationDetail.sortOrder
            };
            return httpService.httpQParamGet(UrlConfiguration.searchCustomerUri, qParams);
        },
        getSelectedCustomerContacts:function (salesChannelName, customerId) {
            var qParams = {
                salesChannel:salesChannelName,
                customerId:customerId
            };
            return httpService.httpQParamGet(UrlConfiguration.getCustomerContactsUri, qParams);
        },
        getCustomer:function (customerId) {
            var qParams = {
                customerId:customerId
            };
            return httpService.httpQParamGet(UrlConfiguration.getCustomerDetailUri, qParams);
        },
        updateCustomer:function (cusId, validStatus) {
            var promise;
            if (!_.isUndefined(cusId)) {
                promise = this.getCustomer(cusId)
            } else if (!_.isUndefined(validStatus)) {
                PageContext.updateCustStatus(validStatus);
                return;
            } else {
                promise = this.getCustomer(PageContext.getCustomer.cusId)
            }


            promise.then(function (customerDto) {
                PageContext.updateCustStatus(customerDto.custValidStatus);
            })

        },
        broadcastCustomerSelectionChanged:function (salesChannel, custId) {
            console.log('Broadcasting customer selection changed : ', salesChannel, custId);
            $rootScope.$broadcast(EVENT.CustomerSelectionChanged, salesChannel, custId);
        },
        createCustomer:function (salesChannelName, customerName, status, contractFriendlyName,contractCeaseTerm,contractLinkedCeaseTerm, callbackFunc) {
            console.log('Creating new customer now');
            var customerCreationDTO = new Object();
            customerCreationDTO.userId = SessionContext.getUser().ein;
            customerCreationDTO.userName = SessionContext.getUser().name;
            customerCreationDTO.cusName = customerName;
            customerCreationDTO.custValidStatus = status;
            customerCreationDTO.salesChannel = salesChannelName;

            var qParam = {
                contractFriendlyName:contractFriendlyName,
                contractCeaseTerm : contractCeaseTerm,
                contractLinkedCeaseTerm : contractLinkedCeaseTerm
            };

            httpService.httpPostDTO(UrlConfiguration.createCustomerUri, qParam, customerCreationDTO, callbackFunc);

        }, updateCustomerAdditionalDetail:function (customerAddDetDto, callbackFunc) {
            console.log('Updating Customer Additional Detail')

            httpService.httpPostDTO(UrlConfiguration.updateCustomerAddDetUri, undefined, customerAddDetDto, callbackFunc);

        }, getCustomerAdditionalDetail:function (customerId, callbackFunc) {
            console.log('Get Customer Additional Detail')
            var qParam = {
                customerID:customerId
            };

            httpService.httpQParamGetFun(UrlConfiguration.getCustomerAddDetUri, qParam, callbackFunc);

        },
        associateCentralSite:function (siteId, callbackFunction) {
            var qParams = {
                contractId:PageContext.getContract().id,
                siteId:siteId
            };
            return httpService.httpPostDTO(UrlConfiguration.associateCentralSiteUri, qParams, undefined, callbackFunction);
        },
        associateSalesChannelToContract:function (salesChannel, contractId, callbackFunction) {
            var qParams = {
                salesChannel:salesChannel
            };
            return httpService.httpPostDTO(UrlConfiguration.associateSalesChannelToContractUri + '/' + contractId, qParams, undefined, callbackFunction);
        },
        getAllAvailableSalesChannel:function () {

            if (_.isUndefined(cachedSalesChannelList)) {
                return httpService.httpGet(UrlConfiguration.getAllAvailableSalesChannelUri, undefined).then(function (data) {
                    cachedSalesChannelList = data;
                    cachedSalesChannelList.splice(0, 0, {orgName:"", roleId:""})
                });
            }

            return cachedSalesChannelList;
        },
        getUserSubGroupList:function () {

            if (_.isUndefined(cachedUserSubGroupList)) {
                return httpService.httpGet(UrlConfiguration.getUserSubGroupsUri, undefined).then(function (data) {
                    cachedUserSubGroupList =data;
                });
            }

            return cachedUserSubGroupList;
        },
        getPortDistributor:function (portRoleId, callbackFunc) {

            var qParams = {
                portRoleId:portRoleId
            };

            return httpService.httpQParamGetFun(UrlConfiguration.getPortDistributorUri, qParams, callbackFunc);
        },
        getAllSalesChannelsWithGfrCode:function (callbackFunc) {
            return httpService.httpQParamGetFun(UrlConfiguration.getAllSalesChannelWithGfrUri, undefined, callbackFunc);
        }
    }

}])

        .factory('contractService',function (httpService, $rootScope, UrlConfiguration, PageContext,UIService,$q) {
      var clientGroupCache;
    return {
        getContracts:function (salesChannel, customerId, callback) {
            var qParam = {salesChannel:salesChannel, customerId:customerId};
            httpService.httpQParamGetFun(UrlConfiguration.getContractUri, qParam, callback);
        },
        getContractById:function () {
            var deferred = $q.defer();
            UIService.block();
            var qParam = {contractID:PageContext.getContract().id};
            httpService.httpQParamGetFun(UrlConfiguration.getContractByIdUri, qParam, function(data,status){
                if('200'==status){
                    deferred.resolve(data);
                }else {
                        UIService.handleException('Get Contract', data, status);
                }
                UIService.unblock();
            });

            return deferred.promise;
        },
        updateContract:function (contractDto) {
            var deferred = $q.defer();
            UIService.block();
            httpService.httpPostDTO(UrlConfiguration.updateContractUri, undefined, contractDto, function(data,status){
                if ('200' == status) {
                    UIService.openDialogBox('Update Contract', 'Update Successful', true, false);
                } else {
                    UIService.handleException('Update Contract', data, status);
                }
                UIService.unblock();
            });
        },
        getClientGroups:function () {
            var deferred = $q.defer();
            if(_.isUndefined(clientGroupCache)){

                httpService.httpQParamGetFun(UrlConfiguration.getClientGroupsUri, undefined, function(data,status){
                    if('200'==status){
                        clientGroupCache = data;
                        deferred.resolve(clientGroupCache);
                    }else{
                        var msg = 'Couldnt fetch Client Group. Status :'+status;
                        deferred.reject(msg);
                        console.log(msg);
                    }
                });

            }else{
                deferred.resolve(clientGroupCache);
            }

            return deferred.promise;
        }
    };
})


        .factory('countryService', ['httpService', '$rootScope', 'UrlConfiguration', function (httpService, $rootScope, UrlConfiguration) {
    var allCountriesData = undefined;
    var mandatoryProviceCountries = ['AUSTRALIA', 'BRAZIL', 'CANADA', 'CHINA', 'INDIA', 'JAPAN', 'UNITED STATES'];
    var mandatoryZipCountries = ['AALAND ISLANDS', 'ALGERIA', 'AMERICAN SAMOA', 'ANDORRA', 'ANGUILLA', 'ANTARCTICA', 'ARGENTINA', 'AUSTRIA', 'AZERBAIJAN', 'BAHRAIN', 'BANGLADESH', 'BARBADOS',
        'BELARUS', 'BELGIUM', 'BERMUDA', 'BHUTAN', 'BOSNIA AND HERZEGOVINA', 'BOUVET ISLAND', 'BRUNEI DARUSSALAM', 'BULGARIA', 'CAMBODIA', 'CAPE VERDE', 'CAYMAN ISLANDS',
        'CHILE', 'CHRISTMAS ISLAND', 'COCOS (KEELING) ISLANDS', 'COLOMBIA', 'CONGO, THE DEMOCRATIC REPUBLIC OF THE', 'COSTA RICA', 'COTE D\'IVOIRE', 'CROATIA', 'CUBA',
        'CYPRUS', 'CZECH REPUBLIC', 'DENMARK', 'DOMINICAN REPUBLIC', 'ECUADOR', 'EGYPT', 'EL SALVADOR', 'ESTONIA', 'ETHIOPIA', 'FALKLAND ISLANDS (MALVINAS)', 'FAROE ISLANDS',
        'FINLAND', 'FRANCE', 'FRENCH GUIANA', 'FRENCH POLYNESIA', 'FRENCH SOUTHERN TERRITORIES', 'GEORGIA', 'GERMANY', 'GIBRALTAR', 'GREECE', 'GREENLAND', 'GUADELOUPE', 'GUAM',
        'GUATEMALA', 'GUERNSEY', 'GUINEA', 'GUINEA-BISSAU', 'HAITI', 'HOLY SEE (VATICAN CITY STATE)', 'HONDURAS', 'HUNGARY', 'INDIA', 'ICELAND', 'INDONESIA', 'IRAN, ISLAMIC REPUBLIC OF',
        'IRAQ', 'ISLE OF MAN', 'ISRAEL', 'ITALY', 'JAMAICA', 'JERSEY', 'JORDAN', 'KAZAKHSTAN', 'KENYA', 'KOREA, DEMOCRATIC PEOPLES REPUBLIC OF', 'KOREA, REPUBLIC OF', 'KUWAIT',
        'KYRGYZSTAN', 'LAO PEOPLES DEMOCRATIC REPUBLIC', 'LATVIA', 'LEBANON', 'LESOTHO', 'LIBERIA', 'LIBYAN ARAB JAMAHIRIYA', 'LIECHTENSTEIN', 'LITHUANIA', 'LUXEMBOURG',
        'MACEDONIA, THE FORMER YUGOSLAV REPUBLIC', 'MADAGASCAR', 'MALAYSIA', 'MALDIVES', 'MALTA', 'MARSHALL ISLANDS', 'MARTINIQUE', 'MAURITIUS', 'MAYOTTE', 'MEXICO', 'MICRONESIA \,FEDERATED STATES OF', 'MOLDOVA, REPUBLIC OF', 'MONACO', 'MONGOLIA', 'MONTENEGRO', 'MOROCCO', 'MOZAMBIQUE', 'MYANMAR', 'NEPAL', 'NETHERLANDS', 'NEW CALEDONIA', 'NEW ZEALAND',
        'NICARAGUA', 'NIGER', 'NIGERIA', 'NORFOLK ISLAND', 'NORTHERN MARIANA ISLANDS', 'NORWAY', 'OMAN', 'PAKISTAN', 'PALAU', 'PALESTINIAN TERRITORY, OCCUPIED', 'PANAMA',
        'PAPUA NEW GUINEA', 'PARAGUAY', 'PERU', 'PHILIPPINES', 'POLAND', 'PORTUGAL', 'PUERTO RICO', 'REUNION', 'ROMANIA', 'RUSSIAN FEDERATION', 'SAINT HELENA',
        'SAINT PIERRE AND MIQUELON', 'SAINT VINCENT AND THE GRENADINES', 'SAN MARINO', 'SAUDI ARABIA', 'SENEGAL', 'SERBIA', 'SINGAPORE', 'SLOVAKIA', 'SLOVENIA', 'SOMALIA',
        'SOUTH AFRICA', 'SPAIN', 'SRI LANKA', 'SUDAN', 'SVALBARD AND JAN MAYEN', 'SWAZILAND', 'SWEDEN', 'SWITZERLAND', 'TAIWAN, PROVINCE OF CHINA', 'TAJIKISTAN', 'THAILAND',
        'TIMOR-LESTE', 'TUNISIA', 'TURKEY', 'TURKMENISTAN', 'TURKS AND CAICOS ISLANDS', 'UKRAINE', 'UNITED KINGDOM', 'UNITED STATES MINOR OUTLYING ISLANDS', 'URUGUAY',
        'UZBEKISTAN', 'VENEZUELA', 'VIRGIN ISLANDS, BRITISH', 'VIRGIN ISLANDS, U.S.', 'WALLIS AND FUTUNA', 'WESTERN SAHARA', 'ZAMBIA'];
    return {
        getAllCountries:function () {
            if (allCountriesData == undefined) {
                httpService.httpGet(UrlConfiguration.getCountiesUri).then(function (data) {
                    if (_.isArray(data)) {
                        allCountriesData = data;
                        allCountriesData.splice(0, 0, {name:""})
                    } else {
                        allCountriesData = data.countryDTO;
                        allCountriesData.splice(0, 0, {name:""})
                    }
                });
            }

            return allCountriesData;
        },

        broadcastSelectedCountryChangedEvent:function (countryName) {
            $rootScope.$broadcast('selectedCountryChanged', countryName);
        },
        isProvinceRequired:function (countryName) {
            var hasMatch = _.find(mandatoryProviceCountries, function (aCountry) {
                                      if (aCountry == countryName) {
                                          return true;
                                      }
                                  }
            )

            if (_.isUndefined(hasMatch)) {
                return false;
            } else {
                return true;
            }
        },
        isZipRequired:function (countryName) {
            var hasMatch = _.find(mandatoryZipCountries, function (aCountry) {
                                      if (aCountry == countryName) {
                                          return true;
                                      }
                                  }
            )

            if (_.isUndefined(hasMatch)) {
                return false;
            } else {
                return true;
            }
        }
    };
}])

        .factory('quoteService', ['httpService', 'UrlConfiguration', 'PageContext', function (httpService, UrlConfiguration, PageContext) {
    return {
        createQuoteService:function (userId, createQuoteFormData,quotePriceBooks, callbackFunction) {
            var qParams = {
                userId:userId ,
                quotePriceBooks:JSON.stringify(quotePriceBooks)
            };
            httpService.httpPostQParamFun(UrlConfiguration.createQuoteUri, qParams, createQuoteFormData, function (responsedata, status) {
                callbackFunction(responsedata, status);
            });
        },
        updateQuote:function (userId, updateQuoteFormData, callbackFunction) {
            var qParams = {
            };
            var quoteDto = {
                quoteRefID:updateQuoteFormData.quoteRefId,
                customerName:updateQuoteFormData.custName,
                quoteVersion:updateQuoteFormData.quoteVer,
                saleschannel:updateQuoteFormData.salesOrgName,
                EIN:userId,
                quoteName:updateQuoteFormData.quoteName,
                quoteIndicativeFlag:updateQuoteFormData.quoteIndFlag.name,
                opportunityReferenceNumber:updateQuoteFormData.opportunityRef
            };
            httpService.httpPutDTO(UrlConfiguration.updateQuoteUri, qParams, quoteDto, callbackFunction);
        },
        getQuotes:function (salesChannel, customerID, siteId) {
            var qParams = {
                salesChannel:salesChannel,
                customerID:customerID,
                siteId:siteId

            };
            return httpService.httpQParamGet(UrlConfiguration.searchQuoteUri, qParams);
        },
        countQuotes:function () {
            var qParams = {
                salesChannel:PageContext.getSalesChannel().name,
                customerID:PageContext.getCustomer().cusId
            };
            return httpService.httpGet(UrlConfiguration.searchQuoteUri, qParams);
        },
        getChannelContacts:function (quoteId, callbackFunction) {
            var url = "/cqm/quotes/getChannelContacts";
            var qParams = {
                quoteId:quoteId
            };
            return httpService.httpQParamGetFun(url, qParams, callbackFunction);
        },
        getBundlingAppURL:function (quoteId, quoteVersion, userId, callbackFunction) { /*get GUID for Existing Quote.*/

            var qParams = {
                quoteId:quoteId,
                quoteVersion:quoteVersion,
                userId:userId
            };
            httpService.httpQParamGetFun(UrlConfiguration.bundlingAppUrl, qParams, function (responsedata, status) {
                callbackFunction(responsedata, status);
            });
        },
        getSqeAppURL:function (quoteId, quoteVersion, quoteHeaderId, userId, callbackFunction) { /*get GUID for Existing Quote.*/

            var qParams = {
                quoteId:quoteId,
                quoteVersion:quoteVersion,
                quoteHeaderId:quoteHeaderId,
                userId:userId
            };
            httpService.httpQParamGetFun(UrlConfiguration.sqeAppUrl, qParams, function (responsedata, status) {
                callbackFunction(responsedata, status);
            });
        },
        getBTdirectoryService:function (ein, fname, lname, email) {
            var url = "/cqm/searchBTDirectory";
            var qParams = {
                ein:ein,
                firstName:fname,
                lastName:lname,
                email:email
            };
            return httpService.httpQParamGet(url, qParams);
        }

    };
}])
        .factory('channelContactService', function ($http) {
                     return{
                         createUpdateDeleteChannelContact:function (quoteId, custId, opType, channelContactsForm, callbackFunction) {
                             var url = "";
                             if (opType == 'save') {
                                 url = "/cqm/quotes/createQuoteChannelContact";
                             }

                             if (opType == 'update') {
                                 url = "/cqm/quotes/updateQuoteChannelContact";
                             }
                             if (opType == 'delete') {
                                 url = "/cqm/quotes/deleteQuoteChannelContact";
                             }

                             var qParams = {
                                 "channelContactID":channelContactsForm.channelContactId,
                                 "quoteID":quoteId,
                                 "ein":channelContactsForm.ein,
                                 "firstName":channelContactsForm.firstName,
                                 "lastName":channelContactsForm.lastName,
                                 "jobTitle":channelContactsForm.jobTitle,
                                 "phoneNumber":channelContactsForm.phone,
                                 "mobileNumber":channelContactsForm.mobile,
                                 "fax":channelContactsForm.fax,
                                 "email":channelContactsForm.email,
                                 "role":channelContactsForm.distributorRole.name,
                                 "customerID":custId
                             };

                             $http.post(url, qParams)
                                     .success(
                                     function (responseData, status) {
                                         callbackFunction(responseData, status);
                                     }).error(function (responseData, status) {
                                                  callbackFunction(responseData, status);
                                              });
                         }


                     }
                 })

        .factory('UIService', [ '$q','$rootScope', function ($q,$rootScope) {
    var dialogScope;
    var blockCount = 0;
    return {
        block:function (message) {
            blockCount++;
            $.blockUI({ message:'<div style="height: 60px;text-align: center;"><b style="font: 13px">Please wait..</b></div>',
                          css:{
                              background:'url("/cqm/static/cqm/web/css/images/ajax-loader.gif") no-repeat scroll 50% 20px #595E62'
                          }
                      });

        },
        unblock:function () {
            if (blockCount > 0) {
                if((--blockCount)<1){
                    $.unblockUI();
                }
            }

        },
        initializeDialog:function (scope) {
            dialogScope = scope;
        },
        openDialogBox:function (hTitle, messageBody, showOkButton, showCancelButton, stack) {
            var deferred = $q.defer();
            messageBody = _.isUndefined(messageBody) ? '' : messageBody;
            if (!_.isUndefined(dialogScope)) {
                dialogScope.message = {};
                dialogScope.message.content = messageBody;
                dialogScope.message.title = hTitle;
                dialogScope.message.stack = _.isUndefined(stack) ? '' : stack;
                dialogScope.hideDetailsButton = _.isUndefined(stack) ? true : false;
                dialogScope.hideCancelButton = _.isUndefined(showCancelButton) ? true : !showCancelButton;
                dialogScope.hideOkButton = _.isUndefined((showOkButton)) ? true : !showOkButton;

                dialogScope.ok = function () {
                    dialogScope.show = false;
                    if (blockCount > 0) {
                        $.blockUI({ message:'<div style="height: 60px;text-align: center;"><b style="font: 13px">Please wait..</b></div>',
                                      css:{
                                          background:'url("/cqm/static/cqm/web/css/images/ajax-loader.gif") no-repeat scroll 50% 20px #595E62'
                                      }
                                  });
                    }
                    deferred.resolve();
                };

                dialogScope.cancel = function () {
                    deferred.reject();
                };
                if (blockCount > 0) {
                    this.unblock();
                    blockCount++;
                }
                dialogScope.show = true;

                if(!$rootScope.$$phase){
                    $rootScope.$apply();
                }
            }
            return {result:deferred.promise};

        },
        handleException:function (hTitle, error, status) {
            var errorDesc = '';
            var errorCode = '';

            if (_.isString(error)) {
                errorDesc = error;
                errorCode = 'unknown';
            } else if (_.isUndefined(error) || _.isNumber(error)) {
                errorDesc = '';
                errorCode = 'unknown';
            } else if (_.isNull(error) || _.isNumber(error)) {
                errorDesc = '';
                errorCode = 'unknown';
            } else {
                errorDesc = error.description;
                errorCode = error.errorId;
            }

            console.log('Title :' + hTitle + '.\n. Status:' + status + '\n Error Code:' + errorCode + '.\n Error Description:' + errorDesc);
            if (status == '500') {
                this.openDialogBox(hTitle, 'Internal Server Error. Error Code ::' + errorCode, true, false, errorDesc);
                this.unblock();
            } else if (status == '400') {
                this.openDialogBox(hTitle, 'Bad Request. Error Code ::' + errorCode, true, false, errorDesc);
                this.unblock();
            } else if (status == '404') {
                this.openDialogBox(hTitle, 'Resource Not Found. Error Code ::' + errorCode, true, false, errorDesc);
                this.unblock();
            } else if (status == '503') {
                this.openDialogBox('Service Unavailable', 'Service Unavailable. Error Code ::' + errorCode, true, false, errorDesc);
                this.unblock();
            } else if (status == '417') {
                this.openDialogBox('Expectation Failure', 'Unexpected Error. Error Code ::' + errorCode, true, false, errorDesc);
                this.unblock();
            } else if (status == '412') {
                this.openDialogBox('Precondition Failed', 'Precondition Failed. Error Code ::' + errorCode, true, false, errorDesc);
                this.unblock();
            } else if (status == '409') {
                this.openDialogBox('Data Conflict', 'Data conflict. Error Code ::' + errorCode, true, false, errorDesc);
                this.unblock();
            }
        }
    };
}])
        .factory('vpnService', ['httpService', '$http', 'UrlConfiguration', 'PageContext', function (httpService, $http, UrlConfiguration, PageContext) {

    return {

        getVPNDetails:function (customerId, callbackFunction) {

            var qParams = {
                customerId:customerId
            };
            return httpService.httpQParamGetFun(UrlConfiguration.getVpnUri, qParams, callbackFunction);
        },
        countVPNDetails:function () {

            var qParams = {
                customerId:PageContext.getCustomer().cusId
            };
            return httpService.httpGet(UrlConfiguration.getVpnUri, qParams);
        },
        getSharedVPNDetails:function (customerId, callbackFunction) {

            var qParams = {
                customerId:customerId
            };
            return httpService.httpQParamGetFun(UrlConfiguration.getSharedVpnDetailsUri, qParams, callbackFunction);

        },
        createSharedVPNDetails:function (vpnServiceID, customerID, selectedSharedCustomer, callbackFunction) {

            var qParams = {
                vpnServiceID:vpnServiceID,
                customerID:customerID
            };
            return httpService.httpQParamGetFun(UrlConfiguration.createSharedVpnDetailsUri, qParams, callbackFunction);
        },


        deleteSharedVPNDetails:function (vpnServiceID, customerID) {

            var qParams = {
                vpnServiceID:vpnServiceID,
                customerID:customerID
            };
            return httpService.httpQParamGetFun(UrlConfiguration.deleteSharedVpnDetailsUri, qParams, callbackFunction);
        }

    };
}])

        .factory('legalEntityService', ['httpService', '$rootScope', 'UrlConfiguration', 'PageContext', function (httpService, $rootScope, UrlConfiguration, PageContext) {
    return {
        getLegalEntity:function (salesUserId, salesChannelName, customerId, callbackFunction) {
            var qParams = {
                customerID:customerId,
                dateTime:new Date().getTime()
            };
            return httpService.httpQParamGetFun(UrlConfiguration.getLegalEntityUri, qParams, callbackFunction);
        }, countLegalEntity:function () {
            var qParams = {
                customerID:PageContext.getCustomer().cusId
            };
            return httpService.httpGet(UrlConfiguration.getLegalEntityUri, qParams);
        },
        createLegalEntity:function (userId, salesChannel, customerID, formData, callbackFunction) {

            var qParams = {
                userId:userId,
                salesChannel:salesChannel,

                customerId:customerID
            }
            return httpService.httpPostQParamFun(UrlConfiguration.createLegalEntityUri, qParams, formData, callbackFunction);

        },
        updateLegalEntity:function (userId, salesChannel, customerID, formData, callbackFunction) {

            var qParams = {
                userId:userId,
                salesChannel:salesChannel,
                customerId:customerID
            }
            return httpService.httpPostQParamFun(UrlConfiguration.updateLegalEntityUri, qParams, formData, callbackFunction);
        },

        getVatPrefixForSelectedCountry:function (country, callbackFunction) {

            var qParams = {
                countryName:country
            }
            return httpService.httpQParamGetFun(UrlConfiguration.vatPrefixUri, qParams, callbackFunction);
        }
    }

}])
        .factory('branchSiteService', ['httpService', '$rootScope', 'UrlConfiguration', '$http', 'PageContext', 'UserContext', function (httpService, $rootScope, UrlConfiguration, $http, PageContext, UserContext) {
    return {
        getBranchSite:function (salesUserId, salesChannelName, customerId, callbackFunction) {
            var qParams = {
                userId:salesUserId,
                salesChannel:salesChannelName,
                customerId:customerId
            };
            return httpService.httpQParamGetFun(UrlConfiguration.getBranchSiteUri, qParams, callbackFunction);
        }/*,
         getSelectedCustomerContacts:function (salesChannelName, customerId) {
         var qParams = {
         salesChannel:salesChannelName,
         customerId:customerId
         }
         return httpService.httpQParamGet(UrlConfiguration.getSelectedCustomerContactsUri, qParams);
         }*/,
        getBranchSiteNamesIds:function (salesUserId, salesChannelName, customerId, callbackFunction) {
            var qParams = {
                userId:salesUserId,
                salesChannel:salesChannelName,
                customerId:customerId
            };
            return httpService.httpQParamGetFun(UrlConfiguration.getBranchSiteNamesIdsUri, qParams, callbackFunction);
        },
        countBranchSite:function () {
            var qParams = {
                customerId:PageContext.getCustomer().cusId
            };
            return httpService.httpGet(UrlConfiguration.getBranchSiteCountUri, qParams);
        },
        createBranchSite:function (userId, customerID, customerName, addressDto, siteDto, callbackFunction) {

            var location = {
                locSitId:siteDto.sitId,
                locFloor:addressDto.floor,
                locRoom:addressDto.room,
                locSubLocationId:siteDto.locSubLocationId,
                locSubPremise:siteDto.locSubPremise
            };

            var uiData = {
                sitId:siteDto.sitId,
                sitName:siteDto.siteName,
                sitCustName:siteDto.sitFriendlyName,
                localCompanyName:siteDto.localCompanyName,
                sitNetworkReportingRef:siteDto.sitNetworkReportingRef,
                sitComment:siteDto.comment,
                createNewRegion:siteDto.isNewRegion,
                sitReference:siteDto.sitReference,
                sitCusReference:siteDto.sitCusReference,
                sitCusRegion:siteDto.sitCusRegion,
                customerId:customerID,
                customerName:customerName,
                adrSitePremises:addressDto.buildingName,
                subBuilding:addressDto.subBuilding,
                adrStreetNumber:addressDto.buildingNumber,
                adrStreetName:addressDto.street,
                subStreet:addressDto.subStreet,
                adrLocality:addressDto.locality,
                subLocality:addressDto.subLocality,
                adrTown:addressDto.city,
                adrCounty:addressDto.state,
                subCountyStateProvince:addressDto.subState,
                adrCountry:addressDto.country.name,
                adrCountryCode:addressDto.country.codeAlpha2,
                adrPostZipCode:addressDto.postCode,
                subPostCode:addressDto.subPostCode,
                adrPoBoxNumber:addressDto.POBox,
                sitPhoneNumber:addressDto.phoneNumber,
                adrLatitude:addressDto.latitude,
                adrLongitude:addressDto.longitude,
                adrAccuracyLevel:addressDto.adrAccuracyLevel,
                adrValidationLevel:addressDto.adrValidationLevel,

                location:location

            };
            var qParams = {
                userId:userId,
                customerName:customerName,
                customerId:customerID
            };

            httpService.httpPostDTO(UrlConfiguration.createBranchSiteUri, qParams, uiData, callbackFunction);


        },
        updateBranchSite:function (userId, customerID, customerName, addressDto, siteDto, callbackFunction) {

            var location = {
                locSitId:siteDto.sitId,
                locFloor:addressDto.floor,
                locRoom:addressDto.room,
                locSubLocationId:siteDto.locSubLocationId,
                locSubPremise:siteDto.locSubPremise
            };

            var uiData = {
                //userId:userId,
                sitId:siteDto.sitId,
                sitName:siteDto.siteName,
                sitType:siteDto.siteType,
                sitCustName:siteDto.sitFriendlyName,
                localCompanyName:siteDto.localCompanyName,
                sitNetworkReportingRef:siteDto.sitNetworkReportingRef,
                sitReference:siteDto.sitReference,
                sitCusReference:siteDto.sitCusReference,
                sitCusRegion:siteDto.sitCusRegion,
                sitComment:siteDto.comment,
                createNewRegion:siteDto.isNewRegion,
                customerId:customerID,
                customerName:customerName,
                adrSitePremises:addressDto.buildingName,
                subBuilding:addressDto.subBuilding,
                adrStreetNumber:addressDto.buildingNumber,
                adrStreetName:addressDto.street,
                subStreet:addressDto.subStreet,
                adrLocality:addressDto.locality,
                subLocality:addressDto.subLocality,
                adrTown:addressDto.city,
                adrCounty:addressDto.state,
                subCountyStateProvince:addressDto.subState,
                adrCountry:addressDto.country.name,
                adrCountryCode:addressDto.country.codeAlpha2,
                adrPostZipCode:addressDto.postCode,
                subPostCode:addressDto.subPostCode,
                adrPoBoxNumber:addressDto.POBox,
                sitPhoneNumber:addressDto.phoneNumber,
                adrLatitude:addressDto.latitude,
                adrLongitude:addressDto.longitude,
                notifySiteUpdate:siteDto.notifySiteUpdate,
                adrAccuracyLevel:addressDto.adrAccuracyLevel,
                adrValidationLevel:addressDto.adrValidationLevel,
                location:location
            };
            var qParams = {
                userId:userId,
                customerName:customerName,
                customerId:customerID
            };

            httpService.httpPostDTO(UrlConfiguration.updateBranchSiteUri, qParams, uiData, callbackFunction);


        },
        createBranchSiteContact:function (userId, customerId, siteId, formData, callbackFunction) {

            var qParams = {
                userId:userId,
                customerId:customerId
            }
            //return httpService.httpPostQParamFun(UrlConfiguration.createBranchSiteContactUri, qParams, formData, callbackFunction);

            var contactDto = {
                contactId:formData.contactID,
                position:formData.jobTitle,
                firstName:formData.firstName,
                lastName:formData.lastName,
                phoneNumber:formData.phoneNumber,
                fax:formData.fax,
                email:formData.email,
                mobileNumber:formData.mobileNumber

            };

            var roleDto = {
                ctpType:formData.role,
                customerId:customerId,
                id:formData.contactRoleId,
                siteId:siteId,
                contact:contactDto
            };

            return httpService.httpPostDTO(UrlConfiguration.createBranchSiteContactUri, qParams, roleDto, callbackFunction);

        },

        updateBranchSiteContact:function (userId, customerId, siteId, formData, callbackFunction) {

            var qParams = {
                userId:userId,
                customerId:customerId
            }
            //return httpService.httpPostQParamFun(UrlConfiguration.updateBranchSiteContactUri, qParams, formData, callbackFunction);

            var contactDto = {
                contactId:formData.contactID,
                position:formData.jobTitle,
                firstName:formData.firstName,
                lastName:formData.lastName,
                phoneNumber:formData.phoneNumber,
                fax:formData.fax,
                email:formData.email,
                mobileNumber:formData.mobileNumber

            };

            var roleDto = {
                ctpType:formData.role,
                customerId:customerId,
                id:formData.contactRoleId,
                siteId:siteId,
                contact:contactDto
            };

            return httpService.httpPostDTO(UrlConfiguration.updateBranchSiteContactUri, qParams, roleDto, callbackFunction);

        },
        createLocation:function (siteId, room, floor, subLocName, callback) {

            var locDto = {
                locSitId:siteId,
                locFloor:floor,
                locRoom:room,
                locSubPremise:subLocName
            }

            return httpService.httpPostDTO(UrlConfiguration.createLocationUri, undefined, locDto, callback);
        },
        getSiteStatus:function (siteId, callback) {

            var qParams = {
                siteId:siteId
            }
            return httpService.httpQParamGetFun(UrlConfiguration.getSiteStatus, qParams, callback);
        },
        getSiteRegions:function (customerId, countryCode, callback) {

            var qParams = {
                countryCode:countryCode,
                customerId:customerId
            }
            return httpService.httpQParamGetFun(UrlConfiguration.getSiteRegions, qParams, callback);
        },

        siteUpdateNotification:function (siteId, callback) {

            var qParams = {
                siteId:siteId
            }
            return httpService.httpQParamGetFun(UrlConfiguration.siteUpdateNotification, qParams, callback);
        },
        getAPOPs:function (callback) {

            return httpService.httpQParamGetFun(UrlConfiguration.getAPOPsUri, undefined, callback);
        },
        getGPOPs:function (gpopFilter, callback) {
            return httpService.httpPostDTO(UrlConfiguration.getGPOPsUri, undefined, gpopFilter, callback);
        }
    }
}])
        .factory('httpService', ['$http', '$q', function ($http, $q) {
    return {
        httpGet:function (urlString, qParams) {
            var deferred = $q.defer();
            $http({
                      method:'GET',
                      url:urlString,
                      params:qParams

                  }).success(
                    function (responseData, status) {
                        deferred.resolve(responseData);
                    }).error(function (responseData, status) {
                                 deferred.reject(responseData);
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
                                 deferred.reject(responseData);
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
}]).factory('channelHierarchyService', ['httpService', '$http', 'UrlConfiguration', 'PageContext', '$q', function (httpService, $http, UrlConfiguration, PageContext, $q) {
    return {
        getParentAccounts:function (accountTypeName, salesChannel, callbackFunction) {

            var qParams = {
                accountType:accountTypeName,
                salesChannel:salesChannel
            };
            return httpService.httpQParamGetFun(UrlConfiguration.getParentAccountsUri, qParams, callbackFunction);
        },
        createChannelPartner:function (customerId, accountType, pAccName, pACRef, bAC, yCR, cType, tLevel, salesChannel, customerName, callbackFunction) {
            var qParams = {
                customerId:customerId,
                accountType:accountType,
                parentCustomerName:pAccName,
                parentAccountReference:pACRef,
                billingAccount:bAC,
                yearlyCommittedRev:yCR,
                salesChannelType:cType,
                tradeLevel:tLevel,
                salesChannelOrg:salesChannel,
                customerName:customerName
            };
            return httpService.httpQParamGetFun(UrlConfiguration.createChannelPartnerUri, qParams, callbackFunction);

        },
        getParentAccountNames:function (accountType, salesChannel) {
            var qParams = {
                accountType:accountType,
                salesChannel:salesChannel
            };
            return httpService.httpQParamGet(UrlConfiguration.getParentAccountNamesUri, qParams);
        },

        getChannelCreationDetails:function (parentAccountName, customerId, callbackFunction) {

            var qParams = {
                parentAccountName:parentAccountName,
                customerId:customerId
            };
            return httpService.httpQParamGetFun(UrlConfiguration.getChannelCreationDetailsUri, qParams, callbackFunction);
        },

        loadChannelPartnerDetails:function (customerId, callbackFunction) {

            var qParams = {
                customerId:customerId
            };
            return httpService.httpQParamGetFun(UrlConfiguration.loadChannelPartnerDetailsUri, qParams, callbackFunction);
        },
        hasChannelPartner:function () {

            var qParams = {
                customerId:PageContext.getCustomer().cusId
            };
            var status = false;
            var deferred = $q.defer();

            var httpPromise = httpService.httpGet(UrlConfiguration.loadChannelPartnerDetailsUri, qParams);

            httpPromise.then(function (data) {
                if (!_.isUndefined(data) && !_.isEmpty(data)) {
                    status = true;
                }
                deferred.resolve(status);
            }, function (error) {
                deferred.resolve(status);
            })

            return deferred.promise;
        },
        getProductNames:function (salesChannelId, callbackFunction) {

            var qParams = {
                salesChannelId:salesChannelId
            };
            return httpService.httpQParamGetFun(UrlConfiguration.getProductNamesUri, qParams, callbackFunction);
        },

        getProductVersions:function (salesChannelId, customerId, productName, callbackFunction) {
            var qParams = {
                salesChannelId:salesChannelId,
                customerId:customerId,
                productName:productName
            };
            return httpService.httpQParamGetFun(UrlConfiguration.getProductVersionsUri, qParams, callbackFunction);
        },

        getPriceBookDetails:function (customerId, callbackFunction) {
            var qParams = {
                customerId:customerId
            };
            return httpService.httpQParamGetFun(UrlConfiguration.getPriceBookDetailsUri, qParams, callbackFunction);
        },
        getPriceBookCodes:function (salesChannelId, customerId, customerName, productName, rrpVersion, ptpVersion, callbackFunction) {
            var  priceBookDto= {
                salesChannelName:salesChannelId,
                customerId:customerId,
                customerName:customerName,
                productName:productName,
                rrpVersion:rrpVersion,
                ptpVersion:ptpVersion
            };
            return httpService.httpPostDTO(UrlConfiguration.getPriceBookCodes,undefined,priceBookDto,callbackFunction);
        },

        countPriceBookDetails:function () {
            var qParams = {
                customerId:PageContext.getCustomer().cusId
            };
            return httpService.httpGet(UrlConfiguration.getPriceBookDetailsUri, qParams);
        },
        savePriceBook:function (salesChannelId, customerId, customerName, productName, rrpVersion, ptpVersion, callbackFunction) {
            var priceBookDto = {
                salesChannelName:salesChannelId,
                customerId:customerId,
                customerName:customerName,
                productName:productName,
                rrpVersion:rrpVersion,
                ptpVersion:ptpVersion
            };
            return httpService.httpPostDTO(UrlConfiguration.savePriceBookUri, undefined, priceBookDto, callbackFunction);
        },
        getPriceBookExtension:function (priceBookId, callbackFunction) {
            var qParams = {
                peID:priceBookId
            };
            return httpService.httpQParamGetFun(UrlConfiguration.getPriceBookExtn, qParams, callbackFunction);
        },
        updatePriceBook:function (salesChannelId, customerId, customerName, productName, rrpVersion, ptpVersion, triggerMonths, committedRevenue, priceBookId, orderSubmittedFlag, productId, callbackFunction) {

            var priceBookExtnDTO = {
                pbId:priceBookId,
                triggerMonths:triggerMonths,
                monthlyCommtRevenue:committedRevenue
            };


            var priceBookDto = {
                salesChannelName:salesChannelId,
                custId:customerId,
                customerName:customerName,
                productName:productName,
                eupVer:rrpVersion,
                ptpVer:ptpVersion,
                priceBookId:priceBookId,
                productId:productId,
                orderSubmittedFlag:orderSubmittedFlag,
                priceBookExtension:priceBookExtnDTO


            };
            return httpService.httpPostDTO(UrlConfiguration.updatePriceBookUri, undefined, priceBookDto, callbackFunction);
        }

    };
}]).factory('orderService', ['httpService', '$http', 'UrlConfiguration', function (httpService, $http, UrlConfiguration) {
    console.log('Inside Order Service');
    return {
        searchOrders:function (salesChannel, customerId, callBackFunction) {
            console.log('Calling GET Order');

            var queryParams = {
                salesChannel:salesChannel,
                customerID:customerId
            };
            return httpService.httpQParamGetFun(UrlConfiguration.searchOrdersUri, queryParams, callBackFunction);
            console.log('After GET Order');


        },

        getOrderLineItems:function (orderId, callBackFunction) {

            var qParams = {
                orderID:orderId
            };

            return httpService.httpQParamGetFun(UrlConfiguration.getOrderLineItemsUri, qParams, callBackFunction);

        },

        uploadFile:function (salesChannel, bfgCusId, attachType, quoteId, file, $upload, callback) {

            var queryParams = {
                salesChannel:salesChannel,
                bfgCusId:bfgCusId,
                attachType:attachType,
                quoteId:quoteId
            };
            $upload.upload({
                               url:UrlConfiguration.uploadAttachmentUri,
                               method:'POST',
                               params:queryParams,
                               headers:{'file':'file'},
                               file:file,
                               fileFormDataName:'file',
                               progress:true
                           }).progress(function (evt) {
                                           console.log('percent: ' + parseInt(100.0 * evt.loaded / evt.total));
                                       }).then(function (evt) {
                                                   //var progress = parseInt(100.0 * evt.loaded / evt.total);
                                                   callback(evt);
                                                   console.log('Progress :' + parseInt(100.0 * evt.loaded / evt.total));
                                               });
        },
        downloadAttachment:function (salesChannel, bfgCustId, attachType, quoteId, documentId, fileName, callback) {
            var queryParams = {
                salesChannel:salesChannel,
                bfgCusId:bfgCusId,
                attachType:attachType,
                quoteId:quoteId,
                documentId:documentId,
                fileName:fileName
            };

            return httpService.httpQParamGetFun(UrlConfiguration.getAttachmentUri, queryParams, function (responseData, status) {
                callback(responseData, status);
            });
        },
        listAttachments:function (salesChannel, bfgCusId, attachType, quoteId, callback) {
            var queryParams = {
                salesChannel:salesChannel,
                bfgCusId:bfgCusId,
                attachType:attachType,
                quoteId:quoteId
            };
            return httpService.httpQParamGetFun(UrlConfiguration.listAttachmentsUri, queryParams, function (responseData, status) {
                callback(responseData, status);
            });
        },

        cancelOrder:function (userId, orderId, orderLineId) {
            console.log('Calling Cancel Order');
            var queryParams = {
                userId:userId,
                orderId:orderId,
                orderLineId:orderLineId
            };

            $http({
                      method:'POST',
                      url:UrlConfiguration.cancelOrderUri,
                      params:queryParams

                  }).success(
                    function (responseData, status) {
                        console.log('Cancelled !!' + responseData);

                    }).error(function (responseData, status) {
                                 console.log('Failed !!' + responseData);
                             });
        },

        requestIFC:function (salesUser, payload, callback) {
            console.log('Calling IFC Request');
            var queryParams = {
                salesUser:salesUser
            };
            $http({
                      method:'POST',
                      url:UrlConfiguration.requestIFCUri,
                      data:payload,
                      params:queryParams,
                      headers:{ 'Content-Type':'application/x-www-form-urlencoded; charset=UTF-8'},
                      transformRequest:function (data) {
                          return jQuery.param(data);
                      }
                  }).success(
                    function (responseData, status) {
                        console.log('IFC Successfully done !!' + responseData);
                        callback(responseData, status);
                    }).error(function (responseData, status) {
                                 console.log('IFC Failed !!' + responseData);
                                 callback(JSON.parse(responseData), status);
                             });
        },

        requestCancel:function (salesUser, payload) {
            console.log('Calling Request Cancel');
            var queryParams = {
                salesUser:salesUser
            };
            $http({
                      method:'POST',
                      url:UrlConfiguration.requestCancelUri,
                      data:payload,
                      params:queryParams,
                      headers:{ 'Content-Type':'application/x-www-form-urlencoded; charset=UTF-8'},
                      transformRequest:function (data) {
                          return jQuery.param(data);
                      }
                  }).success(
                    function (responseData, status) {
                        console.log('Request Cancel Successfully done !!' + responseData);
                        //callback(responseData,status);
                    }).error(function (responseData, status) {
                                 console.log('Request Cancel Failed !!' + responseData);
                                 //callback(responseData,status);
                             });
        }

    };
}])

        .factory('UserManagementService', ['httpService', '$http', 'UrlConfiguration', function (httpService, $http, UrlConfiguration) {
    var roles = [];
    var salesChannels = [];
    return {
        getUserInfo:function (loginId, callback) {

            var queryParams = {
                loginId:loginId
            };

            httpService.httpQParamGetFun(UrlConfiguration.getUserInfoUri, queryParams, function (responsedata, status) {
                callback(responsedata, status);
            });
        },
        getUserSubGroups:function (loginId,callback) {

            var queryParams = {
                loginId:loginId
            };

            return   httpService.httpQParamGetFun(UrlConfiguration.getUserSubGroupsUri, queryParams,callback);
        },
        getSubGroups:function (loginId,callback) {
            var queryParams = {
                loginId:loginId
            };
            return  httpService.httpQParamGetFun(UrlConfiguration.getSubGroupsUri, queryParams, callback);
        },
        addUserSubGroup:function (userSubGroup,loginId,ein,callback) {
            var queryParams = {
                userSubGroup:userSubGroup,
                loginId:loginId,
                ein:ein
            };
            return httpService.httpQParamGetFun(UrlConfiguration.addUserSubGroupsUri, queryParams,callback);
        },
        addSubGroup:function (subGroup,callback) {
            var queryParams = {
                subGroup:subGroup
            };
            return httpService.httpQParamGetFun(UrlConfiguration.addSubGroupUri, queryParams,callback);
        },
        deleteUserSubGroup:function (userSubGroup,loginId,ein,callback) {
            var queryParams = {
                userSubGroup:userSubGroup,
                loginId:loginId,
                ein:ein
            };
            return httpService.httpQParamGetFun(UrlConfiguration.deleteUserSubGroupUri, queryParams,callback);
        },
        saveUserInfo:function (userDTO, callback) {

            $http.post(UrlConfiguration.updateUserInfoUri, userDTO)
                    .success(
                    function (responseData, status) {
                        callback(responseData, status);
                    }).error(function (responseData, status) {
                                 callback(responseData, status);
                             });
        },
        getAllRoles:function (roleTypeId) {

            if (!_.isUndefined(roleTypeId)) {
                var indx = roleTypeId - 1;

                if (_.isUndefined(roles[indx])) {
                    var queryParams = {
                        roleTypeID:roleTypeId
                    };
                    httpService.httpQParamGetFun(UrlConfiguration.getAllRoleUri, queryParams, function (responsedata, status) {
                        if (status == 200) {
                            roles[indx] = responsedata;
                        }
                        return  responsedata;
                    });
                } else {
                    return roles[indx];
                }

            } else {
                return {};
            }

        },
        getAllSalesChannel:function (roleTypeId) {
            if (!_.isUndefined(roleTypeId)) {
                var indx = roleTypeId - 1;

                if (_.isUndefined(salesChannels[indx])) {
                    var queryParams = {
                        roleTypeID:roleTypeId
                    };
                    httpService.httpQParamGetFun(UrlConfiguration.getAllSalesChannelUri, queryParams, function (responsedata, status) {
                        if (status == 200) {
                            salesChannels[indx] = responsedata;
                        }
                        return  responsedata;
                    });
                } else {
                    return salesChannels[indx];
                }

            } else {
                return {};
            }
        }
    };
}])


        .factory('billingAccountService', ['httpService', 'UrlConfiguration', 'PageContext', function (httpService, UrlConfiguration, PageContext) {
    var currencyCodeCached = undefined;

    return{
        getBillingAccounts:function (customerId, contractId, callbackFunction) {
            var queryParams = {
                customerID:customerId,
                contractID:contractId
            };

            httpService.httpQParamGetFun(UrlConfiguration.getBillingAccountsUri, queryParams, function (responsedata, status) {
                callbackFunction(responsedata, status);
            });
        },
        countBillingAccounts:function () {
            var queryParams = {
                customerID:PageContext.getCustomer().cusId,
                contractID:PageContext.getContract().id
            };

            return httpService.httpGet(UrlConfiguration.getBillingAccountsUri, queryParams)
        },
        createBillingAccount:function (userId, contractId, cusId, formData, callbackFunction) {
            var queryParams = {
                userId:userId,
                contractID:contractId,
                customerId:cusId
            };
            httpService.httpPostQParamFun(UrlConfiguration.createBillingAccountUri, queryParams, formData, function (responsedata, status) {
                callbackFunction(responsedata, status);
            });
        },
        updateBillingAccount:function (userId, cusId, formData, callbackFunction) {
            var queryParams = {
                userId:userId,
                customerId:cusId
            };
            httpService.httpPostQParamFun(UrlConfiguration.updateBillingAccountUri, queryParams, formData, function (responsedata, status) {
                callbackFunction(responsedata, status);
            });
        },
        getCurrencyCodes:function () {

            if (_.isUndefined(currencyCodeCached)) {
                httpService.httpGet(UrlConfiguration.getCurrencyCodes).then(function (responsedata) {
                    currencyCodeCached = responsedata;
                });
            }
            return  currencyCodeCached;
        },
        associateLeBillingAccount:function (customerId, leId, oldLeId, leAssociationType, formData, callbackFunction) {
            var queryParams = {
                customerId:customerId,
                leId:leId,
                oldLeId:oldLeId,
                leAssociationType:leAssociationType
            };
            httpService.httpPostQParamFun(UrlConfiguration.mapLeBillingAccountUri, queryParams, formData, function (responsedata, status) {
                callbackFunction(responsedata, status);
            });
        },
        getClarityProjectCodeDetails:function (clarityProjectCode, clarityProjectName, sacId, callbackFunction) {
            var queryParams = {
                clarityProjectCode:clarityProjectCode,
                clarityProjectName:clarityProjectName,
                sacId:sacId
            };

            httpService.httpQParamGetFun(UrlConfiguration.clarityProjectCodeUri, queryParams, function (responsedata, status) {
                callbackFunction(responsedata, status);
            });
        }
    };
}])
        .factory('AuditTrailService', ['httpService', 'UrlConfiguration', function (httpService, UrlConfiguration) {

    return{
        getAuditQuoteSummary:function (customerId, callbackFunc) {
            var queryParam = {
                customerID:customerId
            };

            httpService.httpQParamGetFun(UrlConfiguration.auditQuoteSummaryUri, queryParam, callbackFunc);
        },
        getAuditOrderSummary:function (customerId, callbackFunc) {
            var queryParam = {
                customerID:customerId
            };

            httpService.httpQParamGetFun(UrlConfiguration.auditOrderSummaryUri, queryParam, callbackFunc);
        },
        getAuditQuoteDetail:function (customerId, quoteId, callbackFunc) {
            var queryParam = {
                customerID:customerId,
                quoteID:quoteId
            };

            httpService.httpQParamGetFun(UrlConfiguration.auditQuoteDetailUri, queryParam, callbackFunc);
        },
        getAuditOrderDetail:function (customerId, orderId, callbackFunc) {
            var queryParam = {
                customerID:customerId,
                orderID:orderId
            };

            httpService.httpQParamGetFun(UrlConfiguration.auditOrderDetailUri, queryParam, callbackFunc);
        },
        getSQEReportsURL:function (salesChannel, callbackFunction) { /*get GUID for Existing Quote.*/

            var qrefGenGuidDTO = {
                salesChannel:salesChannel
            }
            httpService.httpPostDTO(UrlConfiguration.generateQrefGuid, undefined, qrefGenGuidDTO, callbackFunction);
        }
    };
}]).factory('activityService', ['httpService', 'UrlConfiguration', 'UserContext', function (httpService, UrlConfiguration, UserContext) {
    return {
        reassignActivity:function (activityId, assignorMailID, assignedToFullName, assignedToFirstName, assignedToMailID, comments, assignorFullName, assignorFirstName, callback) {
            var taskDto = {
                activityID:activityId,
                assignorFullName:assignorFullName,
                assignorFirstName:assignorFirstName,
                assignorMailID:assignorMailID,
                assignedToFullName:assignedToFullName,
                assignedToFirstName:assignedToFirstName,
                assignedToMailID:assignedToMailID,
                commentsforReAssignment:comments

            }
            httpService.httpPostDTO(UrlConfiguration.reassignActivityUri, undefined, taskDto, callback);
        },
        acceptDelegation:function (activityId, assignorMailID, assignorFullName, assignorFirstName, assigneeMailID, assigneeFullName, assigneeFirstName, comments, callback) {
            /*       var firstName;
             var salesUserName = UserContext.getSalesUser().name;
             if(!_.isUndefined(salesUserName)){
             firstName = assignorFullName.subString(" ")[0];
             }*/

            var taskDto = {
                activityID:activityId,
                assigneeFirstName:assigneeFirstName,
                assigneeFullName:assigneeFullName,
                assigneeMailID:assigneeMailID,
                assignorFullName:assignorFullName,
                assignorFirstName:assignorFirstName,
                assignorMailID:assignorMailID,
                assigneeComments:comments

            }
            httpService.httpPostDTO(UrlConfiguration.acceptDelegateUri, undefined, taskDto, callback);
        },
        rejectDelegation:function (activityId, assignorMailID, assignorFullName, assigneeMailID, assigneeFullName, comments, callback) {

            var assigneeFirstName;
            var assignorFirstName;
            if (!_.isUndefined(assigneeFullName)) {
                var n = assigneeFullName.indexOf(" ");
                assigneeFirstName = assigneeFullName.substring(0, n);
            }

            if (!_.isUndefined(assignorFullName)) {
                var n = assignorFullName.indexOf(" ");
                assignorFirstName = assignorFullName.substring(0, n);
            }

            var taskDto = {
                activityID:activityId,
                assigneeName:assigneeFullName,
                assigneeFirstName:assigneeFirstName,
                assigneeMailID:assigneeMailID,
                assignorName:assignorFullName,
                assignorFirstName:assignorFirstName,
                assignorMailID:assignorMailID,
                assignorsCommentsforRejection:comments

            }
            httpService.httpPostDTO(UrlConfiguration.rejectDelegateUri, undefined, taskDto, callback);
        },
        withdrawApproval:function (activityId, bidManagerName, bidManagerFirstName, bidManagerMailID, creatorFullName, creatorFirstName, creatorMailID, comments, callback) {

            var taskDto = {
                activityID:activityId,
                bidManagerName:bidManagerName,
                bidManagerFirstName:bidManagerFirstName,
                bidManagerMailID:bidManagerMailID,
                salesUserName:creatorFullName,
                salesUserFirstName:creatorFirstName,
                salesUserMailID:creatorMailID,
                bidManagerCommentsforWithdrawal:comments

            }
            httpService.httpPostDTO(UrlConfiguration.withdrawApprovalUri, undefined, taskDto, callback);
        }
    };
}])
        .factory('TreeNodeAdapter', ['NodeStatusService', '$rootScope', function (NodeStatusService, $rootScope) {

    function attachBehaviour(node, tab) {
        if (!_.isUndefined(node)) {
            node.expanded = false;
            node.visible = function () {
                return true;
            };

            node.tab = function () {
                return tab;
            };

            _.each(node.children, function (child) {
                attachBehaviour(child, tab);
            });
        }
    }

    return {
        adapt:function (node, tab) {
            attachBehaviour(node, tab);
        }
    };
}])
        .factory('NodeStatusService', ['customerContactService', 'legalEntityService', 'billingAccountService', 'vpnService', 'channelHierarchyService', 'branchSiteService', 'quoteService', 'customerService', function (customerContactService, legalEntityService, billingAccountService, vpnService, channelHierarchyService, branchSiteService, quoteService, customerService) {
    return {
        countRecords:function (node) {
            switch (node.id) {
                case 'customerSite':
                    return customerService.hasValidCentralSite();
                    break;
                case 'createChannelPartner':
                    return channelHierarchyService.hasChannelPartner();
                    break;
                case 'customerContacts':
                    return customerContactService.isSiteContactValid();
                    break;
                case 'legalEntity':
                    return legalEntityService.countLegalEntity();
                    break;
                case 'billingAccount':
                    return billingAccountService.countBillingAccounts();
                    break;
                case 'advanceBillingAccount':
                    return legalEntityService.countLegalEntity();
                    break;

                /*case 'vpnDetails':
                 return vpnService.countVPNDetails();
                 break;*/
                case 'priceBookDetails':
                    return channelHierarchyService.countPriceBookDetails();
                    break;
                case 'branchSites':
                    return branchSiteService.countBranchSite();
                    break;
                case 'searchQuotes':
                    return quoteService.countQuotes();
                    break;

            }
        }
    }
}])
        .factory('TabService', ['httpService', 'TabDataAdapter', 'UrlConfiguration', 'SessionContext', function (httpService, TabDataAdapter, UrlConfiguration, SessionContext) {

    var cqmTabPromise;
    var dslTabPromise;

    function loadFromServer(appName) {
        if (_.isUndefined(appName)) {
            return httpService.httpQParamGet(UrlConfiguration.getTabsUri);
        } else {
            var qParam = {
                appID:appName
            }
            return httpService.httpQParamGet(UrlConfiguration.getTabsUri, qParam);
        }

    }

    function load() {
        var promise;
        switch (SessionContext.getState()) {
            case 'CustomerConfiguration':
                if (_.isUndefined(cqmTabPromise)) {
                    cqmTabPromise = loadFromServer();
                    cqmTabPromise.then(function (data) {
                        TabDataAdapter.adapt(data["tabs"]);
                    });
                }
                promise = cqmTabPromise;
                break;
            case 'DslChecker':

                if (_.isUndefined(dslTabPromise)) {
                    dslTabPromise = loadFromServer('DslChecker');
                    dslTabPromise.then(function (data) {
                        TabDataAdapter.adapt(data["tabs"]);
                    });
                }
                promise = dslTabPromise;
                break;

        }

        return promise;

        /*        if (_.isUndefined(SessionContext.getState())) {

         if (_.isUndefined(cqmTabPromise)) {
         cqmTabPromise = loadFromServer(appName);
         cqmTabPromise.then(function (data) {
         TabDataAdapter.adapt(data["tabs"]);
         });
         }
         return cqmTabPromise;
         } else if (('DSL_CHK' == appName)) {
         if (_.isUndefined(cqmTabPromise)) {
         dslTabPromise = loadFromServer(appName);
         dslTabPromise.then(function (data) {
         TabDataAdapter.adapt(data["tabs"]);
         });
         }
         return dslTabPromise;
         }*/
    }

    return {
        getTabs:function () {
            return load();
        },
        findTab:function (tabs, entity) {
            return _.find(tabs, function (tab) {
                return tab.id == entity;
            });
        }
    };
}])
        .factory('TabDataAdapter', ['TreeNodeAdapter', function (TreeNodeAdapter) {
    function attachBehaviour(tabs) {
        _.each(tabs, function (tab) {
            tab.selected = false;
            TreeNodeAdapter.adapt(tab.treeNode, tab);
        });
    }

    return {
        adapt:function (tabs) {
            attachBehaviour(tabs);
        }};
}]).factory('PageContext', ['SessionContext', '$rootScope', function (SessionContext, $rootScope) {
    var context = {
    };

    return {
        setContext:function (salesChannel, customer, contract, selectRole, mncRealSalesChannelName) {
            context.salesChannel = salesChannel;
            context.customer = customer;
            context.contract = contract;
            context.isMnc = (_.isUndefined(mncRealSalesChannelName) || _.isEmpty(mncRealSalesChannelName)) ? false : true;
            context.mncChannel = undefined;
            if (context.isMnc) {
                context.mncChannel = $.extend(true, {}, context.salesChannel);
                context.mncChannel.name = mncRealSalesChannelName;
                context.mncChannel.userId = mncRealSalesChannelName;
            }

            if (!_.isUndefined(selectRole)) {
                context.selectedRole = selectRole;
            } else {
                var user = SessionContext.getUser()
                if (!_.isUndefined(user) && !_.isUndefined(user.roles)) {
                    context.selectedRole = user.roles[0];
                }
            }
        },
        getSalesChannel:function () {
            if (context.isMnc) {
                return _.isUndefined(context.mncChannel) ? '' : context.mncChannel;
            } else {
                return _.isUndefined(context.salesChannel) ? '' : context.salesChannel;
            }
        },getSalesChannelName:function () {
            if (context.isMnc) {
                return _.isUndefined(context.mncChannel) ? '' : context.mncChannel.name;
            } else {
                return _.isUndefined(context.salesChannel) ? '' : context.salesChannel.name;
            }
        },
        getActualSalesChannel:function () {
            return _.isUndefined(context.salesChannel) ? '' : context.salesChannel;
        },
        getCustomer:function () {
            return _.isUndefined(context.customer) ? '' : context.customer;
        },
        updateCustStatus:function (status) {
            if (!_.isUndefined(context.customer)) {
                context.customer.status = status;
                $rootScope.$broadcast('LoadCustomerEvent');
            }

        },
        getContract:function () {
            return _.isUndefined(context.contract) ? '' : context.contract;
        },
        getSelectedRole:function () {
            return _.isUndefined(context.selectedRole) ? '' : context.selectedRole;
        },
        getCentralSiteId:function () {
            return context.centralSiteId;
        },
        setCentralSiteId:function (siteId) {
            context.centralSiteId = siteId;
        },
        getIsMNC:function () {
            return _.isUndefined(context.isMnc) ? false : context.isMnc;
        },
        clear:function () {
            context = {
            };
        },
        exist:function () {
            return !_.isUndefined(context.salesChannel) && !_.isUndefined(context.customer) && !_.isUndefined(context.contract) && !_.isUndefined(context.selectedRole);
        }
    };
}]).factory('UserContext', ['$http', function ($http) {
    var userContext = {
    };

    function assertContextInitialized() {
        if (_.isUndefined(userContext.salesUser)) {
            throw new Error('UserContext not initialized. Use UserContext.initialize to initialize UserContext');
        }
    }

    return {
        initialize:function (salesUser) {
            userContext.salesUser = salesUser;
        },
        getUser:function () {
            assertContextInitialized();
            return userContext.salesUser;
        },
        getRole:function () {
            if (_.isUndefined(userContext.role)) {
                if (this.exist()) {
                    var role = this.getUser().roles[0];
                    userContext.role = role;
                }
            }
            return userContext.role;

        }, setReqHeader:function (key, value) {
            if (!_.isUndefined(key) && !_.isUndefined(value)) {
                $http.defaults.headers.common[key] = value;
            } else {
                $http.defaults.headers.common.USER_ROLE = this.getRole().roleName;
                $http.defaults.headers.common.USER_EMAIL = this.getUser().emailId;
                $http.defaults.headers.common.USER_NAME = this.getUser().name;
                $http.defaults.headers.common.USER_TYPE = this.getUser().userType;
                $http.defaults.headers.common.BOAT_ID = this.getUser().boatId;
            }

        }, isDirectUser:function () {
            assertContextInitialized();

            return  userContext.salesUser.userType == 'Direct' ? true : false;
        },
        destroy:function () {
            userContext = {};
        },
        exist:function () {
            return !_.isUndefined(userContext.salesUser);
        },
        isSubGroupUser:function () {
            return userContext.salesUser.isSubGroupUser;
        },
        getUserSubGroups:function () {
            return userContext.salesUser.userSubGroups;
        }
    };
}]).factory('UrlConfiguration', [function () {
    return JSON.parse($('#urlConfig').text())
}]).factory('CreateCustomerResponseCode', ['$document', function ($document) {
    return JSON.parse($document.find("#customerCreationResponse").text());
}]).factory('SessionContext', ['$rootScope', 'httpService', 'UrlConfiguration', function ($rootScope, httpService, UrlConfiguration) {
    var context = {
        user:{name:"Guest", selectedRole:"Guest", salesChannelList:[]},
        state:STATE.CustomerSelection
    };

    return {
        put:function (key, value) {
            context[key] = value;
        },
        get:function (key) {
            if (_.isUndefined(context[key])) {
                console.log("Value not found for key: " + key);
                throw new Error("Value not found for key: " + key);
            }
            return context[key];
        },
        getUser:function () {
            if (_.isUndefined(context[SESSION.User])) {
                console.log("User has not logged in.");
                throw new Error("User has not logged in.");
            }
            return context[SESSION.User];
        },
        setUser:function (user) {
            context[SESSION.User] = user;
            $rootScope.$broadcast(EVENT.LoadedSalesUser);
        },
        getCustomer:function () {
            if (_.isUndefined(context[SESSION.Customer])) {
                console.log("No customer selected.");
                throw new Error("No customer selected.");
            }
            return context[SESSION.Customer];
        },
        setCustomer:function (customer) {
            context[SESSION.Customer] = customer;
            $rootScope.$broadcast(EVENT.CustomerSelectionChanged, context[SESSION.Customer]);
        },
        getState:function () {
            if (_.isUndefined(context[SESSION.State])) {
                console.log("No state selected.");
            }
            return context[SESSION.State];
        },
        setState:function (state) {
            context[SESSION.State] = state;
            $rootScope.$broadcast(EVENT.StateChange, context[SESSION.State]);
        },
        navigateToTab:function (tabId) {
            context.state = STATE.CustomerConfiguration;
            $rootScope.$broadcast(EVENT.StateChange, context[SESSION.State]);
            context.subState = tabId;
            $rootScope.$broadcast(EVENT.SubStateChanged, context[SESSION.SubState]);
        },
        navigateToDsl:function () {
            context.state = STATE.DslChecker;
            $rootScope.$broadcast(EVENT.StateChange, context[SESSION.State]);
            context.subState = 'allDocument';
            $rootScope.$broadcast(EVENT.LoadDslCheckerApp);
        },
        clear:function () {
            context = {
            };
            context[SESSION.Customer] = {'name':"Guest", 'selectedRole':"Guest"};
        },
        logout:function (callbackFunction) {
            httpService.httpGetFun(UrlConfiguration.logoutUri, callbackFunction);
        }
    };
}]).factory('WebMetrics', ['UrlConfiguration', 'UserContext', function (UrlConfiguration, UserContext) {

    var userActions = {};
    return {
        captureWebMetrics:function (userAction, navStartTime) {
            if (!_.isUndefined(userActions[userAction])) {
                navStartTime = navStartTime ? navStartTime : userActions[userAction];
                userActions[userAction] = undefined;
            }
            postWebMetrics(UrlConfiguration.webMetricsUri, userAction, navStartTime, null, null, 'CQM', UserContext.getUser().userType, UserContext.getUser().ein);
        },
        registerUserAction:function (userAction) {
            userActions[userAction] = userActions[userAction] ? userActions[userAction] : new Date().getTime();
        },
        UserActions:{
            CreateCustomer:'CQM - Customer Tab - Load Create Customer',

            CentralSite:'CQM - Customer Tab - Customer Site',
            CentralSiteContacts:'CQM - Customer Tab - Customer Contacts',

            LegalEntities:'CQM - Customer Tab - Manage LE',
            BillingAccounts:'CQM - Customer Tab - Billing Account',

            BranchSites:'CQM - Manage Sites Tab - View Sites',
            BranchSiteMaps:'CQM - Manage Sites Tab - Locate Site on Maps',
            BranchSiteContacts:'CQM - Manage Sites Tab - Contacts',

            CreateQuote:'CQM - Manage Quotes Tab - View Quote',
            SearchQuote:'CQM - Manage Quotes Tab - Create Quote',

            SearchOrders:'CQM - Orders Tab - View Orders',

            LoadActivity:'CQM - Manage Activity Tab - View/Update Activity',
            CreateActivity:'CQM - Manage Activity Tab - Create Activity'
        }
    };
}]).factory('Utility',function () {
                return {
                    isBlank:function (value) {
                        return _.isUndefined(value) || _.isNull(value) || (_.isString(value) && value.trim().length === 0);
                    } }

            }).factory('SACService', ['httpService', '$http', 'UrlConfiguration', 'SessionContext', function (httpService, $http, UrlConfiguration, SessionContext) {
    console.log('Inside Order Service');
    return {
        downloadSACTemplate:function (countryName, callback) {
            var queryParams = {
                countryName:countryName
            };

            return httpService.httpQParamGetFun(UrlConfiguration.getSACTemplateUri, queryParams, function (responseData, status) {
                callback(responseData, status);
            });
        },

        uploadFile:function (file, description, $upload, callback) {


            var queryParams = {
                fileDesc:description
            };


            $upload.upload({
                               url:UrlConfiguration.importSacFileUri,
                               method:'POST',
                               params:queryParams,
                               headers:{'file':'file'},
                               file:file,
                               /* fileFormDataName:'file',*/
                               progress:true
                           }).progress(function (evt) {
                                           //console.log('percent: ' + parseInt(100.0 * evt.loaded / evt.total));
                                       }).then(function (evt) {
                                                   callback(evt);
                                                   //console.log('Progress :' + parseInt(100.0 * evt.loaded / evt.total));
                                               });
        },
        getUserUploadedFileList:function (callback2) {
            //var userId= SessionContext.getUser().ein;
            var queryParams = {dateTime:new Date().getTime()};
            return httpService.httpQParamGetFun(UrlConfiguration.userUploadedFileListUri, queryParams, function (responseData2, status) {
                callback2(responseData2, status);
            });

        },
        getAllUserUploadedFileList:function (callback1) {
            var queryParams = {dateTime:new Date().getTime()};
            return httpService.httpQParamGetFun(UrlConfiguration.allUploadedFileListUri, queryParams, function (responseData1, status) {
                callback1(responseData1, status);
            });

        },
        deleteFile:function (inputDto, callback3) {
            var queryParams = {};

            return httpService.httpPostDTO(UrlConfiguration.deleteBulkUploadUri, queryParams, inputDto, function (responseData3, status) {
                callback3(responseData3, status);
            });

        }

    }
}]).config(['$httpProvider', function ($httpProvider) {
    $httpProvider.defaults.withCredentials = true;

}]);



