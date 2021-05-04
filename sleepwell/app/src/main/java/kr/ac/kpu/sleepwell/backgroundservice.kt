package kr.ac.kpu.sleepwell

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.io.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

private const val REQUEST_ALL_PERMISSION=100
private const val DECIBEL = "Decibel"
private const val LOG_TAG = "Error"
var cycleList = mutableListOf<String>()
var timeList = mutableListOf<Long>()
var ccount = 0
var scount = 0
var total = 0.0
var awake = 0
var go_to_sleep = 0
var sleep_light = 0
var sleep_deep = 0
var sleep_rem = 0
var REM = 0
var DEEP = 0
var AWAKE = 0
var LIGHT = 0
var realtime_index = 0

class backgroundservice : Service(), SensorEventListener {
    val database = Firebase.database.reference
    val user = FirebaseAuth.getInstance()
    val userkey = user.uid.toString()
    val db = Firebase.firestore
    var data = hashMapOf(
            "sleep_time" to 0, //분을 단위로 사용
            "sleep_deep" to 0,
            "sleep_light" to 0,
            "sleep_rem" to 0,
            "awake" to 0,
            "go_to_bed" to "pm 11:00",
            "go_to_sleep" to 0,
            "wake_up" to "am 07:00")
    val foldername: String = "LogFolder"
    val filename = "sensorlog.txt"
    val day = findDate()
    var startTime = System.currentTimeMillis()

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
    private val sensorManager by lazy {
        getSystemService(Context.SENSOR_SERVICE) as SensorManager  //센서 매니저에대한 참조를 얻기위함
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        timeList.add(0, startTime)
        cycleList.add(0, "awake")
        if("startForeground".equals(intent!!.action)){
            startForegroundService()


            val format = SimpleDateFormat("a hh:mm", Locale("ko","KR"))
            val date = Date(startTime)
            val sttime = format.format(date)
            val dbRef = db.collection(userkey).document(day)
            dbRef.set(data, SetOptions.merge())
                    .addOnSuccessListener { Log.d("DB", "DocumentSnapshot successfully written!") }
                    .addOnFailureListener { e -> Log.w("DB", "Error writing document", e) }

            dbRef.update("go_to_bed", sttime)
                    .addOnSuccessListener { Log.d("DB", "DocumentSnapshot successfully updated!") }
                    .addOnFailureListener { e -> Log.w("DB", "Error updating document", e) }

            sensorManager.registerListener(this,    // 센서 이벤트 값을 받을 리스너 (현재의 액티비티에서 받음)
                    sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),// 센서 종류
                    SensorManager.SENSOR_DELAY_NORMAL)// 수신 빈도

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

    fun gotoSleep(i :Int, x :String){
        if(i<cycleList.size-1){
            if(cycleList[i] == x) {
                go_to_sleep += 5
                if (cycleList[i + 1] == x) {
                    gotoSleep(i + 1, x)
                }
            }
        }
        else if(i== cycleList.size-1){
            if(cycleList[i] == x) {
                go_to_sleep += 5
            }
        }
    }

    fun addCycleTime(){
        for(i in 0..cycleList.size-1){
            if (cycleList[i]=="sleep_rem"){
                REM += 5
            }
            else if(cycleList[i]=="sleep_deep"){
                DEEP += 5
            }
            else if(cycleList[i]=="sleep_light"){
                LIGHT += 5
            }
            else if(cycleList[i]=="awake"){
                AWAKE += 5
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        var endTime = System.currentTimeMillis()
        val format = SimpleDateFormat("a hh:mm", Locale("ko","KR"))
        val date = Date(endTime)
        val edtime = format.format(date)
        sensorManager.unregisterListener(this)
        val deRef = db.collection(userkey).document(day)
        val time = (endTime - startTime)
        val sectime = time /1000
        val mintime = (sectime/60).toInt() //몇분 잤는지
        addCycleTime()
        gotoSleep(0,"awake")
        deRef.update("sleep_time", mintime)
        deRef.update("go_to_sleep", go_to_sleep)
        deRef.update("awake", AWAKE)
        deRef.update("sleep_deep", DEEP)
        deRef.update("sleep_light", LIGHT)
        deRef.update("sleep_rem", REM)
        deRef.update("wake_up", edtime)

        val cycleRef = db.collection(userkey).document(day).collection("cycle").document("cycle")
        for(i in 0..cycleList.size-1){
            var thisCycle = hashMapOf(i.toString() to cycleList[i],"size" to (i+1).toString())
            cycleRef.set(thisCycle, SetOptions.merge())
                    .addOnSuccessListener { Log.d("DB", "DocumentSnapshot successfully written!") }
                    .addOnFailureListener { e -> Log.w("DB", "Error writing document", e) }
        }

        Log.d("MainActivity", "${sectime}초 수면 = ${mintime}분 수면")

        isTimerfinished=true
        isRunning=false
        isTimergoOkay=false
        stopListening()
        if(amIstartRecording==true){
            stopRecording()
        }
        realtime_index = 0
        stopForeground(true)
        stopSelf()
        Log.d("is Stop?","YES!!")
    }

    private fun startForegroundService(){
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(this, "default")
        builder.setSmallIcon(R.mipmap.ic_launcher_round)
        builder.setPriority(2)
        builder.setContentTitle("SleepWell")
        builder.setContentText("SleepWell 수면 진행중입니다.")

        val notificationintent:Intent= Intent(this, SleepStart::class.java)
        val pendingintent: PendingIntent = PendingIntent.getActivity(this,0,notificationintent,PendingIntent.FLAG_UPDATE_CURRENT)
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

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}

    override fun onSensorChanged(event: SensorEvent?) {
        var x = 0.toFloat()
        var y = 0.toFloat()
        var z = 0.toFloat()
        var avg = 0.0
        event?.let {
            var ftime = System.currentTimeMillis()
            var xtime = timeList.get(ccount)
            x = event.values[0]
            y = event.values[1]
            z = event.values[2]
            var x2 = Math.pow(x.toDouble(), 2.0)//x제곱
            var y2 = Math.pow(y.toDouble(), 2.0)//y제곱
            var z2 = Math.pow(z.toDouble(), 2.0)//z제곱
            var m = Math.sqrt(x2+y2+z2)//움직임 값
            avg = sensorAverage(m)
            if (ftime-xtime>=300000){
                ccount += 1
                timeList.add(ccount, ftime)
                checkCycle(m, avg)
                if (awake > 0)
                {
                    cycleList.add(ccount, "awake")
                    initcycle()
                }
                else{
                    if(sleep_light > 0 ){
                        cycleList.add(ccount, "sleep_light")
                        initcycle()
                    }
                    else {
                        if (sleep_deep > 0) {
                            cycleList.add(ccount, "sleep_deep")
                            initcycle()
                        }
                        else{
                            if(sleep_rem > 0){
                                cycleList.add(ccount, "sleep_rem")
                                initcycle()
                            }
                            else{Log.d(DECIBEL,"checkcycle error") }
                        }
                    }
                }
            }
            else{checkCycle(m, avg)}
            var cycle0 = cycleList.get(ccount)
            var contents = "count:${ccount}, cycle:${cycle0}, change:${Math.abs(m-avg)*1000}  m:${m}, avg:${avg}, time:${getTime()}\n"
            database.child(userkey).child(daytime()).child(realtime_index.toString()).setValue(contents)
            realtime_index += 1
        }
    }
    fun initcycle(){
        awake = 0
        sleep_light = 0
        sleep_rem = 0
        sleep_deep = 0
    }

    fun checkCycle(x: Double, avg: Double) { //수면 분석 알고리즘 version 0.0.1
        var v0 = Math.abs(x - avg) * 1000
        if (ccount > 0) {
            if (cycleList.get(ccount-1)=="awake") {
                if (v0 > 1000) {
                    awake += 1
                }
                else { sleep_light += 1 }
            }
            else if (ccount<43){
                if (v0>1000){
                    awake += 1
                }
                else if(v0 <1000 && v0>300){
                    sleep_light += 1
                }
                else {sleep_deep += 1
                }
            }
            else{
                if (v0>1000){
                    awake += 1
                }
                else if(v0 <1000 && v0>300){
                    sleep_light += 1
                }
                else { sleep_rem += 1 }
            }
        }
    }

    fun sensorAverage(x: Double) : Double { // 시작 5분동안 센서값 평균 구하는 함수
        total += x
        scount+=1
        return total/(scount.toDouble())
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
            //setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            //setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            //setAudioSamplingRate(8000)
            setOutputFormat(MediaRecorder.OutputFormat.AMR_WB)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB)
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
        Log.d("isStart?","Started!!")
        myapp!!.timearraylist.add(onlytime())   //시간 저장
        rfoldername="RecordingFolder"
        directory= File(filesDir, File.separator+rfoldername)
        if(!(directory!!.exists())) {
            directory!!.mkdirs()
        }
        //path="/mnt/sdcard/"+rfoldername
        rfilename="녹음파일 "+getTime()+".mp3"
        output= File(directory,rfilename)
        output2=output!!.absolutePath
        //arraylist.add(output2.toString())
        //myapp!!.filearraylist.add(output!!)
        //myapp!!.arraylist.add(output2.toString())
        mrecorder = MediaRecorder().apply {
            //setAudioEncodingBitRate(16)
            setAudioSource(MediaRecorder.AudioSource.MIC)
            //setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            //setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            //setAudioSamplingRate(8000)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB)
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
           try {
               Log.d("isStop?","stop!!")
               mrecorder?.apply {
                   stop()
                   myapp!!.filearraylist.add(output!!)
                   myapp!!.arraylist.add(output2.toString())
                   Log.d("filearraylist_size",myapp!!.filearraylist.size.toString())
                   //release()
               }
           }catch (e:IllegalStateException){
               e.printStackTrace()
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
    fun findDate(): String {
        val cal = Calendar.getInstance()
        cal.time = Date()
        val df: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        var ampm = cal.get(Calendar.AM_PM)
        if(ampm == Calendar.PM){
            return df.format(cal.time)
        }
        else{cal.add(Calendar.DATE,-1)
            return df.format(cal.time) }
    }
}