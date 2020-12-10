package com.example.museumapplication.utils.services

import android.content.Context
import android.util.Log
import com.example.museumapplication.data.*
import com.huawei.agconnect.cloud.database.*
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException

class CloudDBManager {
    var mCloudDB: AGConnectCloudDB? = null
    var mCloudDBZone: CloudDBZone? = null
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

    fun closeCloudDBZone() {
        try {
            mCloudDB!!.closeCloudDBZone(mCloudDBZone)
        } catch (e: AGConnectCloudDBException) {
            Log.w("CLOUD DB:", "closeCloudDBZoneError: " + e.message)
        }
    }

    fun upsertUser(user: User?) {
        if (mCloudDBZone == null) {
            Log.d("CLOUD DB:", "CloudDBZone is null, try re-open it")
            return
        }
        val upsertTask = mCloudDBZone!!.executeUpsert(user!!)
        upsertTask.addOnSuccessListener { cloudDBZoneResult: Int -> Log.d("CLOUD DB:", "insert $cloudDBZoneResult records") }.addOnFailureListener { e: Exception ->
            Log.e("CLOUD DB:", "Insert user info failed")
            e.printStackTrace()
        }
    }

    fun upsertAccountLinkInfo(account: LinkedAccount?) {
        if (mCloudDBZone == null) {
            Log.d("CLOUD DB:", "CloudDBZone is null, try re-open it")
            return
        }
        val upsertTask = mCloudDBZone!!.executeUpsert(account!!)
        upsertTask.addOnSuccessListener { cloudDBZoneResult: Int -> Log.d("CLOUD DB:", "insert $cloudDBZoneResult records") }.addOnFailureListener { e: Exception ->
            Log.e("CLOUD DB:", "Insert user info failed")
            e.printStackTrace()
        }
    }

    private fun queryUser(query: CloudDBZoneQuery<User>): CloudDBZoneSnapshot<User>? {
        if (mCloudDBZone == null) {
            Log.w("CLOUD DB", "CloudDBZone is null, try re-open it")
            return null
        }
        val queryTask = mCloudDBZone!!.executeQuery(query,
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY)
        queryTask.await()
        if (queryTask.exception != null) {
            Log.e("CLOUD DB:", "Query failed" + queryTask.exception)
            return null
        }
        return queryTask.result
    }

    private fun queryArtifact(query: CloudDBZoneQuery<Artifact>): CloudDBZoneSnapshot<Artifact>? {
        if (mCloudDBZone == null) {
            Log.w("CLOUD DB", "CloudDBZone is null, try re-open it")
            return null
        }
        val queryTask = mCloudDBZone!!.executeQuery(query,
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY)
        queryTask.await()
        if (queryTask.exception != null) {
            Log.e("CLOUD DB:", "Query failed" + queryTask.exception)
            return null
        }
        return queryTask.result
    }

    fun queryAccountInfo(query: CloudDBZoneQuery<LinkedAccount>): CloudDBZoneSnapshot<LinkedAccount>? {
        if (mCloudDBZone == null) {
            Log.w("CLOUD DB", "CloudDBZone is null, try re-open it")
            return null
        }
        val queryTask = mCloudDBZone!!.executeQuery(query, CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY)
        queryTask.await()
        if (queryTask.exception != null) {
            Log.e("CLOUD DB:", "Query failed" + queryTask.exception)
            return null
        }
        return queryTask.result
    }

    @Throws(AGConnectCloudDBException::class)
    fun queryByEmail(email: String?): User? {
        if (mCloudDBZone == null) {
            Log.d("CLOUD DB:", "CloudDBZone is null, try re-open it")
            return null
        }
        val query = CloudDBZoneQuery.where(User::class.java).equalTo("Email", email)
        val result = queryUser(query)
        return result?.snapshotObjects?.get(0)
    }

    @Throws(AGConnectCloudDBException::class)
    fun queryByID(ID: String?): User? {
        if (mCloudDBZone == null) {
            Log.d("CLOUD DB:", "CloudDBZone is null, try re-open it")
            return null
        }
        val query = CloudDBZoneQuery.where(User::class.java).equalTo("UID", ID)
        val result = queryUser(query)
        return result?.snapshotObjects?.get(0)
    }

    @Throws(AGConnectCloudDBException::class)
    fun queryArtifactByID(ID: Int): Artifact? {
        if (mCloudDBZone == null) {
            Log.d("CLOUD DB:", "CloudDBZone is null, try re-open it")
            return null
        }
        val query = CloudDBZoneQuery.where(Artifact::class.java).equalTo("artifactID", ID)
        val result = queryArtifact(query)
        return result?.snapshotObjects?.get(0)
    }

    fun checkFirstTimeUser(email: String?): Boolean {
        val query = CloudDBZoneQuery.where(User::class.java).equalTo("Email", email)
        val result = queryUser(query)
        return result!!.snapshotObjects.size() == 0
    }

    @Throws(AGConnectCloudDBException::class)
    fun getPrimaryAccountID_LinkedAccount(LinkedID: String?): String? {
        if (mCloudDBZone == null) {
            Log.d("CLOUD DB:", "CloudDBZone is null, try re-open it")
            return null
        }
        val query = CloudDBZoneQuery.where(LinkedAccount::class.java).equalTo("LinkedID", LinkedID)
        val result = queryAccountInfo(query)
        return result?.snapshotObjects?.get(0)?.accountID
    }

    companion object {
        //Singleton
        @JvmStatic
        val instance = CloudDBManager()
    }

    private fun queryMuseum(query: CloudDBZoneQuery<Museum>): CloudDBZoneSnapshot<Museum>? {
        if (mCloudDBZone == null) {
            Log.w("CLOUD DB", "CloudDBZone is null, try re-open it")
            return null
        }
        val queryTask = mCloudDBZone!!.executeQuery(query,
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY)
        queryTask.await()
        if (queryTask.exception != null) {
            Log.e("CLOUD DB:", "Query failed" + queryTask.exception)
            return null
        }
        return queryTask.result
    }

    private fun queryVisit(query: CloudDBZoneQuery<Visit>): CloudDBZoneSnapshot<Visit>? {
        if (mCloudDBZone == null) {
            Log.w("CLOUD DB", "CloudDBZone is null, try re-open it")
            return null
        }
        val queryTask = mCloudDBZone!!.executeQuery(query,
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY)
        queryTask.await()
        if (queryTask.exception != null) {
            Log.e("CLOUD DB:", "Query failed" + queryTask.exception)
            return null
        }
        return queryTask.result
    }


    fun checkMuseumIdAndPassword(ID: String, password: String): Museum? {
        if (mCloudDBZone == null) {
            Log.d("CLOUD DB:", "CloudDBZone is null, try re-open it")
            return null
        }
        return if (ID.isNotBlank() && password.isNotBlank()) {
            val query = CloudDBZoneQuery.where(Museum::class.java).equalTo("museumID", ID)
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

    fun getMuseum(ID: String): Museum? {
        if (mCloudDBZone == null) {
            Log.d("CLOUD DB:", "CloudDBZone is null, try re-open it")
            return null
        }
        val query = CloudDBZoneQuery.where(Museum::class.java).equalTo("museumID", ID)
        return queryMuseum(query)!!.snapshotObjects?.get(0)!!
    }


    fun getArtifactsOfMuseum(museumID: String, artifactList: ArrayList<Artifact>) {
        if (mCloudDBZone == null) {
            Log.d("CLOUD DB:", "CloudDBZone is null, try re-open it")
            return
        }
        val query = CloudDBZoneQuery.where(Artifact::class.java).equalTo("museumID", museumID)
        val result = queryArtifact(query)!!.snapshotObjects
        for (i in 0 until result.size()) {
            artifactList.add(result.get(i))
        }
    }

    fun increaseFavoredCountArtifact(artifactID: Int) {
        if (mCloudDBZone == null) {
            Log.d("CLOUD DB:", "CloudDBZone is null, try re-open it")
            return
        }
        val result = queryArtifactByID(artifactID)
        result!!.favoriteCount++
        val upsertTask = mCloudDBZone!!.executeUpsert(result)
        upsertTask.addOnSuccessListener { cloudDBZoneResult: Int ->
            Log.d("CLOUD DB:", "update $cloudDBZoneResult records")
        }.addOnFailureListener { e: Exception ->
            Log.e("CLOUD DB:", "Insert user info failed")
            e.printStackTrace()
        }
    }

    fun decreaseFavoredCountArtifact(artifactID: Int) {
        if (mCloudDBZone == null) {
            Log.d("CLOUD DB:", "CloudDBZone is null, try re-open it")
            return
        }
        val result = queryArtifactByID(artifactID)
        result!!.favoriteCount--
        val upsertTask = mCloudDBZone!!.executeUpsert(result)
        upsertTask.addOnSuccessListener { cloudDBZoneResult: Int -> Log.d("CLOUD DB:", "update $cloudDBZoneResult records") }
                .addOnFailureListener { e: Exception ->
                    Log.e("CLOUD DB:", "Update count failed")
                    e.printStackTrace()
                }
    }

    fun upsertVisit(visit: Visit) {
        if (mCloudDBZone == null) {
            Log.d("CLOUD DB:", "CloudDBZone is null, try re-open it")
            return
        }
        visit.visitID = findNewVisitID()
        val upsertTask = mCloudDBZone!!.executeUpsert(visit)
        upsertTask.addOnSuccessListener { cloudDBZoneResult: Int ->
            Log.d("CLOUD DB:", "update $cloudDBZoneResult records")
        }
                .addOnFailureListener { e: Exception ->
                    Log.e("CLOUD DB:", "Insert visit info failed")
                    e.printStackTrace()
                }
    }

    private fun findNewVisitID(): Int {
        if (mCloudDBZone == null) {
            Log.d("CLOUD DB:", "CloudDBZone is null, try re-open it")
            return -1
        }
        val query = CloudDBZoneQuery.where(Visit::class.java).orderByDesc("visitID")
        val result = queryVisit(query)!!.snapshotObjects

        return if (result.size() == 0)
            1
        else
            result.get(0).visitID + 1
    }

    fun increaseCurrentVisitCount(artifactID: Int) {
        if (mCloudDBZone == null) {
            Log.d("CLOUD DB:", "CloudDBZone is null, try re-open it")
            return
        }
        val result = queryArtifactByID(artifactID)
        result!!.currentVisitorCount++
        val upsertTask = mCloudDBZone!!.executeUpsert(result)
        upsertTask.addOnSuccessListener { cloudDBZoneResult: Int ->
            Log.d("CLOUD DB:", "update $cloudDBZoneResult records")
        }.addOnFailureListener { e: Exception ->
            Log.e("CLOUD DB:", "Insert user info failed")
            e.printStackTrace()
        }
    }

    fun decreaseCurrentVisitCount(artifactID: Int) {
        if (mCloudDBZone == null) {
            Log.d("CLOUD DB:", "CloudDBZone is null, try re-open it")
            return
        }
        val result = queryArtifactByID(artifactID)
        result!!.currentVisitorCount--
        val upsertTask = mCloudDBZone!!.executeUpsert(result)
        upsertTask.addOnSuccessListener { cloudDBZoneResult: Int ->
            Log.d("CLOUD DB:", "update $cloudDBZoneResult records")
        }.addOnFailureListener { e: Exception ->
            Log.e("CLOUD DB:", "Insert user info failed")
            e.printStackTrace()
        }
    }

    fun decreaseFavArtifact(artifactID: Int) {
        if (mCloudDBZone == null) {
            Log.d("CLOUD DB:", "CloudDBZone is null, try re-open it")
            return
        }
        val result = queryArtifactByID(artifactID)
        result!!.favoriteCount--
        val upsertTask = mCloudDBZone!!.executeUpsert(result)
        upsertTask.addOnSuccessListener { cloudDBZoneResult: Int ->
            Log.d("CLOUD DB:", "update $cloudDBZoneResult records")
        }.addOnFailureListener { e: Exception ->
            Log.e("CLOUD DB:", "Insert user info failed")
            e.printStackTrace()
        }
    }

    fun getMuseumVisits(museumID: String, visitList: ArrayList<Visit>) {
        if (mCloudDBZone == null) {
            Log.d("CLOUD DB:", "CloudDBZone is null, try re-open it")
            return
        }
        val query = CloudDBZoneQuery.where(Visit::class.java).equalTo("museumID", museumID)
        val result = queryVisit(query)!!.snapshotObjects
        for (i in 0 until result.size())
            visitList.add(result.get(i))
    }

}