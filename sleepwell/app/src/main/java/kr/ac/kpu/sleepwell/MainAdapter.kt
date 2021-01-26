package kr.ac.kpu.sleepwell

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class MainAdapter(fm:FragmentManager): FragmentPagerAdapter(fm) {

    private val items: ArrayList<Fragment>
    private val itext: ArrayList<String>

    init {
        itext=ArrayList<String>()
        items = ArrayList<Fragment>()
        items.add(Fragment0())
        items.add(Fragment1())
        items.add(Fragment2())
        items.add(Fragment3())


        itext.add("수면 시작")
        itext.add("일일 수면")
        itext.add("트랜드")
        itext.add("마이페이지")
    }
    override fun getItem(position: Int): Fragment {
        return items[position]
    }
    override fun getCount(): Int {
        return items.size
    }
    override fun getPageTitle(position: Int): CharSequence? {
        return itext.get(position)
    }
}