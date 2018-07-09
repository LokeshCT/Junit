package com.bt.nrm.tobedeleted;

public class TestDataUtil {

    public static void generateRequestDetails(int lastRequestId, int lastRequestDetailsId, int noOfRequestDetailsRecordsNeeded){
        lastRequestDetailsId++;
        lastRequestId++;
        int requestDetailsId = lastRequestDetailsId;
        int requestId = lastRequestId;
        String[] requestDetails = {
        "'DecisionCriteria',NULL,'INITIAL LOAD',SYSDATE,'INITIAL LOAD',SYSDATE)//",
        "'PositiveRespTemplate',NULL,'INITIAL LOAD',SYSDATE,'INITIAL LOAD',SYSDATE)//",
        "'NegativeRespTemplate',NULL,'INITIAL LOAD',SYSDATE,'INITIAL LOAD',SYSDATE)//",
        "'SignOffRequirements',NULL,'INITIAL LOAD',SYSDATE,'INITIAL LOAD',SYSDATE)//",
        "'ConfigurationGuides',NULL,'INITIAL LOAD',SYSDATE,'INITIAL LOAD',SYSDATE)//",
        "'MoreInfoFoundAt', NULL, 'INITIAL LOAD', SYSDATE, 'INITIAL LOAD', SYSDATE)//",
        "'Alert Message', NULL, 'INITIAL LOAD', SYSDATE, 'INITIAL LOAD', SYSDATE)//",
        "'Alert on approval only',NULL,'INITIAL LOAD',SYSDATE,'INITIAL LOAD',SYSDATE)//",
        "'SLA Information',NULL,'INITIAL LOAD',SYSDATE,'INITIAL LOAD',SYSDATE)//",
        "'Systems Information',NULL,'INITIAL LOAD',SYSDATE,'INITIAL LOAD',SYSDATE)//",
        "'ACCESS TYPES',NULL,'INITIAL LOAD',SYSDATE,'INITIAL LOAD',SYSDATE)//",
        "'tpeCategory',NULL,'INITIAL LOAD',SYSDATE,'INITIAL LOAD',SYSDATE)//",
        "'TemplateId',NULL,'INITIAL LOAD',SYSDATE,'INITIAL LOAD',SYSDATE)//",
        "'Title','Access - Ethernet 10gig Access',NULL,'INITIAL LOAD',SYSDATE,'INITIAL LOAD',SYSDATE)//",
        "'Access Types', NULL, 'INITIAL LOAD', SYSDATE, 'INITIAL LOAD', SYSDATE)//",
        "'DataBuildRequired','true','INITIAL LOAD',SYSDATE,'INITIAL LOAD',SYSDATE)//",
        "'DataBuildCompleted','false','INITIAL LOAD',SYSDATE,'INITIAL LOAD',SYSDATE)//"};

        for(int i= 0;  i < noOfRequestDetailsRecordsNeeded; i++){
            for (int j=0; j< requestDetails.length; j++) {
                System.out.println("INSERT INTO REQUEST_DETAILS (REQUEST_DETAILS_ID,REQUEST_ID,ATTRIBUTE_NAME,ATTRIBUTE_VALUE,CREATED_USER,CREATED_DATE,MODIFIED_USER,MODIFIED_DATE) VALUES ('" + requestDetailsId +"'," + requestId + "," + requestDetails[j]);
                requestDetailsId++;
            }
            requestId++;
        }
    }

    public static void generateRequestHistory(int requestId, String state, int lastRequestHistoryId, int noOfRequestDetailsRecordsNeeded){
        int requestHistoryId = ++lastRequestHistoryId;
        String[] committedRequestHistory = {
                "'created','issued','Initial Load', 'Initial Load', sysdate)//",
                "'issued','progressing','Initial Load', 'Initial Load', sysdate)//",
                "'progressing','pre_responded','Initial Load', 'Initial Load', sysdate)//",
                "'pre_responded','responded','Initial Load', 'Initial Load', sysdate)//",
                "'responded','committed','Initial Load', 'Initial Load', sysdate)//"};
        for(int i= 0;  i < noOfRequestDetailsRecordsNeeded; i++){
            for (int j=0; j< committedRequestHistory.length; j++) {
                System.out.println("Insert into REQUEST_HISTORY (REQUEST_HISTORY_ID, REQUEST_ID, STATE_FROM, STATE_TO, CREATED_USER, CREATED_USER_NAME, CREATED_DATE) values ('" + requestHistoryId +"'," + requestId + "," + committedRequestHistory[j]);
                requestHistoryId++;
            }
            requestId++;
        }
    }

    public static void generateRequestWhoHasSeenIt(int requestId, int lastRequestWhoHasSeenItId, int noOfRequestDetailsRecordsNeeded){
        int requestWhoHasSeenItId = ++lastRequestWhoHasSeenItId;
        String[] committedRequestWhoHasSeenIt = {
                "'issued','Initial Load', 'Initial Load', sysdate)//",
                "'progressing','Initial Load', 'Initial Load', sysdate)//",
                "'pre_responded','Initial Load', 'Initial Load', sysdate)//",
                "'responded','Initial Load', 'Initial Load', sysdate)//",
                "'committed','Initial Load', 'Initial Load', sysdate)//"};
        for(int i= 0;  i < noOfRequestDetailsRecordsNeeded; i++){
            for (int j=0; j< committedRequestWhoHasSeenIt.length; j++) {
                System.out.println("Insert into REQUEST_WHO_HAS_SEEN (REQUEST_WHO_HAS_SEEN_ID, REQUEST_ID, STATE, CREATED_USER, CREATED_USER_NAME, CREATED_DATE) values ('" + requestWhoHasSeenItId +"'," + requestId + "," + committedRequestWhoHasSeenIt[j]);
                requestWhoHasSeenItId++;
            }
            requestId++;
        }
    }

    public static void main(String[] args) {
        //generateRequestDetails(23, 338, 6);
        generateRequestHistory(24, "", 51, 6);
        generateRequestWhoHasSeenIt(24, 50, 6);
    }
}
