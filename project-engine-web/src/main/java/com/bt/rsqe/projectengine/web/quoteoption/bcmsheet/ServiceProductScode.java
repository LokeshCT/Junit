package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;


public enum ServiceProductScode {

    ConnectAccelerationService("S0308491", "Connect Acceleration Service","Connect Acceleration Svce"),
    RiverbedProfessionalServices("S0308496", "Riverbed Professional Service","Riverbed Service"),
    CascadePilot("S0308499", "Cascade Pilot","Cascade Pilot"),
    CMCSteelheadLicencePack("S0308532", "Central Management Controller Steelhead Management Licence Pack","CMC Steelhead Lce Pack"),
    CMCVESteelheadLicencePack("S0308534", "Central Management Controller Virtual Engine Steelhead Management Licence Pack","CMC VE Steelhead Lce Pack"),
    ConnectAssessmentService("S0315971", "Connect Assessment Service","Connect Assessment Svce"),
    ConnectIntelligenceAPMoService("S0313969", "Connect Intelligence APMo Service","APMO Service"),
    ConnectIntelligenceWPMoService("S0313970", "Connect Intelligence WPMo Service","WPMO Service"),
    CompuwareProfessionalServices("S0313733", "Compuware Professional Services","Compuware Service"),
    ConnectOptimisationCentralServices("S0319693", "Connect Optimisation Central Service","CO Central Service"),
    ConnectOptimisationCentralServiceGenSpecialBid("S0319711", "Connect Optimisation Central Service General Special Bid","CO Central Special Bid"),
    PrimaryDNS("S0317996","Primary DNS","Primary DNS"),
    SecondaryDNS("S0318007","Secondary DNS","Secondary DNS"),
    ReverseFullDelegation("S0319513","Reverse Full Delegation","Reverse Full Delegation"),
    ReverseFractionalDelegation("S0319512","Reverse Fractional Delegation","Reverse Fractional"),
    OneCloudCiscoContract("S0320452","One Cloud Cisco Contract","One Cloud Cisco Contract"),
    OperatorConsole("S0320481","Operator Console","Operator Console"),
    AutoAttendantStarter("S0320485","Auto Attendant Starter (Menu + 10 Concurrent Calls","Auto Attendant Starter"),
    NumberBlock("S0317371","Number Block","Number Block"),
    InventoryReport("S0320489","Inventory Report","Inventory Report"),
    SipTrunkService("S0322591", "SIP Trunk Service","SIP Trunk Service"),
    ConnectAccelerationMonitoringService("S0320511", "Connect Acceleration Monitoring Service","Monitoring Service"),
    ConnectAccelerationMonitoringSiteSteelCentral("S0334691", "SteelCentral","SteelCentral"),
    //waiting for clarification
    /*InternetService("S0316769", "Internet Service","Internet Service"),
    InterceptorProfessionalService("S0308467", "Interceptor Professional Service","Interceptor Service"),
    AMDPortService("S0313708", "AMD Port Service","AMD Port Service"),
    InternetConnectGlobalAdditional("S0318005", "Internet Connect Global Additional Services","ICG Additional Service"),
    InternetConnectGlobalGeneralSpecialBidCOTC("S0319808","Internet Connect Global General Special Bid COTC","ICG Gen Special Bid COTC")*/
    INVALID("","","");

    private final String sCode;
    private final String serviceName;
    private final String shortServiceName;

    private ServiceProductScode(String sCode, String serviceName, String shortServiceName) {
        this.sCode = sCode;
        this.serviceName = serviceName;
        this.shortServiceName = shortServiceName;
    }

    public static String getShortServiceNameByScode(String sCode) {
        for (ServiceProductScode serviceCode : ServiceProductScode.values()) {
            if (serviceCode.sCode.equals(sCode)) {
                return serviceCode.shortServiceName;
            }
        }
        return "";
    }

    public String getsCode() {
        return sCode;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getShortServiceName() {
        return shortServiceName;
    }
}
