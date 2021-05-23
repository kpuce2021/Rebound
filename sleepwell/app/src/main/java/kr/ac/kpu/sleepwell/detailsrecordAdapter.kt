package kr.ac.kpu.sleepwell

import android.content.Context
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import org.w3c.dom.Text
import java.io.FileInputStream

class detailsrecordAdapter(val context: Context, val audiodatalist:ArrayList<detailsRecorddata>): BaseAdapter() {

    private var mediaPlayer: MediaPlayer?=null
    private var isPaused:Boolean=false

    override fun getView(position: Int, convertview: View?, parent: ViewGroup?): View {
        val view: View = LayoutInflater.from(context).inflate(R.layout.details_musicbar, null)

        var tv_number=view.findViewById<TextView>(R.id.tv_numbertext)
        var btn_play=view.findViewById<Button>(R.id.btn_play)
        var btn_playagain=view.findViewById<Button>(R.id.btn_playagain)

        val audiodata = audiodatalist[position]

        tv_number.setText(audiodata.playnumnber)
        var audiopath=audiodata.audiopath

        //재생시작
        btn_play.setOnClickListener{
            btn_play.setBackgroundResource(R.drawable.ic_baseline_pause_24)
            val fis= FileInputStream(audiopath)
            if(mediaPlayer!=null){
                mediaPlayer!!.release()
            }
            try{
                mediaPlayer= MediaPlayer().apply {
                    //재생이 멈췄을 때
                    setOnCompletionListener {
                        btn_play.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24)
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
        //다시재생
        btn_playagain.setOnClickListener{
            if(mediaPlayer!=null && !mediaPlayer!!.isPlaying &&isPaused==true){
                mediaPlayer?.apply {
                    start()
                    //pausePosition?.let { seekTo(it) }
                    isPaused=false
                }
            }
        }
        return view
    }

    override fun getItem(position: Int): Any {
        return audiodatalist[position]
    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }

    override fun getCount(): Int {
        return audiodatalist.size
    }
}