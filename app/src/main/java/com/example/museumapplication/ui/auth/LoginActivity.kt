package com.example.museumapplication.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.museumapplication.R
import com.example.museumapplication.ui.museumpanel.MuseumPanelActivity
import com.example.museumapplication.utils.auth.*
import com.example.museumapplication.utils.auth.AuthUtils.checkGoogleServices
import com.example.museumapplication.utils.auth.AuthUtils.disableAllItems
import com.example.museumapplication.utils.services.CloudDBManager
import com.facebook.login.widget.LoginButton

class LoginActivity : AppCompatActivity() {
    var email: TextView? = null
    var password: TextView? = null
    var auth: IBaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val googleButton = findViewById<ImageButton>(R.id.googleButton)
        if (!checkGoogleServices(this)) {
            googleButton.isEnabled = false
            googleButton.visibility = View.GONE
        }
        email = findViewById(R.id.editEmail)
        password = findViewById(R.id.editPassword)
    }

    fun registerButtonClicked(v: View?) {
        val register = Intent(this, SignupActivity::class.java)
        startActivity(register)
    }

    fun signInButtonClicked(view: View?) {
        auth = EmailAuth(email!!.text.toString(), password!!.text.toString(), this)
        auth!!.login()
    }

    fun googleButtonClicked(view: View?) {
        auth = GoogleAuth(this)
        auth!!.login()
    }

    fun huaweiButtonClicked(view: View?) {
        auth = HuaweiAuth(this)
        auth!!.login()
    }

    fun facebookButtonClicked(view: View?) {
        auth = FacebookAuth(this)
        val facebookButton = findViewById<LoginButton>(R.id.facebookButton)
        facebookButton.performClick()
        auth!!.login()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        disableAllItems(findViewById(R.id.linearLayout))
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 9001) {
            (auth as GoogleAuth?)!!.activityResult(data, this)
        }
        if (requestCode == 8888) {
            (auth as HuaweiAuth?)!!.activityResult(data)
        }
        if (requestCode == 64206) {
            (auth as FacebookAuth?)!!.activityResult(requestCode, resultCode, data)
        }
    }

    override fun onBackPressed() {
        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_HOME)
        startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(startMain)
    }

    fun museumPanelClick(view: View) {
        val museum =CloudDBManager.instance.checkMuseumIdAndPassword(email!!.text.toString(), password!!.text.toString())
        if(museum!=null) {
            val museumPanelActivity = Intent(this, MuseumPanelActivity::class.java)
            val extra = Bundle()
            extra.putString("museumID", museum.museumID)
            museumPanelActivity.putExtras(extra)
            startActivity(museumPanelActivity)
        }
        else
        {
            Toast.makeText(this, "Museum password or ID is wrong!", Toast.LENGTH_SHORT).show()
        }
    }
}