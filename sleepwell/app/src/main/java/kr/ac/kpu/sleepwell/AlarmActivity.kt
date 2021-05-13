package kr.ac.kpu.sleepwell

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.SetOptions
import kotlinx.android.synthetic.main.activity_alarm.*
import java.util.*

class AlarmActivity : AppCompatActivity() {
    var alarm_data = hashMapOf(
            "use_alarm" to true, //분을 단위로 사용
            "alarm" to "설정 없음")
    val dbRef = db.collection("alarm").document(userkey)

    private fun showTimePicker() {
        val cal = Calendar.getInstance()
        TimePickerDialog(this,android.R.style.Theme_Holo_Dialog_NoActionBar, TimePickerDialog.OnTimeSetListener { timePicker, h, m ->

            var am_pm = ""
            var datetime = Calendar.getInstance()
            datetime.set(Calendar.HOUR_OF_DAY, h)
            datetime.set(Calendar.MINUTE, m)
            if (datetime.get(Calendar.AM_PM) == Calendar.AM)
                am_pm = "AM"
            else if (datetime.get(Calendar.AM_PM) == Calendar.PM)
                am_pm = "PM"

            var hour = if (datetime[Calendar.HOUR] === 0) "12" else datetime[Calendar.HOUR].toString() + ""
            var minute = datetime.get(Calendar.MINUTE)
            if(m.toInt() > 9) {
                var text = "$hour:$m $am_pm"
                alarm_t.setText(text)
            }
            else{
                var text = "$hour:0$m $am_pm"
                alarm_t.setText(text)
            }
            dbRef.update("alarm", alarm_t.text)
                    .addOnSuccessListener { Log.d("DB", "DocumentSnapshot successfully updated!") }
                    .addOnFailureListener { e -> Log.w("DB", "Error updating document", e) }
            /*Toast.makeText(this, "$h:$m", Toast.LENGTH_SHORT).show()*/
        }, cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE), false).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)
        dbRef.addSnapshotListener(EventListener<DocumentSnapshot>{snapshot,e->
            if(e != null){
                Log.w("tag", "Listen failed.", e)
                return@EventListener
            }
            if(snapshot != null && snapshot.exists()){
                var ua = snapshot?.data!!["use_alarm"].toString().toBoolean()
                var at = snapshot?.data!!["alarm"].toString()
                alarm_switch.isChecked = ua == true
                alarm_t.setText(at)
            }
        })

        alarm_t.setOnClickListener{
            showTimePicker()
        }

        alarm_switch.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                dbRef.set(alarm_data, SetOptions.merge())
                        .addOnSuccessListener { Log.d("DB", "DocumentSnapshot successfully written!") }
                        .addOnFailureListener { e -> Log.w("DB", "Error writing document", e) }
                dbRef.update("alarm", alarm_t.text)
                        .addOnSuccessListener { Log.d("DB", "DocumentSnapshot successfully updated!") }
                        .addOnFailureListener { e -> Log.w("DB", "Error updating document", e) }
            }
            else{
                dbRef.update("use_alarm", false)
                        .addOnSuccessListener { Log.d("DB", "DocumentSnapshot successfully updated!") }
                        .addOnFailureListener { e -> Log.w("DB", "Error updating document", e) }
            }
        }
        backbutton.setOnClickListener {
            val intent= Intent(this,MainActivity::class.java)
            startActivity(intent)
        }
    }
}