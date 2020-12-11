package com.example.museumapplication.ui.home

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
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
import com.example.museumapplication.R
import com.example.museumapplication.data.UserLoggedIn
import com.example.museumapplication.utils.auth.IBaseAuth.Companion.logout
import com.example.museumapplication.utils.settings.SettingsUtils
import com.facebook.login.LoginManager
import com.google.android.material.navigation.NavigationView
import com.huawei.agconnect.auth.AGConnectAuth
import de.hdodenhof.circleimageview.CircleImageView
import java.net.URL

class HomeActivity : AppCompatActivity() {
    private var mAppBarConfiguration: AppBarConfiguration? = null
    var auth: AGConnectAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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


    fun setNavViewHeader(nav: NavigationView) {
        val email = nav.getHeaderView(0).findViewById<TextView>(R.id.emailTextView)
        val name = nav.getHeaderView(0).findViewById<TextView>(R.id.nameTextView)
        name.text = UserLoggedIn.instance.name
        email.text = UserLoggedIn.instance.email
        DownloadImageTask(nav.getHeaderView(0).findViewById(R.id.profilePictureView))
                .execute(UserLoggedIn.instance.photoUrl)
    }

    @SuppressLint("StaticFieldLeak")
    private inner class DownloadImageTask(var bmImage: CircleImageView) : AsyncTask<String?, Void?, Bitmap?>() {
        override fun doInBackground(vararg params: String?): Bitmap? {
            val urlDisplay = params[0]
            var mIcon11: Bitmap? = null
            try {
                val `in` = URL(urlDisplay).openStream()
                mIcon11 = BitmapFactory.decodeStream(`in`)
            } catch (e: Exception) {
                Log.e("Error", e.message!!)
                e.printStackTrace()
            }
            return mIcon11
        }

        override fun onPostExecute(result: Bitmap?) {
            bmImage.setImageBitmap(result)
            UserLoggedIn.instance.profilePicture = result
        }
    }



    override fun onSupportNavigateUp(): Boolean {
        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        return (NavigationUI.navigateUp(navController, mAppBarConfiguration!!)
                || super.onSupportNavigateUp())
    }

    fun logoutClick(item: MenuItem?) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Museum App")
        builder.setMessage("Do you really want to log out?")
        builder.setNegativeButton("No", null)
        builder.setPositiveButton("Yes") { _, _ ->
            if (auth!!.currentUser.providerId == 2.toString()) LoginManager.getInstance().logOut()
            logout(this@HomeActivity)
        }
        builder.show()
    }

    override fun onBackPressed() {
        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_HOME)
        startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(startMain)
    }

    override fun onDestroy() {
        super.onDestroy()

    }


}