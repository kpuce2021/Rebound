package kr.ac.kpu.sleepwell

import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import kotlinx.android.synthetic.main.fragment_0.view.*
import java.io.*
import java.util.*
import kotlin.collections.ArrayList
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.SystemClock
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_0.view.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

private const val REQUEST_ALL_PERMISSION=100
private const val DECIBEL = "Decibel"
private const val LOG_TAG = "Error"


class Fragment0 : Fragment(), SensorEventListener {
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
    private var permissions2: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private var permissionToRecordAccepted = false
    val foldername: String = "LogFolder"
    val filename = "sensorlog.txt"

    private val sensorManager by lazy {
        requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager  //센서 매니저에대한 참조를 얻기위함
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
        fun Permissions(): Boolean {
        val permissionWRITE_EXTERNAL_STORAGE = activity?.applicationContext?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.WRITE_EXTERNAL_STORAGE) }
        val permissionREAD_EXTERNAL_STORAGE=activity?.applicationContext?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.READ_EXTERNAL_STORAGE) }
        val permissionRECORD=activity?.applicationContext?.let { ContextCompat.checkSelfPermission(it,Manifest.permission.RECORD_AUDIO) }
        val listPermissionsNeeded: MutableList<String> = ArrayList()
        if (permissionWRITE_EXTERNAL_STORAGE != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (permissionREAD_EXTERNAL_STORAGE != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (permissionRECORD != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.RECORD_AUDIO)
            //requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO_PERMISSION)
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
        var startTime = System.currentTimeMillis()
        var endTime= System.currentTimeMillis()
        var i = 0
        var view = inflater.inflate(R.layout.fragment_0,container,false)

        if(!Permissions())
            Toast.makeText(activity,"권한을 허용하세요.",Toast.LENGTH_SHORT).show()

        view.sleep_btn.setOnClickListener{
            val day = findDate()
            if(i==0) {
                startTime = System.currentTimeMillis()
                sensorManager.registerListener(this,    // 센서 이벤트 값을 받을 리스너 (현재의 액티비티에서 받음)
                        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),// 센서 종류
                        SensorManager.SENSOR_DELAY_NORMAL)// 수신 빈도
                startListening()
                isRunning=true
                isTimergoOkay=true
                val Decibelcheckandrecording=getDecibel()
                Decibelcheckandrecording.start()
                i = 1
                view.sleep_btn.setText("수면 중지")
            }
            else{
                endTime = System.currentTimeMillis()
                sensorManager.unregisterListener(this)

                db.collection(userkey).document(day)
                    .set(data, SetOptions.merge())
                    .addOnSuccessListener {Log.d("tag", "DocumentSnapshot added successfully") }
                    .addOnFailureListener { e -> Log.w("tag", "Error adding document", e) }

                isTimerfinished=true
                isRunning=false
                isTimergoOkay=false
                stopListening()
                if(amIstartRecording==true){
                    stopRecording()
                }
                getsizefile=arraylist.size-1
                Log.d(LOG_TAG,getsizefile.toString())
                var bundle:Bundle= Bundle()
                bundle.putInt("getsizefile",getsizefile)
                for(i in 0..getsizefile){
                    bundle.putString("file$i",arraylist.get(i))
                }
                var day_result:Fragment=Day_resultFrag()
                var transaction:FragmentTransaction=requireActivity().supportFragmentManager.beginTransaction()
                day_result.arguments=bundle
                transaction.replace(R.id.Fragment0,day_result)
                transaction.commit()

                i = 0
                view.sleep_btn.setText("수면 시작")
                val time = (endTime - startTime)
                val sectime = time /1000
                val mintime = sectime / 60
                Log.d("MainActivity", "${sectime}초 수면 = ${mintime}분 수면")
            }
        }
        return view
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


    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            var x = event.values[0]
            var y = event.values[1]
            var z = event.values[2]
            var x2 = Math.pow(x.toDouble(), 2.0)//x제곱
            var y2 = Math.pow(y.toDouble(), 2.0)//y제곱
            var z2 = Math.pow(z.toDouble(), 2.0)//z제곱
            var m = Math.sqrt(x2+y2+z2)//움직임 값
            var contents = "x:${event.values[0]}, y:${event.values[1]}, z:${event.values[2]}, m:${m}\n"
            WriteTextFile(foldername,filename,contents)
            Log.d("MainActivity", " x:${event.values[0]}, y:${event.values[1]}, z:${event.values[2]}, m:${m}") // [0] x축값, [1] y축값, [2] z축값, 움직임값
        }
    }
    /*override fun onPause() {
        super.onPause()
        Log.e("Fragment0", "onPause()")
    }*/

    /*override fun onDestroy() {
        super.onDestroy()
        Log.e("Fragment0", "onDestroy()")
    }*/

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
        rfoldername="RecordingFolder"
        directory=File("/mnt/sdcard"+File.separator+rfoldername)
        if(!(directory!!.exists())) {
            directory!!.mkdirs()
        }
        path="/mnt/sdcard/"+rfoldername
        rfilename="녹음파일 "+getTime()+".mp3"
        output=File(path,rfilename)
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
}