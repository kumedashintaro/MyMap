package com.example.mymap

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
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


            val decodedString = Base64.decode(mymap?.picture, Base64.DEFAULT) // 文字列をbase64形式に変更
            val decodeByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size) // base64文字列をBitmap形式に変更
            val pictureDrawable = BitmapDrawable(decodeByte) // Bitmap形式の画像をDrawable型に変更
            pictureView.setImageDrawable(pictureDrawable)//Drawableに変換したものをセット



            deleteBtn.visibility = View.VISIBLE
        }else{
            deleteBtn.visibility = View.INVISIBLE
        }

        val lat = intent.getDoubleExtra("lat", 0.0)
        val lng = intent.getDoubleExtra("lng", 0.0)


        pictureView.setOnClickListener{
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


        saveBtn.setOnClickListener {

            when (mymapId) {
                0L -> {
////////////////////////////////////////////////////////////////////////////////////////////////////////
                    // 添付画像を取得する
                    val drawable = pictureView.drawable as? BitmapDrawable
                    // 添付画像が設定されていれば画像を取り出してBASE64エンコードする
                    val bitmap = drawable?.bitmap
                    val baos = ByteArrayOutputStream()
                    if (bitmap != null) {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                    }
                    val bitmapString = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)
////////////////////////////////////////////////////////////////////////////////////////////////////////

                    val memoStr = memoEdit.text?.toString() ?: ""
                    realm.executeTransaction {
                        val maxId = realm.where<Memo>().max("id")
                        val nextId = (maxId?.toLong() ?: 0L) + 1L
                        val memo = realm.createObject<Memo>(nextId)
                        memo.dateTime = Date()
                        memo.lat = lat
                        memo.lng = lng
                        memo.memo = memoStr
                        memo.picture = bitmapString
                    }
                }
                //修正処理
                else -> {
                    val memoStr = memoEdit.text?.toString() ?: ""

////////////////////////////////////////////////////////////////////////////////////////////////////////
                    // 添付画像を取得する
                    val drawable = pictureView.drawable as? BitmapDrawable
                    // 添付画像が設定されていれば画像を取り出してBASE64エンコードする
                    val bitmap = drawable?.bitmap
                    val baos = ByteArrayOutputStream()
                    if (bitmap != null) {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                    }
                    val bitmapString = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)
////////////////////////////////////////////////////////////////////////////////////////////////////////


                    realm.executeTransaction{
                        val mymap = realm.where<Memo>()
                            .equalTo("id", mymapId).findFirst()
                        mymap?.memo = memoStr
                        mymap?.picture = bitmapString
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CHOOSER_REQUEST_CODE) {

            if (resultCode != Activity.RESULT_OK) {
                if (mPictureUri != null) {
                    contentResolver.delete(mPictureUri!!, null, null)
                    mPictureUri = null
                }
                return
            }

            // 画像を取得
            val uri = if (data == null || data.data == null) mPictureUri else data.data

            // URIからBitmapを取得する
            val image: Bitmap
            try {
                val contentResolver = contentResolver
                val inputStream = contentResolver.openInputStream(uri!!)
                image = BitmapFactory.decodeStream(inputStream)
                inputStream!!.close()
            } catch (e: Exception) {
                return
            }

            // 取得したBimapの長辺を500ピクセルにリサイズする
            val imageWidth = image.width
            val imageHeight = image.height
            val scale = Math.min(500.toFloat() / imageWidth, 500.toFloat() / imageHeight) // (1)

            val matrix = Matrix()
            matrix.postScale(scale, scale)

            val resizedImage = Bitmap.createBitmap(image, 0, 0, imageWidth, imageHeight, matrix, true)

            // BitmapをImageViewに設定する
            pictureView.setImageBitmap(resizedImage)

            mPictureUri = null
        }
    }

    }



