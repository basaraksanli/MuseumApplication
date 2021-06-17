/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import com.huawei.agconnect.auth.AGConnectAuth


class EmailLoginFragment : Fragment() {



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        val binding: FragmentEmailLoginBinding =
                DataBindingUtil.inflate(inflater, R.layout.fragment_email_login, container, false)

        val viewModel = ViewModelProvider(requireActivity()).get(SharedAuthViewModel::class.java)

        binding.lifecycleOwner = this
        binding.viewmodel = viewModel

        if(AGConnectAuth.getInstance().currentUser!=null)
            viewModel.navigateToHomePage.postValue(true)

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
                viewModel.navigateToMuseumPanel.postValue(false)
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