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
package com.example.museumapplication.utils.museumpanel

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.museumapplication.data.Artifact
import com.example.museumapplication.data.Visit
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class StatisticsUtils {

    /**
     * assigning numbers to the days for the calender
     * this is generally used in the graphs
     */
    private fun getDay(dayNo: Int): String {
        when (dayNo) {
            1 -> return "Monday"
            2 -> return "Tuesday"
            3 -> return "Wednesday"
            4 -> return "Thursday"
            5 -> return "Friday"
            6 -> return "Saturday"
            0 -> return "Sunday"
        }
        return ""
    }

    /**
     * Statistical last week visit length calculations
     */
    @SuppressLint("SimpleDateFormat")
    fun getLastWeekVisitLengths(visitList: ArrayList<Visit>): MutableList<Pair<String, Int>> {
        val lengths = mutableListOf(0, 0, 0, 0, 0, 0, 0)
        val dayOfWeekList = arrayListOf<Int>()
        val sdf = SimpleDateFormat("yyyy-MM-dd ")
        val cal: Calendar = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -6)
        for (i in 0..6) {
            var visitCount = 0
            val temp = cal.time
            dayOfWeekList.add(cal.time.day)
            for (visit: Visit in visitList)
                if (sdf.format(temp) == sdf.format(visit.date)) {
                    lengths[i] = lengths[i] + visit.visitTime
                    visitCount++
                }
            if (visitCount != 0)
                lengths[i] = lengths[i] / visitCount
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        val data = mutableListOf<Pair<String, Int>>()
        for (i in 0..6)
            data.add(Pair(getDay(dayOfWeekList[i]), lengths[i]))
        return data
    }

    /**
     * Statistical last week visit counts calculations
     */
    @SuppressLint("SimpleDateFormat")
    fun getLastWeekVisits(visitList: ArrayList<Visit>): MutableList<Pair<String, Int>> {
        val counts = mutableListOf(0, 0, 0, 0, 0, 0, 0)
        val dayOfWeekList = arrayListOf<Int>()
        val sdf = SimpleDateFormat("yyyy-MM-dd ")
        val cal: Calendar = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -6)
        for (i in 0..6) {
            val temp = cal.time
            dayOfWeekList.add(cal.time.day)
            counts[i] = visitList.filter { visit -> sdf.format(visit.date) == sdf.format(temp) }.size
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        val data = mutableListOf<Pair<String, Int>>()
        for (i in 0..6)
            data.add(Pair(getDay(dayOfWeekList[i]), counts[i]))
        return data
    }

    /**
     * last week total visit calculation
     */
    fun findLastWeekTotalVisit(dataEntry: MutableList<Pair<String, Int>>): Int {
        var result = 0
        for (entry: Pair<String, Int>? in dataEntry) {
            result += entry!!.second.toString().toInt()
        }
        return result
    }

    /**
     * last week total visit length calculation
     */
    fun findTotalVisitLength(visitList: ArrayList<Visit>): Int {
        var result = 0
        for (visit: Visit in visitList)
            result += visit.visitTime
        return result
    }

    /**
     * total average visit length calculation
     */
    fun findAverageVisitLength(visitList: ArrayList<Visit>): Int {
        return findTotalVisitLength(visitList) / visitList.size
    }

    /**
     * last week average visit length calculation
     */
    @SuppressLint("SimpleDateFormat")
    fun findLastWeekAverageVisitLength(visitEntries: MutableList<Pair<String, Int>>, lengthEntries: MutableList<Pair<String, Int>>): Int {
        var result = 0
        var count = 0
        for (i in 1 until visitEntries.size) {

            result += (lengthEntries[i].second.toString().toInt()) * visitEntries[i].second.toString().toInt()
            count += visitEntries[i].second.toString().toInt()
        }
        return if (result!=0)
            result / count
        else
            0
    }


    /**
     * String to bitmap function
     */
    fun stringToBitMap(encodedString: String?): Bitmap? {
        val imageBytes = Base64.getDecoder().decode(encodedString)
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    /**
     * Top exhibits calculations
     */
    fun findMostAndLeastVisited(visitList: ArrayList<Visit>, artifactList: ArrayList<Artifact>): Pair<Artifact, Artifact> {
        val map = HashMap<Int, Int>()
        for (visit: Visit in visitList) {
            if (map[visit.artifactID] == null)
                map[visit.artifactID] = 1
            else
                map[visit.artifactID] = (map[visit.artifactID])!!.toInt() + 1
        }
        val list = map.toList().sortedByDescending { (_, value) -> value }
        val most = artifactList.first { x -> x.artifactID == list[0].first }
        val least = artifactList.first { x -> x.artifactID == list[list.size - 1].first }
        return Pair(most, least)
    }

    fun findLongestAndShortestVisit(visitList: ArrayList<Visit>, artifactList: ArrayList<Artifact>): Pair<Artifact, Artifact> {
        val map = HashMap<Int, Int>()
        for (visit: Visit in visitList) {
            if (map[visit.artifactID] == null)
                map[visit.artifactID] = visit.visitTime
            else
                map[visit.artifactID] = (map[visit.artifactID])!!.toInt() + visit.visitTime
        }
        val list = map.toList().sortedByDescending { (_, value) -> value }
        val most = artifactList.first { x -> x.artifactID == list[0].first }
        val least = artifactList.first { x -> x.artifactID == list[list.size - 1].first }
        return Pair(most, least)
    }
}