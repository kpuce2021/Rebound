package kr.ac.kpu.sleepwell

class noticeData {

    private var title: String? = null
    private var day: String? = null

    fun noticeData(
        title: String?,
        day: String?
    ) {
        this.title = title
        this.day = day
    }

    fun getTitleName(): String? {
        return title;
    }

    fun getDay(): String? {
        return day;
    }
}