package kr.ac.kpu.sleepwell

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_day_result_a_c.*
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class Day_resultAC : AppCompatActivity() {


    //rivate lateinit var mFirebaseStorage: FirebaseStorage

    private var daynow:String=""
    private var userEmail:String=""
    private var getfilesize:Int=0
    private var mediaPlayer: MediaPlayer?=null
    private var pausePosition:Int?=null
    private var isPaused:Boolean=false
    private var getvisiblesize:Int=0
    private var btn_pressedcheck: Array<Boolean> = arrayOf(false,false,false,false,false,false,false,false,false,false,false)
    var arraylist=ArrayList<String>(20)   //녹음파일 이름 저장(output2)
    var timearraylist=ArrayList<String>(20) //시간 저장(녹음 파일)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_day_result_a_c)


        /*mFirebaseStorage= FirebaseStorage.getInstance()
        val storageRef: StorageReference = mFirebaseStorage.getReference("gs://sleepwell-8332f.appspot.com")
        //firebase로 이메일 가져오기
        val user=Firebase.auth.currentUser
        if(user!=null){
            user?.let {
                for(profile in it.providerData){
                    userEmail=profile.email.toString()
                }
            }
        }
        else
            Toast.makeText(this,"로그인 되지 않았습니다.",Toast.LENGTH_SHORT).show()*/

        var playlayoutArray: Array<LinearLayout> = arrayOf(findViewById(R.id.noceum1),
            findViewById(R.id.noceum2),findViewById(R.id.noceum3),findViewById(R.id.noceum4) ,
            findViewById(R.id.noceum5),findViewById(R.id.noceum6),findViewById(R.id.noceum7),
            findViewById(R.id.noceum8),findViewById(R.id.noceum9),findViewById(R.id.noceum10),findViewById(R.id.noceum11))

        var timeArray: Array<TextView> = arrayOf(findViewById(R.id.text_time),
                findViewById(R.id.text_time2),findViewById(R.id.text_time3),findViewById(R.id.text_time4) ,
                findViewById(R.id.text_time5),findViewById(R.id.text_time6),findViewById(R.id.text_time7),
                findViewById(R.id.text_time8),findViewById(R.id.text_time9),findViewById(R.id.text_time10),findViewById(R.id.text_time11))

        var Arrayplaybutton:Array<Button> = arrayOf(findViewById(R.id.btn_play),
            findViewById(R.id.btn_play22),
            findViewById(R.id.btn_play33),findViewById(R.id.btn_play44),findViewById(R.id.btn_play44),
            findViewById(R.id.btn_play55),findViewById(R.id.btn_play66),findViewById(R.id.btn_play77),
            findViewById(R.id.btn_play88),findViewById(R.id.btn_play99),findViewById(R.id.btn_play10),findViewById(R.id.btn_play11))

        var ArrayPlayAgainbutton: Array<Button> = arrayOf(findViewById(R.id.btn_playagain),
            findViewById(R.id.btn_playagain22),findViewById(R.id.btn_playagain33),findViewById(R.id.btn_playagain44) ,
            findViewById(R.id.btn_playagain55),findViewById(R.id.btn_playagain66),findViewById(R.id.btn_playagain77),
            findViewById(R.id.btn_playagain88),findViewById(R.id.btn_playagain99),findViewById(R.id.btn_playagain10),findViewById(R.id.btn_playagain11))

        for(i in 0..19){
            //arraylist[i]=i.toString()
            //timearraylist[i]=i.toString()
            arraylist.add(i,i.toString())
            timearraylist.add(i,i.toString())
        }
        arraylist.removeAll(arraylist)
        timearraylist.removeAll(timearraylist)

        arraylist=intent.getSerializableExtra("filenames") as ArrayList<String>
        timearraylist=intent.getSerializableExtra("times") as ArrayList<String>
        if(arraylist.size>0)
            text_noise.isVisible=true
        getfilesize=arraylist.size-1
        if(arraylist.size>3){
            getfilesize-=1
        }
        for(i in 0..getfilesize){
            timeArray[i].setText(timearraylist.get(i))
        }
        for(i in 0..getfilesize){
            playlayoutArray[i].isVisible=true
        }
        for(i in 0..getfilesize){
            //playlayoutArray[i].isVisible=true
            Arrayplaybutton[i].setOnClickListener {
                if(!btn_pressedcheck[i]){
                    playing(arraylist.get(i))
                    Log.d("number i",i.toString())
                    Arrayplaybutton[i].setBackgroundResource(R.drawable.ic_baseline_pause_24)
                    btn_pressedcheck[i]=true
                }
                else{
                    pausePlaying()
                    Arrayplaybutton[i].setBackgroundResource(R.drawable.ic_baseline_play_arrow_24)
                    btn_pressedcheck[i]=false
                }
            }
            ArrayPlayAgainbutton[i].setOnClickListener {
                playAgain()
                //Toast.makeText(this,"Play Again${i+1}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun daytime():String{
        var now:Long=System.currentTimeMillis()
        var mformat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        var mdate: Date = Date(now)
        return mformat.format(mdate)
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