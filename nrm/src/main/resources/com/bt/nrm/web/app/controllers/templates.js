angular.module('app')
        .controller('TemplatesController', ['$scope' , 'ProductTemplateService', function ($scope, ProductTemplateService) {

    $scope.templateListForGrid = [];

    ProductTemplateService.getAllProducts(function (data, status) {
        if (!_.isUndefined(data)) {
            $scope.products = data;

            for(i=0;i<data.length ;i++) {
                for(j=0;j<data[i].templates.length ;j++) {
                    $scope.templateListForGrid.push({productCategoryName: data[i].productCategoryName,
                                                    templateCode: data[i].templates[j].templateCode,
                                                    templateName: data[i].templates[j].templateName,
                                                    versionNumber: data[i].templates[j].versionNumber,
                                                    workflowType: data[i].templates[j].workflowType.workflowName,
                                                    configurationType: data[i].templates[j].configurationType,
                                                    templateState: data[i].templates[j].templateState});
                }
            };

            $scope.gridOptionsTemplatesList.rowData = $scope.templateListForGrid;
            $scope.gridOptionsTemplatesList.api.setRowData();
        }
    });

    var columnDefs = [
        {headerName: 'Product Code', field: 'productCategoryName'},
        {headerName: "Template Code", field: "templateCode", template:'<div class="ui-grid-cell-contents"><a style="padding-left: 5px;" ui-sref="app.templateDetails({templateCode:data.templateCode,templateVersion:data.versionNumber})">{{data.templateCode}}</a></div>'},
        {headerName: "Template Name", field: "templateName", width: 350, template:'<div class="ui-grid-cell-contents"><a ui-sref="app.templateDetails({templateCode:data.templateCode,templateVersion:data.versionNumber})">{{data.templateName}}</a></div>'},
        {headerName: "Template Version", field: "versionNumber"},
        {headerName: "Workflow Type", field: "workflowType"},
        {headerName: "Configuration Type", field: "configurationType"},
        {headerName: "State", field: "templateState"}
    ];

    $scope.gridOptionsTemplatesList = {
        columnDefs: columnDefs,
        rowData: $scope.templateListForGrid,
        rowHeight: 40,
        enableSorting: true,
        enableColResize: true,
        enableFilter: true,
        onModelUpdated: onModelUpdated,
        angularCompileRows : true,
        groupUseEntireRow: true,
        groupKeys: ['productCategoryName']
    };

    function onModelUpdated() {
        var model = $scope.gridOptionsTemplatesList.api.getModel();
        var totalRows = $scope.gridOptionsTemplatesList.rowData.length;
        var processedRows = model.getVirtualRowCount();
        $scope.rowCount = processedRows.toLocaleString() + ' / ' + totalRows.toLocaleString();
    }

    /* $scope.templatesGrid = { data:'product.templates',
        columnDefs:[
            {field:'templateId', displayName:'Template Id',
                cellTemplate: '<a ui-sref="app.templateDetails({templateId:row.entity.templateId})">{{row.entity.templateId}}</a>',  width:'20%', rowHeight: '30'},
            {field:'templateName', displayName:'Template Name',  width:'80%', rowHeight: '30'}
        ]
    };

    $scope.resizeTemplatesGrid = function(){
        window.setTimeout(function () {
            $(window).resize();
        }, 1);
    }*/

}]);
