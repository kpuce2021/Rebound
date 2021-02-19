package kr.ac.kpu.sleepwell

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.multidex.MultiDex
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

private const val DECIBEL = "Decibel"
private const val LOG_TAG = "Error"
private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
private const val REQUEST_WRITE_EXTERNAL_PERMISSION=300
private const val REQUEST_READ_EXTERNAL_PERMISSION=400
private const val REQUEST_ALL_PERMISSION=100
private const val multiplePermissionscode=100
class Fragment4 : Fragment() {
    //private var isRunning:Boolean=false
    private lateinit var mrecorder: MediaRecorder
    private lateinit var mlistener: MediaRecorder
    // private var state: Boolean = false
    // private val EMA_FILTER: Double = 1.0
    private var mEMA: Double = 0.0
    private var filename:String?=null
    private var path: String?=null
    private var output: File?=null
    private var foldername:String?=null
    private var directory: File?=null
    private var mediaPlayer: MediaPlayer?=null
    private var pausePosition:Int?=null
    private var output2:String?=null
    //private var statemediaplayer:Int?=null
    private var isPaused:Boolean=false
    private lateinit var output3:String
    private var count:Int=0
    private var isRunning:Boolean=false
    private val EMA_FILTER: Double = 1.0
    private var isStartListeningCheck:Boolean=false
    private var isStopRecordingOkay:Boolean=false
    private var isTimerfinished:Boolean=true
    private var isTimergoOkay:Boolean=true
    private var amIstartRecording:Boolean=false
    private var getsizefile:Int=0
    var arraylist=ArrayList<String>(100)   //녹음파일 이름 저장(output2)
    //RECORD_AUDIO에 퍼미션 요청 변수
    private var permissions2: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private var permissionToRecordAccepted = false
    /*override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions2, grantResults)
        permissionToRecordAccepted = if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        } else {
            false
        }
        if (!permissionToRecordAccepted) finish()
    }*/
    private fun Permissions(): Boolean {
        val permissionWRITE_EXTERNAL_STORAGE = activity?.applicationContext?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.WRITE_EXTERNAL_STORAGE) }
        //val permissionRECORD = ContextCompat.checkSelfPermission(requireActivity().applicationContext, Manifest.permission.RECORD_AUDIO)
        val permissionRECORD=activity?.applicationContext?.let { ContextCompat.checkSelfPermission(it,Manifest.permission.RECORD_AUDIO) }
        val permissionREAD_EXTERNAL_STORAGE=activity?.applicationContext?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.READ_EXTERNAL_STORAGE) }
        val listPermissionsNeeded: MutableList<String> = ArrayList()
        if (permissionWRITE_EXTERNAL_STORAGE != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            //requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_WRITE_EXTERNAL_PERMISSION)
        }
        if (permissionRECORD != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.RECORD_AUDIO)
            //requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO_PERMISSION)
        }
        if (permissionREAD_EXTERNAL_STORAGE != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            //requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_READ_EXTERNAL_PERMISSION)
        }
        if (!listPermissionsNeeded.isEmpty()) {
           requestPermissions( listPermissionsNeeded.toTypedArray(), REQUEST_ALL_PERMISSION)
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            REQUEST_ALL_PERMISSION->{
                for((i,permission) in permissions.withIndex()){
                    if(grantResults[i]!=PackageManager.PERMISSION_GRANTED)
                        Toast.makeText(activity,"Permission denied! try again",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        var v:View=inflater.inflate(R.layout.fragment_4, container, false)
        if(!Permissions())
            Toast.makeText(activity,"권한을 허용하세요.",Toast.LENGTH_SHORT).show()
        //수면파트
        var btn_start1:Button=v.findViewById(R.id.btn_start) as Button
        var btn_stop1:Button=v.findViewById(R.id.btn_stop) as Button
        //재생파트
        /*var playlayout1: LinearLayout =v.findViewById(R.id.noceum1) as LinearLayout
        var btn_play1:Button=v.findViewById(R.id.btn_play) as Button
        var btn_pause1:Button=v.findViewById(R.id.btn_pause) as Button
        var btn_playagain1:Button=v.findViewById(R.id.btn_playagain) as Button*/

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

        //수면시작
        btn_start1.setOnClickListener {
            btn_stop1.isVisible=true
            btn_start1.isVisible=false
            startListening()
            isRunning=true
            isTimergoOkay=true
            val Decibelcheckandrecording=getDecibel()
            Decibelcheckandrecording.start()
        }
        //수면중지
        btn_stop1.setOnClickListener {
            btn_stop1.isVisible=false
            btn_start1.isVisible=true
            isTimerfinished=true
            isRunning=false
            isTimergoOkay=false
            stopListening()
            if(amIstartRecording==true){
                stopRecording()
            }
            //playlayout1.isVisible=true
            getsizefile=arraylist.size-1
            for(i in 0..getsizefile){
                playlayoutArray[i].isVisible=true
                Arrayplaybutton[i].setOnClickListener {
                    playing(arraylist.get(i))
                    Toast.makeText(activity,"Play${i+1}",Toast.LENGTH_SHORT).show()
                }
                Arraypausebutton[i].setOnClickListener {
                    pausePlaying()
                    Toast.makeText(activity,"Stop${i+1}",Toast.LENGTH_SHORT).show()
                }
                ArrayPlayAgainbutton[i].setOnClickListener {
                    playAgain()
                    Toast.makeText(activity,"Play Again${i+1}",Toast.LENGTH_SHORT).show()
                }
            }
        }
        //1번
        /*btn_play1.setOnClickListener {
            playing(arraylist.get(0))
            Toast.makeText(activity,"play1", Toast.LENGTH_SHORT).show()
        }
        btn_pause1.setOnClickListener {
            pausePlaying()
        }
        btn_playagain1.setOnClickListener {
            playAgain()
        }*/
        return v
    }
    inner class getDecibel:Thread(){
        override fun run() {
            while(isRunning){
                /*if(isStartListeningCheck==true){
                    startListening()
                    isStartListeningCheck=false
                }*/
                var Decibel:Double=SoundDB(32767.0)
                SystemClock.sleep(1000)
                Log.d(DECIBEL,Decibel.toString()+" db")
                if(Decibel>=-15.0 && isTimerfinished){
                    //isStartListeningCheck=true
                    val decibeltimer=Timerclass(3)
                    decibeltimer.start()
                    //stopListening()
                    startRecording()
                    //if(isStopRecordingOkay)
                    //  stopRecording()
                }
            }
        }
    }
    inner class Timerclass(val countnumber:Int):Thread(){
        var countnum:Int=countnumber
        var Decibel:Double?=null
        override fun run() {
            isTimerfinished=false
            while(isTimergoOkay){
                Decibel=SoundDB(32767.0)
                Log.d("Timerclass Decibel ms",Decibel.toString())
                if(Decibel!! >-15.0)
                    countnum=3
                if(countnum<=0){
                    //isStopRecordingOkay=true
                    stopRecording()
                    isTimerfinished=true
                    break
                }
                else{
                    isStopRecordingOkay=false
                    countnum-=1
                    Log.d("countnumber",countnum.toString())
                    SystemClock.sleep(1000)
                }
            }
        }
    }
    //added(2021.02.04)
    fun getAmplitude():Int{
        if(mlistener!=null)
            return mlistener!!.maxAmplitude
        else
            return 0
    }
    //added(2021.02.04)
    fun getAmplitudeEMA():Double{
        var amp:Double=getAmplitude().toDouble()
        mEMA = EMA_FILTER * amp + (1.0 - EMA_FILTER) * mEMA
        return mEMA
    }
    fun SoundDB(ampl:Double):Double{
        return 20*Math.log10(getAmplitudeEMA()/ampl)
    }
    private fun startListening(){
        mlistener=MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile("/dev/null")
            try {
                prepare()
                start()
            }catch (e:IllegalArgumentException) {
                e.printStackTrace();
            }catch (e: IllegalStateException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
    private fun stopListening(){
        if(mlistener!=null){
            mlistener?.apply {
                stop()
                release()
            }
        }
    }
    private fun getTime():String{
        var now:Long=System.currentTimeMillis()
        var mformat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
        var mdate: Date = Date(now)
        return mformat.format(mdate)
    }
    private fun startRecording(){
        foldername="RecordingFolder"
        directory=File("/mnt/sdcard"+File.separator+foldername)
        if(!(directory!!.exists())) {
            directory!!.mkdirs()
        }
        path="/mnt/sdcard/"+foldername
        filename="녹음파일 "+getTime()+".mp3"
        output=File(path,filename)
        output2=output!!.absolutePath
        //arraylist.add(output2.toString())
        mrecorder = MediaRecorder().apply {
            //setAudioEncodingBitRate(16)
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(output2)
            try {
                prepare()
                start()
                amIstartRecording=true
            } catch (e:IllegalArgumentException) {
                e.printStackTrace()
                amIstartRecording=false
            } catch (e:IllegalStateException ) {
                e.printStackTrace()
                amIstartRecording=false
            }catch (e: IOException) {
                amIstartRecording=false
                Log.e(LOG_TAG, "prepare() failed")
            }
        }
        mEMA=0.0
    }
    private fun stopRecording() {
        amIstartRecording=false
        if(mrecorder!=null){
            mrecorder?.apply {
                stop()
                arraylist.add(output2.toString())
                //release()
            }
        }
    }
    private fun releaseRecording(){
        if(mrecorder!=null){
            mrecorder?.apply {
                release()
            }
        }
    }
    fun finish(){
        finish()
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
            mediaPlayer=MediaPlayer().apply {
                setDataSource(fis.fd)
                prepare()
                start()
                isPaused=false
            }
        }catch (e:Exception){
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
    /*override fun onDestroyView() {
        super.onDestroyView()
        if(mediaPlayer!=null){
            mediaPlayer!!.release()
            mediaPlayer=null
        }
    }*/
    //화면이 꺼지고도 앱의 기능을 구현하려면 onDestroy에서 init함수로 변경 후에 클래스 맨 위에 집어넣으시오.
    override fun onDestroy() {
        super.onDestroy()
        if(mediaPlayer!=null){
            mediaPlayer!!.release()
            mediaPlayer=null
        }
        if(mrecorder!=null){
            mrecorder!!.release()
        }
        if(mlistener!=null){
            mlistener!!.release()
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
}