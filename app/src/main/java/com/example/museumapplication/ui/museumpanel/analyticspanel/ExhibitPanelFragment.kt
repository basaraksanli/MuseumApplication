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
import com.example.museumapplication.databinding.ExhibitPanelFragmentBinding

class ExhibitPanelFragment : Fragment() {


    /**
     * Exhibit Panel Fragment
     */

    private lateinit var viewModel: PagerViewModel
    private lateinit var binding: ExhibitPanelFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.exhibit_panel_fragment, container, false)

        viewModel = ViewModelProvider(requireActivity()).get(PagerViewModel::class.java)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        /**
         * Initialize Recycler view for exhibits
         */
        val recyclerView = binding.root.findViewById<RecyclerView>(R.id.recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)
        val exhibitPanelAdapter = ExhibitPanelAdapter(requireContext() , binding.root, viewModel.artifactList ,viewModel.visitList.value!!)
        recyclerView.adapter = exhibitPanelAdapter

        return binding.root
    }


}