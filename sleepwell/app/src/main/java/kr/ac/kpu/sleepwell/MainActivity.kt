package kr.ac.kpu.sleepwell

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.multidex.MultiDex
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fragmentAdapter=MainAdapter(supportFragmentManager)
        viewpager_main.adapter=fragmentAdapter

        tabs_main.setupWithViewPager(viewpager_main)
        val images = ArrayList<Int>()
        images.add(R.drawable.ic_baseline_bedtime_24)
        images.add(R.drawable.ic_baseline_today_24)
        images.add(R.drawable.ic_baseline_assessment_24)
        images.add(R.drawable.ic_baseline_person_24)
        images.add(R.drawable.ic_baseline_bedtime_24)
        for (i in 0..4) tabs_main.getTabAt(i)!!.setIcon(images[i])
    }
}

