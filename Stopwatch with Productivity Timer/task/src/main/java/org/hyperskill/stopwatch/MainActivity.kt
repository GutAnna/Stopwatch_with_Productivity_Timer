package org.hyperskill.stopwatch

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
//import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random


class MainActivity : AppCompatActivity() {
    lateinit var textView: TextView
    lateinit var progressBar: ProgressBar

    private var mSec = 0L
    private val handler = Handler(Looper.getMainLooper())
    private var limit: Long? = null

    private val channelId = "org.hyperskill"
    private val channelName = "Notification"
    private val channelDescriptionText = "Timer"
    lateinit var mChannel: NotificationChannel
    lateinit var notificationManager: NotificationManager
    lateinit var builder: Notification.Builder

    private val runTimer: Runnable = object : Runnable {
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun run() {
            mSec += 1000
            val color = Color.rgb(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))
            textView.text = String.format("%1\$tM:%1\$tS", mSec)
            progressBar.indeterminateTintList = ColorStateList.valueOf(color);
            if (limit != null) if (mSec > limit!! * 1000) { textView.setTextColor(Color.RED); sendNotify()}
            handler.postDelayed(this, 1000)
        }
    }

    private fun startTimer() {
        if (mSec == 0L) {
            handler.postDelayed(runTimer, 1000)
        }
    }

    private fun resetTimer() {
        handler.removeCallbacks(runTimer)
        mSec = 0
        textView.text = getString(R.string.startTime)
        textView.setTextColor(Color.GRAY)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView = findViewById(R.id.textView)
        progressBar = findViewById(R.id.progressBar)
        val settingsButton = findViewById<Button>(R.id.settingsButton)
        settingsButton.setOnClickListener {
            val contentView = LayoutInflater.from(this).inflate(R.layout.dialog_main, null, false)

            AlertDialog.Builder(this)
                .setTitle("Set upper limit in seconds")
                .setView(contentView)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    val edittext = contentView.findViewById<EditText>(R.id.upperLimitEditText)
                    limit = edittext.text.toString().toLong()
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }

        findViewById<Button>(R.id.startButton).setOnClickListener {
            startTimer()
            progressBar.visibility = View.VISIBLE
            settingsButton.isEnabled = false
        }
        findViewById<Button>(R.id.resetButton).setOnClickListener {
            resetTimer()
            progressBar.visibility = View.INVISIBLE
            settingsButton.isEnabled = true
        }
    }
    fun sendNotify() {
        val intent = Intent(this, LauncherActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mChannel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
                description = channelDescriptionText
                setShowBadge(false)
            }

            mChannel.lightColor = Color.BLUE
            mChannel.enableVibration(true)

            notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
            builder = Notification.Builder(this, channelId)
                .setContentTitle("Notification")
                .setContentText("Time exceeded")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)
        }
        notificationManager.notify(393939, builder.build())
    }
}

