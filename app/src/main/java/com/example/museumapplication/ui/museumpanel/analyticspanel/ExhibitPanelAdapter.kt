package com.example.museumapplication.ui.museumpanel.analyticspanel


import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.anychart.APIlib
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.example.museumapplication.R
import com.example.museumapplication.data.Artifact
import com.example.museumapplication.data.Visit
import com.example.museumapplication.utils.StatisticsUtils
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.yashovardhan99.timeit.Timer
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class ExhibitPanelAdapter(private val context: Context, private val root: View, private val artifactList: ArrayList<Artifact>, private val visitList: ArrayList<Visit>) : RecyclerView.Adapter<ExhibitPanelAdapter.ModelViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModelViewHolder {
        //Inflate layout according to our resource file list_view_item
        val view =
                LayoutInflater.from(parent.context).inflate(R.layout.row_item_panel_artifact, parent, false)
        return ModelViewHolder(view)
    }

    override fun getItemCount(): Int {
        return artifactList.size
    }

    //This is the essential function for binding all the information to the items
    override fun onBindViewHolder(holder: ModelViewHolder, position: Int) {
        holder.bindItems(artifactList[position], context, this, visitList, root)
    }


    class ModelViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        //We have initialize our TextViews here.
        private val artifactName: TextView = view.findViewById(R.id.artifactName_row)
        private val artifactImage: ImageView = view.findViewById(R.id.artifactImage)

        private val visitCount: TextView = view.findViewById(R.id.visitCount)
        private val favoriteCount: TextView = view.findViewById(R.id.favoriteCount)

        private val buttonView: LinearLayout = view.findViewById(R.id.linearButton)


        @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
        fun bindItems(item: Artifact, context: Context, listAdapter: ExhibitPanelAdapter, visitList: ArrayList<Visit>, root: View) {


            val artifactVisitList = visitList.filter { visit -> visit.artifactID == item.artifactID } as ArrayList<Visit>
            val statisticUtils = StatisticsUtils()

            artifactName.text = item.artifactName
            artifactImage.setImageBitmap(statisticUtils.stringToBitMap(item.artifactImage.toString()))
            favoriteCount.text = item.favoriteCount.toString()

            var visitCountTemp = 0

            for (visit: Visit in visitList)
                if (visit.artifactID == item.artifactID)
                    visitCountTemp++
            visitCount.text = visitCountTemp.toString()


            buttonView.setOnClickListener {
                val bottomSheetDialog = BottomSheetDialog(
                        context, R.style.BottomSheetDialogTheme)
                val bottomSheetView = LayoutInflater.from(context.applicationContext)
                        .inflate(R.layout.layout_bottom_sheet_panel_artifact, root.findViewById(R.id.bottom_sheet_container)
                        )

                bottomSheetView.findViewById<TextView>(R.id.bottom_sheet_artifactName).text = item.artifactName

                bottomSheetView.findViewById<ImageView>(R.id.bottom_sheet_artifactImage).setImageBitmap(
                        statisticUtils.stringToBitMap(item.artifactImage.toString()))

                bottomSheetView.findViewById<TextView>(R.id.totalVisitsPanelBottom).text = visitCountTemp.toString()

                val lastWeekVisitCountData = statisticUtils.getLastWeekVisits(artifactVisitList)
                bottomSheetView.findViewById<TextView>(R.id.lastWeekVisits).text = statisticUtils.getLastWeekTotalVisit(lastWeekVisitCountData).toString()
                bottomSheetView.findViewById<TextView>(R.id.favoriteCount).text = item.favoriteCount.toString()

                bottomSheetView.findViewById<TextView>(R.id.totalVisitLength).text = (statisticUtils.findTotalVisitLength(artifactVisitList)/60).toString() + " m"
                bottomSheetView.findViewById<TextView>(R.id.averageVisitLength).text = statisticUtils.findAverageVisitLength(artifactVisitList).toString() + " s"
                val lastWeekVisitLengthData = statisticUtils.getLastWeekVisitLengths(artifactVisitList)
                bottomSheetView.findViewById<TextView>(R.id.lastWeekAverageVisitLength).text = statisticUtils.findLastWeekAverageVisitLength(lastWeekVisitCountData, lastWeekVisitLengthData).toString() +" s"


                val anyChartViewVisit = bottomSheetView.findViewById<AnyChartView>(R.id.visitChart)
                APIlib.getInstance().setActiveAnyChartView(anyChartViewVisit)


                val barVisit = AnyChart.bar3d()
                barVisit.data(lastWeekVisitCountData)
                barVisit.palette().items("Red", "#ff6f60")
                barVisit.animation(true)
                barVisit.contextMenu(true)
                barVisit.xAxis(0).labels(true)
                barVisit.yAxis(0).labels().format("{%Value}")
                barVisit.xAxis(0).title("Day")
                barVisit.yAxis(0).title("Count")
                anyChartViewVisit.setChart(barVisit)

                val anyChartViewLength = bottomSheetView.findViewById<AnyChartView>(R.id.visitLengthChart)
                APIlib.getInstance().setActiveAnyChartView(anyChartViewLength)


                val barLength = AnyChart.bar3d()
                barLength.data(lastWeekVisitLengthData)
                barLength.palette().items("Red", "#ff6f60")
                barLength.animation(true)
                barLength.contextMenu(true)
                barLength.xAxis(0).labels(true)
                barLength.yAxis(0).labels().format("{%Value}")
                barLength.xAxis(0).title("Day")
                barLength.yAxis(0).title("Length")

                anyChartViewLength.setChart(barLength)


                bottomSheetDialog.setContentView(bottomSheetView)
                bottomSheetDialog.show()

            }
        }


    }


}

