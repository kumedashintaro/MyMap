package com.example.mymap

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.one_result.view.*

class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    var datetimeText: TextView? = null
    var memoText: TextView? = null
    var picture: ImageView? =null
    init {
        datetimeText = itemView.detatimeText
        memoText = itemView.memoText
        picture = itemView.pictureView
    }
}