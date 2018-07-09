var services = angular.module('cqm-constants', []);

var NODE_STATUS = {
    NOT_APPLICABLE:"Not Applicable",
    INVALID:"Not Configured",
    VALID:"Configured",
    ERROR:"Configured with Error"
};

var EVENT = {
    LoadedSalesUser:"SalesUserLoadedEvent",
    CustomerSelectionChanged: "SelectedCustomerChangedEvent",
    StateChange: "StateChangedEvent",
    SubStateChanged: "SubStateChangedEvent",
    TabChange:"TabChangeEvent",
    TreeNodeNavigation:"TreeNodeNavigationEvent",
    NodeStatusChange:"NodeStatusChangeEvent",
    TabDeSelect:"TabDeSelectEvent",
    ActionDeSelect:"ActionDeSelectEvent",
    GoToCustomerTab:"GoToCustomerTabEvent",
    CustomerLoaded:"CustomerLoadedEvent",
    LoadDslCheckerApp :"LoadDslCheckerAppEvent"

};

var STATE = {
    CustomerSelection:'CustomerSelection',
    CustomerConfiguration:'CustomerConfiguration',
    DslChecker:'DslChecker',
    UserDashboard:'UserDashboard',
    Logout:'Logout'
};

var SESSION = {
    User:'user',
    Customer:'customer',
    State:'state',
    SubState:'subState',
    SelectedRole: 'selectedRole'
};
