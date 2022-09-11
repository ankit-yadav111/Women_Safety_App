package com.example.womensafety

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.telephony.SmsManager
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.womensafety.contact.ContactViewModel
import com.example.womensafety.databinding.ActivityMainBinding
import java.io.IOException
import java.util.*


@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var viewModel:ContactViewModel
    private var recordedData=""
    private lateinit var text:String
    private val onString= "Press Button to Switch ON"
    private val offString = "Press Button to Switch OFF"
    private lateinit var speechRecognizerIntent: Intent
    private lateinit var binding: ActivityMainBinding
    var locationManager: LocationManager? = null
    var latitude = 0.0
    var longitude = 0.0
    var address = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel= ViewModelProvider(this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application))[ContactViewModel::class.java]

        if((ContextCompat.checkSelfPermission
                (this,Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(this,
            Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED))
        {
            checkPermission()
        }
        locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager
        locationManager!!.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            0, 10f, locationListenerGPS
        )
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS,10000)


        binding.imageButton.setOnClickListener {fetchSize()}

       speechRecognizer.setRecognitionListener(object: RecognitionListener {
            override fun onReadyForSpeech(p0: Bundle?) {}

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(p0: Float) {}

            override fun onBufferReceived(p0: ByteArray?) {}

            override fun onEndOfSpeech(){}

            override fun onError(p0: Int){
                binding.status.text=offString
                onClick()
            }

            override fun onResults(bundle: Bundle?) {
                binding.imageButton.setImageResource(R.drawable.start_recording)
                val data = bundle!!.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if(data!=null){
                    recordedData+= data[0]
                }
                checkSpeech(recordedData)
            }
            override fun onPartialResults(p0: Bundle?) {
                val data= p0!!.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (data != null) {
                    if (data.isNotEmpty()){
                        recordedData+=data[0]
                    }
                }
            }

            override fun onEvent(p0: Int, p1: Bundle?) {}
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

    private fun fetchSize(){
        viewModel.allCont.observe(this,  Observer{list->
            list?.let {
                checkContact(list.size)
            }
        })
    }

    private fun checkContact(listSize:Int){
        text=binding.status.text.toString()
        if(text==onString){
            if(listSize>0){
                onClick()
            }
            else {
                Toast.makeText(this,"Add Contact",Toast.LENGTH_LONG).show()
            }
        }
        else{
            onClick()
        }
    }


    private fun onClick() {
        if (text == onString) {
            binding.status.text=offString
            binding.imageButton.setImageResource(R.drawable.start_recording)
            speechRecognizer.startListening(speechRecognizerIntent)
        } else {
            binding.status.text=onString
            binding.imageButton.setImageResource(R.drawable.stop_recording)
            speechRecognizer.stopListening()
        }
    }

    fun checkSpeech(data:String){
        var flag=0
        val wordList = listOf("help","bacho","leave","bachao","live","leaf")
        val inputList = data.split(" ")
        for(item in wordList.distinct()) {
            if(Collections.frequency(inputList,item)>=3){
                flag= 1
                break
            }
        }
        if(flag==0){
            Toast.makeText(this,"Restart the Function",Toast.LENGTH_SHORT).show()
            onClick()
        }
        else{
            Toast.makeText(this,"SuccessFully Complete",Toast.LENGTH_SHORT).show()
            sendMessage()
            binding.status.text=onString
            binding.imageButton.setImageResource(R.drawable.stop_recording)
        }
    }

    private fun sendMessage(){
        val smsManager=SmsManager.getDefault()
        val strUri = "http://maps.google.com/maps?q=loc:$latitude,$longitude (Help!)"
        viewModel.allCont.observe(this,  Observer{list->
            list?.let {
                var i=0
                while(i<list.size){
                    val name=list[i].name
                    val  number= list[i].number
                    val msg = "Hii $name,I am in trouble.\nPlease Help!\nMap Link: $strUri \n"
                    smsManager.sendTextMessage(number,null,
                       msg,null,null)
                    i++
                }
            }
        })

    }

    private fun checkPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.RECORD_AUDIO,Manifest.permission.SEND_SMS,Manifest.permission.ACCESS_FINE_LOCATION),
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

    private var locationListenerGPS: LocationListener = object : LocationListener {

        override fun onLocationChanged(location: Location) {
            latitude = location.latitude
            longitude = location.longitude
            locationManager?.removeUpdates(this)
        }

        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }
}