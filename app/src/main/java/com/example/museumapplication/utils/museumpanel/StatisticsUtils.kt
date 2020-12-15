package com.example.museumapplication.utils.museumpanel

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.example.museumapplication.data.Artifact
import com.example.museumapplication.data.Visit
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class StatisticsUtils {

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

    @SuppressLint("SimpleDateFormat")
    fun getLastWeekVisitLengths(visitList: ArrayList<Visit>): MutableList<DataEntry?> {
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
        val data = mutableListOf<DataEntry?>()
        for (i in 0..6)
            data.add(ValueDataEntry(getDay(dayOfWeekList[i]), lengths[i]))
        return data
    }

    @SuppressLint("SimpleDateFormat")
    fun getLastWeekVisits(visitList: ArrayList<Visit>): MutableList<DataEntry?> {
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
        val data = mutableListOf<DataEntry?>()
        for (i in 0..6)
            data.add(ValueDataEntry(getDay(dayOfWeekList[i]), counts[i]))
        return data
    }

    fun getLastWeekTotalVisit(dataEntry: MutableList<DataEntry?>): Int {
        var result = 0
        for (entry: DataEntry? in dataEntry) {
            result += entry!!.getValue("value").toString().toInt()
        }
        return result
    }

    fun findTotalVisitLength(visitList: ArrayList<Visit>): Int {
        var result = 0
        for (visit: Visit in visitList)
            result += visit.visitTime
        return result
    }

    fun findAverageVisitLength(visitList: ArrayList<Visit>): Int {
        return findTotalVisitLength(visitList) / visitList.size
    }

    @SuppressLint("SimpleDateFormat")
    fun findLastWeekAverageVisitLength(visitEntries: MutableList<DataEntry?>, lengthEntries: MutableList<DataEntry?>): Int {
        var result = 0
        var count = 0
        for (i in 1 until visitEntries.size) {

            result += (lengthEntries[i]!!.getValue("value").toString().toInt()) * visitEntries[i]!!.getValue("value").toString().toInt()
            count += visitEntries[i]!!.getValue("value").toString().toInt()
        }
        return if (result!=0)
            result / count
        else
            0
    }


    fun stringToBitMap(encodedString: String?): Bitmap? {
        val imageBytes = Base64.getDecoder().decode(encodedString)
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

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