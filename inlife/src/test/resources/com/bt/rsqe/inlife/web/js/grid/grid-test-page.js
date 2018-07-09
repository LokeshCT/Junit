'use strict';

var rsqeGridTest = angular.module('rsqeGridTest', ['rsqeGridModule']);

rsqeGridTest.controller('GridTestController', ['$scope', function($scope) {
    $scope.data = [
        {salesChannel:"BT GERMANY",customerName:"NEWLOSB1",quoteOptionName:"ECGSanity1",expedioQuoteId:"000000000194955",createdBy:"Bindu Mahadeva",createdDate:"01-Oct-14",status:"DRAFT",siteName:"SITEGER11",product:"S0333097",action:"PROVIDE"},
        {salesChannel:"BT GERMANY",customerName:"NEWLOSB1",quoteOptionName:"EVC_price",expedioQuoteId:"000000000194955",createdBy:"Bindu Mahadeva",createdDate:"01-Oct-14",status:"DRAFT",siteName:"NEWLO - CS1195",product:"S0333097",action:"PROVIDE"},
        {salesChannel:"BT GERMANY",customerName:"MX132721SC003",quoteOptionName:"PROVIDE QUOTE",expedioQuoteId:"000000000195005",createdBy:"Vigneshwar Mohan",createdDate:"01-Oct-14",status:"ORDER_CREATED",siteName:"MX132 - CS1236",product:"S0324033",action:"PROVIDE"},
        {salesChannel:"BT NETHERLANDS",customerName:"GSCE68254 ICG",quoteOptionName:"QUOTE PROVIDE",expedioQuoteId:"000000000194878",createdBy:"Dinesh Gaddagolla",createdDate:"01-Oct-14",status:"DRAFT",siteName:"SITE1",product:"Access",action:"PROVIDE"},
        {salesChannel:"BT GERMANY",customerName:"NEWLOSB1",quoteOptionName:"EVC_price",expedioQuoteId:"000000000194955",createdBy:"Bindu Mahadeva",createdDate:"01-Oct-14",status:"DRAFT",siteName:"NEWLO - CS1195",product:"S0333094",action:"PROVIDE"},
        {salesChannel:"BT GERMANY",customerName:"MX132721SC003",quoteOptionName:"PROVIDE QUOTE",expedioQuoteId:"000000000195005",createdBy:"Vigneshwar Mohan",createdDate:"01-Oct-14",status:"ORDER_CREATED",siteName:"MX132 - CS1236",product:"S0324031",action:"PROVIDE"},
        {salesChannel:"BTGS UK",customerName:"OCCHCSVS2",quoteOptionName:"TESTAUTOMATION1124852",expedioQuoteId:"000000000195000",createdBy:"Nagaraj Venkobasa",createdDate:"01-Oct-14",status:"ORDER_SUBMITTED",siteName:"TESTAUTOMATION1124852",product:"S0320492",action:"PROVIDE"},
        {salesChannel:"CSP TEST SANDBOX",customerName:"R34_REQUISITE",quoteOptionName:"NEWLATIFCCA68256",expedioQuoteId:"000000000194978",createdBy:"BharathKumar B",createdDate:"01-Oct-14",status:"DRAFT",siteName:"R34_R - CS1004",product:"S0308491",action:"PROVIDE"},
        {salesChannel:"BT AMERICAS",customerName:"MSECUSTOMER1",quoteOptionName:"QUOTEMSE1",expedioQuoteId:"000000000195002",createdBy:"Vijay Raja Murugan",createdDate:"01-Oct-14",status:"DRAFT",siteName:"SITEMSE1",product:"Access",action:"PROVIDE"},
        {salesChannel:"CSP TEST SANDBOX",customerName:"RGS ICG SC039",quoteOptionName:"CEASE 2",expedioQuoteId:"000000000195003",createdBy:"Priyanka Sunduru",createdDate:"01-Oct-14",status:"ORDER_SUBMITTED",siteName:"SITE03",product:"Access",action:"CEASE"},
        {salesChannel:"BT AMERICAS",customerName:"MX132721SC03",quoteOptionName:"discount",expedioQuoteId:"000000000194689",createdBy:"Vigneshwar Mohan",createdDate:"01-Oct-14",status:"DRAFT",siteName:"MX132 - CS1147",product:"S0333151",action:"PROVIDE"},
        {salesChannel:"BT AMERICAS",customerName:"XXX_CUSTOMER_REGRESSION",quoteOptionName:"ICG migrate",expedioQuoteId:"000000000194939",createdBy:"Sruthi Suresh",createdDate:"01-Oct-14",status:"DRAFT",siteName:"BRANCH_GERMNAY",product:"Internet Connect Global",action:"PROVIDE"},
        {salesChannel:"BT AMERICAS",customerName:"OCCUSA1",quoteOptionName:"QUOTEA",expedioQuoteId:"000000000195007",createdBy:"Venkat Jakkula",createdDate:"01-Oct-14",status:"DRAFT",siteName:"OCCUS - CS1219",product:"S0320452",action:"PROVIDE"},
        {salesChannel:"BT AMERICAS",customerName:"XXX_CUSTOMER_REGRESSION",quoteOptionName:"ICG migrate",expedioQuoteId:"000000000194939",createdBy:"Sruthi Suresh",createdDate:"01-Oct-14",status:"DRAFT",siteName:"XXX_C - CS1217",product:"Internet Connect Global",action:"PROVIDE"},
        {salesChannel:"BT GERMANY",customerName:"NEWLOSB1",quoteOptionName:"EVC_price",expedioQuoteId:"000000000194955",createdBy:"Bindu Mahadeva",createdDate:"01-Oct-14",status:"DRAFT",siteName:"SITEGER11",product:"S0333099",action:"PROVIDE"},
        {salesChannel:"BT GERMANY",customerName:"NEWLOSB1",quoteOptionName:"ECGSanity1",expedioQuoteId:"000000000194955",createdBy:"Bindu Mahadeva",createdDate:"01-Oct-14",status:"DRAFT",siteName:"NEWLO - CS1195",product:"S0333094",action:"PROVIDE"},
        {salesChannel:"CSP TEST SANDBOX",customerName:"RGS ICG SC039",quoteOptionName:"CEASE",expedioQuoteId:"000000000195003",createdBy:"Priyanka Sunduru",createdDate:"01-Oct-14",status:"CUSTOMER_APPROVED",siteName:"SITE03",product:"Internet Connect Global",action:"CEASE"},
        {salesChannel:"CSP TEST SANDBOX",customerName:"R34_REQUISITE",quoteOptionName:"NEWLATIFCCA68256",expedioQuoteId:"000000000194978",createdBy:"BharathKumar B",createdDate:"01-Oct-14",status:"DRAFT",siteName:"PRESITE1",product:"S0308454",action:"PROVIDE"},
        {salesChannel:"BT GERMANY",customerName:"NEWLOSB1",quoteOptionName:"ECGSanity1",expedioQuoteId:"000000000194955",createdBy:"Bindu Mahadeva",createdDate:"01-Oct-14",status:"DRAFT",siteName:"SITEGER11",product:"S0333099",action:"PROVIDE"},
        {salesChannel:"BT AMERICAS",customerName:"ICG-R34-CUSTOMER",quoteOptionName:"ICG",expedioQuoteId:"000000000194880",createdBy:"Ommprakash Mannar",createdDate:"01-Oct-14",status:"DRAFT",siteName:"BRAZIL SITE",product:"Internet Connect Global",action:"PROVIDE"},
        {salesChannel:"CSP TEST SANDBOX",customerName:"RGS ICG SC039",quoteOptionName:"CEASE 2",expedioQuoteId:"000000000195003",createdBy:"Priyanka Sunduru",createdDate:"01-Oct-14",status:"ORDER_SUBMITTED",siteName:"SITE03",product:"Internet Connect Global",action:"CEASE"},
        {salesChannel:"BT GERMANY",customerName:"NEWLOSB1",quoteOptionName:"EVC_price",expedioQuoteId:"000000000194955",createdBy:"Bindu Mahadeva",createdDate:"01-Oct-14",status:"DRAFT",siteName:"SITEGER11",product:"S0333097",action:"PROVIDE"},
        {salesChannel:"CSP TEST SANDBOX",customerName:"MSEIFCCUS",quoteOptionName:"MODIFY",expedioQuoteId:"000000000195012",createdBy:"Priyanka Sunduru",createdDate:"01-Oct-14",status:"DRAFT",siteName:"PROVIDEIFC",product:"Access",action:"MODIFY"},
        {salesChannel:"CSP TEST SANDBOX",customerName:"MSEIFCCUS",quoteOptionName:"MODIFY",expedioQuoteId:"000000000195012",createdBy:"Priyanka Sunduru",createdDate:"01-Oct-14",status:"DRAFT",siteName:"PROVIDEIFC",product:"Internet Connect Global",action:"MODIFY"},
        {salesChannel:"BT AMERICAS",customerName:"MX132721SC03",quoteOptionName:"discount",expedioQuoteId:"000000000194689",createdBy:"Vigneshwar Mohan",createdDate:"01-Oct-14",status:"DRAFT",siteName:"MX132 - CS1147",product:"S0324033",action:"PROVIDE"},
        {salesChannel:"CSP TEST SANDBOX",customerName:"R34_REQUISITE",quoteOptionName:"NEWLATIFCCA68256",expedioQuoteId:"000000000194978",createdBy:"BharathKumar B",createdDate:"01-Oct-14",status:"DRAFT",siteName:"R34_R - CS1004",product:"S0308534",action:"PROVIDE"},
        {salesChannel:"CSP TEST SANDBOX",customerName:"R34_REQUISITE",quoteOptionName:"NEWLATIFCCA68256",expedioQuoteId:"000000000194978",createdBy:"BharathKumar B",createdDate:"01-Oct-14",status:"DRAFT",siteName:"PRESITE1",product:"IP Connect",action:"PROVIDE"},
        {salesChannel:"BT AMERICAS",customerName:"MSECUSTOMER1",quoteOptionName:"QUOTEMSE1",expedioQuoteId:"000000000195002",createdBy:"Vijay Raja Murugan",createdDate:"01-Oct-14",status:"DRAFT",siteName:"SITEMSE1",product:"Internet Connect Global",action:"PROVIDE"},
        {salesChannel:"BT NETHERLANDS",customerName:"GSCE68254 ICG",quoteOptionName:"QUOTE PROVIDE",expedioQuoteId:"000000000194878",createdBy:"Dinesh Gaddagolla",createdDate:"01-Oct-14",status:"DRAFT",siteName:"SITE1",product:"Internet Connect Global",action:"PROVIDE"},
        {salesChannel:"CSP TEST SANDBOX",customerName:"RGS ICG SC039",quoteOptionName:"CEASE",expedioQuoteId:"000000000195003",createdBy:"Priyanka Sunduru",createdDate:"01-Oct-14",status:"CUSTOMER_APPROVED",siteName:"SITE03",product:"Access",action:"CEASE"},
        {salesChannel:"BT AMERICAS",customerName:"XXX_CUSTOMER_REGRESSION",quoteOptionName:"ICG migrate",expedioQuoteId:"000000000194939",createdBy:"Sruthi Suresh",createdDate:"01-Oct-14",status:"DRAFT",siteName:"BRANCH_GERMNAY",product:"Access",action:"PROVIDE"},
        {salesChannel:"BTGS UK",customerName:"OCCHCSVS2",quoteOptionName:"TESTAUTOMATION1173636",expedioQuoteId:"000000000195013",createdBy:"Nagaraj Venkobasa",createdDate:"01-Oct-14",status:"ORDER_CREATED",siteName:"TESTAUTOMATION1173636",product:"S0320492",action:"PROVIDE"},
        {salesChannel:"BT AMERICAS",customerName:"MX132721SC03",quoteOptionName:"discount",expedioQuoteId:"000000000194689",createdBy:"Vigneshwar Mohan",createdDate:"01-Oct-14",status:"DRAFT",siteName:"MX132 - CS1147",product:"S0324031",action:"PROVIDE"},
        {salesChannel:"BT GERMANY",customerName:"MX132721SC003",quoteOptionName:"PROVIDE QUOTE",expedioQuoteId:"000000000195005",createdBy:"Vigneshwar Mohan",createdDate:"01-Oct-14",status:"ORDER_CREATED",siteName:"MX132 - CS1236",product:"S0333151",action:"PROVIDE"},
        {salesChannel:"BT AMERICAS",customerName:"ICG-R34-CUSTOMER",quoteOptionName:"ICG",expedioQuoteId:"000000000194880",createdBy:"Ommprakash Mannar",createdDate:"01-Oct-14",status:"DRAFT",siteName:"BRAZIL SITE",product:"Access",action:"PROVIDE"},
        {salesChannel:"BT AMERICAS",customerName:"ICG-R34-CUSTOMER",quoteOptionName:"ICG-01",expedioQuoteId:"000000000194880",createdBy:"Ommprakash Mannar",createdDate:"01-Oct-14",status:"DRAFT",siteName:"BRAZIL SITE",product:"Access",action:"PROVIDE"}
    ];

    $scope.columnDefs = [
        {field:'salesChannel', displayName:'Sales Channel'},
        {field:'customerName', displayName:'Customer Name'},
        {field:'quoteOptionName', displayName:'Quote Option Name'},
        {field:'siteName', displayName:'Site Name'},
        {field:'product', displayName:'Product'},
        {field:'expedioQuoteId', displayName:'Expedio Quote Id'},
        {field:'createdBy', displayName:'Created By'},
        {field:'createdDate', displayName:'Created Date'},
        {field:'status', displayName:'Status'},
        {field:'action', displayName:'Journey'}
    ];
}
]);


rsqeGridTest.controller('QuoteGridTestController', ['$scope', function($scope) {

    $scope.quotesSummaryByChannel = [
        {salesChannel:"BT GERMANY",stats:[
            {quoteOptionCount:"8",lineItemCount:"103", period:"Today",label:"09/10/2014"},
            {quoteOptionCount:"8",lineItemCount:"103", period:"Yesterday",label:"08/10/2014"},
            {quoteOptionCount:"8",lineItemCount:"103", period:"Last 7 Days",label:"01/10/2014 to 08/10/2014"},
            {quoteOptionCount:"8",lineItemCount:"103", period:"Last 30 Days",label:"09/09/2014 to 08/10/2014"},
            {quoteOptionCount:"8",lineItemCount:"103", period:"Last 90 Days",label:"11/07/2014 to 08/10/2014"},
            {quoteOptionCount:"8",lineItemCount:"103", period:"Prior to 90 Days",label:"before 11-07-2014"}
        ]},
        {salesChannel:"BT NETHERLANDS",stats:[
            {quoteOptionCount:"19",lineItemCount:"38", period:"Today",label:"09/10/2014"},
            {quoteOptionCount:"19",lineItemCount:"38", period:"Yesterday",label:"08/10/2014"},
            {quoteOptionCount:"19",lineItemCount:"38", period:"Last 7 Days",label:"01/10/2014 to 08/10/2014"},
            {quoteOptionCount:"19",lineItemCount:"38", period:"Last 30 Days",label:"09/09/2014 to 08/10/2014"},
            {quoteOptionCount:"19",lineItemCount:"38", period:"Last 90 Days",label:"11/07/2014 to 08/10/2014"},
            {quoteOptionCount:"19",lineItemCount:"38", period:"Prior to 90 Days",label:"before 11-07-2014"}
        ]},
        {salesChannel:"BTGS UK",stats:[
            {quoteOptionCount:"29",lineItemCount:"49", period:"Today",label:"09/10/2014"},
            {quoteOptionCount:"29",lineItemCount:"49", period:"Yesterday",label:"08/10/2014"},
            {quoteOptionCount:"29",lineItemCount:"49", period:"Last 7 Days",label:"01/10/2014 to 08/10/2014"},
            {quoteOptionCount:"29",lineItemCount:"49", period:"Last 30 Days",label:"09/09/2014 to 08/10/2014"},
            {quoteOptionCount:"29",lineItemCount:"49", period:"Last 90 Days",label:"11/07/2014 to 08/10/2014"},
            {quoteOptionCount:"29",lineItemCount:"49", period:"Prior to 90 Days",label:"before 11-07-2014"}
        ]},
        {salesChannel:"CSP TEST SANDBOX",stats:[
            {quoteOptionCount:"16",lineItemCount:"84", period:"Today",label:"09/10/2014"},
            {quoteOptionCount:"16",lineItemCount:"84", period:"Yesterday",label:"08/10/2014"},
            {quoteOptionCount:"16",lineItemCount:"84", period:"Last 7 Days",label:"01/10/2014 to 08/10/2014"},
            {quoteOptionCount:"16",lineItemCount:"84", period:"Last 30 Days",label:"09/09/2014 to 08/10/2014"},
            {quoteOptionCount:"16",lineItemCount:"84", period:"Last 90 Days",label:"11/07/2014 to 08/10/2014"},
            {quoteOptionCount:"16",lineItemCount:"84", period:"Prior to 90 Days",label:"before 11-07-2014"}
        ]},
        {salesChannel:"BT AMERICAS",stats:[
            {quoteOptionCount:"26",lineItemCount:"59", period:"Today",label:"09/10/2014"},
            {quoteOptionCount:"26",lineItemCount:"59", period:"Yesterday",label:"08/10/2014"},
            {quoteOptionCount:"26",lineItemCount:"59", period:"Last 7 Days",label:"01/10/2014 to 08/10/2014"},
            {quoteOptionCount:"26",lineItemCount:"59", period:"Last 30 Days",label:"09/09/2014 to 08/10/2014"},
            {quoteOptionCount:"26",lineItemCount:"59", period:"Last 90 Days",label:"11/07/2014 to 08/10/2014"},
            {quoteOptionCount:"26",lineItemCount:"59", period:"Prior to 90 Days",label:"before 11-07-2014"}
        ]}
    ];

    $scope.quoteSummaryColumns = [
        {field:'salesChannel', displayName:'Sales Channel'},
        {field:'today', displayName:'To day'},
        {field:'yesterday', displayName:'Yesterday'},
        {field:'last7days', displayName:'Last 7 days'},
        {field:'last30days', displayName:'This Month'},
        {field:'last90days', displayName:'Last 3 Months'},
        {field:'priorTo90Days', displayName:'Prior to 90 Days'}
    ];

    _.each($scope.quotesSummaryByChannel, function(quoteSummaryByChannel) {
        quoteSummaryByChannel.find = function(period) {
            return _.find(quoteSummaryByChannel.stats, function(stats) {
                return period == stats.period;
            })
        };
    });


    $scope.quoteSummaryData = _.map($scope.quotesSummaryByChannel, function(quoteSummaryByChannel) {
        var todayStats = quoteSummaryByChannel.find('Today');
        var yesterdayStats = quoteSummaryByChannel.find('Yesterday');
        var last7daysStats = quoteSummaryByChannel.find('Last 7 Days');
        var last30dayStats = quoteSummaryByChannel.find('Last 30 Days');
        var last90dayStats = quoteSummaryByChannel.find('Last 90 Days');
        var priorTo90dayStats = quoteSummaryByChannel.find('Prior to 90 Days');
        return {
            salesChannel: quoteSummaryByChannel.salesChannel,
            today: todayStats.quoteOptionCount + "(" + todayStats.lineItemCount + ")",
            yesterday: yesterdayStats.quoteOptionCount + "(" + yesterdayStats.lineItemCount + ")",
            last7days: last7daysStats.quoteOptionCount + "(" + last7daysStats.lineItemCount + ")",
            last30days: last30dayStats.quoteOptionCount + "(" + last30dayStats.lineItemCount + ")",
            last90days: last90dayStats.quoteOptionCount + "(" + last90dayStats.lineItemCount + ")",
            priorTo90Days: priorTo90dayStats.quoteOptionCount + "(" + priorTo90dayStats.lineItemCount + ")"
        };
    });

    $scope.quoteStatsSummary = {"yesterday":{"stats":[],"dateRange":"13-10-2014"},"last7Days":{"stats":[
        {"groupBy":"Connect Acceleration","quoteOptionCount":2,"lineItemCount":2}
    ],"dateRange":"07-10-2014 to 13-10-2014"},"last30Days":{"stats":[
        {"groupBy":"Connect Acceleration","quoteOptionCount":9,"lineItemCount":9},
        {"groupBy":"Internet Connect Reach","quoteOptionCount":3,"lineItemCount":3},
        {"groupBy":"Internet Connect Global","quoteOptionCount":14,"lineItemCount":14},
        {"groupBy":"One Cloud Cisco-Create Contract","quoteOptionCount":9,"lineItemCount":9},
        {"groupBy":"One Cloud Cisco-Build Order","quoteOptionCount":9,"lineItemCount":9},
        {"groupBy":"TEM","quoteOptionCount":5,"lineItemCount":7},
        {"groupBy":"Ethernet Connect Global","quoteOptionCount":5,"lineItemCount":5}
    ],"dateRange":"14-09-2014 to 13-10-2014"},"last90Days":{"stats":[
        {"groupBy":"Connect Acceleration","quoteOptionCount":29,"lineItemCount":29},
        {"groupBy":"Connect Intelligence","quoteOptionCount":6,"lineItemCount":6},
        {"groupBy":"Internet Connect Reach","quoteOptionCount":14,"lineItemCount":17},
        {"groupBy":"One Cloud Cisco-Build Order","quoteOptionCount":148,"lineItemCount":170},
        {"groupBy":"Internet Connect Global","quoteOptionCount":30,"lineItemCount":31},
        {"groupBy":"Connect Optimisation","quoteOptionCount":5,"lineItemCount":5},
        {"groupBy":"One Cloud Cisco-Create Contract","quoteOptionCount":88,"lineItemCount":91},
        {"groupBy":"TEM","quoteOptionCount":7,"lineItemCount":9},
        {"groupBy":"Ethernet Connect Global","quoteOptionCount":8,"lineItemCount":8}
    ],"dateRange":"16-07-2014 to 13-10-2014"},"total":{"stats":[
        {"groupBy":"Connect Acceleration","quoteOptionCount":66,"lineItemCount":68},
        {"groupBy":"Connect Intelligence","quoteOptionCount":12,"lineItemCount":12},
        {"groupBy":"Internet Connect Reach","quoteOptionCount":24,"lineItemCount":32},
        {"groupBy":"One Cloud Cisco-Build Order","quoteOptionCount":209,"lineItemCount":235},
        {"groupBy":"Internet Connect Global","quoteOptionCount":39,"lineItemCount":42},
        {"groupBy":"Connect Optimisation","quoteOptionCount":5,"lineItemCount":5},
        {"groupBy":"One Cloud Cisco-Create Contract","quoteOptionCount":207,"lineItemCount":217},
        {"groupBy":"TEM","quoteOptionCount":7,"lineItemCount":9},
        {"groupBy":"Ethernet Connect Global","quoteOptionCount":8,"lineItemCount":8}
    ],"dateRange":"Total"}};

    $scope.quoteSummaryColumns = [
        {field:'entity', displayName:' '},
        {field:'yesterday', displayName:'Yesterday'},
        {field:'last7days', displayName:'Last 7 days'},
        {field:'last30days', displayName:'This Month'},
        {field:'last90days', displayName:'Last 3 Months'},
        {field:'total', displayName:'Total'}
    ];

    $scope.quoteStatsSummary.findStats = function(range, entity) {

        var stats = _.find($scope.quoteStatsSummary[range].stats, function(stat) {
            return stat.groupBy == entity;
        });

        return _.isUndefined(stats) ? { "groupBy":entity, "quoteOptionCount":0, "lineItemCount":0 } : stats;
    };

    $scope.quoteSummaryData = _.map($scope.quoteStatsSummary.total.stats, function(stats) {
        var yesterdayStats = $scope.quoteStatsSummary.findStats('yesterday', stats.groupBy);
        var last7daysStats = $scope.quoteStatsSummary.findStats('last7Days', stats.groupBy);
        var last30dayStats = $scope.quoteStatsSummary.findStats('last30Days', stats.groupBy);
        var last90dayStats = $scope.quoteStatsSummary.findStats('last90Days', stats.groupBy);
        var priorTo90dayStats = $scope.quoteStatsSummary.findStats('total', stats.groupBy);
        return {
            entity: stats.groupBy,
            yesterday: yesterdayStats.quoteOptionCount + "(" + yesterdayStats.lineItemCount + ")",
            last7days: last7daysStats.quoteOptionCount + "(" + last7daysStats.lineItemCount + ")",
            last30days: last30dayStats.quoteOptionCount + "(" + last30dayStats.lineItemCount + ")",
            last90days: last90dayStats.quoteOptionCount + "(" + last90dayStats.lineItemCount + ")",
            total: priorTo90dayStats.quoteOptionCount + "(" + priorTo90dayStats.lineItemCount + ")"
        };
    });

    $scope.footerTemplate = '<div class="gridFooter" style="margin-top: 2px"><b>Note : </b> 1(7) means 1 Quote and 7 Quote Items</div>';
}
]);

