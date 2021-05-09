package kr.ac.kpu.sleepwell

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.PowerManager
import android.os.SystemClock
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_day_result_a_c.*
import kotlinx.android.synthetic.main.fragment_0.view.*
import kotlinx.coroutines.selects.select
import java.io.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

private const val REQUEST_ALL_PERMISSION=100
private const val DECIBEL = "Decibel"
private const val LOG_TAG = "Error"

class Fragment0 : Fragment() {
    val user = FirebaseAuth.getInstance()
    val userkey = user.uid.toString()
    val db = Firebase.firestore

    //firebase
    private lateinit var storageRef: StorageReference
    //private lateinit var mFirebaseStorage: FirebaseStorage
    //private lateinit var storageRef:StorageReference
    private var userEmail:String=""
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
    private var daynow:String=""
    var arraylist=ArrayList<String>(20)   //녹음파일 이름 저장(output2)
    var timearraylist=ArrayList<String>(20) //시간 저장(녹음 파일)
    var Filearraylist=ArrayList<File>(20)   //녹음파일 자체 저장
    //RECORD_AUDIO에 퍼미션 요청 변수
    private var permissions2: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private var permissionToRecordAccepted = false
    val foldername: String = "LogFolder"
    val filename = "sensorlog.txt"

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

        var selectedItemIndex = ArrayList<Int>()

        var view = inflater.inflate(R.layout.fragment_0, container, false)

        view.factorbox.setOnClickListener {
            val dialog = activity?.let { it1 -> BottomSheet(it1) }
            if (dialog != null) {
                dialog.show()
                dialog.setOnClickedListener(object : BottomSheet.ClickListener{
                    override fun onClicked(stFactor: String, sii : ArrayList<Int>) {
                        view.factor.setText(stFactor)
                        view.factor.setTextColor(ContextCompat.getColor(context!!, R.color.white))
                        selectedItemIndex = sii
                    }
                })
            }
        }
        view.alarmbox.setOnClickListener {
            val intent=Intent(activity,AlarmActivity::class.java)
            startActivity(intent)
        }

        if(!Permissions())
            Toast.makeText(activity,"권한을 허용하세요.",Toast.LENGTH_SHORT).show()
        //firebase init
        val user=Firebase.auth.currentUser
        if(user!=null){
            user?.let {
                for(profile in it.providerData){
                    userEmail=profile.email.toString()
                }
            }
        }
        else{
            Toast.makeText(activity,"로그인 되지 않았습니다.",Toast.LENGTH_SHORT).show()
        }
        view.sleep_btn.setOnClickListener{
                /*if (GroundService.serviceIntent==null) {
                    serviceIntent = Intent(activity, GroundService::class.java)
                    startService(serviceIntent)
                } else {
                    serviceIntent = GroundService.serviceIntent;//getInstance().getApplication();
                }*/
            val intent=Intent(activity,SleepStart::class.java) //background2에서 자동실행
            intent.putExtra("si",selectedItemIndex)
            startActivity(intent)
        }
        return view
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
    private fun findDate(): String {
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

    private fun renameFile(){
        val file = File("/mnt/sdcard/$foldername/$filename")
        val rename = File("/mnt/sdcard/$foldername/$filename-${findDate()}")
        file.renameTo(rename)
    }

    override fun onPause() {
        super.onPause()
        Log.e("Fragment0", "onPause()")
    }

    override fun onStop() {
        super.onStop()
        Log.e("Fragment0", "onStop()")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("Fragment0", "onDestroy()")
    }

    inner class RenewWL():Thread(){
        lateinit var wl : PowerManager.WakeLock
        var recording = false
        fun wlstart(){
            wl = (requireActivity().getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK , "Sleepwell::WakelockTag").apply {
                    acquire(10*60*1000L /*10 minutes*/)
                }
            }
            Log.d("wakelock","start")
            recording = true
        }
        fun wlrelease(){
            wl.release()
            Log.d("wakelock","release")
        }
        override fun run() {
            while(!isInterrupted){
                Log.d("Wakelock","Thread start")
                if(!recording){
                    wlstart()
                }else{
                    wlrelease()
                    wlstart()
                }
                SystemClock.sleep(10*60*900L)
                Log.d("Wakelock","renew")
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
}