package com.bpandof.appdeporte

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import com.bpandof.appdeporte.Constants.INTERVAL_LOCATION
import com.bpandof.appdeporte.LoginActivity.Companion.providerSession
import com.bpandof.appdeporte.LoginActivity.Companion.useremail
import com.facebook.login.LoginManager
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.bpandof.appdeporte.Utility.setHeightLinearLayout
import com.bpandof.appdeporte.Utility.animateViewofInt
import com.bpandof.appdeporte.Utility.animateViewofFloat
import com.bpandof.appdeporte.Utility.getFormattedStopWatch
import com.bpandof.appdeporte.Utility.getSecFromWatch
import com.bpandof.appdeporte.Utility.roundNumber
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Runnable
import me.tankery.lib.circularseekbar.CircularSeekBar

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnMyLocationClickListener {
    companion object {
        val REQUIRED_PERMISSIONS_GPS =
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
    }

    private var mHandler: Handler? = null
    private var mInterval = 1000
    private var timeInSeconds = 0L
    private var rounds: Int = 1

    private var startButtonClicked = false

    //VARIABLES BPANDOF
    private var isRunning: Boolean = false
    private var isWalking: Boolean = false

    //

    private var widthScreenPixels: Int = 0
    private var heightScreenPixels: Int = 0
    private var widthAnimations: Int = 0

    private lateinit var drawer: DrawerLayout

    private lateinit var csbChallengeDistance: CircularSeekBar
    private lateinit var csbCurrentDistance: CircularSeekBar
    private lateinit var csbRecordDistance: CircularSeekBar

    private lateinit var csbCurrentAvgSpeed: CircularSeekBar
    private lateinit var csbRecordAvgSpeed: CircularSeekBar

    private lateinit var csbCurrentSpeed: CircularSeekBar
    private lateinit var csbCurrentMaxSpeed: CircularSeekBar
    private lateinit var csbRecordSpeed: CircularSeekBar

    private lateinit var tvDistanceRecord: TextView
    private lateinit var tvAvgSpeedRecord: TextView
    private lateinit var tvMaxSpeedRecord: TextView

    private lateinit var tvChrono: TextView
    private lateinit var fbCamara: FloatingActionButton

    private lateinit var swIntervalMode: Switch
    private lateinit var npDurationInterval: NumberPicker
    private lateinit var tvRunningTime: TextView
    private lateinit var tvWalkingTime: TextView
    private lateinit var csbRunWalk: CircularSeekBar

    private lateinit var swChallenges: Switch

    private lateinit var npChallengeDistance: NumberPicker
    private lateinit var npChallengeDurationHH: NumberPicker
    private lateinit var npChallengeDurationMM: NumberPicker
    private lateinit var npChallengeDurationSS: NumberPicker

    private var challengeDistance: Float = 0f
    private var challengeDuration: Int = 0

    private lateinit var swVolumes: Switch
    private var mpNotify: MediaPlayer? = null
    private var mpHard: MediaPlayer? = null
    private var mpSoft: MediaPlayer? = null
    private lateinit var sbHardVolume: SeekBar
    private lateinit var sbSoftVolume: SeekBar
    private lateinit var sbNotifyVolume: SeekBar

    private lateinit var sbHardTrack: SeekBar
    private lateinit var sbSoftTrack: SeekBar

    private var ROUND_INTERVAL = 300
    private var hardTime: Boolean = true
    private var TIME_RUNNING: Int = 0

    private lateinit var lyPopupRun: LinearLayout

    private lateinit var map: GoogleMap
    private var mapCentered = true

    private val PERMISSION_ID = 42
    private val LOCATION_PERMISSION_REQ_CODE = 1000

    private var activatedGPS: Boolean = true
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var flagSavedLocation = false
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var init_lt: Double = 0.0
    private var init_ln: Double = 0.0

    private var distance: Double = 0.0
    private var maxSpeed: Double = 0.0
    private var avgSpeed: Double = 0.0
    private var speed: Double = 0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initObjects()
        initToolBar()
        initNavigationView()
        initPermissionsGPS()


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

    private fun initStopWatch() {
        tvChrono.text = getString(R.string.init_stop_watch_value)
    }

    private fun initChrono() {
        tvChrono = findViewById(R.id.tvChrono)
        tvChrono.setTextColor(ContextCompat.getColor(this, R.color.white))
        initStopWatch()

        widthScreenPixels = resources.displayMetrics.widthPixels
        heightScreenPixels = resources.displayMetrics.heightPixels
        widthAnimations = widthScreenPixels

        val lyChronoProgressBg = findViewById<LinearLayout>(R.id.lyChronoProgressBg)
        val lyRoundProgressBg = findViewById<LinearLayout>(R.id.lyRoundProgressBg)

        lyChronoProgressBg.translationX = -widthAnimations.toFloat()
        lyRoundProgressBg.translationX = -widthAnimations.toFloat()

        val tvReset: TextView = findViewById(R.id.tvReset)
        tvReset.setOnClickListener { resetClicked() }

        fbCamara = findViewById(R.id.fbCamera)
        fbCamara.isVisible = false

    }

    private fun hideLayouts() {
        var lyMap = findViewById<LinearLayout>(R.id.lyMap)
        val lyFragmentMap = findViewById<LinearLayout>(R.id.lyFragmentMap)
        val lyIntervalModeSpace = findViewById<LinearLayout>(R.id.lyIntervalModeSpace)
        val lyIntervalMode = findViewById<LinearLayout>(R.id.lyIntervalMode)
        val lyChallengesSpace = findViewById<LinearLayout>(R.id.lyChallengesSpace)
        val lyChallenges = findViewById<LinearLayout>(R.id.lyChallenges)
        val lySettingsVolumesSpace = findViewById<LinearLayout>(R.id.lySettingsVolumesSpace)
        val lySettingsVolumes = findViewById<LinearLayout>(R.id.lySettingsVolumes)
        var lySoftTrack = findViewById<LinearLayout>(R.id.lySoftTrack)
        var lySoftVolume = findViewById<LinearLayout>(R.id.lySoftVolume)


        setHeightLinearLayout(lyMap, 0)
        setHeightLinearLayout(lyIntervalModeSpace, 0)
        setHeightLinearLayout(lyChallengesSpace, 0)
        setHeightLinearLayout(lySettingsVolumesSpace, 0)
        setHeightLinearLayout(lySoftTrack, 0)
        setHeightLinearLayout(lySoftVolume, 0)

        lyFragmentMap.translationY = -300f
        lyIntervalMode.translationY = -300f
        lyChallenges.translationY = -300f
        lySettingsVolumes.translationY = -300f
    }

    private fun initMetrics() {
        csbChallengeDistance = findViewById(R.id.csbChallengeDistance)
        csbCurrentDistance = findViewById(R.id.csbCurrentDistance)
        csbRecordDistance = findViewById(R.id.csbRecordDistance)

        csbCurrentAvgSpeed = findViewById(R.id.csbCurrentAvgSpeed)
        csbRecordAvgSpeed = findViewById(R.id.csbRecordAvgSpeed)

        csbCurrentSpeed = findViewById(R.id.csbCurrentSpeed)
        csbCurrentMaxSpeed = findViewById(R.id.csbCurrentMaxSpeed)
        csbRecordSpeed = findViewById(R.id.csbRecordSpeed)

        csbCurrentDistance.progress = 0f
        csbChallengeDistance.progress = 0f
        csbCurrentAvgSpeed.progress = 0f
        csbCurrentSpeed.progress = 0f
        csbCurrentMaxSpeed.progress = 0f

        tvDistanceRecord = findViewById(R.id.tvDistanceRecord)
        tvAvgSpeedRecord = findViewById(R.id.tvAvgSpeedRecord)
        tvMaxSpeedRecord = findViewById(R.id.tvMaxSpeedRecord)

        tvDistanceRecord.text = ""
        tvAvgSpeedRecord.text = ""
        tvMaxSpeedRecord.text = ""
    }

    private fun initSwitchs() {
        swIntervalMode = findViewById(R.id.swIntervalMode)
        swChallenges = findViewById(R.id.swChallenges)
        swVolumes = findViewById(R.id.swVolumes)
    }

    private fun initIntervalMode() {
        npDurationInterval = findViewById(R.id.npDurationInterval)
        tvRunningTime = findViewById(R.id.tvRunningTime)
        tvWalkingTime = findViewById(R.id.tvWalkingTime)
        csbRunWalk = findViewById(R.id.csbRunWalk)

        npDurationInterval.minValue = 1
        npDurationInterval.maxValue = 60
        npDurationInterval.value = 5
        npDurationInterval.wrapSelectorWheel = true
        npDurationInterval.setFormatter(NumberPicker.Formatter { i -> String.format("%02d", i) })

        npDurationInterval.setOnValueChangedListener { picker, oldVal, newVal ->
            csbRunWalk.max = (newVal * 60).toFloat()
            csbRunWalk.progress = csbRunWalk.max / 2


            tvRunningTime.text =
                getFormattedStopWatch(((newVal * 60 / 2) * 1000).toLong()).subSequence(3, 8)
            tvWalkingTime.text = tvRunningTime.text

            ROUND_INTERVAL = newVal * 60
            TIME_RUNNING = ROUND_INTERVAL / 2
        }

        csbRunWalk.max = 300f
        csbRunWalk.progress = 150f
        csbRunWalk.setOnSeekBarChangeListener(object :
            CircularSeekBar.OnCircularSeekBarChangeListener {

            override fun onProgressChanged(
                circularSeekBar: CircularSeekBar?,
                progress: Float,
                fromUser: Boolean
            ) {
                if (fromUser) {
                    var STEPS_UX: Int = 15
                    if (ROUND_INTERVAL > 600) STEPS_UX = 60
                    if (ROUND_INTERVAL > 1800) STEPS_UX = 300
                    var set: Int = 0
                    var p = progress.toInt()

                    var limit = 60
                    if (ROUND_INTERVAL > 1800) limit = 300

                    if (p % STEPS_UX != 0 && progress != csbRunWalk.max) {
                        while (p >= limit) p -= limit
                        while (p >= STEPS_UX) p -= STEPS_UX
                        if (STEPS_UX - p > STEPS_UX / 2) set = -1 * p
                        else set = STEPS_UX - p

                        if (csbRunWalk.progress + set > csbRunWalk.max)
                            csbRunWalk.progress = csbRunWalk.max
                        else
                            csbRunWalk.progress = csbRunWalk.progress + set
                    }
                    if (csbRunWalk.progress == 0f) manageEnableButtonsRun(false, false)
                    else manageEnableButtonsRun(false, true)
                }

                tvRunningTime.text =
                    getFormattedStopWatch((csbRunWalk.progress.toInt() * 1000).toLong()).subSequence(
                        3,
                        8
                    )
                tvWalkingTime.text =
                    getFormattedStopWatch(((ROUND_INTERVAL - csbRunWalk.progress.toInt()) * 1000).toLong()).subSequence(
                        3,
                        8
                    )
                TIME_RUNNING = getSecFromWatch(tvRunningTime.text.toString())
            }


            override fun onStopTrackingTouch(seekBar: CircularSeekBar?) {
            }


            override fun onStartTrackingTouch(seekBar: CircularSeekBar?) {
            }
        })
    }

    private fun initChallengeMode() {
        npChallengeDistance = findViewById(R.id.npChallengeDistance)
        npChallengeDurationHH = findViewById(R.id.npChallengeDurationHH)
        npChallengeDurationMM = findViewById(R.id.npChallengeDurationMM)
        npChallengeDurationSS = findViewById(R.id.npChallengeDurationSS)

        npChallengeDistance.minValue = 1
        npChallengeDistance.maxValue = 300
        npChallengeDistance.value = 10
        npChallengeDistance.wrapSelectorWheel = true

        npChallengeDistance.setOnValueChangedListener { picker, oldVal, newVal ->
            challengeDistance = newVal.toFloat()
            csbChallengeDistance.max = newVal.toFloat()
            csbChallengeDistance.progress = newVal.toFloat()
            challengeDuration = 0

            if (csbChallengeDistance.max > csbRecordDistance.max)
                csbCurrentDistance.max = csbChallengeDistance.max
        }

        npChallengeDurationHH.minValue = 0
        npChallengeDurationHH.maxValue = 23
        npChallengeDurationHH.value = 1
        npChallengeDurationHH.wrapSelectorWheel = true
        npChallengeDurationHH.setFormatter(NumberPicker.Formatter { i -> String.format("%02d", i) })

        npChallengeDurationMM.minValue = 0
        npChallengeDurationMM.maxValue = 59
        npChallengeDurationMM.value = 0
        npChallengeDurationMM.wrapSelectorWheel = true
        npChallengeDurationMM.setFormatter(NumberPicker.Formatter { i -> String.format("%02d", i) })

        npChallengeDurationSS.minValue = 0
        npChallengeDurationSS.maxValue = 59
        npChallengeDurationSS.value = 0
        npChallengeDurationSS.wrapSelectorWheel = true
        npChallengeDurationSS.setFormatter(NumberPicker.Formatter { i -> String.format("%02d", i) })

        npChallengeDurationHH.setOnValueChangedListener { picker, oldVal, newVal ->
            getChallengeDuration(newVal, npChallengeDurationMM.value, npChallengeDurationSS.value)
        }

        npChallengeDurationMM.setOnValueChangedListener { picker, oldVal, newVal ->
            getChallengeDuration(npChallengeDurationHH.value, newVal, npChallengeDurationSS.value)
        }

        npChallengeDurationSS.setOnValueChangedListener { picker, oldVal, newVal ->
            getChallengeDuration(npChallengeDurationHH.value, npChallengeDurationMM.value, newVal)
        }


    }

    private fun setVolumes() {
        sbHardVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, i: Int, p2: Boolean) {
                mpHard?.setVolume(i / 100.0f, i / 100.0f)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        sbSoftVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, i: Int, p2: Boolean) {
                mpSoft?.setVolume(i / 100.0f, i / 100.0f)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        sbNotifyVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, i: Int, p2: Boolean) {
                mpNotify?.setVolume(i / 100.0f, i / 100.0f)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun updateTimesTrack(timesH: Boolean, timesS: Boolean) {

        if (timesH) {
            val tvHardPosition = findViewById<TextView>(R.id.tvHardPosition)
            val tvHardRemaining = findViewById<TextView>(R.id.tvHardRemaining)
            tvHardPosition.text = getFormattedStopWatch(mpHard!!.currentPosition.toLong())
            tvHardRemaining.text =
                "-" + getFormattedStopWatch(mpHard!!.duration.toLong() - sbHardTrack.progress.toLong())
        }
        if (timesS) {
            val tvSoftPosition = findViewById<TextView>(R.id.tvSoftPosition)
            val tvSoftRemaining = findViewById<TextView>(R.id.tvSoftRemaining)
            tvSoftPosition.text = getFormattedStopWatch(mpSoft!!.currentPosition.toLong())
            tvSoftRemaining.text =
                "-" + getFormattedStopWatch(mpSoft!!.duration.toLong() - sbSoftTrack.progress.toLong())
        }
    }

    private fun setProgressTracks() {
        //val sbHardTrack: SeekBar = findViewById(R.id.sbHardTrack)
        //val sbSoftTrack: SeekBar = findViewById(R.id.sbSoftTrack)
        sbHardTrack.max = mpHard!!.duration
        sbSoftTrack.max = mpSoft!!.duration
        sbHardTrack.isEnabled = false
        sbSoftTrack.isEnabled = false
        updateTimesTrack(true, true)

        sbHardTrack.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, i: Int, fromUser: Boolean) {
                if (fromUser) {
                    mpHard?.pause()
                    mpHard?.seekTo(i)
                    if (isRunning) {
                        mpHard?.start()
                        if (!(timeInSeconds > 0L && hardTime && startButtonClicked)) mpHard?.pause()
                        updateTimesTrack(true, false)
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

        })

        sbSoftTrack.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, i: Int, fromUser: Boolean) {
                if (fromUser) {
                    mpSoft?.pause()
                    mpSoft?.seekTo(i)
                    if (isWalking) {
                        mpSoft?.start()
                        if (!(timeInSeconds > 0L && !hardTime && startButtonClicked)) mpSoft?.pause()
                        updateTimesTrack(false, true)
                    }

                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

        })

    }

    private fun initMusic() {
        mpNotify = MediaPlayer.create(this, R.raw.micmic)
        mpHard = MediaPlayer.create(this, R.raw.hard_music)
        mpSoft = MediaPlayer.create(this, R.raw.soft_music)

        mpHard?.isLooping = true
        mpSoft?.isLooping = true

        sbHardVolume = findViewById(R.id.sbHardVolume)
        sbSoftVolume = findViewById(R.id.sbSoftVolume)
        sbNotifyVolume = findViewById(R.id.sbNotifyVolume)

        sbHardTrack = findViewById(R.id.sbHardTrack)
        sbSoftTrack = findViewById(R.id.sbSoftTrack)
        setVolumes()

        setProgressTracks()
    }

    private fun notifySound() {
        mpNotify?.start()
    }

    private fun hidePopUpRun() {
        var lyWindow = findViewById<LinearLayout>(R.id.lyWindow)
        lyWindow.translationX = 400f
        lyPopupRun = findViewById<LinearLayout>(R.id.lyPopupRun)
        lyPopupRun.isVisible = false

    }

    private fun initObjects() {
        initChrono()
        hideLayouts()
        initMetrics()
        initSwitchs()
        initIntervalMode()
        initChallengeMode()
        initMusic()
        hidePopUpRun()

        initMap()

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

    private fun inflateIntervalMode() {
        val lyIntervalMode = findViewById<LinearLayout>(R.id.lyIntervalMode)
        val lyIntervalModeSpace = findViewById<LinearLayout>(R.id.lyIntervalModeSpace)
        var lySoftTrack = findViewById<LinearLayout>(R.id.lySoftTrack)
        var lySoftVolume = findViewById<LinearLayout>(R.id.lySoftVolume)
        var tvRounds = findViewById<TextView>(R.id.tvRounds)

        if (swIntervalMode.isChecked) {
            animateViewofInt(
                swIntervalMode,
                "textColor",
                ContextCompat.getColor(this, R.color.orange), 500
            )
            setHeightLinearLayout(lyIntervalModeSpace, 600)
            animateViewofFloat(lyIntervalMode, "translationY", 0f, 500)
            animateViewofFloat(tvChrono, "translationX", -110f, 500)
            tvRounds.setText(R.string.rounds)
            animateViewofInt(
                tvRounds,
                "textColor",
                ContextCompat.getColor(this, R.color.white),
                500
            )

            setHeightLinearLayout(lySoftTrack, 120)
            setHeightLinearLayout(lySoftVolume, 200)
            if (swVolumes.isChecked) {
                var lySettingsVolumesSpace = findViewById<LinearLayout>(R.id.lySettingsVolumesSpace)
                setHeightLinearLayout(lySettingsVolumesSpace, 600)
            }
            tvRunningTime = findViewById<TextView>(R.id.tvRunningTime)
            TIME_RUNNING = getSecFromWatch(tvRunningTime.text.toString())


        } else {
            swIntervalMode.setTextColor(ContextCompat.getColor(this, R.color.white))
            setHeightLinearLayout(lyIntervalModeSpace, 0)
            lyIntervalMode.translationY = -200f
            animateViewofFloat(tvChrono, "translationX", 0f, 500)
            tvRounds.text = ""
            setHeightLinearLayout(lySoftTrack, 0)
            setHeightLinearLayout(lySoftVolume, 0)
            if (swVolumes.isChecked) {
                var lySettingsVolumesSpace = findViewById<LinearLayout>(R.id.lySettingsVolumesSpace)
                setHeightLinearLayout(lySettingsVolumesSpace, 400)
            }


        }
    }

    fun callInflateIntervalMode(v: View) {
        inflateIntervalMode()
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
            swChallenges.setTextColor(ContextCompat.getColor(this, R.color.white))
            setHeightLinearLayout(lyChallengesSpace, 0)
            lyChallenges.translationY = -300f

            challengeDistance = 0f
            challengeDuration = 0
        }

    }

    fun showDuration(v: View) {
        if (timeInSeconds.toInt() == 0) showChallege("duration")
    }

    fun showDistance(v: View) {
        if (timeInSeconds.toInt() == 0) showChallege("distance")
    }

    fun showChallege(option: String) {
        var lyChallengeDuration = findViewById<LinearLayout>(R.id.lyChallengeDuration)
        var lyChallengeDistance = findViewById<LinearLayout>(R.id.lyChallengeDistance)
        var tvChallengeDuration = findViewById<TextView>(R.id.tvChallengeDuration)
        var tvChallengeDistance = findViewById<TextView>(R.id.tvChallengeDistance)

        when (option) {
            "duration" -> {
                lyChallengeDuration.translationZ = 5f
                lyChallengeDistance.translationZ = 0f

                tvChallengeDuration.setTextColor(ContextCompat.getColor(this, R.color.orange))
                tvChallengeDuration.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        R.color.gray_dark
                    )
                )

                tvChallengeDistance.setTextColor(ContextCompat.getColor(this, R.color.white))
                tvChallengeDistance.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        R.color.gray_medium
                    )
                )

                challengeDistance = 0f

                getChallengeDuration(
                    npChallengeDurationHH.value,
                    npChallengeDurationMM.value,
                    npChallengeDurationSS.value
                )
            }
            "distance" -> {
                lyChallengeDuration.translationZ = 0f
                lyChallengeDistance.translationZ = 5f

                tvChallengeDuration.setTextColor(ContextCompat.getColor(this, R.color.white))
                tvChallengeDuration.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        R.color.gray_medium
                    )
                )

                tvChallengeDistance.setTextColor(ContextCompat.getColor(this, R.color.orange))
                tvChallengeDistance.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        R.color.gray_dark
                    )
                )

                challengeDuration = 0
                challengeDistance = npChallengeDistance.value.toFloat()
            }

        }
    }

    private fun getChallengeDuration(hh: Int, mm: Int, ss: Int) {
        var hours: String = hh.toString()
        if (hh < 10) hours = "0" + hours
        var minutes = mm.toString()
        if (mm < 10) minutes = "0" + minutes
        var seconds: String = ss.toString()
        if (ss < 10) seconds = "0" + seconds

        //correccion challengeDuration = getSecFromWatch("$hours:$minutes:$seconds")
        challengeDuration = getSecFromWatch("${hours}:${minutes}:${seconds}")

    }

    fun inflateVolumes(v: View) {
        val lySettingsVolumesSpace = findViewById<LinearLayout>(R.id.lySettingsVolumesSpace)
        val lySettingsVolumes = findViewById<LinearLayout>(R.id.lySettingsVolumes)

        if (swVolumes.isChecked) {

            animateViewofInt(
                swVolumes,
                "textColor",
                ContextCompat.getColor(this, R.color.orange),
                500
            )

            var value = 400
            if (swIntervalMode.isChecked) value = 600

            setHeightLinearLayout(lySettingsVolumesSpace, value)
            animateViewofFloat(lySettingsVolumes, "translationY", 0f, 500)

        } else {
            swVolumes.setTextColor(ContextCompat.getColor(this, R.color.white))
            setHeightLinearLayout(lySettingsVolumesSpace, 0)
            lySettingsVolumes.translationY = -300f


        }
    }

    private fun initMap() {
        createMapFragment()
        var lyOpenerButton = findViewById<LinearLayout>(R.id.lyOpenerButton)
        if (allPermissionsGrantedGPS()) lyOpenerButton.isEnabled = true
        else lyOpenerButton.isEnabled = false
    }

    override fun onMyLocationButtonClick(): Boolean {
        return false
    }

    override fun onMyLocationClick(p0: Location) {

    }


    private fun createMapFragment() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentMap) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.mapType = GoogleMap.MAP_TYPE_HYBRID
        enableMyLocation()
        map.setOnMyLocationButtonClickListener(this)
        map.setOnMyLocationClickListener(this)
        map.setOnMapLongClickListener { mapCentered = false }
        map.setOnMapClickListener { mapCentered = false }
        manageLocation()
        centerMap(init_lt, init_ln)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQ_CODE -> {
                var lyOpenerButton = findViewById<LinearLayout>(R.id.lyOpenerButton)

                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    lyOpenerButton.isEnabled = true
                else {
                    var lyMap = findViewById<LinearLayout>(R.id.lyMap)
                    if (lyMap.height > 0) {
                        setHeightLinearLayout(lyMap, 0)

                        var lyFragmentMap = findViewById<LinearLayout>(R.id.lyFragmentMap)
                        lyFragmentMap.translationY = -300f

                        var ivOpenClose = findViewById<ImageView>(R.id.ivOpenClose)
                        ivOpenClose.rotation = 0f
                    }

                    lyOpenerButton.isEnabled = true


                }

            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (!::map.isInitialized) return

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissionLocation()
            return
        }
            else map.isMyLocationEnabled = true

    }

    private fun centerMap(lt: Double, ln: Double){
        val posMap = LatLng(lt, ln)
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(posMap, 16f), 1000, null)

    }

    fun changeTypeMap(v: View){
        var ivTypeMap = findViewById<ImageView>(R.id.ivTypeMap)
        if (map.mapType == GoogleMap.MAP_TYPE_HYBRID){
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            ivTypeMap.setImageResource(R.drawable.map_type_hybrid)
        }
        else{
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            ivTypeMap.setImageResource(R.drawable.map_type_normal)
        }
    }
    fun callCenterMap(v: View){
        mapCentered = true
        if (latitude == 0.0) centerMap(init_lt, init_ln)
        else centerMap(latitude, longitude)
    }



    fun callShowHideMap(v: View) {
        if (allPermissionsGrantedGPS()) {
            var lyMap = findViewById<LinearLayout>(R.id.lyMap)
            var lyFragmentMap = findViewById<LinearLayout>(R.id.lyFragmentMap)
            var ivOpenClose = findViewById<ImageView>(R.id.ivOpenClose)

            if (lyMap.height == 0) {
                setHeightLinearLayout(lyMap, 1157)
                animateViewofFloat(lyFragmentMap, "translationY", 0f, 0)
                ivOpenClose.rotation = 180f
            } else {
                setHeightLinearLayout(lyMap, 0)
                animateViewofFloat(lyFragmentMap, "translationY", -300f, 0)
                ivOpenClose.rotation = 0f
            }

        } else
            requestPermissionLocation()
    }

    private fun initPermissionsGPS() {
        if (allPermissionsGrantedGPS())
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        else
            requestPermissionLocation()
    }

    private fun requestPermissionLocation() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ), PERMISSION_ID
        )
    }

    private fun allPermissionsGrantedGPS() = REQUIRED_PERMISSIONS_GPS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

    }

    private fun activationLocation() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(intent)
    }

    private fun checkPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) ==
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) ==
                PackageManager.PERMISSION_GRANTED
    }


    private fun manageLocation() {
        if (checkPermission()) {

            if (isLocationEnabled()) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) ==
                    PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) ==
                    PackageManager.PERMISSION_GRANTED
                ) {


                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        requestNewLocationData()
                    }
                }
            } else activationLocation()
        } else requestPermissionLocation()
    }


    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        var mLocationRequest = com.google.android.gms.location.LocationRequest()
        mLocationRequest.priority = PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        fusedLocationClient.requestLocationUpdates(
            mLocationRequest,
            mLocationCallBack,
            Looper.myLooper()
        )


    }

    private val mLocationCallBack = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            var mLastLocation: Location? = locationResult.lastLocation
            init_lt = mLastLocation!!.latitude
            init_ln = mLastLocation.longitude
            if (timeInSeconds > 0L) registerNewLocation(mLastLocation)
        }
    }

    private fun registerNewLocation(location: Location) {
        var new_latitude: Double = location.latitude
        var new_longitude: Double = location.longitude

        if (flagSavedLocation) {
            if (timeInSeconds >= INTERVAL_LOCATION) {
                var distanceInterval = calculateDistance(new_latitude, new_longitude)

                updateSpeeds(distanceInterval)
                refreshInterfaceData()
            }
        }
        latitude = new_latitude
        longitude = new_longitude

        if (mapCentered == true) centerMap(latitude, longitude)
    }

    private fun calculateDistance(n_lt: Double, n_lg: Double): Double {
        val radioTierra = 6371.0 //en kilómetros

        val dLat = Math.toRadians(n_lt - latitude)
        val dLng = Math.toRadians(n_lg - longitude)
        val sindLat = Math.sin(dLat / 2)
        val sindLng = Math.sin(dLng / 2)
        val va1 =
            Math.pow(sindLat, 2.0) + (Math.pow(sindLng, 2.0)
                    * Math.cos(Math.toRadians(latitude)) * Math.cos(
                Math.toRadians(n_lt)
            ))
        val va2 = 2 * Math.atan2(Math.sqrt(va1), Math.sqrt(1 - va1))
        var n_distance = radioTierra * va2

        //if (n_distance < LIMIT_DISTANCE_ACCEPTED) distance += n_distance

        distance += n_distance
        return n_distance
    }

    private fun updateSpeeds(d: Double) {
        speed = ((d * 1000) / INTERVAL_LOCATION) * 3.6
        if (speed > maxSpeed) maxSpeed = speed
        avgSpeed = ((distance * 1000) / timeInSeconds) * 3.6
    }

    private fun refreshInterfaceData() {
        var tvCurrentDistance = findViewById<TextView>(R.id.tvCurrentDistance)
        var tvCurrentAvgSpeed = findViewById<TextView>(R.id.tvCurrentAvgSpeed)
        var tvCurrentSpeed = findViewById<TextView>(R.id.tvCurrentSpeed)
        tvCurrentDistance.text = roundNumber(distance.toString(), 2)
        tvCurrentAvgSpeed.text = roundNumber(avgSpeed.toString(), 1)
        tvCurrentSpeed.text = roundNumber(speed.toString(), 1)


        csbCurrentDistance.progress = distance.toFloat()

        csbCurrentAvgSpeed.progress = avgSpeed.toFloat()

        csbCurrentSpeed.progress = speed.toFloat()

        if (speed == maxSpeed) {
            csbCurrentMaxSpeed.max = csbRecordSpeed.max
            csbCurrentMaxSpeed.progress = speed.toFloat()
            csbCurrentSpeed.max = csbRecordSpeed.max
        }
    }

    fun startOrStopButtonClicked(v: View) {
        manageStartStop()
    }

    private fun manageStartStop() {
        if (timeInSeconds == 0L && isLocationEnabled() == false) {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.alertActivationGPSTitle))
                .setMessage(getString(R.string.alertActivationGPSDescription))
                .setPositiveButton(getString(R.string.aceptActivationGPS),
                    DialogInterface.OnClickListener { dialog, wich ->
                        activationLocation()
                    })
                .setNegativeButton(R.string.ignoreActivationGPS,
                    DialogInterface.OnClickListener { dialog, wich ->
                        activatedGPS = false
                        manageRun()
                    })

                .setCancelable(true)
                .show()
        } else
            manageRun()
    }

    fun manageRun() {


        if (timeInSeconds.toInt() == 0) {
            fbCamara.isVisible = true

            swIntervalMode.isClickable = false
            npDurationInterval.isEnabled = false
            csbRunWalk.isEnabled = false

            swChallenges.isClickable = false
            npChallengeDistance.isEnabled = false
            npChallengeDurationHH.isEnabled = false
            npChallengeDurationMM.isEnabled = false
            npChallengeDurationSS.isEnabled = false

            tvChrono.setTextColor(ContextCompat.getColor(this, R.color.chrono_running))
            //correccion
            sbHardTrack.isEnabled = true
            sbSoftTrack.isEnabled = true
            //correccion


            mpHard?.start()
            isRunning = true
            isWalking = false

            if (activatedGPS) {
                flagSavedLocation = false
                manageLocation()
                flagSavedLocation = true
                manageLocation()

            }


        }
        if (!startButtonClicked) {
            startButtonClicked = true
            startTime()
            manageEnableButtonsRun(false, true)

            if (isRunning) {
                mpHard?.start()
            }
            if (isWalking) {
                mpSoft?.start()
            }

        } else {
            startButtonClicked = false
            stopTime()
            manageEnableButtonsRun(true, true)

            if (isRunning) {
                mpHard?.pause()
            }
            if (isWalking) {
                mpSoft?.pause()
            }


        }
    }

    private fun manageEnableButtonsRun(e_reset: Boolean, e_run: Boolean) {
        val tvReset = findViewById<TextView>(R.id.tvReset)
        val btStart = findViewById<LinearLayout>(R.id.btStart)
        val btStartLabel = findViewById<TextView>(R.id.btStartLabel)
        tvReset.setEnabled(e_reset)
        btStart.setEnabled(e_run)

        if (e_reset) {
            tvReset.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
            animateViewofFloat(tvReset, "translationY", 0f, 500)
        } else {
            tvReset.setBackgroundColor(ContextCompat.getColor(this, R.color.gray))
            animateViewofFloat(tvReset, "translationY", 150f, 500)
        }

        if (e_run) {
            if (startButtonClicked) {
                btStart.background = getDrawable(R.drawable.circle_background_topause)
                btStartLabel.setText(R.string.stop)
            } else {
                btStart.background = getDrawable(R.drawable.circle_background_toplay)
                btStartLabel.setText(R.string.start)
            }
        } else {
            btStart.background = getDrawable(R.drawable.circle_background_todisable)
        }


    }

    private fun startTime() {
        mHandler = Handler(Looper.getMainLooper())
        chronometer.run()
    }

    private fun stopTime() {
        mHandler?.removeCallbacks(chronometer)
    }

    private var chronometer: Runnable = object : Runnable {
        override fun run() {
            try {
                if (mpHard!!.isPlaying) {
                    val sbHardTrack: SeekBar = findViewById(R.id.sbHardTrack)
                    sbHardTrack.progress = mpHard!!.currentPosition
                }
                if (mpSoft!!.isPlaying) {
                    val sbSoftTrack: SeekBar = findViewById(R.id.sbSoftTrack)
                    sbSoftTrack.progress = mpSoft!!.currentPosition
                }

                updateTimesTrack(true, true)

                if (activatedGPS && timeInSeconds.toInt() % INTERVAL_LOCATION == 0) manageLocation()

                if (swIntervalMode.isChecked) {
                    checkStopRun(timeInSeconds)
                    checkNewRound(timeInSeconds)
                }


                timeInSeconds += 1
                updateStopWatchView()
            } finally {
                mHandler!!.postDelayed(this, mInterval.toLong())
            }
        }
    }

    private fun updateStopWatchView() {
        tvChrono.text = getFormattedStopWatch(timeInSeconds * 1000)
    }

    private fun resetClicked() {
        resetVariablesRun()
        resetTimeView()
        resetInterface()
    }

    private fun resetVariablesRun() {
        timeInSeconds = 0
        rounds = 1

        isRunning = false
        isWalking = false

        challengeDistance = 0f
        challengeDuration = 0

        activatedGPS = true
        flagSavedLocation = false

        initStopWatch()
    }

    private fun resetTimeView() {

        manageEnableButtonsRun(false, true)
        //val btStart: LinearLayout = findViewById(R.id.btStart)
        tvChrono.setTextColor(ContextCompat.getColor(this, R.color.white))
    }

    private fun resetInterface() {

        fbCamara.isVisible = false

        val tvCurrentDistance = findViewById<TextView>(R.id.tvCurrentDistance)
        val tvCurrentAvgSpeed = findViewById<TextView>(R.id.tvCurrentAvgSpeed)
        val tvCurrentSpeed = findViewById<TextView>(R.id.tvCurrentSpeed)

        tvCurrentDistance.text = "0.0"
        tvCurrentAvgSpeed.text = "0.0"
        tvCurrentSpeed.text = "0.0"

        tvDistanceRecord.setTextColor(ContextCompat.getColor(this, R.color.gray_dark))
        tvAvgSpeedRecord.setTextColor(ContextCompat.getColor(this, R.color.gray_dark))
        tvMaxSpeedRecord.setTextColor(ContextCompat.getColor(this, R.color.gray_dark))


        csbCurrentDistance.progress = 0f
        csbCurrentAvgSpeed.progress = 0f
        csbCurrentSpeed.progress = 0f
        csbCurrentMaxSpeed.progress = 0f

        //correccion
        val tvRounds: TextView = findViewById(R.id.tvRounds) as TextView
        tvRounds.text = getString(R.string.rounds)
        //correccion

        val lyChronoProgressBg = findViewById<LinearLayout>(R.id.lyChronoProgressBg)
        val lyRoundProgressBg = findViewById<LinearLayout>(R.id.lyRoundProgressBg)
        lyChronoProgressBg.translationX = -widthAnimations.toFloat()
        lyRoundProgressBg.translationX = -widthAnimations.toFloat()


        swIntervalMode.isClickable = true
        npDurationInterval.isEnabled = true
        csbRunWalk.isEnabled = true

        swChallenges.isClickable = true
        npChallengeDistance.isEnabled = true
        npChallengeDurationHH.isEnabled = true
        npChallengeDurationMM.isEnabled = true
        npChallengeDurationSS.isEnabled = true

        //val sbHardTrack: SeekBar = findViewById(R.id.sbHardTrack)
        //val sbSoftTrack: SeekBar = findViewById(R.id.sbSoftTrack)
        sbHardTrack.isEnabled = false
        sbSoftTrack.isEnabled = false


    }

    private fun updateProgressBarRound(secs: Long) {

        var s = secs.toInt()
        while (s >= ROUND_INTERVAL) s -= ROUND_INTERVAL
        s++

        var lyRoundProgressBg = findViewById<LinearLayout>(R.id.lyRoundProgressBg)
        if (tvChrono.getCurrentTextColor() == ContextCompat.getColor(
                this,
                R.color.chrono_running
            )
        ) {

            var movement = -1 * (widthAnimations - (s * widthAnimations / TIME_RUNNING)).toFloat()
            animateViewofFloat(lyRoundProgressBg, "translationX", movement, 1000L)
            isRunning = true
            isWalking = false
        }
        if (tvChrono.getCurrentTextColor() == ContextCompat.getColor(
                this,
                R.color.chrono_walking
            )
        ) {
            s -= TIME_RUNNING
            var movement =
                -1 * (widthAnimations - (s * widthAnimations / (ROUND_INTERVAL - TIME_RUNNING))).toFloat()
            animateViewofFloat(lyRoundProgressBg, "translationX", movement, 1000L)
            isRunning = false
            isWalking = true

        }
    }

    private fun checkStopRun(Secs: Long) {
        var secAux: Long = Secs
        while (secAux.toInt() > ROUND_INTERVAL) secAux -= ROUND_INTERVAL

        if (secAux.toInt() == TIME_RUNNING) {
            tvChrono.setTextColor(ContextCompat.getColor(this, R.color.chrono_walking))

            var lyRoundProgressBg = findViewById<LinearLayout>(R.id.lyRoundProgressBg)
            lyRoundProgressBg.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    R.color.chrono_walking
                )
            )
            lyRoundProgressBg.translationX = -widthAnimations.toFloat()

            //isRunning = false
            mpHard?.pause()
            notifySound()
            //isWalking = true


            mpSoft?.start()


        } else
            updateProgressBarRound(Secs)
    }


    private fun checkNewRound(Secs: Long) {
        if (Secs.toInt() % ROUND_INTERVAL == 0 && Secs.toInt() > 0) {
            val tvRounds: TextView = findViewById(R.id.tvRounds)
            rounds++
            tvRounds.text = "Round $rounds"

            tvChrono.setTextColor(ContextCompat.getColor(this, R.color.chrono_running))
            val lyRoundProgressBg = findViewById<LinearLayout>(R.id.lyRoundProgressBg)
            lyRoundProgressBg.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    R.color.chrono_running
                )
            )
            lyRoundProgressBg.translationX = -widthAnimations.toFloat()
            //isWalking = false
            mpSoft?.pause()
            notifySound()
            //isRunning = true
            mpHard?.start()
        } else
            updateProgressBarRound(Secs)

    }


}



