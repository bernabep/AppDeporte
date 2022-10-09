package com.bpandof.appdeporte

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.bpandof.appdeporte.LoginActivity.Companion.providerSession
import com.bpandof.appdeporte.LoginActivity.Companion.useremail
import com.facebook.login.LoginManager
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity(),NavigationView.OnNavigationItemSelectedListener {

    lateinit var drawer: DrawerLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initToolBar()
        initNavigationView()

    }

    private fun initToolBar() {
        var toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_main)
        setSupportActionBar(toolbar)

        drawer = findViewById(R.id.drawer_layout)
        val toggle =
            ActionBarDrawerToggle(this,drawer,toolbar,R.string.bar_title,R.string.bar_title_close)

        drawer.addDrawerListener(toggle)

        toggle.syncState()
    }

    private fun initNavigationView(){
        var navigationView:NavigationView= findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        var headerView: View = LayoutInflater.from(this).inflate(R.layout.nav_header_main,navigationView,false)
        navigationView.removeHeaderView(headerView)
        navigationView.addHeaderView(headerView)

        var tvUser:TextView = headerView.findViewById(R.id.tvUser)
        tvUser.text = useremail
    }


    fun callSignOut(view: View) {
        signOff()
    }

    private fun signOff() {
        useremail = ""

        if (providerSession == "Facebook") LoginManager.getInstance().logOut()
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this, LoginActivity::class.java))
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        TODO("Not yet implemented")

        //return true
    }
}