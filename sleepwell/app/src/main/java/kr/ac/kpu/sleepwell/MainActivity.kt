package kr.ac.kpu.sleepwell

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fragmentAdapter=MainAdapter(supportFragmentManager)
        viewpager_main.adapter=fragmentAdapter

        tabs_main.setupWithViewPager(viewpager_main)
        val images = ArrayList<Int>()
        images.add(R.drawable.ic_baseline_bedtime_24_3)
        images.add(R.drawable.ic_baseline_today_24)
        images.add(R.drawable.ic_baseline_assessment_24)
        images.add(R.drawable.ic_baseline_person_24)
        val images2 = ArrayList<Int>()
        images.add(R.drawable.ic_baseline_bedtime_24_2)
        images.add(R.drawable.ic_baseline_today_24_2)
        images.add(R.drawable.ic_baseline_assessment_24_2)
        images.add(R.drawable.ic_baseline_person_24_2)
        for (i in 0..3) tabs_main.getTabAt(i)!!.setIcon(images[i])
    }
}

