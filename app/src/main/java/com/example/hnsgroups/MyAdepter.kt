package com.example.hnsgroups

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


open class MyAdepter(private val userList : ArrayList<User>) : RecyclerView.Adapter<MyAdepter.MyViewHolder> () {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.recyle_view, parent, false)
        return MyViewHolder(itemView)
    }


    @SuppressLint("ResourceType")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val currentItem = userList[position]

        holder.pages.text = currentItem.Pages
        holder.subject.text = currentItem.Subject
        holder.price.text = currentItem.Price


        Log.d("TAGS", "Status is onBindViewHolder method")

        if (currentItem.Status == "Pending") {
            holder.status.setImageResource(R.drawable.progress)
        }

    }

    override fun getItemCount(): Int {

        Log.d("TAGS", "Status is getItemCount method")
        return userList.size
    }

    open class MyViewHolder(itemView : View) : RecyclerView.ViewHolder (itemView) {

        val pages : TextView = itemView.findViewById(R.id.Pages)
        val subject : TextView = itemView.findViewById(R.id.Subject)
        val price : TextView = itemView.findViewById(R.id.Price)
        val status : ImageView = itemView.findViewById(R.id.Status)

    }

}