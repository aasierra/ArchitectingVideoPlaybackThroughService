package com.example.anthonysierra.architectingvideoplaybackexample

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.widget.RemoteViews
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.extractor.mp4.Mp4Extractor
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.LoopingMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.RawResourceDataSource

class VideoService : Service() {

    companion object {
        const val VIDEO_FILE_ID = "VideoFileID"
        const val PLAY_PAUSE_ACTION = "playPauseAction"
        const val NOTIFICAITON_ID = 0
    }

    private lateinit var exoPlayer: SimpleExoPlayer

    /**
     * Will be called by our activity to get information about exo player.
     */
    override fun onBind(intent: Intent?): IBinder {
        intent?.let {
            exoPlayer.playWhenReady = true//Tell exoplayer to start as soon as it's content is loaded.
            loadExampleMedia(intent.getIntExtra(VIDEO_FILE_ID, 0))
            displayNotification()
        }
        return VideoServiceBinder()
    }


    override fun onCreate() {
        super.onCreate()
        val trackSelection = AdaptiveTrackSelection.Factory(DefaultBandwidthMeter())
        val trackSelector = DefaultTrackSelector(trackSelection)
        exoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            val action = it.getIntExtra(PLAY_PAUSE_ACTION, -1)
            when (action) {
                0 -> exoPlayer.playWhenReady = false
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    /**
     * This class will be what is returned when an activity binds to this service.
     * The activity will also use this to know what it can get from our service to know
     * about the video playback.
     */
    inner class VideoServiceBinder : Binder() {

        /**
         * This method should be used only for setting the exoplayer instance.
         * If exoplayer's internal are altered or accessed we can not guarantee
         * things will work correctly.
         */
        fun getExoPlayerInstance() = exoPlayer
    }

    /**
     * When called will load into exo player our sample playback video.
     */
    private fun loadExampleMedia(resourceFile:Int) {
        var rawUri = RawResourceDataSource.buildRawResourceUri(resourceFile)
        val dataSource = RawResourceDataSource(this)
        dataSource.open(DataSpec(rawUri))
        val source = ExtractorMediaSource(rawUri, DataSource.Factory { dataSource }, Mp4Extractor.FACTORY, null, null)
        exoPlayer.prepare(LoopingMediaSource(source, 10))
    }

    private fun displayNotification() {
        //Lets create our remote view.
        val remoteView = RemoteViews(packageName, R.layout.video_notification)

        //Next create a pending intent and make it stop our video playback.
        val intent = PendingIntent.getService(this, 0, Intent(this, VideoService::class.java).apply {
            putExtra(PLAY_PAUSE_ACTION, 0)
        }, 0)
        remoteView.setOnClickPendingIntent(R.id.stop_player_btn, intent)

        //Now for showing through the notification manager.
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationBuilder = NotificationCompat.Builder(this, "Default")
        notificationBuilder.setContent(remoteView)
        notificationBuilder.setSmallIcon(android.R.drawable.sym_def_app_icon)

        //Check for version and create a channel if needed.
        if (Build.VERSION.SDK_INT > 26) {
            manager.createNotificationChannel(NotificationChannel("ID", "Main", NotificationManager.IMPORTANCE_DEFAULT))
            notificationBuilder.setChannelId("ID")
        }
        val notification = notificationBuilder.build()
        startForeground(0, notification)
        manager.notify(NOTIFICAITON_ID, notification)
    }
}