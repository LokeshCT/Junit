package com.bt.nrm.repository;

import com.bt.nrm.repository.entity.QuoteEntity;
import com.bt.nrm.repository.entity.RequestEntity;

import java.util.List;

public interface QuoteOptionRequestRepository {
    List<RequestEntity> getRequestsByUserId(String userId);
    List<RequestEntity> getRequestsByUserIdAndStates(String userId, String requestState);
    RequestEntity getRequestsByRequestId(String requestId);
    List<RequestEntity> getDataBuildRequests(String userId);
    int saveRequestGroupComments(String modifiedBy, String requestGroupId, String comments);
    int saveRequestComments(String userId, String modifiedBy, String comments);
    RequestEntity createRequest(RequestEntity requestEntity);
    List<QuoteEntity> getAllQuoteOptions();
    List<RequestEntity> getAllRequestsByQuoteId(String quoteOptionId);
    QuoteEntity getQuoteByQuoteOptionId(String quoteOptionId);
    int updateDataBuildStatus(String requestId,String dataBuildState,String modifiedBy);
    String getRequestIdFromSequence();
}