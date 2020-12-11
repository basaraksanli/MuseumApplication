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