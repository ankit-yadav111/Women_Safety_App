package com.example.womensafety.contact

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.contact.R

class ContactAdapter (private val context: Context ) : RecyclerView.Adapter<ContactAdapter.NoteViewHolder>(){

    val allcont = ArrayList<contact>()

    inner class NoteViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val textView = itemView.findViewById<TextView>(R.id.text)
        val deleteButton = itemView.findViewById<ImageView>(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val viewHolder = NoteViewHolder(LayoutInflater.from(context).inflate(R.layout.contact_item_list,parent, false))
//        viewHolder.deleteButton.setOnClickListener{
//            listener.onItemClicked(allcont[viewHolder.adapterPosition])
//        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val currentNote =allcont[position]
        holder.textView.text = currentNote.text
    }

    override fun getItemCount(): Int {
        return allcont.size
    }

    fun updateList(newlist: List<contact>){
        allcont.clear()
        allcont.addAll(newlist)

        notifyDataSetChanged()
    }

}

interface IContact{
    fun onItemClicked(note: contact)
}