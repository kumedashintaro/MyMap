package shintaro.mymap

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.format.DateFormat
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import shintaro.mymap.Memo
import shintaro.mymap.ViewHolder
import io.realm.RealmResults
import shintaro.mymap.R


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
        val decodedString = Base64.decode(mymap?.picture, Base64.DEFAULT) // 文字列をbase64形式に変更
        val decodeByte = BitmapFactory.decodeByteArray(
            decodedString,
            0,
            decodedString.size
        ) // base64文字列をBitmap形式に変更
        val pictureDrawable = BitmapDrawable(decodeByte) // Bitmap形式の画像をDrawable型に変更

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


