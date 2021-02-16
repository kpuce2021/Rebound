package kr.ac.kpu.sleepwell

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_0.view.*
import java.io.*


class Fragment0 : Fragment(), SensorEventListener {


    val foldername: String = "LogFolder"
    val filename = "sensorlog.txt"


    private val sensorManager by lazy {
        activity!!.getSystemService(Context.SENSOR_SERVICE) as SensorManager  //센서 매니저에대한 참조를 얻기위함
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    private fun Permissions(): Boolean {
        val permissionWRITE_EXTERNAL_STORAGE = activity?.applicationContext?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.WRITE_EXTERNAL_STORAGE) }
        val permissionREAD_EXTERNAL_STORAGE=activity?.applicationContext?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.READ_EXTERNAL_STORAGE) }
        val listPermissionsNeeded: MutableList<String> = ArrayList()
        if (permissionWRITE_EXTERNAL_STORAGE != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (permissionREAD_EXTERNAL_STORAGE != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(activity?.applicationContext as Activity, listPermissionsNeeded.toTypedArray(), 100)
            return false
        }
        return true
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        var i = 0
        var view = inflater.inflate(R.layout.fragment_0,container,false)
        //if(!Permissions())
            //finish()
        view.sleep_btn.setOnClickListener{
            if(i==0) {
                sensorManager.registerListener(this,    // 센서 이벤트 값을 받을 리스너 (현재의 액티비티에서 받음)
                        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),// 센서 종류
                        SensorManager.SENSOR_DELAY_NORMAL)// 수신 빈도
                i = 1
                view.sleep_btn.setText("수면 중지")
            }
            else{
                sensorManager.unregisterListener(this)
                i = 0
                view.sleep_btn.setText("수면 시작")
            }
        }
        return view
    }
    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}

    fun finish(){
        finish()
    }

    fun WriteTextFile(foldername: String, filename: String, contents: String?) {
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
            var contents = "x:${event.values[0]}, y:${event.values[1]}, z:${event.values[2]}, m:${m}"
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
}