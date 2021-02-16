package kr.ac.kpu.sleepwell

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment



class Fragment2 : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var v:View=inflater.inflate(R.layout.fragment_2, container, false)

        childFragmentManager.beginTransaction().replace(R.id.childFragment,Trend_childFrag1_week()).commit()

        var btn_week1:Button=v.findViewById(R.id.btn_week) as Button
        var btn_month1:Button=v.findViewById(R.id.btn_month) as Button
        var btn_all1:Button=v.findViewById(R.id.btn_all) as Button

        btn_week1.setOnClickListener {
            childFragmentManager.beginTransaction().replace(R.id.childFragment,Trend_childFrag1_week()).commit()
        }
        btn_month1.setOnClickListener {
            childFragmentManager.beginTransaction().replace(R.id.childFragment,Trend_childFrag2_month()).commit()
        }
        btn_all1.setOnClickListener {
            childFragmentManager.beginTransaction().replace(R.id.childFragment,Trend_childFrag3_all()).commit()
        }
        return v
    }
}

