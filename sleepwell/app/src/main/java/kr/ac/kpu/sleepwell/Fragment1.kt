package kr.ac.kpu.sleepwell

import android.content.ContentValues.TAG
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


private const val LOG_TAG = "Error"
val db = Firebase.firestore
val user = FirebaseAuth.getInstance()
val userkey = user.uid.toString()
var isRunning=true

class Fragment1 : Fragment() {
    var sleepDatalist=ArrayList<dayrecordData>()
    /*private var REM_values=ArrayList<BarEntry>()
    private var deepSleep_values=ArrayList<BarEntry>()
    private var lightSleep_values=ArrayList<BarEntry>()
    private var Awake_values=ArrayList<BarEntry>()
    private val colorlist=ArrayList<Int>()*/

    //private lateinit var block_btn:Button
    private var getfilesize:Int=0
    private var mediaPlayer: MediaPlayer?=null
    private var pausePosition:Int?=null
    private var isPaused:Boolean=false
    var arraylist=ArrayList<String>(100)   //녹음파일 이름 저장(output2)
    var documentnames=ArrayList<String>()
    private lateinit var callback: OnBackPressedCallback
    private var checkinglist:Boolean=false
    lateinit var ref2:QueryDocumentSnapshot

    //get data


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var v:View=inflater.inflate(R.layout.fragment_1, container, false)

        val sleepdataAdapter= activity?.let { dayrecordlistAdapter(it,sleepDatalist) }
        val block_btn:Button=v.findViewById<Button>(R.id.block_btn)
        var frag_daylistview=v.findViewById<ListView>(R.id.daylistview)
        frag_daylistview.adapter=sleepdataAdapter
/*        var calx = Calendar.getInstance()
        calx.time = Date()
        val dfx: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        calx.add(Calendar.DATE,-1)
        var daily = "dfsdf"
        daily= dfx.format(calx.time)
        day.setText(daily)//하루 치 결과를 보여줌*/

        /*val day = findDate1()
        val Ref_day = db.collection(userkey).document(day)
        Ref_day.addSnapshotListener(EventListener<DocumentSnapshot> {snapshot,e->
            if(e != null){
            Log.w(tag, "Listen failed.", e)
            return@EventListener
            }
            if(snapshot != null && snapshot.exists()){

                val day2 = findDate2()
                //today.setText(day2)

                var sleep_time = snapshot?.data!!["sleep_time"].toString()  //몇분 잤는지
                var sleep_start = snapshot?.data!!["go_to_bed"].toString()  //자러 간 시간
                var sleep_deep = snapshot?.data!!["sleep_deep"].toString()  //깊은수면
                var sleep_light = snapshot?.data!!["sleep_light"].toString()    //얕은수면
                var sleep_rem = snapshot?.data!!["sleep_rem"].toString()    //램수면
                var go_to_sleep = snapshot?.data!!["go_to_sleep"].toString()

                /*changeSleep(sleep_time)
                changeDeep(sleep_deep)
                changeLight(sleep_light)
                changeRem(sleep_rem)
                changeGotoSleep(go_to_sleep)
                sleep_st.setText(sleep_start)*/

                if(snapshot?.data!!["sleep_time"] != null){
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
            }
        })*/

        db.collection(userkey)
                .addSnapshotListener{value, e ->
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e)
                        return@addSnapshotListener
                    }
                    if(value!!.size()==0){
                        block_btn.visibility=View.VISIBLE
                        Log.d("valuesize",value.size().toString())
                        sleepdataAdapter?.notifyDataSetChanged()
                    }
                    else{
                        block_btn.visibility=View.GONE
                        Log.d("valuesize",value.size().toString())
                        for (document in value!!) {
                            Log.d("TAG", "${document.id} => ${document.data}")
                            checkinglist=false
                            for(i in 0 until sleepDatalist.size) {
                                if (document.id.replace("-", "/").equals(sleepDatalist.get(i).sleep_date)) {
                                    Log.d("alreadyhave", "data added error")
                                    //update
                                    sleepDatalist.get(i).sleeptime=document.data["sleep_time"].toString()
                                    sleepDatalist.get(i).startsleeptime=document.data["go_to_bed"].toString()
                                    sleepDatalist.get(i).timetotakesleeptime=document.data["go_to_sleep"].toString()
                                    sleepDatalist.get(i).finishsleep=document.data["wake_up"].toString()
                                    sleepDatalist.get(i).sleep_date=document.id.replace("-","/")
                                    sleepDatalist.get(i).Rem_sleep=document.data["sleep_rem"].toString()
                                    sleepDatalist.get(i).deep_sleep=document.data["sleep_deep"].toString()
                                    sleepDatalist.get(i).light_sleep=document.data["sleep_light"].toString()
                                    sleepDatalist.get(i).awake_sleep=document.data["awake"].toString()
                                    checkinglist = true
                                    break
                                }
                            }
                            if(!checkinglist){
                                sleepDatalist.add(dayrecordData(
                                        document.data["sleep_time"].toString(),
                                        document.data["go_to_bed"].toString(),
                                        document.data["go_to_sleep"].toString(),
                                        document.data["wake_up"].toString(),
                                        document.id.replace("-","/"),
                                        document.data["sleep_rem"].toString(),
                                        document.data["sleep_deep"].toString(),
                                        document.data["sleep_light"].toString(),
                                        document.data["awake"].toString()
                                ))
                                sleepdataAdapter?.notifyDataSetChanged()
                            }
                            sleepdataAdapter?.notifyDataSetChanged()
                        }
                        sleepdataAdapter?.notifyDataSetChanged()
                    }
                    sleepdataAdapter?.notifyDataSetChanged()
                }
        return v
    }

/* private fun changeSleep(x :String){
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
    }*/

    /*
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
                if(snapshot?.data!!["awake"] != null){
                    val day2 = findDate2()
                    today.setText(day2)

                    var awake = snapshot?.data!!["awake"].toString().toInt()
                    var sleep_deep = snapshot?.data!!["sleep_deep"].toString().toInt()
                    var sleep_light = snapshot?.data!!["sleep_light"].toString().toInt()
                    var sleep_rem = snapshot?.data!!["sleep_rem"].toString().toInt()

                    awak = kr.ac.kpu.sleepwell.awake.toFloat()
                    rem = kr.ac.kpu.sleepwell.sleep_rem.toFloat()
                    deep = kr.ac.kpu.sleepwell.sleep_deep.toFloat()
                    light = kr.ac.kpu.sleepwell.sleep_light.toFloat()

                    var pieChart: PieChart
                    pieChart=v.findViewById<PieChart>(R.id.day_piechart)
                    pieChart.setUsePercentValues(true)
                    val entries=ArrayList<PieEntry>()
                    if(rem > 0f){entries.add(PieEntry(rem,"REM"))}
                    if(deep > 0f){entries.add(PieEntry(deep,"Deep Sleep"))}
                    if(light > 0f){entries.add(PieEntry(light,"Light Sleep"))}
                    if(kr.ac.kpu.sleepwell.awake > 0f){ entries.add(PieEntry(awak,"Awake"))}

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
            }
        })
    }*/

    /*private fun AwakeDrawingGraph(v:View){
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
                type.add("04:00")
                type.add("05:00")
                type.add("06:00")
                type.add("07:00")
                type.add("08:00")


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

    }*/
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