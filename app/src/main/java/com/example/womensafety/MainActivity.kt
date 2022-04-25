package com.example.womensafety

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.example.womensafety.databinding.ActivityMainBinding
import java.io.IOException


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var output: String=""
    private var mediaRecorder: MediaRecorder?=null
    private lateinit var countDowntimer:CountDownTimer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        output="${externalCacheDir?.absolutePath}/Audio.mp3"
        mediaRecorder=MediaRecorder()

        binding.imageButton.setOnClickListener{CurrentStatus()}
    }


    fun start(){
        Log.e("Tag","Start!")
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            val permissions = arrayOf(android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE)
            ActivityCompat.requestPermissions(this, permissions,0)
        } else {
            mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            mediaRecorder?.setOutputFile(output)
            startRecording()
        }
    }

    fun CurrentStatus(){
        Log.e("Tag","CurrentStatus!")
        if(binding.status.text=="Press Button to Switch ON"){
            binding.status.text="Press Button to Switch OFF"
            start()
        }
        else{
            stop()
        }
    }

    fun startRecording(){
        Log.e("Tag","Start Recording!")
        try {
            mediaRecorder?.prepare()
            mediaRecorder?.start()
            Toast.makeText(this, "Recording started!", Toast.LENGTH_SHORT).show()
            timer()
        } catch (e: IOException) {
            Log.e("Tag","Prepare Failed!")
        }
    }

    fun stopRecording(){
        Log.e("Tag","Stop Recording")
        mediaRecorder?.stop()
        mediaRecorder?.release()
        mediaRecorder=null
        Toast.makeText(this,"Recording Stopped", Toast.LENGTH_SHORT).show()
        Log.e("Tag","Stopped")
    }

    fun transmitTOpython(){
        Log.e("Tag","Python")
        if (! Python.isStarted()) {
            Python.start( AndroidPlatform(this))
        }

        val py = Python.getInstance()
        val module = py.getModule("MyProgram")

        val bytes = module.callAttr("main",output)
        Toast.makeText(this,"${bytes.toString()}",Toast.LENGTH_SHORT).show()
        Log.e("Tag","Python Done")
        start()
    }

    fun stop(){
        binding.status.text="Press Button to Switch ON"
        countDowntimer?.cancel()
        stopRecording()
    }

    fun timer(){
        Log.e("Tag","Timer!")
        countDowntimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                stopRecording()
                transmitTOpython()
            }
        }.start()
    }
}