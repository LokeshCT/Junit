package com.bt.cqm.repository.user;


import com.bt.cqm.dto.user.SalesChannelDTO;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "SALES_CHANNEL")
public class SalesChannelEntity {
    @Id
    @Column(name = "SALES_CHANNEL_ID")
    private String id;

    @Column(name = "SALES_CHANNEL_NAME")
    private String salesChannelName;


    @Column(name = "GFR_CODE")
    private String gfrCode;

    @ManyToOne
    @JoinColumn(name = "ROLE_TYPE", referencedColumnName = "ROLE_TYPE_ID")
    private RoleTypeEntity roleType;

///CLOVER:OFF

    public SalesChannelEntity() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSalesChannelName() {
        return salesChannelName;
    }

    public void setSalesChannelName(String salesChannelName) {
        this.salesChannelName = salesChannelName;
    }

    public RoleTypeEntity getRoleType() {
        return roleType;
    }

    public void setRoleType(RoleTypeEntity roleType) {
        this.roleType = roleType;
    }

    public SalesChannelDTO toDto(SalesChannelDTO dto){
        dto.setId(this.id);
        dto.setName(this.getSalesChannelName());

        return dto;
    }

    public  SalesChannelDTO toNewDTO(){
        return toDto(new SalesChannelDTO());
    }

///CLOVER:ON
}
