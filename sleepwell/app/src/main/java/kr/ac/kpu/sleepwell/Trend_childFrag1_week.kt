package kr.ac.kpu.sleepwell

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.ColorTemplate


class Trend_childFrag1_week : Fragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        var v:View=inflater.inflate(R.layout.fragment_trend_child_frag1_week, container, false)
        ///////////////////////////////////
        //google fit api로 data받아오는 코드
        ///////////////////////////////////
        SleepcycleCheck(v)
        SleepingTimeCheck(v)
        sleeptimecheck(v)
        return v
    }
    fun SleepcycleCheck(v:View){
        var pieChart: PieChart
        pieChart=v.findViewById<PieChart>(R.id.piechart)
        pieChart.setUsePercentValues(true)
        val entries=ArrayList<PieEntry>()
        entries.add(PieEntry(508f,"Awake(during Sleep)"))
        entries.add(PieEntry(600f,"light Sleep"))
        entries.add(PieEntry(750f,"Deep Sleep"))
        entries.add(PieEntry(508f,"REM"))
        entries.add(PieEntry(670f,"Sleep"))

        val colorItems=ArrayList<Int>()
        for(c in ColorTemplate.VORDIPLOM_COLORS) colorItems.add(c)
        for(c in ColorTemplate.JOYFUL_COLORS) colorItems.add(c)
        for(c in ColorTemplate.COLORFUL_COLORS) colorItems.add(c)
        for(c in ColorTemplate.LIBERTY_COLORS) colorItems.add(c)
        for(c in ColorTemplate.PASTEL_COLORS) colorItems.add(c)
        colorItems.add(ColorTemplate.getHoloBlue())

        val pieDataSet= PieDataSet(entries,"")
        pieDataSet.apply {
            colors=colorItems
            valueTextColor= Color.BLACK
            valueTextSize=16f
        }

        val pieData= PieData(pieDataSet)
        pieChart.apply {
            data=pieData
            description.isEnabled=false
            isRotationEnabled=false
            centerText="Sleep Cycle"
            setEntryLabelColor(Color.BLACK)
            animateY(1400, Easing.EaseInOutQuad)
            animate()
        }
    }

    fun SleepingTimeCheck(v:View){

        var barChart: BarChart
        barChart=v.findViewById<BarChart>(R.id.barchart)

        barChart.apply {
            description.isEnabled=false
            setMaxVisibleValueCount(7)
            setPinchZoom(false)
            setDrawBarShadow(false)
            setDrawGridBackground(false)
            setDrawBorders(false)
            legend.isEnabled=false
            setTouchEnabled(false)
            isDoubleTapToZoomEnabled=false
            animateY(2000)
        }

        val values=ArrayList<BarEntry>()
        val type=ArrayList<String>()
        val colorlist=ArrayList<Int>()
        var set:BarDataSet

        values.add(BarEntry(1.0f,8.5f))
        values.add(BarEntry(2.0f,7.1f))
        values.add(BarEntry(3.0f,10.5f))
        values.add(BarEntry(4.0f,6.7f))
        values.add(BarEntry(5.0f,8.6f))
        values.add(BarEntry(6.0f,8.4f))
        values.add(BarEntry(7.0f,7.5f))

        type.add("")
        type.add("Mon")
        type.add("Tues")
        type.add("Wedn")
        type.add("Thurs")
        type.add("Fri")
        type.add("Sat")
        type.add("Sun")

        colorlist.add(Color.parseColor("#FFFA8072"))
        colorlist.add(Color.parseColor("#FFFA8072"))
        colorlist.add(Color.parseColor("#FFFA8072"))
        colorlist.add(Color.parseColor("#FFFA8072"))
        colorlist.add(Color.parseColor("#FFFA8072"))
        colorlist.add(Color.parseColor("#FFFA8072"))
        colorlist.add(Color.parseColor("#FFFA8072"))

        if(barChart.data!=null && barChart.data.dataSetCount>1){
            val chartData=barChart.data
            set=chartData?.getDataSetByIndex(0) as BarDataSet
            set.values=values
            chartData.notifyDataChanged()
            barChart.notifyDataSetChanged()
        }
        else{
            set= BarDataSet(values,"수면시간")
            set.colors=colorlist
            set.setDrawValues(false)

            val dataSets=ArrayList<IBarDataSet>()
            dataSets.add(set)

            val data=BarData(dataSets)
            barChart.data=data
            barChart.setVisibleXRange(1.0f,7.0f)
            barChart.setFitBars(false)

            //x축 설정
            val xAxis=barChart.xAxis
            xAxis.apply{
                setDrawGridLines(false)
                isEnabled=true
                position=XAxis.XAxisPosition.BOTTOM
                setDrawAxisLine(false)
                granularity=1f
                isGranularityEnabled=false
                valueFormatter=IndexAxisValueFormatter(type)
                textSize=12f
            }

            //y축 설정
            val yAxis=barChart.axisLeft
            yAxis.apply {
                setDrawLabels(true)
                isEnabled = true
                axisMinimum = 0f // 최소값
                axisMaximum = 15f // 최대값
                granularity = 1f // 값 만큼 라인선 설정
                textColor = Color.RED // 색상 설정
                axisLineColor = Color.BLACK // 축 색상 설정
                gridColor = Color.BLUE // 격자 색상 설정
            }
            barChart.invalidate()
        }

    }

    fun sleeptimecheck(v:View){

        var lineChart: LineChart
        lineChart=v.findViewById<LineChart>(R.id.linechart)

        lineChart.apply {
            description.isEnabled=false
            setMaxVisibleValueCount(8)
            setPinchZoom(false)
            setDrawGridBackground(false)
            setDrawBorders(false)
            legend.isEnabled=false
            setTouchEnabled(false)
            isDoubleTapToZoomEnabled=false
            animateY(2000)
        }

        val type=ArrayList<String>()
        type.add("")
        type.add("Mon")
        type.add("Tues")
        type.add("Wedn")
        type.add("Thurs")
        type.add("Fri")
        type.add("Sat")
        type.add("Sun")

        val xaxis=lineChart.xAxis
        xaxis.apply{
            setDrawGridLines(false)
            isEnabled=true
            position= XAxis.XAxisPosition.BOTTOM
            setDrawAxisLine(false)
            granularity=1f
            isGranularityEnabled=false
            valueFormatter= IndexAxisValueFormatter(type)
            textSize=12f
        }

        var entries=ArrayList<Entry>()

        entries.add(Entry(1.0f,3.0f))
        entries.add(Entry(2.0f,7.0f))
        entries.add(Entry(3.0f,4.0f))
        entries.add(Entry(4.0f,9.0f))
        entries.add(Entry(5.0f,5.0f))
        entries.add(Entry(6.0f,8.0f))
        entries.add(Entry(7.0f,7.5f))

        var set1:LineDataSet= LineDataSet(entries,"Dataset1")

        var datasets=ArrayList<ILineDataSet>()
        datasets.add(set1)

        var data:LineData= LineData(datasets)

        set1.setColor(Color.BLACK)
        set1.setCircleColor(Color.BLACK)
        lineChart.data=data
        lineChart.setVisibleXRange(1.0f,7.0f)
    }
}