package kr.ac.kpu.sleepwell

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
private const val REQUEST_ALL_PERMISSION=100
private const val DECIBEL = "Decibel"
private const val LOG_TAG = "Error"
class backgroundservice : Service() {
    private lateinit var mrecorder: MediaRecorder
    private lateinit var mlistener: MediaRecorder
    private var mEMA: Double = 0.0
    private var rfilename:String?=null
    private var path: String?=null
    private var output: File?=null
    private var rfoldername:String?=null
    private var directory: File?=null
    private var mediaPlayer: MediaPlayer?=null
    private var pausePosition:Int?=null
    private var output2:String?=null
    private var isRunning:Boolean=false
    private val EMA_FILTER: Double = 1.0
    private var isStartListeningCheck:Boolean=false
    private var isStopRecordingOkay:Boolean=false
    private var isTimerfinished:Boolean=true
    private var isTimergoOkay:Boolean=true
    private var amIstartRecording:Boolean=false
    var myapp:MyglobalArraylist?=null
    //var arraylist=ArrayList<String>(20)   //녹음파일 이름 저장(output2)
    //var timearraylist=ArrayList<String>(20) //시간 저장(녹음 파일)
    //var Filearraylist=ArrayList<File>(20)   //녹음파일 자체 저장
    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if("startForeground".equals(intent!!.action)){
            startForegroundService()


            /*for(i in 0..19){
                //arraylist[i]=i.toString()
                //timearraylist[i]=i.toString()
                arraylist.add(i,i.toString())
                timearraylist.add(i,i.toString())
            }
            arraylist.removeAll(arraylist)
            timearraylist.removeAll(timearraylist)*/
            myapp=application as MyglobalArraylist
            myapp!!.AllarraylistInit()
            startListening()
            isRunning=true
            isTimergoOkay=true
            val Decibelcheckandrecording=getDecibel()
            Decibelcheckandrecording.start()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        isTimerfinished=true
        isRunning=false
        isTimergoOkay=false
        stopListening()
        if(amIstartRecording==true){
            stopRecording()
        }
        Log.d("is Stop?","YES!!")
        super.onDestroy()
    }

    private fun startForegroundService(){
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(this, "default")
        builder.setSmallIcon(R.mipmap.ic_launcher_round)
        builder.setContentTitle("SleepWell")
        builder.setContentText("SleepWell 수면 진행중입니다.")

        val notificationintent:Intent= Intent(this, SleepStart::class.java)
        val pendingintent: PendingIntent = PendingIntent.getActivity(this,0,notificationintent,0)
        builder.setContentIntent(pendingintent)
        builder.setAutoCancel(true)


        val manager: NotificationManager =getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel("default","기본채널",
                    NotificationManager.IMPORTANCE_DEFAULT)
            )
        }
        startForeground(1,builder.build())
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
                if(Decibel>=-15.0 && isTimerfinished && myapp!!.arraylist.size<11){
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
        var Decibel:Double=-20.0
        override fun run() {
            isTimerfinished=false
            while(isTimergoOkay){
                Log.d("Timerclass Decibel ms",Decibel.toString())
                if(Decibel >-15.0)
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
                    Decibel=SoundDB(32767.0)
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
        mlistener= MediaRecorder().apply {
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
    private fun onlytime():String{
        var o_now:Long=System.currentTimeMillis()
        var o_mformat: SimpleDateFormat = SimpleDateFormat("hh:mm")
        var o_mdate: Date = Date(o_now)
        return o_mformat.format(o_mdate)
    }
    private fun daytime():String{
        var d_now:Long=System.currentTimeMillis()
        var d_mformat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        var d_mdate: Date = Date(d_now)
        return d_mformat.format(d_mdate)
    }
    private fun startRecording(){
        myapp!!.timearraylist.add(onlytime())   //시간 저장
        rfoldername="RecordingFolder"
        directory= File("/mnt/sdcard"+ File.separator+rfoldername)
        if(!(directory!!.exists())) {
            directory!!.mkdirs()
        }
        path="/mnt/sdcard/"+rfoldername
        rfilename="녹음파일 "+getTime()+".mp3"
        output= File(path,rfilename)
        output2=output!!.absolutePath
        //arraylist.add(output2.toString())
        //myapp!!.filearraylist.add(output!!)
        //myapp!!.arraylist.add(output2.toString())
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
                myapp!!.filearraylist.add(output!!)
                myapp!!.arraylist.add(output2.toString())
                Log.d("filearraylist_size",myapp!!.filearraylist.size.toString())
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

}