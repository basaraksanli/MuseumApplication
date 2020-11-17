package com.example.museumapplication.ui.favorite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.alfianyusufabdullah.SPager
import com.example.museumapplication.R

import com.google.android.material.tabs.TabLayout
import java.lang.reflect.Field

class FavoriteFragment : Fragment() {


    lateinit var tabLayout: TabLayout
    lateinit var viewPager: SPager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.favorite_fragment, container, false)

        tabLayout = root.findViewById(R.id.tabLayout)
        viewPager = root.findViewById(R.id.viewPager)
        viewPager.initFragmentManager(childFragmentManager)
        viewPager.addPages("Museums", MuseumFragment())
        viewPager.addPages("Exhibits", ExhibitFragment())
        viewPager.addTabLayout(tabLayout)
        viewPager.build()

        return root
    }

}