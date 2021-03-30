package kr.ac.kpu.sleepwell

import androidx.core.content.ContextCompat
import android.content.Context
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentTransaction
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_day_result_a_c.*
import kotlinx.android.synthetic.main.fragment_1.*
import java.io.FileInputStream
import java.lang.Exception
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

private const val LOG_TAG = "Error"

class Fragment1 : Fragment() {
    val db = Firebase.firestore
    val user = FirebaseAuth.getInstance()
    val userkey = user.uid.toString()


    private var REM_values=ArrayList<BarEntry>()
    private var deepSleep_values=ArrayList<BarEntry>()
    private var lightSleep_values=ArrayList<BarEntry>()
    private var Awake_values=ArrayList<BarEntry>()
    private val colorlist=ArrayList<Int>()

    private var getfilesize:Int=0
    private var mediaPlayer: MediaPlayer?=null
    private var pausePosition:Int?=null
    private var isPaused:Boolean=false
    var arraylist=ArrayList<String>(100)   //녹음파일 이름 저장(output2)
    private lateinit var callback: OnBackPressedCallback

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var v:View=inflater.inflate(R.layout.fragment_1, container, false)



/*        var calx = Calendar.getInstance()
        calx.time = Date()
        val dfx: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        calx.add(Calendar.DATE,-1)
        var daily = "dfsdf"
        daily= dfx.format(calx.time)
        day.setText(daily)//하루 치 결과를 보여줌*/

        val day = findDate1()
        val Ref_day = db.collection(userkey).document(day)
        Ref_day.addSnapshotListener(EventListener<DocumentSnapshot> {snapshot,e->
            if(e != null){
            Log.w(tag, "Listen failed.", e)
            return@EventListener
            }
            if(snapshot != null && snapshot.exists()){
                val day2 = findDate2()
                today.setText(day2)

                var sleep_time = snapshot?.data!!["sleep_time"].toString()
                var sleep_start = snapshot?.data!!["go_to_bed"].toString()
                var sleep_deep = snapshot?.data!!["sleep_deep"].toString()
                var sleep_light = snapshot?.data!!["sleep_light"].toString()
                var sleep_rem = snapshot?.data!!["sleep_rem"].toString()
                var go_to_sleep = snapshot?.data!!["go_to_sleep"].toString()

                changeSleep(sleep_time)
                changeDeep(sleep_deep)
                changeLight(sleep_light)
                changeRem(sleep_rem)
                changeGotoSleep(go_to_sleep)
                sleep_st.setText(sleep_start)
            }
        })
        /*document 전체 읽을 때 주석 품
        Ref.get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        Log.d("tag", "${document.id} => ${document.data}")
                    }
                    if (result != null){
                        block_btn.visibility = View.GONE
                    } else{}
                }
                .addOnFailureListener { exception ->
                    Log.d(tag, "get failed with ", exception)
                }
        if(){
        }*/
        AwakeDrawingGraph(v)
        SleepcycleCheck(v)
        return v
    }


    private fun changeSleep(x :String){
        var hour = x.toInt()/60
        var minute = ((x.toDouble()/60 - hour.toDouble())*60).toInt()
        if(hour==0){
            sleep_t.setText(x+"분")
        }
        else{
            sleep_t.setText(hour.toString()+"시간 "+ minute.toString()+"분")
        }
    }

    private fun changeGotoSleep(x :String){
        var hour = x.toInt()/60
        var minute = ((x.toDouble()/60 - hour.toDouble())*60).toInt()
        if(hour==0){
            go_sleep.setText(x+"분")
        }
        else{
            go_sleep.setText(hour.toString()+"시간 "+ minute.toString()+"분")
        }
    }

    private fun changeRem(x :String){
        var hour = x.toInt()/60
        var minute = ((x.toDouble()/60 - hour.toDouble())*60).toInt()
        if(hour==0){
            sleep_r.setText(x+"분")
        }
        else{
            sleep_r.setText(hour.toString()+"시간 "+ minute.toString()+"분")
        }
    }

    private fun changeDeep(x :String){
        var hour = x.toInt()/60
        var minute = ((x.toDouble()/60 - hour.toDouble())*60).toInt()
        if(hour==0){
            sleep_d.setText(x+"분")
        }
        else{
            sleep_d.setText(hour.toString()+"시간 "+ minute.toString()+"분")
        }
    }

    private fun changeLight(x :String){
        var hour = x.toInt()/60
        var minute = ((x.toDouble()/60 - hour.toDouble())*60).toInt()
        if(hour==0){
            sleep_m.setText(x+"분")
        }
        else{
            sleep_m.setText(hour.toString()+"시간 "+ minute.toString()+"분")
        }
    }

    fun SleepcycleCheck(v:View){
        var awak = 0f
        var rem = 0f
        var deep = 0f
        var light = 0f
        val day = findDate1()
        val Ref_day = db.collection(userkey).document(day)
        Ref_day.get().addOnSuccessListener { result ->
            if (result != null){
                block_btn.visibility = View.GONE}
        }
        Ref_day.addSnapshotListener(EventListener<DocumentSnapshot> {snapshot,e->
            if(e != null){
                Log.w(tag, "Listen failed.", e)
                return@EventListener
            }
            if(snapshot != null && snapshot.exists()){
                val day2 = findDate2()
                today.setText(day2)

                var awake = snapshot?.data!!["awake"].toString().toInt()
                var sleep_deep = snapshot?.data!!["sleep_deep"].toString().toInt()
                var sleep_light = snapshot?.data!!["sleep_light"].toString().toInt()
                var sleep_rem = snapshot?.data!!["sleep_rem"].toString().toInt()

                awak = awake.toFloat()
                rem = sleep_rem.toFloat()
                deep = sleep_deep.toFloat()
                light = sleep_light.toFloat()

                var pieChart: PieChart
                pieChart=v.findViewById<PieChart>(R.id.day_piechart)
                pieChart.setUsePercentValues(true)
                val entries=ArrayList<PieEntry>()
                if(rem > 0f){entries.add(PieEntry(rem,"REM"))}
                if(deep > 0f){entries.add(PieEntry(deep,"Deep Sleep"))}
                if(light > 0f){entries.add(PieEntry(light,"Light Sleep"))}
                if(awake > 0f){ entries.add(PieEntry(awak,"Awake"))}

                val colorItems=ArrayList<Int>()
                for(c in ColorTemplate.PASTEL_COLORS) colorItems.add(c)
                for(c in ColorTemplate.LIBERTY_COLORS) colorItems.add(c)
                for(c in ColorTemplate.VORDIPLOM_COLORS) colorItems.add(c)
                for(c in ColorTemplate.MATERIAL_COLORS) colorItems.add(c)
                colorItems.add(ColorTemplate.getHoloBlue())

                val pieDataSet= PieDataSet(entries,"")

                pieDataSet.apply {
                    colors=colorItems
                    valueTextColor= Color.BLACK
                    valueTextSize=12f
                }

                val pieData= PieData(pieDataSet)
                pieChart.apply {
                    data=pieData
                    description.isEnabled=false
                    isRotationEnabled=false
                    centerText="수면 비율"
                    setCenterTextSize(20f)
                    setEntryLabelColor(Color.BLACK)
                    animateY(1400, Easing.EaseInOutQuad)
                    animate()
                }
            }
        })
    }
    private fun AwakeDrawingGraph(v:View){
        var barChart: BarChart
        barChart=v.findViewById<BarChart>(R.id.sleep_graph)
        barChart.apply {
            description.isEnabled=false
            // setMaxVisibleValueCount(30) //최대 보이는 그래프 개수
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
        var REM_set: BarDataSet
        var lightSleep_set:BarDataSet
        var deepSleep_set:BarDataSet
        var Awake_set:BarDataSet
        //values.add(BarEntry(0.0f,7.0f))

        val day = findDate1()
        val Ref_day = db.collection(userkey).document(day)
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
                        Awake_values.add(BarEntry(i.toFloat(),10f))
                    }
                    else if(cycles[i]=="sleep_rem"){
                        REM_values.add(BarEntry(i.toFloat(),4f))
                    }
                    else if(cycles[i]=="sleep_light"){
                        lightSleep_values.add(BarEntry(i.toFloat(),6f))
                    }
                    else if(cycles[i]=="sleep_deep"){
                        deepSleep_values.add(BarEntry(i.toFloat(),1f))
                    }
                }
                type.add("01:00")
                for(i in 0..cycles.size-3){
                    type.add("")
                }
                type.add("09:00")


                colorlist.add(Color.parseColor("#FFFF88"))  //light
                colorlist.add(Color.parseColor("#88ffff"))    //deep
                colorlist.add(Color.parseColor("#88ff88"))  //REM
                colorlist.add(Color.parseColor("#FFFA8072"))  //Awake

                if(barChart.data!=null && barChart.data.dataSetCount>1){
                    val chartData=barChart.data
                    REM_set=chartData?.getDataSetByIndex(0) as BarDataSet
                    lightSleep_set=chartData?.getDataSetByIndex(1) as BarDataSet
                    deepSleep_set=chartData?.getDataSetByIndex(2) as BarDataSet
                    Awake_set=chartData?.getDataSetByIndex(3) as BarDataSet
                    REM_set.values=REM_values
                    lightSleep_set.values=lightSleep_values
                    deepSleep_set.values=deepSleep_values
                    Awake_set.values=Awake_values
                    chartData.notifyDataChanged()
                    barChart.notifyDataSetChanged()
                }
                else{
                    REM_set= BarDataSet(REM_values,"REM")
                    REM_set.setColor(colorlist.get(2))
                    REM_set.setDrawValues(false)

                    lightSleep_set= BarDataSet(lightSleep_values,"lightsleep")
                    //lightSleep_set.colors=colorlist
                    lightSleep_set.setColor(colorlist.get(0))
                    lightSleep_set.setDrawValues(false)

                    deepSleep_set= BarDataSet(deepSleep_values,"deepsleep")
                    //deepSleep_set.colors=colorlist
                    deepSleep_set.setColor(colorlist.get(1))
                    deepSleep_set.setDrawValues(false)

                    Awake_set= BarDataSet(Awake_values,"Awake")
                    //Awake_set.colors=colorlist
                    Awake_set.setColor(colorlist.get(3))
                    Awake_set.setDrawValues(false)

                    val dataSets=ArrayList<IBarDataSet>()
                    dataSets.add(REM_set)
                    dataSets.add(lightSleep_set)
                    dataSets.add(deepSleep_set)
                    dataSets.add(Awake_set)

                    val data= BarData(dataSets)
                    barChart.data=data
                    barChart.setFitBars(false)

                    //x축 설정
                    val xAxis=barChart.xAxis
                    xAxis.apply{
                        setDrawGridLines(false)
                        isEnabled=true
                        position=XAxis.XAxisPosition.BOTTOM
                        setDrawAxisLine(true)
                        granularity=1f
                        isGranularityEnabled=true
                        valueFormatter=IndexAxisValueFormatter(type)
                        textSize=12f
                        textColor=Color.WHITE
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

    }
}


fun findDate2(): String {
    val cal = Calendar.getInstance()
    cal.time = Date()
    val df: DateFormat = SimpleDateFormat("yyyy년 MM월 dd일")
    cal.add(Calendar.DATE,-1)
    return df.format(cal.time)
}

fun findDate1(): String {
    val cal = Calendar.getInstance()
    cal.time = Date()
    val df: DateFormat = SimpleDateFormat("yyyy-MM-dd")
    cal.add(Calendar.DATE,-1)
    return df.format(cal.time)
}