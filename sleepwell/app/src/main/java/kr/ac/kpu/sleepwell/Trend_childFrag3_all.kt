package kr.ac.kpu.sleepwell

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet


class Trend_childFrag3_all : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var v:View=inflater.inflate(R.layout.fragment_trend_child_frag3_all, container, false)
        ///////////////////////////////////
        //google fit api로 data받아오는 코드
        ///////////////////////////////////
        //SleepcycleCheck(v)
        //SleepingTimeCheck(v)
        SleepCycleRadar(v)
        MaxDevibelcheck(v)
        return v
    }

    fun SleepCycleRadar(v:View){
        var radarChart: RadarChart

        val data_radar=ArrayList<RadarEntry>()
        data_radar.add(RadarEntry(8f,15f))
        data_radar.add(RadarEntry(4f,20f))
        data_radar.add(RadarEntry(6f,30f))
        data_radar.add(RadarEntry(9f,20f))

        var dataset: RadarDataSet = RadarDataSet(data_radar,"SleepCycle")
        dataset.setColor(Color.BLUE)


        val data = RadarData()
        data.addDataSet(dataset)
        val labels = arrayOf("Awake", "RemSleep", "LightSleep", "DeepSleep")
    }

    fun MaxDevibelcheck(v:View){

        var lineChart: LineChart

        val type=ArrayList<String>()
        type.add("")
        type.add("12:00")
        type.add("")
        type.add("")
        type.add("")
        type.add("")
        type.add("")
        type.add("")
        type.add("7:00")

        val y_type=ArrayList<String>()
        y_type.add("0db")
        y_type.add("20db")
        y_type.add("40db")
        y_type.add("60db")
        y_type.add("80db")
        y_type.add("100db")

        var entries=ArrayList<Entry>()

        entries.add(Entry(1.0f,22.0f))
        entries.add(Entry(2.0f,40.0f))
        entries.add(Entry(3.0f,21.0f))
        entries.add(Entry(4.0f,21.0f))
        entries.add(Entry(5.0f,25.0f))
        entries.add(Entry(6.0f,28.0f))
        entries.add(Entry(7.0f,20.5f))
        entries.add(Entry(8.0f,21.5f))
        entries.add(Entry(9.0f,21.5f))
        entries.add(Entry(10.0f,21.5f))
        entries.add(Entry(11.0f,22.5f))
        entries.add(Entry(12.0f,21.5f))
        entries.add(Entry(13.0f,25.5f))
        entries.add(Entry(14.0f,24.5f))
        entries.add(Entry(15.0f,26.5f))
        entries.add(Entry(16.0f,27.5f))

        var set1: LineDataSet = LineDataSet(entries,"Dataset1")
        set1.mode= LineDataSet.Mode.CUBIC_BEZIER
        set1.setDrawFilled(true)
        set1.setFillColor(R.color.teal_700)

        var datasets=ArrayList<ILineDataSet>()
        datasets.add(set1)

        var data: LineData = LineData(datasets)

        set1.setColor(Color.BLACK)
        set1.setCircleColor(Color.BLACK)
    }

}