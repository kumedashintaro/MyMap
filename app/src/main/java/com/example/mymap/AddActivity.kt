package com.example.mymap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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

        val lat = intent.getDoubleExtra("lat", 0.0)
        val lng = intent.getDoubleExtra("lng", 0.0)

        saveBtn.setOnClickListener {
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
            showToast("保存しました")
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



