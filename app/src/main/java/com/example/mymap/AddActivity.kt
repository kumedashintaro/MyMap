package com.example.mymap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_add.*
import java.util.*

class AddActivity : AppCompatActivity() {
    private lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)
        realm = Realm.getDefaultInstance()

//一覧からクリックされた場合
        val mymapId = intent.getLongExtra("id", 0L)
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
    }



