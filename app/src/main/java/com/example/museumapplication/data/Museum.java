/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2019-2019. All rights reserved.
 * Generated by the CloudDB ObjectType compiler.  DO NOT EDIT!
 */
package com.example.museumapplication.data;

import com.huawei.agconnect.cloud.database.CloudDBZoneObject;
import com.huawei.agconnect.cloud.database.annotations.DefaultValue;
import com.huawei.agconnect.cloud.database.Text;
import com.huawei.agconnect.cloud.database.annotations.NotNull;
import com.huawei.agconnect.cloud.database.annotations.IsIndex;
import com.huawei.agconnect.cloud.database.annotations.PrimaryKey;

import java.util.Date;

/**
 * Definition of ObjectType Museum.
 *
 * @since 2020-11-18
 */
public class Museum extends CloudDBZoneObject {
    @PrimaryKey
    @IsIndex(indexName = "museumID")
    private String museumID;

    private String museumName;

    private String museumAdress;

    private String museumLocation;

    private String museumPhone;

    private String museumEmail;

    private String museumPage;

    @NotNull
    @DefaultValue(stringValue = "0")
    private String totalFavorites;

    private String museumPass;

    @NotNull
    @DefaultValue(intValue = 0)
    private Integer exhibitCount;

    @NotNull
    @DefaultValue(intValue = 0)
    private Integer totalVisits;

    private String museumImageUrl;

    private String openningHour;

    private String closingHour;

    private String ticketPrice;

    public Museum() {
        super();
        this.totalFavorites = "0";
        this.exhibitCount = 0;
        this.totalVisits = 0;

    }
    public void setMuseumID(String museumID) {
        this.museumID = museumID;
    }

    public String getMuseumID() {
        return museumID;
    }

    public void setMuseumName(String museumName) {
        this.museumName = museumName;
    }

    public String getMuseumName() {
        return museumName;
    }

    public void setMuseumAdress(String museumAdress) {
        this.museumAdress = museumAdress;
    }

    public String getMuseumAdress() {
        return museumAdress;
    }

    public void setMuseumLocation(String museumLocation) {
        this.museumLocation = museumLocation;
    }

    public String getMuseumLocation() {
        return museumLocation;
    }

    public void setMuseumPhone(String museumPhone) {
        this.museumPhone = museumPhone;
    }

    public String getMuseumPhone() {
        return museumPhone;
    }

    public void setMuseumEmail(String museumEmail) {
        this.museumEmail = museumEmail;
    }

    public String getMuseumEmail() {
        return museumEmail;
    }

    public void setMuseumPage(String museumPage) {
        this.museumPage = museumPage;
    }

    public String getMuseumPage() {
        return museumPage;
    }

    public void setTotalFavorites(String totalFavorites) {
        this.totalFavorites = totalFavorites;
    }

    public String getTotalFavorites() {
        return totalFavorites;
    }

    public void setMuseumPass(String museumPass) {
        this.museumPass = museumPass;
    }

    public String getMuseumPass() {
        return museumPass;
    }

    public void setExhibitCount(Integer exhibitCount) {
        this.exhibitCount = exhibitCount;
    }

    public Integer getExhibitCount() {
        return exhibitCount;
    }

    public void setTotalVisits(Integer totalVisits) {
        this.totalVisits = totalVisits;
    }

    public Integer getTotalVisits() {
        return totalVisits;
    }

    public void setMuseumImageUrl(String museumImageUrl) {
        this.museumImageUrl = museumImageUrl;
    }

    public String getMuseumImageUrl() {
        return museumImageUrl;
    }

    public void setOpenningHour(String openningHour) {
        this.openningHour = openningHour;
    }

    public String getOpenningHour() {
        return openningHour;
    }

    public void setClosingHour(String closingHour) {
        this.closingHour = closingHour;
    }

    public String getClosingHour() {
        return closingHour;
    }

    public void setTicketPrice(String ticketPrice) {
        this.ticketPrice = ticketPrice;
    }

    public String getTicketPrice() {
        return ticketPrice;
    }

}