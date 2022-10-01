package com.jbekas.cocoin.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.jbekas.cocoin.fragment.MonthlyViewFragment
import com.jbekas.cocoin.db.RecordManager
import timber.log.Timber
import java.util.*

// Todo optimize this
class MonthViewFragmentAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    var list = mutableListOf<MonthlyViewFragment>()
    private var monthNumber: Int
    private var startYear = 0
    private var startMonth = 0
    private var endYear = 0
    private var endMonth = 0
    var IS_EMPTY = false

    init {
        monthNumber = 0
        IS_EMPTY = RecordManager.RECORDS.isEmpty()
        Timber.d("IS_EMPTY: $IS_EMPTY")
        if (!IS_EMPTY) {
            startYear = RecordManager.RECORDS[0].calendar[Calendar.YEAR]
            endYear = RecordManager.RECORDS[RecordManager.RECORDS.size - 1].calendar[Calendar.YEAR]
            startMonth = RecordManager.RECORDS[0].calendar[Calendar.MONTH]
            endMonth =
                RecordManager.RECORDS[RecordManager.RECORDS.size - 1].calendar[Calendar.MONTH]
            monthNumber = (endYear - startYear) * 12 + endMonth - startMonth + 1
            for (i in 0 until monthNumber) {
                list.add(MonthlyViewFragment.newInstance(i, monthNumber))
            }
        } else {
        }
    }

//    override fun getItem(i: Int): Fragment {
//        return if (IS_EMPTY) MonthlyViewFragment.newInstance(0, -1) else list[i]
//    }
//
//    override fun getCount(): Int {
//        return if (IS_EMPTY) 1 else monthNumber
//    }

//    override fun getPageTitle(position: Int): CharSequence? {
//        if (IS_EMPTY) return ""
//        val nowMonth = (startMonth + (monthNumber - position - 1)) % 12
//        val nowYear = startYear + (startMonth + (monthNumber - position - 1)) / 12
//        return CoCoinUtil.GetMonthShort(nowMonth + 1) + " " + nowYear
//    }

    override fun getItemCount(): Int {
        return if (IS_EMPTY) 1 else monthNumber
    }

    override fun createFragment(position: Int): Fragment {
        return if (IS_EMPTY) {
            MonthlyViewFragment.newInstance(0, -1)
        } else {
            list[position]
        }
    }
}