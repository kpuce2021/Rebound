package kr.ac.kpu.sleepwell

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.CompoundButton
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_sleep_start.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_sleep_start.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class SleepStart : AppCompatActivity() {

    var isRunning:Boolean=false
    lateinit var backgroundintent:Intent
    lateinit var dayresultintent:Intent
    var countnum=4

    private val http = "http://"
    var hueid = "."
    var huelighturl = arrayOfNulls<String>(size=8)
    var lightid = arrayOfNulls<ResBody>(size=8)
    var numlight = arrayOfNulls<Int>(size=8)
    var huelightstate = arrayOfNulls<Retrofit>(size=8)
    var huelightremote = arrayOfNulls<HueLight>(size=8)
    var lightbri : Int = 0
    val userkey = FirebaseAuth.getInstance().currentUser?.uid.toString()
    private fun findDateFactor(): String {
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

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sleep_start)
        val myapp=application as MyglobalArraylist
        backgroundintent= Intent(this,backgroundservice::class.java)
        dayresultintent= Intent(this,Day_resultAC::class.java)
        if (intent.hasExtra("si")){
            val day = findDateFactor()
            var selectedItemIndex = intent.getIntegerArrayListExtra("si")
            dayresultintent.putExtra("si",selectedItemIndex)
            dayresultintent.putExtra("day",day)
        }
        backgroundintent.setAction("startForeground")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(backgroundintent)
        }else{
            startService(backgroundintent)
        }
        /*btn_stop.setOnClickListener {
            stopService(intent)
            val intent= Intent(this,Day_resultAC::class.java)
            startActivity(intent)
            finish()
        }*/


        btn_stop.setOnTouchListener { view, motionEvent ->
            when(motionEvent.action){
                MotionEvent.ACTION_DOWN->{
                    tv_count.isVisible=true
                    isRunning=true
                    val UI_thread:Thread=runUI_thread()
                    UI_thread.start()
                }
                MotionEvent.ACTION_UP->{
                    tv_count.isVisible=false
                    isRunning=false
                    countnum=4
                }
            }
            return@setOnTouchListener false
        }
        val huebtn = db.collection("hue").document(userkey)
        huebtn.get()
                .addOnSuccessListener { document ->
                    if(document != null){
                        val able = document["able"].toString()
                        if(able=="1"){
                            btn_huelink.visibility = View.VISIBLE
                            btn_huelink2.visibility = View.GONE
                        }
                        Log.d("read complete",document["able"].toString())
                    }
                }
                .addOnFailureListener{ document ->
                    Log.d("failed with",document.toString())
                }




        btn_huelink.setOnClickListener {
            val getstore = db.collection("hue").document(userkey)
            getstore.get()
                    .addOnSuccessListener { document ->
                        if(document != null){
                            val bridge = document["address"].toString()
                            hueid = document["hueid"].toString()
                            Log.d("read complete",document["address"].toString())
                            Log.d("read complete",document["hueid"].toString())
                            Log.d("username", hueid)
                            val hueurl = bridge+"/api/"+hueid+"/"
                            val huelink = Retrofit.Builder()
                                    .baseUrl(hueurl)
                                    .addConverterFactory(GsonConverterFactory.create())
                                    .build()
                            val link = huelink.create(Huelink::class.java)
                            val newlight = link.findnewlight()
                            Runnable {
                                newlight.enqueue(object : Callback<Response> {
                                    override fun onResponse(call: Call<Response>, response: retrofit2.Response<Response>) {
                                        Log.d("link","complete : "+response.toString())
                                    }

                                    override fun onFailure(call: Call<Response>, t: Throwable) {
                                        Log.d("link","failed : "+t)
                                    }

                                })
                            }.run()

                            val lightsid = link.getlightsid()
                            Runnable {
                                lightsid.enqueue(object : Callback<Response> {
                                    override fun onFailure(call: Call<Response>, t: Throwable) {
                                        Log.d("lights id","failed : "+t)
                                        Log.d("light body","failed : "+call.toString())
                                    }

                                    override fun onResponse(call: Call<Response>, response: retrofit2.Response<Response>) {
                                        lightid[0] = response.body()?.lightid1
                                        lightid[1] = response.body()?.lightid2
                                        lightid[2] = response.body()?.lightid3
                                        lightid[3] = response.body()?.lightid4
                                        lightid[4] = response.body()?.lightid5
                                        lightid[5] = response.body()?.lightid6
                                        lightid[6] = response.body()?.lightid7
                                        lightid[7] = response.body()?.lightid8

                                        for(i in 0..7){
                                            if(lightid[i]!=null){
                                                numlight[i]=i+1
                                                huelighturl[i]=hueurl+"lights/"+(i+1).toString()+"/"
                                            }
                                        }

                                        Log.d("lights id","complete : "+numlight.toString())
                                    }

                                })
                            }.run()

                            for(i in 0..7){
                                if(huelighturl[i]!=null){
                                    huelightstate[i] = Retrofit.Builder()
                                            .baseUrl(huelighturl[i].toString())
                                            .addConverterFactory(GsonConverterFactory.create())
                                            .build()
                                    Log.d("id","id : "+(i+1).toString()+" complete")
                                    huelightremote[i] = huelightstate[i]?.create(HueLight::class.java)
                                    Runnable {
                                        huelightremote[i]?.getlightstate()?.enqueue(object : Callback<Response> {
                                            override fun onFailure(call: Call<Response>, t: Throwable) {
                                                Log.d("remote make","failed : "+(i+1).toString())
                                            }

                                            override fun onResponse(call: Call<Response>, response: retrofit2.Response<Response>) {
                                                Log.d("remote make","complete : "+(i+1).toString())
                                                //여기에 리모콘 만들거 만들기, 아직 모름
                                            }

                                        })
                                    }.run()
                                }
                            }


                        } else{
                            Log.d("read failed","no document")
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.d("failed with",exception.toString())
                    }
            hueswitch.visibility = View.VISIBLE
            huebri.visibility = View.VISIBLE
        }
        btn_huelink2.setOnClickListener {
            val getstore = db.collection("hue").document(userkey)
            getstore.get()
                    .addOnSuccessListener { document ->
                        if(document != null){
                            val bridge = document["address"].toString()
                            hueid = document["hueid"].toString()
                            Log.d("read complete",document["address"].toString())
                            Log.d("read complete",document["hueid"].toString())
                            Log.d("username", hueid)
                            val hueurl = bridge+"/api/"+hueid+"/"
                            val huelink = Retrofit.Builder()
                                    .baseUrl(hueurl)
                                    .addConverterFactory(GsonConverterFactory.create())
                                    .build()
                            val link = huelink.create(Huelink::class.java)
                            val newlight = link.findnewlight()
                            Runnable {
                                newlight.enqueue(object : Callback<Response> {
                                    override fun onResponse(call: Call<Response>, response: retrofit2.Response<Response>) {
                                        Log.d("link","complete : "+response.toString())
                                    }

                                    override fun onFailure(call: Call<Response>, t: Throwable) {
                                        Log.d("link","failed : "+t)
                                    }

                                })
                            }.run()

                            val lightsid = link.getlightsid()
                            Runnable {
                                lightsid.enqueue(object : Callback<Response> {
                                    override fun onFailure(call: Call<Response>, t: Throwable) {
                                        Log.d("lights id","failed : "+t)
                                        Log.d("light body","failed : "+call.toString())
                                    }

                                    override fun onResponse(call: Call<Response>, response: retrofit2.Response<Response>) {
                                        lightid[0] = response.body()?.lightid1
                                        lightid[1] = response.body()?.lightid2
                                        lightid[2] = response.body()?.lightid3
                                        lightid[3] = response.body()?.lightid4
                                        lightid[4] = response.body()?.lightid5
                                        lightid[5] = response.body()?.lightid6
                                        lightid[6] = response.body()?.lightid7
                                        lightid[7] = response.body()?.lightid8

                                        for(i in 0..7){
                                            if(lightid[i]!=null){
                                                numlight[i]=i+1
                                                huelighturl[i]=hueurl+"lights/"+(i+1).toString()+"/"
                                            }
                                        }

                                        Log.d("lights id","complete : "+numlight.toString())
                                    }

                                })
                            }.run()

                            for(i in 0..7){
                                if(huelighturl[i]!=null){
                                    huelightstate[i] = Retrofit.Builder()
                                            .baseUrl(huelighturl[i].toString())
                                            .addConverterFactory(GsonConverterFactory.create())
                                            .build()
                                    Log.d("id","id : "+(i+1).toString()+" complete")
                                    huelightremote[i] = huelightstate[i]?.create(HueLight::class.java)
                                    Runnable {
                                        huelightremote[i]?.getlightstate()?.enqueue(object : Callback<Response> {
                                            override fun onFailure(call: Call<Response>, t: Throwable) {
                                                Log.d("remote make","failed : "+(i+1).toString())
                                            }

                                            override fun onResponse(call: Call<Response>, response: retrofit2.Response<Response>) {
                                                Log.d("remote make","complete : "+(i+1).toString())
                                                //여기에 리모콘 만들거 만들기, 아직 모름
                                            }

                                        })
                                    }.run()
                                }
                            }


                        } else{
                            Log.d("read failed","no document")
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.d("failed with",exception.toString())
                    }
            hueswitch.visibility = View.VISIBLE
            huebri.visibility = View.VISIBLE
        }

        huebri.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                lightbri = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                lightbri = seekBar!!.progress
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                lightbri = seekBar!!.progress
                val bri = JSONObject()
                bri.put("bri",lightbri)
                val briString = bri.toString()
                val brirequest = briString.toRequestBody("application/json".toMediaTypeOrNull())
                Runnable {
                    huelightremote[0]?.lightservice(brirequest)?.enqueue(object : Callback<List<Response>>{
                        override fun onFailure(call: Call<List<Response>>, t: Throwable) {
                            Log.d("bri","failed : "+t)
                        }

                        override fun onResponse(call: Call<List<Response>>, response: retrofit2.Response<List<Response>>) {
                            Log.d("bri","success : "+response.body().toString())
                        }

                    })
                }.run()
            }

        })
        hueswitch.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                if(isChecked){
                    val lighton = JSONObject()
                    lighton.put("on",true)
                    val lightonString = lighton.toString()
                    val lightonrequest = lightonString.toRequestBody("application/json".toMediaTypeOrNull())
                    Runnable {
                        huelightremote[0]?.lightservice(lightonrequest)?.enqueue(object : Callback<List<Response>>{
                            override fun onFailure(call: Call<List<Response>>, t: Throwable) {
                                Log.d("on","failed : "+t)
                            }

                            override fun onResponse(call: Call<List<Response>>, response: retrofit2.Response<List<Response>>) {
                                Log.d("on","success : "+response.body().toString())
                                btn_huelink.visibility = View.GONE
                                btn_huelink2.visibility = View.VISIBLE
                            }

                        })
                    }.run()
                }else{
                    val lightoff = JSONObject()
                    lightoff.put("on",false)
                    val lightoffString = lightoff.toString()
                    val lightoffrequest = lightoffString.toRequestBody("application/json".toMediaTypeOrNull())
                    Runnable {
                        huelightremote[0]?.lightservice(lightoffrequest)?.enqueue(object : Callback<List<Response>>{
                            override fun onFailure(call: Call<List<Response>>, t: Throwable) {
                                Log.d("off","failed : "+t)
                            }

                            override fun onResponse(call: Call<List<Response>>, response: retrofit2.Response<List<Response>>) {
                                Log.d("off","success : "+response.body().toString())
                                btn_huelink.visibility = View.VISIBLE
                                btn_huelink2.visibility = View.GONE
                            }

                        })
                    }.run()
                }
            }
        })
    }

    inner class runUI_thread():Thread(){
        override fun run() {
            countnum=4
            while(isRunning){
                SystemClock.sleep(500)
                countnum-=1
                if(countnum<0){
                    isRunning=false
                    stopService(backgroundintent)
                    startActivity(dayresultintent)
                    finish()
                    break
                }
                Log.d("text",countnum.toString())
                runOnUiThread{
                    tv_count.setText(countnum.toString())
                }
            }
        }
    }
    override fun onBackPressed() {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Sleep Well")
            .setIcon(R.mipmap.ic_launcher_round)
            .setMessage("수면을 종료하려면 Stop 버튼을 누르세요.")
            .setNegativeButton("닫기",null)
            .create()
        alertDialog.show()
    }
}