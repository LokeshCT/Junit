angular.module('app')
    .config([
        '$ocLazyLoadProvider', function($ocLazyLoadProvider) {
            $ocLazyLoadProvider.config({
                debug: true,
                events: true,
                modules: [
                    {
                        name: 'ui.select',
                        files: [
                            '/user-management/static/lib/modules/angular-ui-select/select.css',
                            '/user-management/static/lib/modules/angular-ui-select/select.js'
                        ]
                    }
                ]
            });
        }
    ]);