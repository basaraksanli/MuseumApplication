/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.museumapplication.data;

import com.huawei.agconnect.cloud.database.CloudDBZoneObject;
import com.huawei.agconnect.cloud.database.annotations.IsIndex;
import com.huawei.agconnect.cloud.database.annotations.PrimaryKey;

import java.util.Date;

/**
 * Definition of ObjectType Visit.
 *
 * @since 2020-11-18
 */
public class Visit extends CloudDBZoneObject {
    @PrimaryKey
    private Integer visitID;

    @IsIndex(indexName = "index")
    private Integer artifactID;

    private String userID;

    private Date date;

    private Integer visitTime;

    @IsIndex(indexName = "index2")
    private String museumID;

    public Visit() {
        super();

    }
    public void setVisitID(Integer visitID) {
        this.visitID = visitID;
    }

    public Integer getVisitID() {
        return visitID;
    }

    public void setArtifactID(Integer artifactID) {
        this.artifactID = artifactID;
    }

    public Integer getArtifactID() {
        return artifactID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserID() {
        return userID;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    public void setVisitTime(Integer visitTime) {
        this.visitTime = visitTime;
    }

    public Integer getVisitTime() {
        return visitTime;
    }

    public void setMuseumID(String museumID) {
        this.museumID = museumID;
    }

    public String getMuseumID() {
        return museumID;
    }

}
