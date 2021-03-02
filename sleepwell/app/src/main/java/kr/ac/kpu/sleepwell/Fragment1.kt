package kr.ac.kpu.sleepwell

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
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
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

        val Ref = db.collection(userkey)
        val day = findDate()
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

                var sleep_time = snapshot?.data!!["sleep_time"].toString()
                var sleep_start = snapshot?.data!!["go_to_bed"].toString()
                var sleep_deep = snapshot?.data!!["sleep_deep"].toString()
                var sleep_light = snapshot?.data!!["sleep_light"].toString()

                var sleep_h = sleep_time.toInt()/60
                var sleep_h2 = ((sleep_time.toDouble()/60 - sleep_h.toDouble())*60).toInt()

                if(sleep_h==0){
                    sleep_t.setText(sleep_time+"분")
                }
                else{
                    sleep_t.setText(sleep_h.toString()+"시간 "+ sleep_h2.toString()+"분" )
                }
                sleep_st.setText(sleep_start)
                sleep_d.setText(sleep_deep+"시간")
                sleep_m.setText(sleep_light+"시간")
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

        SleepcycleCheck(v)
        return v
    }
}


fun SleepcycleCheck(v:View){

    var pieChart: PieChart
    pieChart=v.findViewById<PieChart>(R.id.day_piechart)

    pieChart.setUsePercentValues(true)
    val entries=ArrayList<PieEntry>()
    entries.add(PieEntry(0f,"Awake"))
    entries.add(PieEntry(0f,"Light Sleep"))
    entries.add(PieEntry(0f,"Deep Sleep"))

    val colorItems=ArrayList<Int>()
    for(c in ColorTemplate.VORDIPLOM_COLORS) colorItems.add(c)
    for(c in ColorTemplate.PASTEL_COLORS) colorItems.add(c)
    for(c in ColorTemplate.LIBERTY_COLORS) colorItems.add(c)
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
        centerText="수면 비율"
        setCenterTextSize(20f)
        setEntryLabelColor(Color.BLACK)
        animateY(1400, Easing.EaseInOutQuad)
        animate()
    }
}

fun findDate2(): String {
    val cal = Calendar.getInstance()
    cal.time = Date()
    val df: DateFormat = SimpleDateFormat("yyyy년 MM월 dd일")
    cal.add(Calendar.DATE,-1)
    return df.format(cal.time)
}

fun findDate(): String {
    val cal = Calendar.getInstance()
    cal.time = Date()
    val df: DateFormat = SimpleDateFormat("yyyy-MM-dd")
    cal.add(Calendar.DATE,-1)
    return df.format(cal.time)
}