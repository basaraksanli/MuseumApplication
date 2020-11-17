package com.example.museumapplication.ui.museumpanel

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.alfianyusufabdullah.SPager
import com.example.museumapplication.R
import com.example.museumapplication.ui.museumpanel.analyticspanel.GeneralPanelFragment
import com.example.museumapplication.ui.museumpanel.analyticspanel.ExhibitPanelFragment
import com.google.android.material.tabs.TabLayout

class MuseumPanelActivity : AppCompatActivity() {
    lateinit var tabLayout: TabLayout
    lateinit var viewPager: SPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.museum_panel_activity)
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)
        viewPager.bringToFront()
        tabLayout.addTab(tabLayout.newTab().setText("Analysis"))
        tabLayout.addTab(tabLayout.newTab().setText("Details"))


        viewPager.initFragmentManager(supportFragmentManager)
        viewPager.addPages("General", GeneralPanelFragment())
        viewPager.addPages("Exhibits", ExhibitPanelFragment())
        viewPager.addTabLayout(tabLayout)
        viewPager.build()
    }
}