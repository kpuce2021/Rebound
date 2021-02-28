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
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentTransaction
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import java.io.FileInputStream
import java.lang.Exception
private const val LOG_TAG = "Error"
class Fragment1 : Fragment() {
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