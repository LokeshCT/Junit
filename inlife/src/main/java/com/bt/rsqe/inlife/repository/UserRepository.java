package com.bt.rsqe.inlife.repository;

import com.bt.rsqe.inlife.entities.UserEntity;

import java.util.List;

public interface UserRepository
{
    int save(UserEntity entity);

    void persist(UserEntity entity);

    List<UserEntity> getUserEntity ();

    UserEntity getUserEntityByName(String value);
}
