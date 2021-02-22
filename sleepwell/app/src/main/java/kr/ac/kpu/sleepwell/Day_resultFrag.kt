package kr.ac.kpu.sleepwell

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
import androidx.core.view.isVisible
import java.io.FileInputStream
import java.lang.Exception

private const val LOG_TAG = "Error"
class Day_resultFrag : Fragment() {

    private var getfilesize:Int=0
    private var mediaPlayer: MediaPlayer?=null
    private var pausePosition:Int?=null
    private var isPaused:Boolean=false
    var arraylist=ArrayList<String>(100)   //녹음파일 이름 저장(output2)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        var v:View=inflater.inflate(R.layout.fragment_day_result, container, false)
        var playlayoutArray: Array<LinearLayout> = arrayOf(v.findViewById(R.id.noceum1) as LinearLayout,
                v.findViewById(R.id.noceum2) as LinearLayout,v.findViewById(R.id.noceum3) as LinearLayout,v.findViewById(R.id.noceum4) as LinearLayout,
                v.findViewById(R.id.noceum5) as LinearLayout,v.findViewById(R.id.noceum6) as LinearLayout,v.findViewById(R.id.noceum7) as LinearLayout,
                v.findViewById(R.id.noceum8) as LinearLayout,v.findViewById(R.id.noceum9) as LinearLayout,v.findViewById(R.id.noceum10) as LinearLayout)

        var Arrayplaybutton= arrayOf(v.findViewById(R.id.btn_play) as Button,
                v.findViewById(R.id.btn_play22) as Button,
                v.findViewById(R.id.btn_play33) as Button,v.findViewById(R.id.btn_play44) as Button,v.findViewById(R.id.btn_play44) as Button,
                v.findViewById(R.id.btn_play55) as Button,v.findViewById(R.id.btn_play66) as Button,v.findViewById(R.id.btn_play77) as Button,
                v.findViewById(R.id.btn_play88) as Button,v.findViewById(R.id.btn_play99) as Button,v.findViewById(R.id.btn_play10) as Button)

        var Arraypausebutton: Array<Button> = arrayOf(v.findViewById(R.id.btn_pause) as Button,
                v.findViewById(R.id.btn_pause22) as Button,v.findViewById(R.id.btn_pause33) as Button,v.findViewById(R.id.btn_pause44) as Button,
                v.findViewById(R.id.btn_pause55) as Button,v.findViewById(R.id.btn_pause66) as Button,v.findViewById(R.id.btn_pause77) as Button,
                v.findViewById(R.id.btn_pause88) as Button,v.findViewById(R.id.btn_pause99) as Button,v.findViewById(R.id.btn_pause10) as Button)

        var ArrayPlayAgainbutton: Array<Button> = arrayOf(v.findViewById(R.id.btn_playagain) as Button,
                v.findViewById(R.id.btn_playagain22) as Button,v.findViewById(R.id.btn_playagain33) as Button,v.findViewById(R.id.btn_playagain44) as Button,
                v.findViewById(R.id.btn_playagain55) as Button,v.findViewById(R.id.btn_playagain66) as Button,v.findViewById(R.id.btn_playagain77) as Button,
                v.findViewById(R.id.btn_playagain88) as Button,v.findViewById(R.id.btn_playagain99) as Button,v.findViewById(R.id.btn_playagain10) as Button)

        val bundle: Bundle? =arguments
        if(bundle!=null){
            getfilesize=bundle.getInt("getsizefile")
            Log.d(LOG_TAG,getfilesize.toString())
            for(i in 0..getfilesize){
                arraylist.add(bundle.getString("file$i").toString())
            }
        }

        for(i in 0..getfilesize){
            //Log.d("showlayout","getfile$i")
            playlayoutArray[i].isVisible=true
            Arrayplaybutton[i].setOnClickListener {
                playing(arraylist.get(i))
                Toast.makeText(activity,"Play${i+1}", Toast.LENGTH_SHORT).show()
            }
            Arraypausebutton[i].setOnClickListener {
                pausePlaying()
                Toast.makeText(activity,"Stop${i+1}", Toast.LENGTH_SHORT).show()
            }
            ArrayPlayAgainbutton[i].setOnClickListener {
                playAgain()
                Toast.makeText(activity,"Play Again${i+1}", Toast.LENGTH_SHORT).show()
            }
        }
        return v
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
    private fun playing(path:String){
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

}