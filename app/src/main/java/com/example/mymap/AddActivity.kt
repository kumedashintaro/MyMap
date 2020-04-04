package com.example.mymap

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_add.*
import java.io.ByteArrayOutputStream
import java.util.*
import android.util.Base64

class AddActivity : AppCompatActivity() {
    private lateinit var realm: Realm

    companion object {
        private val PERMISSIONS_REQUEST_CODE = 100
        private val CHOOSER_REQUEST_CODE = 100
    }
    private var mPictureUri: Uri? = null

    @SuppressLint("WrongThread")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)
        realm = Realm.getDefaultInstance()


        val mymapId = intent.getLongExtra("id", 0L)//一覧からクリックされた場合IDを受け取る
        if (mymapId > 0L){
            val mymap = realm.where<Memo>()
                .equalTo("id", mymapId).findFirst()
            memoEdit.setText(mymap?.memo.toString())
            deleteBtn.visibility = View.VISIBLE
        }else{
            deleteBtn.visibility = View.INVISIBLE
        }

        val lat = intent.getDoubleExtra("lat", 0.0)
        val lng = intent.getDoubleExtra("lng", 0.0)


        imageView.setOnClickListener{
                // パーミッションの許可状態を確認する
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        // 許可されている
                        showChooser()
                    } else {
                        // 許可されていないので許可ダイアログを表示する
                        requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)

                        return@setOnClickListener
                    }
                } else {
                    showChooser()
                }

        }

// 添付画像を取得する





        saveBtn.setOnClickListener {

            when(mymapId){
                0L -> {

                    val memoStr = memoEdit.text?.toString() ?: ""
                    realm.executeTransaction {
                        val maxId = realm.where<Memo>().max("id")
                        val nextId = (maxId?.toLong() ?: 0L) + 1L
                        val memo = realm.createObject<Memo>(nextId)
                        memo.dateTime = Date()
                        memo.lat = lat
                        memo.lng = lng
                        memo.memo = memoStr
                    }
                }
                //修正処理
                else -> {
                    val memoStr = memoEdit.text?.toString() ?: ""
                    realm.executeTransaction{
                        val mymap = realm.where<Memo>()
                            .equalTo("id", mymapId).findFirst()
                        mymap?.memo = memoStr
                    }
                }
            }
            showToast("保存しました")
            finish()
        }

        deleteBtn.setOnClickListener(){
            realm.executeTransaction{
                val mymap = realm.where<Memo>()
                    .equalTo("id", mymapId)
                    ?.findFirst()
                    ?.deleteFromRealm()
            }
            showToast("削除しました")
            finish()
        }

        canselBtn.setOnClickListener {
            finish()
        }
    }

        override fun onDestroy() {
            super.onDestroy()
            realm.close()
        }

        private fun showToast(msg: String){
            val toast = Toast.makeText(this, msg, Toast.LENGTH_LONG)
            toast.show()
        }


    private fun showChooser() {
        // ギャラリーから選択するIntent
        val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
        galleryIntent.type = "image/*"
        galleryIntent.addCategory(Intent.CATEGORY_OPENABLE)

        // カメラで撮影するIntent
        val filename = System.currentTimeMillis().toString() + ".jpg"
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, filename)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        mPictureUri = contentResolver
            .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPictureUri)

        // ギャラリー選択のIntentを与えてcreateChooserメソッドを呼ぶ
        val chooserIntent = Intent.createChooser(galleryIntent, "画像を取得")

        // EXTRA_INITIAL_INTENTS にカメラ撮影のIntentを追加
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent))

        startActivityForResult(chooserIntent, CHOOSER_REQUEST_CODE)
    }

    }



