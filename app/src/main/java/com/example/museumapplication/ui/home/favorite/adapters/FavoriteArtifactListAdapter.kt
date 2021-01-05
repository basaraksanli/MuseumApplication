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
package com.example.museumapplication.ui.home.favorite.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.example.museumapplication.R
import com.example.museumapplication.data.FavoriteArtifact
import com.example.museumapplication.data.UserLoggedIn.Companion.instance
import com.example.museumapplication.utils.services.CloudDBManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.*


class FavoriteArtifactListAdapter(private val artifactList: ArrayList<FavoriteArtifact>, private val context: Context, private val root: View): RecyclerView.Adapter<FavoriteArtifactListAdapter.ModelViewHolder>() {


    fun update() {
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModelViewHolder {
        //Inflate layout according to our resource file list_view_item
        val view =
                LayoutInflater.from(parent.context).inflate(R.layout.row_item_favorite_artifact, parent, false)
        return ModelViewHolder(view)
    }

    override fun getItemCount(): Int {
        return artifactList.size
    }

    //This is the essential function for binding all the information to the items
    override fun onBindViewHolder(holder: ModelViewHolder, position: Int) {
        holder.bindItems(artifactList[position], context, artifactList, this, root)
    }



    class ModelViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        //We have initialize our TextViews here.
        private val artifactName: TextView = view.findViewById(R.id.artifactName_row)
        private val artifactImage: ImageView = view.findViewById(R.id.artifactImage)
        private val museumName : TextView = view.findViewById(R.id.museumName_Row)

        private val showInformation : ImageView = view.findViewById(R.id.showInformation)
        private val favoriteButton : ImageView = view.findViewById(R.id.delete_favorite)



        @SuppressLint("SetTextI18n")
        fun bindItems(item: FavoriteArtifact, context: Context, artifactList: ArrayList<FavoriteArtifact>, listAdapter: FavoriteArtifactListAdapter, root: View) {

            artifactName.text = item.artifactName
            artifactImage.setImageBitmap(stringToBitMap(item.artifactImage))
            museumName.text = item.museumName

            favoriteButton.setOnClickListener{
                artifactList.remove(item)
                instance.favoriteArtifactList.remove(item)
                instance.saveFavoriteMuseumListToDevice(context)
                listAdapter.update()
                CloudDBManager.instance.decreaseFavArtifact(item.artifactID)
            }
            val sp = PreferenceManager.getDefaultSharedPreferences(context)
            val darkMode: Boolean = sp.getBoolean("darkMode", false)

            showInformation.setOnClickListener{
                val bottomSheetDialog = BottomSheetDialog(
                        context, R.style.BottomSheetDialogTheme)
                val bottomSheetView = LayoutInflater.from(context.applicationContext)
                        .inflate(R.layout.layout_bottom_sheet_favorite_artifact, root.findViewById(R.id.bottom_sheet_container)
                        )
                if (!darkMode) {
                    bottomSheetView.findViewById<View>(R.id.bottom_sheet_container).background.setTint(Color.WHITE)
                    bottomSheetView.findViewById<TextView>(R.id.bottom_sheet_artifactName).setTextColor(Color.BLACK)
                    bottomSheetView.findViewById<TextView>(R.id.bottom_sheet_artifactDesc).setTextColor(Color.BLACK)
                }
                bottomSheetView.findViewById<TextView>(R.id.bottom_sheet_museumName).text = item.museumName
                bottomSheetView.findViewById<TextView>(R.id.bottom_sheet_artifactName).text = item.artifactName
                bottomSheetView.findViewById<TextView>(R.id.bottom_sheet_artifactDesc).text = item.artifactDescription
                bottomSheetView.findViewById<ImageView>(R.id.bottom_sheet_artifactImage).setImageBitmap(stringToBitMap(item.artifactImage))
                bottomSheetView.findViewById<ImageView>(R.id.starArtifact).setColorFilter(root.context.resources.getColor(R.color.color_gold), PorterDuff.Mode.SRC_IN)

                bottomSheetView.findViewById<ImageButton>(R.id.playButton).setOnClickListener{
                    instance.ttsUtils.startTTSreading(item.artifactDescription)
                }
                bottomSheetView.findViewById<ImageButton>(R.id.stopButton).setOnClickListener{
                    instance.ttsUtils.stopTTSreading()
                }

                bottomSheetDialog.setContentView(bottomSheetView)
                bottomSheetDialog.show()

                bottomSheetView.findViewById<ImageView>(R.id.starArtifact).setOnClickListener {
                    artifactList.remove(item)
                    instance.favoriteArtifactList.remove(item)
                    instance.saveFavoriteMuseumListToDevice(context)
                    listAdapter.update()
                    bottomSheetDialog.hide()
                    instance.ttsUtils.stopTTSreading()
                    CloudDBManager.instance.decreaseFavArtifact(item.artifactID)
                }

            }
        }
        private fun stringToBitMap(encodedString: String?): Bitmap? {
            val imageBytes = Base64.getDecoder().decode(encodedString)
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        }
    }
}

