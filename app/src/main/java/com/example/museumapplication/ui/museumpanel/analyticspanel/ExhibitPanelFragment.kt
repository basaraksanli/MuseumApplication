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
import com.example.museumapplication.databinding.ExhibitFragmentBinding
import com.example.museumapplication.databinding.ExhibitPanelFragmentBinding
import com.example.museumapplication.databinding.GeneralPanelFragmentBinding
import com.example.museumapplication.ui.favorite.FavoriteArtifactListAdapter

class ExhibitPanelFragment : Fragment() {


    private lateinit var viewModel: PagerViewModel
    private lateinit var binding: ExhibitPanelFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.exhibit_panel_fragment, container, false)

        viewModel = ViewModelProvider(requireActivity()).get(PagerViewModel::class.java)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        val recyclerView = binding.root.findViewById<RecyclerView>(R.id.recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)
        val exhibitPanelAdapter = ExhibitPanelAdapter(requireContext() , binding.root, viewModel.artifactList ,viewModel.visitList.value!!)
        recyclerView.adapter = exhibitPanelAdapter

        return binding.root
    }


}