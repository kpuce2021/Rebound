package kr.ac.kpu.sleepwell

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_sleep_start.*
import java.text.SimpleDateFormat
import java.util.*

class SleepStart : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sleep_start)

        val myapp=application as MyglobalArraylist
        val intent= Intent(this,backgroundservice::class.java)
        intent.setAction("startForeground")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        }else{
            startService(intent)
        }

        btn_stop.setOnClickListener {
            stopService(intent)
            val intent= Intent(this,Day_resultAC::class.java)
            startActivity(intent)
            finish()
        }
    }
}