package kr.ac.kpu.sleepwell

class dayrecordData(var sleeptime:String,   //총 수면시간 sleep_time
                    var startsleeptime:String,  //수면시작시간 go_to_bed
                    var timetotakesleeptime:String,  //잠드는데 소요된 시간 go_to_sleep
                    var finishsleep:String, //수면종료시간 wake_up
                    var sleep_date:String,   //수면날짜 sleep_date
                    var Rem_sleep:String,   //램수면비율 sleep_rem
                    var deep_sleep:String,  //깊은수면비율 sleep_deep
                    var light_sleep:String,  //얉은수면비율 sleep_light
                    var awake_sleep:String //깬수면 awake
                    )