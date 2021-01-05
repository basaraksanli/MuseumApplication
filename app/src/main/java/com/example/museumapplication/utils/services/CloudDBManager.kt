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
package com.example.museumapplication.utils.services

import android.content.Context
import android.util.Log
import com.example.museumapplication.data.*
import com.huawei.agconnect.cloud.database.*
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException

/**
 * Cloud DB processes
 */
class CloudDBManager {
    var mCloudDB: AGConnectCloudDB? = null
    var mCloudDBZone: CloudDBZone? = null

    /**
     * Singleton implementation of Cloud Db Manager
     */
    companion object {
        const val TAG = "Cloud DB"
        private const val cloudDBError = "CloudDBZone is null, try re-open it"
        private const val queryFailedError = "Query failed"
        //Singleton
        @JvmStatic
        val instance = CloudDBManager()
    }

    /**
     * Cloud Db initialization
     */
    fun initAGConnectCloudDB(context: Context?) {
        AGConnectCloudDB.initialize(context!!)
        mCloudDB = AGConnectCloudDB.getInstance()
        try {
            mCloudDB!!.createObjectType(ObjectTypeInfoHelper.getObjectTypeInfo())
        } catch (e: AGConnectCloudDBException) {
            e.printStackTrace()
        }
        val mConfig = CloudDBZoneConfig("MuseumApp",
                CloudDBZoneConfig.CloudDBZoneSyncProperty.CLOUDDBZONE_CLOUD_CACHE,
                CloudDBZoneConfig.CloudDBZoneAccessProperty.CLOUDDBZONE_PUBLIC)
        mConfig.persistenceEnabled = true
        try {
            mCloudDBZone = mCloudDB!!.openCloudDBZone(mConfig, true)
        } catch (e: AGConnectCloudDBException) {
            Log.w("Login Activity:", "openCloudDBZone: " + e.message)
        }
    }

    /**
     * Close Cloud Db zone
     */
    fun closeCloudDBZone() {
        try {
            mCloudDB!!.closeCloudDBZone(mCloudDBZone)
        } catch (e: AGConnectCloudDBException) {
            Log.w(TAG, "closeCloudDBZoneError: " + e.message)
        }
    }

    /**
     * Upsert user ( update and insert)
     */
    fun upsertUser(user: User?) {
        if (mCloudDBZone == null) {
            Log.d(TAG, cloudDBError)
            return
        }
        val upsertTask = mCloudDBZone!!.executeUpsert(user!!)
        upsertTask.addOnSuccessListener { cloudDBZoneResult: Int -> Log.d(TAG, "insert $cloudDBZoneResult records") }.addOnFailureListener { e: Exception ->
            Log.e(TAG, "Insert user info failed")
            e.printStackTrace()
        }
    }

    /**
     * upsert Account link info - Account Link objects are used to link the accounts
     * It contains original login method ID and the side login methods IDs
     */
    fun upsertAccountLinkInfo(account: LinkedAccount?) {
        if (mCloudDBZone == null) {
            Log.d(TAG, cloudDBError)
            return
        }
        val upsertTask = mCloudDBZone!!.executeUpsert(account!!)
        upsertTask.addOnSuccessListener { cloudDBZoneResult: Int -> Log.d(TAG, "insert $cloudDBZoneResult records") }.addOnFailureListener { e: Exception ->
            Log.e(TAG, "Insert account link info failed")
            e.printStackTrace()
        }
    }

    /**
     * Query user Function
     */
    private fun queryUser(query: CloudDBZoneQuery<User>): CloudDBZoneSnapshot<User>? {
        if (mCloudDBZone == null) {
            Log.w(TAG, cloudDBError)
            return null
        }
        val queryTask = mCloudDBZone!!.executeQuery(query,
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY)
        queryTask.await()
        if (queryTask.exception != null) {
            Log.e(TAG, queryFailedError + queryTask.exception)
            return null
        }
        return queryTask.result
    }

    /**
     * Query Artifact Function
     */
    private fun queryArtifact(query: CloudDBZoneQuery<Artifact>): CloudDBZoneSnapshot<Artifact>? {
        if (mCloudDBZone == null) {
            Log.w(TAG, cloudDBError)
            return null
        }
        val queryTask = mCloudDBZone!!.executeQuery(query,
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY)
        queryTask.await()
        if (queryTask.exception != null) {
            Log.e(TAG, queryFailedError + queryTask.exception)
            return null
        }
        return queryTask.result
    }

    /**
     * queryAccountInfo function
     */
    private fun queryAccountInfo(query: CloudDBZoneQuery<LinkedAccount>): CloudDBZoneSnapshot<LinkedAccount>? {
        if (mCloudDBZone == null) {
            Log.e(TAG, cloudDBError)
            return null
        }
        val queryTask = mCloudDBZone!!.executeQuery(query, CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY)
        queryTask.await()
        if (queryTask.exception != null) {
            Log.e(TAG, queryFailedError + queryTask.exception)
            return null
        }
        return queryTask.result
    }

    /**
     * queries user by email
     */
    @Throws(AGConnectCloudDBException::class)
    fun getUserByEmail(email: String?): User? {
        if (mCloudDBZone == null) {
            Log.d(TAG, cloudDBError)
            return null
        }
        val query = CloudDBZoneQuery.where(User::class.java).equalTo("email", email)
        val result = queryUser(query)
        return result?.snapshotObjects?.get(0)
    }

    /**
     * queries user by ID
     */
    @Throws(AGConnectCloudDBException::class)
    fun getUserByID(id: String?): User? {
        if (mCloudDBZone == null) {
            Log.d(TAG, cloudDBError)
            return null
        }
        val query = CloudDBZoneQuery.where(User::class.java).equalTo("uID", id)
        val result = queryUser(query)
        return result?.snapshotObjects?.get(0)
    }

    /**
     * query artifact by id
     */
    @Throws(AGConnectCloudDBException::class)
    fun getArtifactByID(id: Int): Artifact? {
        if (mCloudDBZone == null) {
            Log.d(TAG, cloudDBError)
            return null
        }
        val query = CloudDBZoneQuery.where(Artifact::class.java).equalTo("artifactID", id)
        val result = queryArtifact(query)
        return result?.snapshotObjects?.get(0)
    }

    /**
     * Checks if it is the new user
     */
    fun checkFirstTimeUser(email: String?): Boolean {
        val query = CloudDBZoneQuery.where(User::class.java).equalTo("email", email)
        val result = queryUser(query)
        return result!!.snapshotObjects.size() == 0
    }

    /**
     * Checks the Linked Account table, and get the original account's ID
     */
    @Throws(AGConnectCloudDBException::class)
    fun getMainAccountIDofLinkedAccount(linkedID: String?): String? {
        if (mCloudDBZone == null) {
            Log.d(TAG, cloudDBError)
            return null
        }
        val query = CloudDBZoneQuery.where(LinkedAccount::class.java).equalTo("linkedID", linkedID)
        val result = queryAccountInfo(query)
        return result?.snapshotObjects?.get(0)?.accountID
    }


    /**
     * Query Museum
     */
    private fun queryMuseum(query: CloudDBZoneQuery<Museum>): CloudDBZoneSnapshot<Museum>? {
        if (mCloudDBZone == null) {
            Log.w(TAG, cloudDBError)
            return null
        }
        val queryTask = mCloudDBZone!!.executeQuery(query,
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY)
        queryTask.await()
        if (queryTask.exception != null) {
            Log.e(TAG, queryFailedError + queryTask.exception)
            return null
        }
        return queryTask.result
    }

    /**
     * Query Visit
     */
    private fun queryVisit(query: CloudDBZoneQuery<Visit>): CloudDBZoneSnapshot<Visit>? {
        if (mCloudDBZone == null) {
            Log.w(TAG, cloudDBError)
            return null
        }
        val queryTask = mCloudDBZone!!.executeQuery(query,
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY)
        queryTask.await()
        if (queryTask.exception != null) {
            Log.e(TAG, queryFailedError + queryTask.exception)
            return null
        }
        return queryTask.result
    }


    /**
     * Museum panel login control
     */
    fun checkMuseumIdAndPassword(id: String, password: String): Museum? {
        if (mCloudDBZone == null) {
            Log.d(TAG, cloudDBError)
            return null
        }
        return if (id.isNotBlank() && password.isNotBlank()) {
            val query = CloudDBZoneQuery.where(Museum::class.java).equalTo("museumID", id)
            val snapshot = queryMuseum(query)
            if(snapshot!!.snapshotObjects.size()!=0) {
                val result = queryMuseum(query)!!.snapshotObjects?.get(0)!!
                if (result.museumPass == password)
                    result
                else
                    null
            }
            else
                null
        } else
            null
    }

    /**
     * query museum by ID
     */
    fun getMuseum(id: String): Museum? {
        if (mCloudDBZone == null) {
            Log.d(TAG, cloudDBError)
            return null
        }
        val query = CloudDBZoneQuery.where(Museum::class.java).equalTo("museumID", id)
        return queryMuseum(query)!!.snapshotObjects?.get(0)!!
    }


    /**
     * Get all artifacts of the specific museum
     */
    fun getArtifactsOfMuseum(museumID: String, artifactList: ArrayList<Artifact>) {
        if (mCloudDBZone == null) {
            Log.d(TAG, cloudDBError)
            return
        }
        val query = CloudDBZoneQuery.where(Artifact::class.java).equalTo("museumID", museumID)
        val result = queryArtifact(query)!!.snapshotObjects
        for (i in 0 until result.size()) {
            artifactList.add(result.get(i))
        }
    }

    /**
     * This function is increases the favored count of the artifact
     */
    fun increaseFavoredCountArtifact(artifactID: Int) {
        if (mCloudDBZone == null) {
            Log.d(TAG, cloudDBError)
            return
        }
        val result = getArtifactByID(artifactID)
        result!!.favoriteCount++
        val upsertTask = mCloudDBZone!!.executeUpsert(result)
        upsertTask.addOnSuccessListener { cloudDBZoneResult: Int ->
            Log.d(TAG, "update $cloudDBZoneResult records")
        }.addOnFailureListener { e: Exception ->
            Log.e(TAG, "Increase favored count is failed")
            e.printStackTrace()
        }
    }
    /**
     * This function is decreases the favored count of the artifact
     */
    fun decreaseFavoredCountArtifact(artifactID: Int) {
        if (mCloudDBZone == null) {
            Log.d(TAG, cloudDBError)
            return
        }
        val result = getArtifactByID(artifactID)
        result!!.favoriteCount--
        val upsertTask = mCloudDBZone!!.executeUpsert(result)
        upsertTask.addOnSuccessListener { cloudDBZoneResult: Int -> Log.d(TAG, "update $cloudDBZoneResult records") }
                .addOnFailureListener { e: Exception ->
                    Log.e(TAG, "Update count failed")
                    e.printStackTrace()
                }
    }

    /**
     * Visit upsert function update and insert
     */
    fun upsertVisit(visit: Visit) {
        if (mCloudDBZone == null) {
            Log.d(TAG, cloudDBError)
            return
        }
        visit.visitID = generateNewVisitID()
        val upsertTask = mCloudDBZone!!.executeUpsert(visit)
        upsertTask.addOnSuccessListener { cloudDBZoneResult: Int ->
            Log.d(TAG, "update $cloudDBZoneResult records")
        }
                .addOnFailureListener { e: Exception ->
                    Log.e(TAG, "Insert visit info failed")
                    e.printStackTrace()
                }
    }

    /**
     * Generates primary key for visit entry
     */
    private fun generateNewVisitID(): Int {
        if (mCloudDBZone == null) {
            Log.d(TAG, cloudDBError)
            return -1
        }
        val query = CloudDBZoneQuery.where(Visit::class.java).orderByDesc("visitID")
        val result = queryVisit(query)!!.snapshotObjects

        return if (result.size() == 0)
            1
        else
            result.get(0).visitID + 1
    }

    /**
     * increases current visit count of the artifact
     */
    fun increaseCurrentVisitCount(artifactID: Int) {
        if (mCloudDBZone == null) {
            Log.d(TAG, cloudDBError)
            return
        }
        val result = getArtifactByID(artifactID)
        result!!.currentVisitorCount++
        val upsertTask = mCloudDBZone!!.executeUpsert(result)
        upsertTask.addOnSuccessListener { cloudDBZoneResult: Int ->
            Log.d(TAG, "update $cloudDBZoneResult records")
        }.addOnFailureListener { e: Exception ->
            Log.e(TAG, "Increase current visit count is failed")
            e.printStackTrace()
        }
    }

    /**
     * decreases current visit count of the artifact
     */
    fun decreaseCurrentVisitCount(artifactID: Int) {
        if (mCloudDBZone == null) {
            Log.d(TAG, cloudDBError)
            return
        }
        val result = getArtifactByID(artifactID)
        result!!.currentVisitorCount--
        val upsertTask = mCloudDBZone!!.executeUpsert(result)
        upsertTask.addOnSuccessListener { cloudDBZoneResult: Int ->
            Log.d(TAG, "update $cloudDBZoneResult records")
        }.addOnFailureListener { e: Exception ->
            Log.e(TAG, "Decrease current visit count is failed")
            e.printStackTrace()
        }
    }


    /**
     * decreases favorite count of the artifact
     */
    fun decreaseFavArtifact(artifactID: Int) {
        if (mCloudDBZone == null) {
            Log.d(TAG, cloudDBError)
            return
        }
        val result = getArtifactByID(artifactID)
        result!!.favoriteCount--
        val upsertTask = mCloudDBZone!!.executeUpsert(result)
        upsertTask.addOnSuccessListener { cloudDBZoneResult: Int ->
            Log.d(TAG, "update $cloudDBZoneResult records")
        }.addOnFailureListener { e: Exception ->
            Log.e(TAG, "Decrease favorite artifact is failed")
            e.printStackTrace()
        }
    }

    /**
     * get all visit entries of the museum
     */
    fun getMuseumVisits(museumID: String, visitList: ArrayList<Visit>) {
        if (mCloudDBZone == null) {
            Log.d(TAG, cloudDBError)
            return
        }
        val query = CloudDBZoneQuery.where(Visit::class.java).equalTo("museumID", museumID)
        val result = queryVisit(query)!!.snapshotObjects
        for (i in 0 until result.size())
            visitList.add(result.get(i))
    }

}