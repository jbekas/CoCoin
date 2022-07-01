package com.jbekas.cocoin.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.jbekas.cocoin.fragment.TodayViewFragment

class TodayViewFragmentAdapter(fragmentActivity: FragmentActivity?) : FragmentStateAdapter(
    fragmentActivity!!) {
    override fun createFragment(position: Int): Fragment {
        return TodayViewFragment.newInstance(position)
    }

    override fun getItemCount(): Int {
        return TODAY_VIEW_FRAGMENT_NUMBER
    }

    companion object {
        private const val TODAY_VIEW_FRAGMENT_NUMBER = 8
    }
}