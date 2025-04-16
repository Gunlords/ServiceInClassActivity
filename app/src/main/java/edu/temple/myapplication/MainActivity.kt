package edu.temple.myapplication

import android.content.*
import android.os.*
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import edu.temple.myapplication.R

class MainActivity : AppCompatActivity() {

    private var timerService: TimerService.TimerBinder? = null
    private var isBound = false

    companion object {
        const val PREFS_NAME = "TimerPrefs"
        const val KEY_SAVED_TIME = "saved_time"
        const val DEFAULT_TIME = 100
    }

    private val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            findViewById<TextView>(R.id.textView).text = msg.what.toString()
        }
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            timerService = service as TimerService.TimerBinder
            isBound = true
            timerService?.setHandler(handler)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
            timerService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intent = Intent(this, TimerService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)

        findViewById<Button>(R.id.startButton).setOnClickListener {
            handleStartButtonClick()
        }

        findViewById<Button>(R.id.stopButton).setOnClickListener {
            handleStopButtonClick()
        }
    }

    private fun handleStartButtonClick() {
        val sharedPref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val savedTime = sharedPref.getInt(KEY_SAVED_TIME, DEFAULT_TIME)

        if (isBound) {
            if (!timerService!!.isRunning) {
                timerService?.start(savedTime)
            } else {
                timerService?.pause()

                val currentTimeText = findViewById<TextView>(R.id.textView).text.toString()
                with(sharedPref.edit()) {
                    putInt(KEY_SAVED_TIME, currentTimeText.toIntOrNull() ?: DEFAULT_TIME)
                    apply()
                }
            }
        }
    }

    private fun handleStopButtonClick() {
        if (isBound) {
            timerService?.stop()

            val sharedPref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            with(sharedPref.edit()) {
                remove(KEY_SAVED_TIME)
                apply()
            }

            findViewById<TextView>(R.id.textView).text = DEFAULT_TIME.toString()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        unbindService(connection)

        val sharedPref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        if (timerService?.paused == false) {
            with(sharedPref.edit()) {
                remove(KEY_SAVED_TIME)
                apply()
            }
        }
    }
}
