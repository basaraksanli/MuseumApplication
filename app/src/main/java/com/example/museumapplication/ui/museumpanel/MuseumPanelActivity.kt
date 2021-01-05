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
package com.example.museumapplication.ui.museumpanel

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import com.alfianyusufabdullah.SPager
import com.example.museumapplication.R
import com.example.museumapplication.databinding.MuseumPanelActivityBinding
import com.example.museumapplication.ui.museumpanel.analyticspanel.ExhibitPanelFragment
import com.example.museumapplication.ui.museumpanel.analyticspanel.GeneralPanelFragment
import com.example.museumapplication.ui.museumpanel.analyticspanel.PagerViewModel
import com.example.museumapplication.utils.services.CloudDBManager
import com.google.android.material.tabs.TabLayout


class MuseumPanelActivity : AppCompatActivity(), LifecycleOwner {
    lateinit var tabLayout: TabLayout
    lateinit var viewPager: SPager
    private var pagerAgentViewModel: PagerViewModel? = null


    /**
     * Museum Panel Activity
     * contains 2 fragments - Exhibit Panel - General Panel
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding =DataBindingUtil.setContentView<MuseumPanelActivityBinding>(this, R.layout.museum_panel_activity)

        pagerAgentViewModel = ViewModelProviders.of(this)[PagerViewModel::class.java]

        binding.viewmodel = pagerAgentViewModel
        binding.lifecycleOwner = this


        /**
         * Pager assignment and initialization
         */
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)

        viewPager.initFragmentManager(supportFragmentManager)
        viewPager.addPages("General", GeneralPanelFragment())
        viewPager.addPages("Exhibits", ExhibitPanelFragment())
        viewPager.addTabLayout(tabLayout)
        viewPager.build()



        val museumID = intent.getStringExtra("museumID")
        pagerAgentViewModel!!.museum.value = CloudDBManager.instance.getMuseum(museumID!!)!!
        pagerAgentViewModel!!.initialize()


    }

}