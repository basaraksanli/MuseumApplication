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
import com.example.museumapplication.databinding.FragmentLoginBinding
import com.example.museumapplication.ui.home.HomeActivity
import com.example.museumapplication.utils.auth.FacebookAuth
import com.example.museumapplication.utils.auth.GoogleAuth
import com.example.museumapplication.utils.auth.HuaweiAuth
import com.facebook.login.LoginManager

class AccountLoginFragment : Fragment() {

    lateinit var viewModel : SharedAuthViewModel

    /**
     * Login fragment for login tasks
     * It uses Shared Auth View Model of Auth Activity
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        val binding: FragmentLoginBinding =
                DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)

        val viewModel = ViewModelProvider(requireActivity()).get(SharedAuthViewModel::class.java)

        binding.lifecycleOwner = this
        binding.viewmodel = viewModel


        /**
         * Whenever an intent is assigned to the signInIntent variable in Shared Auth View Model
         * it starts activity for the result
         */
        viewModel.signInIntent.observe(viewLifecycleOwner, {
            startActivityForResult(it , viewModel.RC_SIGN_IN)
        })

        /**
         * Custom Facebook Login Button Click observation
         */
        viewModel.facebookLoginClicked.observe(viewLifecycleOwner, {
            if(it)
            {
                LoginManager.getInstance().logInWithReadPermissions(this, arrayListOf("public_profile", "user_friends"));
                viewModel.facebookLoginClicked.postValue(false)
            }
        })

        /**
         * Navigation to Home Page
         */
        viewModel.navigateToHomePage.observe(viewLifecycleOwner, {
            if (it) {
                startActivity(Intent(activity, HomeActivity::class.java))
                viewModel.navigateToHomePage.postValue(false)
            }
        })

        /**
         * Email login navigation
         */
        viewModel.navigateToEmailLogin.observe(viewLifecycleOwner,{
            if(it)
            {
                parentFragmentManager.beginTransaction().replace(R.id.container, EmailLoginFragment()).commit()
                viewModel.navigateToEmailLogin.value = false
            }

        })

        return binding.root
    }


    /**
     * onActivityResult for Login Methods
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val viewModel = ViewModelProvider(requireActivity()).get(SharedAuthViewModel::class.java)

        viewModel.itemClickableOrEnabled.postValue(false)
        super.onActivityResult(requestCode, resultCode, data)


        if (requestCode == GoogleAuth.RC_SIGN_IN) {
            (viewModel.auth as GoogleAuth).activityResult(data)
        }
        if (requestCode == HuaweiAuth.RC_SIGN_IN) {
            (viewModel.auth as HuaweiAuth).activityResult(data)
        }
        if (requestCode == FacebookAuth.RC_SIGN_IN) {
            (viewModel.auth as FacebookAuth).activityResult(requestCode, resultCode, data)
        }
    }

}