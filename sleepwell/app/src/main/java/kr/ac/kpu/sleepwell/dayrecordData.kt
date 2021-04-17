package kr.ac.kpu.sleepwell

class dayrecordData(val sleeptime:String,   //수면시간
                    val startsleeptime:String,  //수면시작시간
                    val timetotakesleeptime:String,  //잠드는데 소요된 시간
                    val finishsleep:String, //수면종료시간
                    val sleep_date:String,   //수면날짜
                    val Rem_sleep:String,   //램수면비율
                    val deep_sleep:String,  //깊은수면비율
                    val light_sleep:String,  //얉은수면비율
                    val awake_sleep:String //깬수면
                    ){}