package kr.ac.kpu.sleepwell

import android.graphics.Color
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_day_result_a_c.*
import kotlinx.android.synthetic.main.fragment_0.view.*
import kotlinx.android.synthetic.main.fragment_1.*
import kotlinx.android.synthetic.main.fragment_trend_child_frag1_week.*
import java.io.FileInputStream
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class Day_resultAC : AppCompatActivity() {
    val db = Firebase.firestore
    val user = FirebaseAuth.getInstance()
    val userkey = user.uid.toString()
    //rivate lateinit var mFirebaseStorage: FirebaseStorage

    private val random=Random()
    private var daynow:String=""
    private var userEmail:String=""
    private var getfilesize:Int=0
    private var mediaPlayer: MediaPlayer?=null
    private var pausePosition:Int?=null
    private var isPaused:Boolean=false
    private var getvisiblesize:Int=0
    private var btn_pressedcheck: Array<Boolean> = arrayOf(false,false,false,false,false,false,false,false,false,false,false)
    private lateinit var storageRef: StorageReference
    private var REM_values=ArrayList<BarEntry>()
    private var deepSleep_values=ArrayList<BarEntry>()
    private var lightSleep_values=ArrayList<BarEntry>()
    private var Awake_values=ArrayList<BarEntry>()
    private val colorlist=ArrayList<Int>()
    private lateinit var Arrayplaybutton:Array<Button>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_day_result_a_c)

        val myapp:MyglobalArraylist=application as MyglobalArraylist
        val day2 = findDate2()
        day_a.setText(day2)

        val Ref = db.collection(userkey)
        val day = findDate()
        val Ref_day = db.collection(userkey).document(day)
        Ref_day.addSnapshotListener(EventListener<DocumentSnapshot>{snapshot, e->
            if(e != null){
                Log.w("tag", "Listen failed.", e)
            }
            if(snapshot != null && snapshot.exists()){
                var strFactor = ""
                var dbitems = arrayListOf<String>()
                if (snapshot?.data!!["alcohol"].toString() == "true") {
                    dbitems.add("알코올")
                }
                if (snapshot?.data!!["caffeine"].toString() == "true") {
                    dbitems.add("카페인")
                }
                if (snapshot?.data!!["cold"].toString() == "true") {
                    dbitems.add("감기")
                }
                if (snapshot?.data!!["food"].toString() == "true") {
                    dbitems.add("야식")
                }
                if (snapshot?.data!!["other_bed"].toString() == "true") {
                    dbitems.add("다른 침대")
                }
                if (snapshot?.data!!["pill"].toString() == "true") {
                    dbitems.add("수면 보조제")
                }
                if (snapshot?.data!!["shower"].toString() == "true") {
                    dbitems.add("샤워")
                }
                if (snapshot?.data!!["smoke"].toString() == "true") {
                    dbitems.add("흡연")
                }
                if (snapshot?.data!!["work_out"].toString() == "true") {
                    dbitems.add("운동")
                }
                if (dbitems.size > 0) {
                    for (i in 0..dbitems.size - 1) {
                        var x = dbitems.get(i)
                        if (i==dbitems.size-1){
                            strFactor = strFactor.plus(x)
                        }
                        else{
                            strFactor = strFactor.plus(x + ", ")
                        }
                        sleep_factor_a.setText(strFactor)
                    }
                }

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
                changeGotoSleep(go_to_sleep, sleep_time)
                sleep_st_a.setText(sleep_start)
                //sleep_d_a.setText(sleep_deep+"시간")
                //sleep_m_a.setText(sleep_light+"시간")
            }
        })

        AwakeDrawingGraph(awake_barchart)
        SleepcycleCheck(day_result_piechart)
        /*필요 없어 보여서 주석 처리
        awakestatepiechartDrawing(awake_state)
        lightsleeppiechartDrawing(light_state)
        deepsleepstatepiechartDrawing(deep_state)*/

        //firebase로 이메일 가져오기
        val user=Firebase.auth.currentUser
        if(user!=null){
            user?.let {
                for(profile in it.providerData){
                    userEmail=profile.email.toString()
                }
            }
        }
        else
            Toast.makeText(this,"로그인 되지 않았습니다.",Toast.LENGTH_SHORT).show()

        var playlayoutArray: Array<LinearLayout> = arrayOf(findViewById(R.id.noceum1),
            findViewById(R.id.noceum2),findViewById(R.id.noceum3),findViewById(R.id.noceum4) ,
            findViewById(R.id.noceum5),findViewById(R.id.noceum6),findViewById(R.id.noceum7),
            findViewById(R.id.noceum8),findViewById(R.id.noceum9),findViewById(R.id.noceum10),findViewById(R.id.noceum11))

        var timeArray: Array<TextView> = arrayOf(findViewById(R.id.text_time),
                findViewById(R.id.text_time2),findViewById(R.id.text_time3),findViewById(R.id.text_time4),
                findViewById(R.id.text_time5),findViewById(R.id.text_time6),findViewById(R.id.text_time7),
                findViewById(R.id.text_time8),findViewById(R.id.text_time9),findViewById(R.id.text_time10),findViewById(R.id.text_time11))

        Arrayplaybutton = arrayOf(findViewById(R.id.btn_play),
            findViewById(R.id.btn_play22),
            findViewById(R.id.btn_play33),findViewById(R.id.btn_play44),findViewById(R.id.btn_play44),
            findViewById(R.id.btn_play55),findViewById(R.id.btn_play66),findViewById(R.id.btn_play77),
            findViewById(R.id.btn_play88),findViewById(R.id.btn_play99),findViewById(R.id.btn_play10),findViewById(R.id.btn_play11))

        var ArrayPlayAgainbutton: Array<Button> = arrayOf(findViewById(R.id.btn_playagain),
            findViewById(R.id.btn_playagain22),findViewById(R.id.btn_playagain33),findViewById(R.id.btn_playagain44),
            findViewById(R.id.btn_playagain55),findViewById(R.id.btn_playagain66),findViewById(R.id.btn_playagain77),
            findViewById(R.id.btn_playagain88),findViewById(R.id.btn_playagain99),findViewById(R.id.btn_playagain10),findViewById(R.id.btn_playagain11))

        //firebase storage start
        daynow=daytime()
        if(myapp.arraylist.size>0)
            text_noise.isVisible=true
        getfilesize=myapp.arraylist.size-1
        storageRef= FirebaseStorage.getInstance().reference
        val userFileRef=storageRef.child(userEmail).child(daynow)
        Log.d("fiearraylist_size",getfilesize.toString())
        for(i in 0..getfilesize){
            val filenameRef: StorageReference =userFileRef.child(" 녹음파일$i.mp3")
            filenameRef.putFile(Uri.fromFile(myapp.filearraylist.get(i))) }
        //firebase storage finish

        if(myapp.arraylist.size>3){
            getfilesize-=1
        }
        for(i in 0..getfilesize){
            timeArray[i].setText(myapp.timearraylist.get(i))
        }
        for(i in 0..getfilesize){
            playlayoutArray[i].isVisible=true
        }
        for(i in 0..getfilesize){
            //playlayoutArray[i].isVisible=true
            Arrayplaybutton[i].setOnClickListener {
                if(!btn_pressedcheck[i]){
                    playing(myapp.arraylist.get(i),i)
                    Log.d("number i",i.toString())
                    Arrayplaybutton[i].setBackgroundResource(R.drawable.ic_baseline_pause_24)
                    btn_pressedcheck[i]=true
                }
                else{
                    pausePlaying()
                    Arrayplaybutton[i].setBackgroundResource(R.drawable.ic_baseline_play_arrow_24)
                    btn_pressedcheck[i]=false
                }
            }
            ArrayPlayAgainbutton[i].setOnClickListener {
                playAgain()
                //Toast.makeText(this,"Play Again${i+1}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    fun SleepcycleCheck(pieChart: PieChart){
        var awak = 0f
        var rem = 0f
        var deep = 0f
        var light = 0f
        val day = findDate()
        val Ref_day = db.collection(userkey).document(day)
        Ref_day.addSnapshotListener(EventListener<DocumentSnapshot> {snapshot,e->
            if(e != null){
                Log.w("tag", "Listen failed.", e)
                return@EventListener
            }
            if(snapshot != null && snapshot.exists()){
                val day2 = findDate2()
                day_a.setText(day2)

                var awake = snapshot?.data!!["awake"].toString().toInt()
                var sleep_deep = snapshot?.data!!["sleep_deep"].toString().toInt()
                var sleep_light = snapshot?.data!!["sleep_light"].toString().toInt()
                var sleep_rem = snapshot?.data!!["sleep_rem"].toString().toInt()

                awak = awake.toFloat()
                rem = sleep_rem.toFloat()
                deep = sleep_deep.toFloat()
                light = sleep_light.toFloat()

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
                    setEntryLabelColor(Color.BLACK)
                    animateY(1400, Easing.EaseInOutQuad)
                    animate()
                }
            }
        })
    }
    private fun changeSleep(x :String){
        var hour = x.toInt()/60
        var minute = ((x.toDouble()/60 - hour.toDouble())*60).toInt()
        if(hour==0){
            sleep_t_a.setText(x+"분")
        }
        else{
            sleep_t_a.setText(hour.toString()+"시간 "+ minute.toString()+"분")
        }
    }

    private fun changeGotoSleep(x :String, y :String){
        var st = y.toInt()
        var hour = x.toInt()/60
        var minute = ((x.toDouble()/60 - hour.toDouble())*60).toInt()
        if (st < 5) {go_sleep_a.setText("0분")}
        else {
            if (hour == 0) {
                go_sleep_a.setText(x + "분")
            } else {
                go_sleep_a.setText(hour.toString() + "시간 " + minute.toString() + "분")
            }
        }
    }

    private fun changeRem(x :String){
        var hour = x.toInt()/60
        var minute = ((x.toDouble()/60 - hour.toDouble())*60).toInt()
        if(hour==0){
            sleep_r_a.setText(x+"분")
        }
        else{
            sleep_r_a.setText(hour.toString()+"시간 "+ minute.toString()+"분")
        }
    }

    private fun changeDeep(x :String){
        var hour = x.toInt()/60
        var minute = ((x.toDouble()/60 - hour.toDouble())*60).toInt()
        if(hour==0){
            sleep_d_a.setText(x+"분")
        }
        else{
            sleep_d_a.setText(hour.toString()+"시간 "+ minute.toString()+"분")
        }
    }

    private fun changeLight(x :String){
        var hour = x.toInt()/60
        var minute = ((x.toDouble()/60 - hour.toDouble())*60).toInt()
        if(hour==0){
            sleep_m_a.setText(x+"분")
        }
        else{
            sleep_m_a.setText(hour.toString()+"시간 "+ minute.toString()+"분")
        }
    }

    private fun daytime():String{
        var now:Long=System.currentTimeMillis()
        var mformat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        var mdate: Date = Date(now)
        return mformat.format(mdate)
    }
    private fun getTime():String{
        var now:Long=System.currentTimeMillis()
        var mformat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
        var mdate: Date = Date(now)
        return mformat.format(mdate)
    }
    //다시재생(멈춘순간부터)
    private fun playAgain(){
        if(mediaPlayer!=null && !mediaPlayer!!.isPlaying &&isPaused==true){
            mediaPlayer?.apply {
                start()
                pausePosition?.let { seekTo(it) }
                isPaused=false
            }
        }
    }
    private fun playing(path:String,index:Int){
        /*if(mediaPlayer!=null){
            mediaPlayer!!.release()
        }
        try{
            val fis=FileInputStream(path)
            mediaPlayer=MediaPlayer().apply {
                reset()
                setDataSource(fis.fd)
                prepare()
                start()
            }
        }catch(e:Exception){
            e.printStackTrace()
        }*/
        val fis= FileInputStream(path)
        if(mediaPlayer!=null){
            mediaPlayer!!.release()
        }
        try{
            mediaPlayer= MediaPlayer().apply {
                setOnCompletionListener {
                    Arrayplaybutton[index].setBackgroundResource(R.drawable.ic_baseline_play_arrow_24)
                }
                setDataSource(fis.fd)
                prepare()
                start()
                isPaused=false
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }
    //일시정지
    private fun pausePlaying(){
        if(mediaPlayer!=null && mediaPlayer!!.isPlaying){
            mediaPlayer?.apply {
                pause()
                isPaused=true
                pausePosition= mediaPlayer!!.currentPosition
                Log.d("pause check",":"+pausePosition)
            }
        }
    }
    fun findDate(): String {
        val cal = Calendar.getInstance()
        cal.time = Date()
        val df: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        var ampm = cal.get(Calendar.AM_PM)
        if(ampm == Calendar.PM){
            return df.format(cal.time)
        }
        else{cal.add(Calendar.DATE,-1)
            return df.format(cal.time) }
    }

    fun findDate2(): String {
        val cal = Calendar.getInstance()
        cal.time = Date()
        val df: DateFormat = SimpleDateFormat("yyyy년 MM월 dd일")
        cal.add(Calendar.DATE,-1)
        return df.format(cal.time)
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
        var lightSleep_set:BarDataSet
        var deepSleep_set:BarDataSet
        var Awake_set:BarDataSet
        //values.add(BarEntry(0.0f,7.0f))

        val day = findDate()
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
                        position=XAxis.XAxisPosition.BOTTOM
                        setDrawAxisLine(true)
                        granularity=1f
                        isGranularityEnabled=true
                        valueFormatter=IndexAxisValueFormatter(type)
                        textSize=12f
                        textColor=ContextCompat.getColor(applicationContext,R.color.white)
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


    /*이거 왜 한지 몰라서 주석 처리
    private fun awakestatepiechartDrawing(pieChart: PieChart){
        pieChart.setUsePercentValues(true)
        val entries=ArrayList<PieEntry>()

        entries.add(PieEntry(20f,"Awake State"))
        val pieDataSet= PieDataSet(entries,"")

        val colorItems=ArrayList<Int>()
        colorItems.add(colorlist.get(3))
        colorItems.add(R.color.background2)
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
            centerText="Awake"
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(20f)
            animateY(1400, Easing.EaseInOutQuad)
            animate()
        }
    }
    private fun lightsleeppiechartDrawing(pieChart: PieChart){
        pieChart.setUsePercentValues(true)
        val entries=ArrayList<PieEntry>()
        entries.add(PieEntry(60f))
        entries.add(PieEntry(40f))

        val colorItems=ArrayList<Int>()
        colorItems.add(colorlist.get(0))
        colorItems.add(R.color.background2)

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
            centerText="light"
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(20f)
            animateY(1400, Easing.EaseInOutQuad)
            animate()
        }
    }
    private fun deepsleepstatepiechartDrawing(pieChart: PieChart){
        pieChart.setUsePercentValues(true)
        val entries=ArrayList<PieEntry>()
        entries.add(PieEntry(40f))

        val pieDataSet= PieDataSet(entries,"")
        pieDataSet.apply {
            setColor(colorlist.get(1))  //그래프 색이랑 그래프안의 글자색 및 크기 조절
            valueTextColor= Color.BLACK
            valueTextSize=12f
        }
        val pieData= PieData(pieDataSet)
        pieChart.apply {
            data=pieData
            description.isEnabled=false
            isRotationEnabled=false
            centerText="deep"
            setEntryLabelColor(Color.WHITE)
            setEntryLabelTextSize(20f)
            animateY(1400, Easing.EaseInOutQuad)
            animate()
        }
    }*/