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
import com.huawei.agconnect.cloud.database.Text;
import com.huawei.agconnect.cloud.database.annotations.IsIndex;
import com.huawei.agconnect.cloud.database.annotations.PrimaryKey;

/**
 * Definition of ObjectType Artifact.
 *
 * @since 2020-11-18
 */
public class Artifact extends CloudDBZoneObject {
    @PrimaryKey
    @IsIndex(indexName = "artifactIndex")
    private Integer artifactID;

    private String artifactName;

    private Text artifactDescription;

    private Text artifactImage;

    private String museumID;

    private Integer currentVisitorCount;

    private Integer favoriteCount;

    private String category;

    public Artifact() {
        super();

    }
    public void setArtifactID(Integer artifactID) {
        this.artifactID = artifactID;
    }

    public Integer getArtifactID() {
        return artifactID;
    }

    public void setArtifactName(String artifactName) {
        this.artifactName = artifactName;
    }

    public String getArtifactName() {
        return artifactName;
    }

    public void setArtifactDescription(Text artifactDescription) {
        this.artifactDescription = artifactDescription;
    }

    public Text getArtifactDescription() {
        return artifactDescription;
    }

    public void setArtifactImage(Text artifactImage) {
        this.artifactImage = artifactImage;
    }

    public Text getArtifactImage() {
        return artifactImage;
    }

    public void setMuseumID(String museumID) {
        this.museumID = museumID;
    }

    public String getMuseumID() {
        return museumID;
    }

    public void setCurrentVisitorCount(Integer currentVisitorCount) {
        this.currentVisitorCount = currentVisitorCount;
    }

    public Integer getCurrentVisitorCount() {
        return currentVisitorCount;
    }

    public void setFavoriteCount(Integer favoriteCount) {
        this.favoriteCount = favoriteCount;
    }

    public Integer getFavoriteCount() {
        return favoriteCount;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

}
