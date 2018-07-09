package com.bt.rsqe.inlife.repository;

import com.bt.rsqe.inlife.client.dto.ApplicationProperty;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "APPLICATION_PROPERTY_STORE")
public class ApplicationPropertyEntity {
    @Id
    @Column(name = "PROPERTY_NAME")
    private String name;

    @Column(name = "PROPERTY_VALUE")
    private String value;

    public ApplicationPropertyEntity() {}

    public ApplicationPropertyEntity(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public ApplicationProperty dto() {
        return new ApplicationProperty(name, value);
    }

    public static ApplicationPropertyEntity fromDto(ApplicationProperty property) {
        return new ApplicationPropertyEntity(property.getName(), property.getValue());
    }
}
