'use strict';
angular.module('app')
    .run(
        [
            '$rootScope', '$state', '$stateParams',
            function($rootScope, $state, $stateParams) {
                $rootScope.$state = $state;
                $rootScope.$stateParams = $stateParams;
            }
        ]
    )
    .config(
        [
            '$stateProvider', '$urlRouterProvider',
            function($stateProvider, $urlRouterProvider) {

                $urlRouterProvider
                    .otherwise('/app/roleGroupSelection');
                $stateProvider
                    .state('app', {
                        abstract: true,
                        url: '/app',
                        templateUrl: '/user-management/static/views/layout.html'
                    })
                    .state('app.roleGroupSelection', {
                        url: '/roleGroupSelection',
                        templateUrl: '/user-management/static/views/roleGroupSelection.html',
                        ncyBreadcrumb: {
                            label: 'Select Responsibility',
                            description: ''
                        },
                        resolve: {
                           deps: [
                               '$ocLazyLoad',
                               function($ocLazyLoad) {
                                   return $ocLazyLoad.load({
                                       serie: true,
                                       files: [
                                            '/user-management/static/app/controllers/roleGroupSelection.js'
                                       ]
                                   });
                               }
                           ]
                       }
                    })

                    .state('error404', {
                        url: '/error404',
                        templateUrl: '/user-management/static/views/error-404.html',
                        ncyBreadcrumb: {
                            label: 'Error 404 - The page not found'
                        }
                    })
                }
        ]
    );