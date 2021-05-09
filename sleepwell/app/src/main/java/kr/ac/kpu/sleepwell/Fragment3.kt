package kr.ac.kpu.sleepwell

import android.app.ActionBar
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_3.*
import org.w3c.dom.Text

class Fragment3 : Fragment() {

    //private var userEmail:String=""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var v :View = inflater.inflate(R.layout.fragment_3, container, false)

        var textemail:TextView=v.findViewById(R.id.text_email)  //이메일
        var btn_inform:Button=v.findViewById(R.id.btn_inform)   //내정보
        var btn_notice:Button=v.findViewById(R.id.btn_notice)   //공지사항
        var btn_sleepstart:Button=v.findViewById(R.id.btn_sleepstart)   //수면시작알람
        var btn_howtouse:Button=v.findViewById(R.id.btn_howtouse)   //사용법
        var btn_feedback:Button=v.findViewById(R.id.btn_feedback)   //피드백
        var btn_call:Button=v.findViewById(R.id.btn_call)   //고객센터
        var btn_FAQ:Button=v.findViewById(R.id.btn_FAQ)   //FAQ
        var btn_logout:Button=v.findViewById(R.id.btn_logout)   //로그아웃

        //firebase로 이메일 가져오기
        val user= Firebase.auth.currentUser
        if(user!=null){
            user?.let {
                for(profile in it.providerData){
                    textemail.setText(profile.email.toString())
                }
            }
        }
        else
            Toast.makeText(activity,"로그인 되지 않았습니다.", Toast.LENGTH_SHORT).show()

        
        btn_call.setOnClickListener {
            val hueintent = Intent(activity,HueActivity::class.java)
            startActivity(hueintent)
        }

        return v
    }
}