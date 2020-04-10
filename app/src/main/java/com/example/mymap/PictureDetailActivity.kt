package com.example.mymap

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Base64
import android.view.View
import com.google.android.gms.maps.model.LatLng
import io.realm.Realm
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_add.*
import kotlinx.android.synthetic.main.activity_picture_detail.*
import java.io.ByteArrayOutputStream

class PictureDetailActivity : AppCompatActivity() {

    private lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picture_detail)
        realm = Realm.getDefaultInstance()

        val mymapId = intent.getStringExtra("marker")//タップされたマーカーtitle(日付を受け取る）

        val realmResult = realm.where(Memo::class.java)
            .findAll()//タップされたマーカーと保存されている日付と同じものを探す
        for (memo: Memo in realmResult) {
            val markertitle = (DateFormat.format("yyyy/MM/dd kk:mm", memo.dateTime).toString())

            if (mymapId == markertitle) {

                val decodedString = Base64.decode(memo?.picture, Base64.DEFAULT) // 文字列をbase64形式に変更
                val decodeByte = BitmapFactory.decodeByteArray(
                    decodedString,
                    0,
                    decodedString.size
                ) // base64文字列をBitmap形式に変更
                val pictureDrawable = BitmapDrawable(decodeByte) // Bitmap形式の画像をDrawable型に変更
                picturedetailView.setImageDrawable(pictureDrawable)//Drawableに変換したものをセット

            }
        }
    }
}
