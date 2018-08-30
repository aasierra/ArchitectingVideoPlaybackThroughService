package com.example.anthonysierra.architectingvideoplaybackexample

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    /**
     * Create our connection to the service to be used in our bindService call.
     */
    private val connection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {

        }

        /**
         * Called after a successful bind with our VideoService.
         */
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            //We expect the service binder to be the video services binder.
            //As such we cast.
            if (service is VideoService.VideoServiceBinder) {
                //Then we simply set the exoplayer instance on this view.
                //Notice we are only getting information.
                playerView.player = service.getExoPlayerInstance()
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //Start the service up with video playback information.
        val intent = Intent(this, VideoService::class.java)
        intent.putExtra(VideoService.VIDEO_FILE_ID, R.raw.atoms)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
        stop_player_btn.setOnClickListener {
            startService(Intent(this, VideoService::class.java).apply {
                putExtra(VideoService.PLAY_PAUSE_ACTION, 0)
            })
        }
    }
}
