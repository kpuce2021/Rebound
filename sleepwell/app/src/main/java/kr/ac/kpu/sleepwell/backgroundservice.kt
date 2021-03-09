package kr.ac.kpu.sleepwell

import android.app.*
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
private const val REQUEST_ALL_PERMISSION=100
private const val DECIBEL = "Decibel"
private const val LOG_TAG = "Error"
class backgroundservice : Service(), SensorEventListener {
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
            "wake_up" to "am 07:00",
            "sleep_score" to 0,
            "alcohol" to false,
            "caffeine" to false,
            "smoke" to false,
            "midnight_snack" to false,
            "workout" to false
    )
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
        if("startForeground".equals(intent!!.action)){
            startForegroundService()

            val format = SimpleDateFormat("a hh:mm", Locale("ko","KR"))
            val date = Date(startTime)
            val sttime = format.format(date)
            val dbRef = db.collection(userkey).document(day)
            dbRef.set(data, SetOptions.merge())
            dbRef.update("go_to_bed", sttime)
                    .addOnSuccessListener { Log.d("tag", "DocumentSnapshot successfully updated!") }
                    .addOnFailureListener { e -> Log.w("tag", "Error updating document", e) }

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

    override fun onDestroy() {

        var endTime = System.currentTimeMillis()
        sensorManager.unregisterListener(this)
        val deRef = db.collection(userkey).document(day)
        val time = (endTime - startTime)
        val sectime = time /1000
        val mintime = (sectime/60).toInt() //몇분 잤는지
        deRef.update("sleep_time", mintime)
                .addOnSuccessListener { Log.d("tag", "DocumentSnapshot successfully updated!") }
                .addOnFailureListener { e -> Log.w("tag", "Error updating document", e) }
        Log.d("MainActivity", "${sectime}초 수면 = ${mintime}분 수면")
        renameFile()


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
    private fun renameFile(){
        val file = File("/mnt/sdcard/$foldername/$filename")
        val rename = File("/mnt/sdcard/$foldername/$filename-${getTime()}")
        file.renameTo(rename)
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

    private fun WriteTextFile(foldername: String, filename: String, contents: String?) {
        try {
            val dir = File("/mnt/sdcard"+File.separator+foldername)
            //디렉토리 폴더가 없으면 생성함
            if (!dir.exists()) {
                dir.mkdir()
            }
            //파일 output stream 생성
            val fos = FileOutputStream("/mnt/sdcard/$foldername/$filename", true)
            //파일쓰기
            val writer = BufferedWriter(OutputStreamWriter(fos))
            writer.write(contents)
            Log.d("Sensorlog",foldername)
            writer.flush()
            writer.close()
            fos.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            var x = event.values[0]
            var y = event.values[1]
            var z = event.values[2]
            var x2 = Math.pow(x.toDouble(), 2.0)//x제곱
            var y2 = Math.pow(y.toDouble(), 2.0)//y제곱
            var z2 = Math.pow(z.toDouble(), 2.0)//z제곱
            var m = Math.sqrt(x2+y2+z2)//움직임 값
            var contents = "x:${event.values[0]}, y:${event.values[1]}, z:${event.values[2]}, m:${m} ${getTime()}\n"
            WriteTextFile(foldername,filename,contents)
            Log.d("Sensorlog", " x:${event.values[0]}, y:${event.values[1]}, z:${event.values[2]}, m:${m}") // [0] x축값, [1] y축값, [2] z축값, 움직임값
        }
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