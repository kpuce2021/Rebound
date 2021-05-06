package kr.ac.kpu.sleepwell

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_day_result_a_c.*

class dayrecordlistAdapter(val context: Context, val sleepDatalist: ArrayList<dayrecordData>):BaseAdapter() {

    private val db = Firebase.firestore
    private val user = FirebaseAuth.getInstance()
    private val userkey = user.uid.toString()

    private var ratio_rem: Int = 0 //렘수면 비율
    private var ratio_deep: Int = 0    //깊은수면 비율
    private var ratio_light: Int = 0   //얕은수면 비율
    private var ratio_awake: Int = 0   //뒤척인 수면 비율

    var Awake_values = ArrayList<BarEntry>()
    var REM_values = ArrayList<BarEntry>()
    var lightSleep_values = ArrayList<BarEntry>()
    var deepSleep_values = ArrayList<BarEntry>()
    val colorlist = ArrayList<Int>()

    private lateinit var REM_set: BarDataSet
    private lateinit var lightSleep_set: BarDataSet
    private lateinit var deepSleep_set: BarDataSet
    private lateinit var Awake_set: BarDataSet

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = LayoutInflater.from(context).inflate(R.layout.sleep_item, null)

        //view.minimumHeight = 600  //sleep_item.xml height
        //view.minimumWidth = 300


        val tv_date = view.findViewById<TextView>(R.id.tv_date)   //수면 날짜
        val details = view.findViewById<Button>(R.id.btn_details) //더보기버튼

        val sleep_ratio_piechart = view.findViewById<PieChart>(R.id.sleep_ratio_piechart) //수면비율그래프

        //val tv_startSleep = view.findViewById<TextView>(R.id.tv_startSleep)   //수면시작시간 12:00------
        //val tv_finishSleep = view.findViewById<TextView>(R.id.tv_finishSleep) //수면종료시간 ------8:00

        val tv_sleeptime = view.findViewById<TextView>(R.id.tv_sleeptime) // 수면시간 textview
        val tv_startsleeptime = view.findViewById<TextView>(R.id.tv_startsleeptime)   //수면시작시간 textview
        val tv_deepsleep = view.findViewById<TextView>(R.id.tv_deepsleep) //깊은수면 textview
        val tv_awakesleep = view.findViewById<TextView>(R.id.tv_awakesleep)   //뒤척인수면 textview

        val sleepdata = sleepDatalist[position]

        //바그래프 data
        ratio_rem = sleepdata.Rem_sleep.toInt()       //램수면
        ratio_deep = sleepdata.deep_sleep.toInt()     //깊은수면
        ratio_awake = sleepdata.awake_sleep.toInt()   //깬수면
        ratio_light = sleepdata.light_sleep.toInt()   //얉은수면

        tv_date.text = sleepdata.sleep_date
        //tv_startSleep.text = sleepdata.startsleeptime
        //tv_finishSleep.text = sleepdata.finishsleep
        tv_sleeptime.text = sleepdata.sleeptime + "분"
        tv_startsleeptime.text = sleepdata.startsleeptime
        tv_deepsleep.text = sleepdata.deep_sleep + "분"
        tv_awakesleep.text = sleepdata.awake_sleep + "%"

        details.setOnClickListener {
            val intent=Intent(context,dayrecord_details::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra("date_id",sleepdata.sleep_date.replace("/","-"))
            //intent.putExtra("ratio_rem",sleepdata.Rem_sleep.toInt())
            //intent.putExtra("ratio_deep",sleepdata.deep_sleep.toInt())
            //intent.putExtra("ratio_awake",sleepdata.awake_sleep.toInt())
            //intent.putExtra("ratio_light",sleepdata.light_sleep.toInt())
            context.startActivity(intent)
        }
        DrawingPiechart(sleep_ratio_piechart,ratio_rem,ratio_deep,ratio_light,ratio_awake)
        return view
    }

    override fun getItem(position: Int): Any {
        return sleepDatalist[position]
    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }

    override fun getCount(): Int {
        return sleepDatalist.size
    }

    fun DrawingPiechart(pieChart: PieChart,ratio_rem:Int,ratio_deep:Int,ratio_light:Int,ratio_awake:Int){
        var awak = 0f
        var rem = 0f
        var deep = 0f
        var light = 0f

        awak = ratio_awake.toFloat()
        rem = ratio_rem.toFloat()
        deep = ratio_deep.toFloat()
        light = ratio_light.toFloat()

        pieChart.setUsePercentValues(true)
        val entries=ArrayList<PieEntry>()
        if(rem > 0f){entries.add(PieEntry(rem,"REM"))}
        if(deep > 0f){entries.add(PieEntry(deep,"Deep Sleep"))}
        if(light > 0f){entries.add(PieEntry(light,"Light Sleep"))}
        if(awak > 0f){ entries.add(PieEntry(awak,"Awake"))}

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
}