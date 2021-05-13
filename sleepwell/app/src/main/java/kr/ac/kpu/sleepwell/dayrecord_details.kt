package kr.ac.kpu.sleepwell

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
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
import kotlinx.android.synthetic.main.activity_dayrecord_details.*
import kotlinx.android.synthetic.main.activity_dayrecord_details.awake_barchart

class dayrecord_details : AppCompatActivity() {

    val db = Firebase.firestore
    val user = FirebaseAuth.getInstance()
    val userkey = user.uid.toString()
    var date_id:String=""
    private var REM_values=ArrayList<BarEntry>()
    private var deepSleep_values=ArrayList<BarEntry>()
    private var lightSleep_values=ArrayList<BarEntry>()
    private var Awake_values=ArrayList<BarEntry>()
    private val colorlist=ArrayList<Int>()
    private var awak = 0f
    private var rem = 0f
    private var deep = 0f
    private var light = 0f
    private var ratio_rem:String=""
    private var ratio_awake:String=""
    private var ratio_deep:String=""
    private var ratio_light:String=""

    private var audiodatalist=ArrayList<detailsRecorddata>()    //musicbar
    private var audiopathlist = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dayrecord_details)

        val intent: Intent =getIntent()
        date_id= intent.getStringExtra("date_id").toString()
        //var ratio_rem=intent.getStringExtra("ratio_rem").toString()
        //var ratio_awake=intent.getStringExtra("ratio_awake").toString()
        //var ratio_light=intent.getStringExtra("ratio_light").toString()
        //var ratio_deep=intent.getStringExtra("ratio_deep").toString()

        Log.d("date_id",date_id)

        if (date_id != null) {
            id_date.text = date_id.replace("-"," / ")
        }

        /*val Ref_day = db.collection(userkey).document(date_id)
        Ref_day.addSnapshotListener(EventListener<DocumentSnapshot> { snapshot, e ->
            if (e != null) {
                Log.w("tag", "Listen failed.", e)
                return@EventListener
            }
            if (snapshot != null && snapshot.exists()) {

                var awake = snapshot?.data!!["awake"].toString().toInt()
                var sleep_deep = snapshot?.data!!["sleep_deep"].toString().toInt()
                var sleep_light = snapshot?.data!!["sleep_light"].toString().toInt()
                var sleep_rem = snapshot?.data!!["sleep_rem"].toString().toInt()

                awak = awake.toFloat()
                rem = sleep_rem.toFloat()
                deep = sleep_deep.toFloat()
                light = sleep_light.toFloat()
            }
        })*/

        val audiodataAdapter= detailsrecordAdapter(this,audiodatalist)
        recordlistview.adapter=audiodataAdapter

        val Ref_day = db.collection(userkey).document(date_id)
        Ref_day.addSnapshotListener(EventListener<DocumentSnapshot> {snapshot,e->
            if(e != null){
                Log.w("tag", "Listen failed.", e)
                return@EventListener
            }
            if(snapshot != null && snapshot.exists()){
                var ratio_deep = snapshot?.data!!["sleep_deep"].toString() //깊은수면
                var ratio_light = snapshot?.data!!["sleep_light"].toString() //얕은수면
                var ratio_rem = snapshot?.data!!["sleep_rem"].toString() //램수면
                var ratio_awake = snapshot?.data!!["awake"].toString()

                Log.d("awak",ratio_awake)
                Log.d("rem",ratio_rem)
                Log.d("deep",ratio_deep)
                Log.d("light",ratio_light)

                awak = ratio_awake.toFloat()
                rem = ratio_rem.toFloat()
                deep = ratio_deep.toFloat()
                light = ratio_light.toFloat()
                rem_time.text= rem.toInt().toString()+" min"
                SleepcycleCheck_piechart(details_deep_piechart2,rem,deep,light,awak)
            }
        })

        val Ref_alarm = db.collection("alarm").document(userkey)
        Ref_alarm.addSnapshotListener(EventListener<DocumentSnapshot> {snapshot,e->
            if(e != null){
                Log.w("tag", "Listen failed.", e)
                return@EventListener
            }
            if(snapshot != null && snapshot.exists()){
                var alarm_t = snapshot?.data!!["alarm"].toString() //알람 시간
                var use = snapshot?.data!!["use_alarm"].toString() //알람 여부
                if(use == "true"){
                    alarm_time.text = alarm_t
                }
            }
        })



        val getdecibelfromdb = db.collection(userkey).document(date_id).collection("record").document("decibel")
        getdecibelfromdb.addSnapshotListener(EventListener<DocumentSnapshot> { snapshot, e ->
            if (e != null) {
                Log.w("tag", "Listen failed.", e)
                return@EventListener
            }
            if (snapshot != null && snapshot.exists()) {

                var MaxDecibel = snapshot?.data!!["MaxDecibel"].toString()
                //var MinDecibel = snapshot?.data!!["MinDecibel"].toString().toInt()
                //var olimDecibel=round(MaxDecibel.toFloat()*10)/10
                maxDecibel.text= Math.round(MaxDecibel.toFloat()).toString() + " db"
            }
        })
        //val getaudiopathfromdb = db.collection(userkey).document(date_id).collection("record").document("audiopath")
        /*getaudiopathfromdb.addSnapshotListener(EventListener<DocumentSnapshot> { snapshot, e ->
            if (e != null) {
                Log.w("tag", "Listen failed.", e)
                return@EventListener
            }
            if (snapshot != null && snapshot.exists()) {
                var size = snapshot?.data!!["size"].toString()
                Log.d("justsize",size)
                for(i in 0 until size.toInt()) {
                    Log.d("audiosnapshot", snapshot?.data!!["audio $i"].toString())
                    audiodatalist.add(detailsRecorddata(
                            snapshot?.data!!["audio $i"].toString(),
                            "play "+(i+1).toString()
                    ))
                }
            }
        })*/
        val factor_day = db.collection(userkey).document(date_id)
        factor_day.addSnapshotListener(EventListener<DocumentSnapshot>{snapshot, e->
            if(e != null){
                Log.w("tag", "Listen failed.", e)
            }
            if(snapshot != null && snapshot.exists()){
                //var strFactor = ""
                //var dbitems = arrayListOf<String>()
                if (snapshot?.data!!["alcohol"].toString() == "true") {
                    tv_sleepfactor.visibility=View.VISIBLE
                    sleepfactor_layout.visibility=View.VISIBLE
                    image_windbar.visibility= View.VISIBLE
                }
                if (snapshot?.data!!["caffeine"].toString() == "true") {
                    tv_sleepfactor.visibility=View.VISIBLE
                    sleepfactor_layout.visibility=View.VISIBLE
                    image_smoking.visibility= View.VISIBLE
                }
                if (snapshot?.data!!["cold"].toString() == "true") {
                    tv_sleepfactor.visibility=View.VISIBLE
                    sleepfactor_layout.visibility=View.VISIBLE
                    image_caffaine.visibility= View.VISIBLE
                }
                if (snapshot?.data!!["food"].toString() == "true") {
                    tv_sleepfactor.visibility=View.VISIBLE
                    sleepfactor_layout.visibility=View.VISIBLE
                    image_retaurant.visibility= View.VISIBLE
                }
                if (snapshot?.data!!["other_bed"].toString() == "true") {
                    tv_sleepfactor.visibility=View.VISIBLE
                    sleepfactor_layout.visibility=View.VISIBLE
                    image_exercise.visibility= View.VISIBLE
                }
                if (snapshot?.data!!["pill"].toString() == "true") {
                    tv_sleepfactor.visibility=View.VISIBLE
                    sleepfactor_layout.visibility=View.VISIBLE
                    image_cold.visibility= View.VISIBLE
                }
                if (snapshot?.data!!["shower"].toString() == "true") {
                    tv_sleepfactor.visibility=View.VISIBLE
                    sleepfactor_layout.visibility=View.VISIBLE
                    image_sleeppill.visibility= View.VISIBLE
                }
                if (snapshot?.data!!["smoke"].toString() == "true") {
                    tv_sleepfactor.visibility=View.VISIBLE
                    sleepfactor_layout.visibility=View.VISIBLE
                    image_shower.visibility= View.VISIBLE
                }
                if (snapshot?.data!!["work_out"].toString() == "true") {
                    tv_sleepfactor.visibility=View.VISIBLE
                    sleepfactor_layout.visibility=View.VISIBLE
                    image_anotherbed.visibility= View.VISIBLE
                }
            }
        })
        AwakeDrawingGraph(awake_barchart)
    }


    fun SleepcycleCheck_piechart(pieChart: PieChart,rem:Float,deep:Float,light:Float,awake:Float){

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
            valueTextSize=10f
        }

        val pieData= PieData(pieDataSet)
        pieChart.apply {
            data=pieData
            description.isEnabled=false
            isRotationEnabled=false
            centerText="수면 비율"
            setCenterTextSize(15f)
            setEntryLabelColor(Color.WHITE)
            animateY(1400, Easing.EaseInOutQuad)
            animate()
        }
    }

    private fun AwakeDrawingGraph(barChart: BarChart){
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
        var lightSleep_set: BarDataSet
        var deepSleep_set: BarDataSet
        var Awake_set: BarDataSet
        //values.add(BarEntry(0.0f,7.0f))

        val day = date_id
        val cycleRef = db.collection(userkey).document(day).collection("cycle").document("cycle")
        cycleRef.addSnapshotListener(EventListener<DocumentSnapshot> { snapshot, e->
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
                    if(cycles[i]=="sleep_rem"){
                        REM_values.add(BarEntry(i.toFloat(),4f))
                    }
                    if(cycles[i]=="sleep_light"){
                        lightSleep_values.add(BarEntry(i.toFloat(),6f))
                    }
                    if(cycles[i]=="sleep_deep"){
                        deepSleep_values.add(BarEntry(i.toFloat(),1f))
                    }
                }
                // type.add("12:00")
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
                        position= XAxis.XAxisPosition.BOTTOM
                        setDrawAxisLine(true)
                        granularity=1f
                        isGranularityEnabled=true
                        valueFormatter= IndexAxisValueFormatter(type)
                        textSize=12f
                        textColor= ContextCompat.getColor(applicationContext,R.color.white)
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