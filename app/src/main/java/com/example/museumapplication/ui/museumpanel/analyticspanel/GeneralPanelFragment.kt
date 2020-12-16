package com.example.museumapplication.ui.museumpanel.analyticspanel

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.museumapplication.R
import com.example.museumapplication.databinding.GeneralPanelFragmentBinding

class GeneralPanelFragment : Fragment() {


    /**
     * General Panel Fragment
     */
    private lateinit var viewModel: PagerViewModel
    private lateinit var binding: GeneralPanelFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.general_panel_fragment, container, false)

        viewModel = ViewModelProvider(requireActivity()).get(PagerViewModel::class.java)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        return binding.root
    }
}