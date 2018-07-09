angular.module('app')
        .controller('QuoteOptionsController', ['$scope' , 'RequestsService','nrmUserService', function ($scope,RequestsService,nrmUserService) {

    $scope.quotesForGrid = [];

    RequestsService.getAllQuoteOptions( function (data, status) {
            if (!_.isUndefined(data)) {
                for(i=0;i<data.length ;i++) {
                    $scope.quotesForGrid.push({
                                                  customerName: data[i].customerName,
                                                  quoteOptionId: data[i].quoteId,
                                                  quoteOptionName: data[i].quoteOptionName,
                                                  sourceSystem:  data[i].sourceSystem,
                                                  salesChannelName:  data[i].salesChannelName,
                                                  createdUser:  data[i].createdUser,
                                                  createdDate:  $scope.getReadableDate(data[i].createdDate),
                                                  createdById:  data[i].createdById,
                                                  quote: data[i]
                                                         });
                };

                $scope.quoteGridOptions.rowData = $scope.quotesForGrid;
                $scope.quoteGridOptions.api.setRowData();
                $scope.columnStates = $scope.quoteGridOptions.columnApi.getState();
            }
    });

    $scope.quoteGridOptions = {
        rowData: $scope.quotesForGrid,
        rowSelection: 'multiple',
        enableColResize: true,
        enableSorting: true,
        enableFilter: true,
        rowHeight: 35,
        angularCompileRows : true,
        columnDefs:[
            {field:'customerName', headerName:'Customer Name'},
            {field:'quoteOptionId', headerName:'Quote Option Id', template:'<div class="ui-grid-cell-contents"><a style="padding-left: 5px;" ui-sref="app.quoteOptionDetails({quote:{{data.quote}}})">{{data.quoteOptionId}}</a></div>'},
            {field:'quoteOptionName', headerName:'Quote Option Name'},
            {field:'sourceSystem', headerName:'System'},
            {field:'salesChannelName', headerName:'Sales Channel Name'},
            {field:'createdUser', headerName:'Created By', cellRenderer:emailCellRenderer},
            {field:'createdDate', headerName:'Created Date'}
        ]
    };

    $scope.m_names = new Array("January", "February", "March",
                               "April", "May", "June", "July", "August", "September",
                               "October", "November", "December");

    $scope.getReadableDate = function(dateString){
        var createdDate = new Date(dateString);
        var createdDay = createdDate.getDate();
        var createdMonth = $scope.m_names[createdDate.getMonth()];
        var createdYear = createdDate.getFullYear();

        return createdDay+"-"+createdMonth+"-"+createdYear;
    }

    $scope.sendEmail = function(createdById,quoteId) {
        nrmUserService.getUserByEINOrName(createdById,function (data, status) {
            if (!_.isUndefined(data)) {
               var link = "mailto:"+ data[0].emailId
                                   + "?subject=Regarding Quote "+ quoteId;
                window.location.href = link;
            }
        });

    };

    function emailCellRenderer(params) {
        params.$scope.sendEmail = $scope.sendEmail;
        return '<div  class="ui-grid-cell-contents" style="padding-left: 5px;cursor:pointer;" ng-click="sendEmail({{data.createdById}},{{data.quoteOptionId}})"><a style="padding-left: 5px;">{{data.createdUser}}</a></div>';
    }

}]);

