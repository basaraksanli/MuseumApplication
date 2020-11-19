package com.example.museumapplication.ui.museumpanel.analyticspanel

import android.annotation.SuppressLint
import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.anychart.APIlib
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.bumptech.glide.Glide
import com.example.museumapplication.data.Artifact
import com.example.museumapplication.data.Museum
import com.example.museumapplication.data.Visit
import com.example.museumapplication.utils.StatisticsUtils
import com.example.museumapplication.utils.services.CloudDBHelper
import kotlin.collections.ArrayList


class PagerViewModel : ViewModel() {
    // TODO: Implement the ViewModel
    var museum = MutableLiveData<Museum>()
    var visitList = MutableLiveData<ArrayList<Visit>>()
    var artifactList = arrayListOf<Artifact>()


    var totalVisits = MutableLiveData(0)
    var progressBarVisibility = MutableLiveData(View.VISIBLE)
    var averageVisitLength = MutableLiveData(0)
    var totalFavorites = MutableLiveData(0)
    var totalExhibits = MutableLiveData(0)

    var mostVisitedArtifactName = MutableLiveData<String>()
    var mostVisitedArtifactImage = MutableLiveData<String>()

    var leastVisitedArtifactName = MutableLiveData<String>()
    var leastVisitedArtifactImage = MutableLiveData<String>()

    var longestVisitArtifactName = MutableLiveData<String>()
    var longestVisitArtifactImage = MutableLiveData<String>()

    var shortestVisitArtifactName = MutableLiveData<String>()
    var shortestVisitArtifactImage = MutableLiveData<String>()
    private val statisticsUtils = StatisticsUtils()

    init {
        visitList.value = ArrayList()
    }

    companion object {
        private val statisticsUtils = StatisticsUtils()
        @JvmStatic
        @BindingAdapter("museumImage")
        fun loadImage(view: ImageView, museumImage: String) {
            Glide.with(view.context)
                    .load(museumImage)
                    .into(view)
        }

        @SuppressLint("SimpleDateFormat")
        @JvmStatic
        @BindingAdapter("loadPlotTotalVisit")
        fun loadPlotTotalVisit(view: AnyChartView, visitList: ArrayList<Visit>) {

            APIlib.getInstance().setActiveAnyChartView(view)

            val data = statisticsUtils.getLastWeekVisits(visitList)

            val bar = AnyChart.bar3d()
            bar.data(data)
            bar.palette().items("Red", "#ff6f60")
            bar.animation(true)
            bar.contextMenu(true)
            bar.xAxis(0).labels(true)
            bar.yAxis(0).labels().format("{%Value}")
            bar.xAxis(0).title("Day")
            bar.yAxis(0).title("Count")
            bar.credits().enabled(false)
            view.setChart(bar)
        }

        @SuppressLint("SimpleDateFormat")
        @JvmStatic
        @BindingAdapter("loadPlotVisitLength")
        fun loadPlotVisitLength(view: AnyChartView, visitList: ArrayList<Visit>) {
            val statisticsUtils = StatisticsUtils()
            APIlib.getInstance().setActiveAnyChartView(view)

            val data = statisticsUtils.getLastWeekVisitLengths(visitList)
            val bar = AnyChart.bar3d()
            bar.data(data)
            bar.palette().items("Red", "#ff6f60")
            bar.animation(true)
            bar.contextMenu(true)
            bar.xAxis(0).labels(true)
            bar.yAxis(0).labels().format("{%Value} seconds")
            bar.xAxis(0).title("Day")
            bar.yAxis(0).title("Seconds")

            bar.credits().enabled(false)
            view.setChart(bar)
        }

        @JvmStatic
        @BindingAdapter("loadImageExhibitMost")
        fun loadImageExhibitMost(view: ImageView, artifactImage: String) {
            view.setImageBitmap(statisticsUtils.stringToBitMap(artifactImage))
        }
        @JvmStatic
        @BindingAdapter("loadImageExhibitLeast")
        fun loadImageExhibitLeast(view: ImageView, artifactImage: String) {
            view.setImageBitmap(statisticsUtils.stringToBitMap(artifactImage))
        }
        @JvmStatic
        @BindingAdapter("loadImageExhibitLongest")
        fun loadImageExhibitLongest(view: ImageView, artifactImage: String) {
            view.setImageBitmap(statisticsUtils.stringToBitMap(artifactImage))
        }
        @JvmStatic
        @BindingAdapter("loadImageExhibitShortest")
        fun loadImageExhibitShortest(view: ImageView, artifactImage: String) {
            view.setImageBitmap(statisticsUtils.stringToBitMap(artifactImage))
        }

    }


    fun initialize() {
        CloudDBHelper.instance.getMuseumVisits(museum.value!!.museumID, visitList.value!!)
        CloudDBHelper.instance.getArtifactsOfMuseum(museum.value!!.museumID, artifactList)
        totalVisits.value = visitList.value!!.size
        progressBarVisibility.value = View.GONE

        calculateAverageVisit()
        totalFavorites.value = museum.value!!.totalFavorites.toInt()
        totalExhibits.value = museum.value!!.exhibitCount


        artifactList.sortWith { rhs, lhs -> lhs.favoriteCount.compareTo(rhs.favoriteCount) }
        val pairVisitCount = statisticsUtils.findMostAndLeastVisited(visitList.value!!, artifactList)

        val mostVisited = pairVisitCount.first
        val leastVisited = pairVisitCount.second

        mostVisitedArtifactName.value = mostVisited.artifactName
        mostVisitedArtifactImage.value = mostVisited.artifactImage.toString()

        leastVisitedArtifactName.value = leastVisited.artifactName
        leastVisitedArtifactImage.value = leastVisited.artifactImage.toString()

        val pairVisitTime = statisticsUtils.findLongestAndShortestVisit(visitList.value!!, artifactList)

        val longestVisit = pairVisitTime.first
        val shortestVisit = pairVisitTime.second

        longestVisitArtifactName.value = longestVisit.artifactName
        longestVisitArtifactImage.value = longestVisit.artifactImage.toString()

        shortestVisitArtifactName.value = shortestVisit.artifactName
        shortestVisitArtifactImage.value = shortestVisit.artifactImage.toString()

    }

    private fun calculateAverageVisit() {
        var temp = 0
        for (visit: Visit in visitList.value!!) {
            temp += visit.visitTime
        }
        averageVisitLength.value = (temp / visitList.value!!.size)
    }


}