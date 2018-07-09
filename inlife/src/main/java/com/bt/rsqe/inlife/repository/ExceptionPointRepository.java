package com.bt.rsqe.inlife.repository;

import com.bt.rsqe.inlife.entities.ExceptionPointEntity;

import java.util.List;

public interface ExceptionPointRepository
{
    int save(ExceptionPointEntity entity);

    void persist(ExceptionPointEntity entity);

    List<ExceptionPointEntity> getExceptionPointEntity ();

    ExceptionPointEntity getExceptionPointEntityByName (String value);
}
