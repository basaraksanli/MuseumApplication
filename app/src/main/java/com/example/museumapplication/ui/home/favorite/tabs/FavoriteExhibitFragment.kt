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

package com.example.museumapplication.ui.home.favorite.tabs

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.museumapplication.R
import com.example.museumapplication.databinding.FavoriteExhibitFragmentBinding
import com.example.museumapplication.ui.home.favorite.FavoritePageSharedModelView
import com.example.museumapplication.ui.home.favorite.adapters.FavoriteArtifactListAdapter



/**
 * FavoriteExhibit Fragment -- Adapter is set for recycler view here
 */
class FavoriteExhibitFragment : Fragment() {


    private lateinit var favoriteListAdapter: FavoriteArtifactListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding: FavoriteExhibitFragmentBinding =
                DataBindingUtil.inflate(inflater, R.layout.favorite_exhibit_fragment, container, false)


        val viewModel = ViewModelProvider(this ).get(FavoritePageSharedModelView::class.java)

        binding.viewmodel = viewModel
        binding.lifecycleOwner = requireParentFragment()


        val recyclerView = binding.root.findViewById<RecyclerView>(R.id.recyclerView)


        //Setting Layout manager for recyclerView
        recyclerView.layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)
        favoriteListAdapter = FavoriteArtifactListAdapter(viewModel.artifactList.value!! ,requireContext() , binding.root)
        recyclerView.adapter = favoriteListAdapter


        return binding.root
    }



}