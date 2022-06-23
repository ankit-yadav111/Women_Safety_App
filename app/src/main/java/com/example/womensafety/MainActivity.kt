package com.example.womensafety

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.example.womensafety.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var speechRecognizer: SpeechRecognizer
    private var count:Int=0
    private lateinit var speechRecognizerIntent: Intent
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        if(ContextCompat.checkSelfPermission
                (this,android.Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED)
        {
            checkPermission()
        }
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

        Log.e("Ankit","Intent")

        binding.imageButton.setOnClickListener {onClick() }

        speechRecognizer.setRecognitionListener(object: RecognitionListener {
            override fun onReadyForSpeech(p0: Bundle?) {}

            override fun onBeginningOfSpeech() {
                Log.e("Ankit","Beg Fun")
            }

            override fun onRmsChanged(p0: Float) {
                Log.e("Ankit","onRms")
            }

            override fun onBufferReceived(p0: ByteArray?) {
                Log.e("Ankit","onBuffer")
            }

            override fun onEndOfSpeech() {
                Log.e("Ankit","onEnd")
            }

            override fun onError(p0: Int) {
                Log.e("Ankit","onError")
                count=0
                onClick()
            }

            override fun onResults(bundle: Bundle?) {
                Log.e("Ankit","Result Fun")
                binding.imageButton.setImageResource(R.drawable.start_recording)
                val data = bundle!!.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                transmitToPython(data!![0])
            }

            override fun onPartialResults(p0: Bundle?) {
                Log.e("Ankit","onPartialResults")
            }

            override fun onEvent(p0: Int, p1: Bundle?) {
                Log.e("Ankit","OnEvent")
            }

        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.layout_menu, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.Contact->{
                val intent = Intent(this, ContactActivity::class.java)
                startActivity(intent)
                true
            }
            else -> {false}
        }
    }


    private fun onClick() {
        Log.e("Ankit","OnCLick Fun")
        if (count == 0) {
            Log.e("Ankit","000000")
            count = 1
            binding.imageButton.setImageResource(R.drawable.start_recording)
            speechRecognizer.startListening(speechRecognizerIntent)
        } else {
            Log.e("Ankit","111111")
            count = 0
            speechRecognizer.stopListening()
        }
    }

    fun transmitToPython(data:String){
        Log.e("Tag","Python")
        if (! Python.isStarted()) {
            Python.start( AndroidPlatform(this))
        }

        val py = Python.getInstance()
        val module = py.getModule("MyProgram")

        val bytes = module.callAttr("main",data)
        Toast.makeText(this,"${bytes.toString()}",Toast.LENGTH_SHORT).show()
        Log.e("Tag","Python Done")
    }

    private fun checkPermission() {

        ActivityCompat.requestPermissions(
            this, arrayOf(android.Manifest.permission.RECORD_AUDIO),
            RecordAudioRequestCode
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode== RecordAudioRequestCode && grantResults.isNotEmpty()){
            Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
        }
    }

    companion object{
        const val RecordAudioRequestCode= 1
    }
}