package com.example.womensafety

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.telephony.SmsManager
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.example.womensafety.contact.ContactViewModel
import com.example.womensafety.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var viewModel:ContactViewModel
    private var recordedData=""
    private lateinit var text:String
    private val onString= "Press Button to Switch ON"
    private val offString = "Press Button to Switch OFF"
    private lateinit var speechRecognizerIntent: Intent
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel= ViewModelProvider(this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application))[ContactViewModel::class.java]



        if((ContextCompat.checkSelfPermission
                (this,Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED) && ContextCompat.checkSelfPermission(this,
            Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED)
        {
            checkPermission()
        }
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS,30000)


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
                transmitToPython(recordedData)
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
                Toast.makeText(this,listSize.toString(),Toast.LENGTH_SHORT).show()
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

    fun transmitToPython(data:String){
        if (! Python.isStarted()) {
            Python.start( AndroidPlatform(this))
        }
        val py = Python.getInstance()
        val module = py.getModule("MyProgram")
        val bytes = module.callAttr("main",data).toString()
        if(bytes=="0"){
            Toast.makeText(this,"Restart the Function",Toast.LENGTH_SHORT).show()
            onClick()
        }
        else{
            Toast.makeText(this,"SuccessFully Complete",Toast.LENGTH_SHORT).show()
            sendMessage()
            binding.imageButton.setImageResource(R.drawable.stop_recording)
        }
    }

    private fun sendMessage(){
        val smsManager=SmsManager.getDefault()
        viewModel.allCont.observe(this,  Observer{list->
            list?.let {
                var i=0
                while(i<list.size){
                    val name=list[i].name
                    val  number= list[i].number
                    smsManager.sendTextMessage(number,null,
                        "Hii $name,I am in trouble.\nPlease Help!",null,null)
                    i++
                }
            }
        })

    }

    private fun checkPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.RECORD_AUDIO,Manifest.permission.SEND_SMS),
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