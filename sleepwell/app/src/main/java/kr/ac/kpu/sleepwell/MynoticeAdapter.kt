package kr.ac.kpu.sleepwell

import android.R
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import org.w3c.dom.Text


class MynoticeAdapter : BaseAdapter() {
    var mContext: Context? = null
    var mLayoutInflater: LayoutInflater? = null
    var sample: ArrayList<noticeData>? = null

    override fun getView(position:Int,converView:View , parent:ViewGroup): View {
        TODO()
    }

    override fun getItem(position: Int): Any {
        return sample!!.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return sample!!.size
    }
}