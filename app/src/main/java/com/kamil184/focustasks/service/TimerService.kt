package com.kamil184.focustasks.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.os.Binder
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat.*
import com.kamil184.focustasks.MainActivity
import com.kamil184.focustasks.R
import com.kamil184.focustasks.data.manager.TimerManager
import com.kamil184.focustasks.data.model.NonLiveDataTimer
import com.kamil184.focustasks.data.model.TimerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TimerService : Service() {

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_CLOSE = "ACTION_CLOSE"

        const val timerNotificationId = 1
        const val timerRunningNotificationChannelId = "timer Running"
        const val timerExpiredNotificationChannelId = "timer Expired"
    }

    private lateinit var notificationManager: NotificationManager
    private lateinit var timerRunningBuilder: Builder
    private lateinit var timerExpiredBuilder: Builder

    private var timerManager: TimerManager? = null
    private lateinit var timer: NonLiveDataTimer
    private var countDownTimer: CountDownTimer? = null

    private lateinit var startPendingIntent: PendingIntent
    private lateinit var pausePendingIntent: PendingIntent
    private lateinit var stopPendingIntent: PendingIntent
    private lateinit var closePendingIntent: PendingIntent

    override fun onCreate() {
        super.onCreate()
        timerManager = TimerManager(this)
        CoroutineScope(Dispatchers.IO).launch {
            timerManager!!.nonLiveDataTimerFlow.collect {
                timer = it
            }
        }

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createNotificationChannels()
        initActionsPendingIntents()
        initBuilders()

        //только в случае, если вызов будет с ACTION_START
        updateActions(timerRunningBuilder, ACTION_PAUSE, ACTION_STOP, ACTION_CLOSE)
        updateNotification(timerRunningBuilder)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action != null) {
            countDownTimer?.cancel()
            when (intent.action) {
                ACTION_START -> {
                    timer.state = TimerState.Running
                    updateActions(timerRunningBuilder, ACTION_PAUSE, ACTION_STOP, ACTION_CLOSE)

                    countDownTimer =
                        object : CountDownTimer(timer.timeRemaining.toLong(), 1000) {
                            override fun onFinish() {
                                timer.state = TimerState.Stopped
                                timer.timeRemaining = timer.length
                                updateActions(timerExpiredBuilder, ACTION_START, ACTION_CLOSE)
                                updateNotification(timerExpiredBuilder)
                            }

                            override fun onTick(millisUntilFinished: Long) {
                                timer.timeRemaining = millisUntilFinished.toInt()
                                updateNotification(timerRunningBuilder)
                            }
                        }.start()
                }

                ACTION_PAUSE -> {
                    timer.state = TimerState.Paused
                    updateActions(timerRunningBuilder, ACTION_START, ACTION_STOP, ACTION_CLOSE)
                    updateNotification(timerRunningBuilder)
                }
                ACTION_STOP -> {
                    timer.state = TimerState.Stopped
                    timer.timeRemaining = timer.length
                    updateActions(timerRunningBuilder, ACTION_START, ACTION_CLOSE)
                    updateNotification(timerRunningBuilder)
                }
                ACTION_CLOSE -> {
                    timer.state = TimerState.Paused
                    stopSelf()
                }
            }
        }
        startForeground(timerNotificationId, timerRunningBuilder.build())
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        countDownTimer?.cancel()
        notificationManager.cancel(timerNotificationId)
        CoroutineScope(Dispatchers.IO).launch {
            timerManager?.saveTimer(timer)
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder {
        return Binder()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannels() {
        val timerRunningChannel = NotificationChannel(timerRunningNotificationChannelId, "Timer",
            NotificationManager.IMPORTANCE_LOW)
        timerRunningChannel.enableVibration(false)
        timerRunningChannel.description = "Show timer running in a notification"
        notificationManager.createNotificationChannel(timerRunningChannel)

        val timerExpiredChannel =
            NotificationChannel(timerExpiredNotificationChannelId, "Timer expired",
                NotificationManager.IMPORTANCE_HIGH)
        timerExpiredChannel.enableVibration(true)

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()
        timerExpiredChannel.setSound(Settings.System.DEFAULT_NOTIFICATION_URI, audioAttributes) //TODO another sound

        notificationManager.createNotificationChannel(timerExpiredChannel)
    }

    private fun initActionsPendingIntents() {
        val startIntent = Intent(this, TimerService::class.java)
        startIntent.action = ACTION_START
        val flags =
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        startPendingIntent = PendingIntent.getService(this,
            0,
            startIntent,
            flags)

        val pauseIntent = Intent(this, TimerService::class.java)
        pauseIntent.action = ACTION_PAUSE
        pausePendingIntent = PendingIntent.getService(this,
            0,
            pauseIntent,
            flags)

        val stopIntent = Intent(this, TimerService::class.java)
        stopIntent.action = ACTION_STOP
        stopPendingIntent = PendingIntent.getService(this,
            0,
            stopIntent,
            flags)

        val closeIntent = Intent(this, TimerService::class.java)
        closeIntent.action = ACTION_CLOSE
        closePendingIntent = PendingIntent.getService(this,
            0,
            closeIntent,
            flags)
    }

    private fun updateActions(builder: Builder, vararg actions: String) {
        builder.clearActions()
        actions.forEach {
            when (it) {
                ACTION_START -> builder.addAction(R.drawable.ic_play_arrow_24,
                    getString(R.string.start),
                    startPendingIntent)
                ACTION_PAUSE -> builder.addAction(R.drawable.ic_pause_24,
                    getString(R.string.pause),
                    pausePendingIntent)
                ACTION_STOP -> builder.addAction(R.drawable.ic_stop_24,
                    getString(R.string.stop),
                    stopPendingIntent)
                ACTION_CLOSE -> builder.addAction(R.drawable.ic_close_24,
                    getString(R.string.close),
                    closePendingIntent)
            }
        }
    }

    private fun updateNotification(builder: Builder) {
        builder.setContentText(timer.getTimeRemainingText())

        if (timer.state != TimerState.Stopped)
            builder.setProgress(timer.length, timer.length - timer.timeRemaining, false)
        else builder.setProgress(0, 0, false)

        when (timer.state) {
            TimerState.Stopped -> builder.setContentTitle(getString(R.string.timer_ready_to_start))
            TimerState.Paused -> builder.setContentTitle(getString(R.string.timer_paused))
            TimerState.Running -> builder.setContentTitle(getString(R.string.timer_running))
        }

        notificationManager.notify(timerNotificationId, builder.build())
    }

    private fun initBuilders() {
        val activityIntent = Intent(this, MainActivity::class.java)
        val flags =
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        val activityPendingIntent = PendingIntent.getActivity(this, 0,
            activityIntent, flags)

        timerRunningBuilder = Builder(this, timerRunningNotificationChannelId)
            .setSmallIcon(R.drawable.ic_timer_48)
            .setContentIntent(activityPendingIntent)
            .setPriority(PRIORITY_LOW)
            .setAutoCancel(false)
            .setShowWhen(false)
            .setCategory(CATEGORY_PROGRESS)

        timerExpiredBuilder = Builder(this, timerExpiredNotificationChannelId)
            .setSmallIcon(R.drawable.ic_timer_48)
            .setContentIntent(activityPendingIntent)
            .setPriority(PRIORITY_MAX)
            .setSound(Settings.System.DEFAULT_NOTIFICATION_URI) //TODO another sound
            .setAutoCancel(false)
            .setShowWhen(false)
            .setCategory(CATEGORY_REMINDER)
            .setFullScreenIntent(activityPendingIntent, true)

    }
}