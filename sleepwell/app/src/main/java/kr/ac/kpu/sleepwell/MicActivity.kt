package kr.ac.kpu.sleepwell

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.media.MediaRecorder
import android.os.Environment
import kotlinx.android.synthetic.main.activity_mic.*
import java.io.File
import java.io.IOException

class MicActivity : AppCompatActivity() {

    private var Recorder : MediaRecorder? = null
    private lateinit var path : File
    private lateinit var Fname : String
    private var audionum : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mic)

        buttonRecord.setOnClickListener {
            recordStart()
        }
        buttonStop.setOnClickListener {
            stopRecord()
        }

    }

    private fun recordStart(){
        path = Environment.getDataDirectory()
        val newaudio = File(path,"recorded$audionum.aac")
        Fname = newaudio.absolutePath
        Recorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        Recorder!!.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
        Recorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT)
        Recorder!!.setOutputFile(Fname)

        try {
            Recorder!!.prepare()
            Recorder!!.start()
        } catch (e: IOException){
            e.printStackTrace()
        }
    }

    private fun stopRecord(){
        if(Recorder!=null){
            Recorder!!.stop()
            Recorder!!.release()
            audionum++
            Recorder = null
        }
    }
}
