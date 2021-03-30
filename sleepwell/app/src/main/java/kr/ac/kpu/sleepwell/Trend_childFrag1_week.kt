package kr.ac.kpu.sleepwell

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet


class Trend_childFrag1_week : Fragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        var v:View=inflater.inflate(R.layout.fragment_trend_child_frag1_week, container, false)
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
        radarChart=v.findViewById<RadarChart>(R.id.radar_chart)

        val data_radar=ArrayList<RadarEntry>()
        data_radar.add(RadarEntry(8f,15f))
        data_radar.add(RadarEntry(4f,20f))
        data_radar.add(RadarEntry(6f,30f))
        data_radar.add(RadarEntry(9f,20f))

        var dataset:RadarDataSet=RadarDataSet(data_radar,"SleepCycle")
        dataset.setColor(Color.BLUE)


        val data = RadarData()
        data.addDataSet(dataset)
        val labels = arrayOf("Awake", "RemSleep", "LightSleep", "DeepSleep")
        val xAxis = radarChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.textColor=R.color.white
        radarChart.data = data
    }

    fun MaxDevibelcheck(v:View){

        var lineChart: LineChart
        lineChart=v.findViewById<LineChart>(R.id.sound_linechart)

        lineChart.apply {
            description.isEnabled=false
            setMaxVisibleValueCount(16)
            setPinchZoom(false)
            setDrawGridBackground(false)
            setDrawBorders(false)
            legend.isEnabled=false
            setTouchEnabled(false)
            isDoubleTapToZoomEnabled=false
            animateY(2000)

            axisRight.run {
                isEnabled=false
                textSize=15f
            }
        }

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

        //x축설정
        val xaxis=lineChart.xAxis
        xaxis.apply{
            setDrawGridLines(false)
            isEnabled=true
            position= XAxis.XAxisPosition.BOTTOM
            setDrawAxisLine(false)
            textColor=R.color.white
            granularity=0.5f
            isGranularityEnabled=false
            valueFormatter= IndexAxisValueFormatter(type)
            textSize=12f
        }
        //y축설정
        val yaxis=lineChart.axisLeft
        yaxis.apply {
            isEnabled = true
            axisMinimum = 0f // 최소값
            axisMaximum = 100f // 최대값
            granularity = 10f // 값 만큼 라인선 설정
            setDrawLabels(true) // 값 셋팅 설정
            textColor = Color.RED // 색상 설정
            axisLineColor = Color.BLACK // 축 색상 설정
            gridColor = Color.BLACK // 격자 색상 설정
            //valueFormatter=IndexAxisValueFormatter(y_type)
        }

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

        var set1:LineDataSet= LineDataSet(entries,"Dataset1")
        set1.mode=LineDataSet.Mode.CUBIC_BEZIER
        set1.setDrawFilled(true)
        set1.setFillColor(R.color.teal_700)

        var datasets=ArrayList<ILineDataSet>()
        datasets.add(set1)

        var data:LineData= LineData(datasets)

        set1.setColor(Color.BLACK)
        set1.setCircleColor(Color.BLACK)
        lineChart.data=data
        lineChart.setVisibleXRange(1.0f,16.0f)
    }
}