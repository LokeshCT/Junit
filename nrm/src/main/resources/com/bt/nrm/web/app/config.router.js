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
                    .otherwise('/app/templates');
                $stateProvider
                    .state('app', {
                        abstract: true,
                        url: '/app',
                        templateUrl: '/nrm/static/views/layout.html'
                    })
                    .state('app.templates', {
                        url: '/templates',
                        templateUrl: '/nrm/static/views/templates.html',
                        ncyBreadcrumb: {
                            label: 'Templates',
                            description: ''
                        },
                       resolve: {
                           deps: [
                               '$ocLazyLoad',
                               function($ocLazyLoad) {
                                   return $ocLazyLoad.load(['720kb.tooltips','agGrid']).then(function() {
                                       return $ocLazyLoad.load(
                                               {
                                                   serie: true,
                                                   files: [
                                                       '/nrm/static/app/controllers/templates.js'
                                                   ]
                                               });
                                   });
                               }
                           ]
                       }
                    })
                    .state('app.templateDetails', {
                       url: '/templateDetails',
                       templateUrl: '/nrm/static/views/templateDetails.html',
                       params: {templateCode: null,templateVersion: null},
                       ncyBreadcrumb: {
                           label: 'Template Details',
                           description: BREADCRUM_DESCR.templateDesc
                       },
                       resolve: {
                           deps: [
                               '$ocLazyLoad',
                               function($ocLazyLoad) {
                                   return $ocLazyLoad.load({
                                           serie: true,
                                           files: [
                                               '/nrm/static/app/controllers/template.js'
                                           ]
                                       });
                               }
                           ]
                       }
                    })
                    .state('app.quoteOptions', {
                        url: '/quoteOptions',
                        templateUrl: '/nrm/static/views/quoteOptions.html',
                        params: null,
                        ncyBreadcrumb: {
                           label: 'Quotes/Quote Options',
                           description:  null
                        },
                        resolve: {
                           deps: [
                               '$ocLazyLoad',
                               function($ocLazyLoad) {
                                   return $ocLazyLoad.load(['agGrid']).then(function() {
                                       return $ocLazyLoad.load({
                                            serie: true,
                                            files: [
                                                '/nrm/static/app/controllers/quoteOptions.js'
                                            ]
                                       });
                                   });
                               }
                           ]
                        }
                    })
                    .state('app.requests', {
                        url: '/requests',
                        templateUrl: '/nrm/static/views/requests.html',
                        params : { requestState: null},
                        ncyBreadcrumb: {
                           label: 'Manage Requests',
                           description: 'Following list shows all requests associated with your product group'
                        },
                        resolve: {
                           deps: [
                               '$ocLazyLoad',
                               function($ocLazyLoad) {
                                   return $ocLazyLoad.load(['ui.select','agGrid']).then(function() {
                                       return $ocLazyLoad.load(
                                       {
                                           serie: true,
                                           files: [
                                               '/nrm/static/app/controllers/requests.js'
                                           ]
                                       });
                                   });
                               }
                           ]
                        }
                    })
                    .state('app.requestDetails', {
                       url: '/requestDetails',
                       templateUrl: '/nrm/static/views/requestDetails.html',
                       params : { request: null, requestId: null},
                       ncyBreadcrumb: {
                           label: 'Request Details',
                           description: BREADCRUM_DESCR.requestDetails
                       },
                       resolve: {
                           deps: [
                               '$ocLazyLoad',
                               function($ocLazyLoad) {
                                   return $ocLazyLoad.load(
                                   {
                                       serie: true,
                                       files: [
                                           '/nrm/static/app/controllers/requestDetails.js'
                                       ]
                                   });
                               }
                           ]
                       }
                    })
                    .state('app.actions', {
                       url: '/actions',
                       templateUrl: '/nrm/static/views/actions.html',
                       params: null,
                       ncyBreadcrumb: {
                           label: 'Actions',
                           description:  null
                       },
                       resolve: {
                           deps: [
                               '$ocLazyLoad',
                               function($ocLazyLoad) {
                                   return $ocLazyLoad.load({
                                       serie: true,
                                       files: [
                                           '/nrm/static/app/controllers/actions.js'
                                       ]
                                   });
                               }
                           ]
                       }
                    })
                    .state('app.actionDetails', {
                               url: '/actionDetails',
                               templateUrl: '/nrm/static/views/actionsDetails.html',
                               params: {requestEvaluatorId: null,requestId: null},
                               ncyBreadcrumb: {
                                   label: 'Action Details',
                                   description: null
                               },
                               resolve: {
                                   deps: [
                                       '$ocLazyLoad',
                                       function($ocLazyLoad) {
                                           return $ocLazyLoad.load(['agGrid']).then(function() {
                                               return $ocLazyLoad.load(
                                                       {
                                                           serie: true,
                                                           files: [
                                                               '/nrm/static/app/controllers/actionsDetails.js'
                                                           ]
                                                       });
                                           });
                                       }
                                   ]
                               }
                           })
                    .state('app.searchUser', {
                       url: '/searchUser',
                       templateUrl: '/nrm/static/views/userSearch.html',
                       ncyBreadcrumb: {
                           label: 'Users'
                       },
                       resolve: {
                           deps: [
                               '$ocLazyLoad',
                               function($ocLazyLoad) {
                                   return $ocLazyLoad.load({
                                       serie: true,
                                       files: [
                                           '/nrm/static/app/controllers/userSearch.js'
                                       ]
                                   });
                               }
                           ]
                       }
                    })
                    .state('app.userDetails', {
                       url: '/userDetails',
                       templateUrl: '/nrm/static/views/userDetails.html',
                       params: {selectedUserId: null},
                       ncyBreadcrumb: {
                           label: 'Contact Details',
                           description: 'User Details'
                       },
                       resolve: {
                           deps: [
                               '$ocLazyLoad',
                               function($ocLazyLoad) {
                                   return $ocLazyLoad.load(['ui.select','as.sortable']).then(function() {
                                       return $ocLazyLoad.load({
                                           serie: true,
                                           files: [
                                               '/nrm/static/app/controllers/userDetails.js'
                                           ]
                                        });
                                   });
                               }
                           ]
                       }
                    })
                    .state('app.reports', {
                       url: '/reports',
                       templateUrl: '/nrm/static/views/reports.html',
                       params: null,
                       ncyBreadcrumb: {
                           label: 'Reports',
                           description: null
                       },
                       resolve: {
                           deps: [
                               '$ocLazyLoad',
                               function($ocLazyLoad) {
                                   return $ocLazyLoad.load({
                                       serie: true,
                                       files: [
                                           '/nrm/static/lib/jquery/charts/chartjs/chart.js',
                                           '/nrm/static/app/controllers/reports.js'
                                       ]
                                   });
                                }
                           ]
                       }
                    })
                    .state('app.myProfile', {
                        url: '/profile',
                        templateUrl: '/nrm/static/views/myProfile.html',
                        ncyBreadcrumb: {
                            label: 'MyProfile'
                        },
                        resolve: {
                            deps: [
                                '$ocLazyLoad',
                                function($ocLazyLoad) {
                                    return $ocLazyLoad.load({
                                        serie: true,
                                        files: [
                                            '/nrm/static/app/controllers/myProfile.js'
                                        ]
                                    });
                                }
                            ]
                        }
                    })
                    .state('app.dataBuild', {
                       url: '/dataBuild',
                       templateUrl: '/nrm/static/views/dataBuild.html',
                       ncyBreadcrumb: {
                           label: 'Data Build Status'
                       },
                       resolve: {
                           deps: [
                               '$ocLazyLoad',
                               function($ocLazyLoad) {
                                   return $ocLazyLoad.load(['ui.select','720kb.tooltips','agGrid']).then(function() {
                                       return $ocLazyLoad.load(
                                               {
                                                   serie: true,
                                                   files: [
                                                       '/nrm/static/app/controllers/dataBuild.js'
                                                   ]
                                               });
                                   });
                               }
                           ]
                       }
                    })
                    .state('app.quoteOptionDetails', {
                       url: '/quoteOptionDetails',
                       templateUrl: '/nrm/static/views/quoteOptionDetails.html',
                       params : { quote: null},
                       ncyBreadcrumb: {
                             label: 'Quote/Quote Option Details'
                       },
                       resolve: {
                           deps: [
                               '$ocLazyLoad',
                               function($ocLazyLoad) {
                                   return $ocLazyLoad.load(['agGrid']).then(function() {
                                       return $ocLazyLoad.load({
                                            serie: true,
                                            files: [
                                                    '/nrm/static/app/controllers/quoteOptionDetails.js'
                                                   ]
                                            });
                                       })
                                   }
                               ]
                           }
                    })

                    .state('error404', {
                        url: '/error404',
                        templateUrl: '/nrm/static/views/partials/error-404.html',
                        ncyBreadcrumb: {
                            label: 'Error 404 - The page not found'
                        }
                    })
                }
        ]
    );