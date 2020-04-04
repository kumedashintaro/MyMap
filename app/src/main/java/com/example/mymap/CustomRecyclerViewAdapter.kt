package com.example.mymap

import android.content.Intent
import android.graphics.Color
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.realm.RealmResults

class CustomRecyclerViewAdapter(realmResults: RealmResults<Memo>): RecyclerView.Adapter<ViewHolder>(){
    private val rResults: RealmResults<Memo> = realmResults

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.one_result, parent, false)
        val viewHolder = ViewHolder(view)
        return viewHolder
    }

    override fun getItemCount(): Int {
        return rResults.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int){
        val mymap = rResults[position]
        holder.datetimeText?.text = DateFormat.format("yyyyy/MM/dd kk:mm", mymap?.dateTime)
        holder.memeText?.text = mymap?.memo.toString()

        holder.itemView.setBackgroundColor(if (position % 2 == 0) Color.TRANSPARENT else Color.WHITE)

        holder.itemView.setOnClickListener{
            val intent = Intent(it.context, AddActivity::class.java)
            intent.putExtra("id", mymap?.id)
            it.context.startActivity(intent)
        }
    }
}