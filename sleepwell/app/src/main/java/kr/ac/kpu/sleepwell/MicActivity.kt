package kr.ac.kpu.sleepwell

import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_mic.*
import java.io.File
import java.io.IOException

class MicActivity : AppCompatActivity() {

    private var Recorder : MediaRecorder? = null
    private var listener : MediaRecorder? = null
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
        initListener()
        Log.d("init","done")
        startListener()
        //var detect = Micdetect()
        //handler?.postDelayed(detect,1000)
        buttonRecord.setOnClickListener {
            stopListener()
            initrecorder()
            startRecord()
        }
        buttonStop.setOnClickListener {
            stopRecord()
            initListener()
            startListener()
        }

    }
    /*inner class Micdetect : Thread(){
        override fun run() {
            Log.d("micrun","start")
            if(getAmplitude()!! > 50){

                decibel = getAmplitude()
                decibelTV.text = "Decibel = $decibel"
                recordStart()
            }else {
                closeRecord()
                decibel = getAmplitude()
                decibelTV.text = "Decibel = $decibel"
                initrecorder()
            }
            handler?.postDelayed(this,1000)
        }
    }*/
    private fun initrecorder(){
        Recorder?.setAudioSource(
                MediaRecorder.AudioSource.MIC)
        Recorder?.setOutputFormat(
                MediaRecorder.OutputFormat.THREE_GPP)
        Recorder?.setAudioEncoder(
                MediaRecorder.AudioEncoder.DEFAULT)
    }
    private fun initListener(){
        listener?.setAudioSource(
                MediaRecorder.AudioSource.MIC)
        listener?.setOutputFormat(
                MediaRecorder.OutputFormat.THREE_GPP)
        listener?.setAudioEncoder(
                MediaRecorder.AudioEncoder.DEFAULT)
        listener?.setOutputFile("/dev/null")
    }

    private fun startRecord(){
        path = Environment.getDataDirectory()
        val newaudio = File(path,"recorded$audionum")
        Fname = newaudio.absolutePath
        Recorder?.setOutputFile(Fname)
        try {
            Recorder?.prepare()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        Recorder?.start()
        Log.d("Record path",Fname)
        Log.d("Recorder","started")
    }
    private fun startListener(){
        try {
            listener?.prepare()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        listener?.start()
        Log.d("listener","started")
    }

    private fun stopListener(){
        listener?.stop()
        listener?.release()
        Log.d("listener","stopped")
    }

    private fun stopRecord(){
        Recorder?.stop()
        Recorder?.release()
        audionum++
        Log.d("recorder","stopped")
    }

    fun getAmplitude(): Int? {
        return Recorder?.getMaxAmplitude()
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
