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

/**
 * Definition of ObjectType User.
 *
 * @since 2020-12-24
 */

public class User extends CloudDBZoneObject {
    @PrimaryKey
    private String uID;

    private String providerID;

    private String email;

    private String displayName;

    private String photoURL;

    public User() {
        super();

    }
    public User(String uID, String providerUID, String email, String displayName, String photoUrl){
        this.uID = uID;
        this.providerID = providerUID;
        this.email = email;
        this.displayName = displayName;
        this.photoURL = photoUrl;
    }

    public void setUID(String uID) {
        this.uID = uID;
    }

    public String getUID() {
        return uID;
    }

    public void setProviderID(String providerID) {
        this.providerID = providerID;
    }

    public String getProviderID() {
        return providerID;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }

    public String getPhotoURL() {
        return photoURL;
    }

}
