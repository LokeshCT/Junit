var module = angular.module('cqm.controllers');

module.controller('ContractController',  function ($scope, $rootScope, UIService, salesChannelService, contractService) {
    console.log('Inside contractController');

    $scope.contractDTO = {};
    $scope.contractFormData = {};
    $scope.cctToolTipMsg = 'Please enter value within the range of 0-240 ';
    $scope.cltGrpDisable = false;

    $scope.durations = [
        {label:'1 Year', value:'1'},
        {label:'2 Year', value:'2'},
        {label:'3 Year', value:'3'},
        {label:'4 Year', value:'4'},
        {label:'5 Year', value:'5'},
        {label:'6 Year', value:'6'},
        {label:'7 Year', value:'7'},
        {label:'8 Year', value:'8'},
        {label:'9 Year', value:'9'},
        {label:'10 Year', value:'10'}
    ];

    $scope.clientGroups = ['BT Global Services'];
    $scope.onPageLoad = function () {
        $scope.contractDTO.duration = $scope.durations[3];
        $scope.getContract();
    };

    $scope.getContract = function () {
        UIService.block();
        contractService.getContractById().then(function (data) {
            if (!_.isUndefined(data)) {
                    $scope.contractDTO = data;

                    if ($scope.contractDTO.conQuoteOnlyFlag == 'N') {
                        $scope.cltGrpDisable = false;

                        contractService.getClientGroups().then(function (data) {
                                $scope.clientGroups = data;
                                $scope.cltGrpDisable = false;

                                if (!$rootScope.$$phase) {
                                    $rootScope.$digest();
                                }
                            });

                    } else {
                        $scope.cltGrpDisable = true
                    }


                    $scope.fillFormData(data);
                    if (!$rootScope.$$phase) {
                        $rootScope.$digest();
                    }

                }
            UIService.unblock();

        })


    }

    $scope.formToDto = function () {

        if (!_.isUndefined($scope.contractFormData)) {
            $scope.contractDTO.custRefNumber = $scope.contractFormData.contractFriendlyName;
            $scope.contractDTO.refNumber = $scope.contractFormData.contractRef;
            $scope.contractDTO.contractualCeaseTerm = $scope.contractFormData.contractCeaseTerm;
            $scope.contractDTO.linkedContractualCeaseTerm = $scope.contractFormData.linkedContractualCeaseTerm;
            $scope.contractDTO.startDateInString = $scope.contractFormData.contractStartDate;

            $scope.contractDTO.duration = $scope.contractFormData.contractDuration.label;
            $scope.contractDTO.cgpId = $scope.contractFormData.clientGroup;
        }
    }

    $scope.fillFormData = function (dto) {

        if (!_.isUndefined(dto)) {
            $scope.contractFormData.contractFriendlyName = dto.custRefNumber;
            $scope.contractFormData.contractRef = dto.refNumber;
            $scope.contractFormData.contractCeaseTerm = dto.contractualCeaseTerm;
            $scope.contractFormData.linkedContractualCeaseTerm = dto.linkedContractualCeaseTerm;
            $scope.contractFormData.contractStartDate = dto.startDateInString;

            if (!_.isEmpty(dto.duration)) {
                var num = dto.duration.match(/\d+/g);
                if (!_.isNull(num) || !_.isEmpty(num)) {
                    var duration = _.find($scope.durations, function (dur) {
                        if (dur.value.indexOf(num[0]) > -1) {
                            return true;
                        }
                    });

                    if (!_.isUndefined(duration)) {
                        $scope.contractFormData.contractDuration = duration;
                    }
                }
            }

            //$scope.contractFormData.contractDuration = dto.duration;
            $scope.contractFormData.clientGroup = dto.cgpId;

        }
    }

    $scope.updateContract = function () {
        $scope.formToDto();
        contractService.updateContract($scope.contractDTO)
    };

});
