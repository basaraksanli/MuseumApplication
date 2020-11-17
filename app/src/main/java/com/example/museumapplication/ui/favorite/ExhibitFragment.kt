package com.example.museumapplication.ui.favorite

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
import com.example.museumapplication.databinding.MuseumFragmentBinding

class ExhibitFragment : Fragment() {


    private lateinit var viewModel: ExhibitViewModel
    private lateinit var favoriteListAdapter: FavoriteArtifactListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding: ExhibitFragmentBinding =
                DataBindingUtil.inflate(inflater, R.layout.exhibit_fragment, container, false)


        val viewModel = ViewModelProvider(this ).get(ExhibitViewModel::class.java)

        binding.viewmodel = viewModel
        binding.lifecycleOwner = this


        val recyclerView = binding.root.findViewById<RecyclerView>(R.id.recyclerView)


        //Setting Layout manager for recyclerView
        recyclerView.layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)
        favoriteListAdapter = FavoriteArtifactListAdapter(requireContext() , binding.root)
        recyclerView.adapter = favoriteListAdapter


        return binding.root
    }



}