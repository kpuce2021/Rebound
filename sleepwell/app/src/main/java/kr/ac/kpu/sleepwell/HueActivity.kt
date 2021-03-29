package kr.ac.kpu.sleepwell

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
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
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.GET


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
        @SerializedName("type")
        var code:Int,
        @SerializedName("address")
        var address:String
)

interface HueService{
    @POST("/api")
    fun getuserid(
            @Body request: okhttp3.RequestBody
    ):Call<List<Response>>
}


class HueActivity : AppCompatActivity() {
    //192.168.0.3
    private val http = "http://"
    private lateinit var bridge :String
    private lateinit var hueid : String
    val huenm : String = "test"
    private lateinit var hueurl : String



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hue)


        useridBtn.setOnClickListener{
            bridge=http+ipET.text.toString()
            val retrofitsetting = Retrofit.Builder()
                    .baseUrl(bridge)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            val api = retrofitsetting.create(HueService::class.java)
            val newuser =JSONObject()
            newuser.put("devicetype",huenm)
            val newuserString = newuser.toString()
            val userrequest = newuserString.toRequestBody("application/json".toMediaTypeOrNull())
            Runnable {
                api.getuserid(userrequest).enqueue(object : Callback<List<Response>> {

                    override fun onFailure(call: Call<List<Response>>, t: Throwable) {
                        Log.d("fail", "failed to get userid : "+ "$t")
                        Log.d("failresponse","$call")
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
                            hueurl = bridge+"/api/"+hueid
                        }
                    }
                })
            }.run()
        }
    }
}