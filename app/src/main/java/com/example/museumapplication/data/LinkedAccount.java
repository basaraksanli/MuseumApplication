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
import com.huawei.agconnect.cloud.database.annotations.DefaultValue;
import com.huawei.agconnect.cloud.database.annotations.IsIndex;
import com.huawei.agconnect.cloud.database.annotations.NotNull;
import com.huawei.agconnect.cloud.database.annotations.PrimaryKey;

/**
 * Definition of ObjectType LinkedAccount.
 *
 * @since 2020-12-24
 */
public class LinkedAccount extends CloudDBZoneObject {

    @PrimaryKey
    private String linkedID;

    @NotNull
    @DefaultValue(stringValue = "NA")
    private String accountID;

    public LinkedAccount() {
        this.accountID = "NA";

    }
    public LinkedAccount(String linkedID, String accountID) {
        this.linkedID = linkedID;
        this.accountID = accountID;
    }
    public void setLinkedID(String linkedID) {
        this.linkedID = linkedID;
    }

    public String getLinkedID() {
        return linkedID;
    }

    public void setAccountID(String accountID) {
        this.accountID = accountID;
    }

    public String getAccountID() {
        return accountID;
    }

}
