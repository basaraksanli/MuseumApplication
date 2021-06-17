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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.museumapplication.R
import com.example.museumapplication.databinding.FavoriteMuseumFragmentBinding
import com.example.museumapplication.ui.home.favorite.FavoritePageSharedModelView
import com.example.museumapplication.ui.home.favorite.adapters.FavoriteMuseumListAdapter

class FavoriteMuseumFragment : Fragment() {


    private lateinit var viewModel: FavoritePageSharedModelView
    private lateinit var favoriteListAdapter: FavoriteMuseumListAdapter

    /**
     * FavoriteMuseum Fragment -- Adapter is set for recycler view here
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding: FavoriteMuseumFragmentBinding =
                DataBindingUtil.inflate(inflater, R.layout.favorite_museum_fragment, container, false)


        val viewModel = ViewModelProvider(this ).get(FavoritePageSharedModelView::class.java)


        binding.viewmodel = viewModel
        binding.lifecycleOwner = requireParentFragment()

        val recyclerView = binding.root.findViewById<RecyclerView>(R.id.recyclerView)

        //Setting Layout manager for recyclerView
        recyclerView.layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)
        favoriteListAdapter = FavoriteMuseumListAdapter(viewModel.museumList.value!! ,requireContext() ,requireActivity())
        recyclerView.adapter = favoriteListAdapter



        return binding.root
    }



}