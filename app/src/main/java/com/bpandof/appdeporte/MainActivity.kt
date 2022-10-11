package com.bpandof.appdeporte

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bpandof.appdeporte.LoginActivity.Companion.providerSession
import com.bpandof.appdeporte.LoginActivity.Companion.useremail
import com.facebook.login.LoginManager
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.bpandof.appdeporte.Objects.setHeightLinearLayout
import com.bpandof.appdeporte.Objects.animateViewofInt
import com.bpandof.appdeporte.Objects.animateViewofFloat

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var drawer: DrawerLayout
    lateinit var swIntervalMode: Switch
    lateinit var swChallenges: Switch

    private var challengeDistance:Float = 0f
    private var challengeDuration:Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initToolBar()
        initObjects()
        initNavigationView()


    }

    override fun onBackPressed() {
        //super.onBackPressed()
        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START)
        else
            signOut()
    }

    private fun initToolBar() {
        var toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_main)
        setSupportActionBar(toolbar)

        drawer = findViewById(R.id.drawer_layout)
        val toggle =
            ActionBarDrawerToggle(
                this,
                drawer,
                toolbar,
                R.string.bar_title,
                R.string.bar_title_close
            )

        drawer.addDrawerListener(toggle)

        toggle.syncState()
    }

    private fun initNavigationView() {
        var navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        var headerView: View =
            LayoutInflater.from(this).inflate(R.layout.nav_header_main, navigationView, false)
        navigationView.removeHeaderView(headerView)
        navigationView.addHeaderView(headerView)

        var tvUser: TextView = headerView.findViewById(R.id.tvUser)
        tvUser.text = useremail
    }

    private fun initObjects() {
        var lyMap = findViewById<LinearLayout>(R.id.lyMap)
        var lyFragmenMap = findViewById<LinearLayout>(R.id.lyFragmentMap)
        var lyIntervalModeSpace = findViewById<LinearLayout>(R.id.lyIntervalModeSpace)
        var lyIntervalMode = findViewById<LinearLayout>(R.id.lyIntervalMode)
        var lyChallengesSpace = findViewById<LinearLayout>(R.id.lyChallengesSpace)
        var lyChallenges = findViewById<LinearLayout>(R.id.lyChallenges)
        var lySettingsVolumesSpace = findViewById<LinearLayout>(R.id.lySettingsVolumesSpace)
        var lySettingsVolumes = findViewById<LinearLayout>(R.id.lySettingsVolumes)


        setHeightLinearLayout(lyMap, 0)
        setHeightLinearLayout(lyIntervalModeSpace, 0)
        setHeightLinearLayout(lyChallengesSpace, 0)
        setHeightLinearLayout(lySettingsVolumesSpace, 0)

        lyFragmenMap.translationY = -300f
        lyIntervalMode.translationY = -300f
        lyChallenges.translationY = -300f
        lySettingsVolumes.translationY = -300f

        swIntervalMode = findViewById(R.id.swIntervalMode)
        swChallenges = findViewById(R.id.swChallenges)

    }


    fun callSignOut(view: View) {
        signOut()
    }

    private fun signOut() {
        useremail = ""

        if (providerSession == "Facebook") LoginManager.getInstance().logOut()
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this, LoginActivity::class.java))
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.nav_item_record -> callRecordActivity()
            R.id.nav_item_signout -> signOut()
        }

        drawer.closeDrawer(GravityCompat.START)

        return true
    }

    private fun callRecordActivity() {
        val intent = Intent(this, RecordActivity::class.java)
        startActivity(intent)
    }

    fun inflateChallenges(v: View) {
        val lyChallengesSpace = findViewById<LinearLayout>(R.id.lyChallengesSpace)
        val lyChallenges = findViewById<LinearLayout>(R.id.lyChallenges)
        if (swChallenges.isChecked) {
            animateViewofInt(
                swChallenges,
                "textColor",
                ContextCompat.getColor(this, R.color.orange),
                500
            )
            setHeightLinearLayout(lyChallengesSpace, 750)
            animateViewofFloat(lyChallenges, "translationY", 0f, 500)
        } else {
            swChallenges.setTextColor(ContextCompat.getColor(this,R.color.white))
            setHeightLinearLayout(lyChallengesSpace, 0)
            lyChallenges.translationY = -300f

            challengeDistance = 0f
            challengeDuration = 0
        }

    }

    fun inflateVolumes(v: View) {
        var swVolumen = findViewById<Switch>(R.id.swVolumes)
        val lySettingsVolumesSpace = findViewById<LinearLayout>(R.id.lySettingsVolumesSpace)
        val lySettingsVolumes = findViewById<LinearLayout>(R.id.lySettingsVolumes)

        if (swVolumen.isChecked) {

            animateViewofInt(
                swVolumen,
                "textColor",
                ContextCompat.getColor(this, R.color.orange),
                500
            )

            var value = 400
            if (swIntervalMode.isChecked) value = 600

            setHeightLinearLayout(lySettingsVolumesSpace, value)
            animateViewofFloat(lySettingsVolumes, "translationY", 0f, 500)

        } else {
            swVolumen.setTextColor(ContextCompat.getColor(this, R.color.white))
            setHeightLinearLayout(lySettingsVolumesSpace, 0)
            lySettingsVolumes.translationY = -300f


        }
    }
}





























