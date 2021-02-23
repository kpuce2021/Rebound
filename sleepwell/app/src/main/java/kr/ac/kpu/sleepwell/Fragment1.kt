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


    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                var transaction: FragmentTransaction =requireActivity().supportFragmentManager.beginTransaction()
                var fragment0:Fragment=Fragment0()
                transaction.replace(R.id.Day_resultFrag,fragment0)
                transaction.commit()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }
    override fun onDetach() {
        super.onDetach()
        callback.remove()
    }

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
    //다시재생(멈춘순간부터)

}