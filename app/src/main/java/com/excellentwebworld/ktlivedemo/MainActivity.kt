package com.excellentwebworld.ktlivedemo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.agora.rtc2.Constants


class MainActivity : AppCompatActivity() {

    var channelProfile = 0
    companion object {
        val channelMessage = "com.agora.samtan.agorabroadcast.CHANNEL"
        val profileMessage = "com.agora.samtan.agorabroadcast.PROFILE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val MY_PERMISSIONS_REQUEST_CAMERA = 0
        // Here, this is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO),
                MY_PERMISSIONS_REQUEST_CAMERA)
        }
    }

    fun onRadioButtonClicked(view: View) {
        val checked = (view as RadioButton).isChecked
        when (view.getId()) {
            R.id.host -> if (checked) {
                channelProfile = Constants.CLIENT_ROLE_BROADCASTER
            }
            R.id.audience -> if (checked) {
                channelProfile = Constants.CLIENT_ROLE_AUDIENCE
            }
        }
    }

    fun onSubmit(view: View?) {
        val channel = findViewById<View>(R.id.channel) as EditText
        val channelName = channel.text.toString()
        val intent = Intent(this, VideoActivity::class.java)
        intent.putExtra(channelMessage, channelName)
        intent.putExtra(profileMessage, channelProfile)
        startActivity(intent)
    }
}