package com.example.museumapplication.ui.museumpanel.analyticspanel

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.museumapplication.R

class GeneralPanelFragment : Fragment() {

    companion object {
        fun newInstance() = GeneralPanelFragment()
    }

    private lateinit var viewModel: GeneralPanelViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.general_panel_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(GeneralPanelViewModel::class.java)
        // TODO: Use the ViewModel
    }

}