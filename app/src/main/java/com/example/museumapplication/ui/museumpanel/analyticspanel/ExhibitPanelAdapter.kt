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
package com.example.museumapplication.ui.museumpanel.analyticspanel


import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.museumapplication.R
import com.example.museumapplication.data.Artifact
import com.example.museumapplication.data.Visit
import com.example.museumapplication.utils.museumpanel.StatisticsUtils
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.*


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


            /**
             * Exhibit information is shown with Bottom Sheet Dialog
             */
            buttonView.setOnClickListener {
                val bottomSheetDialog = BottomSheetDialog(
                        context, R.style.BottomSheetDialogTheme)
                val bottomSheetView = LayoutInflater.from(context.applicationContext)
                        .inflate(R.layout.layout_bottom_sheet_panel_artifact, root.findViewById(R.id.bottom_sheet_container)
                        )


                /**
                 * UI assignments
                 */
                bottomSheetView.findViewById<TextView>(R.id.bottom_sheet_artifactName).text = item.artifactName

                bottomSheetView.findViewById<ImageView>(R.id.bottom_sheet_artifactImage).setImageBitmap(
                        statisticUtils.stringToBitMap(item.artifactImage.toString()))

                bottomSheetView.findViewById<TextView>(R.id.totalVisitsPanelBottom).text = visitCountTemp.toString()

                val lastWeekVisitCountData = statisticUtils.getLastWeekVisits(artifactVisitList)
                bottomSheetView.findViewById<TextView>(R.id.lastWeekVisits).text = statisticUtils.findLastWeekTotalVisit(lastWeekVisitCountData).toString()
                bottomSheetView.findViewById<TextView>(R.id.favoriteCount).text = item.favoriteCount.toString()

                bottomSheetView.findViewById<TextView>(R.id.totalVisitLength).text = (statisticUtils.findTotalVisitLength(artifactVisitList)/60).toString() + " m"
                bottomSheetView.findViewById<TextView>(R.id.averageVisitLength).text = statisticUtils.findAverageVisitLength(artifactVisitList).toString() + " s"
                val lastWeekVisitLengthData = statisticUtils.getLastWeekVisitLengths(artifactVisitList)
                bottomSheetView.findViewById<TextView>(R.id.lastWeekAverageVisitLength).text = statisticUtils.findLastWeekAverageVisitLength(lastWeekVisitCountData, lastWeekVisitLengthData).toString() +" s"

                val textViewListCountDays = arrayListOf<TextView>()
                textViewListCountDays.add(bottomSheetView.findViewById(R.id.firstDay))
                textViewListCountDays.add(bottomSheetView.findViewById(R.id.secondDay))
                textViewListCountDays.add(bottomSheetView.findViewById(R.id.thirdDay))
                textViewListCountDays.add(bottomSheetView.findViewById(R.id.fourthDay))
                textViewListCountDays.add(bottomSheetView.findViewById(R.id.fifthDay))
                textViewListCountDays.add(bottomSheetView.findViewById(R.id.sixthDay))
                textViewListCountDays.add(bottomSheetView.findViewById(R.id.seventhDay))

                val textViewListLengthDays = arrayListOf<TextView>()
                textViewListLengthDays.add(bottomSheetView.findViewById(R.id.firstDayL))
                textViewListLengthDays.add(bottomSheetView.findViewById(R.id.secondDayL))
                textViewListLengthDays.add(bottomSheetView.findViewById(R.id.thirdDayL))
                textViewListLengthDays.add(bottomSheetView.findViewById(R.id.fourthDayL))
                textViewListLengthDays.add(bottomSheetView.findViewById(R.id.fifthDayL))
                textViewListLengthDays.add(bottomSheetView.findViewById(R.id.sixthDayL))
                textViewListLengthDays.add(bottomSheetView.findViewById(R.id.seventhDayL))

                for (i in 0..6){
                    textViewListCountDays[i].text = lastWeekVisitCountData[i].first
                    textViewListLengthDays[i].text = lastWeekVisitCountData[i].first
                }


                val textViewListCount = arrayListOf<TextView>()
                textViewListCount.add(bottomSheetView.findViewById(R.id.firstDayCount))
                textViewListCount.add(bottomSheetView.findViewById(R.id.secondDayCount))
                textViewListCount.add(bottomSheetView.findViewById(R.id.thirdDayCount))
                textViewListCount.add(bottomSheetView.findViewById(R.id.fourthDayCount))
                textViewListCount.add(bottomSheetView.findViewById(R.id.fifthDayCount))
                textViewListCount.add(bottomSheetView.findViewById(R.id.sixthDayCount))
                textViewListCount.add(bottomSheetView.findViewById(R.id.seventhDayCount))

                val textViewListLength = arrayListOf<TextView>()
                textViewListLength.add(bottomSheetView.findViewById(R.id.firstDayLength))
                textViewListLength.add(bottomSheetView.findViewById(R.id.secondDayLength))
                textViewListLength.add(bottomSheetView.findViewById(R.id.thirdDayLength))
                textViewListLength.add(bottomSheetView.findViewById(R.id.fourthDayLength))
                textViewListLength.add(bottomSheetView.findViewById(R.id.fifthDayLength))
                textViewListLength.add(bottomSheetView.findViewById(R.id.sixthDayLength))
                textViewListLength.add(bottomSheetView.findViewById(R.id.seventhDayLength))

                for (i in 0..6){
                    textViewListCount[i].text = lastWeekVisitCountData[i].second.toString()
                }

                for (i in 0..6){
                    textViewListLength[i].text = lastWeekVisitLengthData[i].second.toString()
                }

                bottomSheetDialog.setContentView(bottomSheetView)
                bottomSheetDialog.show()

            }
        }


    }


}

