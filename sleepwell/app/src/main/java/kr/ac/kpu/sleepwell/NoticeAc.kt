package kr.ac.kpu.sleepwell

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class NoticeAc : AppCompatActivity() {
    var movieDataList: ArrayList<noticeData>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notice)
    }
}