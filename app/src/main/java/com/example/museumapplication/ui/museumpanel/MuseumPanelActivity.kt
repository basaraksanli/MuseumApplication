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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding =DataBindingUtil.setContentView<MuseumPanelActivityBinding>(this, R.layout.museum_panel_activity)

        pagerAgentViewModel = ViewModelProviders.of(this)[PagerViewModel::class.java]

        binding.viewmodel = pagerAgentViewModel
        binding.lifecycleOwner = this



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