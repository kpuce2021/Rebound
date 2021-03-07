package kr.ac.kpu.sleepwell

import android.app.Application
import java.io.File

class MyglobalArraylist:Application() {
    var arraylist=ArrayList<String>(20)   //녹음파일 이름 저장(output2)
    var timearraylist=ArrayList<String>(20) //시간 저장(녹음 파일)
    var filearraylist=ArrayList<File>(20)   //녹음파일 자체 저장
    var path: String="/mnt/sdcard/"

    fun AllarraylistInit(){
        for(i in 0..19){
            //arraylist[i]=i.toString()
            //timearraylist[i]=i.toString()
            arraylist.add(i,i.toString())
            timearraylist.add(i,i.toString())
            filearraylist.add(i,File(path,"file$i"))
        }
        arraylist.removeAll(arraylist)
        timearraylist.removeAll(timearraylist)
        filearraylist.removeAll(filearraylist)
    }
    /*fun arraylistSize():Int{
        return arraylist.size
    }
    fun timearraylistSize():Int{
        return timearraylist.size
    }
    fun filearraylistSize():Int{
        return filearraylist.size
    }
    fun ADDarraylist(filename:String){
        arraylist.add(filename)
    }
    fun ADDtimearraylist(timename:String){
        timearraylist.add(timename)
    }
    fun ADDFilearraylist(files:File){
        filearraylist.add(files)
    }
    fun getarraylist():ArrayList<String>{
        return arraylist
    }
    fun gettimearraylist():ArrayList<String>{
        return timearraylist
    }
    fun gettingfilearraylist():ArrayList<File>{
        return filearraylist
    }*/
}