package com.bt.rsqe.inlife.repository;

import com.bt.rsqe.inlife.entities.ErrorFactEntity;
import com.bt.rsqe.monitoring.ErrorFactDTO;

import java.util.List;

public interface ErrorFactRepository
{
    int save(ErrorFactEntity entity);

    ErrorFactEntity getErrorFactEntityById (int id);

    long getErrorFactEntityByOptionLineItemId(String id);

    List<ErrorFactEntity> getErrorFactsWithinTimeFrame(String startTime, String endTime);
}
