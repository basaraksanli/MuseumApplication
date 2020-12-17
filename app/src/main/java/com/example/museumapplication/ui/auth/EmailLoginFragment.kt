package com.example.museumapplication.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.museumapplication.R
import com.example.museumapplication.databinding.FragmentEmailLoginBinding
import com.example.museumapplication.ui.home.HomeActivity
import com.example.museumapplication.ui.museumpanel.MuseumPanelActivity


class EmailLoginFragment : Fragment() {
    // TODO: Rename and change types of parameters


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        val binding: FragmentEmailLoginBinding =
                DataBindingUtil.inflate(inflater, R.layout.fragment_email_login, container, false)

        val viewModel = ViewModelProvider(requireActivity()).get(SharedAuthViewModel::class.java)

        binding.lifecycleOwner = this
        binding.viewmodel = viewModel

        /**
         * Home Page Navigation in case registration is complete
         */
        viewModel.navigateToHomePage.observe(viewLifecycleOwner, {
            if (it) {
                startActivity(Intent(activity, HomeActivity::class.java))
                viewModel.navigateToHomePage.postValue(false)
            }

        })
        viewModel.navigateToSignUpFragment.observe(viewLifecycleOwner, {
            if (it) {
                parentFragmentManager.beginTransaction().replace(R.id.container, SignupFragment()).commit()
                viewModel.navigateToSignUpFragment.value = false
            }
        })
        viewModel.navigateToMuseumPanel.observe(viewLifecycleOwner, {
            if (it){
                val newIntent = Intent(activity, MuseumPanelActivity::class.java)
                newIntent.putExtras(viewModel.extra)
                requireActivity().startActivity(newIntent)
            }
        })

        /**
         * navigation to Login Page
         */
        viewModel.navigateToLoginPage.observe(viewLifecycleOwner, {
            if(it){
                parentFragmentManager.beginTransaction().replace(R.id.container, AccountLoginFragment()).commit()
                viewModel.navigateToLoginPage.value = false
            }
        })

        return binding.root
    }

}