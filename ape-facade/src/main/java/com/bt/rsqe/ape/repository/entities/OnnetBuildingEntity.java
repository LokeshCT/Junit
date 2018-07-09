package com.bt.rsqe.ape.repository.entities;

import com.bt.rsqe.ape.dto.OnnetBuildingDTO;
import com.bt.rsqe.ape.dto.OnnetBuildingForAccess;
import com.google.common.collect.Lists;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.Date;

import static com.bt.rsqe.utils.AssertObject.*;
import static com.google.common.collect.Lists.*;

@Entity
@Table(name = "ONNET_BUILDING")
public class OnnetBuildingEntity {

    @Id
    @SequenceGenerator(name = "ONNET_BUILDING_ID", sequenceName = "ONNET_BUILDING_ID", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ONNET_BUILDING_ID")
    @Column(name = "ONNET_BUILDING_ID")
    private Long id;

    @Column(name = "FLOOR_NAME")
    private String floorName;

    @Column(name = "BUILDING_CODE")
    private String buildingCode;

    @Column(name = "ADDRESS")
    private String address;

    @Column(name = "STREET_NUMBER")
    private String streetNumber;

    @Column(name = "STREET_NAME")
    private String streetName;

    @Column(name = "POST_CODE")
    private String postCode;

    @Column(name = "CITY")
    private String city;

    @Column(name = "STATE")
    private String state;

    @Column(name = "COUNTRY")
    private String country;

    @Column(name = "KGI")
    private String kgi;

    @Column(name = "ACCURACY")
    private Integer accuracy;

    @Column(name = "LATITUDE")
    private Double latitude;

    @Column(name = "LONGITUDE")
    private Double longitude;


    @Column(name = "DISTANCE")
    private String distance;

    @Column(name = "GROUP_NUMBER")
    private String groupNumber;

    @Column(name = "SUPPLIER")
    private String supplier;

    @Column(name = "ADDRESS_TYPE")
    private String addressType;

    @Column(name = "SELECTION")
    private String selection;

    @Column(name = "CREATED_BY")
    private String createdBy;

    @Column(name = "CREATED_DATE")
    private Date createDate;

    @ManyToOne
    @JoinColumn(name = "BFG_SITE_ID")
    private OnnetBuildingsWithEFMEntity onnetBuildingsWithEFMEntity;


    public OnnetBuildingEntity() {/*for JAXB*/}

    public OnnetBuildingEntity(String floorName, String buildingCode, String address, String streetNumber, String streetName, String postCode, String city, String state, String country, String kgi, Integer accuracy, Double latitude, Double longitude, String distance, String groupNumber, String supplier, String addressType, String selection, String createdBy, Date createDate, OnnetBuildingsWithEFMEntity onnetBuildingsWithEFMEntity) {
        this.floorName = floorName;
        this.buildingCode = buildingCode;
        this.address = address;
        this.streetNumber = streetNumber;
        this.streetName = streetName;
        this.postCode = postCode;
        this.city = city;
        this.state = state;
        this.country = country;
        this.kgi = kgi;
        this.accuracy = accuracy;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distance = distance;
        this.groupNumber = groupNumber;
        this.supplier = supplier;
        this.addressType = addressType;
        this.selection = selection;
        this.createdBy = createdBy;
        this.createDate = createDate;
        this.onnetBuildingsWithEFMEntity = onnetBuildingsWithEFMEntity;
    }

    public OnnetBuildingEntity(String floorName, String buildingCode, String address, String selection, String city) {
        this.floorName = floorName;
        this.buildingCode = buildingCode;
        this.address = address;
        this.selection = selection;
        this.city = city;
    }

    public String getFloorName() {
        return floorName;
    }

    public void setFloorName(String floorName) {
        this.floorName = floorName;
    }

    public String getBuildingCode() {
        return buildingCode;
    }

    public void setBuildingCode(String buildingCode) {
        this.buildingCode = buildingCode;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStreetNumber() {
        return streetNumber;
    }

    public void setStreetNumber(String streetNumber) {
        this.streetNumber = streetNumber;
    }

    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getKgi() {
        return kgi;
    }

    public void setKgi(String kgi) {
        this.kgi = kgi;
    }

    public Integer getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Integer accuracy) {
        this.accuracy = accuracy;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }


    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getGroupNumber() {
        return groupNumber;
    }

    public void setGroupNumber(String groupNumber) {
        this.groupNumber = groupNumber;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public String getAddressType() {
        return addressType;
    }

    public void setAddressType(String addressType) {
        this.addressType = addressType;
    }

    public String getSelection() {
        return selection;
    }

    public void setSelection(String selection) {
        this.selection = selection;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public OnnetBuildingDTO toOnnetBuildingDTO() {
        return new OnnetBuildingDTO(getBuildingCode(), getAddress(), getStreetNumber(), getStreetName(), getPostCode(),
                                    getCity(), getState(), getCountry(), getKgi(), getAccuracy(), getLatitude(),
                                    getLongitude(), isNull(getFloorName()) ? Lists.<String>newArrayList() : newArrayList(getFloorName()),
                                    getDistance(), getGroupNumber(), getSelection(), getFloorName());
    }

    public OnnetBuildingForAccess toOnnetBuilding() {
        return new OnnetBuildingForAccess(getBuildingCode(),getFloorName(),getAddressType());
    }
}
