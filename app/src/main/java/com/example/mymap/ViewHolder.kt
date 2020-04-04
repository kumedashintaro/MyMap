package com.example.mymap

import android.view.View
import android.widget.TextView
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.one_result.view.*

class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    var datetimeText: TextView? = null
    var memeText: TextView? = null
    init {
        datetimeText = itemView.detatimeText
        memeText = itemView.memoText
    }
}