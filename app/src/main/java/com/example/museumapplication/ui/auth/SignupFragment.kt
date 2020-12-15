package com.example.museumapplication.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.museumapplication.R
import com.example.museumapplication.databinding.FragmentLoginBinding
import com.example.museumapplication.databinding.FragmentSignupBinding
import com.example.museumapplication.ui.home.HomeActivity
import com.example.museumapplication.ui.museumpanel.analyticspanel.PagerViewModel
import com.example.museumapplication.utils.auth.EmailAuth
import com.example.museumapplication.utils.auth.IBaseAuth
import com.example.museumapplication.utils.auth.AuthUtils.checkFields

class SignupFragment : Fragment() {

    /**
     * SignUp Fragment
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val binding: FragmentSignupBinding =
                DataBindingUtil.inflate(inflater, R.layout.fragment_signup, container, false)

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

        /**
         * navigation to Login Page
         */
        viewModel.navigateToLoginPage.observe(viewLifecycleOwner, {
          if(it){
              parentFragmentManager.beginTransaction().replace(R.id.container, LoginFragment()).commit()
              viewModel.navigateToLoginPage.value = false
          }
        })

        return binding.root
    }
}