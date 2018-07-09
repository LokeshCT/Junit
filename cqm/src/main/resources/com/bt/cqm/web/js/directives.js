'use strict';

/* Directives */


var directive = angular.module('cqm.directives', []);

directive.directive('cqmSelectCustomer', [ function () {
    return {
        restrict:'E',
        replace:true,
        controller:'SelectCustomerController',
        templateUrl:'/cqm/static/cqm/web/partials/customer.html',
        scope:{
            salesUser:'=',
            context:'='
        }
    };
}]);


directive.directive('cqmCreateCustomerDialog', [ function () {
    return {
        restrict:'E',
        controller:'MatchingCustomerController',
        templateUrl:'/cqm/static/cqm/web/partials/grid/createCustomer.html',
        scope:{
            customerName:'=',
            matchingCustomersList:'=',
            display:'=',
            totalServerItems:'=',
            customerExistByName:'='
        }
    };
}]);

directive.directive('associatedSiteQuoteDisplayDialog', [ function () {
    return {
        restrict:'E',
        controller:'customerBranchSiteController',
        templateUrl:'/cqm/static/cqm/web/partials/grid/branchSiteAssociatedQuote.html',
        scope:{
            display:'=',
            associatedQuotes:'=',
            disablePriceFields:'='
        }
    };
}]);


directive.directive('showMultipleCentralSite', [ function () {
    return {
        restrict:'E',
        controller:'MultipleCentralSiteController',
        templateUrl:'/cqm/static/cqm/web/partials/templates/multipleCentralSite.html',
        scope:{
            sites:'=',
            display:'='
        }
    };
}]);

directive.directive('showGpopProductSelection', [ function () {
    return {
        restrict:'E',
        controller:'GPopProductController',
        templateUrl:'/cqm/static/cqm/web/partials/templates/gPopProductSelection.html',
        scope:{
            display:'='
        }
    };
}]);

directive.directive('associateSaleschannel', [ function () {
    return {
        restrict:'E',
        controller:'AssociateSalesChannelController',
        templateUrl:'/cqm/static/cqm/web/partials/templates/associateSaleschannel.html',
        scope:{
            display:'=',
            contract:'='
        }
    };
}]);

directive.directive('cqmCustomerQuoteConfig', [ function () {
    return {
        restrict:'E',
        replace:true,
        controller:'CustomerQuoteConfigController',
        templateUrl:'/cqm/static/cqm/web/partials/customerQuoteConfiguration.html',
        scope:{
            context:"="
        }
    };
}]);

directive.directive('addLocationDialog', [ function () {
    return {
        restrict:'E',
        controller:'locationController',
        templateUrl:'/cqm/static/cqm/web/partials/grid/addLocationDialog.html',
        scope:{
            display:'=',
            subLocationName:'=',
            siteId:'=',
            room:'=',
            floor:'='
        }
    };
}]);

directive.directive('dslCheckerConfig', [ function () {
    return {
        restrict:'E',
        replace:true,
        controller:'DslCheckerController',
        templateUrl:'/cqm/static/cqm/web/partials/dslCheckerBasePage.html',
        scope:{
            context:"="
        }
    };
}]);


directive.directive('cqmPageHeader', [ function () {
    return {
        restrict:'E',
        replace:true,
        controller:'headerController',
        templateUrl:'/cqm/static/cqm/web/partials/templates/header.html',
        scope:{
            salesUser:'=salesUser',
            context:'='
        }
    };
}]);


directive.directive('cqmUserDashboard', [ function () {
    return {
        restrict:'E',
        replace:true,
        controller:'dashboardController',
        template:'<div><div  class="dashboard-backBtn"><a class="list-group-item" href="" ng-click="backToHomePage()"><i class="fa fa-home fa-fw"></i> Home</a> </div><ng-view class="view-animate"></ng-view></div>',
        scope:{
            salesUser:'=',
            context:'='
        }
    };
}]);


directive.directive('cqmLogout', [ 'UrlConfiguration', function (UrlConfiguration) {
    return {
        restrict:'E',
        replace:true,
        template:'<div class="logout"><p>You have successfully logged out! Close browser for complete signOff.</p></div>'
    };
}]);

directive.directive('custDialog', [function ($rootScope, modals) {
    return{

        link:function (scope, element, attributes) {
            scope.subview = null;

            element.on("click", function handleClickEvent(event) {
                if (element[0] !== event.target) {
                    return;
                }

                scope.$apply(modals.reject);
            });

            $rootScope.$on("modals.open", function handleModalOpenEvent(event, modalType) {
                scope.subview = modalType;
            });

            $rootScope.$on("modals.close", function handleModalCloseEvent(event) {
                scope.subview = null;
            })
        }
    }
}]);


directive.directive('cqmTree', [ function () {
    return {
        restrict:'EA',
        replace:true,
        controller:'TreeNodeController',
        templateUrl:'/cqm/static/cqm/web/partials/templates/cqm-tree.html',
        scope:{
            node:"=",
            depth:'='
        }
    };
}]);


directive.directive('cqmTreeList', ['$compile', function ($compile) {
    return {
        restrict:'EA',
        replace:true,
        terminal:true,
        controller:'TreeNodeController',
        scope:{
            node:'=',
            depth:'=',
            isNodeFirst:'='
        },
        link:function (scope, element, attrs) {
            element.append(
                    '<span class="label" ng-class="node | selectedCssClasses:depth" ng-click="select(node)" cqm-title="node.label">{{node.label}}</span>' +
                    '<ul ng-show="node.expanded">' +
                    '<li ng-class="child | validCssClasses:depth+1" ng-repeat="child in node.children" ng-init="child.expanded = true" cqm-nav-id="child.id">' +
                    '<div ng-class="child | visibleCssClasses">' +
                    '<i ng-class="child | treeNodeValidClasses" cqm-nav-id="child.id" rel="{{child.label}}"></i>' +
                    '<a ng-click="toggleExpanded(child)" ng-show="hasChildren(child)" class="expand-icon">' +
                    '<i ng-class="child | expandedCssClasses"></i>' +
                    '</a>' +
                    '<div ng-class="child | isExpandableCssClasses">' +
                    '<div cqm-tree-list node="child" depth="depth + 1"></div>' +
                    '</div>' +
                    '</div>' +
                    '</li>' +
                    '</ul>');

            var childScope = scope.$new();
            $compile(element.contents())(childScope);
        }
    };
}]);

directive.directive('cqmNavId', function () {
    return function (scope, element, attrs) {
        var nodeId = scope.$eval(attrs.rsqeNavId);
        element.attr('nav-id', nodeId);
    }
});

directive.directive('cqmTitle', function () {
    return function (scope, element, attrs) {
        var titleValue = scope.$eval(attrs.rsqeTitle);
        element.attr('title', titleValue);
    }
});

directive.directive('cqmDatepicker', function () {
    return {
        restrict:'A',
        require:'ngModel',
        link:function (scope, element, attrs, ngModelCtrl) {
            $(element).datepicker({
                                      dateFormat:'yy-mm-dd',

                                      onSelect:function (date) {
                                          var ngModelName = this.attributes['ng-model'].value;

                                          // if value for the specified ngModel is a property of
                                          // another object on the scope
                                          if (ngModelName.indexOf(".") != -1) {
                                              var objAttributes = ngModelName.split(".");
                                              var lastAttribute = objAttributes.pop();
                                              var partialObjString = objAttributes.join(".");
                                              var partialObj = eval("scope." + partialObjString);

                                              partialObj[lastAttribute] = date;
                                          }
                                          // if value for the specified ngModel is directly on the scope
                                          else {
                                              scope[ngModelName] = date;
                                          }
                                          scope.$apply();
                                      }

                                  });
        }
    };
});

/*

directive.directive('cqmMandatory', function () {
    console.log('Inside cqm Mandatory directive');
    return {
        restrict:'A',
        require:'ngModel',
        template:'<input type="text" style="border:RED,height: 30px,border-radius :4px"/>',
        link:function (scope, elem, attrs, ngModel) {
            elem.css({
                         border:'1px solid red'
                     });

            scope.$watch(attrs.ngModel, function () {

                if (ngModel.$modelValue == "" || ngModel.$modelValue == undefined) {
                    elem.css({
                                 border:'1px solid red',
                                 backgroundColor:'#ffff99'*/
/*,
                             height:'30px',
                             border-radius:'4px'*//*
});

                } else {
                    elem.css({
                                 border:'1px solid red',
                                 backgroundColor:'white'});
                }
            }, function (newVal, oldVal) {
                console.log('Model value ' + newVal);
            });
        }
    }
});
*/


directive.directive('addrConditionalMandatoryField', function () {
    return {
        restrict:'A',
        require:'ngModel',
        link:function (scope, elem, attrs, ngModel) {
            scope.$watch(attrs.ngModel, function () {
                var index = scope.emptyMandatoryFieldsArr.indexOf(attrs.id);
                if (_.isUndefined(ngModel.$modelValue) || (_.isString(ngModel.$modelValue) && _.isEmpty(ngModel.$modelValue)) || (_.isNumber(ngModel.$modelValue) && _.isEmpty(ngModel.$modelValue.toString())) || (('country' == attrs.id) && (_.isObject(ngModel.$modelValue) && (_.isEmpty(ngModel.$modelValue.name))))) {
                    if (index < 0) {
                        scope.emptyMandatoryFieldsArr.push(attrs.id);
                    }
                } else {
                    if (index > -1) {
                        scope.emptyMandatoryFieldsArr.splice(index, 1);
                    }
                }


                if (_.isEmpty(scope.emptyMandatoryFieldsArr)) {
                    scope.disableSubmit = false;
                    scope.disableSearch = false;
                } else {

                    if (_.contains(scope.emptyMandatoryFieldsArr, 'country') || _.contains(scope.emptyMandatoryFieldsArr, 'city')) {
                        scope.disableSearch = true;
                    } else {
                        scope.disableSearch = false;
                    }


                    if (!scope.disableSearch) {
                        if ((!_.contains(scope.emptyMandatoryFieldsArr, 'poBox') ||
                             ((!_.contains(scope.emptyMandatoryFieldsArr, 'buildingName') || !_.contains(scope.emptyMandatoryFieldsArr, 'buildingNumber')) &&
                              (!_.contains(scope.emptyMandatoryFieldsArr, 'street') || !_.contains(scope.emptyMandatoryFieldsArr, 'locality')))) &&
                            (!_.contains(scope.emptyMandatoryFieldsArr, 'latitude') && !_.contains(scope.emptyMandatoryFieldsArr, 'longitude'))) {
                            scope.disableSubmit = false;
                        } else {
                            scope.disableSubmit = true;
                        }
                    } else {
                        scope.disableSubmit = true;
                    }
                }

            }, function (newVal, oldVal) {
            });

        }
    }
});


directive.directive('legalEntityField', function () {
    return {
        restrict:'A',
        require:'ngModel',
        link:function (scope, elem, attrs, ngModel) {
            scope.$watch(attrs.ngModel, function () {
                var index = scope.emptyMandatoryFieldsArr.indexOf(attrs.id);

                if (_.isUndefined(ngModel.$modelValue) || (_.isString(ngModel.$modelValue) && _.isEmpty(ngModel.$modelValue)) || (_.isNumber(ngModel.$modelValue) && _.isEmpty(ngModel.$modelValue.toString())) || (('country' == attrs.id) && (_.isObject(ngModel.$modelValue) && (_.isEmpty(ngModel.$modelValue.name))))) {
                    if (index < 0) {
                        scope.emptyMandatoryFieldsArr.push(attrs.id);
                    }
                } else {
                    if (index > -1) {
                        scope.emptyMandatoryFieldsArr.splice(index, 1);
                    }
                }


                if (_.isEmpty(scope.emptyMandatoryFieldsArr)) {
                    //scope.disableUpdate = false;
                    scope.disableSearch = false;
                    scope.disableCreate = false;
                } else {

                    if (_.contains(scope.emptyMandatoryFieldsArr, 'country') || _.contains(scope.emptyMandatoryFieldsArr, 'city')) {
                        scope.disableSearch = true;
                    } else {
                        scope.disableSearch = false;
                    }


                    if (!scope.disableSearch) {
                        if (_.contains(scope.emptyMandatoryFieldsArr, 'companyName') || _.contains(scope.emptyMandatoryFieldsArr, 'street') || _.contains(scope.emptyMandatoryFieldsArr, 'postCode')) {
                            //scope.disableUpdate = true;
                            scope.disableCreate = true;
                        } else {
                            scope.disableCreate = false;
                            /*if (_.contains(scope.emptyMandatoryFieldsArr, 'leId')) {
                             scope.disableUpdate = true;
                             }*/
                        }
                    } else {
                        //scope.disableUpdate = true;
                        scope.disableCreate = true;
                    }
                }

            });

        }
    }
});


var PHONE_REGEXP = /[a-zA-Z\~\!\`\@\#\$\%\^\&\*\_\=\{\}\[\]\:\;\"\'\<\,\>\.\?\/\|\\]+/;

directive.directive('noAlpha', function () {
    return {
        restrict:'A',
        require:'ngModel',
        link:function (scope, element, attrs, ngModel) {


            scope.$watch(attrs.ngModel, function () {

                if (!_.isUndefined(ngModel)) {

                    var value = ngModel.$modelValue;
                    if ((_.isString(value) && !_.isEmpty(value)) || (_.isNumber(value) && !_.isNull(value))) {
                        if (value.length > 0 && PHONE_REGEXP.test(value) ) {
                            ngModel.$setValidity('', false);
                        } else {
                            ngModel.$setValidity('', true);
                        }
                    }
                }
            });

        }
    }
});

var ZIPCODE_REGEXP = /^[0-9]{6}|[0-9]{5}-[0-9]{4}$/;
directive.directive('zipcode', function () {
    return {
        restrict:'A',
        require:'ngModel',
        link:function (scope, element, attrs, ctrl) {
            angular.element(element).bind('blur', function () {
                var value = this.value;
                if (value.length < 1 || ZIPCODE_REGEXP.test(value)) {
                    // Valid input
                    angular.element(this).next().next().css('display', 'none');
                    ctrl.$setValidity('', true);
                } else {
                    // Invalid input
                    angular.element(this).next().next().css('display', 'block');
                    ctrl.$setValidity('', false);

                }
            });
        }
    }
});

/*directive.directive('cqmMandatorySelect', function () {
    console.log('Inside cqm Mandatory select directive');
    return {
        restrict:'A',
        require:'ngModel',
        template:'<select style="border:RED"/>',
        link:function (scope, elem, attrs, ngModel) {
            elem.css({
                         border:'1px solid red'
                     });


            scope.$watch(attrs.ngModel, function () {

                if (ngModel.$modelValue == "" || ngModel.$modelValue == undefined) {
                    elem.css({
                                 border:'1px solid red',
                                 backgroundColor:'#ffff99'
                             });
                    //pageForm.$valid = false;
                } else if (_.isObject(ngModel.$modelValue) && !_.isUndefined(ngModel.$modelValue.name) && _.isEmpty(ngModel.$modelValue.name)) {
                    elem.css({
                                 border:'1px solid red',
                                 backgroundColor:'#ffff99'
                             });
                } else {
                    elem.css({
                                 border:'1px solid red',
                                 backgroundColor:'white'
                             });
                }
            }, function (newVal, oldVal) {
                console.log('Model value ' + newVal);
            });
        }
    }
});*/


directive.directive('cqmDialog', ['$parse', function ($parse) {
    return {
        transclude:true,
        templateUrl:'/cqm/static/cqm/web/partials/templates/dialog.html',
        link:function (scope, element, attrs) {
            var dialogOptions = {
                autoOpen:false,
                modal:true,
                width:(!_.isUndefined(attrs.width)) ? attrs.width : "auto",
                height:(!_.isUndefined(attrs.height)) ? attrs.height : "auto",
                resizable:false
            };
            var dialog = element.dialog(
                    dialogOptions
            );

            var container = element.parent();
            container.addClass('rsqe-dialog-container');

            if (!_.isUndefined(attrs.noModal)) {
                element.dialog("option", "modal", false);
            }

            if (!_.isUndefined(attrs.noOkCancelButtons)) {
                scope.hideOkButton = true;
                scope.hideCancelButton = true;
                scope.hideDetailsButton = true;
            }

            if (!_.isUndefined(attrs.noDetailsButton)) {
                scope.hideDetailsButton = true;
            }

            if (!_.isUndefined(attrs.okButNoCancelButton)) {
                scope.hideCancelButton = true;
            }

            function closeAndAction(action) {
                scope.$apply(function () {
                    dialog.dialog('close');
                    $parse(attrs.cqmDialog).assign(scope, false);
                    scope.show = false;
                    if (!_.isUndefined(action)) {
                        action();
                    }
                });
            }

            var closeCross = container.find(".ui-dialog-titlebar-close");
            closeCross.removeAttr('href');
            closeCross.unbind('click');
            closeCross.click(function () {
                closeAndAction(scope.$eval(attrs.onExit));
            });

            container.find('.action-ok').click(function () {
                closeAndAction(scope.$eval(attrs.onOk));
            });

            container.find('.action-cancel').click(function () {
                closeAndAction(scope.$eval(attrs.onCancel));
            });

            scope.$watch(attrs.title, function (title) {
                container.find('.ui-dialog-title').text(scope.$eval(attrs.title));
            });

            scope.$watch(attrs.cqmDialog, function (shown) {
                var openOrClose = (shown ? 'open' : 'close');
                dialog.dialog(openOrClose);

                if('open'==openOrClose){
                var initMethod = scope.$eval(attrs.onShow);
                if (!_.isUndefined(initMethod)) {
                    (scope.$eval(attrs.onShow))();
                }
                }
            });
        }
    }
}]);


/*directive.directive('address', function () {
 return {
 restrict:'E',
 templateUrl:'/cqm/static/cqm/web/partials/templates/address-grid.html',
 scope:{
 addressDto:'=',
 onsubmit:'&',
 disabled:'='
 },
 link:function (scope, element, attrs) {
 scope.$watch(attrs['disabled'], function (disabled) {
 if (disabled) {
 scope.isAdrSearchSuccess = false;
 }
 })
 },
 controller:'GenericAddressController'

 };
 });*/


directive.directive('cqmMessageDialog', function () {
    return {
        restrict:'EA',
        templateUrl:"/cqm/static/cqm/web/partials/templates/error-warning-dialogue.html",
        controller:'MessageDialogController'
    };
});


directive.directive('aDisabled', function () {
    return {
        compile:function (tElement, tAttrs, transclude) {
            //Disable href, based on class
            tElement.on("click", function (e) {
                if (tElement.hasClass("disabled")) {
                    e.preventDefault();
                }
            });

            //Disable ngClick
            tAttrs["ngClick"] = ("ng-click", "!(" + tAttrs["aDisabled"] + ") && (" + tAttrs["ngClick"] + ")");

            //Toggle "disabled" to class when aDisabled becomes true
            return function (scope, iElement, iAttrs) {
                scope.$watch(iAttrs["aDisabled"], function (newValue) {
                    if (newValue !== undefined) {
                        iElement.toggleClass("disabled", newValue);
                    }
                });
            };
        }
    };
});

directive.directive('capitalize', function () {
    return {
        require:'ngModel',
        link:function (scope, element, attrs, modelCtrl) {
            var capitalize = function (inputValue) {
                if (inputValue == undefined) inputValue = '';
                var capitalized = inputValue.toUpperCase();
                if (capitalized !== inputValue) {
                    modelCtrl.$setViewValue(capitalized);
                    modelCtrl.$render();
                }
                return capitalized;
            }
            modelCtrl.$parsers.push(capitalize);
            capitalize(scope[attrs.ngModel]);  // capitalize initial value
        }
    };
});




