package kr.ac.kpu.sleepwell

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.fragment_2.*
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.gms.common.SupportErrorDialogFragment.newInstance
import com.google.android.material.slider.LabelFormatter
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_2.view.*
import kotlinx.android.synthetic.main.fragment_trend_child_frag1_week.*
import kr.ac.kpu.sleepwell.Fragment0.Companion.newInstance
import kr.ac.kpu.sleepwell.Fragment1.Companion.newInstance
import kr.ac.kpu.sleepwell.Fragment3.Companion.newInstance
import java.lang.reflect.Array.newInstance
import java.net.URLClassLoader.newInstance
import java.util.*
import javax.xml.datatype.DatatypeFactory.newInstance
import javax.xml.transform.TransformerFactory.newInstance
import kotlin.collections.ArrayList


class Fragment2 : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var v:View=inflater.inflate(R.layout.fragment_2, container, false)


        childFragmentManager.beginTransaction().replace(R.id.childFragment,Trend_childFrag1_week()).commit()

        var btn_week1:Button=v.findViewById(R.id.btn_week) as Button
        var btn_month1:Button=v.findViewById(R.id.btn_month) as Button
        var btn_all1:Button=v.findViewById(R.id.btn_all) as Button

        btn_week1.setOnClickListener {
            childFragmentManager.beginTransaction().replace(R.id.childFragment,Trend_childFrag1_week()).commit()
        }
        btn_month1.setOnClickListener {
            childFragmentManager.beginTransaction().replace(R.id.childFragment,Trend_childFrag2_month()).commit()
        }
        btn_all1.setOnClickListener {
            childFragmentManager.beginTransaction().replace(R.id.childFragment,Trend_childFrag3_all()).commit()
        }
        return v
    }








}

