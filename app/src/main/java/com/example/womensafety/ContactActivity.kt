package com.example.womensafety

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.womensafety.contact.ContactAdapter
import com.example.womensafety.contact.ContactViewModel
import com.example.womensafety.contact.contact
import com.example.womensafety.databinding.ActivityContactBinding

class ContactActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContactBinding
    private lateinit var viewModel :ContactViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityContactBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.title="Add Contact"


        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter =ContactAdapter(this,this)

        binding.recyclerView.adapter=adapter

        viewModel= ViewModelProvider(this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application))[ContactViewModel::class.java]

        viewModel.allCont.observe(this, Observer {list->
            list?.let{
                adapter.updateList(it)
            }
        })

        binding.submitButton.setOnClickListener{onClickSubmit()}
    }

    private fun onClickSubmit(){
        val name = binding.nameField.text.toString().trim()
        val number = binding.numberField.text.toString().trim()
        if(name=="" || number=="" ){
            if(name==""){
                binding.nameField.requestFocus()
                binding.nameField.error = "FIELD CANNOT BE EMPTY"}
            if(number==""){
                binding.numberField.requestFocus()
                binding.numberField.error = "FIELD CANNOT BE EMPTY"}
        }
        else if(number.length!=10){
            binding.numberField.requestFocus()
            binding.numberField.error = "Wrong Input"
        }
        else{
            insertData(name,number)
        }
    }

    fun onItemClicked(cont: contact) {
        viewModel.deleteCont(cont)
    }


    private fun insertData(name: String, number: String) {
        viewModel.insertCont(contact(name,number))
    }

}