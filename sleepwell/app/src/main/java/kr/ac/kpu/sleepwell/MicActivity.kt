package kr.ac.kpu.sleepwell

import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_mic.*
import java.io.File
import java.io.IOException
import java.util.*

class MicActivity : AppCompatActivity() {

    private var Recorder : MediaRecorder? = null
    private lateinit var path : File
    private lateinit var Fname : String
    private var audionum : Int = 0
    private var player : MediaPlayer? = null
    private var position : Int = 0
    private var handler : Handler? = null
    private var decibel : Int? = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mic)
        var detect = Micdetect()
        handler?.postDelayed(detect,1000)

        buttonRecord.setOnClickListener {
            recordStart()
        }
        buttonStop.setOnClickListener {
            stopRecord()
        }

    }
    inner class Micdetect : Thread(){
        override fun run() {
            if(getAmplitude()!! > 50){
                decibel = getAmplitude()
                decibelTV.text = "Decibel = $decibel"
                recordStart()
            }else {
                decibel = getAmplitude()
                decibelTV.text = "Decibel = $decibel"
                start()
            }
        }
    }

    private fun recordStart(){
        closeRecord()
        path = Environment.getDataDirectory()
        val newaudio = File(path,"recorded$audionum")
        Fname = newaudio.absolutePath
        Recorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        Recorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        Recorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT)
        Recorder!!.setOutputFile(Fname)

        try {
            Recorder!!.prepare()
            Recorder!!.start()
        } catch (e: IOException){
            e.printStackTrace()
        }
    }
    private fun closeRecord(){
        if(Recorder != null){
            Recorder!!.stop()
            Recorder!!.release()
            Recorder = null
        }
    }

    private fun stopRecord(){
        if(Recorder!=null){
            Recorder!!.stop()
            Recorder!!.release()
            audionum++
            Recorder = null
        }
        start()
    }

    fun getAmplitude(): Int? {
        return Recorder?.getMaxAmplitude()
    }
    fun start() {
        closeRecord()
        if (Recorder == null) {
            Recorder = MediaRecorder()
            Recorder!!.setAudioSource(
                    MediaRecorder.AudioSource.MIC)
            Recorder!!.setOutputFormat(
                    MediaRecorder.OutputFormat.THREE_GPP)
            Recorder!!.setAudioEncoder(
                    MediaRecorder.AudioEncoder.DEFAULT)
            Recorder!!.setOutputFile("/dev/null")
            try {
                Recorder!!.prepare()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            Recorder!!.start()
        }
    }


    private fun playAudio(){
        try {
            closeAudio()
            player?.setDataSource(Fname)
            player?.prepare()
            player?.start()

        } catch (e: Exception){
            e.printStackTrace()
        }

    }
    private fun closeAudio(){
        if(player != null){
            player?.release()
            player = null
        }
    }
    private fun pauseAudio(){
        if(player != null){
            position = player?.currentPosition!!
            player?.pause()
        }
    }
    private fun resumeAudio(){
        if(player != null && !player!!.isPlaying){
            player?.seekTo(position)
            player?.start()
        }
    }
    private fun stopAudio(){
        if(player!=null&& player!!.isPlaying){
            player?.stop()
        }
    }
}
