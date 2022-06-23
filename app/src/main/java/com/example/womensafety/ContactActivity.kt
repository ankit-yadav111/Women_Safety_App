package com.example.womensafety

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.womensafety.databinding.ActivityContactBinding

class ContactActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContactBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityContactBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.submitButton.setOnClickListener{}
    }

}