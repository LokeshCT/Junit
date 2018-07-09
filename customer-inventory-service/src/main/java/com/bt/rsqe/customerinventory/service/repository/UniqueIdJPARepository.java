package com.bt.rsqe.customerinventory.service.repository;

import com.bt.rsqe.customerinventory.repository.jpa.entities.FutureAssetUniqueIdEntity;
import com.bt.rsqe.encode.AlphaBase26;
import com.bt.rsqe.persistence.JPAPersistenceManager;

import javax.persistence.LockModeType;

public class UniqueIdJPARepository {
    public static final String GLOBAL_TYPE = "GLOBAL";
    private final JPAPersistenceManager jpa;

    public UniqueIdJPARepository(JPAPersistenceManager jpa) {
        this.jpa = jpa;
    }

    public String getNextUniqueId(String type) {
        FutureAssetUniqueIdEntity entity = jpa.entityManager().find(FutureAssetUniqueIdEntity.class, type, LockModeType.PESSIMISTIC_READ);
        entity.update();
        jpa.save(entity);
        return AlphaBase26.encode(entity.getSequenceValue());
    }
}
