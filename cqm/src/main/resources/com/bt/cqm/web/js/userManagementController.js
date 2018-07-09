var module = angular.module('cqm.controllers');

module.controller('userManagementController', [ '$scope', '$modal', 'UIService', 'UserManagementService', 'UserContext',
    function ($scope, $modal, UIService, UserManagementService, UserContext) {

        console.log('Inside userManagementController');

        $scope.userConfigFormData = {};
        $scope.displayuserDetails = false;
        $scope.disableAddButton = true;
        $scope.hasModified = false;
        $scope.disableSaveButton = true;
        $scope.userSubGroupList = [];
        $scope.subGroupList = [];
        $scope.selectedUserSubGroup=[];
        $scope.disableDeleteButton = true;
        $scope.availableSalesChannelListSize = 0;
        $scope.userSalesChannelListSize = 0;
        $scope.userRolesListSize = 0;
        $scope.errorMsg = '';
        $scope.hasError = false;

        $scope.userConfigFormData.loginId = '';
        $scope.cachedUserDetailDao = '';
        $scope.userRolesList = [];

        $scope.assignedRoles = [];
        $scope.availableRoles = [];
        $scope.disableRoleAddButton = true;
        $scope.disableRoleRemoveButton = true;
        $scope.disableSalesChnlAddButton = true;
        $scope.disableSalesChnlRemoveButton = true;
        $scope.isDirectTypeDisabled = false;
        $scope.isDefaultSelectEmpty = false;
        $scope.hasModifiedDataRoleType = false;


        $scope.userAddRoleSelect = function () {
            $scope.disableRoleAddButton = false;
            $scope.disableRoleRemoveButton = true;
        };


        $scope.onRemoveRoleSelect = function () {
            $scope.disableRoleRemoveButton = false;
            $scope.disableRoleAddButton = true;
        };

        $scope.userAddSalesChannelSelect = function () {
            $scope.disableSalesChnlAddButton = false;
            $scope.disableSalesChnlRemoveButton = true;
        };


        $scope.onRemoveSalesChannelSelect = function () {
            $scope.disableSalesChnlAddButton = true;
            $scope.disableSalesChnlRemoveButton = false;
        };

        $scope.onPageLoad = function () {

            //Load All Direct an Indirect Roles
            UserManagementService.getAllRoles(1);
            UserManagementService.getAllRoles(2);
            UserManagementService.getAllSalesChannel(1);
            UserManagementService.getAllSalesChannel(2);

        };


        $scope.subGroups = [];

        $scope.roleTypeSelected = function () {

            if (!_.isUndefined($scope.cachedUserDetailDao) && !_.isEmpty($scope.cachedUserDetailDao.trim())) {
                var cacheUserDetObj = JSON.parse($scope.cachedUserDetailDao);
                if ($scope.userConfigFormData.roleTypeId != cacheUserDetObj.userType.roleTypeId) {
                    $scope.hasModifiedDataRoleType = true;
                }
                else {
                    $scope.hasModifiedDataRoleType = false;
                }
            }
            if (!_.isUndefined($scope.userConfigFormData.roleTypeId)) {
                $scope.userConfigFormData.availableRoleList = UserManagementService.getAllRoles($scope.userConfigFormData.roleTypeId);

                if (!_.isUndefined($scope.userConfigFormData.availableRoleList)) {
                    $scope.availableRoleSize = $scope.userConfigFormData.availableRoleList.length;
                    if (_.isUndefined($scope.availableRoleSize)) {
                        $scope.availableRoleSize = 1;
                        $scope.userConfigFormData.availableRoleList = [$scope.userConfigFormData.availableRoleList];
                    }
                } else {
                    $scope.availableRoleSize = 0;
                }

                $scope.userConfigFormData.availableSalesChannelList = UserManagementService.getAllSalesChannel($scope.userConfigFormData.roleTypeId);

                if (!_.isUndefined($scope.userConfigFormData.availableSalesChannelList)) {
                    $scope.availableSalesChannelListSize = $scope.userConfigFormData.availableSalesChannelList.length;
                    if (_.isUndefined($scope.availableSalesChannelListSize)) {
                        $scope.availableSalesChannelListSize = 1;
                        $scope.userConfigFormData.availableSalesChannelList = [$scope.userConfigFormData.availableSalesChannelList];
                    }
                } else {
                    $scope.availableSalesChannelListSize = 0;
                }

            }

            $scope.disableRoleAddButton = true;
            $scope.disableRoleRemoveButton = true;
            //$scope.hasModified = $scope.hasModifiedData();
        }

        $scope.userConfigFormData.userTypeList = [
            {roleTypeName:"Direct", value:"Direct", roleTypeId:"1"},
            {roleTypeName:"Indirect", value:"Indirect", roleTypeId:"2"}
        ];


        $scope.loginSearch = function (withNoBlocker) {

            var loginId = $scope.userConfigFormData.loginId;
            var title = 'User Configuration';
            $scope.clearErrorMsg();
            $scope.displayuserDetails = false;
            if (!(_.isUndefined(loginId) || loginId.length < 1)) {
                UIService.block();

                UserManagementService.getUserInfo(loginId, (function (data, status) {
                    var userDetails = data;

                    if (status == 200) {

                        $scope.displayuserDetails = true;
                        $scope.populateForm(userDetails);
                        $scope.cachedUserDetailDao = JSON.stringify(userDetails);

                    } else {
                        $scope.hasError = true;
                        $scope.errorMsg = userDetails;

                        var dialogInstance = UIService.openDialogBox(title, "No Active User found for login ID " + loginId, true, false);
                        dialogInstance.result.then(function () {
                            UIService.unblock();
                        }, function () {
                            UIService.unblock();
                        });
                    }


                    UIService.unblock();


                }));

            }
        };


        $scope.moveItem = function (itemsToMove, targetList, operation) {

            console.log("ITEM's to add : " + itemsToMove);

            angular.forEach(itemsToMove, function (item) {

                var isDuplicate = false;

                angular.forEach(targetList, function (singleItem) {

                    if (operation == 'ROLE') {
                        if (_.isEqual(item.roleId, singleItem.roleId)) {
                            isDuplicate = true;
                        }
                    } else {
                        if (_.isEqual(item.name, singleItem.name)) {
                            isDuplicate = true;
                            //break;
                        }
                    }
                });

                if (!isDuplicate) {
                    targetList.push(item);
                    $scope.hasModifiedData = true;
                }

            })

            var size = targetList.length;
            if (operation == 'ROLE') {
                $scope.assignedRoleSize = size;
            } else {
                $scope.userSalesChannelListSize = size;
            }

            targetList.sort();

        };


        $scope.removeItem = function (itemsToMove, targetList, operation) {

            console.log("ITEM  to remove : " + itemsToMove);

            angular.forEach(itemsToMove, function (item) {

                var idx = targetList.indexOf(item);
                targetList.splice(idx, 1);

                if (operation == 'ROLE') {
                    if (item.isDefault == true) {
                        $scope.userConfigFormData.defaultRole = undefined;
                    }
                } else {
                    if (item.isDefault == true) {
                        $scope.userConfigFormData.defaultChannel = undefined;
                    }
                }

            })

            var size = targetList.length;
            if (operation == 'ROLE') {
                $scope.assignedRoleSize = size;
            } else {
                $scope.userSalesChannelListSize = size;
            }

            targetList.sort();
            $scope.hasModifiedData = true;
            $scope.isDefaultSelectEmpty = !$scope.hasDefaultSelected();

        };

        $scope.hasDefaultSelected = function () {

            return !(_.isUndefined($scope.userConfigFormData.defaultChannel) || _.isUndefined($scope.userConfigFormData.defaultRole));
        }

        $scope.setDefaultRole = function (defaultRole) {

            angular.forEach($scope.userConfigFormData.assignedRoleList, function (role) {
                if (role.roleId == defaultRole.roleId) {
                    role.isDefault = true;
                } else {
                    role.isDefault = false;
                }
            });

            $scope.userConfigFormData.defaultRole = defaultRole;
            $scope.hasModifiedData = true;
            $scope.isDefaultSelectEmpty = !$scope.hasDefaultSelected();
        };

        $scope.setDefaultChannel = function (defaultChannel) {

            angular.forEach($scope.userConfigFormData.userSalesChannelList, function (channel) {
                if (channel.name == defaultChannel.name) {
                    channel.isDefault = true;
                } else {
                    channel.isDefault = false;
                }
            });

            $scope.userConfigFormData.defaultChannel = defaultChannel;
            $scope.hasModifiedData = true;
            $scope.isDefaultSelectEmpty = !$scope.hasDefaultSelected();
        };

        $scope.submit = function () {

            $scope.disableAddButton = true;

            $scope.userConfigFormData.modifiedBy = UserContext.getUser().ein;

            var userDTO = new Object();

            userDTO.userId = $scope.userConfigFormData.userId;
            userDTO.userName = $scope.userConfigFormData.userName;
            userDTO.modifiedUser = UserContext.getUser().ein;
            userDTO.roles = $scope.userConfigFormData.assignedRoleList;
            userDTO.userSalesChannelList = $scope.userConfigFormData.userSalesChannelList;
            userDTO.userRoleTypeId = $scope.userConfigFormData.roleTypeId;
            userDTO.loginId = $scope.userConfigFormData.loginId;
            ;


            UIService.block();

            UserManagementService.saveUserInfo(userDTO, function (responseData, status) {

                var btns = [
                    {result:'OK', label:'OK'}
                ];

                var title = 'User Configuration';

                if (status == '200') {
                    var msg = 'Saved successfully ';

                    var dialogInstance = UIService.openDialogBox(title, msg, true, false);
                    dialogInstance.result.then(function () {
                    }, function () {
                    });
                    //Fetch the saved data from server so that the data gets cached in the client
                    $scope.loginSearch(true);
                } else {

                    var msg = "Save failed! \n" +
                              " Please retry after sometime.";

                    var dialogInstance = UIService.openDialogBox(title, msg, true, false);
                    dialogInstance.result.then(function () {
                    }, function () {
                    });
                }
                UIService.unblock();

            });

            $scope.disableSaveButton = true;
        };

        $scope.reset = function () {
            var cacheUserDetObj = JSON.parse($scope.cachedUserDetailDao);
            $scope.populateForm(cacheUserDetObj);
            $scope.disableSaveButton = true;
            $scope.disableRoleAddButton = true;
            $scope.disableRoleRemoveButton = true;
            $scope.disableSalesChnlAddButton = true;
            $scope.disableSalesChnlRemoveButton = true;
            $scope.hasModifiedData = false;
            $scope.hasModifiedDataRoleType = false;
        };


        /********************************************************* Helper methods *******************************************************/

        $('#loginIDTextFld').keyup(function (event) {
            if (event.keyCode == 13) {
                $('#searchBut').click();
            }
        });

        $scope.onLoad = function () {
            $('#loginIDTextFld').focus();
        };

        $scope.onLoadSubGroup = function () {
            UIService.block();
            var loginId = $scope.userConfigFormData.loginId;
            var title = 'User Sub Groups';
            $scope.clearErrorMsg();
            UserManagementService.getSubGroups(loginId, (function (data, status) {
                if (status == 200) {
                    $scope.subGroups = data;
                    $scope.subGroupList = [];
                    if (!_.isEmpty(data)) {
                        for (var i = 0; i < data.length; i++) {
                            var subGroup = {subGroupName:data[i]};
                            $scope.subGroupList.push(subGroup);
                        }
                    }
                    UIService.unblock();
                } else {
                    $scope.hasError = true;
                    var dialogInstance = UIService.openDialogBox(title, "No Sub Groups Configured In the System " , true, false);
                    dialogInstance.result.then(function () {
                        UIService.unblock();
                    }, function () {
                        UIService.unblock();
                    });
                }
                UIService.unblock();
            }));
          //  UIService.unblock();
        };

        $scope.clearErrorMsg = function () {
            $scope.errorMsg = '';
            $scope.hasError = false;

        };


        $scope.populateForm = function (userDetailsDao) {

            $scope.userConfigFormData.userId = userDetailsDao.userId;
            $scope.userConfigFormData.userName = userDetailsDao.userName;
            $scope.userConfigFormData.roleTypeId = userDetailsDao.userType.roleTypeId;
            $scope.userConfigFormData.roleTypeName = userDetailsDao.userType.roleTypeName;

            $scope.userConfigFormData.userSalesChannelList = [];

            $scope.userConfigFormData.assignedRoleList = [];

            // Populate User Sales Channel
            if (userDetailsDao.userSalesChannelList != null) {

                if (userDetailsDao.userSalesChannelList.length == undefined) {
                    $scope.userConfigFormData.userSalesChannelList.push(userDetailsDao.userSalesChannelList);
                    $scope.userConfigFormData.defaultChannel = userDetailsDao.userSalesChannelList;
                }
                else {
                    angular.forEach(userDetailsDao.userSalesChannelList, function (salesChannelObj) {

                        $scope.userConfigFormData.userSalesChannelList.push(salesChannelObj)

                        if (salesChannelObj.isDefault == true) {
                            $scope.userConfigFormData.defaultChannel = salesChannelObj;
                        }

                    });
                }
            }

            // Populate User Roles
            if (userDetailsDao.roles != null) {

                if (userDetailsDao.roles.length == undefined) {
                    $scope.userConfigFormData.assignedRoleList.push(userDetailsDao.roles);
                    $scope.userConfigFormData.defaultRole = userDetailsDao.roles;
                }
                else {
                    angular.forEach(userDetailsDao.roles, function (role) {

                        $scope.userConfigFormData.assignedRoleList.push(role)

                        if (role.isDefault == true) {
                            $scope.userConfigFormData.defaultRole = role;
                        }

                    });
                }
            }

            $scope.userConfigFormData.userSalesChannelList.sort();
            $scope.userConfigFormData.assignedRoleList.sort();

            $scope.assignedRoleSize = $scope.userConfigFormData.assignedRoleList.length;
            $scope.userSalesChannelListSize = $scope.userConfigFormData.userSalesChannelList.length;

            /* if ($scope.userConfigFormData.roleTypeId == '1') {
             $scope.isDirectTypeDisabled = false;
             }*/

            $scope.roleTypeSelected();


            $scope.disableRoleAddButton = true;
            $scope.disableRoleRemoveButton = true;
            $scope.disableSalesChnlAddButton = true;
            $scope.disableSalesChnlRemoveButton = true;

            $scope.hasModifiedData = false;
            $scope.hasModifiedDataRoleType = false;

        };
        $scope.addUserSubGroup = function () {
            var subGroupSelected = $scope.userConfigFormData.subGroup;
            var title = 'User Sub Groups';
            UIService.block();
            if (!(_.isUndefined(subGroupSelected) || subGroupSelected.length < 1)) {
                UIService.block();
                UserManagementService.addUserSubGroup(subGroupSelected,$scope.userConfigFormData.loginId,$scope.userConfigFormData.userId,(function (data, status) {
                    if (status == 200) {
                        if (!_.isEmpty(data)) {
                            var dialogInstance = UIService.openDialogBox(title, data, true, false);
                            dialogInstance.result.then(function () {
                                UIService.unblock();
                            }, function () {
                                UIService.unblock();
                            });
                        }
                        $scope.loginSearchForSubGroup();
                    }
                else {
                        var dialogInstance = UIService.openDialogBox(title, data, true, false);
                        dialogInstance.result.then(function () {
                            UIService.unblock();
                        }, function () {
                            UIService.unblock();
                        });
                    }
                    UIService.unblock();
                }));

            }
            else
            {
                UIService.unblock();
            }
        }

        $scope.deleteUserSubGroup = function () {
            var subGroupSelected = $scope.selectedUserSubGroup[0].subGroupName;
            var title = 'User Sub Groups';
            UIService.block();
            if (!(_.isUndefined(subGroupSelected) || subGroupSelected.length < 1)) {
                UIService.block();
                UserManagementService.deleteUserSubGroup(subGroupSelected,$scope.userConfigFormData.loginId,$scope.userConfigFormData.userId,(function (data, status) {
                    if (status == 200) {
                        if (!_.isEmpty(data)) {
                            var dialogInstance = UIService.openDialogBox(title, data, true, false);
                            dialogInstance.result.then(function () {
                                UIService.unblock();
                            }, function () {
                                UIService.unblock();
                            });
                        }
                        $scope.loginSearchForSubGroup();
                    }
                    else {
                        var dialogInstance = UIService.openDialogBox(title, data, true, false);
                        dialogInstance.result.then(function () {
                            UIService.unblock();
                        }, function () {
                            UIService.unblock();
                        });
                    }
                    UIService.unblock();
                }));

            }
            else
            {
                UIService.unblock();
            }
        }

        $scope.addSubGroup = function () {
            var subGroupName = $scope.createSubGroupFormData.subGroupName;
            var title = 'Sub Groups';
            UIService.block();
            if (!(_.isUndefined(subGroupName) || subGroupName.length < 1)) {
                UIService.block();
                UserManagementService.addSubGroup(subGroupName,(function (data, status) {
                    if (status == 200) {
                        if (!_.isEmpty(data)) {
                            var dialogInstance = UIService.openDialogBox(title, data, true, false);
                            dialogInstance.result.then(function () {
                                UIService.unblock();
                            }, function () {
                                UIService.unblock();
                            });
                        }
                        $scope.createSubGroupFormData.subGroupName="";
                        $scope.onLoadSubGroup();
                       // $scope.loginSearchForSubGroup();
                    }
                    else {
                        var dialogInstance = UIService.openDialogBox(title, data, true, false);
                        dialogInstance.result.then(function () {
                            UIService.unblock();
                        }, function () {
                            UIService.unblock();
                        });
                    }
                    UIService.unblock();
                }));

            }
            else
            {
                UIService.unblock();
            }
        }

        $scope.loginSearchForSubGroup = function () {

            var loginId = $scope.userConfigFormData.loginId;
            var title = 'User Sub Groups';
            $scope.clearErrorMsg();
            $scope.displayuserDetails = false;
            $scope.userConfigFormData.subGroup={};
            if (!(_.isUndefined(loginId) || loginId.length < 1)) {
                UIService.block();

                UserManagementService.getUserInfo(loginId, (function (data, status) {
                    var userDetails = data;

                    if (status == 200) {

                        $scope.displayuserDetails = true;

                        $scope.cachedUserDetailDao = JSON.stringify(userDetails);
                        $scope.userSubGroupList = [];
                        UserManagementService.getUserSubGroups(loginId, (function (data, status) {
                            if (status == 200) {
                                $scope.userSubGroupList=[];
                                if (!_.isEmpty(data)) {
                                    for (var i = 0; i < data.length; i++) {
                                        var subGroup = {subGroupName:data[i]};
                                        $scope.userSubGroupList.push(subGroup);
                                    }
                                }
                                else
                                {
                                    var dialogInstance = UIService.openDialogBox(title, "No Sub Groups found for login ID " + loginId, true, false);
                                    dialogInstance.result.then(function () {
                                        UIService.unblock();
                                    }, function () {
                                        UIService.unblock();
                                    });
                                }
                                $scope.displayuserDetails = true;
                                UIService.unblock();
                            } else {
                                var dialogInstance = UIService.openDialogBox(title,"No Sub Groups found for login ID ", true, false);
                                dialogInstance.result.then(function () {
                                    UIService.unblock();
                                }, function () {
                                    UIService.unblock();
                                });
                                UIService.unblock();
                            }
                           // UIService.unblock();
                        }));
                        $scope.populateForm(userDetails);

                    } else {
                        $scope.hasError = true;
                        $scope.errorMsg = userDetails;

                        var dialogInstance = UIService.openDialogBox(title, "No Active User found for login ID " + loginId, true, false);
                        dialogInstance.result.then(function () {
                            UIService.unblock();
                        }, function () {
                            UIService.unblock();
                        });
                    }


                    UIService.unblock();


                }));

            }


        };

        $scope.userSubGroupGrid = {data:'userSubGroupList',
            selectedItems:$scope.selectedUserSubGroup,
            multiSelect:false,
            enableColumnResize:true,
            showGroupPanel:false,
            showColumnMenu:false,
            showFilter:true,
            afterSelectionChange:function (item, event) {
                if (item.selected == false) {
                    $scope.disableDeleteButton = true;
                    console.log('got de-select event');
                    return;
                }
                else {
                    $scope.disableDeleteButton = false;

                    return;
                }
            },
            columnDefs:[
                {field:'subGroupName', displayName:'Sub Group Name', width:"*"}
            ]
        };

        $scope.subGroupGrid = {data:'subGroupList',
           /* selectedItems:$scope.selectedUserSubGroup,*/
            multiSelect:false,
            enableColumnResize:true,
            showGroupPanel:false,
            showColumnMenu:false,
            showFilter:true,
            afterSelectionChange:function (item, event) {
                if (item.selected == false) {
                   // $scope.disableDeleteButton = true;
                    console.log('got de-select event');
                    return;
                }
                else {
                   // $scope.disableDeleteButton = false;

                    return;
                }
            },
            columnDefs:[
                {field:'subGroupName', displayName:'Sub Group Name', width:"*"}
            ]
        };

    }])
