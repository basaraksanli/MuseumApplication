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

class FavoriteExhibitFragment : Fragment() {


    private lateinit var viewModel: FavoritePageSharedModelView
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