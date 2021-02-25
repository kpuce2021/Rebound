package kr.ac.kpu.sleepwell

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.activity_day_result_a_c.*
import java.io.FileInputStream
import java.lang.Exception

class Day_resultAC : AppCompatActivity() {
    private var getfilesizestring:String=""
    private var getfilesize:Int=0
    private var mediaPlayer: MediaPlayer?=null
    private var pausePosition:Int?=null
    private var isPaused:Boolean=false
    var arraylist=ArrayList<String>(10)   //녹음파일 이름 저장(output2)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_day_result_a_c)


        var playlayoutArray: Array<LinearLayout> = arrayOf(findViewById(R.id.noceum1),
            findViewById(R.id.noceum2),findViewById(R.id.noceum3),findViewById(R.id.noceum4) ,
            findViewById(R.id.noceum5),findViewById(R.id.noceum6),findViewById(R.id.noceum7),
            findViewById(R.id.noceum8),findViewById(R.id.noceum9),findViewById(R.id.noceum10))

        var Arrayplaybutton= arrayOf(findViewById(R.id.btn_play),
            findViewById(R.id.btn_play22),
            findViewById(R.id.btn_play33),findViewById(R.id.btn_play44),findViewById(R.id.btn_play44),
            findViewById(R.id.btn_play55),findViewById(R.id.btn_play66),findViewById(R.id.btn_play77),
            findViewById(R.id.btn_play88) as Button,findViewById(R.id.btn_play99),findViewById(R.id.btn_play10))

        var Arraypausebutton: Array<Button> = arrayOf(findViewById(R.id.btn_pause),
            findViewById(R.id.btn_pause22) ,findViewById(R.id.btn_pause33) ,findViewById(R.id.btn_pause44),
            findViewById(R.id.btn_pause55) ,findViewById(R.id.btn_pause66) ,findViewById(R.id.btn_pause77),
            findViewById(R.id.btn_pause88) ,findViewById(R.id.btn_pause99) ,findViewById(R.id.btn_pause10))

        var ArrayPlayAgainbutton: Array<Button> = arrayOf(findViewById(R.id.btn_playagain),
            findViewById(R.id.btn_playagain22),findViewById(R.id.btn_playagain33),findViewById(R.id.btn_playagain44) ,
            findViewById(R.id.btn_playagain55),findViewById(R.id.btn_playagain66),findViewById(R.id.btn_playagain77),
            findViewById(R.id.btn_playagain88),findViewById(R.id.btn_playagain99),findViewById(R.id.btn_playagain10))
        for(i in 0..9){
            arraylist.add(i.toString())
        }
        arraylist.removeAll(arraylist)
        getfilesizestring=intent.getStringExtra("getsizefile").toString()
        getfilesize=getfilesizestring.toInt()
        if(getfilesize>10)
            getfilesize=10
        for(i in 0..getfilesize){
            arraylist.add(intent.getStringExtra("file$i").toString())
        }
        for(i in 0..getfilesize){
            //Log.d("showlayout","getfile$i")
            playlayoutArray[i].isVisible=true
            Arrayplaybutton[i].setOnClickListener {
                playing(arraylist.get(i))
                Toast.makeText(this,"Play${i+1}", Toast.LENGTH_SHORT).show()
            }
            Arraypausebutton[i].setOnClickListener {
                pausePlaying()
                Toast.makeText(this,"Stop${i+1}", Toast.LENGTH_SHORT).show()
            }
            ArrayPlayAgainbutton[i].setOnClickListener {
                playAgain()
                Toast.makeText(this,"Play Again${i+1}", Toast.LENGTH_SHORT).show()
            }
        }
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