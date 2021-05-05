package kr.ac.kpu.sleepwell

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CompoundButton
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.annotations.SerializedName
import kotlinx.android.synthetic.main.activity_hue.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.GET
import java.lang.Thread.sleep


data class Response(
        @SerializedName("success")
        var success:ResBody,
        @SerializedName("error")
        var error:ResBody,
        @SerializedName("1")
        var lightid1:ResBody,
        @SerializedName("2")
        var lightid2:ResBody,
        @SerializedName("3")
        var lightid3:ResBody,
        @SerializedName("4")
        var lightid4:ResBody,
        @SerializedName("5")
        var lightid5:ResBody,
        @SerializedName("6")
        var lightid6:ResBody,
        @SerializedName("7")
        var lightid7:ResBody,
        @SerializedName("8")
        var lightid8:ResBody,
        @SerializedName("state")
        var lightstate: ResBody
)

data class ResBody(
        @SerializedName("description")
        var description: String,
        @SerializedName("username")
        var userid:String,
        @SerializedName("state")
        var lightstate: ResBody?,
        @SerializedName("on")
        var onoff:Boolean,
        @SerializedName("sat")
        var sat : Int,
        @SerializedName("hue")
        var hue : Int,
        @SerializedName("bri")
        var bri : Int,
        @SerializedName("address")
        var address:String
)

interface Hueuserid{
    @POST("/api")
    fun getuserid(
            @Body request: okhttp3.RequestBody
    ):Call<List<Response>>
}

interface Huelink{
    @GET("lights/new")
    fun findnewlight():Call<Response>
    @GET("lights")
    fun getlightsid():Call<Response>
}

interface HueLight{
    @GET(".")
    fun getlightstate():Call<Response>
    @PUT("state")
    fun lightservice(
            @Body request: okhttp3.RequestBody
    ):Call<List<Response>>
}


class HueActivity : AppCompatActivity() {

    //192.168.0.3
    private val http = "http://"
    var bridge = http
    var hueid = "."
    val huenm : String = "test"
    private lateinit var hueurl : String
    var huelighturl = arrayOfNulls<String>(size=8)
    var lightid = arrayOfNulls<ResBody>(size=8)
    var numlight = arrayOfNulls<Int>(size=8)
    var huelightstate = arrayOfNulls<Retrofit>(size=8)
    var huelightremote = arrayOfNulls<HueLight>(size=8)
    var lightbri : Int = 0
    val userkey = FirebaseAuth.getInstance().currentUser?.uid.toString()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hue)

        Log.d("userkey",userkey)
/*
        val getstore = db.collection(userkey).document("hue")
        getstore.get()
                .addOnSuccessListener { document ->
                    if(document != null){
                        bridge = document["address"].toString()
                        hueid = document["hueid"].toString()
                        Log.d("read complete",document["address"].toString())
                        Log.d("read complete",document["hueid"].toString())
                        Log.d("username", hueid)
                        testTV.text = hueid
                        hueurl = bridge+"/api/"+hueid+"/"
                        ipET.hint = "로드 완료"
                        val huelink = Retrofit.Builder()
                                .baseUrl(hueurl)
                                .addConverterFactory(GsonConverterFactory.create())
                                .build()
                        val link = huelink.create(Huelink::class.java)
                        val newlight = link.findnewlight()
                        Runnable {
                            newlight.enqueue(object : Callback<Response>{
                                override fun onResponse(call: Call<Response>, response: retrofit2.Response<Response>) {
                                    Log.d("link","complete : "+response.toString())
                                }

                                override fun onFailure(call: Call<Response>, t: Throwable) {
                                    Log.d("link","failed : "+t)
                                }

                            })
                        }.run()

                        sleep(100)

                        val lightsid = link.getlightsid()
                        Runnable {
                            lightsid.enqueue(object : Callback<Response>{
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

                        sleep(100)

                        for(i in 0..7){
                            if(huelighturl[i]!=null){
                                huelightstate[i] = Retrofit.Builder()
                                        .baseUrl(huelighturl[i].toString())
                                        .addConverterFactory(GsonConverterFactory.create())
                                        .build()
                                Log.d("id","id : "+(i+1).toString()+" complete")
                                huelightremote[i] = huelightstate[i]?.create(HueLight::class.java)
                                Runnable {
                                    huelightremote[i]?.getlightstate()?.enqueue(object : Callback<Response>{
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
                        useridBtn.isClickable = false
                        linkBtn.isClickable = false
                    } else{
                        Log.d("read failed","no document")
                    } 
                }
                .addOnFailureListener { exception ->
                    Log.d("failed with",exception.toString())
                }
*/
        useridBtn.setOnClickListener{
            if(ipET.text.isBlank()){
                Toast.makeText(this,"휴 브릿지의 ip 주소를 입력해주세요",Toast.LENGTH_SHORT).show()
            }else{
                bridge = http+ipET.text.toString()
                val huesetting = Retrofit.Builder()
                        .baseUrl(bridge)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                val api = huesetting.create(Hueuserid::class.java)
                val newuser = JSONObject()
                newuser.put("devicetype", huenm)
                val newuserString = newuser.toString()
                val userrequest = newuserString.toRequestBody("application/json".toMediaTypeOrNull())
                Runnable {
                    api.getuserid(userrequest).enqueue(object : Callback<List<Response>> {

                        override fun onFailure(call: Call<List<Response>>, t: Throwable) {
                            Log.d("fail", "failed to get userid : " + "$t")
                            Log.d("failresponse", "$call")
                            testTV.text = t.toString()
                        }


                        override fun onResponse(call: Call<List<Response>>, response: retrofit2.Response<List<Response>>) {
                            Log.d("Response:: ", response.toString())
                            Log.d("Response body:: ", response.body().toString())
                            val idget: ResBody? = response.body()?.get(0)?.success
                            if (idget != null) {
                                hueid = idget.userid
                                Log.d("username", hueid)
                                testTV.text = hueid
                                hueurl = bridge + "/api/" + hueid + "/"
                            }
                        }
                    })
                }.run()
                if(hueid == "."){
                    Toast.makeText(this,"브릿지의 링크 버튼을 눌러주세요",Toast.LENGTH_SHORT).show()
                }
                else{
                    val huestore = db.collection(userkey)
                    val hueaddress = hashMapOf(
                            "address" to bridge,
                            "hueid" to hueid
                    )
                    huestore.document("hue").set(hueaddress)
                }

            }
        }
        linkBtn.setOnClickListener {
            val huelink = Retrofit.Builder()
                    .baseUrl(hueurl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            val link = huelink.create(Huelink::class.java)
            val newlight = link.findnewlight()
            Runnable {
                newlight.enqueue(object : Callback<Response>{
                    override fun onResponse(call: Call<Response>, response: retrofit2.Response<Response>) {
                        Log.d("link","complete : "+response.toString())
                    }

                    override fun onFailure(call: Call<Response>, t: Throwable) {
                        Log.d("link","failed : "+t)
                    }

                })
            }.run()

            sleep(100)

            val lightsid = link.getlightsid()
            Runnable {
                lightsid.enqueue(object : Callback<Response>{
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

            sleep(100)

            for(i in 0..7){
                if(huelighturl[i]!=null){
                    huelightstate[i] = Retrofit.Builder()
                            .baseUrl(huelighturl[i].toString())
                            .addConverterFactory(GsonConverterFactory.create())
                            .build()
                    Log.d("id","id : "+(i+1).toString()+" complete")
                    huelightremote[i] = huelightstate[i]?.create(HueLight::class.java)
                    Runnable {
                        huelightremote[i]?.getlightstate()?.enqueue(object : Callback<Response>{
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

        }


        briseekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                lightbri = progress
                testTV.text = lightbri.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                lightbri = seekBar!!.progress
                testTV.text = lightbri.toString()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                lightbri = seekBar!!.progress
                testTV.text = lightbri.toString()
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

        onoffswitch.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
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
                            }

                        })
                    }.run()
                }
            }
        })
    }
}