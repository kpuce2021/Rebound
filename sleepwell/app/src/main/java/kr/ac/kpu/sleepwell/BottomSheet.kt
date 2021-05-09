package kr.ac.kpu.sleepwell

import android.content.Context
import android.view.View
import android.widget.CompoundButton
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.factor_dialog.*


class BottomSheet(context: Context) : BottomSheetDialog(context) {
    interface ClickListener{
        fun onClicked(stFactor: String, sii : ArrayList<Int>)
    }

    private lateinit var onClickListener: ClickListener

    fun setOnClickedListener(listener: ClickListener){
        onClickListener = listener
    }

    init {

        //R.layout.confirm_bottom_dialog 하단 다이어로그 생성 버튼을 눌렀을 때 보여질 레이아웃
        val view: View = layoutInflater.inflate(R.layout.factor_dialog, null)
        setContentView(view)
        val items = arrayOf("알코올", "카페인", "흡연", "야식", "운동", "감기", "수면 보조제", "샤워", "다른 침대")
        val selectedItemIndex = ArrayList<Int>()

        var listener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                when(buttonView.id){
                    R.id.f_alcohol ->{selectedItemIndex.add(0)}
                    R.id.f_caffeine ->{selectedItemIndex.add(1)}
                    R.id.f_smoke ->{selectedItemIndex.add(2)}
                    R.id.f_food ->{selectedItemIndex.add(3)}
                    R.id.f_workout ->{selectedItemIndex.add(4)}
                    R.id.f_cold ->{selectedItemIndex.add(5)}
                    R.id.f_pill ->{selectedItemIndex.add(6)}
                    R.id.f_shower ->{selectedItemIndex.add(7)}
                    R.id.f_otherbed ->{selectedItemIndex.add(8)}
                }
            }
            else{
                when(buttonView.id){
                    R.id.f_alcohol ->{selectedItemIndex.remove(0)}
                    R.id.f_caffeine ->{selectedItemIndex.remove(1)}
                    R.id.f_smoke ->{selectedItemIndex.remove(2)}
                    R.id.f_food ->{selectedItemIndex.remove(3)}
                    R.id.f_workout ->{selectedItemIndex.remove(4)}
                    R.id.f_cold ->{selectedItemIndex.remove(5)}
                    R.id.f_pill ->{selectedItemIndex.remove(6)}
                    R.id.f_shower ->{selectedItemIndex.remove(7)}
                    R.id.f_otherbed ->{selectedItemIndex.remove(8)}
                }
            }
        }

        f_alcohol.setOnCheckedChangeListener(listener)
        f_caffeine.setOnCheckedChangeListener(listener)
        f_smoke.setOnCheckedChangeListener(listener)
        f_food.setOnCheckedChangeListener(listener)
        f_workout.setOnCheckedChangeListener(listener)
        f_cold.setOnCheckedChangeListener(listener)
        f_pill.setOnCheckedChangeListener(listener)
        f_shower.setOnCheckedChangeListener(listener)
        f_otherbed.setOnCheckedChangeListener(listener)


        //확인 버튼
        button_bottom_sheet.setOnClickListener {
            var selected_factor = ArrayList<String>()
            var strFactor = ""
            for (j in selectedItemIndex) {
                selected_factor.add(items[j])
            }
            for(i in 0..selected_factor.size-1) {
                var x = selected_factor.get(i)
                if (i == selected_factor.size - 1) {
                    strFactor = strFactor.plus(x)
                } else {
                    strFactor = strFactor.plus(x + "/")
                }
            }
            onClickListener.onClicked(strFactor, selectedItemIndex)
            //다이어 로그 숨기기
            dismiss()
        }
    }
}

