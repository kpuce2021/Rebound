package kr.ac.kpu.sleepwell

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.io.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class GroundService : Service(), SensorEventListener {
    val user = FirebaseAuth.getInstance()
    val userkey = user.uid.toString()
    val db = Firebase.firestore
    var data = hashMapOf(
        "sleep_time" to 0,
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

    private val sensorManager by lazy {
        Activity().getSystemService(Context.SENSOR_SERVICE) as SensorManager  //센서 매니저에대한 참조를 얻기위함
    }

    //private var isRunning:Boolean=false
    private lateinit var mrecorder: MediaRecorder
    private lateinit var mlistener: MediaRecorder
    // private var state: Boolean = false
    // private val EMA_FILTER: Double = 1.0
    private var mEMA: Double = 0.0
    private var rfilename:String?=null
    private var path: String?=null
    private var output: File?=null
    private var rfoldername:String?=null
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
    val foldername: String = "LogFolder"
    val filename = "sensorlog.txt"

    private var mainThread: Thread? = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        serviceIntent = intent
        showToast(application, "Start Service")
        sensorManager.registerListener(this,    // 센서 이벤트 값을 받을 리스너 (현재의 액티비티에서 받음)
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),// 센서 종류
            SensorManager.SENSOR_DELAY_NORMAL)// 수신 빈도
        startListening()
        isRunning=true
        isTimergoOkay=true
        val Decibelcheckandrecording=getDecibel()
        Decibelcheckandrecording.start()
        mainThread = Thread(Runnable {
            val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
            var run = true
            while (run) {
                try {
                    Thread.sleep(1000 * 60 * 1.toLong()) // 1 minute
                    val date = Date()
                    showToast(application, sdf.format(date))
                    sendNotification(sdf.format(date))
                } catch (e: InterruptedException) {
                    run = false
                    e.printStackTrace()
                }
            }
        })
        mainThread!!.start()
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceIntent = null
        setAlarmTimer()
        sensorManager.unregisterListener(this)
        stopListening()
        if(amIstartRecording==true){
            stopRecording()
        }
        Thread.currentThread().interrupt()
        if (mainThread != null) {
            mainThread!!.interrupt()
            mainThread = null
        }
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onUnbind(intent: Intent): Boolean {
        return super.onUnbind(intent)
    }

    fun showToast(application: Application, msg: String?) {
        val h = Handler(application.mainLooper)
        h.post { Toast.makeText(application, msg, Toast.LENGTH_LONG).show() }
    }

    private fun setAlarmTimer() {
        val c = Calendar.getInstance()
        c.timeInMillis = System.currentTimeMillis()
        c.add(Calendar.SECOND, 1)
        val intent = Intent(this, AlarmReceiver::class.java)
        val sender = PendingIntent.getBroadcast(this, 0, intent, 0)
        val mAlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        mAlarmManager[AlarmManager.RTC_WAKEUP, c.timeInMillis] = sender
    }

    private fun sendNotification(messageBody: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT)
        val channelId = "fcm_default_channel" //getString(R.string.default_notification_channel_id)
        val notificationBuilder: Notification.Builder = Notification.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher) //drawable.splash)
                .setContentTitle("Sleep Recording")
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Channel human readable title", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}




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
            Log.d("savepath",foldername)
            writer.flush()
            writer.close()
            fos.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun getTime():String{
        val now:Long=System.currentTimeMillis()
        val mformat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
        val mdate: Date = Date(now)
        return mformat.format(mdate)
    }


    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            val x2 = Math.pow(x.toDouble(), 2.0)//x제곱
            val y2 = Math.pow(y.toDouble(), 2.0)//y제곱
            val z2 = Math.pow(z.toDouble(), 2.0)//z제곱
            val m = Math.sqrt(x2+y2+z2)//움직임 값
            val contents = "x:${event.values[0]}, y:${event.values[1]}, z:${event.values[2]}, m:${m} ${getTime()}\n"
            WriteTextFile(foldername,filename,contents)
            Log.d("MainActivity", " x:${event.values[0]}, y:${event.values[1]}, z:${event.values[2]}, m:${m}") // [0] x축값, [1] y축값, [2] z축값, 움직임값
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
                Log.d("DECIBEL",Decibel.toString()+" db")
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
    fun getAmplitude():Int{
        if(mlistener!=null)
            return mlistener!!.maxAmplitude
        else
            return 0
    }
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

    private fun startRecording(){
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
                Log.e("LOG_TAG", "prepare() failed")
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

    companion object {
        var serviceIntent: Intent? = null
    }
}