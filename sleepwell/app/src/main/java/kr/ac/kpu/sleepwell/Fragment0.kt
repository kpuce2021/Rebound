package kr.ac.kpu.sleepwell

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
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

    private val sensorManager by lazy {
        requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager  //센서 매니저에대한 참조를 얻기위함
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        var i = 0
        var view = inflater.inflate(R.layout.fragment_0,container,false)
        view.sleep_btn.setOnClickListener{
            val day = findDate()
            if(i==0) {
                sensorManager.registerListener(this,    // 센서 이벤트 값을 받을 리스너 (현재의 액티비티에서 받음)
                        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),// 센서 종류
                        SensorManager.SENSOR_DELAY_NORMAL)// 수신 빈도
                i = 1
                view.sleep_btn.setText("수면 중지")
            }
            else{
                sensorManager.unregisterListener(this)
                db.collection(userkey).document(day)
                    .set(data, SetOptions.merge())
                    .addOnSuccessListener {Log.d("tag", "DocumentSnapshot added successfully") }
                    .addOnFailureListener { e -> Log.w("tag", "Error adding document", e) }
                i = 0
                view.sleep_btn.setText("수면 시작")
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

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            var x = event.values[0]
            var y = event.values[1]
            var z = event.values[2]
            var x2 = Math.pow(x.toDouble(), 2.0)//x제곱
            var y2 = Math.pow(y.toDouble(), 2.0)//y제곱
            var z2 = Math.pow(z.toDouble(), 2.0)//z제곱
            var m = Math.sqrt(x2+y2+z2)//움직임 값
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