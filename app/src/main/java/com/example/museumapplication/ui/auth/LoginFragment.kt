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
import com.facebook.login.widget.LoginButton

class LoginFragment : Fragment() {

    lateinit var viewModel : SharedAuthViewModel
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        val binding: FragmentLoginBinding =
                DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)

        val viewModel = ViewModelProvider(requireActivity()).get(SharedAuthViewModel::class.java)

        binding.lifecycleOwner = this
        binding.viewmodel = viewModel

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

        viewModel.signInIntent.observe(viewLifecycleOwner, {
            startActivityForResult(it , viewModel.RC_SIGN_IN)
        })

        viewModel.facebookLoginClicked.observe(viewLifecycleOwner, {
            if(it)
            {
                val facebookButton = binding.root.findViewById<LoginButton>(R.id.facebookButton)
                facebookButton.performClick()
                viewModel.facebookLoginClicked.postValue(false)
            }
        })

        viewModel.navigateToHomePage.observe(viewLifecycleOwner, {
            if (it) {
                startActivity(Intent(activity, HomeActivity::class.java))
                viewModel.navigateToHomePage.postValue(false)
            }
        })

        return binding.root
    }



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