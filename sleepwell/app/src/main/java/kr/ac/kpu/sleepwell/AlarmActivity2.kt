package kr.ac.kpu.sleepwell

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_alarm.*
import kotlinx.android.synthetic.main.activity_alarm2.*

class AlarmActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm2)
        backbutton2.setOnClickListener {
            val intent= Intent(this,AlarmActivity::class.java)
            startActivity(intent)
        }
    }
}