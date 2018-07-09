angular.module('app')
    .config([
        '$ocLazyLoadProvider', function($ocLazyLoadProvider) {
            $ocLazyLoadProvider.config({
                debug: true,
                events: true,
                modules: [
                    {
                        name: 'toaster',
                        files: [
                            '/nrm/static/lib/modules/angularjs-toaster/toaster.css',
                            '/nrm/static/lib/modules/angularjs-toaster/toaster.js'
                        ]
                    },
                    {
                        name: 'ui.select',
                        files: [
                            '/nrm/static/lib/modules/angular-ui-select/select.css',
                            '/nrm/static/lib/modules/angular-ui-select/select.js'
                        ]
                    },
                    {
                        name: 'ngTagsInput',
                        files: [
                            '/nrm/static/lib/modules/ng-tags-input/ng-tags-input.js'
                        ]
                    },
                    {
                        name: 'daterangepicker',
                        files: [
                            '/nrm/static/lib/modules/angular-daterangepicker/moment.js',
                            '/nrm/static/lib/modules/angular-daterangepicker/daterangepicker.js',
                            '/nrm/static/lib/modules/angular-daterangepicker/angular-daterangepicker.js'
                        ]
                    },
                    {
                        name: 'vr.directives.slider',
                        files: [
                            '/nrm/static/lib/modules/angular-slider/angular-slider.min.js'
                        ]
                    },
                    {
                        name: 'minicolors',
                        files: [
                            '/nrm/static/lib/modules/angular-minicolors/jquery.minicolors.js',
                            '/nrm/static/lib/modules/angular-minicolors/angular-minicolors.js'
                        ]
                    },
                    {
                        name: 'textAngular',
                        files: [
                            '/nrm/static/lib/modules/text-angular/textAngular-sanitize.min.js',
                            '/nrm/static/lib/modules/text-angular/textAngular-rangy.min.js',
                            '/nrm/static/lib/modules/text-angular/textAngular.min.js'
                        ]
                    },
                    {
                        name: 'ng-nestable',
                        files: [
                            '/nrm/static/lib/modules/angular-nestable/jquery.nestable.js',
                            '/nrm/static/lib/modules/angular-nestable/angular-nestable.js'
                        ]
                    },
                    {
                        name: 'angularBootstrapNavTree',
                        files: [
                            '/nrm/static/lib/modules/angular-bootstrap-nav-tree/abn_tree_directive.js'
                        ]
                    },
                    {
                        name: 'ui.calendar',
                        files: [
                            '/nrm/static/lib/jquery/jquery-ui-1.10.4.custom.js',
                            '/nrm/static/lib/modules/angular-daterangepicker/moment.js',
                            '/nrm/static/lib/jquery/fullcalendar/fullcalendar.js',
                            '/nrm/static/lib/modules/angular-ui-calendar/calendar.js'
                        ]
                    },
                    {
                        name: 'ui.grid',
                        files: [
                            '/nrm/static/lib/modules/angular-ui-grid/ui-grid.js',
                            '/nrm/static/lib/modules/angular-ui-grid/ui-grid.css'
                        ]
                    },
                    {
                        name: 'as.sortable',
                        files: [
                            '/nrm/static/lib/modules/ng-sortable/ng-sortable.js',
                            '/nrm/static/lib/modules/ng-sortable/ng-sortable.css'
                        ]
                    },
                    {
                        name: '720kb.tooltips',
                        files: [
                            '/nrm/static/lib/modules/angular-tooltips/angular-tooltips.js',
                            '/nrm/static/lib/modules/angular-tooltips/angular-tooltips.css'
                        ]
                    },
                    {
                        name: 'agGrid',
                        files: [
                            '/nrm/static/lib/modules/ag-grid/ag-grid.js',
                            '/nrm/static/lib/modules/ag-grid/ag-grid.css',
                            '/nrm/static/lib/modules/ag-grid/theme-fresh.css'
                        ]
                    }
                ]
            });
        }
    ]);