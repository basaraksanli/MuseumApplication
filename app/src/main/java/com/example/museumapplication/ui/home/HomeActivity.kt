package com.example.museumapplication.ui.home

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.museumapplication.R
import com.example.museumapplication.data.UserLoggedIn
import com.example.museumapplication.ui.home.beacon.VirtualGuideFragment
import com.example.museumapplication.ui.home.favorite.MainFavoriteFragment
import com.example.museumapplication.ui.home.settings.SettingsFragment
import com.example.museumapplication.utils.auth.IBaseAuth.Companion.logout
import com.example.museumapplication.utils.settings.SettingsUtils
import com.facebook.login.LoginManager
import com.google.android.material.navigation.NavigationView
import com.huawei.agconnect.auth.AGConnectAuth
import de.hdodenhof.circleimageview.CircleImageView


class HomeActivity : AppCompatActivity() {
    private var mAppBarConfiguration: AppBarConfiguration? = null
    var auth: AGConnectAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        UserLoggedIn.instance.retrieveFavoriteMuseumList(this)
        UserLoggedIn.instance.retrieveFavoriteArtifactList(this)

        SettingsUtils.setTheme(this)
        setContentView(R.layout.activity_home)


        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        setSupportActionBar(toolbar)
        auth = AGConnectAuth.getInstance()
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)


        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = AppBarConfiguration.Builder(
                R.id.nav_explore, R.id.nav_settings, R.id.nav_map, R.id.nav_favorites)
                .setDrawerLayout(drawer)
                .build()


        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration!!)
        NavigationUI.setupWithNavController(navigationView, navController)
        setNavViewHeader(navigationView)

    }

    /**
     * Navigation Drawer View Assignments
     */
    private fun setNavViewHeader(nav: NavigationView) {
        val email = nav.getHeaderView(0).findViewById<TextView>(R.id.emailTextView)
        val name = nav.getHeaderView(0).findViewById<TextView>(R.id.nameTextView)
        val profilePictureView = nav.getHeaderView(0).findViewById<CircleImageView>(R.id.profilePictureView)
        name.text = UserLoggedIn.instance.name
        email.text = UserLoggedIn.instance.email

        /**
         * Profile picture download and assignment
         */
        Glide.with(this)
                .asBitmap()
                .load(UserLoggedIn.instance.photoUrl)
                .into(object : CustomTarget<Bitmap>(){
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        UserLoggedIn.instance.profilePicture = resource
                        profilePictureView.setImageBitmap(resource)
                    }
                    override fun onLoadCleared(placeholder: Drawable?) {
                        Log.d("Glide", "profile picture onLoadCleared")
                    }
                })
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        return (NavigationUI.navigateUp(navController, mAppBarConfiguration!!)
                || super.onSupportNavigateUp())
    }

    /**
     * AGCUser Logout
     * If provider id is 2 -- initiate facebook login
     */
    fun logoutClick(item: MenuItem?) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Museum App")
        builder.setMessage("Do you really want to log out?")
        builder.setNegativeButton("No", null)
        builder.setPositiveButton("Yes") { _, _ ->
            if (auth!!.currentUser.providerId == 2.toString())
                LoginManager.getInstance().logOut()
            logout(this@HomeActivity)
        }
        builder.show()
    }

    /**
     * On Back Pressed - User is directed to home page of the device
     */
    override fun onBackPressed() {
        when (supportFragmentManager.fragments.first().childFragmentManager.fragments[0]) {
            is SettingsFragment -> {
                val nav =  Navigation.findNavController(this, R.id.nav_host_fragment)
                nav.navigate(R.id.action_nav_settings_to_nav_map)
            }
            is VirtualGuideFragment -> {
                val nav = Navigation.findNavController(this, R.id.nav_host_fragment)
                nav.navigate(R.id.action_nav_explore_to_nav_map)
            }
            is MainFavoriteFragment ->{
                val nav = Navigation.findNavController(this, R.id.nav_host_fragment)
                nav.navigate(R.id.action_nav_favorites_to_nav_map)
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