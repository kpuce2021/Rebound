package kr.ac.kpu.sleepwell

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_alarm.*

class AlarmActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)
        backbutton.setOnClickListener {
            val intent= Intent(this,MainActivity::class.java)
            startActivity(intent)
        }
    }
}