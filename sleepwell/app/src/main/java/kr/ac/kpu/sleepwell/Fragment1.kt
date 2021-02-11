package kr.ac.kpu.sleepwell

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate

class Fragment1 : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
}