var services = angular.module('app.constants', []);

var REQUEST_GRID_HEADER = {
    waitingToBeIssued: "Requests to be issued",
    issued: "Issued Requests",
    progressing: "Requests to be Signed In",
    signedOut :  "Signed Out Requests",
    otherRequests : "Other Requests"
};

var BREADCRUM_DESCR = {
    waitingToBeIssued: "Following Requests are in created state and waiting to be issued",
    issued: "Following Requests are already issued and waiting to be Signed In",
    progressing: "Following Requests are already Signed In and in progress",
    signedOut :  "Following Requests are Signed Out and waiting to be Signed Off",
    otherRequests : "Following are the remaining requests from the list",
    requestDetails: "",
    templateDesc : "Showing complete information about this Template"
}




