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
import com.example.museumapplication.ui.museumpanel.MuseumPanelActivity
import com.example.museumapplication.utils.auth.FacebookAuth
import com.example.museumapplication.utils.auth.GoogleAuth
import com.example.museumapplication.utils.auth.HuaweiAuth
import com.facebook.login.LoginManager
import com.facebook.login.widget.LoginButton

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
         * Sign Up activity navigation
         */

        /**
         * Museum panel navigation
         */


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