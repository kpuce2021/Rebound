package kr.ac.kpu.sleepwell

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import kotlinx.android.synthetic.main.activity_main.*

private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
class MainActivity : AppCompatActivity() {

    private val multiplePermissionsCode=100
    private var permissionToRecordAccepted = false

    private val requiredPermissions = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        checkPermissions()  //퍼미션 요청

        val fragmentAdapter=MainAdapter(supportFragmentManager)
        viewpager_main.adapter=fragmentAdapter

        //abs_main.getChildAt(0).setBackgroundColor(Color.parseColor("#00000000"))

        tabs_main.setupWithViewPager(viewpager_main)


        val images = ArrayList<Int>()
        images.add(R.drawable.ic_baseline_bedtime_24_3)
        images.add(R.drawable.ic_baseline_today_24)
        images.add(R.drawable.ic_baseline_assessment_24)
        images.add(R.drawable.ic_baseline_person_24)
        val images2 = ArrayList<Int>()
        images.add(R.drawable.ic_baseline_bedtime_24_2)
        images.add(R.drawable.ic_baseline_today_24_2)
        images.add(R.drawable.ic_baseline_assessment_24_2)
        images.add(R.drawable.ic_baseline_person_24_2)
        for (i in 0..3) tabs_main.getTabAt(i)!!.setIcon(images[i])
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    private fun checkPermissions(){
        var rejectedPermissionList = ArrayList<String>()

        //필요한 퍼미션들을 하나씩 끄집어내서 현재 권한을 받았는지 체크
        for(permission in requiredPermissions){
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                //만약 권한이 없다면 rejectedPermissionList에 추가
                rejectedPermissionList.add(permission)
            }
        }
        //거절된 퍼미션이 있다면...
        if(rejectedPermissionList.isNotEmpty()){
            //권한 요청!
            val array = arrayOfNulls<String>(rejectedPermissionList.size)
            ActivityCompat.requestPermissions(this, rejectedPermissionList.toArray(array), multiplePermissionsCode)
        }
    }

    /*private fun checkpermission2(){
        val permissionWRITE_EXTERNAL_STORAGE = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val permissionRECORD_AUDIO=ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO)
        val permissionREAD_EXTERNAL_STORAGE=ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)
        if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){

        }

    }*/

    /*override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            multiplePermissionsCode->{
                if(grantResults.isNotEmpty()){
                    for((i,permission) in permissions.withIndex()){
                        if(grantResults[i]!=PackageManager.PERMISSION_GRANTED){
                            Log.i("failure","failure")
                        }
                    }
                }
            }
        }
    }*/

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        when(requestCode){
            multiplePermissionsCode->{
                if(grantResults.isNotEmpty()){
                    for((i, permission) in permissions.withIndex()) {
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            //권한 획득 실패
                            Log.i("TAG", "The user has denied to $permission")
                            Log.i("TAG", "I can't work for you anymore then. ByeBye!")
                            finish()
                        }
                    }
                }
            }
        }
    }
}

