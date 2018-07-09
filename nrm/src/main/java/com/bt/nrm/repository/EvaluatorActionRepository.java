package com.bt.nrm.repository;

import com.bt.nrm.dto.EvaluatorActionsDTO;
import com.bt.nrm.dto.RequestEvaluatorDTO;
import com.bt.nrm.dto.RequestEvaluatorPriceGroupDTO;
import com.bt.nrm.dto.UserGroupDTO;

import java.util.List;
import java.util.Map;

public interface EvaluatorActionRepository {
    Map<String, List<EvaluatorActionsDTO>> getAllEvaluatorActions(List<UserGroupDTO> userGroupList, String userId);
    int acceptAgentAction(RequestEvaluatorDTO requestEvaluatorDTO);
    int updatePriceGroups(String requestId, Map<String,RequestEvaluatorPriceGroupDTO> requestEvaluatorPriceGroupDTOMap, String modifiedBy);
}
