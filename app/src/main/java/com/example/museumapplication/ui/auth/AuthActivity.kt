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
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.example.museumapplication.R
import com.example.museumapplication.databinding.ActivityAuthBinding


class AuthActivity : AppCompatActivity() {

    lateinit var viewModel: SharedAuthViewModel

    /**
     * Auth Activity which contains fragment container. Login and Sign Up fragments are called inside of this activity
     * This activity is the life cycle owner of the Shared Auth View Model
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = DataBindingUtil.setContentView<ActivityAuthBinding>(this, R.layout.activity_auth)

        viewModel = ViewModelProviders.of(this)[SharedAuthViewModel::class.java]

        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

    }

    /**
     * In order to stop this activity to go back Splash Activity, OnBackPressed function is overridden
     * If the active fragment is Sign Up fragment, activity navigates to the Login Page
     */
    override fun onBackPressed() {

        when (supportFragmentManager.findFragmentById(R.id.container)) {
            is EmailLoginFragment -> {
                viewModel.navigateToLoginPage.postValue(true)
            }
            is SignupFragment -> {
                viewModel.navigateToEmailLogin.postValue(true)
            }
            else -> {
                val startMain = Intent(Intent.ACTION_MAIN)
                startMain.addCategory(Intent.CATEGORY_HOME)
                startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(startMain)
            }
        }

    }
}