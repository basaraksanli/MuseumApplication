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
package com.example.museumapplication.ui.home.favorite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.alfianyusufabdullah.SPager
import com.example.museumapplication.R
import com.example.museumapplication.databinding.FavoriteMainFragmentBinding
import com.example.museumapplication.ui.home.favorite.tabs.FavoriteExhibitFragment
import com.example.museumapplication.ui.home.favorite.tabs.FavoriteMuseumFragment
import com.google.android.material.tabs.TabLayout

class MainFavoriteFragment : Fragment() , LifecycleOwner{


    lateinit var tabLayout: TabLayout
    lateinit var viewPager: SPager

    /**
     * Favorite Main Fragment
     * There are nested fragments inside with tabs
     * This fragment is the owner of Favorite Page Shared Model View
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding: FavoriteMainFragmentBinding =
                DataBindingUtil.inflate(inflater, R.layout.favorite_main_fragment, container, false)

        val viewModel = ViewModelProvider(this ).get(FavoritePageSharedModelView::class.java)

        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        /**
         * SPager Library Usage
         */
        tabLayout = binding.root.findViewById(R.id.tabLayout)
        viewPager = binding.root.findViewById(R.id.viewPager)
        viewPager.initFragmentManager(childFragmentManager)
        viewPager.addPages("Museums", FavoriteMuseumFragment())
        viewPager.addPages("Exhibits", FavoriteExhibitFragment())
        viewPager.addTabLayout(tabLayout)
        viewPager.build()

        return binding.root
    }

}