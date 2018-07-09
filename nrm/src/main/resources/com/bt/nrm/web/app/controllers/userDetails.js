angular.module('app')
        .controller('UserDetailsController', ['$scope' , '$rootScope', 'nrmUserService', '$stateParams',  '$modal', '$log', 'NRMUserRolesService', function ($scope, $rootScope, nrmUserService, $stateParams,  $modal, $log, NRMUserRolesService) {

    $scope.nrmUserRolesService = NRMUserRolesService;
    $scope.models = {"group" : {}, "product": {}};
    $scope.modalData = {message: '', group: ''};
    $scope.selectedUser = {};
    $scope.existingProductsForUser = [];
    $scope.doUserGroupsExist = false;

    nrmUserService.getUserManagementData($stateParams.selectedUserId, function (data, status) {
        //This method loads all data related to selected user on page load
            if (status == '200' && !_.isUndefined(data)) {
                $scope.selectedUser = data.nrmUser;
                $scope.allGroups = data.allEvaluatorGroups;
                $scope.allProductCategory = data.allProductCategory;
                $scope.unassignedProducts = data.unassignedProducts;
                $scope.allRoles = data.allRoles;
                for(i=0;i<data.nrmUser.products.length;i++){
                    $scope.existingProductsForUser.push(data.nrmUser.products[i].product);
                }
                $scope.userRoleModel = {
                    "superUser": {"roleName": $scope.nrmUserRolesService.superUser, "granted": (_.find($scope.selectedUser.roles, function(role){ return role.roleName == $scope.nrmUserRolesService.superUser;}) ? true : false)},
                    "controller": {"roleName": $scope.nrmUserRolesService.controller, "granted": (_.find($scope.selectedUser.roles, function(role){ return role.roleName == $scope.nrmUserRolesService.controller;}) ? true : false)},
                    "evaluator": {"roleName": $scope.nrmUserRolesService.evaluator, "granted": (_.find($scope.selectedUser.roles, function(role){ return role.roleName == $scope.nrmUserRolesService.evaluator;}) ? true : false)},
                    "dataBuild": {"roleName": $scope.nrmUserRolesService.dataBuild, "granted": (_.find($scope.selectedUser.roles, function(role){ return role.roleName == $scope.nrmUserRolesService.dataBuild;}) ? true : false)}
                };
                if(_.isUndefined($scope.selectedUser.groups) || $scope.selectedUser.groups.length === 0){
                    $scope.doUserGroupsExist = false;
                } else{
                    $scope.doUserGroupsExist = true;
                }

            } else {//In case of errors, go back to search user page
                $scope.openGenericPageLoadErrorModal("Error occurred while loading selected user data. Please select user again.", 'app.searchUser');
            }
    });

    $scope.addGroupToUser = function(){
        if(!_.isUndefined($scope.models.group.selected) && !_.isUndefined($scope.models.product.selected)){
            // Check Product and Group already exists
            var userGroupExists = _.find($scope.selectedUser.groups, function (userGroup) {
                return ((userGroup.group.evaluatorGroupId === $scope.models.group.selected.evaluatorGroupId) && (userGroup.product.productCategoryCode === $scope.models.product.selected.productCategoryCode));
            });
            if(_.isUndefined(userGroupExists)){
                var UserGroupDTO = new Object();
                UserGroupDTO.userId = $scope.selectedUser.EIN;
                UserGroupDTO.group = $scope.models.group.selected;
                UserGroupDTO.product = $scope.models.product.selected;
                UserGroupDTO.createdUser = $scope.nrmUser.EIN;
                nrmUserService.addGroupToUser(UserGroupDTO,function (data, status) {
                    if (status == '200') {
                        $scope.openGenericSuccessModal("Group saved successfully!");
                        $scope.tempAddedGroupData = {};
                        $scope.tempAddedGroupData['userId'] = $scope.selectedUser.EIN;
                        $scope.tempAddedGroupData['group'] = $scope.models.group.selected;
                        $scope.tempAddedGroupData['product'] = $scope.models.product.selected;
                        $scope.selectedUser.groups.push($scope.tempAddedGroupData);
                        $scope.models.group.selected = '';
                        $scope.models.product.selected = '';
                        $scope.doUserGroupsExist = true;
                    }else if (status == '404'){
                        $scope.openGenericErrorModal("Save/Update was unsuccessful. Please try again.");
                    }else {
                        $scope.openGenericErrorModal("Bad Request! Please try again.");
                    }
                });
            } else {
                $scope.openGenericErrorModal("User Group already exists. Please select new Group.");
            }

        }
    };

    $scope.deleteGroupFromUser = function(userGroup){
        $scope.openGenericWarningModal("Are you sure you want to delete " + userGroup.group.name + " associated to "+ userGroup.product.productCategoryName +" for this user?", function (){
            var UserGroupDTO = userGroup;
            UserGroupDTO.createdUser = $scope.nrmUser.EIN;
            nrmUserService.deleteGroupFromUser(UserGroupDTO,function (data, status) {
                if (status == '200') {
                    $scope.openGenericSuccessModal("Group deleted successfully!");
                    var index  = _.indexOf(_.pluck($scope.selectedUser.groups, 'group'), userGroup.group) ;
                    $scope.selectedUser.groups.splice(index, 1);
                    if($scope.selectedUser.groups.length === 0){
                        $scope.doUserGroupsExist = false;
                    }
                }else if (status == '404'){
                    $scope.openGenericErrorModal("Group deletion was unsuccessful. Please try again.");
                }else {
                    $scope.openGenericErrorModal("Bad Request! Please try again.");
                }
            });
        });

    };

    $scope.sortableOptions = {
        containment: '#productListContainer',
        containerPositioning:'absolute',
        accept: function (sourceItemHandleScope, destSortableScope) {
            return true;
        },
        itemMoved: function (event) {
            //event.source.itemScope.modelValue.status = event.dest.sortableScope.$parent.column.name;
        }
    };

    $scope.addProductsToUser = function(){

        var userProductDTOList = [];

        for(i=0;i<$scope.existingProductsForUser.length ;i++){
            var userProductDTO = new Object();
            userProductDTO.userId = $scope.selectedUser.EIN;
            userProductDTO.createdUser = $scope.nrmUser.EIN;
            userProductDTO.product = $scope.existingProductsForUser[i];
            userProductDTOList.push(userProductDTO) ;
        }


        nrmUserService.addProductsToUser(userProductDTOList, $scope.selectedUser.EIN,function (data, status) {
            if (status == '200') {
                $scope.openGenericSuccessModal("Products saved successfully!");
            }else if (status == '404'){
                $scope.openGenericErrorModal("Save/Update was unsuccessful. Please try again.");
                return false;
            }else{
                $scope.openGenericErrorModal("Bad Request! Please try again.");
                return false;
            }

    })};

    $scope.userRoleSelectionChanged = function(selectedRoleName){
        var roleModel = _.find($scope.userRoleModel, function(role){ return role.roleName == selectedRoleName;});
        var userRoleConfigDTO = new Object();

        if(!roleModel.granted){ //If the model is false, delete the role.
            var role = _.find($scope.selectedUser.roles, function(role){ return role.roleName == selectedRoleName;});
            userRoleConfigDTO.userId = $scope.selectedUser.EIN;
            userRoleConfigDTO.roleId = role.roleId;
            nrmUserService.deleteRoleFromUser(userRoleConfigDTO,function (data, status) {
                if (status == '200') {
                    $scope.openGenericSuccessModal("User Role Removed successfully!");
                }else if (status == '404'){
                    $scope.openGenericErrorModal("User Role Update was unsuccessful. Please try again.");
                    roleModel.granted = false;
                }else {
                    $scope.openGenericErrorModal("Bad Request! Please try again.");
                    roleModel.granted = false;
                }

            })
        }else{ //The model is true. Add role to the user
            role = _.find($scope.allRoles, function(role){ return role.roleName == selectedRoleName;});
            userRoleConfigDTO.userId = $scope.selectedUser.EIN;
            userRoleConfigDTO.roleId = role.roleId;
            userRoleConfigDTO.createdUser = $scope.nrmUser.EIN;
            nrmUserService.addRoleToUser(userRoleConfigDTO,function (data, status) {
                if (status == '200') {
                    $scope.openGenericSuccessModal("User Role added successfully!");
                }else if (status == '404'){
                    $scope.openGenericErrorModal("Save was unsuccessful. Please try again.");
                    roleModel.granted = true;
                }else {
                    $scope.openGenericErrorModal("Bad Request! Please try again.");
                    roleModel.granted = true;
                }
            })
        }
    }

}]);

