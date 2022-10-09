package com.bpandof.appdeporte

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.content.ContextCompat

class RecordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_record)
        setSupportActionBar(toolbar)

        toolbar.title = getString(R.string.bar_title_record)
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.gray_dark))
        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.gray_light))

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.order_records_by, menu)
        return true //super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.orderby_date -> {
                if (item.title == getString(R.string.orderby_dateZA)) {
                    item.title = getString(R.string.orderby_dateAZ)
                } else {
                    item.title = getString(R.string.orderby_dateZA)
                }
                return true
            }
            R.id.orderby_duration -> {
                if (item.title == getString(R.string.orderby_durationZA)) {
                    item.title = getString(R.string.orderby_durationAZ)
                } else {
                    item.title = getString(R.string.orderby_durationZA)
                }
                return true
            }
            R.id.orderby_distance -> {
                if (item.title == getString(R.string.orderby_distanceZA)) {
                    item.title = getString(R.string.orderby_distanceAZ)
                } else {
                    item.title = getString(R.string.orderby_distanceZA)
                }
                return true
            }
            R.id.orderby_avgspeed -> {
                if (item.title == getString(R.string.orderby_avgspeedZA)) {
                    item.title = getString(R.string.orderby_avgspeedAZ)
                } else {
                    item.title = getString(R.string.orderby_avgspeedZA)
                }
                return true
            }
            R.id.orderby_maxspeed -> {
                if (item.title == getString(R.string.orderby_maxspeedZA)) {
                    item.title = getString(R.string.orderby_maxspeedAZ)
                } else {
                    item.title = getString(R.string.orderby_maxspeedZA)
                }
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

}
























