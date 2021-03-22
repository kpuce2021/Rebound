package kr.ac.kpu.sleepwell

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.annotations.SerializedName
import kotlinx.android.synthetic.main.activity_hue.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST


data class Response(
        @SerializedName("success")
        var success:List<ResBody>,
        @SerializedName("error")
        var error:List<ResBody>,
        @SerializedName("1")
        var lightid1:List<ResBody>,
        @SerializedName("2")
        var lightid2:List<ResBody>,
        @SerializedName("3")
        var lightid3:List<ResBody>,
        @SerializedName("4")
        var lightid4:List<ResBody>,
        @SerializedName("5")
        var lightid5:List<ResBody>,
        @SerializedName("6")
        var lightid6:List<ResBody>,
        @SerializedName("7")
        var lightid7:List<ResBody>,
        @SerializedName("8")
        var lightid8:List<ResBody>,
        @SerializedName("state")
        var lightstate: List<ResBody>

)

data class ResBody(
        @SerializedName("description")
        var description: String,
        @SerializedName("username")
        var userid:String,
        @SerializedName("state")
        var lightstate:List<ResBody>,
        @SerializedName("on")
        var onoff:Boolean,
        @SerializedName("sat")
        var sat : Int,
        @SerializedName("hue")
        var hue : Int,
        @SerializedName("bri")
        var bri : Int
)

interface HueService{
    @FormUrlEncoded
    @POST("api")
    fun getuserid(
            @Field("devicetype") huenm:String
    ):Call<Response>
}


class HueActivity : AppCompatActivity() {

    private val bridge = "https://192.168.0.3/"
    private lateinit var hueid : String
    val huenm : String = "test"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hue)

        useridBtn.setOnClickListener{
            val retrofitsetting = Retrofit.Builder()
                    .baseUrl(bridge)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            val api = retrofitsetting.create(HueService::class.java)
            Runnable {
                api.getuserid(huenm).enqueue(object : Callback<Response> {
                    override fun onFailure(call: Call<Response>, t: Throwable) {
                        Log.d("userid", "failed to get userid "+huenm)
                    }

                    override fun onResponse(call: Call<Response>, response: retrofit2.Response<Response>) {
                        Log.d("Response:: ", response.body().toString())
                        Log.d("Res error:: ", response.body()?.error.toString())
                        Log.d("Res success:: ", response.body()?.success.toString())
                        val idget: List<ResBody>? = response.body()?.success
                        if (idget != null) {
                            hueid = idget[0].userid
                            Log.d("username", hueid)
                            testTV.text = hueid
                        }
                    }
                })
            }.run()
        }
    }
}