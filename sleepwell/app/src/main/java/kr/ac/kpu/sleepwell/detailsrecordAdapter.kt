package kr.ac.kpu.sleepwell

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import org.w3c.dom.Text

class detailsrecordAdapter(val context: Context, val audiodatalist:ArrayList<detailsRecorddata>): BaseAdapter() {
    override fun getView(position: Int, convertview: View?, parent: ViewGroup?): View {
        val view: View = LayoutInflater.from(context).inflate(R.layout.details_musicbar, null)

        var tv_number=view.findViewById<TextView>(R.id.tv_numbertext)
        var btn_play=view.findViewById<Button>(R.id.btn_play)
        var btn_playagain=view.findViewById<Button>(R.id.btn_playagain)

        val audiodata = audiodatalist[position]
        return view
    }

    override fun getItem(p0: Int): Any {
        TODO("Not yet implemented")
    }

    override fun getItemId(p0: Int): Long {
        TODO("Not yet implemented")
    }

    override fun getCount(): Int {
        TODO("Not yet implemented")
    }
}