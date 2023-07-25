package com.excellentwebworld.ktlivedemo

import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.video.VideoCanvas
import io.agora.rtc2.video.VideoEncoderConfiguration


class VideoActivity : AppCompatActivity() {
    private var mRtcEngine: RtcEngine? = null
    private var channelName: String? = null
    private var channelProfile = 0
    val LOGIN_MESSAGE = "com.agora.samtan.agorabroadcast.CHANNEL_LOGIN"
    private val mRtcEventHandler: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {
        override fun onFirstRemoteVideoDecoded(uid: Int, width: Int, height: Int, elapsed: Int) {
            runOnUiThread { setupRemoteVideo(uid) }
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            runOnUiThread { onRemoteUserLeft() }
        }

        override fun onUserMuteVideo(uid: Int, muted: Boolean) {
            runOnUiThread { onRemoteUserVideoMuted(uid, muted) }
        }
    }

    fun onLocalAudioMuteClicked(view: View) {
        val iv: ImageView = view as ImageView
        if (iv.isSelected) {
            iv.isSelected = false
            iv.clearColorFilter()
        } else {
            iv.isSelected = true
            iv.setColorFilter(resources.getColor(R.color.purple_200), PorterDuff.Mode.MULTIPLY)
        }
        mRtcEngine!!.muteLocalAudioStream(iv.isSelected)
    }

    private fun onRemoteUserVideoMuted(uid: Int, muted: Boolean) {
        try {
            val container = findViewById<View>(R.id.remote_video_view_container) as FrameLayout
            val surfaceView = container.getChildAt(0) as SurfaceView
            val tag = surfaceView.tag
            if (tag != null && tag as Int == uid) {
                surfaceView.visibility = if (muted) View.GONE else View.VISIBLE
            }
        } catch (e: Exception) {
            Log.e("TAG", "onRemoteUserVideoMuted: ${e.printStackTrace()}" )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)
        val intent = intent
        channelName = intent.getStringExtra(MainActivity.channelMessage)
        channelProfile = intent.getIntExtra(MainActivity.profileMessage, -1)
        if (channelProfile == -1) {
            Log.e("TAG: ", "No profile")
        }
        initAgoraEngineAndJoinChannel()
    }

    fun onLocalVideoMuteClicked(view: View) {
        val iv: ImageView = view as ImageView
        if (iv.isSelected()) {
            iv.isSelected = false
            iv.clearColorFilter()
        } else {
            iv.setSelected(true)
            iv.setColorFilter(resources.getColor(R.color.purple_200), PorterDuff.Mode.MULTIPLY)
        }
        mRtcEngine!!.muteLocalVideoStream(iv.isSelected())
        val container = findViewById<View>(R.id.local_video_view_container) as FrameLayout
        val surfaceView = container.getChildAt(0) as SurfaceView
        surfaceView.setZOrderMediaOverlay(!iv.isSelected())
        surfaceView.setVisibility(if (iv.isSelected()) View.GONE else View.VISIBLE)
    }

    private fun setupRemoteVideo(uid: Int) {
        val container = findViewById<View>(R.id.remote_video_view_container) as FrameLayout
        //        if (container.getChildCount() > 1) {
//            return;
//        }
        val surfaceView = RtcEngine.CreateRendererView(baseContext)
        container.addView(surfaceView)
        mRtcEngine!!.setupRemoteVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, uid))
    }

    private fun onRemoteUserLeft() {
        val container = findViewById<View>(R.id.remote_video_view_container) as FrameLayout
        container.removeAllViews()
    }

    private fun initAgoraEngineAndJoinChannel() {
        initalizeAgoraEngine()
        mRtcEngine!!.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING)
        mRtcEngine!!.setClientRole(channelProfile)
        setupVideoProfile()
        setupLocalVideo()
        joinChannel()
    }

    private fun initalizeAgoraEngine() {
        try {
            mRtcEngine =
                RtcEngine.create(baseContext, getString(R.string.private_app_id), mRtcEventHandler)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupVideoProfile() {
        mRtcEngine!!.enableVideo()
        mRtcEngine!!.setVideoEncoderConfiguration(VideoEncoderConfiguration(
            VideoEncoderConfiguration.VD_640x480,
            VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
            VideoEncoderConfiguration.STANDARD_BITRATE,
            VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT))
    }

    private fun setupLocalVideo() {
        val container = findViewById<View>(R.id.local_video_view_container) as FrameLayout
        val surfaceView = RtcEngine.CreateRendererView(baseContext)
        surfaceView.setZOrderMediaOverlay(true)
        container.addView(surfaceView)
        mRtcEngine!!.setupLocalVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, 0))
    }

    private fun joinChannel() {
        mRtcEngine!!.joinChannel(null, channelName, "Optional Data", 0)
    }

    private fun leaveChannel() {
        mRtcEngine!!.leaveChannel()
    }

    override fun onDestroy() {
        super.onDestroy()
        leaveChannel()
        RtcEngine.destroy()
        mRtcEngine = null
    }

    fun onSwitchCameraClicked(view: View?) {
        mRtcEngine!!.switchCamera()
    }

    fun onEndCallClicked(view: View?) {
        finish()
    }
}