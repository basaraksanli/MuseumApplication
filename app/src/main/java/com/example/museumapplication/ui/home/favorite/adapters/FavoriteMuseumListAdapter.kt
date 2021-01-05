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
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.museumapplication.R
import com.example.museumapplication.data.FavoriteMuseum
import com.example.museumapplication.data.UserLoggedIn.Companion.instance
import com.example.museumapplication.ui.home.HomeActivity

class FavoriteMuseumListAdapter(private val museumList: ArrayList<FavoriteMuseum>, private val context: Context, private val activity: Activity): RecyclerView.Adapter<FavoriteMuseumListAdapter.ModelViewHolder>() {



    fun update() {
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModelViewHolder {
        //Inflate layout according to our resource file list_view_item
        val view =
                LayoutInflater.from(parent.context).inflate(R.layout.row_item_favorite_museum, parent, false)
        return ModelViewHolder(view)
    }

    override fun getItemCount(): Int {
        return museumList.size
    }

    //This is the essential function for binding all the information to the items
    override fun onBindViewHolder(holder: ModelViewHolder, position: Int) {
        holder.bindItems(museumList[position], context,museumList , this , activity)
    }



    class ModelViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        //We have initialize our TextViews here.
        private val museumName: TextView = view.findViewById(R.id.museumName_Row)
        private val museumAddress: TextView = view.findViewById(R.id.museum_address)
        private val museumPhone: TextView = view.findViewById(R.id.museum_telephone)
        private val museumPage: TextView = view.findViewById(R.id.museum_webpage)
        private val showOnMap : ImageView = view.findViewById(R.id.show_onMap)
        private val favoriteButton : ImageView = view.findViewById(R.id.delete_favorite)




        @SuppressLint("SetTextI18n")
        fun bindItems(item: FavoriteMuseum, context: Context, museumList: ArrayList<FavoriteMuseum>, listAdapter: FavoriteMuseumListAdapter, activity: Activity) {

            museumName.text = item.museumName
            museumAddress.text = item.museumAddress
            museumPage.text = item.museumPage
            museumPhone.text = item.museumPhone

            favoriteButton.setOnClickListener{
                museumList.remove(item)
                instance.favoriteMuseumList.remove(item)
                instance.saveFavoriteMuseumListToDevice(context)
                listAdapter.update()
            }

            showOnMap.setOnClickListener{
                val extra = Bundle()
                extra.putDouble("favoriteLocationLat",item.museumLocation.lat)
                extra.putDouble("favoriteLocationLng",item.museumLocation.lng)
                val homeActivity = Intent(activity, HomeActivity::class.java)
                homeActivity.putExtras(extra)
                activity.startActivity(homeActivity)
            }
        }
    }
}

