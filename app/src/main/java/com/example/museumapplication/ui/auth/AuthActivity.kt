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


        val fragment  = supportFragmentManager.findFragmentById(R.id.container)
        if ( fragment is EmailLoginFragment)
        {
            viewModel.navigateToLoginPage.postValue(true)
        }
        else if(fragment is SignupFragment)
        {
            viewModel.navigateToEmailLogin.postValue(true)
        }
        else
        {
            val startMain = Intent(Intent.ACTION_MAIN)
            startMain.addCategory(Intent.CATEGORY_HOME)
            startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(startMain)
        }

    }
}