package edu.temple.myapplication


import android.content.*
import android.os.*
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private var timerService: TimerService.TimerBinder? = null
    private var isBound = false

    //handler
    private val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            findViewById<TextView>(R.id.textView).text = msg.what.toString()
        }
    }

    private val connection = object : ServiceConnection {
       //pass the handler to service connection
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
        if (isBound) {
            if (!timerService!!.isRunning) {
                timerService?.start(10)
            } else {
                timerService?.pause()
            }
        }
    }

    private fun handleStopButtonClick() {
        if (isBound) {
            timerService?.stop()
        }
    }
}