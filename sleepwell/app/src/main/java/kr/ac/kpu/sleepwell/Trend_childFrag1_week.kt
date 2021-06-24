package kr.ac.kpu.sleepwell

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import kotlinx.android.synthetic.main.fragment_trend_child_frag1_week.*


class Trend_childFrag1_week : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        var v:View=inflater.inflate(R.layout.fragment_trend_child_frag1_week, container, false)
        return v
    }

    override fun onResume() {
        super.onResume()
        val ref_trend = db.collection("trend").document(userkey)
        ref_trend.collection("week").document("data").addSnapshotListener(EventListener<DocumentSnapshot> {snapshot, e->
            if(e != null){
                Log.w(tag, "Listen failed.", e)
                return@EventListener }
            if(snapshot != null && snapshot.exists()) {
                no_w_data.visibility = View.INVISIBLE
                var week_time = snapshot?.data!!["week_time"].toString()  //몇분 잤는지
                var week_start = snapshot?.data!!["week_start"].toString()  //자러 간 시간
                var week_score = snapshot?.data!!["week_score"].toString()  //깊은수면
                var week_end = snapshot?.data!!["week_end"].toString()    //얕은수면

                var x = week_time.toInt()
                var hour = x/60
                var minute = ((x.toDouble()/60 - hour.toDouble())*60).toInt()
                if(hour==0){
                    w_time.setText(x.toString() +"분")
                }
                else{
                    w_time.setText(hour.toString()+"시간 "+ minute.toString()+"분")
                }
                w_score.setText(week_score)
                w_start.setText(week_start)
                w_end.setText(week_end)
            }
        })
        w_barchart.apply{
            description.isEnabled = false
            setMaxVisibleValueCount(7) //최대 보이는 그래프 개수
            setPinchZoom(false) //zoom in out
            setDrawBarShadow(false) //그래프 그림자
            setDrawGridBackground(false)    //격자구조 넣을껀지
            //setDrawBorders(false)
            legend.isEnabled=false
            setTouchEnabled(false)
            isDoubleTapToZoomEnabled=false
            animateY(1000)
        }
        val xAxis=w_barchart.xAxis
        xAxis.apply{
            setDrawGridLines(false)
            isEnabled=true
            position= XAxis.XAxisPosition.BOTTOM
            setDrawAxisLine(true)
            granularity=1f
            isGranularityEnabled=true
            textSize=12f
            // textColor= ContextCompat.getColor(R.color.white)
        }
        val yAxis_l=w_barchart.axisLeft
        yAxis_l.apply {
            setDrawLabels(false)
            isEnabled = false
            axisMinimum = 0f // 최소값
            axisMaximum = 10f // 최대값
            granularity = 1f // 값 만큼 라인선 설정
            textColor = Color.RED // 색상 설정
            axisLineColor = Color.BLACK // 축 색상 설정
            //gridColor = Color.BLUE // 격자 색상 설정
        }

        val values = ArrayList<BarEntry>()
        val type = ArrayList<String>()
        val colorlist = ArrayList<Int>()
        val set : BarDataSet

        values.add(BarEntry(0.toFloat(),10f))
        values.add(BarEntry(1.toFloat(),10f))
        values.add(BarEntry(2.toFloat(),10f))
        values.add(BarEntry(3.toFloat(),10f))
        values.add(BarEntry(4.toFloat(),10f))
        values.add(BarEntry(5.toFloat(),0f))
        values.add(BarEntry(6.toFloat(),0f))
        type.add("일")
        type.add("월")
        type.add("화")
        type.add("수")
        type.add("목")
        type.add("금")
        type.add("토")
        colorlist.add(Color.parseColor("#66dddd"))

        if (w_barchart.data !=null && w_barchart.data.dataSetCount > 1){
            val chartData = w_barchart.data
            set = chartData?.getDataSetByIndex(0) as BarDataSet
            set.values = values
            chartData.notifyDataChanged()
            w_barchart.notifyDataSetChanged()
        }else{
            set = BarDataSet(values, " ")
            set.colors = colorlist
            set.setDrawValues(true)

            val dataSets = ArrayList<IBarDataSet>()
            dataSets.add(set)

            val data = BarData(dataSets)
            w_barchart.data = data
            w_barchart.setVisibleXRange(1.0f,7.0f)
            w_barchart.setFitBars(true)

            val xAxis = w_barchart.xAxis
            xAxis.apply{
                granularity = 1f
                isGranularityEnabled = true
                valueFormatter = IndexAxisValueFormatter(type)
            }
            w_barchart.invalidate()
        }

    }
   /* private fun BarGraph(barChart: BarChart){
        barChart.apply {
            description.isEnabled=false
            setMaxVisibleValueCount(7) //최대 보이는 그래프 개수
            setPinchZoom(false) //zoom in out
            setDrawBarShadow(false) //그래프 그림자
            setDrawGridBackground(false)    //격자구조 넣을껀지
            //setDrawBorders(false)
            legend.isEnabled=true
            setTouchEnabled(false)
            isDoubleTapToZoomEnabled=false
            animateY(1000)
        }
        //val values=ArrayList<BarEntry>()
        val type=ArrayList<String>()
        var Bar_set: BarDataSet
        //values.add(BarEntry(0.0f,7.0f))
        /*
        colorlist.add(Color.parseColor("#88ffff"))    //deep
        Bar_values.add(BarEntry(0.toFloat(),10f))
        Bar_values.add(BarEntry(1.toFloat(),10f))
        Bar_values.add(BarEntry(2.toFloat(),10f))
        Bar_values.add(BarEntry(3.toFloat(),10f))
        Bar_values.add(BarEntry(4.toFloat(),10f))
        Bar_values.add(BarEntry(5.toFloat(),10f))
        Bar_values.add(BarEntry(6.toFloat(),10f))
        */
        type.add("일")
        type.add("월")
        type.add("화")
        type.add("수")
        type.add("목")
        type.add("금")
        type.add("토")

        if(barChart.data!=null && barChart.data.dataSetCount>1){
            val chartData=barChart.data
            Bar_set=chartData?.getDataSetByIndex(0) as BarDataSet
            Bar_set.values=Bar_values
            chartData.notifyDataChanged()
            barChart.notifyDataSetChanged()
        }
        else{
            Bar_set= BarDataSet(Bar_values,"수면 시간")
            Bar_set.setColor(colorlist.get(0))
            Bar_set.setDrawValues(false)

            val dataSets=ArrayList<IBarDataSet>()
            dataSets.add(Bar_set)

            val data= BarData(dataSets)
            barChart.data=data
            barChart.setFitBars(false)

            //x축 설정
            val xAxis=barChart.xAxis
            xAxis.apply{
                setDrawGridLines(false)
                isEnabled=true
                position= XAxis.XAxisPosition.BOTTOM
                setDrawAxisLine(true)
                granularity=1f
                isGranularityEnabled=true
                valueFormatter= IndexAxisValueFormatter(type)
                textSize=12f
                // textColor= ContextCompat.getColor(R.color.white)
            }
            //y축 설정
            val yAxis_l=barChart.axisLeft
            yAxis_l.apply {
                setDrawLabels(false)
                isEnabled = false
                axisMinimum = 0f // 최소값
                axisMaximum = 10f // 최대값
                granularity = 1f // 값 만큼 라인선 설정
                textColor = Color.RED // 색상 설정
                axisLineColor = Color.BLACK // 축 색상 설정
                //gridColor = Color.BLUE // 격자 색상 설정
            }
            val yAxis_R=barChart.axisRight
            yAxis_R.apply {
                setDrawLabels(false)
                isEnabled = false
                axisMinimum = 0f // 최소값
                axisMaximum = 10f // 최대값
                granularity = 1f // 값 만큼 라인선 설정
                textColor = Color.RED // 색상 설정
                axisLineColor = Color.BLACK // 축 색상 설정
                //gridColor = Color.BLUE // 격자 색상 설정
            }
            val values = ArrayList<BarEntry>()
            val type = ArrayList<String>()
            val colorlist =

        }
      */


        /*val day = findDate()
        val cycleRef = db.collection(userkey).document(day).collection("cycle").document("cycle")
        cycleRef.addSnapshotListener(EventListener<DocumentSnapshot> {snapshot,e->
            if(e != null){
                Log.w("tag", "Listen failed.", e)
                return@EventListener
            }
            if(snapshot != null && snapshot.exists()){
                var cycles = mutableListOf<String>()
                var size = snapshot?.data!!["size"].toString().toInt()
                for(i in 0..size-1){
                    cycles.add(snapshot?.data!![i.toString()].toString())
                }
                for(i in 0..cycles.size-1){
                    if(cycles[i]=="awake"){
                        Bar_values.add(BarEntry(i.toFloat(),10f))
                    }
                    if(cycles[i]=="sleep_rem"){
                        Bar_values.add(BarEntry(i.toFloat(),4f))
                    }
                    if(cycles[i]=="sleep_light"){
                        Bar_values.add(BarEntry(i.toFloat(),6f))
                    }
                    if(cycles[i]=="sleep_deep"){
                        Bar_values.add(BarEntry(i.toFloat(),1f))
                    }
                }


                colorlist.add(Color.parseColor("#88ffff"))    //deep

                if(barChart.data!=null && barChart.data.dataSetCount>1){
                    val chartData=barChart.data
                    Bar_set=chartData?.getDataSetByIndex(0) as BarDataSet
                    Bar_set.values=Bar_values
                    chartData.notifyDataChanged()
                    barChart.notifyDataSetChanged()
                }
                else{
                    Bar_set= BarDataSet(Bar_values,"수면 시간")
                    Bar_set.setColor(colorlist.get(0))
                    Bar_set.setDrawValues(false)

                    val dataSets=ArrayList<IBarDataSet>()
                    dataSets.add(Bar_set)

                    val data= BarData(dataSets)
                    barChart.data=data
                    barChart.setFitBars(false)

                    //x축 설정
                    val xAxis=barChart.xAxis
                    xAxis.apply{
                        setDrawGridLines(false)
                        isEnabled=true
                        position= XAxis.XAxisPosition.BOTTOM
                        setDrawAxisLine(true)
                        granularity=1f
                        isGranularityEnabled=true
                        valueFormatter= IndexAxisValueFormatter(type)
                        textSize=12f
                       // textColor= ContextCompat.getColor(R.color.white)
                    }
                    //y축 설정
                    val yAxis_l=barChart.axisLeft
                    yAxis_l.apply {
                        setDrawLabels(false)
                        isEnabled = false
                        axisMinimum = 0f // 최소값
                        axisMaximum = 10f // 최대값
                        granularity = 1f // 값 만큼 라인선 설정
                        textColor = Color.RED // 색상 설정
                        axisLineColor = Color.BLACK // 축 색상 설정
                        //gridColor = Color.BLUE // 격자 색상 설정
                    }
                    val yAxis_R=barChart.axisRight
                    yAxis_R.apply {
                        setDrawLabels(false)
                        isEnabled = false
                        axisMinimum = 0f // 최소값
                        axisMaximum = 10f // 최대값
                        granularity = 1f // 값 만큼 라인선 설정
                        textColor = Color.RED // 색상 설정
                        axisLineColor = Color.BLACK // 축 색상 설정
                        //gridColor = Color.BLUE // 격자 색상 설정
                    }
                    barChart.invalidate()
                }
            }
        })
         */
    }
    /*
    fun SleepCycleRadar(v:View){
        var radarChart: RadarChart

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

        var set1:LineDataSet= LineDataSet(entries,"Dataset1")
        set1.mode=LineDataSet.Mode.CUBIC_BEZIER
        set1.setDrawFilled(true)
        set1.setFillColor(R.color.teal_700)

        var datasets=ArrayList<ILineDataSet>()
        datasets.add(set1)

        var data:LineData= LineData(datasets)

        set1.setColor(Color.BLACK)
        set1.setCircleColor(Color.BLACK)
    }

 */