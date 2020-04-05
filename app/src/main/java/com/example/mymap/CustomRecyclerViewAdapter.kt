package com.example.mymap

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import io.realm.RealmResults


class CustomRecyclerViewAdapter(realmResults: RealmResults<Memo>) :
    RecyclerView.Adapter<ViewHolder>() {
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

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mymap = rResults[position]

        var pictureDrawable = mymap?.picture as Drawable//Drawableに変換

        holder.datetimeText?.text = DateFormat.format("yyyyy/MM/dd kk:mm", mymap?.dateTime)
        holder.memoText?.text = mymap?.memo.toString()
        holder.picture?.setImageDrawable(pictureDrawable)//Drawableに変換したものをセット

        holder.itemView.setBackgroundColor(if (position % 2 == 0) Color.TRANSPARENT else Color.WHITE)

        holder.itemView.setOnClickListener {
            val intent = Intent(it.context, AddActivity::class.java)
            intent.putExtra("id", mymap?.id)
            it.context.startActivity(intent)
        }
    }

}


