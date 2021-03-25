package kr.ac.kpu.sleepwell

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.MotionEvent
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_sleep_start.*
import java.text.SimpleDateFormat
import java.util.*

class SleepStart : AppCompatActivity() {

    var isRunning:Boolean=false
    lateinit var backgroundintent:Intent
    lateinit var dayresultintent:Intent
    var countnum=4
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sleep_start)

        val myapp=application as MyglobalArraylist
        backgroundintent= Intent(this,backgroundservice::class.java)
        dayresultintent= Intent(this,Day_resultAC::class.java)
        backgroundintent.setAction("startForeground")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(backgroundintent)
        }else{
            startService(backgroundintent)
        }

        /*btn_stop.setOnClickListener {
            stopService(intent)
            val intent= Intent(this,Day_resultAC::class.java)
            startActivity(intent)
            finish()
        }*/

        btn_stop.setOnTouchListener { view, motionEvent ->
            when(motionEvent.action){
                MotionEvent.ACTION_DOWN->{
                    tv_count.isVisible=true
                    isRunning=true
                    val UI_thread:Thread=runUI_thread()
                    UI_thread.start()
                }
                MotionEvent.ACTION_UP->{
                    tv_count.isVisible=false
                    isRunning=false
                    countnum=4
                }
            }
            return@setOnTouchListener false
        }
    }

    inner class runUI_thread():Thread(){
        override fun run() {
            countnum=4
            while(isRunning){
                SystemClock.sleep(500)
                countnum-=1
                if(countnum<0){
                    isRunning=false
                    stopService(backgroundintent)
                    startActivity(dayresultintent)
                    finish()
                    break
                }
                Log.d("text",countnum.toString())
                runOnUiThread{
                    tv_count.setText(countnum.toString())
                }
            }
        }
    }
    override fun onBackPressed() {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Sleep Well")
            .setIcon(R.mipmap.ic_launcher_round)
            .setMessage("수면을 종료하려면 Stop 버튼을 누르세요.")
            .setNegativeButton("닫기",null)
            .create()
        alertDialog.show()
    }
}